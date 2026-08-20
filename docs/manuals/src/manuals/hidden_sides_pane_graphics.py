
"""Generated cartoon illustrations for the HiddenSidesPane developer manual."""
from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "hidden-sides-pane"
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
    s = Sketch(1000, 500, seed=201)
    s.window(80, 45, 840, 400, title="HiddenSidesPane")
    _node(s, 180, 125, 640, 240, "content", SLATE_PALE, MUTED, 24)
    _node(s, 330, 58, 340, 70, "top", AMBER_PALE, AMBER)
    _node(s, 810, 150, 90, 190, "right", INDIGO_PALE, INDIGO)
    _node(s, 100, 150, 90, 190, "left", ROSE_PALE, ROSE)
    s.cursor(880, 245, scale=1.3)
    s.dimension(830, 390, 910, 390, "triggerDistance", stroke=ROSE, offset=18)
    return s

def anatomy():
    s = Sketch(1120, 560, seed=202)
    _node(s, 85, 90, 580, 360, "content fills pane", SLATE_PALE, MUTED, 22)
    _node(s, 85, 44, 580, 64, "top side", AMBER_PALE, AMBER)
    _node(s, 640, 90, 100, 360, "right", INDIGO_PALE, INDIGO)
    _node(s, 10, 90, 100, 360, "left", ROSE_PALE, ROSE)
    _node(s, 85, 435, 580, 64, "bottom side", TEAL_PALE, TEAL)
    keys=[('1',355,250,'content node',120),('2',360,76,'top property',180),('3',692,250,'right property',240),('4',60,250,'left property',300),('5',360,468,'bottom property',360),('6',740,90,'clip rectangle',420)]
    lx=790
    for k,x,y,t,ly in keys:
        s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+28,ly+5,t,size=19,anchor='start'); s.arrow(lx-22,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,430,seed=203)
    for x,label,frac in [(55,'hidden',0),(365,'sliding',.55),(675,'visible',1)]:
        _node(s,x,90,250,210,'content',SLATE_PALE,MUTED,18)
        w=80; off=w*frac
        s.box(x+250-off,105,w,180,fill=INDIGO_PALE,stroke=INDIGO,radius=9,shadow=True)
        s.label(x+125,345,label,size=20,fill=INK_SOFT)
    s.arrow(318,190,355,190,bend=0,stroke=AMBER,width=2.8); s.arrow(628,190,665,190,bend=0,stroke=AMBER,width=2.8)
    s.label(500,392,'visibility goes from 0 to 1 using animationDelay and animationDuration',size=20,fill=AMBER)
    return s

def layout():
    s=Sketch(1040,540,seed=204)
    _node(s,160,90,620,340,'content: width x height',SLATE_PALE,MUTED,21)
    s.box(90,90,95,340,fill=ROSE_PALE,stroke=ROSE,radius=10,shadow=True); s.text(137,265,'left',size=20,fill=ROSE)
    s.box(755,90,95,340,fill=INDIGO_PALE,stroke=INDIGO,radius=10,shadow=True); s.text(802,265,'right',size=20,fill=INDIGO)
    s.dimension(90,455,185,455,'prefWidth * visibility',stroke=ROSE,offset=20)
    s.dimension(755,455,850,455,'prefWidth * visibility',stroke=INDIGO,offset=20)
    s.dimension(160,55,780,55,'contentWidth',stroke=TEAL,offset=-18)
    s.label(520,505,'side nodes are unmanaged overlays clipped by the pane bounds',size=21,fill=INK_SOFT)
    return s

def interaction():
    s=Sketch(1000,460,seed=205)
    _node(s,70,80,330,230,'mouse near edge',SLATE_PALE,MUTED,20); s.cursor(80,175,scale=1.3); s.arrow(96,185,150,185,bend=0,stroke=ROSE,width=2.8)
    _node(s,585,80,330,230,'pinned side',SLATE_PALE,MUTED,20); _node(s,585,80,85,230,'left',ROSE_PALE,ROSE,18); s.badge(675,105,'pin',fill=AMBER,size=18)
    s.box(420,150,145,90,fill=AMBER_PALE,stroke=AMBER,radius=12); s.text(492,188,'show(side)',size=20,fill=AMBER); s.text(492,215,'or pinnedSide',size=17,fill=AMBER)
    s.arrow(405,190,418,190,bend=0,stroke=AMBER,width=2.5); s.arrow(565,190,582,190,bend=0,stroke=AMBER,width=2.5)
    s.label(500,385,'mouse exit hides unless a side is pinned or the mouse is pressed',size=20,fill=INK_SOFT)
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
