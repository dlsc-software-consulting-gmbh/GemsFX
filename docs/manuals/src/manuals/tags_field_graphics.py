
"""Generated cartoon illustrations for the TagsField manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "tags-field"
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
    s=Sketch(1000,480,seed=220)
    s.window(60,34,880,400,title="TagsField")
    _chip(s,130,125,"Germany"); _chip(s,270,125,"France"); _field(s,420,117,330,"Uni"); _popup(s,420,184,330,3); s.cursor(720,280,scale=1.2)
    return s

def anatomy():
    s=Sketch(1120,540,seed=220+1)
    s.window(50,42,610,420,title="TagsField")
    s.box(110,120,470,150,fill=PAPER,stroke=INK,radius=12,shadow=True); _chip(s,135,150,"Germany"); _chip(s,275,150,"France"); _field(s,135,210,360,"Uni"); _popup(s,135,285,360,2)
    lx=710
    for key,kx,ky,text,ly in [('1', 180, 170, 'tag view node', 95), ('2', 238, 170, 'close icon', 150), ('3', 330, 235, 'embedded editor', 205), ('4', 300, 315, 'suggestions exclude tags', 260), ('5', 430, 170, 'selected tag state', 315)]:
       s.badge(lx,ly,key,fill=INDIGO,size=16); s.text(lx+28,ly+5,text,size=18,anchor="start",fill=INK); s.arrow(lx-22,ly,kx,ky,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,420,seed=220+2)
    for x,l,c in [(90,"empty",0),(390,"filled",1),(690,"selected",2)]:
       s.box(x,105,230,90,fill=PAPER,stroke=INK,radius=12,shadow=True);
       if c: _chip(s,x+20,130,"Germany",fill=AMBER_PALE if c==2 else TEAL_PALE,stroke=AMBER if c==2 else TEAL);
       s.label(x+115,270,l,size=20,fill=INK_SOFT,bg=PAPER)
    s.text(500,340,"BACK_SPACE removes; shortcut+Z restores",size=20,fill=AMBER)
    return s

def flow():
    s=Sketch(1000,430,seed=220+3)
    for x,title,fill,stroke in [(70,"model",TEAL_PALE,TEAL),(390,"control",INDIGO_PALE,INDIGO),(710,"callback",AMBER_PALE,AMBER)]:
       s.box(x,95,220,90,fill=fill,stroke=stroke,radius=14,shadow=True); s.text(x+110,132,title,size=23,fill=stroke); s.text(x+110,158,"public API",size=18,fill=stroke)
    s.arrow(292,140,388,140,bend=0,stroke=AMBER,width=3); s.arrow(612,140,708,140,bend=0,stroke=AMBER,width=3)
    _chip(s,300,265,"Germany"); _chip(s,450,265,"France"); s.text(500,350,"Commands update the tags ObservableList",size=21,fill=INK_SOFT)
    return s

def styling():
    s=Sketch(1000,390,seed=220+4)
    s.box(90,70,390,190,fill=PAPER,stroke=INK,radius=14,shadow=True)
    _chip(s,130,115,"first"); _chip(s,270,115,"selected",fill=AMBER_PALE,stroke=AMBER,w=150)
    s.box(570,55,330,260,fill=SLATE_PALE,stroke=MUTED,radius=12)
    for i,txt in enumerate(['.tags-field', ':contains-focus', ':filled', '> .flow-pane', '.tag-view', '.tag-view:selected', '.tag-view.first / .middle / .last / .only', '.close-icon > .close'][:7]): s.text(605,95+i*31,txt,size=18,anchor="start",fill=INK)
    return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"states.svg":states,"flow.svg":flow,"styling.svg":styling}
def generate():
    for name,factory in DRAWINGS.items(): factory().save(OUT/name)
if __name__=="__main__": generate()
