# Content of the GlassPane developer manual.
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, PageBreak, Para, Property, PropertyTable, Section, Table
G='glass-pane'
MANUAL=Manual(control='GlassPane',package='com.dlsc.gemsfx',subtitle='A semi-transparent overlay that blocks input',abstract=('GlassPane is a StackPane overlay with a black CSS background, a separate blockingOpacity property and optional fade in/out animation. It is used by DrawerStackPane and can also be layered directly over application content.'),cover_svg=f'{G}/cover.svg',cover_caption='GlassPane darkens the UI and remains mouse-opaque while visible.',chapters=[
Chapter('Introduction',[Para('<b>GlassPane</b> extends <font face="Courier">StackPane</font>. It is a lightweight overlay that blocks mouse input to content below it and optionally fades between hidden and shown states.'),Bullets(['Root style class is <font face="Courier">glass-pane</font>.','The constructor sets <font face="Courier">mouseTransparent</font> to false and <font face="Courier">visible</font> to false.','The <font face="Courier">hide</font> property controls showing and hiding.','A separate <font face="Courier">blockingOpacity</font> lets fade animation use opacity without losing the configured target opacity.']),Code('''StackPane root = new StackPane(content);
GlassPane glass = new GlassPane();
root.getChildren().add(glass);

// show the overlay
glass.setHide(false);''')]),
Chapter('Getting started',[Para('Add the glass pane as the last child of a StackPane or another layout where it covers the content. Since the CSS sets max width and height to Infinity, common panes can stretch it to fill the available area.'),Code('''GlassPane glassPane = new GlassPane();
glassPane.setBlockingOpacity(0.45);
glassPane.setFadeInOut(true);
glassPane.setFadeInOutDuration(Duration.millis(150));

glassPane.setOnMouseClicked(evt -> glassPane.setHide(true));'''),Figure(f'{G}/states.svg','The hide property switches the pane between hidden, fading and shown.')]),
Chapter('Anatomy',[Figure(f'{G}/anatomy.svg','GlassPane is a StackPane plus styleable animation properties.'),Table(['Part','Source','Description'],[['Root node','StackPane','The overlay container and event blocker.'],['Background','glass-pane.css','Black background color.'],['Opacity','blockingOpacity','Target opacity while shown.'],['Fade transition','FadeTransition','Bound to fadeInOutDuration and installed on the glass pane.'],['Visibility','hide listener','Visible is true while shown and during fade-out.']],widths=[24,26,50])]),
Chapter('Control API',[PropertyTable([Property('blockingOpacity','DoubleProperty','0.5','Target opacity while the overlay blocks input. Values outside 0..1 are rejected by restoring the old value. Styleable.'),Property('fadeInOut','BooleanProperty','false','Whether show/hide transitions animate opacity. Styleable.'),Property('fadeInOutDuration','ObjectProperty&lt;Duration&gt;','100 ms','Duration of the fade transition. Styleable.'),Property('hide','BooleanProperty','true','True means hidden; false means shown. This property is not styleable.')])]),
Chapter('Behaviour',[Figure(f'{G}/fade.svg','Fade animation uses blockingOpacity as its target value.'),Table(['When hide changes','fadeInOut false','fadeInOut true'],[['false','Opacity becomes blockingOpacity and visible becomes true.','Visible becomes true; transition runs from 0 to blockingOpacity.'],['true','Opacity becomes 0 and visible becomes false.','Transition runs from blockingOpacity to 0; visible becomes false when finished.']],widths=[24,38,38]),Para('If a fade transition is already running when hide changes again, the current transition is stopped before the new one starts.')]),
Chapter('Layout and sizing',[Para('GlassPane itself does not override layout. It relies on StackPane sizing and its user agent CSS:'),Code('''.glass-pane {
    -fx-max-width: Infinity;
    -fx-max-height: Infinity;
    -fx-background-color: black;
}'''),Callout('Place the glass pane after the content in the parent children list so it is painted and hit-tested above the content.',kind='tip'),PageBreak()]),
Chapter('Styling with CSS',[Figure(f'{G}/css.svg','CSS controls the fill color; styleable properties control opacity and timing.'),Table(['CSS property','Type','Default'],[['<font face="Courier">-fx-blocking-opacity</font>','number','0.5'],['<font face="Courier">-fx-fade-in-out</font>','boolean','false'],['<font face="Courier">-fx-fade-in-out-duration</font>','Duration','100ms']],widths=[48,26,26]),Code('''.glass-pane {
    -fx-background-color: #111827;
    -fx-blocking-opacity: 0.62;
    -fx-fade-in-out: true;
    -fx-fade-in-out-duration: 180ms;
}'''),PageBreak()]),
Chapter('Interaction patterns',[Para('The class only supplies the overlay mechanics. Applications decide what a click means. DrawerStackPane uses its glass pane to auto-hide the drawer when the user clicks outside it.'),Code('''glassPane.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
    if (evt.getButton() == MouseButton.PRIMARY) {
        glassPane.setHide(true);
    }
});'''),Numbered(['Create the glass pane once.','Keep it as the top child of the layered parent.','Set hide to false before starting work that must block input.','Set hide to true after the blocking operation or dialog completes.']),PageBreak()]),
Chapter('Recipes',[Section('Bind to a running property'),Code('''glassPane.hideProperty().bind(service.runningProperty().not());'''),Section('Use without animation'),Code('''glassPane.setFadeInOut(false);
glassPane.setHide(false);'''),Section('Animated modal backdrop'),Code('''glassPane.setFadeInOut(true);
glassPane.setBlockingOpacity(0.35);
glassPane.setFadeInOutDuration(Duration.millis(120));'''),PageBreak()]),
Chapter('Troubleshooting',[Bullets(['Remember that <font face="Courier">hide=false</font> means the pane is shown.','Setting blockingOpacity outside 0..1 is ignored by reverting to the previous value.','The class does not set an AccessibleRole or accessible text in the source.','If clicks reach controls below, verify the glass pane is the top child and mouseTransparent is still false.'])])])
