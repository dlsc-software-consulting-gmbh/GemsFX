"""Generated cartoon illustrations for the AvatarView manual."""
from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch
OUT = Path(__file__).resolve().parent.parent / "graphics" / "avatar-view"
avatar = "avatar"
LINE = "#B7C2D0"

def _lines(s,x,y,w,n=3):
    for i in range(n): s.line(x,y+i*25,x+w*(.68 if i%3==2 else 1),y+i*25,stroke=LINE,width=2.3)
def _screen(s,x,y,w,h,label,fill=INDIGO_PALE):
    s.box(x,y,w,h,fill=fill,stroke=INDIGO,radius=14,shadow=True); s.text(x+w/2,y+h/2+7,label,size=22,fill=INDIGO)
def _photo(s,x,y,w,h,clip="circle",fill=TEAL_PALE):
    if clip=="circle": s.blob(x+w/2,y+h/2,min(w,h)/2,min(w,h)/2,fill=fill,stroke=INK,shadow=True)
    else: s.box(x,y,w,h,fill=fill,stroke=INK,radius=18,shadow=True)
    s.line(x+20,y+h-35,x+w-20,y+30,stroke=TEAL,width=2.2); s.sparkle(x+w-38,y+42,size=11,fill=AMBER)
def _avatar(s,x,y,r,text=None,fill=INDIGO_PALE):
    s.blob(x,y,r,r,fill=fill,stroke=INK,shadow=True)
    if text: s.text(x,y+8,text,size=26,fill=INK,bold=True)
    else: s.blob(x,y-10,r*.25,r*.25,fill=PAPER,stroke=INK,width=2); s.box(x-r*.42,y+8,r*.84,r*.38,fill=PAPER,stroke=INK,radius=20)
def _svg_icon(s,x,y,w,h):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True); s.text(x+25,y+32,"<svg>",size=20,anchor="start",fill=INDIGO); s.shape([(x+w/2,y+70),(x+w-45,y+h-40),(x+45,y+h-40)],fill=TEAL_PALE,stroke=TEAL,width=3); s.sparkle(x+w-35,y+45,size=12,fill=AMBER)
def _before(s,x,y,w,h,pos=.5,vertical=False):
    s.box(x,y,w,h,fill=ROSE_PALE,stroke=INK,radius=12,shadow=True); s.box(x+w*pos if not vertical else x, y if not vertical else y+h*pos, w*(1-pos) if not vertical else w, h if not vertical else h*(1-pos), fill=TEAL_PALE,stroke=None,radius=0,opacity=.9)
    if not vertical: s.line(x+w*pos,y,x+w*pos,y+h,stroke=INK,width=5); s.blob(x+w*pos,y+h/2,22,22,fill=PAPER,stroke=INK)
    else: s.line(x,y+h*pos,x+w,y+h*pos,stroke=INK,width=5); s.blob(x+w/2,y+h*pos,22,22,fill=PAPER,stroke=INK)
def _masked(s,x,y,w,h):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True); s.box(x+25,y+45,w-50,h-90,fill=TEAL_PALE,stroke=TEAL,radius=10); s.box(x,y,90,h,fill=AMBER_PALE,stroke=AMBER,radius=12,opacity=.7); s.box(x+w-90,y,90,h,fill=AMBER_PALE,stroke=AMBER,radius=12,opacity=.7)

def cover():
    s=Sketch(1000,480,seed=420)
    s.window(60,35,880,400,title="AvatarView")
    if avatar=="avatar":
        _avatar(s,230,210,62,"LD",TEAL_PALE); _avatar(s,400,210,62,None,SLATE_PALE); _avatar(s,570,210,62,"AB",AMBER_PALE); s.arrow(650,210,760,210,bend=0,stroke=AMBER,width=3); s.text(805,218,"fallback",size=24,fill=AMBER)
    elif avatar=="photo":
        _photo(s,190,95,260,260,"circle"); s.box(520,130,260,190,fill=SLATE_PALE,stroke=INK,radius=12,shadow=True); s.dimension(520,345,780,345,"crop output",stroke=ROSE,size=17,offset=22); s.cursor(420,280,scale=1.2)
    elif avatar=="svg":
        _svg_icon(s,180,105,260,240); s.arrow(460,220,570,220,bend=0,stroke=AMBER,width=3); s.box(590,140,230,150,fill=INDIGO_PALE,stroke=INDIGO,radius=14,shadow=True); s.text(705,205,"JavaFX Image",size=24,fill=INDIGO)
    elif avatar=="before":
        _before(s,180,95,620,280,.48); s.cursor(485,240,scale=1.25)
    elif avatar=="masked":
        _masked(s,165,120,660,210); s.dimension(165,350,255,350,"fade",stroke=ROSE,size=17,offset=18); s.dimension(735,350,825,350,"fade",stroke=ROSE,size=17,offset=18)
    elif avatar=="screens":
        _screen(s,140,130,290,175,"Primary"); _screen(s,455,100,220,140,"Screen 1",TEAL_PALE); _screen(s,700,170,160,105,"Screen 2",AMBER_PALE)
    return s

def anatomy():
    s=Sketch(1120,540,seed=420+1); s.window(50,42,610,420,title="AvatarView")
    if avatar=="avatar":
        _avatar(s,205,205,58,"LD",TEAL_PALE); _avatar(s,355,205,58,None,SLATE_PALE); s.box(125,320,360,45,fill=INDIGO_PALE,stroke=INDIGO,radius=10)
        keys=[("1",205,205,"image / initials / icon wrapper",100),("2",205,148,"shape clip",155),("3",355,205,"fallback icon",210),("4",300,342,"styleN class from initials",265),("5",470,205,"size bound to dimensions",320)]
    elif avatar=="photo":
        _photo(s,160,100,250,250,"circle"); s.box(145,370,280,28,fill=SLATE_PALE,stroke=MUTED,radius=14); s.box(230,366,28,36,fill=AMBER_PALE,stroke=AMBER,radius=10)
        keys=[("1",285,225,"ImageBox with clip",100),("2",285,100,"border circle / rectangle",155),("3",250,382,"zoom slider",210),("4",170,210,"placeholder when empty",265),("5",330,285,"drag changes translation",320)]
    elif avatar=="svg":
        _svg_icon(s,180,115,260,230); s.box(470,150,120,90,fill=INDIGO_PALE,stroke=INDIGO,radius=12)
        keys=[("1",230,145,"svgUrl",100),("2",310,230,"jSVG document",155),("3",530,195,"ImageView",210),("4",430,115,"fitWidth / fitHeight",265),("5",380,320,"render scale listeners",320)]
    elif avatar=="before":
        _before(s,135,115,390,245,.52); keys=[("1",190,230,"before wrapper clip",100),("2",420,230,"after wrapper clip",155),("3",335,238,"divider",210),("4",335,238,"handle",265),("5",500,360,"orientation pseudo class",320)]
    elif avatar=="masked":
        _masked(s,115,155,440,160); keys=[("1",160,230,"left gradient clip",100),("2",335,230,"solid center clip",155),("3",515,230,"right gradient clip",210),("4",335,190,"content StackPane",265),("5",250,330,"fadingSize",320)]
    else:
        _screen(s,105,170,230,135,"Primary"); _screen(s,360,130,170,105,"S1",TEAL_PALE); s.box(190,210,90,60,fill=PAPER,stroke=ROSE,radius=6); s.box(395,165,70,45,fill=PAPER,stroke=ROSE,radius=6)
        keys=[("1",220,235,"screen bounds",100),("2",450,182,"wallpaper/background",155),("3",240,235,"live WindowView",210),("4",500,130,"visible area overlay",265),("5",330,330,"scaled union bounds",320)]
    lx=700
    for key,kx,ky,text,ly in keys:
        s.badge(lx,ly,key,fill=INDIGO,size=16); s.text(lx+28,ly+5,text,size=18,anchor="start",fill=INK); s.arrow(lx-22,ly,kx,ky,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def states():
    s=Sketch(1000,420,seed=420+2)
    if avatar=="avatar":
        for x,t,label,fill in [(145,"IMG","image",TEAL_PALE),(420,"LD","initials",AMBER_PALE),(695,None,"icon",SLATE_PALE)]: _avatar(s,x,145,55,t,fill); s.label(x,245,label,size=20,fill=INK_SOFT,bg=PAPER)
    elif avatar=="photo":
        _photo(s,100,95,190,190,"circle"); _photo(s,405,95,190,190,"rect"); s.arrow(690,250,760,185,bend=.15,stroke=ROSE,width=3); _photo(s,710,95,190,190,"circle",AMBER_PALE); s.text(500,345,"drag + zoom → delayed croppedImage",size=22,fill=AMBER)
    elif avatar=="svg":
        for x,label in [(105,"intrinsic"),(390,"fit box"),(675,"HiDPI reload")]: _svg_icon(s,x,95,220,190); s.label(x+110,320,label,size=20,fill=INK_SOFT,bg=PAPER)
    elif avatar=="before":
        _before(s,80,95,240,180,.25); _before(s,380,95,240,180,.5); _before(s,680,95,240,180,.78); s.text(500,340,"drag clamps dividerPosition to 0..1",size=22,fill=AMBER)
    elif avatar=="masked":
        for x,label in [(90,"left overflow"),(390,"centered"),(690,"right overflow")]: _masked(s,x,115,220,120); s.label(x+110,285,label,size=20,fill=INK_SOFT,bg=PAPER)
    else:
        _screen(s,95,110,230,150,"wallpaper"); _screen(s,390,110,230,150,"windows",TEAL_PALE); _screen(s,685,110,230,150,"shapes",AMBER_PALE); s.text(500,340,"screen and window lists trigger rebuild",size=22,fill=AMBER)
    return s

def flow():
    s=Sketch(1000,430,seed=420+3)
    for x,title,fill,stroke in [(70,"source",TEAL_PALE,TEAL),(390,"skin",INDIGO_PALE,INDIGO),(710,"output",AMBER_PALE,AMBER)]:
        s.box(x,90,220,90,fill=fill,stroke=stroke,radius=14,shadow=True); s.text(x+110,127,title,size=23,fill=stroke); s.text(x+110,153,"public state",size=18,fill=stroke)
    s.arrow(292,135,388,135,bend=0,stroke=AMBER,width=3); s.arrow(612,135,708,135,bend=0,stroke=AMBER,width=3)
    if avatar=="avatar": _avatar(s,430,260,45,"LD",TEAL_PALE); s.text(500,350,"image → initials → icon",size=22,fill=INK_SOFT)
    elif avatar=="photo": _photo(s,390,235,190,150,"circle"); s.text(500,350,"photoZoom and translate feed crop",size=22,fill=INK_SOFT)
    elif avatar=="svg": _svg_icon(s,385,230,210,150); s.text(500,350,"jSVG renders BufferedImage → FX Image",size=22,fill=INK_SOFT)
    elif avatar=="before": _before(s,365,225,260,150,.55); s.text(500,350,"dividerPosition controls both clips",size=22,fill=INK_SOFT)
    elif avatar=="masked": _masked(s,345,245,310,105); s.text(500,370,"translateX controls edge gradients",size=22,fill=INK_SOFT)
    else: _screen(s,370,235,260,145,"scaled union",TEAL_PALE); s.text(500,365,"real geometry scaled to fit",size=22,fill=INK_SOFT)
    return s

def layout():
    s=Sketch(1000,430,seed=420+4)
    if avatar=="avatar":
        _avatar(s,250,190,80,"AB",AMBER_PALE); s.dimension(170,300,330,300,"size = 50 default",stroke=ROSE,size=17,offset=22); s.text(610,185,"SQUARE uses arcSize\nROUND uses circle radius",size=22,fill=INK_SOFT)
    elif avatar=="photo":
        _photo(s,180,85,260,260,"circle"); s.dimension(180,370,440,370,"visible crop",stroke=ROSE,size=17,offset=22); s.arrow(620,280,700,190,bend=.2,stroke=ROSE,width=3); s.text(690,165,"photoTranslateX/Y",size=22,fill=ROSE)
    elif avatar=="svg":
        s.box(180,100,260,200,fill=SLATE_PALE,stroke=MUTED,radius=10); _svg_icon(s,235,130,150,140); s.dimension(180,330,440,330,"fitWidth / fitHeight",stroke=ROSE,size=17,offset=20); s.text(640,185,"0 means intrinsic SVG size",size=22,fill=INK_SOFT)
    elif avatar=="before":
        _before(s,180,95,600,230,.62); s.dimension(180,350,552,350,"dividerPosition × width",stroke=ROSE,size=17,offset=22)
    elif avatar=="masked":
        _masked(s,170,120,640,170); s.dimension(170,325,290,325,"fadingSize",stroke=ROSE,size=17,offset=20); s.dimension(690,325,810,325,"fadingSize",stroke=ROSE,size=17,offset=20)
    else:
        _screen(s,150,160,300,160,"0,0"); _screen(s,500,100,220,120,"+x,-y",TEAL_PALE); s.dimension(150,350,720,350,"union bounds × 0.75 scale",stroke=ROSE,size=17,offset=24)
    return s

def styling():
    s=Sketch(1000,390,seed=420+5)
    s.box(80,65,360,220,fill=PAPER,stroke=INK,radius=14,shadow=True)
    if avatar=="avatar": _avatar(s,260,175,58,"ST",TEAL_PALE)
    elif avatar=="photo": _photo(s,155,95,190,160,"circle")
    elif avatar=="svg": _svg_icon(s,145,95,200,160)
    elif avatar=="before": _before(s,120,100,260,150,.5)
    elif avatar=="masked": _masked(s,110,120,300,120)
    else: _screen(s,130,120,240,135,"screen")
    s.box(560,55,350,260,fill=SLATE_PALE,stroke=MUTED,radius=12)
    selectors = {
        "avatar":[".avatar-view",".style0 … .style4",".text-wrapper",".image-wrapper","-fx-avatar-size"],
        "photo":[".photo-view",":empty",":focused",".image-box",".border-circle"],
        "svg":[".svg-image-view","-fx-svg-url","-fx-fit-width","-fx-preserve-ratio"],
        "before":[".before-after-view",":horizontal",":vertical",".divider",".handle"],
        "masked":[".masked-view",".container","-fx-fading-size"],
        "screens":[".screens-view",".screen",".visible-area",".glass",".window"],
    }[avatar]
    for i,txt in enumerate(selectors): s.text(595,100+i*34,txt,size=19,anchor="start",fill=INK)
    return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"states.svg":states,"flow.svg":flow,"layout.svg":layout,"styling.svg":styling}
def generate():
    for name,factory in DRAWINGS.items(): factory().save(OUT/name)
if __name__=="__main__": generate()
