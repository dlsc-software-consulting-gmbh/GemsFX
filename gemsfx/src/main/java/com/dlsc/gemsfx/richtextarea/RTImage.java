package com.dlsc.gemsfx.richtextarea;

import javafx.scene.image.Image;

public class RTImage extends RTElement<RTImage> {

    private Image image;

    private RTImage() {
    }

    public static RTImage create() {
        return new RTImage();
    }

    public Image getImage() {
        return image;
    }

    public RTImage withImage(Image image) {
        this.image = image;
        return this;
    }
}
