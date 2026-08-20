"""Generates cartoon illustrations for the AutoscrollListView manual."""

from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch

OUT = Path(__file__).resolve().parent.parent / "graphics" / "autoscroll-list-view"
CONTROL="AutoscrollListView"; KIND="list"; ITEMS=['Task A', 'Task B', 'Task C', 'Task D', 'Task E']; LEGEND=['ListView', 'hot zones', 'VirtualFlow', 'scroll thread']; LINE="#B7C2D0"

def _lines(s,x,y,w,rows=3,gap=22):
    for i in range(rows): s.line(x,y+i*gap,x+w*(1 if i%3 else .72),y+i*gap,stroke=LINE,width=2.2)

def _card(s,x,y,w,h,text,fill=PAPER,selected=False):
    s.box(x,y,w,h,fill=INDIGO_PALE if selected else fill,stroke=INDIGO if selected else INK,radius=9,shadow=selected)
    s.text(x+w/2,y+h/2+7,text,size=18 if len(text)<10 else 16,fill=INDIGO if selected else INK)

def _draw_table(s,x,y,w,h,grid=False):
    s.box(x,y,w,h,fill=PAPER,radius=12,shadow=True)
    cols=len(ITEMS); ch=46
    for i,it in enumerate(ITEMS):
        cx=x+i*w/cols
        s.box(cx,y,w/cols,ch,fill=SLATE_PALE,radius=5,stroke=LINE,width=2)
        s.text(cx+w/cols/2,y+30,it,size=17,fill=INK)
    rows=4
    for r in range(rows):
        yy=y+ch+r*(h-ch)/rows
        fill=TEAL_PALE if r%2==0 else PAPER
        s.box(x,yy,w,(h-ch)/rows,fill=fill if grid else (SLATE_PALE if r%2 else PAPER),radius=3,stroke=LINE,width=1.8)
        for c in range(cols):
            s.line(x+c*w/cols,yy,x+c*w/cols,yy+(h-ch)/rows,stroke=LINE,width=1.3)
            _lines(s,x+c*w/cols+14,yy+21,w/cols-28,1)
    if grid:
        s.box(x+w-120,y+h-80,90,50,fill=AMBER_PALE,radius=8,shadow=True)
        s.text(x+w-75,y+h-49,"load",size=18,fill=AMBER)
    else:
        s.dimension(x+20,y+h+28,x+w-20,y+h+28,"resize to content",stroke=ROSE,size=17,offset=15)

def _draw_list(s,x,y,w,h):
    s.box(x,y,w,h,fill=PAPER,radius=12,shadow=True)
    for i,it in enumerate(ITEMS[:6]):
        yy=y+24+i*48
        _card(s,x+28,yy,w-56,36,it,fill=TEAL_PALE if i%2 else PAPER,selected=i==2)
    s.box(x+6,y+8,w-12,42,fill=ROSE_PALE,stroke=ROSE,radius=8,opacity=.45)
    s.text(x+w-55,y+35,"hot",size=18,fill=ROSE)
    s.box(x+6,y+h-50,w-12,42,fill=ROSE_PALE,stroke=ROSE,radius=8,opacity=.45)
    s.text(x+w-55,y+h-23,"hot",size=18,fill=ROSE)
    s.cursor(x+w-85,y+h-45,scale=1.1)

def _draw_board(s,x,y,w,h):
    colw=(w-50)/4
    for i,it in enumerate(ITEMS[:4]):
        cx=x+i*(colw+14)
        s.box(cx,y,colw,h,fill=SLATE_PALE,radius=12,shadow=True)
        s.text(cx+colw/2,y+32,it,size=18,fill=INK)
        for r in range(3): _card(s,cx+12,y+55+r*70,colw-24,50,"card",fill=PAPER,selected=(i==2 and r==1))
    s.arrow(x+colw*1.5,y+180,x+colw*2.4,y+180,bend=.2,stroke=AMBER,width=3,head=13)

def _draw_strip(s,x,y,w,h):
    s.box(x,y,w,h,fill=SLATE_PALE,radius=14,shadow=True)
    s.box(x+20,y+20,w-40,h-40,fill=PAPER,radius=10)
    cellw=92
    for i,it in enumerate(ITEMS): _card(s,x+45+i*(cellw+10),y+45,cellw,50,it,fill=PAPER,selected=i==2)
    s.box(x+8,y+h/2-20,28,40,fill=AMBER_PALE,radius=8); s.text(x+22,y+h/2+7,"‹",size=28,fill=AMBER)
    s.box(x+w-36,y+h/2-20,28,40,fill=AMBER_PALE,radius=8); s.text(x+w-22,y+h/2+7,"›",size=28,fill=AMBER)
    s.dimension(x+40,y+h+25,x+180,y+h+25,"fade",stroke=ROSE,size=16,offset=14)

def _draw_tree(s,x,y,w,h):
    pts={"Root":(x+w/2,y+35),"A":(x+w/2-160,y+145),"B":(x+w/2,y+145),"C":(x+w/2+160,y+145),"A1":(x+w/2-210,y+255),"A2":(x+w/2-110,y+255),"B1":(x+w/2,y+255)}
    for a,b in [('Root','A'),('Root','B'),('Root','C'),('A','A1'),('A','A2'),('B','B1')]:
        s.arrow(pts[a][0],pts[a][1]+28,pts[b][0],pts[b][1]-28,bend=.05,stroke=MUTED,width=2,head=8)
    for name,(px,py) in pts.items(): _card(s,px-45,py-24,90,48,name,fill=TEAL_PALE if name=='Root' else PAPER,selected=name=='B')
    s.label(x+w/2,y+h-20,"regular / compact • four directions",size=19,fill=INK_SOFT)

def _body(s,x,y,w,h,detail=False):
    if KIND=='table': _draw_table(s,x,y,w,h,grid=False)
    elif KIND=='grid': _draw_table(s,x,y,w,h,grid=True)
    elif KIND=='list': _draw_list(s,x,y,w,h)
    elif KIND=='board': _draw_board(s,x,y,w,h)
    elif KIND=='strip': _draw_strip(s,x,y,w,h)
    else: _draw_tree(s,x,y,w,h)

def cover():
    s=Sketch(1000,480,seed=211+len(CONTROL))
    s.window(60,35,880,410,title="GemsFX")
    _lines(s,105,100,330,4)
    s.box(105,240,300,115,fill=SLATE_PALE,radius=12,stroke=LINE,width=2)
    _body(s,480,105,390,270)
    s.badge(875,112,"API",fill=INDIGO,size=18); s.sparkle(875,230,size=17,fill=AMBER); s.cursor(820,345,scale=1.1)
    return s

def anatomy():
    s=Sketch(1100,560,seed=231+len(CONTROL))
    _body(s,70,80,540,350,True)
    lx=690
    coords=[(170,110),(330,160),(470,230),(280,330)]
    for i,txt in enumerate(LEGEND,1):
        ly=105+i*72; s.badge(lx,ly,str(i),fill=INDIGO,size=16); s.text(lx+32,ly+6,txt,size=20,anchor='start',fill=INK)
        tx,ty=coords[i-1]; s.arrow(lx-22,ly,tx,ty,bend=.05,stroke=MUTED,width=1.7,head=9)
    return s

def behaviour():
    s=Sketch(1000,430,seed=251+len(CONTROL))
    for n,x in enumerate((45,365,685)):
        s.window(x,45,270,255,title='',titlebar=22,shadow=True)
        _body(s,x+25,95,220,155)
        if n==1: s.cursor(x+190,250,scale=1.0)
        if n==2: s.sparkle(x+210,120,size=16,fill=TEAL)
        s.label(x+135,340,['model','interaction','updated view'][n],size=20,fill=INK_SOFT)
    s.arrow(322,175,356,175,bend=0,stroke=AMBER,width=3,head=12); s.arrow(642,175,676,175,bend=0,stroke=AMBER,width=3,head=12)
    return s

def flow():
    s=Sketch(1000,430,seed=271+len(CONTROL))
    labels=['items / model','factory / skin','cells / links','events']
    for i,l in enumerate(labels):
        x=80+i*220; s.box(x,155,160,82,fill=[INDIGO_PALE,TEAL_PALE,AMBER_PALE,ROSE_PALE][i],radius=14,shadow=True); s.text(x+80,204,l,size=21,fill=INK)
        if i<3: s.arrow(x+170,196,x+210,196,bend=0,stroke=MUTED,width=2.6,head=12)
    s.label(500,305,'observable data drives generated nodes',size=22,fill=INK_SOFT)
    return s

def styling():
    s=Sketch(1000,470,seed=291+len(CONTROL))
    _body(s,80,95,360,240)
    s.box(540,75,360,300,fill=SLATE_PALE,radius=14,shadow=True); s.text(570,122,'CSS hooks',size=25,anchor='start')
    for i,txt in enumerate(LEGEND):
        y=166+i*44; s.badge(575,y,str(i+1),fill=TEAL,size=14); s.text(605,y+6,txt,size=19,anchor='start')
    s.dimension(100,380,430,380,'layout width',stroke=ROSE,size=17,offset=18)
    return s

def recipes():
    s=Sketch(1000,470,seed=311+len(CONTROL))
    s.window(70,45,860,360,title='Recipe board')
    for i,(txt,fill) in enumerate((('model',INDIGO_PALE),('factory',TEAL_PALE),('style',AMBER_PALE),('events',ROSE_PALE))):
        x=115+(i%2)*390; y=110+(i//2)*135
        s.box(x,y,320,95,fill=fill,radius=14,shadow=True); s.text(x+28,y+38,txt,size=25,anchor='start'); _lines(s,x+30,y+62,245,2,gap=20)
    s.cursor(835,342,scale=1.2); return s

def generate():
    OUT.mkdir(parents=True,exist_ok=True)
    for name,fn in {"cover":cover,"anatomy":anatomy,"behaviour":behaviour,"styling":styling,"recipes":recipes}.items():
        if name=='flow':
            # keep exactly five SVGs by using flow as behaviour adjunct in code only when saved below for manuals that reference it
            pass
        fn().save(OUT / f"{name}.svg")
