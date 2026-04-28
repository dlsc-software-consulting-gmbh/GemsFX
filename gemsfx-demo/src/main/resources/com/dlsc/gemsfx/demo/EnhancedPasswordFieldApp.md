### EnhancedPasswordField

A custom password field that enhances the standard `PasswordField` with additional features.

This component allows for the display of icons or nodes on both the left and right sides of the password field.
It also provides the capability to toggle the visibility of the password, allowing the password to be shown
as plain text or masked with a customizable echo character. The echo character and visibility can be styled
and controlled through CSS or programmatically.

Key features:

  - Customizable echo character: The character used to mask the password can be customized.
  - Toggle password visibility: Users can toggle between hiding and showing the password as plain text.
  - Left and right nodes: Allows adding custom nodes (like buttons or icons) to either side of the field.

Usage example:

```java
EnhancedPasswordField passwordField = new EnhancedPasswordField();
passwordField.setLeft(new ImageView(new Image("path/to/icon.png")));
passwordField.setRight(new Button("Show", e -> passwordField.setShowPassword(!passwordField.isShowPassword())));
```
