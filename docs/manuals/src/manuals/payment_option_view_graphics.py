from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "payment-option-view"
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
 s=Sketch(1000,480,seed=321); s.window(70,45,860,360,title="Checkout"); _card(s,170,120,220,132,'VISA',INDIGO_PALE); _card(s,430,120,220,132,'PAYPAL',TEAL_PALE); _card(s,690,120,150,90,'APPLE',SLATE_PALE); s.label(500,330,'Bundled payment artwork is rendered through ImageView',size=22,fill=INDIGO,bg=INDIGO_PALE); return s
def anatomy():
 s=Sketch(1120,560,seed=322); _card(s,140,150,360,216,'MASTERCARD',AMBER_PALE); keys=[('1',160,170,'ImageView subclass',100),('2',320,210,'option selects file name',175),('3',405,170,'theme appends -dark or -light',250),('4',500,258,'PNG resource under paymentoptions',325),('5',320,365,'fitWidth defaults to 100',405),('6',490,362,'preserveRatio=true',480)]
 for k,x,y,t,ly in keys: s.badge(620,ly,k); s.text(652,ly+6,t,size=19,anchor='start'); s.arrow(600,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def options():
 s=Sketch(1000,430,seed=323); names=['CREDIT_CARD','VISA','MASTERCARD','AMEX','PAYPAL','APPLE_PAY','SEPA','BITCOIN','STRIPE'];
 for i,n in enumerate(names): _card(s,65+(i%3)*300,55+(i//3)*120,230,82,n, [INDIGO_PALE,TEAL_PALE,AMBER_PALE][i%3])
 return s
def themes():
 s=Sketch(1000,430,seed=324); _card(s,180,115,260,156,'DARK',INDIGO_PALE); _card(s,560,115,260,156,'LIGHT',PAPER); s.label(310,320,'Theme.DARK → solid colored resource',size=18,fill=INDIGO,bg=INDIGO_PALE); s.label(690,320,'Theme.LIGHT → white background resource',size=18,fill=INK_SOFT,bg=SLATE_PALE); return s
def sizing():
 s=Sketch(1000,430,seed=325); _card(s,170,130,200,120,'100 px',TEAL_PALE); _card(s,560,80,320,192,'scaled',AMBER_PALE); s.dimension(170,275,370,275,'fitWidth',stroke=TEAL,size=17,offset=24); s.dimension(900,80,900,272,'ratio preserved',stroke=AMBER,size=17,offset=28); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"options.svg":options,"themes.svg":themes,"sizing.svg":sizing}
def generate(): save_all(DRAWINGS)
