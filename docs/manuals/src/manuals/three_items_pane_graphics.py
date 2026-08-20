
"""Generated cartoon illustrations for the ThreeItemsPane developer manual."""
from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "three-items-pane"
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
    s=Sketch(1000,500,seed=221); s.window(70,55,860,380,title='ThreeItemsPane')
    _node(s,120,215,220,70,'item1',TEAL_PALE,TEAL); _node(s,415,210,170,80,'item2',INDIGO_PALE,INDIGO); _node(s,710,215,160,70,'item3',AMBER_PALE,AMBER)
    s.dimension(120,330,870,330,'left edge  •  center  •  right edge',stroke=ROSE,offset=20)
    return s

def anatomy():
    s=Sketch(1120,560,seed=222)
    s.box(75,135,650,250,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True)
    _node(s,105,225,185,70,'item1',TEAL_PALE,TEAL); _node(s,360,220,150,80,'item2',INDIGO_PALE,INDIGO); _node(s,565,225,130,70,'item3',AMBER_PALE,AMBER)
    keys=[('1',197,260,'item1 property',130),('2',435,260,'item2 property (centered)',200),('3',630,260,'item3 property',270),('4',325,306,'spacing',340),('5',75,135,'pane insets',410)]
    lx=780
    for k,x,y,t,ly in keys:
        s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+28,ly+5,t,size=19,anchor='start'); s.arrow(lx-22,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,430,seed=223)
    s.box(80,70,360,260,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,100,165,90,60,'1',TEAL_PALE,TEAL); _node(s,210,165,90,60,'2',INDIGO_PALE,INDIGO); _node(s,330,165,90,60,'3',AMBER_PALE,AMBER); s.label(260,365,'HORIZONTAL',size=20,fill=INK_SOFT)
    s.box(590,55,250,310,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,665,75,100,55,'1',TEAL_PALE,TEAL); _node(s,665,180,100,55,'2',INDIGO_PALE,INDIGO); _node(s,665,290,100,55,'3',AMBER_PALE,AMBER); s.label(715,395,'VERTICAL',size=20,fill=INK_SOFT)
    return s

def layout():
    s=Sketch(1040,540,seed=224)
    s.box(110,150,780,180,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,130,210,150,60,'item1',TEAL_PALE,TEAL); _node(s,445,205,140,70,'item2',INDIGO_PALE,INDIGO); _node(s,735,210,135,60,'item3',AMBER_PALE,AMBER)
    s.line(500,125,500,350,stroke=ROSE,width=1.8,dash='7 6'); s.dimension(130,365,280,365,'prefWidth1',stroke=TEAL,offset=18); s.dimension(280,365,445,365,'spacing / minimumX',stroke=ROSE,offset=18); s.dimension(445,95,585,95,'centered item2',stroke=INDIGO,offset=-18); s.dimension(735,365,870,365,'right aligned item3',stroke=AMBER,offset=18)
    s.label(500,470,'later items move right/down when earlier items plus spacing would overlap',size=21,fill=INK_SOFT)
    return s

def interaction():
    s=Sketch(1000,460,seed=225)
    _node(s,95,110,210,70,'setItem1(A)',TEAL_PALE,TEAL); _node(s,95,215,210,70,'setItem2(B)',INDIGO_PALE,INDIGO); _node(s,95,320,210,70,'setItem3(C)',AMBER_PALE,AMBER)
    s.box(415,160,170,120,fill=AMBER_PALE,stroke=AMBER,radius=12); s.text(500,205,'updateChildren()',size=20,fill=AMBER); s.text(500,232,'clear + add non-null',size=16,fill=AMBER)
    s.box(695,115,230,240,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,715,145,70,50,'A',TEAL_PALE,TEAL,17); _node(s,775,210,70,50,'B',INDIGO_PALE,INDIGO,17); _node(s,835,285,70,50,'C',AMBER_PALE,AMBER,17)
    s.arrow(315,250,410,220,bend=-.1,stroke=AMBER,width=2.6); s.arrow(590,220,690,240,bend=.1,stroke=AMBER,width=2.6)
    s.label(500,420,'children list is derived from the three item properties',size=20,fill=INK_SOFT)
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
