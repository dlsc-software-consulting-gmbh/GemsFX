# Content of the Skeleton developer manual.
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, PageBreak, Para, Property, PropertyTable, Section, Table
G='skeleton'
MANUAL=Manual(control='Skeleton',package='com.dlsc.gemsfx',subtitle='A single shimmer placeholder for loading content',abstract=('Skeleton is a Control that draws one placeholder unit: a rounded rectangle, a circle or stacked text lines. Its skin renders base rectangles plus a moving shimmer band, and all visual parameters are styleable through skeleton.css.'),cover_svg=f'{G}/cover.svg',cover_caption='Skeleton placeholders mimic the shape of content while real data loads.',chapters=[
Chapter('Introduction',[Para('<b>Skeleton</b> extends <font face="Courier">Control</font>. It represents one loading placeholder, not an entire loading layout. Compose several Skeleton instances directly or place them inside <font face="Courier">SkeletonPane</font> to swap a placeholder tree for real content.'),Bullets(['Three variants: ROUNDED_RECTANGLE, CIRCULAR and TEXT.','The shimmer animation travels left-to-right across the control.','The skin reports unbounded maximum size so containers can stretch the placeholder.','The constructor accepts an optional variant; null leaves the default styleable from CSS.']),Code('''Skeleton title = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
title.setPrefSize(160, 16);

Skeleton avatar = new Skeleton(Skeleton.Variant.CIRCULAR);
avatar.setPrefSize(48, 48);''')]),
Chapter('Getting started',[Para('Use skeletons to match the eventual content geometry. The demo builds an avatar plus text column and wraps it in a SkeletonPane.'),Code('''Skeleton paragraph = new Skeleton(Skeleton.Variant.TEXT);
paragraph.setLineCount(3);
paragraph.setLineHeight(12);
paragraph.setLineSpacing(8);
paragraph.setLastLineFillPercent(65);

VBox placeholder = new VBox(8, title, paragraph);'''),Figure(f'{G}/variants.svg','The three variants drawn by the skin.')]),
Chapter('Anatomy',[Figure(f'{G}/anatomy.svg','The skin separates base shapes, shimmer mask and moving band.'),Table(['Part','Node','Description'],[['Shape layer','Group','Contains rectangles filled with baseColor.'],['Shimmer layer','Group','Contains the moving shimmerBand and is clipped to the mask.'],['Shimmer mask','Group','Rectangles matching the base blocks.'],['Shimmer band','Rectangle','Moves from -shimmerWidth to content width on each cycle.']],widths=[24,26,50])]),
Chapter('Control API',[PropertyTable([Property('variant','ObjectProperty&lt;Variant&gt;','ROUNDED_RECTANGLE','Shape to render. Null falls back to the default variant at use site. Styleable.'),Property('cornerRadius','DoubleProperty','4.0','Corner radius for rounded rectangles. Negative and NaN render as 0. Styleable.'),Property('baseColor','ObjectProperty&lt;Paint&gt;','#e0e0e0 / CSS derive','Base fill. Null renders no base fill. Styleable.'),Property('shimmerFill','ObjectProperty&lt;Paint&gt;','white translucent gradient','Paint used for the moving band. Null renders no shimmer. Styleable.'),Property('cycleDuration','ObjectProperty&lt;Duration&gt;','1500 ms','Duration of one sweep. Null, unknown, indefinite or <= 0 disables animation. Styleable.'),Property('shimmerWidth','DoubleProperty','56.0','Band width in pixels. Negative, NaN and infinite render as 0. Styleable.'),Property('lineCount','IntegerProperty','1','Number of TEXT lines; values below 1 render as 1. Styleable.'),Property('lineHeight','DoubleProperty','14.0','Height of TEXT lines. Invalid values render as 0. Styleable.'),Property('lineSpacing','DoubleProperty','8.0','Vertical gap between TEXT lines. Invalid values render as 0. Styleable.'),Property('lastLineFillPercent','DoubleProperty','70.0','Width of final TEXT line, clamped to 0..100 at render time. Styleable.')])]),
Chapter('Variants and rendering',[Figure(f'{G}/timing.svg','The shimmer timeline repeats while the control is showing.'),Table(['Variant','Rendering rule'],[['ROUNDED_RECTANGLE','One rectangle filling the content area, with cornerRadius.'],['CIRCULAR','A circle inscribed in min(width, height) and centered.'],['TEXT','Stacked rounded rectangles using lineCount, lineHeight, lineSpacing and lastLineFillPercent.']],widths=[30,70]),Callout('The animation pauses when the control is no longer showing in a scene and resumes when it becomes visible again.',kind='note')]),
Chapter('Layout and sizing',[Figure(f'{G}/sizing.svg','Preferred sizes and text-line layout come from the skin.'),Table(['Variant','Preferred size'],[['ROUNDED_RECTANGLE','120 x 16 plus insets'],['TEXT','120 wide; height from lineCount, lineHeight and lineSpacing'],['CIRCULAR','48 x 48 plus insets']],widths=[35,65]),Para('Minimum size is only the control insets. Maximum width and height are <font face="Courier">Double.MAX_VALUE</font>, which lets layouts stretch skeletons to mirror real content.'),PageBreak()]),
Chapter('Styling with CSS',[Para('The user agent stylesheet is <font face="Courier">skeleton.css</font>. It declares every visual property.'),Table(['CSS property','Default'],[['<font face="Courier">-fx-variant</font>','rounded-rectangle'],['<font face="Courier">-fx-corner-radius</font>','4px'],['<font face="Courier">-fx-base-color</font>','derive(-fx-background, -8%)'],['<font face="Courier">-fx-shimmer-fill</font>','translucent white linear gradient'],['<font face="Courier">-fx-cycle-duration</font>','1500ms'],['<font face="Courier">-fx-shimmer-width</font>','56px'],['<font face="Courier">-fx-line-count</font>','1'],['<font face="Courier">-fx-line-height</font>','14px'],['<font face="Courier">-fx-line-spacing</font>','8px'],['<font face="Courier">-fx-last-line-fill-percent</font>','70']],widths=[55,45]),Code('''.skeleton {
    -fx-base-color: #e5e7eb;
    -fx-shimmer-width: 72px;
    -fx-cycle-duration: 1200ms;
}

.skeleton.text-placeholder {
    -fx-variant: text;
    -fx-line-count: 3;
}'''),PageBreak()]),
Chapter('Localization',[Para('Skeleton uses <font face="Courier">ResourceBundleManager.BundleType.SKELETON</font> for its accessible role description.'),Table(['Key','English text'],[['<font face="Courier">accessible.role-description</font>','loading placeholder']],widths=[55,45])]),
Chapter('Accessibility',[Para('The constructor sets <font face="Courier">AccessibleRole.NODE</font> with a localized role description. The source does not bind accessible text to animation progress because the control is decorative placeholder content.'),Code('''Skeleton skeleton = new Skeleton(Skeleton.Variant.TEXT);
skeleton.setAccessibleText("Loading profile details");'''),PageBreak()]),
Chapter('Recipes',[Section('Profile card placeholder'),Code('''Skeleton avatar = new Skeleton(Skeleton.Variant.CIRCULAR);
avatar.setPrefSize(48, 48);

Skeleton body = new Skeleton(Skeleton.Variant.TEXT);
body.setLineCount(2);
body.setLastLineFillPercent(68);

HBox row = new HBox(14, avatar, body);'''),Section('Disable shimmer'),Code('''skeleton.setCycleDuration(Duration.ZERO);
// or in CSS: -fx-cycle-duration: 0ms;'''),Numbered(['Match placeholder dimensions to the real content.','Use TEXT only for line stacks; use rounded rectangles for titles and buttons.','Keep circular skeletons square with pref and max sizes.']),PageBreak()]),
Chapter('Troubleshooting',[Bullets(['If nothing is drawn, check that width and height are positive.','A zero or invalid lineHeight makes TEXT produce no blocks.','A zero shimmerWidth or disabled cycleDuration removes the sweep but not the base shape.','Use SkeletonPane when you need to swap an entire placeholder tree with real content.'])])])
