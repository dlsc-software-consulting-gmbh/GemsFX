"""Generates the cartoon illustrations for the DockingPane manual."""

from pathlib import Path

from manualkit.svgstyle import (
    AMBER,
    INDIGO,
    INDIGO_PALE,
    INK,
    INK_SOFT,
    MUTED,
    PAPER,
    ROSE,
    SLATE_PALE,
    TEAL,
    TEAL_PALE,
    Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "docking-pane"


def _tab(s: Sketch, x, y, w, title, selected=False):
    fill = PAPER if selected else SLATE_PALE
    s.box(x, y, w, 34, fill=fill, stroke=INK, radius=8)
    s.text(x + 14, y + 22, title, size=17, fill=INK, anchor="start")
    s.box(x + w - 22, y + 10, 10, 10, fill=MUTED, stroke=INK, radius=2)


def _pane_shell(s: Sketch, x, y, w, h, title="DockingPane"):
    s.box(x, y, w, h, fill=PAPER, stroke=INK, radius=12, shadow=True)
    s.box(x + 10, y + 10, w - 20, 42, fill=INDIGO_PALE, stroke=INDIGO, radius=10)
    s.text(x + 24, y + 36, title, size=19, fill=INDIGO, anchor="start")


def cover() -> Sketch:
    s = Sketch(1060, 520, seed=101)
    s.window(45, 30, 970, 455, title="Workstation")

    _pane_shell(s, 90, 78, 620, 340)
    _tab(s, 118, 144, 122, "Editor", selected=True)
    _tab(s, 246, 144, 122, "Preview")
    _tab(s, 374, 144, 130, "Console")
    s.box(120, 184, 278, 206, fill=TEAL_PALE, stroke=TEAL, radius=10)
    s.text(259, 294, "center", size=23, fill=TEAL)
    s.box(404, 184, 278, 96, fill=SLATE_PALE, stroke=INK_SOFT, radius=10)
    s.text(543, 241, "right split", size=20, fill=INK_SOFT)
    s.box(404, 292, 278, 98, fill=SLATE_PALE, stroke=INK_SOFT, radius=10)
    s.text(543, 350, "bottom split", size=20, fill=INK_SOFT)

    _pane_shell(s, 742, 122, 236, 240, title="Floating")
    _tab(s, 764, 186, 156, "Inspector", selected=True)
    s.box(764, 226, 194, 116, fill=TEAL_PALE, stroke=TEAL, radius=10)

    s.arrow(684, 214, 742, 222, bend=0.06, stroke=AMBER, width=3, head=12)
    s.cursor(694, 205, scale=1.15)
    s.label(528, 450, "Drag tabs to split, merge, or detach into app-created stages", size=20, fill=AMBER)
    return s


def anatomy() -> Sketch:
    s = Sketch(1140, 560, seed=102)

    x, y, w, h = 55, 42, 640, 460
    _pane_shell(s, x, y, w, h)
    _tab(s, x + 28, y + 66, 130, "Navigator")
    _tab(s, x + 164, y + 66, 124, "Editor", selected=True)
    _tab(s, x + 294, y + 66, 130, "Preview")

    s.box(x + 28, y + 108, 278, 176, fill=TEAL_PALE, stroke=TEAL, radius=10)
    s.text(x + 168, y + 208, "tab group", size=21, fill=TEAL)
    s.box(x + 318, y + 108, 294, 176, fill=SLATE_PALE, stroke=INK_SOFT, radius=10)
    s.text(x + 464, y + 208, "second group", size=21, fill=INK_SOFT)
    s.box(x + 28, y + 300, 584, 180, fill=INDIGO_PALE, stroke=INDIGO, radius=10)
    s.text(x + 320, y + 397, "bottom split child", size=22, fill=INDIGO)

    legend_x = 748
    entries = [
        ("1", x + 24, y + 24, "DockingPane root (Control)", 110),
        ("2", x + 94, y + 84, "Custom tab strip + title tabs", 168),
        ("3", x + 168, y + 198, "Tabs layout node", 226),
        ("4", x + 464, y + 198, "Sibling tabs in same split", 284),
        ("5", x + 320, y + 296, "Split divider creates second region", 342),
        ("6", x + 328, y + 398, "Selected dockable content", 400),
        ("7", x + 586, y + 118, "Context tracks IDs + active pane", 458),
    ]

    for key, px, py, text, ly in entries:
        s.badge(legend_x, ly, key, fill=INDIGO, size=16)
        s.text(legend_x + 30, ly + 5, text, size=18, fill=INK, anchor="start")
        s.arrow(legend_x - 18, ly, px, py, bend=0.03, stroke=MUTED, width=1.7, head=9)

    return s


def interactions() -> Sketch:
    s = Sketch(1040, 430, seed=103)

    frames = [
        (42, "tab reorder", "CENTER"),
        (365, "edge split", "RIGHT"),
        (688, "cross-pane move", "LEFT"),
    ]

    for x, caption, zone in frames:
        _pane_shell(s, x, 50, 300, 245, title="Pane")
        _tab(s, x + 24, 108, 102, "A")
        _tab(s, x + 130, 108, 102, "B", selected=True)
        s.box(x + 24, 148, 250, 124, fill=TEAL_PALE, stroke=TEAL, radius=10)
        if zone == "CENTER":
            s.box(x + 114, 190, 80, 40, fill=AMBER, stroke=AMBER, radius=8, opacity=0.35)
        elif zone == "RIGHT":
            s.box(x + 204, 148, 70, 124, fill=AMBER, stroke=AMBER, radius=8, opacity=0.35)
        else:
            s.box(x + 24, 148, 70, 124, fill=AMBER, stroke=AMBER, radius=8, opacity=0.35)
        s.cursor(x + 156, 124, scale=1.0)
        s.label(x + 150, 332, caption, size=18, fill=INK_SOFT)

    s.arrow(330, 176, 355, 176, bend=0.0, stroke=ROSE, width=2.4, head=11)
    s.arrow(653, 176, 680, 176, bend=0.0, stroke=ROSE, width=2.4, head=11)
    s.label(520, 390, "Drop zones resolve to CENTER / LEFT / RIGHT / TOP / BOTTOM targets", size=19, fill=AMBER)
    return s


def floating_stage() -> Sketch:
    s = Sketch(1060, 470, seed=104)
    s.window(44, 34, 970, 392, title="Main Stage")
    _pane_shell(s, 88, 78, 614, 316, title="Primary Pane")
    _tab(s, 116, 142, 126, "Editor", selected=True)
    _tab(s, 248, 142, 126, "Inspector")
    s.box(118, 182, 556, 188, fill=TEAL_PALE, stroke=TEAL, radius=10)

    s.window(742, 90, 236, 272, title="Floating Dock")
    _pane_shell(s, 760, 122, 198, 214, title="Pane")
    _tab(s, 776, 170, 136, "Inspector", selected=True)
    s.box(778, 208, 164, 104, fill=SLATE_PALE, stroke=INK_SOFT, radius=8)

    s.arrow(694, 190, 744, 196, bend=0.08, stroke=AMBER, width=3, head=12)
    s.text(310, 452, "Stage factory hosts each detached pane and can add app-specific chrome", size=20, fill=INK_SOFT)
    return s


def persistence() -> Sketch:
    s = Sketch(1080, 500, seed=105)

    s.box(74, 84, 280, 186, fill=INDIGO_PALE, stroke=INDIGO, radius=12, shadow=True)
    s.text(214, 128, "1. Capture", size=23, fill=INDIGO)
    s.text(214, 164, "context.getLayout()", size=18, fill=INK)
    s.text(214, 194, "DockingLayout DTO", size=18, fill=INK_SOFT)

    s.box(400, 84, 280, 186, fill=TEAL_PALE, stroke=TEAL, radius=12, shadow=True)
    s.text(540, 128, "2. Store", size=23, fill=TEAL)
    s.text(540, 164, "application serializer", size=18, fill=INK)
    s.text(540, 194, "JSON / prefs / DB", size=18, fill=INK_SOFT)

    s.box(726, 84, 280, 186, fill=SLATE_PALE, stroke=INK_SOFT, radius=12, shadow=True)
    s.text(866, 128, "3. Restore", size=23, fill=INK_SOFT)
    s.text(866, 164, "context.restoreLayout(...)", size=18, fill=INK)
    s.text(866, 194, "unresolved IDs reported", size=18, fill=INK_SOFT)

    s.arrow(356, 170, 398, 170, bend=0.0, stroke=AMBER, width=2.8, head=11)
    s.arrow(682, 170, 724, 170, bend=0.0, stroke=AMBER, width=2.8, head=11)

    s.box(74, 316, 932, 126, fill=PAPER, stroke=MUTED, radius=12)
    s.text(540, 352, "Serialization, versioning, and migrations stay application-owned", size=22, fill=INK)
    s.text(540, 386, "GemsFX only provides immutable layout DTOs and restore validation", size=19, fill=INK_SOFT)
    return s


def styling() -> Sketch:
    s = Sketch(1060, 500, seed=106)
    _pane_shell(s, 70, 60, 920, 362, title="docking-pane.css")

    _tab(s, 104, 126, 148, ".docking-tab", selected=True)
    _tab(s, 258, 126, 168, ".selected")
    _tab(s, 432, 126, 184, ".close-button")

    s.box(104, 170, 408, 220, fill=TEAL_PALE, stroke=TEAL, radius=10)
    s.text(308, 292, "content area", size=22, fill=TEAL)

    s.box(530, 170, 420, 102, fill=SLATE_PALE, stroke=INK_SOFT, radius=10)
    s.text(740, 228, "split region", size=20, fill=INK_SOFT)

    s.box(530, 288, 420, 102, fill=AMBER, stroke=AMBER, radius=10, opacity=0.25)
    s.text(740, 346, "drop-zone preview", size=20, fill=AMBER)

    s.dimension(104, 436, 946, 436, "style classes scope under .docking-pane", stroke=ROSE, offset=18)
    return s


DRAWINGS = {
    "cover.svg": cover,
    "anatomy.svg": anatomy,
    "interactions.svg": interactions,
    "floating-stage.svg": floating_stage,
    "persistence.svg": persistence,
    "styling.svg": styling,
}


def generate() -> None:
    for name, factory in DRAWINGS.items():
        factory().save(OUT / name)


if __name__ == "__main__":
    generate()
    print(f"wrote {len(DRAWINGS)} graphics to {OUT}")
