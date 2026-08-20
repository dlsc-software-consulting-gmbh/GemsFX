
"""Generated cartoon illustrations for the ChipView manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "chip-view"
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
    s=Sketch(1000,480,seed=230)
    s.window(60,34,880,400,title="ChipView")
    _chip(s,180,160,"Active",w=190,fill=INDIGO_PALE,stroke=INDIGO); _chip(s,430,160,"Owner: Alex",w=230); _chip(s,710,160,"Due",w=150,fill=AMBER_PALE,stroke=AMBER); s.cursor(810,235,scale=1.2)
    return s

def anatomy():
    s=Sketch(1120,540,seed=230+1)
    s.window(50,42,610,420,title="ChipView")
    _chip(s,180,180,"Priority",w=270,fill=INDIGO_PALE,stroke=INDIGO); s.blob(215,199,13,13,fill=TEAL_PALE,stroke=TEAL,width=2)
    lx=710
    for key,kx,ky,text,ly in [('1', 215, 199, 'optional graphic', 100), ('2', 300, 205, 'text property', 155), ('3', 430, 199, 'close icon from onClose', 210), ('4', 310, 180, 'pill container', 265), ('5', 350, 235, 'value passed to callback', 320)]:
       s.badge(lx,ly,key,fill=INDIGO,size=16); s.text(lx+28,ly+5,text,size=18,anchor="start",fill=INK); s.arrow(lx-22,ly,kx,ky,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,420,seed=230+2)
    for x,l,cl in [(110,"plain",False),(390,"closable",True),(670,"pressed",True)]:
       _chip(s,x,135,"Filter",w=210,fill=AMBER_PALE if l=="pressed" else INDIGO_PALE,stroke=AMBER if l=="pressed" else INDIGO,close=cl);
       s.label(x+105,245,l,size=20,fill=INK_SOFT,bg=PAPER)
    return s

def flow():
    s=Sketch(1000,430,seed=230+3)
    for x,title,fill,stroke in [(70,"model",TEAL_PALE,TEAL),(390,"control",INDIGO_PALE,INDIGO),(710,"callback",AMBER_PALE,AMBER)]:
       s.box(x,95,220,90,fill=fill,stroke=stroke,radius=14,shadow=True); s.text(x+110,132,title,size=23,fill=stroke); s.text(x+110,158,"public API",size=18,fill=stroke)
    s.arrow(292,140,388,140,bend=0,stroke=AMBER,width=3); s.arrow(612,140,708,140,bend=0,stroke=AMBER,width=3)
    _chip(s,380,260,"value",w=210,fill=INDIGO_PALE,stroke=INDIGO); s.text(500,340,"onClose receives getValue()",size=21,fill=INK_SOFT)
    return s

def styling():
    s=Sketch(1000,390,seed=230+4)
    s.box(90,70,390,190,fill=PAPER,stroke=INK,radius=14,shadow=True)
    _chip(s,130,130,".chip-view",w=240,fill=INDIGO_PALE,stroke=INDIGO)
    s.box(570,55,330,260,fill=SLATE_PALE,stroke=MUTED,radius=12)
    for i,txt in enumerate(['.chip-view', '> .chip-container', '> .chip-container > .label', '.close-icon', '.close-icon:hover', '.close-icon:pressed', '.close-icon > .ikonli-font-icon'][:7]): s.text(605,95+i*31,txt,size=18,anchor="start",fill=INK)
    return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"states.svg":states,"flow.svg":flow,"styling.svg":styling}
def generate():
    for name,factory in DRAWINGS.items(): factory().save(OUT/name)
if __name__=="__main__": generate()
