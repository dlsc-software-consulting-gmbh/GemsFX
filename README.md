[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=bugs)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=code_smells)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=ncloc)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=alert_status)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=security_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=sqale_index)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_GemsFX&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)

# GemsFX

*At least **JDK 11** is required.*

## Keyboard / KeyboardPane

An on-screen keyboard, especially useful for touch applications (mobile, tablet).

![Keyboard](gemsfx/docs/keyboard.png)

## PDFView

A custom control that allows an application to display PDF files. The control utilizes Apache's PDFBox project.

![PDFView](gemsfx/docs/pdf-view.png)

The view has excellent built-in search capabilities.

![PDFView](gemsfx/docs/pdf-view-search.png)

## DrawerStackPane

A stackpane with an optional node that can be shown inside a drawer. The drawer is animated and can slide in and out. When the drawer is showing a semi-transparent glass pane will cover the background. In addition the last height of the drawer can be persisted via the preferences API so that next time the drawer will show itself like in the last user session.

![DrawerStackPane](gemsfx/docs/drawer-stackpane.png)

## FilterView

A control for filtering the content of an obserable list. Works in combination with TableView, ListView, or any control that is based on observable lists.

![FilterView](gemsfx/docs/filter-view.png)

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








