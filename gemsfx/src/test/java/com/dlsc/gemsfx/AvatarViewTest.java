package com.dlsc.gemsfx;

import javafx.scene.image.WritableImage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AvatarView}.
 */
public class AvatarViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        AvatarView view = invoke(AvatarView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        AvatarView view = invoke(AvatarView::new);
        assertTrue(view.getStyleClass().contains("avatar-view"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        AvatarView view = invoke(AvatarView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        AvatarView view = invoke(AvatarView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void testInitialsConstructor() {
        AvatarView view = invoke(() -> new AvatarView("JD"));
        assertEquals("JD", view.getInitials());
    }

    @Test
    public void testSetInitials() {
        AvatarView view = invoke(AvatarView::new);
        runFx(() -> view.setInitials("AB"));
        assertEquals("AB", view.getInitials());
    }

    @Test
    public void testImageConstructor() {
        WritableImage img = invoke(() -> new WritableImage(10, 10));
        AvatarView view = invoke(() -> new AvatarView(img));
        assertNotNull(view.getImage());
    }

    @Test
    public void testSetImage() {
        AvatarView view = invoke(AvatarView::new);
        WritableImage img = invoke(() -> new WritableImage(10, 10));
        runFx(() -> view.setImage(img));
        assertNotNull(view.getImage());
    }

    @Test
    public void testDefaultAvatarShape() {
        AvatarView view = invoke(AvatarView::new);
        assertEquals(AvatarView.AvatarShape.SQUARE, view.getAvatarShape());
    }

    @Test
    public void testSetAvatarShape() {
        AvatarView view = invoke(AvatarView::new);
        runFx(() -> view.setAvatarShape(AvatarView.AvatarShape.ROUND));
        assertEquals(AvatarView.AvatarShape.ROUND, view.getAvatarShape());
    }

    @Test
    public void testDefaultSizePositive() {
        AvatarView view = invoke(AvatarView::new);
        assertTrue(view.getSize() > 0);
    }

    @Test
    public void testSetSize() {
        AvatarView view = invoke(AvatarView::new);
        runFx(() -> view.setSize(80));
        assertEquals(80, view.getSize(), 0.001);
    }

    @Test
    public void testPropertyAccessors() {
        AvatarView view = invoke(AvatarView::new);
        assertNotNull(view.initialsProperty());
        assertNotNull(view.imageProperty());
        assertNotNull(view.avatarShapeProperty());
        assertNotNull(view.sizeProperty());
    }
}
