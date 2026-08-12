package com.dlsc.gemsfx;

import javafx.scene.image.WritableImage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link PhotoView}.
 */
public class PhotoViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        PhotoView view = invoke(PhotoView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        PhotoView view = invoke(PhotoView::new);
        assertTrue(view.getStyleClass().contains("photo-view"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        PhotoView view = invoke(PhotoView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        PhotoView view = invoke(PhotoView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void testDefaultPhotoIsNull() {
        PhotoView view = invoke(PhotoView::new);
        assertNull(view.getPhoto());
    }

    @Test
    public void testSetPhoto() {
        PhotoView view = invoke(PhotoView::new);
        WritableImage img = invoke(() -> new WritableImage(10, 10));
        runFx(() -> view.setPhoto(img));
        assertNotNull(view.getPhoto());
    }

    @Test
    public void testDefaultPhotoZoom() {
        PhotoView view = invoke(PhotoView::new);
        assertEquals(1.0, view.getPhotoZoom(), 0.001);
    }

    @Test
    public void testSetPhotoZoom() {
        PhotoView view = invoke(PhotoView::new);
        runFx(() -> view.setPhotoZoom(1.5));
        assertEquals(1.5, view.getPhotoZoom(), 0.001);
    }

    @Test
    public void testDefaultCreateCroppedImage() {
        PhotoView view = invoke(PhotoView::new);
        assertTrue(view.isCreateCroppedImage());
    }

    @Test
    public void testSetCreateCroppedImage() {
        PhotoView view = invoke(PhotoView::new);
        runFx(() -> view.setCreateCroppedImage(false));
        assertFalse(view.isCreateCroppedImage());
    }

    @Test
    public void testDefaultEditable() {
        PhotoView view = invoke(PhotoView::new);
        assertTrue(view.isEditable());
    }

    @Test
    public void testDefaultClipShape() {
        PhotoView view = invoke(PhotoView::new);
        assertNotNull(view.getClipShape());
    }

    @Test
    public void testSetClipShape() {
        PhotoView view = invoke(PhotoView::new);
        runFx(() -> view.setClipShape(PhotoView.ClipShape.RECTANGLE));
        assertEquals(PhotoView.ClipShape.RECTANGLE, view.getClipShape());
    }

    @Test
    public void testPropertyAccessors() {
        PhotoView view = invoke(PhotoView::new);
        assertNotNull(view.photoProperty());
        assertNotNull(view.photoZoomProperty());
        assertNotNull(view.clipShapeProperty());
        assertNotNull(view.editableProperty());
        assertNotNull(view.createCroppedImageProperty());
    }
}
