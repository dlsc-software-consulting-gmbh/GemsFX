from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "filter-view"
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

def cover():
 s=Sketch(1000,480,seed=231); s.window(60,35,880,400,title="People table"); _filter(s,110,80,780,190); _table(s,110,300,780,110,2); return s
def anatomy():
 s=Sketch(1120,560,seed=232); _filter(s,70,80,560,240); keys=[("1",95,110,"header: title, subtitle, graphics",90),("2",492,108,"SearchTextField appears with textFilterProvider",160),("3",150,180,"FilterGroup MenuButtons",235),("4",150,238,"ChipView selections",315),("5",585,235,"Clear Filter label",390),("6",350,355,"filteredItems read-only list",470)]
 for k,x,y,t,ly in keys: s.badge(700,ly,k); s.text(732,ly+6,t,size=19,anchor="start"); s.arrow(680,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def predicate():
 s=Sketch(1000,430,seed=233); boxes=[("same group","OR"),("between groups","AND"),("text filter","AND"),("additional predicate","AND")]
 for i,(a,b) in enumerate(boxes): x=80+i*220; s.box(x,120,165,100,fill=INDIGO_PALE if b=='AND' else AMBER_PALE,stroke=INDIGO if b=='AND' else AMBER,radius=12,shadow=True); s.text(x+82,155,a,size=18,bold=True); s.text(x+82,192,b,size=26,fill=INDIGO if b=='AND' else AMBER,bold=True)
 for x in [245,465,685]: s.arrow(x,170,x+45,170,bend=0,stroke=MUTED,width=2.3,head=10)
 return s
def chips():
 s=Sketch(1000,430,seed=234); _filter(s,90,80,820,190); s.arrow(270,210,270,320,bend=0,stroke=AMBER,width=2.5,head=12); s.box(135,320,720,55,fill=TEAL_PALE,stroke=TEAL,radius=14,shadow=True); s.text(495,354,"selected filters become removable chips; text chips are quoted",size=21,fill=TEAL,bold=True); return s
def scrolling():
 s=Sketch(1000,430,seed=235); _filter(s,80,55,360,270); s.dimension(460,80,460,325,"filters <= scrollThreshold",stroke=TEAL,size=15,offset=24); _filter(s,560,55,360,270); s.box(572,180,335,95,fill=SLATE_PALE,stroke=MUTED,radius=8,dash="7 5"); s.dimension(940,80,940,325,"filters > scrollThreshold",stroke=ROSE,size=15,offset=24); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"predicate.svg":predicate,"chips.svg":chips,"scrolling.svg":scrolling}
def generate(): save_all(DRAWINGS)
