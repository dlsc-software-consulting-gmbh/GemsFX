package com.dlsc.gemsfx.rtf;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.net.MalformedURLException;
import java.net.URL;

public class RTLink extends RTTextElement<RTLink> {

    private RTLink() {
    }

    public static RTLink create(String text, String url) {
        try {
            return new RTLink().withText(text).withURL(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RTLink withURL(URL url) {
        this.url.set(url);
        return this;
    }

    private final ObjectProperty<URL> url = new SimpleObjectProperty<>(this, "url");

    public URL getUrl() {
        return url.get();
    }

    public ObjectProperty<URL> urlProperty() {
        return url;
    }

    public void setUrl(URL url) {
        this.url.set(url);
    }
}
