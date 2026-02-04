#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image


def place_center(img: Image.Image, size: int, scale: float) -> Image.Image:
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    w = max(1, int(size * scale))
    resized = img.resize((w, w), Image.LANCZOS)
    x = (size - w) // 2
    y = (size - w) // 2
    canvas.paste(resized, (x, y), resized)
    return canvas


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Generate launcher icons from a source PNG."
    )
    parser.add_argument(
        "source",
        nargs="?",
        default="cursor.png",
        help="Path to source PNG (default: cursor.png)",
    )
    parser.add_argument(
        "--scale",
        type=float,
        default=0.4,
        help="Scale for icon inside the square (default: 0.4)",
    )
    parser.add_argument(
        "--res-dir",
        default="app/src/main/res",
        help="Resources directory (default: app/src/main/res)",
    )
    args = parser.parse_args()

    root = Path.cwd()
    src_path = (root / args.source).resolve()
    res_dir = (root / args.res_dir).resolve()

    if not src_path.exists():
        raise SystemExit(f"Source image not found: {src_path}")

    src = Image.open(src_path).convert("RGBA")

    foreground_sizes = {
        "drawable-mdpi": 108,
        "drawable-hdpi": 162,
        "drawable-xhdpi": 216,
        "drawable-xxhdpi": 324,
        "drawable-xxxhdpi": 432,
    }
    for folder, size in foreground_sizes.items():
        out_dir = res_dir / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        out_img = place_center(src, size, args.scale)
        out_img.save(out_dir / "ic_launcher_foreground.png", format="PNG")

    mipmap_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, size in mipmap_sizes.items():
        out_dir = res_dir / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        out_img = place_center(src, size, args.scale)
        out_img.save(out_dir / "ic_launcher.png", format="PNG")
        out_img.save(out_dir / "ic_launcher_round.png", format="PNG")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
