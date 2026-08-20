"""Content model for GemsFX developer manuals.

A manual is declared as data: a :class:`Manual` holding :class:`Chapter` objects,
each of which holds a list of content blocks. Rendering and layout happen
exclusively in :mod:`manualkit.document`, so a content module never contains
styling decisions.
"""

from dataclasses import dataclass, field
from typing import List, Optional, Sequence


class Block:
    """Marker base class for everything that can appear inside a chapter."""


@dataclass
class Section(Block):
    """A numbered sub heading (``1.1``, ``1.2``, ...)."""

    title: str


@dataclass
class Para(Block):
    """A paragraph of body text. Supports ReportLab inline markup."""

    text: str


@dataclass
class Bullets(Block):
    items: Sequence[str]


@dataclass
class Numbered(Block):
    items: Sequence[str]


@dataclass
class Code(Block):
    """A Java / CSS source snippet."""

    source: str
    caption: Optional[str] = None


@dataclass
class Property(Block):
    """One row of a property table."""

    name: str
    type: str
    default: str
    description: str


@dataclass
class PropertyTable(Block):
    rows: Sequence[Property]
    caption: Optional[str] = None


@dataclass
class Table(Block):
    """A generic table with a free choice of column headers."""

    headers: Sequence[str]
    rows: Sequence[Sequence[str]]
    widths: Optional[Sequence[float]] = None
    caption: Optional[str] = None


@dataclass
class Figure(Block):
    """A cartoon illustration, referenced by its file name below ``graphics/``."""

    svg: str
    caption: str
    width: Optional[float] = None


@dataclass
class Callout(Block):
    """A highlighted note, tip or warning box."""

    text: str
    kind: str = "note"  # note | tip | warning


@dataclass
class Spacer(Block):
    height: float = 6.0


@dataclass
class PageBreak(Block):
    pass


@dataclass
class Chapter:
    title: str
    blocks: List[Block] = field(default_factory=list)


@dataclass
class Manual:
    """Everything needed to render one control manual."""

    control: str
    package: str
    subtitle: str
    abstract: str
    cover_svg: str
    cover_caption: str
    chapters: List[Chapter] = field(default_factory=list)

    @property
    def file_name(self) -> str:
        """Kebab-case PDF file name derived from the control class name."""
        out = []
        for index, char in enumerate(self.control):
            if index > 0 and char.isupper():
                previous = self.control[index - 1]
                following = self.control[index + 1] if index + 1 < len(self.control) else ""
                # start of a new word, or the last letter of an acronym
                if not previous.isupper() or following.islower():
                    out.append("-")
            out.append(char.lower())
        return "".join(out) + ".pdf"
