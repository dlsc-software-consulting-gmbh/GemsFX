from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "dialog-pane"
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
 s=Sketch(1000,480,seed=301); s.window(60,35,880,400,title="Application"); s.box(60,35,880,400,fill=INK_SOFT,stroke='none',opacity=.18); _dialog(s,300,95,400,275,"Information","info",("OK",)); return s
def anatomy():
 s=Sketch(1120,560,seed=302); s.window(60,55,680,420,title="StackPane root"); s.box(60,55,680,420,fill=INK_SOFT,stroke='none',opacity=.12); _dialog(s,210,110,380,290,"Dialog title","confirm",("Yes","No")); keys=[("1",90,95,"DialogPane root covers scene",90),("2",170,105,"GlassPane blocks input",160),("3",270,120,"content-pane per Dialog",230),("4",400,170,"DialogHeader from factory",305),("5",405,250,"custom content node",380),("6",440,370,"DialogButtonBar footer",455)]
 for k,x,y,t,ly in keys: s.badge(780,ly,k); s.text(812,ly+6,t,size=19,anchor='start'); s.arrow(760,ly,x,y,bend=.05,stroke=MUTED,width=1.7,head=9)
 return s
def types():
 s=Sketch(1000,430,seed=303); kinds=[('Info','info'),('Warning','warning'),('Error','error'),('Confirm','confirm')]
 for i,(t,k) in enumerate(kinds): _dialog(s,50+i*235,70,195,235,t,k,("OK",));
 s.label(500,365,"INPUT uses text controls; BLANK omits default header/footer padding",size=20,fill=INK_SOFT,bg=SLATE_PALE); return s
def lifecycle():
 s=Sketch(1000,430,seed=304); boxes=[('showXxx / showNode','create Dialog'),('delay optional','showDialog'),('button press','validate + commit'),('onClose','future result')]
 for i,(a,b) in enumerate(boxes): x=70+i*225; s.box(x,135,175,100,fill=INDIGO_PALE if i%2==0 else TEAL_PALE,stroke=INDIGO if i%2==0 else TEAL,radius=12,shadow=True); s.text(x+87,168,a,size=18,bold=True); s.text(x+87,198,b,size=16,fill=INK_SOFT)
 for x in [245,470,695]: s.arrow(x,185,x+48,185,bend=0,stroke=AMBER,width=2.5,head=12)
 return s
def stacking():
 s=Sketch(1000,430,seed=305); s.window(80,55,840,320,title="Multiple dialogs"); s.box(80,55,840,320,fill=INK_SOFT,stroke='none',opacity=.12); _dialog(s,220,100,300,220,'Blocked','warning',('OK',)); _dialog(s,455,135,330,225,'Top dialog','error',('OK',)); s.label(500,395,"Only the newest dialog is active; older dialogs are blocked by a nested glass pane",size=20,fill=ROSE,bg=ROSE_PALE); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"types.svg":types,"lifecycle.svg":lifecycle,"stacking.svg":stacking}
def generate(): save_all(DRAWINGS)
