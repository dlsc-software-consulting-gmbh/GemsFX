from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "paging-grid-table-view"
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
 s=Sketch(1000,480,seed=221); s.window(60,35,880,400,title="PagingGridTableView"); _table(s,120,80,760,250,5); _pager(s,120,350,760); return s
def anatomy():
 s=Sketch(1120,560,seed=222); _table(s,70,75,520,290,5); _pager(s,70,392,520); keys=[("1",180,110,"GridTableView",95),("2",195,112,"columnsProperty",160),("3",315,260,"itemsOnCurrentPage",230),("4",410,420,"PagingControls",305),("5",540,340,"fillLastPage → minNumberOfRows",385),("6",500,462,"onOpenItem / loadingStatus bindings",470)]
 for k,x,y,t,ly in keys: s.badge(650,ly,k); s.text(682,ly+6,t,size=19,anchor="start"); s.arrow(630,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
 return s
def columns():
 s=Sketch(1000,430,seed=223); _table(s,90,70,500,275,4); s.box(650,90,260,210,fill=TEAL_PALE,stroke=TEAL,radius=12,shadow=True); s.text(780,130,"GridTableColumn",size=23,fill=TEAL,bold=True); _lines(s,685,170,180,4); s.arrow(645,190,585,165,bend=.1,stroke=AMBER,width=2.5,head=12); return s
def loading():
 s=Sketch(1000,430,seed=224); boxes=[("request","page + pageSize"),("loader","query / slice"),("response","items + total count"),("grid table","refresh rows")]
 for i,(a,b) in enumerate(boxes): x=70+i*225; s.box(x,135,175,100,fill=INDIGO_PALE if i%2==0 else TEAL_PALE,stroke=INDIGO if i%2==0 else TEAL,radius=12,shadow=True); s.text(x+87,168,a,size=20,bold=True); s.text(x+87,198,b,size=16,fill=INK_SOFT)
 for x in [245,470,695]: s.arrow(x,185,x+48,185,bend=0,stroke=AMBER,width=2.5,head=12)
 return s
def last_page():
 s=Sketch(1000,430,seed=225); _table(s,110,70,330,260,3); s.label(275,365,"fillLastPage = false",size=18,fill=INK_SOFT,bg=SLATE_PALE); _table(s,560,70,330,260,5); s.label(725,365,"fillLastPage = true",size=18,fill=INDIGO,bg=INDIGO_PALE); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"columns.svg":columns,"loading.svg":loading,"last-page.svg":last_page}
def generate(): save_all(DRAWINGS)
