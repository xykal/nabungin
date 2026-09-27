"""Extract selected 3D objects from AI green-screen art, consistently across app.
No remote runtime images. Preserve a soft edge, not the lime key color.
"""
from pathlib import Path
import cv2
import numpy as np
from PIL import Image
root = Path(__file__).resolve().parent
out = root.parent.parent/'app/src/main/res/drawable-nodpi'
out.mkdir(exist_ok=True)
for name, dst, width in [('logo_mascot','brand_mark',512),('ill_goal','art_goal',960),('ill_growth','art_growth',960)]:
    im = np.asarray(Image.open(root/f'{name}.png').convert('RGB'))
    h,w=im.shape[:2]
    # Green-screen chroma, learned from four corners. Force only border-connected green to transparent,
    # so green translucent glass at center remains part of the object.
    bg = np.median(np.concatenate([im[:55,:55].reshape(-1,3), im[:55,-55:].reshape(-1,3),
                                   im[-55:,:55].reshape(-1,3),im[-55:,-55:].reshape(-1,3)]),axis=0)
    diff=np.max(np.abs(im.astype('float32')-bg),axis=2)
    # Chroma condition handles AI's slight background color shifts and its soft cast shadow.
    chroma=(im[:,:,1].astype('int16')-im[:,:,2].astype('int16'))
    near=((diff<47) | ((chroma>124)&(im[:,:,0]>62))).astype('uint8')
    flood=np.zeros((h+2,w+2),dtype=np.uint8)
    connected=np.zeros_like(near)
    for x,y in [(0,0),(w-1,0),(0,h-1),(w-1,h-1)]:
        if near[y,x]:
            component=near.copy()
            cv2.floodFill(component,flood,(x,y),2)
            connected |= (component==2).astype('uint8')
            flood[:]=0
    # High confidence foreground and blurred edge; eliminate background hue fringe via alpha matting.
    alpha=(1-connected)*255
    # Anti-aliased cutout without leaking opaque green chroma into the composited art.
    alpha=cv2.GaussianBlur(alpha.astype('float32'),(3,3),0.6).astype('uint8')
    # Spill correction around semi-transparent silhouette; don't recolor the glass interior.
    if name=='logo_mascot':
        # Remove the AI motion blur only where it falls in the background above the lid.
        yy,xx=np.indices(alpha.shape)
        blur=(xx>435)&(xx<610)&(yy<285)&(im[:,:,0]<205)&(im[:,:,1]>140)
        alpha[blur]=0
    semi=(alpha>0)&(alpha<245)
    clean=im.copy()
    clean[:,:,1][semi]=np.minimum(clean[:,:,1][semi],(clean[:,:,0][semi].astype('int16')+55).clip(0,255)).astype('uint8')
    rgba=np.dstack([clean,alpha])
    image=Image.fromarray(rgba,'RGBA')
    bb=image.getbbox()
    image=image.crop(bb)
    if name=='logo_mascot':
        image.thumbnail((width,width),Image.Resampling.LANCZOS)
        canvas=Image.new('RGBA',(768,768))
        canvas.alpha_composite(image,((768-image.width)//2,(768-image.height)//2))
        image=canvas
    else:
        image.thumbnail((width,560),Image.Resampling.LANCZOS)
        canvas=Image.new('RGBA',(960,560))
        canvas.alpha_composite(image,((960-image.width)//2,(560-image.height)//2))
        image=canvas
    path=out/f'{dst}.png'; image.save(path,optimize=True)
    print(dst,'bbox',bb,'bytes',path.stat().st_size,'background',bg)
