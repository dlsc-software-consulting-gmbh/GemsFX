from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "enhanced-label"
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

def _label(s,x,y,w,text='EnhancedLabel',selected=False): s.box(x,y,w,44,fill=INDIGO_PALE if selected else PAPER,stroke=INDIGO if selected else LINE,radius=8,shadow=True); s.text(x+16,y+29,text,size=18,fill=INDIGO if selected else INK,anchor='start',bold=selected)
def cover():
 s=Sketch(1000,480,seed=351); s.window(70,45,860,360,title="Copyable labels"); _label(s,190,150,620,'Double click to select, then copy',True); _label(s,190,230,620,'Custom supplier copies only ID: 12345678',False); return s
def anatomy():
 s=Sketch(1120,560,seed=352); _label(s,110,185,560,'Customer account #12345678',True); keys=[('1',120,205,'Label subclass',100),('2',180,205,'enhanced-label style class',175),('3',360,205,'selected pseudo class',250),('4',610,205,'ContextMenu with copy item',325),('5',405,265,'copyContentSupplier',405),('6',500,205,'onCopyAction',480)]
 for k,x,y,t,ly in keys: s.badge(730,ly,k); s.text(762,ly+6,t,size=19,anchor='start'); s.arrow(710,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def interactions():
 s=Sketch(1000,430,seed=353); _label(s,120,100,320,'plain label',False); s.arrow(450,120,560,120,bend=.1,stroke=AMBER,width=2.5,head=12); _label(s,610,100,300,'selected',True); s.label(500,230,'double-click or context-menu request selects the label',size=21,fill=INDIGO,bg=INDIGO_PALE); s.label(500,310,'losing focus clears selected',size=19,fill=INK_SOFT,bg=SLATE_PALE); return s
def clipboard():
 s=Sketch(1000,430,seed=354); boxes=[('shortcut+C','copy'),('context menu','copy text'),('supplier','clipboard string'),('onCopyAction','override')]
 for i,(a,b) in enumerate(boxes): x=80+i*220; s.box(x,130,165,100,fill=INDIGO_PALE if i%2==0 else TEAL_PALE,stroke=INDIGO if i%2==0 else TEAL,radius=12,shadow=True); s.text(x+82,164,a,size=18,bold=True); s.text(x+82,197,b,size=15,fill=INK_SOFT)
 for x in [245,465,685]: s.arrow(x,180,x+45,180,bend=0,stroke=AMBER,width=2.4,head=11)
 return s
def styling():
 s=Sketch(1000,430,seed=355); _label(s,160,120,300,'normal',False); _label(s,540,120,300,'selected',True); s.label(500,270,'.enhanced-label:selected uses accent background and white text',size=21,fill=INDIGO,bg=INDIGO_PALE); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"interactions.svg":interactions,"clipboard.svg":clipboard,"styling.svg":styling}
def generate(): save_all(DRAWINGS)
