#!/usr/bin/env python3
"""Adds a "Manual (PDF)" link to every control card in docs/index.html for
which a manual PDF exists.

Idempotent: cards that already link their manual are left untouched.
Run from anywhere:  python3 docs/manuals/src/link_manuals.py
"""

import re
from pathlib import Path

DOCS = Path(__file__).resolve().parent.parent.parent
INDEX = DOCS / "index.html"
MANUALS = DOCS / "manuals"

CARD = re.compile(r'<article class="control-card".*?</article>', re.S)
NAME = re.compile(r'data-name="([^"]+)"')
API_LINK = re.compile(r'(\n(\s*)<a class="card-api-link" href="api/[^"]+"[^>]*>API \u2192</a>)')


def kebab(name: str) -> str:
    out = []
    for index, char in enumerate(name):
        if index > 0 and char.isupper():
            previous = name[index - 1]
            following = name[index + 1] if index + 1 < len(name) else ""
            if not previous.isupper() or following.islower():
                out.append("-")
        out.append(char.lower())
    return "".join(out)


def main() -> int:
    html = INDEX.read_text(encoding="utf-8")
    added, missing = [], []

    def patch(match: re.Match) -> str:
        card = match.group(0)
        name_match = NAME.search(card)
        if not name_match:
            return card
        name = name_match.group(1)
        pdf = f"{kebab(name)}.pdf"
        if not (MANUALS / pdf).exists():
            missing.append(name)
            return card
        if f'manuals/{pdf}' in card:
            return card
        link_match = API_LINK.search(card)
        if not link_match:
            return card
        indent = link_match.group(2)
        replacement = (
            f'{link_match.group(1)}\n{indent}'
            f'<a class="card-api-link" href="manuals/{pdf}" target="_blank">Manual (PDF) \u2192</a>'
        )
        added.append(name)
        return card[: link_match.start()] + replacement + card[link_match.end():]

    html = CARD.sub(patch, html)
    INDEX.write_text(html, encoding="utf-8")

    print(f"linked {len(added)} manual(s)")
    if added:
        print("  added:   " + ", ".join(sorted(added)))
    if missing:
        print(f"  missing ({len(missing)}): " + ", ".join(sorted(missing)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
