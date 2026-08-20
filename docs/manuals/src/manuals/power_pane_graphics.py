
"""Generated cartoon illustrations for the PowerPane developer manual."""
from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "power-pane"
LINE = "#B7C2D0"


def _lines(s, x, y, w, rows=4, gap=24, colour=LINE):
    for i in range(rows):
        s.line(x, y + i * gap, x + w * (1 - .12 * (i % 3)), y + i * gap, stroke=colour, width=2.4)


def _node(s, x, y, w, h, label, fill=INDIGO_PALE, stroke=INDIGO, size=19):
    s.box(x, y, w, h, fill=fill, stroke=stroke, radius=12, shadow=True)
    s.text(x + w / 2, y + h / 2 + 7, label, size=size, fill=stroke)


def _tile(s, x, y, w, h, label, fill=TEAL_PALE):
    s.box(x, y, w, h, fill=fill, stroke=TEAL, radius=10)
    s.text(x + w / 2, y + h / 2 + 6, label, size=18, fill=TEAL)


def cover():
    s=Sketch(1000,500,seed=251); s.window(70,45,860,400,title='PowerPane')
    _node(s,160,135,600,250,'application content',SLATE_PALE,MUTED,22); _node(s,160,315,600,70,'DrawerStackPane',AMBER_PALE,AMBER,18); _node(s,110,135,85,250,'Hidden',ROSE_PALE,ROSE,17); _node(s,620,95,240,105,'DialogPane',INDIGO_PALE,INDIGO,18); _node(s,760,135,95,250,'Info',TEAL_PALE,TEAL,17)
    return s

def anatomy():
    s=Sketch(1120,560,seed=252)
    _node(s,90,75,230,85,'InfoCenterPane',TEAL_PALE,TEAL); _node(s,405,75,230,85,'StackPane',SLATE_PALE,MUTED); _node(s,720,75,230,85,'DialogPane',INDIGO_PALE,INDIGO)
    _node(s,250,245,230,85,'DrawerStackPane',AMBER_PALE,AMBER); _node(s,580,245,230,85,'HiddenSidesPane',ROSE_PALE,ROSE); _node(s,415,400,230,85,'content property',SLATE_PALE,MUTED)
    for x1,y1,x2,y2 in [(320,118,405,118),(635,118,720,118),(520,160,365,245),(520,160,695,245),(695,330,530,400)]: s.arrow(x1,y1,x2,y2,bend=.08,stroke=MUTED,width=2.2)
    return s

def states():
    s=Sketch(1000,430,seed=253)
    labels=['hidden side','drawer','dialog','info center']
    fills=[ROSE_PALE,AMBER_PALE,INDIGO_PALE,TEAL_PALE]
    strokes=[ROSE,AMBER,INDIGO,TEAL]
    for i,(lab,fill,stroke) in enumerate(zip(labels,fills,strokes)):
        x=60+i*230; _node(s,x,95,185,190,'content',SLATE_PALE,MUTED,17); s.box(x+25,y:=115,w:=135,h:=95,fill=fill,stroke=stroke,radius=10,shadow=True); s.text(x+92,y+55,lab,size=17,fill=stroke); s.label(x+92,325,lab,size=18,fill=INK_SOFT)
    return s

def layout():
    s=Sketch(1040,540,seed=254)
    s.box(95,70,850,390,fill=PAPER,stroke=INK_SOFT,radius=14,shadow=True); _node(s,140,115,760,300,'InfoCenterPane root child',TEAL_PALE,TEAL,22); _node(s,220,175,600,200,'StackPane(drawerStackPane, dialogPane)',SLATE_PALE,MUTED,18); _node(s,260,230,520,115,'DrawerStackPane → HiddenSidesPane → content',AMBER_PALE,AMBER,17)
    s.dimension(140,435,900,435,'PowerPane has exactly one direct child',stroke=TEAL,offset=20)
    s.dimension(80,115,80,415,'all nested panes fill available space',stroke=ROSE,offset=20)
    return s

def interaction():
    s=Sketch(1000,460,seed=255)
    _node(s,70,170,170,80,'app calls\ngetDialogPane()',INDIGO_PALE,INDIGO,17); _node(s,295,70,180,80,'show dialog',INDIGO_PALE,INDIGO,18); _node(s,295,185,180,80,'show drawer',AMBER_PALE,AMBER,18); _node(s,295,300,180,80,'pin side',ROSE_PALE,ROSE,18); _node(s,560,185,330,95,'delegated to composed pane',TEAL_PALE,TEAL,20)
    s.arrow(245,210,290,110,bend=-.1,stroke=INDIGO,width=2.4); s.arrow(245,210,290,225,bend=0,stroke=AMBER,width=2.4); s.arrow(245,210,290,340,bend=.1,stroke=ROSE,width=2.4); s.arrow(480,225,555,225,bend=0,stroke=TEAL,width=2.4)
    s.label(500,420,'PowerPane owns the panes; applications configure them through final getters',size=20,fill=INK_SOFT)
    return s


DRAWINGS = {
    "cover.svg": cover,
    "anatomy.svg": anatomy,
    "states.svg": states,
    "layout.svg": layout,
    "interaction.svg": interaction,
}


def generate() -> None:
    for name, factory in DRAWINGS.items():
        factory().save(OUT / name)

if __name__ == "__main__":
    generate()
    print(f"wrote {len(DRAWINGS)} graphics to {OUT}")
