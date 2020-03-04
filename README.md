# GemsFX

## Keyboard / KeyboardPane

An on-screen keyboard, especially useful for touch applications (mobile, tablet).

![Keyboard](gemsfx/docs/keyboard.png)

## PDFView

A custom control that allows an application to display PDF files. The control utilizes Apache's PDFBox project.

![PDFView](gemsfx/docs/pdf-view.png)

## DrawerStackPane

A stackpane with an optional node that can be shown inside a drawer. The drawer is animated and can slide in and out. When the drawer is showing a semi-transparent glass pane will cover the background. In addition the last height of the drawer can be persisted via the preferences API so that next time the drawer will show itself like in the last user session.

![DrawerStackPane](gemsfx/docs/drawer-stackpane.png)

## RichTextArea

A read-only text area that is capable of displaying nicely formatted text. The control comes with a rich model and a fluent API that will allow you to quickly compose rich text.

![RichTextArea](gemsfx/docs/rich-textarea.png)

```java
RichTextArea area = new RichTextArea();
        area.setDocument(
                RTDocument.create(
                        RTHeading.create("Heading 1"),
                        RTParagraph.create(
                                RTText.create("This is the first paragraph. "),
                                RTText.create("Some text comes here before the link that "),
                                RTLink.create("points to the website ", "https://www.dlsc.com"),
                                RTText.create("of DLSC Software & Consulting.")
                        ),
                        RTParagraph.create(
                                RTText.create("Here comes the second paragraph.")
                        ),
                        RTParagraph.create(),
                        RTHeading.create("Heading 2"),
                        RTParagraph.create(
                                RTText.create("Some text for the first paragraph after heading 2."),
                                RTList.create(
                                        RTListItem.create("List item 1"),
                                        RTListItem.create("List item 2"),
                                        RTListItem.create("List item 3",
                                                RTList.create(
                                                        RTListItem.create("Sub item A"),
                                                        RTListItem.create("Sub item B"),
                                                        RTListItem.create("Sub item C"),
                                                        RTListItem.create("Sub item D")
                                                )
                                        ),
                                        RTListItem.create("List item 4")
                                )
                        )
                )
        );
```








