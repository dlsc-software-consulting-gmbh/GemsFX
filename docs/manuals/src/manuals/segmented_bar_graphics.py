from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "segmented-bar"
LINE="#B7C2D0"
def _txt(s,x,y,t,size=18,fill=INK,anchor="middle",bold=False): s.text(x,y,t,size=size,fill=fill,anchor=anchor,bold=bold)
def _button(s,x,y,w,t,fill=SLATE_PALE): s.box(x,y,w,34,fill=fill,stroke=LINE,radius=8); s.text(x+w/2,y+23,t,size=15,fill=INK)
def _dialog(s,x,y,w,h,title="Dialog",kind="info",buttons=("OK",)):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True)
    col={"info":INDIGO_PALE,"warning":AMBER_PALE,"error":ROSE_PALE,"confirm":TEAL_PALE,"blank":PAPER}.get(kind,INDIGO_PALE)
    s.blob(x+w/2,y+50,24,18,fill=col,stroke=INDIGO if kind=='info' else AMBER if kind=='warning' else ROSE if kind=='error' else TEAL)
    s.text(x+w/2,y+92,title,size=23,fill=INK,bold=True)
    s.line(x+50,y+125,x+w-50,y+125,stroke=LINE,width=2)
    for i in range(3): s.line(x+60,y+155+i*24,x+w-60-(i==2)*90,y+155+i*24,stroke=LINE,width=2.2)
    s.box(x,y+h-58,w,58,fill=SLATE_PALE,stroke=LINE,radius=0)
    bx=x+w-25-len(buttons)*90
    for i,b in enumerate(buttons): _button(s,bx+i*90,y+h-45,72,b,fill=INDIGO_PALE if i==0 else PAPER)
def _popover(s,x,y,w,h,side="top"):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=14,shadow=True)
    if side=="top": s.line(x+w/2-18,y,x+w/2,y-25,stroke=INK,width=3); s.line(x+w/2,y-25,x+w/2+18,y,stroke=INK,width=3)
    if side=="left": s.line(x,y+h/2-18,x-25,y+h/2,stroke=INK,width=3); s.line(x-25,y+h/2,x,y+h/2+18,stroke=INK,width=3)
    _txt(s,x+w/2,y+42,"Popover content",18,INDIGO,bold=True); s.line(x+35,y+75,x+w-35,y+75,stroke=LINE,width=2); s.line(x+35,y+105,x+w-70,y+105,stroke=LINE,width=2)
def _card(s,x,y,w,h,name="VISA",fill=INDIGO_PALE): s.box(x,y,w,h,fill=fill,stroke=INK,radius=12,shadow=True); s.text(x+w/2,y+h/2+9,name,size=28,fill=INDIGO,bold=True); s.line(x+20,y+h-28,x+w-20,y+h-28,stroke=LINE,width=2)
def _bar(s,x,y,w,h,vals,vertical=False):
    total=sum(vals); off=0; colors=[INDIGO_PALE,TEAL_PALE,AMBER_PALE,ROSE_PALE,SLATE_PALE]
    for i,v in enumerate(vals):
        frac=v/total if total else 0
        if vertical:
            hh=h*frac; s.box(x,y+h-off-hh,w,hh,fill=colors[i%5],stroke=PAPER,radius=5); off+=hh
        else:
            ww=w*frac; s.box(x+off,y,ww,h,fill=colors[i%5],stroke=PAPER,radius=5); off+=ww
    s.box(x,y,w,h,fill="none",stroke=INK,radius=8)
def _textview(s,x,y,w,h,sel=True):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=10,shadow=True)
    for i in range(6): s.line(x+30,y+35+i*28,x+w-35-(i%3)*65,y+35+i*28,stroke=LINE,width=2.2)
    if sel: s.box(x+140,y+62,220,30,fill=INDIGO_PALE,stroke=INDIGO,radius=5); s.text(x+250,y+83,"selected words",size=16,fill=INDIGO)
def save_all(drawings):
    for name,fn in drawings.items(): fn().save(OUT/name)

def cover():
 s=Sketch(1000,480,seed=331); s.window(70,45,860,360,title="Storage usage"); _bar(s,160,170,680,58,[14,32,9,40,5,35]); s.label(500,280,'Segment values are normalized against total',size=22,fill=INDIGO,bg=INDIGO_PALE); return s
def anatomy():
 s=Sketch(1120,560,seed=332); _bar(s,110,180,560,70,[1,10,40,30,10,50]); keys=[('1',112,215,'segments list',100),('2',350,215,'Segment.value drives size',175),('3',505,215,'Segment.text bound into default label',250),('4',120,180,'first / middle / last style classes',325),('5',650,215,'segmentViewFactory creates nodes',405),('6',600,250,'total is read-only sum',480)]
 for k,x,y,t,ly in keys: s.badge(730,ly,k); s.text(762,ly+6,t,size=19,anchor='start'); s.arrow(710,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def generics():
 s=Sketch(1000,430,seed=333); boxes=[('Segment','value + text'),('TypeSegment','adds enum type'),('IssueStatusSegment','adds status'),('factory','returns Node')]
 for i,(a,b) in enumerate(boxes): x=80+i*220; s.box(x,130,165,100,fill=INDIGO_PALE if i%2==0 else TEAL_PALE,stroke=INDIGO if i%2==0 else TEAL,radius=12,shadow=True); s.text(x+82,164,a,size=18,bold=True); s.text(x+82,196,b,size=15,fill=INK_SOFT)
 for x in [245,465,685]: s.arrow(x,180,x+45,180,bend=0,stroke=AMBER,width=2.4,head=11)
 return s
def orientation():
 s=Sketch(1000,430,seed=334); _bar(s,100,110,520,55,[20,30,10,40],False); s.label(360,195,'HORIZONTAL',size=18,fill=INDIGO,bg=INDIGO_PALE); _bar(s,760,60,60,300,[20,30,10,40],True); s.label(790,390,'VERTICAL',size=18,fill=INDIGO,bg=INDIGO_PALE); return s
def minsize():
 s=Sketch(1000,430,seed=335); _bar(s,110,120,780,60,[1,1000],False); s.dimension(110,210,130,210,'minSegmentSize keeps tiny values visible',stroke=ROSE,size=18,offset=28); s.label(500,310,'The skin steals space from larger segments when a segment would be smaller than the minimum',size=19,fill=INK_SOFT,bg=SLATE_PALE); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"generics.svg":generics,"orientation.svg":orientation,"min-size.svg":minsize}
def generate(): save_all(DRAWINGS)
