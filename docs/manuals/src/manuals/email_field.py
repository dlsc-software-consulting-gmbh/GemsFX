"""Content of the EmailField developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "email-field"

MANUAL = Manual(
    control='EmailField',
    package='com.dlsc.gemsfx',
    subtitle='Validated email input with domain suggestions',
    abstract='EmailField wraps a CustomTextField with email validation, optional mail and warning icons, localized invalid text and a popup of matching email domains.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='A generated cartoon overview of EmailField.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>EmailField</b> is a control for entering one email address, or a comma-separated list of addresses. It validates text with a pluggable <font face='Courier'>Predicate&lt;String&gt;</font>, exposes only valid values through its model properties and can suggest common domains after the at sign."),
                Section('Key features'),
                Bullets(["Built-in domain list and auto completion popup after <font face='Courier'>@</font>.", 'Optional mail icon on the left and validation icon on the right.', 'Single-address and multiple-address modes.', "Read-only <font face='Courier'>valid</font> state with <font face='Courier'>:valid</font> and <font face='Courier'>:invalid</font> pseudo classes.", 'Localized invalid tooltip text.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Para('Create the control, set a prompt, decide whether the field is required and observe the validated output property.'),
                Code('EmailField field = new EmailField();\nfield.setPromptText("Email address");\nfield.setRequired(true);\n\nfield.validProperty().addListener((obs, old, valid) -> {\n    if (valid) {\n        System.out.println(field.getEmailAddress());\n    }\n});', caption='A complete single-address setup.'),
                Figure(f"{G}/states.svg", 'Optional, invalid and valid states of the field.')
            ]),
        Chapter('Anatomy', [
                Para("The skin puts the internal <font face='Courier'>CustomTextField</font> into the control and installs the left mail icon, the right validation icon, the tooltip and the domain popup."),
                Figure(f"{G}/anatomy.svg", 'Parts of an EmailField.'),
                Table(['Part', 'Node / value', 'Description'], [['Root', "<font face='Courier'>.email-field</font>", 'Control style class and validity pseudo classes.'], ['Editor', "<font face='Courier'>CustomTextField</font>", 'Receives focus and stores the user text.'], ['Domain popup', "<font face='Courier'>PopupControl</font>", 'Shows filtered domains while the editor is focused.'], ['Suggestion list', "<font face='Courier'>ListView&lt;String&gt;</font>", "Uses <font face='Courier'>domainListCellFactory</font>."]], widths=[24, 28, 48])
            ]),
        Chapter('Control API', [
                Section('Validation and values'),
                PropertyTable([Property('emailValidator', 'ObjectProperty&lt;Predicate&lt;String&gt;&gt;', 'EmailValidator.getInstance()::isValid', 'Predicate used for each individual address.'), Property('required', 'BooleanProperty', 'false', 'Blank text is invalid only when required is true. Styleable.'), Property('valid', 'ReadOnlyBooleanProperty', 'derived', 'True when the current text satisfies the current mode and validator.'), Property('emailAddress', 'StringProperty', 'null', 'Valid single address, or null while invalid.'), Property('multipleEmailAddresses', 'ListProperty&lt;String&gt;', 'empty list', 'Valid addresses in multiple-address mode.')]),
                Section('Editor and suggestions'),
                PropertyTable([Property('domainList', 'ListProperty&lt;String&gt;', 'gmail.com, yahoo.com, outlook.com, hotmail.com, icloud.com, aol.com, mail.com, protonmail.com, gmx.com, zoho.com, qq.com, 163.com, 126.com, yeah.net, msn.com, live.com, me.com', 'Known domains used by the popup.'), Property('autoDomainCompletionEnabled', 'BooleanProperty', 'true', 'Enables the popup. Styleable.'), Property('domainListCellFactory', 'ObjectProperty&lt;Callback&lt;ListView&lt;String&gt;, ListCell&lt;String&gt;&gt;&gt;', 'null', 'Custom cells for domain suggestions.'), Property('supportingMultipleAddresses', 'BooleanProperty', 'false', 'Comma-separated address mode. Styleable.'), Property('promptText', 'StringProperty', 'null', 'Bound to the internal editor prompt.')]),
                Section('Icons and tooltip'),
                PropertyTable([Property('showMailIcon', 'BooleanProperty', 'true', 'Shows the mail icon. Styleable.'), Property('showValidationIcon', 'BooleanProperty', 'true', 'Shows the validation icon when invalid. Styleable.'), Property('invalidText', 'StringProperty', 'Invalid email address.', 'Tooltip text installed on the validation icon.')])
            ]),
        Chapter('Validation and suggestions', [
                Figure(f"{G}/flow.svg", 'How typed text becomes suggestions and validated values.'),
                Para('The popup appears only when the editor is focused, auto completion is enabled, text contains an at sign, at least one domain starts with the typed suffix and no domain matches it exactly. Selecting a row replaces the suffix after the last at sign.'),
                Para("In multiple-address mode the text is tokenized by comma. Every trimmed token must pass <font face='Courier'>emailValidator</font>; the list property is updated with the valid tokens collected so far."),
                Code('field.setSupportingMultipleAddresses(true);\nfield.setText("ada@example.com, grace@example.org");\nObservableList<String> addresses = field.getMultipleEmailAddresses();')
            ]),
        Chapter('Styling', [
                Para("The user agent stylesheet is <font face='Courier'>email-field.css</font>."),
                Section('Style classes and pseudo classes'),
                Table(['Selector', 'Description'], [["<font face='Courier'>.email-field</font>", 'root control'], ["<font face='Courier'>.email-field:valid</font>", 'valid state'], ["<font face='Courier'>.email-field:invalid</font>", 'invalid state'], ["<font face='Courier'>.mail-icon-wrapper / .mail-icon</font>", 'left icon nodes'], ["<font face='Courier'>.validation-icon-wrapper / .validation-icon</font>", 'right validation icon'], ["<font face='Courier'>.suggestion-popup .content-pane .suggestion-list-view</font>", 'domain popup list']], widths=[42, 58]),
                Section('Styleable CSS properties'),
                Table(['CSS property', 'Type', 'Default'], [["<font face='Courier'>-fx-auto-domain-completion-enabled</font>", 'boolean', 'true'], ["<font face='Courier'>-fx-required</font>", 'boolean', 'false'], ["<font face='Courier'>-fx-show-mail-icon</font>", 'boolean', 'true'], ["<font face='Courier'>-fx-show-validation-icon</font>", 'boolean', 'true'], ["<font face='Courier'>-fx-supporting-multiple-addresses</font>", 'boolean', 'false']], widths=[50, 25, 25]),
                Code('.email-field {\n    -fx-required: true;\n    -fx-show-mail-icon: false;\n}\n\n.email-field:invalid > .custom-text-field {\n    -fx-border-color: red;\n}')
            ]),
        Chapter('Localization', [
                Para("The invalid tooltip is loaded through <font face='Courier'>ResourceBundleManager</font> from <font face='Courier'>email-field.properties</font>."),
                Table(['Key', 'English text'], [["<font face='Courier'>validation.invalid-email</font>", 'Invalid email address.']], widths=[44, 56])
            ]),
        Chapter('Accessibility', [
                Para("The constructor sets <font face='Courier'>AccessibleRole.TEXT_FIELD</font> and binds accessible text to <font face='Courier'>emailAddressProperty()</font>. Automatic accessible-text updates stop if application code sets accessible text itself.")
            ]),
        Chapter('Recipes', [
                Section('Replace the domain list'),
                Code('field.getDomainList().setAll("example.com", "example.org", "company.test");'),
                Section('Use a stricter validator'),
                Code('field.setEmailValidator(address -> address.endsWith("@example.com"));'),
                Section('Customize suggestion rows'),
                Code('field.setDomainListCellFactory(view -> new ListCell<>() {\n    @Override protected void updateItem(String item, boolean empty) {\n        super.updateItem(item, empty);\n        setText(empty ? null : "@" + item);\n    }\n});'),
                Section('Checklist'),
                Numbered(["Decide whether blank input is valid by setting <font face='Courier'>required</font>.", "Use <font face='Courier'>emailAddress</font> only in single-address mode.", "Use <font face='Courier'>multipleEmailAddresses</font> only in multiple-address mode.", "Replace <font face='Courier'>invalidText</font> for product-specific wording."])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.EmailFieldApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.EmailFieldApp</font>)", "<font face='Courier'>SearchTextField</font> - another text-field control with an embedded popup.", "<font face='Courier'>CustomTextField</font> - the embedded editor used by the skin.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
