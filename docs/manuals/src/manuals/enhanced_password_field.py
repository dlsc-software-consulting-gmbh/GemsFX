"""Content of the EnhancedPasswordField developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "enhanced-password-field"

MANUAL = Manual(
    control='EnhancedPasswordField',
    package='com.dlsc.gemsfx',
    subtitle='Password input with custom echo characters and side nodes',
    abstract='EnhancedPasswordField extends PasswordField with left and right embedded nodes, a clickable default eye icon, a show-password state and a styleable echo character.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='A generated cartoon overview of EnhancedPasswordField.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>EnhancedPasswordField</b> extends JavaFX <font face='Courier'>PasswordField</font>. It keeps the normal text-field API, adds slots for left and right nodes and replaces the skin text binding so the displayed string can be either masked or plain."),
                Section('Key features'),
                Bullets(["Default clickable eye icon toggles <font face='Courier'>showPassword</font>.", "Styleable <font face='Courier'>echoChar</font> property, default <font face='Courier'>●</font>.", 'Optional application-supplied left and right nodes.', "Root pseudo class <font face='Courier'>:showing-password</font> while text is visible."]),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('EnhancedPasswordField field = new EnhancedPasswordField();\nfield.setPromptText("Password");\nfield.setEchoChar(\'★\');\nfield.setLeft(new FontIcon(MaterialDesign.MDI_KEY));\n\nfield.showPasswordProperty().addListener((obs, old, showing) -> {\n    System.out.println(showing ? "plain" : "masked");\n});', caption='A complete setup with a custom echo character and left node.'),
                Figure(f"{G}/states.svg", 'Masked and plain-text display states.')
            ]),
        Chapter('Anatomy', [
                Para("The control uses <font face='Courier'>CustomTextFieldSkin</font> semantics for left and right nodes, then the specialized skin rebinds the internal text node."),
                Figure(f"{G}/anatomy.svg", 'Parts of an EnhancedPasswordField.'),
                Table(['Part', 'Node / property', 'Description'], [['Root', "<font face='Courier'>.enhanced-password-field</font>", 'PasswordField subclass with user agent stylesheet.'], ['Left slot', "<font face='Courier'>left</font>", 'Optional node supplied by the application.'], ['Text node', "internal <font face='Courier'>Text</font>", 'Shows repeated echo characters or plain text.'], ['Right slot', "<font face='Courier'>right</font>", 'Defaults to the clickable eye wrapper.']], widths=[24, 30, 46])
            ]),
        Chapter('Control API', [
                PropertyTable([Property('left', 'ObjectProperty&lt;Node&gt;', 'null', 'Node shown on the left side of the field.'), Property('right', 'ObjectProperty&lt;Node&gt;', 'eye icon wrapper', 'Node shown on the right side. The default toggles password visibility.'), Property('showPassword', 'BooleanProperty', 'false', 'Shows plain text when true.'), Property('echoChar', 'ObjectProperty&lt;Character&gt;', '●', "Masking character. Styleable via <font face='Courier'>-fx-echo-char</font>."), Property('text', 'StringProperty', 'empty string', 'Inherited PasswordField content.')])
            ]),
        Chapter('Masking behaviour', [
                Figure(f"{G}/flow.svg", 'How the skin computes the displayed text.'),
                Para("The skin locates the real text node created by the standard text-field skin, unbinds its text and binds it to <font face='Courier'>text</font>, <font face='Courier'>showPassword</font> and <font face='Courier'>echoChar</font>."),
                Para("<font face='Courier'>getEchoCharSafe()</font> returns the default character when the property is unset, null or cannot be converted from CSS."),
                Code('field.setShowPassword(false);  // displays one echo char per character\nfield.setShowPassword(true);   // displays field.getText()')
            ]),
        Chapter('Side nodes and reveal interaction', [
                Figure(f"{G}/cover.svg", 'The default right node toggles the reveal state.'),
                Para("The constructor installs a <font face='Courier'>StackPane</font> right node containing a <font face='Courier'>Region</font> with style class <font face='Courier'>right-icon</font>. A mouse click on that wrapper toggles <font face='Courier'>showPassword</font> when <font face='Courier'>UIUtil.isClickOnNode(event)</font> reports a click on the node."),
                Table(['Customization', 'Effect'], [["Set <font face='Courier'>left</font>", 'Adds an application node before the text, for example a key icon.'], ["Set <font face='Courier'>right</font>", 'Replaces the built-in eye toggle completely.'], ["Bind <font face='Courier'>showPassword</font>", 'Lets an external checkbox or button drive reveal state.']], widths=[34, 66]),
                Code('''CheckBox reveal = new CheckBox("Show password");
field.showPasswordProperty().bind(reveal.selectedProperty());'''),
                Callout('If you replace the right node, the built-in click-to-toggle handler is gone. Recreate that behaviour explicitly if the field should still reveal text.', kind='tip')
            ]),
        Chapter('Styling', [
                Section('Style classes and pseudo classes'),
                Table(['Selector', 'Description'], [["<font face='Courier'>.enhanced-password-field</font>", 'root field'], ["<font face='Courier'>.enhanced-password-field:showing-password</font>", 'root while plain text is visible'], ["<font face='Courier'>.right-icon-wrapper</font>", 'default clickable wrapper'], ["<font face='Courier'>.right-icon</font>", 'eye / eye-off icon region']], widths=[46, 54]),
                Section('Styleable CSS properties'),
                Table(['CSS property', 'Type', 'Default'], [["<font face='Courier'>-fx-echo-char</font>", 'char', '●']], widths=[50, 25, 25]),
                Code(".enhanced-password-field {\n    -fx-echo-char: '*';\n}\n\n.enhanced-password-field:showing-password {\n    -fx-background-color: #fff8dc;\n}")
            ]),
        Chapter('Accessibility', [
                Para("The constructor sets <font face='Courier'>AccessibleRole.PASSWORD_FIELD</font>. The control does not bind a generated accessible text, so applications can provide their own accessible text or description if needed.")
            ]),
        Chapter('Recipes', [
                Section('Replace the right node'),
                Code('Button clear = new Button("Clear");\nclear.setOnAction(evt -> field.clear());\nfield.setRight(clear);'),
                Section('Start visible'),
                Code('EnhancedPasswordField field = new EnhancedPasswordField("temporary");\nfield.setShowPassword(true);'),
                Section('Use CSS for the echo character'),
                Code('field.setStyle("-fx-echo-char: \'■\';");'),
                Section('Checklist'),
                Numbered(["Avoid logging <font face='Courier'>getText()</font> from password fields.", 'Keep the right node keyboard-accessible if replacing the default eye.', "Use <font face='Courier'>showPassword</font> only for deliberate reveal interactions."])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.EnhancedPasswordFieldApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.EnhancedPasswordFieldApp</font>)", "<font face='Courier'>CustomTextField</font> - base class for embedded left and right nodes.", "<font face='Courier'>EmailField</font> - another field with embedded icons.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
