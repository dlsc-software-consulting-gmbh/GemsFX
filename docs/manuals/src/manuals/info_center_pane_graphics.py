from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "info-center-pane"
LINE="#B7C2D0"
def _lines(s,x,y,w,rows=3,gap=24,colour=LINE):
    for i in range(rows): s.line(x,y+i*gap,x+w*(.72 if i%3==2 else 1),y+i*gap,stroke=colour,width=2.4)
def _button(s,x,y,w,t,fill=PAPER,accent=False):
    s.box(x,y,w,34,fill=INDIGO_PALE if accent else fill,stroke=INDIGO if accent else LINE,radius=8); s.text(x+w/2,y+23,t,size=16,fill=INDIGO if accent else INK)
def _pager(s,x,y,w,current=3,pages=9):
    s.box(x,y,w,70,fill=PAPER,stroke=INK,radius=12,shadow=True)
    s.text(x+20,y+27,"Showing items 21 to 30 of 87.",size=16,anchor="start",fill=INK_SOFT)
    bx=x+210
    for i,t in enumerate(["First","Prev"]): _button(s,bx+i*62,y+32,55,t)
    for i in range(5): _button(s,bx+130+i*42,y+32,34,str(current+i-2),accent=(i==2))
    for i,t in enumerate(["Next","Last"]): _button(s,bx+350+i*62,y+32,55,t)
    s.box(x+w-125,y+22,100,34,fill=SLATE_PALE,stroke=LINE,radius=8); s.text(x+w-75,y+44,"10 / page",size=15)
def _list(s,x,y,w,h,rows=5):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True)
    for i in range(rows):
        yy=y+18+i*((h-36)/rows); s.box(x+16,yy,w-32,((h-42)/rows)-6,fill=SLATE_PALE if i%2 else PAPER,stroke=LINE,radius=7); s.text(x+36,yy+22,f"Item {i+1}",size=16,anchor="start")
def _table(s,x,y,w,h,rows=5):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True)
    cols=[.15,.45,.25,.15]; xx=x
    for c,frac in enumerate(cols):
        cw=w*frac; s.box(xx+4,y+10,cw-8,32,fill=INDIGO_PALE,stroke=INDIGO,radius=5); s.text(xx+cw/2,y+32,["#","Title","Type","Year"][c],size=14,fill=INDIGO,bold=True); xx+=cw
    for r in range(rows):
        yy=y+54+r*((h-66)/rows); s.line(x+8,yy,x+w-8,yy,stroke=LINE,width=1.4)
        s.text(x+28,yy+21,str(r+1),size=14); s.text(x+85,yy+21,"Record " + str(r+1),size=14,anchor="start"); s.text(x+w*.62,yy+21,"Info",size=14,anchor="start"); s.text(x+w*.88,yy+21,"2026",size=14,anchor="start")
def _filter(s,x,y,w,h):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True); s.text(x+18,y+32,"Title",size=22,anchor="start",fill=INDIGO,bold=True); s.text(x+18,y+58,"Subtitle",size=16,anchor="start",fill=INK_SOFT); s.box(x+w-230,y+20,190,34,fill=SLATE_PALE,stroke=LINE,radius=8); s.text(x+w-215,y+42,"search text",size=15,anchor="start")
    for i,g in enumerate(["Role","Birthday","Status"]): _button(s,x+18+i*125,y+82,110,g)
    for i,c in enumerate(["Parent","1970-1980","\"smith\"","Clear Filter"]): _button(s,x+18+i*130,y+138,115,c,fill=TEAL_PALE if i<3 else ROSE_PALE,accent=False)
def _notify(s,x,y,w,title="Mail",typ="info",stack=False):
    fill={"info":PAPER,"warning":AMBER_PALE,"danger":ROSE_PALE,"success":TEAL_PALE}.get(typ,PAPER)
    if stack:
        s.box(x+18,y+18,w-36,76,fill=SLATE_PALE,stroke=LINE,radius=10); s.box(x+9,y+9,w-18,76,fill=SLATE_PALE,stroke=LINE,radius=10)
    s.box(x,y,w,86,fill=fill,stroke=INK,radius=12,shadow=True); s.blob(x+28,y+42,14,14,fill=INDIGO_PALE,stroke=INDIGO); s.text(x+52,y+30,title,size=17,anchor="start",bold=True); s.text(x+52,y+55,"A short summary of the event",size=15,anchor="start",fill=INK_SOFT); s.text(x+w-22,y+30,"now",size=13,anchor="end",fill=MUTED)
def save_all(drawings):
    for name,fn in drawings.items(): fn().save(OUT/name)

def _center(s,x,y,w,h,side="right"):
 s.window(x,y,w,h,title="Application"); _lines(s,x+40,y+70,w-180,4); ix=x+w-250 if side=="right" else x+20; s.box(ix,y+45,230,h-70,fill=INK_SOFT,stroke=INK,radius=14,shadow=True,opacity=.25); _notify(s,ix+18,y+75,190,"Mail","info",True); _notify(s,ix+18,y+180,190,"Calendar","warning",False); _notify(s,ix+18,y+285,190,"Build","success",False)
def cover():
 s=Sketch(1000,480,seed=251); _center(s,70,40,860,390,"right"); s.cursor(830,235,1.1); return s
def anatomy():
 s=Sketch(1120,560,seed=252); _center(s,60,55,610,410,"right"); keys=[("1",120,130,"content node fills pane",95),("2",475,105,"InfoCenterView overlay",165),("3",500,145,"NotificationGroup",235),("4",510,210,"NotificationView stack",305),("5",640,80,"position controls slide side",385),("6",530,330,"auto-hide / pinned state",465)]
 for k,x,y,t,ly in keys: s.badge(720,ly,k); s.text(752,ly+6,t,size=19,anchor="start"); s.arrow(700,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def model():
 s=Sketch(1000,430,seed=253); boxes=[("NotificationGroup","name, sortOrder, pinned"),("Notification","title, summary, time, type"),("NotificationView","graphic, content, actions"),("NotificationAction","callback → behaviour")]
 for i,(a,b) in enumerate(boxes): x=70+i*225; s.box(x,120,175,110,fill=INDIGO_PALE if i%2==0 else TEAL_PALE,stroke=INDIGO if i%2==0 else TEAL,radius=12,shadow=True); s.text(x+87,156,a,size=17,bold=True); s.text(x+87,190,b,size=14,fill=INK_SOFT)
 for x in [245,470,695]: s.arrow(x,175,x+48,175,bend=0,stroke=AMBER,width=2.4,head=11)
 return s
def stacking():
 s=Sketch(1000,430,seed=254); _notify(s,120,120,260,"Mail","info",True); s.label(250,255,"collapsed stack",size=19,fill=INK_SOFT,bg=SLATE_PALE); _notify(s,620,65,260,"Mail 1","info",False); _notify(s,620,170,260,"Mail 2","info",False); _notify(s,620,275,260,"Mail 3","info",False); s.label(750,390,"expanded group",size=19,fill=INDIGO,bg=INDIGO_PALE); s.arrow(400,170,590,170,bend=.1,stroke=AMBER,width=2.5,head=12); return s
def positions():
 s=Sketch(1000,430,seed=255); _center(s,55,55,400,300,"left"); _center(s,545,55,400,300,"right"); s.label(255,390,"TOP/CENTER/BOTTOM_LEFT slide from left",size=17,fill=INDIGO,bg=INDIGO_PALE); s.label(745,390,"TOP/CENTER/BOTTOM_RIGHT slide from right",size=17,fill=INDIGO,bg=INDIGO_PALE); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"model.svg":model,"stacking.svg":stacking,"positions.svg":positions}
def generate(): save_all(DRAWINGS)
