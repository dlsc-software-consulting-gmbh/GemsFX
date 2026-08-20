"""Rasterization of the cartoon SVG graphics for embedding into the PDF.

ReportLab cannot embed SVG directly, so the graphics are converted to high
resolution PNGs via ``rsvg-convert``. Results are cached in ``.raster-cache``
(git-ignored) and only regenerated when the SVG changes.
"""

import hashlib
import shutil
import subprocess
from pathlib import Path

SRC_DIR = Path(__file__).resolve().parent.parent
GRAPHICS_DIR = SRC_DIR / "graphics"
CACHE_DIR = SRC_DIR / ".raster-cache"

# Rendering density of the cartoon graphics inside the PDF.
DPI = 300


def _converter() -> str:
    for candidate in ("rsvg-convert", "cairosvg", "inkscape"):
        if shutil.which(candidate):
            return candidate
    raise RuntimeError(
        "No SVG converter found. Install one of: rsvg-convert (brew install librsvg), "
        "cairosvg (pip install cairosvg) or inkscape."
    )


def rasterize(svg_name: str) -> Path:
    """Convert ``graphics/<svg_name>`` to a PNG and return the PNG path."""
    svg_path = GRAPHICS_DIR / svg_name
    if not svg_path.exists():
        raise FileNotFoundError(f"missing graphic: {svg_path}")

    digest = hashlib.sha1(svg_path.read_bytes()).hexdigest()[:12]
    CACHE_DIR.mkdir(exist_ok=True)
    png_path = CACHE_DIR / f"{svg_path.stem}-{digest}.png"
    if png_path.exists():
        return png_path

    tool = _converter()
    if tool == "rsvg-convert":
        cmd = [tool, "--dpi-x", str(DPI), "--dpi-y", str(DPI), "-o", str(png_path), str(svg_path)]
    elif tool == "cairosvg":
        cmd = [tool, str(svg_path), "-o", str(png_path), "--dpi", str(DPI)]
    else:
        cmd = [tool, str(svg_path), "--export-type=png", f"--export-dpi={DPI}", f"--export-filename={png_path}"]

    subprocess.run(cmd, check=True, capture_output=True)
    return png_path
