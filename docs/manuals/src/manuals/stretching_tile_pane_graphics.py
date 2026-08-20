
"""Generated cartoon illustrations for the StretchingTilePane developer manual."""
from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "stretching-tile-pane"
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
    s=Sketch(1000,500,seed=231); s.window(70,50,860,385,title='StretchingTilePane')
    for r,y in enumerate([120,245]):
        for c,x in enumerate([120,315,510,705]): _tile(s,x,y,160,85,f'Tile {r*4+c+1}',INDIGO_PALE if r==0 else TEAL_PALE)
    s.dimension(120,365,865,365,'row width filled by equal stretched tiles',stroke=ROSE,offset=20)
    return s

def anatomy():
    s=Sketch(1120,560,seed=232)
    s.box(80,80,660,360,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True)
    for i,(x,y) in enumerate([(110,120),(315,120),(520,120),(110,250),(315,250),(520,250)]): _tile(s,x,y,170,85,f'tile {i+1}',TEAL_PALE)
    keys=[('1',110,120,'managed children only',120),('2',285,205,'hgap',190),('3',195,245,'vgap',260),('4',605,165,'equal stretched width',330),('5',85,80,'padding / insets',400)]
    lx=790
    for k,x,y,t,ly in keys:
        s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+28,ly+5,t,size=19,anchor='start'); s.arrow(lx-22,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,430,seed=233)
    for x,w,cols,label in [(45,260,1,'narrow'),(370,300,2,'medium'),(720,230,3,'wide')]:
        s.box(x,70,w,240,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True)
        tw=(w-50-(cols-1)*10)/cols
        for i in range(6):
            c=i%cols; r=i//cols; _tile(s,x+25+c*(tw+10),90+r*65,tw,50,str(i+1),INDIGO_PALE)
        s.label(x+w/2,355,label,size=19,fill=INK_SOFT)
    s.label(500,400,'columnCount = floor(contentWidth / (maxPrefTileWidth + hgap))',size=19,fill=AMBER)
    return s

def layout():
    s=Sketch(1040,540,seed=234)
    s.box(110,105,820,320,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True)
    cols=4; hgap=18; tw=(760-(cols-1)*hgap)/cols
    for c in range(cols): _tile(s,140+c*(tw+hgap),190,tw,90,f'{c+1}',TEAL_PALE)
    s.dimension(140,145,140+tw,145,'w = availableWidth / columns',stroke=TEAL,offset=-18)
    s.dimension(140+tw,145,140+tw+hgap,145,'hgap',stroke=ROSE,offset=-18)
    s.dimension(140,310,900,310,'availableWidth = contentWidth - gaps',stroke=INDIGO,offset=20)
    s.dimension(80,105,80,425,'prefHeight = rows * tileHeight + gaps + insets',stroke=AMBER,offset=20)
    return s

def interaction():
    s=Sketch(1000,460,seed=235)
    s.box(90,80,330,250,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _tile(s,115,120,120,60,'A',TEAL_PALE); _tile(s,255,120,120,60,'B',TEAL_PALE)
    s.box(585,80,330,250,fill=PAPER,stroke=INK_SOFT,radius=12,shadow=True); _tile(s,610,120,130,60,'A',INDIGO_PALE); _tile(s,760,120,130,60,'B',INDIGO_PALE); _tile(s,610,200,130,60,'C',INDIGO_PALE); _tile(s,760,200,130,60,'D',INDIGO_PALE)
    s.arrow(425,205,575,205,bend=0,stroke=AMBER,width=2.8)
    s.label(500,375,'children are not scaled independently; the pane recomputes a grid on layout',size=20,fill=INK_SOFT)
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
