"""Extract flat logo from AI green screen, keep mark inside adaptive icon safe zone."""
from pathlib import Path
from PIL import Image
root = Path(__file__).parent
im = Image.open(root/'raw/logo_mark.png').convert('RGB')
# Upper-center coin mark; exclude wide backdrop around the artwork.
im = im.crop((460, 70, 980, 700))
im.thumbnail((255, 310), Image.Resampling.LANCZOS)
canvas = Image.new('RGBA', (432, 432))
for y in range(im.height):
    for x in range(im.width):
        r,g,b = im.getpixel((x,y))
        # Flat background is moss green, with ±few points of AI texture.
        signal = max(r-g, min(r,g,b)-115)
        alpha = max(0, min(255, int((signal+8)*255/20)))
        canvas.putpixel(((432-im.width)//2+x,(432-im.height)//2+y),(r,g,b,alpha))
output = root.parent/'app/src/main/res/drawable-nodpi/logo_ai_foreground.png'
canvas.save(output,optimize=True)
print(output, output.stat().st_size)
