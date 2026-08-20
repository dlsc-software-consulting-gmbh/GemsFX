"""Generates cartoon illustrations for the Skeleton manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TITLE_SIZE, LABEL_SIZE, TEXT_SIZE, SMALL_SIZE
OUT=Path(__file__).resolve().parent.parent/'graphics'/'skeleton'

def _shimmer(s,x,y,w,h):
    s.box(x,y,w,h,fill=SLATE_PALE,stroke=MUTED,radius=10); s.box(x+w*.38,y,w*.28,h,fill=PAPER,stroke=None,radius=8,opacity=.65)

def _text_lines(s,x,y,w,count=3):
    for i in range(count): _shimmer(s,x,y+i*32,w*(.72 if i==count-1 else 1),14)

def cover():
    s=Sketch(1000,480,seed=301); s.window(80,35,840,405,title='Profile'); s.box(180,120,640,230,fill=PAPER,radius=18,shadow=True); s.blob(255,205,48,48,fill=SLATE_PALE,stroke=MUTED); s.box(210,157,90,96,fill=PAPER,stroke=None,opacity=.35); _shimmer(s,330,158,220,18); _text_lines(s,330,205,380,3); s.text(500,400,'Skeleton',size=32,fill=INDIGO); return s

def anatomy():
    s=Sketch(1120,540,seed=302); _shimmer(s,120,130,470,90); s.box(290,130,120,90,fill=PAPER,stroke=None,radius=8,opacity=.7); s.text(355,255,'shimmer band',size=LABEL_SIZE,fill=INDIGO); keys=[('1',160,150,'shape-layer: base rectangles',130),('2',350,170,'shimmer-band fill',210),('3',500,130,'shimmer-layer clipped by mask',290),('4',585,220,'control bounds',370),('5',360,220,'mouse transparent nodes',450)]; lx=680
    for k,tx,ty,text,ly in keys: s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+30,ly+5,text,size=TEXT_SIZE,anchor='start',fill=INK); s.arrow(lx-25,ly,tx,ty,bend=.04,stroke=MUTED,width=1.8,head=9)
    return s

def variants():
    s=Sketch(1000,420,seed=303); data=[('ROUNDED_RECTANGLE',0),('CIRCULAR',1),('TEXT',2)]
    for name,i in data:
        x=80+i*300; s.box(x,60,230,235,fill=PAPER,radius=16,shadow=True)
        if i==0: _shimmer(s,x+45,145,140,50)
        elif i==1: s.blob(x+115,170,55,55,fill=SLATE_PALE,stroke=MUTED); s.box(x+95,115,35,110,fill=PAPER,stroke=None,opacity=.55)
        else: _text_lines(s,x+40,130,150,3)
        s.label(x+115,335,name,size=LABEL_SIZE,fill=INK_SOFT)
    return s

def timing():
    s=Sketch(1040,410,seed=304); s.line(130,220,900,220,stroke=MUTED,width=2.5); s.arrow(150,160,850,160,bend=0,stroke=AMBER,width=3,head=12); s.label(500,128,'cycleDuration = 1500 ms by default',size=LABEL_SIZE,fill=AMBER,bg=PAPER)
    for x,t in [(160,'-shimmerWidth'),(500,'band crosses content'),(840,'repeat indefinitely')]: s.line(x,200,x,240,stroke=ROSE,width=2); s.label(x,270,t,size=SMALL_SIZE,fill=INK_SOFT,bg=PAPER)
    _shimmer(s,310,55,420,60); return s

def sizing():
    s=Sketch(1000,430,seed=305); s.box(160,90,270,160,fill=PAPER,radius=14); _shimmer(s,205,160,180,30); s.dimension(160,285,430,285,'pref width 120 + insets',stroke=ROSE,size=SMALL_SIZE,offset=22); s.box(610,90,260,180,fill=PAPER,radius=14); _text_lines(s,650,135,180,4); s.dimension(900,135,900,135+4*14+3*8,'line layout',stroke=TEAL,size=SMALL_SIZE,offset=28); s.label(500,365,'Rounded/text pref width 120; circular pref size 48; max size is unbounded',size=TEXT_SIZE,fill=MUTED); return s

DRAWINGS={'cover.svg':cover,'anatomy.svg':anatomy,'variants.svg':variants,'timing.svg':timing,'sizing.svg':sizing}
def generate():
    for n,f in DRAWINGS.items(): f().save(OUT/n)
if __name__=='__main__': generate(); print(f'wrote {len(DRAWINGS)} graphics to {OUT}')
