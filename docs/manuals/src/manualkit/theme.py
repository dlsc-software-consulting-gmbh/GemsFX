"""Central definition of the visual identity shared by all GemsFX manuals.

Every colour, font and metric used by a manual is defined here. Content modules
never define styling of their own, which is what keeps the manuals consistent.
"""

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm

PAGE_SIZE = A4
PAGE_WIDTH, PAGE_HEIGHT = PAGE_SIZE

MARGIN_LEFT = 22 * mm
MARGIN_RIGHT = 22 * mm
MARGIN_TOP = 24 * mm
MARGIN_BOTTOM = 20 * mm

CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

# The palette is derived from the GemsFX documentation site (docs/css/style.css)
# and shared with the cartoon graphics (see manualkit/svgstyle.py).
INK = colors.HexColor("#1F2937")
INK_SOFT = colors.HexColor("#475569")
MUTED = colors.HexColor("#64748B")
ACCENT = colors.HexColor("#4F46E5")
ACCENT_LIGHT = colors.HexColor("#818CF8")
ACCENT_PALE = colors.HexColor("#EEF0FF")
TEAL = colors.HexColor("#0D9488")
TEAL_PALE = colors.HexColor("#E6FAF7")
AMBER = colors.HexColor("#D97706")
AMBER_PALE = colors.HexColor("#FEF6E7")
RULE = colors.HexColor("#D8DEE9")
TABLE_HEAD_BG = colors.HexColor("#EEF0FF")
TABLE_ROW_BG = colors.HexColor("#F8FAFC")
CODE_BG = colors.HexColor("#F5F7FA")
CODE_BORDER = colors.HexColor("#E2E8F0")

# Only the PDF base-14 fonts are used, so the manuals stay small, portable and
# free of font licensing concerns.
FONT_BODY = "Helvetica"
FONT_BOLD = "Helvetica-Bold"
FONT_ITALIC = "Helvetica-Oblique"
FONT_MONO = "Courier"
FONT_MONO_BOLD = "Courier-Bold"

BODY_SIZE = 9.8
BODY_LEADING = 14.2
CODE_SIZE = 8.2
CODE_LEADING = 11.2
TABLE_SIZE = 8.4
TABLE_LEADING = 11.0

PROJECT_NAME = "GemsFX"
PROJECT_URL = "https://github.com/dlsc-software-consulting-gmbh/GemsFX"
PROJECT_TAGLINE = "JavaFX Custom Controls"
