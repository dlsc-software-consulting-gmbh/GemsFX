from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "pop-over"
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
 s=Sketch(1000,480,seed=311); s.window(70,45,860,360,title="Owner window"); _button(s,435,330,120,'Owner',INDIGO_PALE); _popover(s,350,110,300,170,'top'); s.arrow(500,280,500,330,bend=0,stroke=AMBER,width=2.5,head=12); return s
def anatomy():
 s=Sketch(1120,560,seed=312); _button(s,260,410,110,'Owner',INDIGO_PALE); _popover(s,170,170,300,180,'top'); keys=[('1',320,145,'arrow path points at owner',95),('2',190,180,'background path',165),('3',220,215,'content BorderPane',235),('4',470,170,'border path',305),('5',320,365,'offset overlap',380),('6',255,178,'root StackPane copies style classes',460)]
 for k,x,y,t,ly in keys: s.badge(610,ly,k); s.text(642,ly+6,t,size=19,anchor='start'); s.arrow(590,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def geometry():
 s=Sketch(1000,430,seed=313); _popover(s,250,140,500,180,'top'); s.dimension(490,110,510,110,'arrowSize = 10',stroke=INDIGO,size=17,offset=26); s.dimension(285,137,365,137,'arrowIndent = 12',stroke=TEAL,size=17,offset=-28); s.dimension(250,140,290,180,'cornerRadius = 6',stroke=AMBER,size=17,offset=28); s.label(500,370,'The skin builds one path and inserts arrow segments for the computed location',size=20,fill=INK_SOFT,bg=SLATE_PALE); return s
def locations():
 s=Sketch(1000,430,seed=314); _button(s,450,185,100,'Owner',INDIGO_PALE); _popover(s,370,40,260,105,'top'); _popover(s,370,285,260,105,'top'); _popover(s,665,130,210,140,'left'); _popover(s,125,130,210,140,'left'); s.label(500,405,'Preferred arrowLocation may flip to stay on screen; computedArrowLocation reports the effective side',size=18,fill=INDIGO,bg=INDIGO_PALE); return s
def detach():
 s=Sketch(1000,430,seed=315); _button(s,180,300,100,'Owner',INDIGO_PALE); _popover(s,120,120,220,120,'top'); s.arrow(350,180,570,160,bend=.1,stroke=AMBER,width=2.5,head=12); s.box(590,95,290,170,fill=PAPER,stroke=INK,radius=14,shadow=True); s.label(735,125,'detached',size=18,fill=ROSE,bg=ROSE_PALE); _txt(s,735,185,'No arrow + autoHide false',19,ROSE,bold=True); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"geometry.svg":geometry,"locations.svg":locations,"detach.svg":detach}
def generate(): save_all(DRAWINGS)
