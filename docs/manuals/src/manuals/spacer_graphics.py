
"""Generated cartoon illustrations for the Spacer developer manual."""
from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "spacer"
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
    s=Sketch(1000,500,seed=241); s.window(85,70,830,360,title='Spacer')
    _node(s,160,210,120,65,'Hello',TEAL_PALE,TEAL); s.box(300,210,360,65,fill=ROSE_PALE,stroke=ROSE,radius=12,shadow=True); s.text(480,250,'Spacer grows',size=22,fill=ROSE); _node(s,680,210,120,65,'World',INDIGO_PALE,INDIGO)
    s.dimension(300,310,660,310,'available space',stroke=ROSE,offset=20)
    return s

def anatomy():
    s=Sketch(1120,560,seed=242)
    s.box(100,190,620,120,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,130,220,110,60,'left',TEAL_PALE,TEAL); s.box(265,220,300,60,fill=ROSE_PALE,stroke=ROSE,radius=10); s.text(415,258,'Spacer',size=22,fill=ROSE); _node(s,590,220,110,60,'right',INDIGO_PALE,INDIGO)
    keys=[('1',415,250,'Region with .spacer style class',150),('2',415,280,'active controls visible + managed',220),('3',415,220,'HBox/VBox grow priority ALWAYS',290),('4',565,250,'siblings pushed apart',360)]
    lx=760
    for k,x,y,t,ly in keys:
        s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+28,ly+5,t,size=19,anchor='start'); s.arrow(lx-22,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,430,seed=243)
    _node(s,90,130,100,55,'A',TEAL_PALE,TEAL); s.box(210,130,260,55,fill=ROSE_PALE,stroke=ROSE,radius=10); s.text(340,166,'active=true',size=19,fill=ROSE); _node(s,490,130,100,55,'B',INDIGO_PALE,INDIGO)
    _node(s,90,260,100,55,'A',TEAL_PALE,TEAL); _node(s,210,260,100,55,'B',INDIGO_PALE,INDIGO); s.label(420,295,'active=false → invisible and unmanaged',size=20,fill=INK_SOFT,anchor='start')
    return s

def layout():
    s=Sketch(1040,540,seed=244)
    s.box(150,80,260,360,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,205,105,150,55,'top',TEAL_PALE,TEAL); s.box(205,180,150,150,fill=ROSE_PALE,stroke=ROSE,radius=10); s.text(280,260,'VBox\nSpacer',size=20,fill=ROSE); _node(s,205,360,150,55,'bottom',INDIGO_PALE,INDIGO)
    s.box(540,200,360,110,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _node(s,565,225,85,60,'left',TEAL_PALE,TEAL); s.box(670,225,95,60,fill=ROSE_PALE,stroke=ROSE,radius=10); s.text(717,262,'HBox',size=18,fill=ROSE); _node(s,790,225,85,60,'right',INDIGO_PALE,INDIGO)
    s.dimension(365,180,365,330,'vertical growth',stroke=ROSE,offset=18); s.dimension(670,330,765,330,'horizontal growth',stroke=ROSE,offset=18)
    return s

def interaction():
    s=Sketch(1000,460,seed=245)
    s.box(115,115,270,100,fill=TEAL_PALE,stroke=TEAL,radius=12); s.text(250,155,'CheckBox selected',size=20,fill=TEAL); s.text(250,182,'bind active',size=18,fill=TEAL)
    s.arrow(390,165,585,165,bend=0,stroke=AMBER,width=2.6)
    s.box(600,115,270,100,fill=ROSE_PALE,stroke=ROSE,radius=12); s.text(735,155,'Spacer visible',size=20,fill=ROSE); s.text(735,182,'and managed',size=18,fill=ROSE)
    s.label(500,330,'managedProperty is bound to visibleProperty, visibleProperty is bound to activeProperty',size=19,fill=INK_SOFT)
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
