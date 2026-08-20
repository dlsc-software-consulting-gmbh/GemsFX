
"""Generated cartoon illustrations for the SearchField manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "search-field"
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
    s=Sketch(1000,480,seed=210)
    s.window(60,34,880,400,title="SearchField")
    _field(s,150,110,520,"United"); _popup(s,150,178,520,4); s.text(190,213,"United Kingdom",size=20,anchor="start",fill=INK); s.box(720,135,120,70,fill=AMBER_PALE,stroke=AMBER,radius=12,shadow=True); s.text(780,176,"async",size=22,fill=AMBER); s.cursor(620,315,scale=1.2)
    return s

def anatomy():
    s=Sketch(1120,540,seed=210+1)
    s.window(50,42,610,420,title="SearchField")
    _field(s,120,120,430,"Uni"); _popup(s,120,190,430,3); s.box(128,122,34,50,fill=TEAL_PALE,stroke=TEAL,radius=8); s.text(200,154,"ted",size=20,fill=MUTED)
    lx=710
    for key,kx,ky,text,ly in [('1', 140, 145, 'optional left node', 95), ('2', 220, 145, 'embedded TextField editor', 150), ('3', 550, 145, 'search / busy graphic', 205), ('4', 250, 220, 'suggestion ListView', 260), ('5', 200, 295, 'placeholder when empty', 315)]:
       s.badge(lx,ly,key,fill=INDIGO,size=16); s.text(lx+28,ly+5,text,size=18,anchor="start",fill=INK); s.arrow(lx-22,ly,kx,ky,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,420,seed=210+2)
    for x,l,t,n in [(80,"typing","Uni",3),(390,"searching","United",0),(700,"committed","United States",1)]:
       s.window(x,55,220,210,title=l,titlebar=22); _field(s,x+22,95,176,t);
       if n: _popup(s,x+22,160,176,n)
       if l=="searching": s.sparkle(x+185,120,size=14,fill=AMBER)
       s.label(x+110,315,l,size=20,fill=INK_SOFT,bg=PAPER)
    return s

def flow():
    s=Sketch(1000,430,seed=210+3)
    for x,title,fill,stroke in [(70,"model",TEAL_PALE,TEAL),(390,"control",INDIGO_PALE,INDIGO),(710,"callback",AMBER_PALE,AMBER)]:
       s.box(x,95,220,90,fill=fill,stroke=stroke,radius=14,shadow=True); s.text(x+110,132,title,size=23,fill=stroke); s.text(x+110,158,"public API",size=18,fill=stroke)
    s.arrow(292,140,388,140,bend=0,stroke=AMBER,width=3); s.arrow(612,140,708,140,bend=0,stroke=AMBER,width=3)
    _field(s,300,265,400,"typed + completion"); s.text(500,350,"250 ms delayed Service updates suggestions",size=21,fill=INK_SOFT)
    return s

def styling():
    s=Sketch(1000,390,seed=210+4)
    s.box(90,70,390,190,fill=PAPER,stroke=INK,radius=14,shadow=True)
    _field(s,130,115,300,"Berlin")
    s.box(570,55,330,260,fill=SLATE_PALE,stroke=MUTED,radius=12)
    for i,txt in enumerate(['.search-field', '.auto-completion-label', '.graphic-wrapper', '.history-button', '.search-field-popup', '.search-field-list-view', '.search-field-list-cell .text.middle', ':disabled-popup', ':left-node-visible / :right-node-visible / :no-side-nodes'][:7]): s.text(605,95+i*31,txt,size=18,anchor="start",fill=INK)
    return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"states.svg":states,"flow.svg":flow,"styling.svg":styling}
def generate():
    for name,factory in DRAWINGS.items(): factory().save(OUT/name)
if __name__=="__main__": generate()
