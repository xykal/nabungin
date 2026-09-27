"""Ship AI artwork with uniform green background; size-conscious JPEG resources."""
from pathlib import Path
from PIL import Image
root = Path(__file__).parent
out = root.parent / 'app/src/main/res/drawable-nodpi'
out.mkdir(exist_ok=True)
for name in ('ill_coin_jar', 'ill_growth_stairs', 'ill_target_sprout', 'ill_calendar_coin', 'ill_lock_sprout'):
    im = Image.open(root / 'raw' / f'{name}.png').convert('RGB')
    im.thumbnail((960, 525), Image.Resampling.LANCZOS)
    im.save(out / f'{name}.jpg', 'JPEG', quality=81, optimize=True)
    print(name, (out / f'{name}.jpg').stat().st_size)
