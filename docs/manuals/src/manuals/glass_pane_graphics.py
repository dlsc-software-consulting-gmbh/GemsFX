"""Generates cartoon illustrations for the GlassPane manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, GLASS, INDIGO, INK, INK_SOFT, MUTED, PAPER, ROSE, SLATE_PALE, TEAL, Sketch, TITLE_SIZE, LABEL_SIZE, TEXT_SIZE, SMALL_SIZE
OUT=Path(__file__).resolve().parent.parent/'graphics'/'glass-pane'

def _content(s,x,y,w,h):
    s.window(x,y,w,h,title='App',shadow=True)
    for i in range(5): s.line(x+45,y+70+i*30,x+w-55-(i%2)*100,y+70+i*30,stroke='#B7C2D0',width=2.5)

def cover():
    s=Sketch(1000,480,seed=401); _content(s,80,38,840,400); s.box(82,64,836,374,fill=GLASS,stroke=None,radius=6,opacity=.48); s.text(500,245,'input blocked',size=34,fill=PAPER); s.cursor(710,210,scale=1.5); s.label(500,425,'GlassPane is a StackPane overlay with optional fade in/out',size=LABEL_SIZE,fill=INK_SOFT); return s

def anatomy():
    s=Sketch(1120,540,seed=402); _content(s,70,65,560,380); s.box(72,91,556,354,fill=GLASS,stroke=None,radius=5,opacity=.5); keys=[('1',240,145,'StackPane root .glass-pane',130),('2',360,240,'black CSS background',215),('3',530,410,'mouseTransparent = false',300),('4',75,95,'max width / height Infinity',385),('5',500,120,'hide property controls visible + opacity',465)]; lx=690
    for k,tx,ty,text,ly in keys: s.badge(lx,ly,k,fill=INDIGO,size=16); s.text(lx+30,ly+5,text,size=TEXT_SIZE,anchor='start',fill=INK); s.arrow(lx-25,ly,tx,ty,bend=.04,stroke=MUTED,width=1.8,head=9)
    return s

def states():
    s=Sketch(1000,410,seed=403)
    for i,(cap,op) in enumerate([('hide = true',0),('fading',.25),('hide = false',.5)]):
        x=70+i*310; _content(s,x,55,240,230)
        if op: s.box(x+2,81,236,204,fill=GLASS,stroke=None,radius=5,opacity=op)
        s.label(x+120,330,cap,size=LABEL_SIZE,fill=INK_SOFT)
    s.label(500,380,'fadeInOut chooses animation or immediate visible/opacity updates',size=TEXT_SIZE,fill=MUTED); return s

def fade():
    s=Sketch(1040,410,seed=404); s.line(120,225,900,225,stroke=MUTED,width=2.5); s.arrow(160,160,850,160,bend=0,stroke=AMBER,width=3,head=12); s.label(500,128,'fadeInOutDuration = 100 ms by default',size=LABEL_SIZE,fill=AMBER,bg=PAPER)
    for x,t in [(160,'from 0'),(500,'blockingOpacity'),(850,'setVisible')]: s.line(x,200,x,250,stroke=ROSE,width=2); s.label(x,282,t,size=SMALL_SIZE,fill=INK_SOFT,bg=PAPER)
    return s

def css():
    s=Sketch(1000,430,seed=405); s.box(210,90,260,240,fill=PAPER,radius=15,shadow=True); s.text(340,145,'-fx-blocking-opacity',size=TEXT_SIZE,fill=INDIGO); s.text(340,190,'-fx-fade-in-out',size=TEXT_SIZE,fill=INDIGO); s.text(340,235,'-fx-fade-in-out-duration',size=TEXT_SIZE,fill=INDIGO); s.box(560,90,260,240,fill=SLATE_PALE,radius=15,shadow=True); s.text(690,170,'background: black',size=TEXT_SIZE,fill=INK); s.text(690,220,'max: Infinity',size=TEXT_SIZE,fill=INK_SOFT); s.label(500,380,'CSS sets the color; properties set the opacity and animation',size=TEXT_SIZE,fill=MUTED); return s

DRAWINGS={'cover.svg':cover,'anatomy.svg':anatomy,'states.svg':states,'fade.svg':fade,'css.svg':css}
def generate():
    for n,f in DRAWINGS.items(): f().save(OUT/n)
if __name__=='__main__': generate(); print(f'wrote {len(DRAWINGS)} graphics to {OUT}')
