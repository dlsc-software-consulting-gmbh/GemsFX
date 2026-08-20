"""Renders a :class:`manualkit.content.Manual` into a PDF.

All layout decisions live here so that every control manual comes out with an
identical look and feel: cover page, table of contents with dot leaders, running
header and footer, numbered chapters and sections, code blocks, property tables
and cartoon figures.
"""

from pathlib import Path
from typing import List

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import mm
from reportlab.lib.utils import ImageReader
from reportlab.platypus import (
    BaseDocTemplate,
    CondPageBreak,
    Flowable,
    Frame,
    Image,
    KeepTogether,
    ListFlowable,
    ListItem,
    NextPageTemplate,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)
from reportlab.platypus.tableofcontents import TableOfContents

from . import content as C
from . import theme as T
from .raster import rasterize

OUTPUT_DIR = Path(__file__).resolve().parent.parent.parent


# ---------------------------------------------------------------------------
# Paragraph styles
# ---------------------------------------------------------------------------


def _styles():
    body = ParagraphStyle(
        "Body",
        fontName=T.FONT_BODY,
        fontSize=T.BODY_SIZE,
        leading=T.BODY_LEADING,
        textColor=T.INK,
        alignment=TA_JUSTIFY,
        spaceAfter=6,
    )
    return {
        "body": body,
        "bullet": ParagraphStyle("Bullet", parent=body, alignment=0, spaceAfter=2),
        "chapter": ParagraphStyle(
            "Chapter",
            fontName=T.FONT_BOLD,
            fontSize=16,
            leading=20,
            textColor=T.ACCENT,
            spaceBefore=4,
            spaceAfter=9,
        ),
        "section": ParagraphStyle(
            "Section",
            fontName=T.FONT_BOLD,
            fontSize=11,
            leading=15,
            textColor=T.INK,
            spaceBefore=10,
            spaceAfter=4,
        ),
        "code": ParagraphStyle(
            "Code",
            fontName=T.FONT_MONO,
            fontSize=T.CODE_SIZE,
            leading=T.CODE_LEADING,
            textColor=T.INK,
        ),
        "cell": ParagraphStyle(
            "Cell",
            fontName=T.FONT_BODY,
            fontSize=T.TABLE_SIZE,
            leading=T.TABLE_LEADING,
            textColor=T.INK,
        ),
        "cellhead": ParagraphStyle(
            "CellHead",
            fontName=T.FONT_BOLD,
            fontSize=T.TABLE_SIZE,
            leading=T.TABLE_LEADING,
            textColor=T.ACCENT,
        ),
        "caption": ParagraphStyle(
            "Caption",
            fontName=T.FONT_ITALIC,
            fontSize=8.4,
            leading=11,
            textColor=T.MUTED,
            alignment=TA_CENTER,
            spaceBefore=4,
            spaceAfter=8,
        ),
        "callout": ParagraphStyle(
            "Callout",
            fontName=T.FONT_BODY,
            fontSize=9.2,
            leading=13,
            textColor=T.INK,
        ),
        "toc0": ParagraphStyle(
            "Toc0", fontName=T.FONT_BOLD, fontSize=10.4, leading=17, textColor=T.INK, spaceBefore=6
        ),
        "toc1": ParagraphStyle(
            "Toc1", fontName=T.FONT_BODY, fontSize=9.2, leading=13.6, textColor=T.INK_SOFT, leftIndent=14
        ),
    }


class _Rule(Flowable):
    """A thin horizontal rule used to separate blocks."""

    def __init__(self, width, thickness=0.7, colour=T.RULE, space=3):
        super().__init__()
        self.width = width
        self.thickness = thickness
        self.colour = colour
        self.height = thickness + space

    def draw(self):
        self.canv.setStrokeColor(self.colour)
        self.canv.setLineWidth(self.thickness)
        self.canv.line(0, self.height / 2, self.width, self.height / 2)


# ---------------------------------------------------------------------------
# Page furniture
# ---------------------------------------------------------------------------


class _ManualDoc(BaseDocTemplate):
    def __init__(self, path: Path, manual: C.Manual):
        super().__init__(
            str(path),
            pagesize=T.PAGE_SIZE,
            leftMargin=T.MARGIN_LEFT,
            rightMargin=T.MARGIN_RIGHT,
            topMargin=T.MARGIN_TOP,
            bottomMargin=T.MARGIN_BOTTOM,
            title=f"{manual.control} \u2013 Developer Manual",
            author=T.PROJECT_NAME,
            subject=f"{T.PROJECT_NAME} {manual.control} control",
            creator=f"{T.PROJECT_NAME} manual generator",
        )
        self.manual = manual
        frame = Frame(
            self.leftMargin,
            self.bottomMargin,
            self.width,
            self.height,
            id="content",
            leftPadding=0,
            rightPadding=0,
            topPadding=0,
            bottomPadding=0,
        )
        cover_frame = Frame(
            self.leftMargin,
            self.bottomMargin,
            self.width,
            self.height + T.MARGIN_TOP - 18 * mm,
            id="cover",
            leftPadding=0,
            rightPadding=0,
            topPadding=0,
            bottomPadding=0,
        )
        self.addPageTemplates(
            [
                PageTemplate(id="cover", frames=[cover_frame], onPage=self._cover_page),
                PageTemplate(id="content", frames=[frame], onPage=self._content_page),
            ]
        )

    def _cover_page(self, canvas, doc):
        canvas.saveState()
        canvas.setFillColor(T.ACCENT)
        canvas.rect(0, T.PAGE_HEIGHT - 9 * mm, T.PAGE_WIDTH, 9 * mm, stroke=0, fill=1)
        canvas.setFillColor(T.ACCENT_LIGHT)
        canvas.rect(0, 0, T.PAGE_WIDTH, 4 * mm, stroke=0, fill=1)
        canvas.setFont(T.FONT_BODY, 7.6)
        canvas.setFillColor(T.MUTED)
        canvas.drawCentredString(
            T.PAGE_WIDTH / 2,
            9 * mm,
            f"{T.PROJECT_NAME} \u2013 {T.PROJECT_TAGLINE} \u2013 {T.PROJECT_URL}",
        )
        canvas.restoreState()

    def _content_page(self, canvas, doc):
        canvas.saveState()
        canvas.setFont(T.FONT_BODY, 7.8)
        canvas.setFillColor(T.MUTED)
        top = T.PAGE_HEIGHT - T.MARGIN_TOP + 7 * mm
        canvas.drawString(T.MARGIN_LEFT, top, f"{self.manual.control} \u2013 Developer Manual")
        canvas.drawRightString(T.PAGE_WIDTH - T.MARGIN_RIGHT, top, T.PROJECT_NAME)
        canvas.setStrokeColor(T.RULE)
        canvas.setLineWidth(0.6)
        canvas.line(T.MARGIN_LEFT, top - 2.5 * mm, T.PAGE_WIDTH - T.MARGIN_RIGHT, top - 2.5 * mm)

        bottom = T.MARGIN_BOTTOM - 7 * mm
        canvas.line(T.MARGIN_LEFT, bottom + 4 * mm, T.PAGE_WIDTH - T.MARGIN_RIGHT, bottom + 4 * mm)
        canvas.drawString(T.MARGIN_LEFT, bottom, T.PROJECT_URL)
        canvas.drawRightString(T.PAGE_WIDTH - T.MARGIN_RIGHT, bottom, f"Page {doc.page - 1}")
        canvas.restoreState()

    def afterFlowable(self, flowable):
        """Feed headings into the table of contents."""
        level = getattr(flowable, "_toc_level", None)
        if level is None:
            return
        key = getattr(flowable, "_toc_key")
        self.canv.bookmarkPage(key)
        self.notify("TOCEntry", (level, flowable.getPlainText(), self.page - 1, key))


# ---------------------------------------------------------------------------
# Block rendering
# ---------------------------------------------------------------------------


class _Renderer:
    def __init__(self, manual: C.Manual):
        self.manual = manual
        self.s = _styles()
        self.width = T.CONTENT_WIDTH
        self._key = 0

    # -- helpers -----------------------------------------------------------

    def _heading(self, text: str, level: int) -> Paragraph:
        self._key += 1
        key = f"h{self._key}"
        style = self.s["chapter"] if level == 0 else self.s["section"]
        para = Paragraph(text, style)
        para._toc_level = level
        para._toc_key = key
        return para

    def _figure(self, svg: str, caption: str, width: float = None) -> List:
        png = rasterize(svg)
        reader = ImageReader(str(png))
        native_w, native_h = reader.getSize()
        target_w = width or self.width
        target_w = min(target_w, self.width)
        image = Image(str(png), width=target_w, height=target_w * native_h / native_w)
        image.hAlign = "CENTER"
        parts = [Spacer(1, 4), image]
        if caption:
            parts.append(Paragraph(caption, self.s["caption"]))
        else:
            parts.append(Spacer(1, 8))
        return parts

    def _code(self, block: C.Code) -> List:
        lines = block.source.strip("\n").rstrip().split("\n")
        text = "<br/>".join(
            line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;")
            for line in lines
        )
        table = Table(
            [[Paragraph(text, self.s["code"])]],
            colWidths=[self.width],
            style=TableStyle(
                [
                    ("BACKGROUND", (0, 0), (-1, -1), T.CODE_BG),
                    ("BOX", (0, 0), (-1, -1), 0.7, T.CODE_BORDER),
                    ("LINEBEFORE", (0, 0), (0, -1), 2.4, T.ACCENT_LIGHT),
                    ("LEFTPADDING", (0, 0), (-1, -1), 9),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 8),
                    ("TOPPADDING", (0, 0), (-1, -1), 7),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
                ]
            ),
        )
        parts = [table]
        if block.caption:
            parts.append(Paragraph(block.caption, self.s["caption"]))
        else:
            parts.append(Spacer(1, 8))
        return parts

    def _table(self, headers, rows, widths, caption) -> List:
        data = [[Paragraph(h, self.s["cellhead"]) for h in headers]]
        for row in rows:
            data.append([Paragraph(cell, self.s["cell"]) for cell in row])

        if widths:
            total = sum(widths)
            col_widths = [self.width * w / total for w in widths]
        else:
            col_widths = [self.width / len(headers)] * len(headers)

        style = [
            ("BACKGROUND", (0, 0), (-1, 0), T.TABLE_HEAD_BG),
            ("LINEBELOW", (0, 0), (-1, 0), 0.9, T.ACCENT_LIGHT),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 6),
            ("RIGHTPADDING", (0, 0), (-1, -1), 6),
            ("TOPPADDING", (0, 0), (-1, -1), 5),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
            ("GRID", (0, 0), (-1, -1), 0.4, T.RULE),
        ]
        for index in range(1, len(data)):
            if index % 2 == 0:
                style.append(("BACKGROUND", (0, index), (-1, index), T.TABLE_ROW_BG))

        table = Table(data, colWidths=col_widths, style=TableStyle(style), repeatRows=1)
        parts = [table]
        if caption:
            parts.append(Paragraph(caption, self.s["caption"]))
        else:
            parts.append(Spacer(1, 9))
        return parts

    def _callout(self, block: C.Callout) -> List:
        palette = {
            "note": (T.ACCENT_PALE, T.ACCENT, "Note"),
            "tip": (T.TEAL_PALE, T.TEAL, "Tip"),
            "warning": (T.AMBER_PALE, T.AMBER, "Careful"),
        }
        background, accent, title = palette.get(block.kind, palette["note"])
        text = f'<font color="#{accent.hexval()[2:]}"><b>{title}.</b></font> {block.text}'
        table = Table(
            [[Paragraph(text, self.s["callout"])]],
            colWidths=[self.width],
            style=TableStyle(
                [
                    ("BACKGROUND", (0, 0), (-1, -1), background),
                    ("LINEBEFORE", (0, 0), (0, -1), 3.0, accent),
                    ("LEFTPADDING", (0, 0), (-1, -1), 10),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                    ("TOPPADDING", (0, 0), (-1, -1), 8),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
                ]
            ),
        )
        return [table, Spacer(1, 9)]

    # -- assembly ----------------------------------------------------------

    def cover(self) -> List:
        story: List = [Spacer(1, 26 * mm)]
        story.append(
            Paragraph(
                self.manual.control,
                ParagraphStyle(
                    "CoverTitle",
                    fontName=T.FONT_BOLD,
                    fontSize=34,
                    leading=38,
                    textColor=T.ACCENT,
                    alignment=TA_CENTER,
                ),
            )
        )
        story.append(
            Paragraph(
                "Developer Manual",
                ParagraphStyle(
                    "CoverSub",
                    fontName=T.FONT_BODY,
                    fontSize=17,
                    leading=22,
                    textColor=T.INK,
                    alignment=TA_CENTER,
                    spaceBefore=2,
                ),
            )
        )
        story.append(Spacer(1, 5 * mm))
        story.append(
            Paragraph(
                f"{T.PROJECT_NAME} &nbsp;\u2022&nbsp; {self.manual.package} &nbsp;\u2022&nbsp; {T.PROJECT_TAGLINE}",
                ParagraphStyle(
                    "CoverMeta",
                    fontName=T.FONT_BODY,
                    fontSize=9.4,
                    leading=13,
                    textColor=T.MUTED,
                    alignment=TA_CENTER,
                ),
            )
        )
        story.append(Spacer(1, 9 * mm))
        story.extend(self._figure(self.manual.cover_svg, self.manual.cover_caption, width=self.width))
        story.append(Spacer(1, 6 * mm))
        story.append(
            Paragraph(
                self.manual.abstract,
                ParagraphStyle(
                    "CoverAbstract",
                    fontName=T.FONT_BODY,
                    fontSize=10.4,
                    leading=15.5,
                    textColor=T.INK_SOFT,
                    alignment=TA_CENTER,
                    leftIndent=14 * mm,
                    rightIndent=14 * mm,
                ),
            )
        )
        story.append(NextPageTemplate("content"))
        story.append(PageBreak())
        return story

    def toc(self) -> List:
        toc = TableOfContents()
        toc.levelStyles = [self.s["toc0"], self.s["toc1"]]
        toc.dotsMinLevel = 0
        return [
            Paragraph(
                "Table of Contents",
                ParagraphStyle(
                    "TocTitle",
                    fontName=T.FONT_BOLD,
                    fontSize=15,
                    leading=19,
                    textColor=T.ACCENT,
                    spaceAfter=8,
                ),
            ),
            _Rule(self.width),
            Spacer(1, 5),
            toc,
            PageBreak(),
        ]

    def chapters(self) -> List:
        story: List = []
        for chapter_no, chapter in enumerate(self.manual.chapters, start=1):
            if chapter_no > 1:
                story.append(CondPageBreak(150))
                story.append(Spacer(1, 8))
            story.append(self._heading(f"{chapter_no}. {chapter.title}", 0))
            section_no = 0
            for block in chapter.blocks:
                if isinstance(block, C.Section):
                    section_no += 1
                    story.append(CondPageBreak(46))
                    story.append(self._heading(f"{chapter_no}.{section_no} {block.title}", 1))
                elif isinstance(block, C.Para):
                    story.append(Paragraph(block.text, self.s["body"]))
                elif isinstance(block, C.Bullets):
                    story.append(
                        ListFlowable(
                            [ListItem(Paragraph(item, self.s["bullet"]), leftIndent=14) for item in block.items],
                            bulletType="bullet",
                            bulletFontSize=6,
                            bulletColor=T.ACCENT,
                            start="circle",
                            leftIndent=13,
                        )
                    )
                    story.append(Spacer(1, 7))
                elif isinstance(block, C.Numbered):
                    story.append(
                        ListFlowable(
                            [ListItem(Paragraph(item, self.s["bullet"]), leftIndent=16) for item in block.items],
                            bulletType="1",
                            bulletFontName=T.FONT_BOLD,
                            bulletFontSize=T.BODY_SIZE,
                            bulletColor=T.ACCENT,
                            leftIndent=15,
                        )
                    )
                    story.append(Spacer(1, 7))
                elif isinstance(block, C.Code):
                    parts = self._code(block)
                    # Long snippets are allowed to split, otherwise they would
                    # leave large gaps at the bottom of a page.
                    story.extend(parts if len(block.source.strip().split("\n")) > 13 else [KeepTogether(parts)])
                elif isinstance(block, C.PropertyTable):
                    story.extend(
                        self._table(
                            ["Property", "Type", "Default", "Description"],
                            [[r.name, r.type, r.default, r.description] for r in block.rows],
                            [24, 21, 15, 40],
                            block.caption,
                        )
                    )
                elif isinstance(block, C.Table):
                    story.extend(self._table(block.headers, block.rows, block.widths, block.caption))
                elif isinstance(block, C.Figure):
                    story.append(KeepTogether(self._figure(block.svg, block.caption, block.width)))
                elif isinstance(block, C.Callout):
                    story.append(KeepTogether(self._callout(block)))
                elif isinstance(block, C.Spacer):
                    story.append(Spacer(1, block.height))
                elif isinstance(block, C.PageBreak):
                    story.append(PageBreak())
                else:
                    raise TypeError(f"unsupported block: {block!r}")
        return story


def build(manual: C.Manual, output_dir: Path = OUTPUT_DIR) -> Path:
    """Render ``manual`` and return the path of the generated PDF."""
    output_dir.mkdir(parents=True, exist_ok=True)
    path = output_dir / manual.file_name
    renderer = _Renderer(manual)
    doc = _ManualDoc(path, manual)
    story = renderer.cover() + renderer.toc() + renderer.chapters()
    doc.multiBuild(story)
    return path
