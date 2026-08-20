"""Content of the PhotoView developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "photo-view"

def esc(value):
    return value.replace("<", "&lt;").replace(">", "&gt;")

PROPS = [('photo', 'ObjectProperty<Image>', 'null', 'Original image.'), ('croppedImage', 'ReadOnlyObjectProperty<Image>', 'null', 'Cropped image produced by the skin.'), ('createCroppedImage', 'BooleanProperty', 'true', 'Enables delayed crop generation.'), ('photoEffect', 'ObjectProperty<Effect>', 'null', 'Effect applied to the ImageView only.'), ('placeholder', 'ObjectProperty<Node>', 'localized Label with upload icon', 'Shown when photo is null, editable is true and photoSupplier is non-null.'), ('editable', 'BooleanProperty', 'true', 'Enables gestures and slider; styleable.'), ('photoSupplier', 'ObjectProperty<Supplier<Image>>', 'FileChooser supplier', 'Provides images for click, SPACE and ENTER.'), ('clipShape', 'ObjectProperty<ClipShape>', 'CIRCLE', 'CIRCLE or RECTANGLE; styleable.'), ('photoZoom', 'DoubleProperty', '1', 'User zoom; slider min is 1.'), ('photoTranslateX / photoTranslateY', 'DoubleProperty', '0', 'Percentage-based image translations.'), ('maxZoom', 'DoubleProperty', '5.0', 'Maximum user zoom; styleable.')]
CSS_PROPS = [('-fx-editable', 'boolean', 'true'), ('-fx-clip-shape', 'ClipShape', 'CIRCLE'), ('-fx-max-zoom', 'size', '5.0')]
SELECTORS = ['.photo-view', ':empty', ':focused', '> .box', '> .box > .image-box', '> .box > .image-box > .placeholder', '.upload-icon', '.border-circle', '.border-rectangle', '.file-drag (CSS only; no source sets it)']
LOC = [('placeholder.drop-or-click', 'DROP IMAGE FILE\\nOR CLICK TO ADD'), ('file-chooser.title.load-image', 'Load Image File'), ('file-chooser.filter.image-files', 'Image Files'), ('accessible.role-description', 'photo')]

MANUAL = Manual(
    control="PhotoView",
    package="com.dlsc.gemsfx",
    subtitle='Editable cropped profile photo view',
    abstract='PhotoView displays and optionally edits a profile image. Users can load, drag, zoom, clip and crop an image while the control exposes the original and cropped versions.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of PhotoView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>PhotoView</b> PhotoView displays and optionally edits a profile image. Users can load, drag, zoom, clip and crop an image while the control exposes the original and cropped versions."),
            Section("Key features"),
            Bullets(['Editable by default with mouse drag, scroll wheel and pinch zoom.', 'Default placeholder invites dropping or clicking to add an image.', 'ClipShape.CIRCLE is the default; RECTANGLE is also supported.', 'createCroppedImage defaults to true and updates croppedImage after a 200 ms delay.', 'Supported drag/drop file extensions are .bmp, .png, .gif, .jpg and .jpeg.', 'BACK_SPACE and DELETE remove the photo; SPACE and ENTER invoke the supplier.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="The control lives in module <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The following snippet uses only public API verified in the control source."),
            Code('PhotoView photoView = new PhotoView();\nphotoView.setClipShape(PhotoView.ClipShape.CIRCLE);\nphotoView.setMaxZoom(4);\nphotoView.setPhotoSupplier(() -> loadImageFromApplicationDialog());\nphotoView.croppedImageProperty().addListener((obs, old, img) -> savePreview(img));', caption="A compact setup for PhotoView."),
            Figure(f"{G}/cover.svg", "A generated overview of PhotoView in use."),
        ]),
        Chapter("Anatomy", [
            Para("The anatomy diagram identifies the implementation pieces that matter when configuring, styling or debugging the control."),
            Figure(f"{G}/anatomy.svg", "The parts of PhotoView."),
            Table(["Part", "Verified detail"], [['Root', "Style class <font face='Courier'>.photo-view</font> is added by the constructor."], ['Stylesheet', "User-agent stylesheet <font face='Courier'>photo-view.css</font> is returned by the control."]], widths=[32,68]),
        ]),
        Chapter("Control API", [
            Section("Properties and callbacks"),
            PropertyTable([Property(name, esc(type_), esc(default), desc) for name, type_, default, desc in PROPS]),
            Callout("Defaults and property names in this table were checked against the Java source for this batch.", kind="note"),
        ]),
        Chapter("Behaviour", [
            Figure(f"{G}/states.svg", "Important runtime states of PhotoView."),
            Bullets(['Setting a new photo resets photoZoom to 1 and translations to 0.', 'The skin fits the image so the clip area is filled, then applies user zoom and translation.', 'Dragging changes percentage translations, so layout size changes do not lose the composition.', 'Scroll wheel adds or subtracts 0.1 zoom; pinch zoom multiplies by the zoom factor.', 'CropService waits 200 ms and writes the result to control properties under cropped.image.']),
            Figure(f"{G}/flow.svg", "How data and geometry flow through PhotoView."),
        ]),
        Chapter("Layout and rendering", [
            Para('Rendering centers the image, scales it to cover the clip area, then applies user zoom and percentage-based translations. Cropping reads the visible region after a short delay.'),
            Figure(f"{G}/layout.svg", "Rendering and sizing rules for PhotoView."),
            Table(["Concern", "Rule"], [['Clip', 'CIRCLE uses a centered Circle; RECTANGLE uses a centered square Rectangle.'], ['Zoom', 'Slider range is 1 to maxZoom and binds bidirectionally to photoZoom.'], ['Crop', 'Crop rectangle is computed from image size, fit size, zoom and translations.']], widths=[32,68]),
        ]),
        Chapter("Styling", [
            Para('The user-agent stylesheet is photo-view.css. The table lists selectors and pseudo classes that exist in source, skin or stylesheet.'),
            Figure(f"{G}/styling.svg", "Style hooks for PhotoView."),
            Section("Style classes and pseudo classes"),
            Table(["Selector / pseudo class", "Purpose"], [[f"<font face='Courier'>{selector}</font>", "Verified in source, skin or CSS."] for selector in SELECTORS], widths=[48,52]),
            Section("Styleable CSS properties"),
            Table(["CSS property", "Type", "Default"], [[f"<font face='Courier'>{prop}</font>", type_, default] for prop, type_, default in CSS_PROPS], widths=[48,26,26]) if CSS_PROPS else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."),
            Code('.photo-view {\n    /* start with the documented root selector */\n}', caption="CSS example using documented hooks."),
        ]),
        Chapter("Localization", [
            Table(["Key", "English text"], [[f"<font face='Courier'>{key}</font>", text] for key, text in LOC], widths=[45,55]) if LOC else Para("The verified source has no ResourceBundleManager keys for PhotoView."),
        ]),
        Chapter("Accessibility", [
            Para('PhotoView sets AccessibleRole.IMAGE_VIEW with localized role description "photo". It does not bind accessible text to the current image.'),
        ]),
        Chapter("Recipes", [
            Section("Programmatic configuration"),
            Code('PhotoView photoView = new PhotoView();\nphotoView.setClipShape(PhotoView.ClipShape.CIRCLE);\nphotoView.setMaxZoom(4);\nphotoView.setPhotoSupplier(() -> loadImageFromApplicationDialog());\nphotoView.croppedImageProperty().addListener((obs, old, img) -> savePreview(img));'),
            Section("Practical checklist"),
            Numbered(['Disable createCroppedImage if delayed crops are too expensive.', 'Use a custom photoSupplier when the default FileChooser is not appropriate.', 'Use the public properties listed in the API chapter.', 'Style only through documented selectors and styleable properties.', 'Do not depend on private skin node structure except for documented CSS selectors.']),
        ]),
        Chapter("Integration notes", [
            Para('The CSS contains .photo-view.file-drag, but the verified source does not add that style class.'),
            Table(["Topic", "Recommendation"], [["Threading", "Keep image loading and expensive rendering off the UI path when the control exposes a background option."], ["Styling", "Scope selectors under the documented root style class."], ["Accessibility", "Preserve the source-defined accessible role and add app-specific text when the control does not bind it."], ["State", "Prefer public properties over skin node lookup."]], widths=[30,70]),
        ]),
        Chapter("See also", [
            Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.PhotoViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.PhotoViewApp</font>)", "Related GemsFX media controls: <font face='Courier'>AvatarView</font>, <font face='Courier'>PhotoView</font>, <font face='Courier'>SVGImageView</font>, <font face='Courier'>BeforeAfterView</font>, <font face='Courier'>MaskedView</font>, <font face='Courier'>ScreensView</font>.", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])
        ]),
    ],
)
