"""Generates cartoon illustrations for the LoadingPane manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, GLASS, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TITLE_SIZE, LABEL_SIZE, TEXT_SIZE, SMALL_SIZE
OUT = Path(__file__).resolve().parent.parent / "graphics" / "loading-pane"

def _content(s,x,y,w,h):
    s.box(x,y,w,h,fill=PAPER,stroke="#CBD5E1",radius=12)
    for i in range(4): s.line(x+30,y+42+i*32,x+w-40-(i%2)*90,y+42+i*32,stroke="#B7C2D0",width=2.5)

def _spinner(s,cx,cy,r=42):
    s.raw(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="#D1D5DB" stroke-width="10"/>')
    s.raw(f'<path d="M {cx} {cy-r} A {r} {r} 0 0 1 {cx+r*.87:.1f} {cy+r*.5:.1f}" fill="none" stroke="{INDIGO}" stroke-width="10" stroke-linecap="round"/>')

def cover():
    s=Sketch(1000,480,seed=201); s.window(70,36,860,405,title="Dashboard"); _content(s,130,100,740,285); s.box(350,145,300,200,fill=SLATE_PALE,stroke=MUTED,radius=18,shadow=True); _spinner(s,500,235,48); s.text(500,318,"LOADING",size=TITLE_SIZE,fill=INDIGO); s.label(500,430,"Content is wrapped; status selects content, progress or error",size=LABEL_SIZE,fill=INK_SOFT); return s

def anatomy():
    s=Sketch(1120,540,seed=202); _content(s,70,70,560,380); s.box(230,155,240,190,fill=SLATE_PALE,stroke=MUTED,radius=15,shadow=True); _spinner(s,350,230,45); s.text(350,304,"progress wrapper",size=LABEL_SIZE,fill=INK_SOFT); keys=[('1',250,125,'content node',135),('2',350,230,'progressIndicator',215),('3',350,330,'progress-indicator-wrapper',300),('4',430,390,'error pane / errorNode',385),('5',570,90,'committedStatus controls visibility',465)]; lx=690
    for k,tx,ty,text,ly in keys:
        s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+30,ly+5,text,size=TEXT_SIZE,anchor='start',fill=INK); s.arrow(lx-25,ly,tx,ty,bend=.05,stroke=MUTED,width=1.8,head=9)
    return s

def states():
    s=Sketch(1000,410,seed=203)
    for i,(cap,kind) in enumerate([('OK','ok'),('LOADING','load'),('ERROR','err')]):
        x=70+i*310; s.box(x,55,240,230,fill=PAPER,radius=16,shadow=True)
        if kind=='ok': _content(s,x+28,95,184,135); s.text(x+120,250,'content visible',size=TEXT_SIZE,fill=TEAL)
        elif kind=='load': _spinner(s,x+120,160,44); s.text(x+120,250,'indicator visible',size=TEXT_SIZE,fill=INDIGO)
        else: s.raw(f'<circle cx="{x+120}" cy="145" r="40" fill="{ROSE_PALE}" stroke="{ROSE}" stroke-width="3"/>'); s.text(x+120,160,'!',size=42,fill=ROSE); s.text(x+120,230,'error text',size=TEXT_SIZE,fill=ROSE)
        s.label(x+120,326,cap,size=LABEL_SIZE,fill=INK_SOFT)
    s.label(500,380,'status is requested immediately; committedStatus may wait for commitDelay',size=TEXT_SIZE,fill=MUTED); return s

def delay():
    s=Sketch(1050,430,seed=204); y=210; s.line(120,y,900,y,stroke=MUTED,width=2.4); marks=[(140,'setStatus(LOADING)'),(380,'commitDelay 200 ms'),(620,'committedStatus=LOADING'),(850,'progress==1.0 → OK')]
    for x,t in marks: s.line(x,y-22,x,y+22,stroke=ROSE,width=2); s.label(x,y-48,t,size=SMALL_SIZE,fill=INK_SOFT,bg=PAPER)
    s.arrow(150,y+60,610,y+60,bend=0,stroke=AMBER,width=3,head=12); s.label(380,y+95,'fast operations can return before the spinner appears',size=TEXT_SIZE,fill=AMBER); _spinner(s,620,120,34); s.text(620,330,'commitStatusThread posts to Platform.runLater',size=TEXT_SIZE,fill=INK); return s

def sizing():
    s=Sketch(1000,430,seed=205)
    for i,(name,size) in enumerate([('SMALL',60),('MEDIUM',90),('LARGE',150)]):
        x=100+i*300; s.box(x,60,210,245,fill=PAPER,radius=14,shadow=True); s.box(x+55,y:=105,100,100,fill=SLATE_PALE,stroke=MUTED,radius=10); _spinner(s,x+105,155,size/3); s.label(x+105,338,name,size=LABEL_SIZE,fill=INDIGO)
    s.label(500,390,'CSS pseudo-classes :small, :medium and :large resize the wrapper',size=TEXT_SIZE,fill=MUTED); return s

DRAWINGS={'cover.svg':cover,'anatomy.svg':anatomy,'states.svg':states,'delay.svg':delay,'sizing.svg':sizing}
def generate():
    for n,f in DRAWINGS.items(): f().save(OUT/n)
if __name__=='__main__': generate(); print(f'wrote {len(DRAWINGS)} graphics to {OUT}')
