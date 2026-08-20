
"""Generated cartoon illustrations for the ResponsivePane developer manual."""
from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "responsive-pane"
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
    s=Sketch(1000,500,seed=211); s.window(70,45,860,400,title='ResponsivePane')
    _node(s,155,130,95,230,'small',TEAL_PALE,TEAL); _node(s,265,130,230,230,'large',INDIGO_PALE,INDIGO); _node(s,515,130,330,230,'content',SLATE_PALE,MUTED)
    s.dimension(155,390,845,390,'small + gap + large + gap + content',stroke=ROSE,offset=20)
    return s

def anatomy():
    s=Sketch(1120,560,seed=212)
    _node(s,70,120,640,320,'content',SLATE_PALE,MUTED,22); _node(s,70,120,80,320,'smallSidebar',TEAL_PALE,TEAL,17); _node(s,150,120,190,320,'largeSidebar',INDIGO_PALE,INDIGO,18); s.box(340,120,370,320,fill=ROSE_PALE,stroke=ROSE,radius=12,opacity=.25); s.text(525,285,'GlassPane',size=20,fill=ROSE)
    keys=[('1',390,280,'content',115),('2',110,280,'smallSidebar',180),('3',245,280,'largeSidebar',245),('4',525,280,'glassPane while forced',310),('5',70,120,'side pseudo class',375),('6',150,455,'gap',440)]
    lx=760
    for k,x,y,t,ly in keys:
        s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+28,ly+5,t,size=19,anchor='start'); s.arrow(lx-22,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,430,seed=213)
    frames=[(35,260,'showing-none',False,False),(365,280,'showing-small',True,False),(695,285,'showing-large',False,True)]
    for x,w,label,small,large in frames:
        _node(s,x,95,w,180,'content',SLATE_PALE,MUTED,17)
        if small: _node(s,x,95,48,180,'S',TEAL_PALE,TEAL,18)
        if large: _node(s,x,95,105,180,'Large',INDIGO_PALE,INDIGO,17)
        s.label(x+w/2,325,label,size=19,fill=INK_SOFT)
    s.label(500,385,'thresholds come from pref sizes of content and sidebars plus gap',size=20,fill=AMBER)
    return s

def layout():
    s=Sketch(1040,540,seed=214)
    _node(s,155,140,650,230,'available inside bounds',SLATE_PALE,MUTED,21); _node(s,155,140,95,230,'small',TEAL_PALE,TEAL); _node(s,265,140,180,230,'large',INDIGO_PALE,INDIGO)
    s.dimension(155,405,250,405,'smallWidth',stroke=TEAL,offset=18); s.dimension(250,405,265,405,'gap',stroke=ROSE,offset=18); s.dimension(265,405,445,405,'largeWidth',stroke=INDIGO,offset=18); s.dimension(445,405,805,405,'contentWidth',stroke=MUTED,offset=18)
    s.box(70,70,870,385,fill=None,stroke=INK_SOFT,radius=12,dash='8 7')
    s.label(505,505,'LEFT/RIGHT use widths; TOP/BOTTOM use heights with the same rules',size=21,fill=INK_SOFT)
    return s

def interaction():
    s=Sketch(1000,460,seed=215)
    _node(s,80,100,285,210,'small + content',SLATE_PALE,MUTED,18); _node(s,80,100,55,210,'S',TEAL_PALE,TEAL)
    s.box(455,105,135,90,fill=AMBER_PALE,stroke=AMBER,radius=12); s.text(522,142,'forceLarge',size=20,fill=AMBER); s.text(522,168,'= true',size=18,fill=AMBER)
    _node(s,665,100,285,210,'forced large',SLATE_PALE,MUTED,18); _node(s,665,100,55,210,'S',TEAL_PALE,TEAL); _node(s,720,100,115,210,'Large',INDIGO_PALE,INDIGO,17); s.box(835,100,115,210,fill=ROSE_PALE,stroke=ROSE,radius=12,opacity=.25); s.cursor(880,200,scale=1.25)
    s.arrow(370,190,450,150,bend=-.15,stroke=AMBER,width=2.6); s.arrow(595,150,658,190,bend=.12,stroke=AMBER,width=2.6)
    s.label(500,375,'clicking the glass pane sets forceLargeSidebarDisplay(false)',size=20,fill=INK_SOFT)
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
