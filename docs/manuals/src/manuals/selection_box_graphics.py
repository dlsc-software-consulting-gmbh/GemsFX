
"""Generated cartoon illustrations for the SelectionBox manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "selection-box"
LINE = "#B7C2D0"

def _lines(s,x,y,w,n=3):
    for i in range(n): s.line(x,y+i*26,x+w*(.68 if i%3==2 else 1),y+i*26,stroke=LINE,width=2.3)
def _field(s,x,y,w,text="type"):
    s.box(x,y,w,54,fill=PAPER,radius=13,shadow=True); s.text(x+22,y+35,text,size=20,anchor="start",fill=INK if text!="type" else MUTED); s.blob(x+w-28,y+27,14,14,fill=INDIGO_PALE,stroke=INDIGO,width=2); s.line(x+w-17,y+38,x+w-7,y+48,stroke=INDIGO,width=2.4)
def _chip(s,x,y,t,w=130,fill=TEAL_PALE,stroke=TEAL,close=True):
    s.box(x,y,w,38,fill=fill,stroke=stroke,radius=19); s.text(x+18,y+25,t,size=18,anchor="start",fill=INK)
    if close:
       s.blob(x+w-20,y+19,10,10,fill=PAPER,stroke=stroke,width=1.7); s.line(x+w-24,y+15,x+w-16,y+23,stroke=stroke,width=2); s.line(x+w-16,y+15,x+w-24,y+23,stroke=stroke,width=2)
def _popup(s,x,y,w,n=4,checks=False):
    s.box(x,y,w,38+n*38,fill=PAPER,radius=10,shadow=True)
    for i in range(n):
       yy=y+27+i*38
       if checks: s.box(x+16,yy-11,18,18,fill=TEAL_PALE if i%2==0 else PAPER,stroke=TEAL,radius=3,width=2)
       s.line(x+(48 if checks else 24),yy,x+w-24,yy,stroke=LINE,width=2.2)

def cover():
    s=Sketch(1000,480,seed=250)
    s.window(60,34,880,400,title="SelectionBox")
    s.box(220,105,350,58,fill=PAPER,stroke=INK,radius=10,shadow=True); s.text(245,141,"3 items selected",size=21,anchor="start",fill=INK); s.text(535,142,"▾",size=25,fill=INK); _popup(s,220,172,350,5,checks=True)
    return s

def anatomy():
    s=Sketch(1120,540,seed=250+1)
    s.window(50,42,610,420,title="SelectionBox")
    s.box(125,100,390,58,fill=PAPER,stroke=INK,radius=10,shadow=True); s.text(150,136,"Item 1",size=21,anchor="start",fill=INK); s.text(485,136,"▾",size=25,fill=INK); _popup(s,125,175,390,4,checks=True)
    lx=710
    for key,kx,ky,text,ly in [('1', 250, 128, 'display label', 95), ('2', 485, 128, 'arrow button', 150), ('3', 165, 205, 'Clear / Select All top node', 205), ('4', 165, 245, 'radio or check items', 260), ('5', 510, 330, 'popup decorations', 315)]:
       s.badge(lx,ly,key,fill=INDIGO,size=16); s.text(lx+28,ly+5,text,size=18,anchor="start",fill=INK); s.arrow(lx-22,ly,kx,ky,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,420,seed=250+2)
    for x,l,ch in [(90,"single",False),(390,"multiple",True),(690,"read only",True)]:
       s.box(x,90,230,55,fill=PAPER,stroke=INK,radius=10,shadow=True); s.text(x+20,125,"Select",size=19,anchor="start",fill=MUTED); s.text(x+200,126,"▾",size=23,fill=MUTED if l=="read only" else INK);
       if l!="read only": _popup(s,x,155,230,3,checks=ch)
       s.label(x+115,320,l,size=20,fill=INK_SOFT,bg=PAPER)
    return s

def flow():
    s=Sketch(1000,430,seed=250+3)
    for x,title,fill,stroke in [(70,"model",TEAL_PALE,TEAL),(390,"control",INDIGO_PALE,INDIGO),(710,"callback",AMBER_PALE,AMBER)]:
       s.box(x,95,220,90,fill=fill,stroke=stroke,radius=14,shadow=True); s.text(x+110,132,title,size=23,fill=stroke); s.text(x+110,158,"public API",size=18,fill=stroke)
    s.arrow(292,140,388,140,bend=0,stroke=AMBER,width=3); s.arrow(612,140,708,140,bend=0,stroke=AMBER,width=3)
    s.text(500,285,"Selection model + converters produce display text",size=21,fill=INK_SOFT)
    return s

def styling():
    s=Sketch(1000,390,seed=250+4)
    s.box(90,70,390,190,fill=PAPER,stroke=INK,radius=14,shadow=True)
    s.box(125,110,280,58,fill=PAPER,stroke=INK,radius=10,shadow=True); s.text(145,146,".display-label",size=20,anchor="start",fill=INK); s.text(380,146,"▾",size=24,fill=INK)
    s.box(570,55,330,260,fill=SLATE_PALE,stroke=MUTED,radius=12)
    for i,txt in enumerate(['.selection-box', '.display-label', '.arrow-button', '.arrow', '.selection-popup > .content', '.extra-buttons-box .extra-button', '.options-scroll-pane .radio-button', '.options-scroll-pane .check-box', ':showing', ':empty', ':single', ':multiple', ':readonly'][:7]): s.text(605,95+i*31,txt,size=18,anchor="start",fill=INK)
    return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"states.svg":states,"flow.svg":flow,"styling.svg":styling}
def generate():
    for name,factory in DRAWINGS.items(): factory().save(OUT/name)
if __name__=="__main__": generate()
