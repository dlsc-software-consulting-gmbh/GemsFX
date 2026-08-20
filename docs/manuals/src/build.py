#!/usr/bin/env python3
"""Build GemsFX developer manuals.

Usage::

    ./.venv/bin/python build.py                 # build every manual
    ./.venv/bin/python build.py drawer_stack_pane

Each manual consists of two modules below ``manuals/``:

* ``<control>_graphics.py`` with a ``generate()`` function writing the cartoon
  SVGs into ``graphics/<control>/``
* ``<control>.py`` exposing a ``MANUAL`` object describing the content

The generated PDFs are written to ``docs/manuals/``.
"""

import argparse
import importlib
import pkgutil
import sys
from pathlib import Path

SRC_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(SRC_DIR))

import manuals  # noqa: E402
from manualkit import build  # noqa: E402


def _manual_modules():
    names = []
    for module in pkgutil.iter_modules(manuals.__path__):
        if not module.name.endswith("_graphics"):
            names.append(module.name)
    return sorted(names)


def build_manual(name: str) -> Path:
    try:
        graphics = importlib.import_module(f"manuals.{name}_graphics")
    except ModuleNotFoundError:
        graphics = None
    if graphics is not None and hasattr(graphics, "generate"):
        graphics.generate()

    module = importlib.import_module(f"manuals.{name}")
    path = build(module.MANUAL)
    print(f"  {module.MANUAL.control:<26} -> {path.relative_to(SRC_DIR.parent.parent)}")
    return path


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manuals", nargs="*", help="module names below manuals/ (default: all)")
    args = parser.parse_args()

    names = args.manuals or _manual_modules()
    if not names:
        print("no manuals found")
        return 1

    print(f"Building {len(names)} manual(s):")
    for name in names:
        build_manual(name)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
