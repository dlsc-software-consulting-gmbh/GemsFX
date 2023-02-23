package com.dlsc.gemsfx.demo.fake;

import javafx.scene.image.Image;

public enum WeatherCondition {
    
    CLOUDY("Cloudy", ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/Cloudy.png")),
    PARTIALLY_CLOUDY("Partially\nCloudy", ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/PartiallyCloudy.png")),
    RAINY("Rainy",ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/Rainy.png")),
    SNOWY("Snowy",ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/Snowy.png")),
    STORMY("Stormy",ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/Stormy.png")),
    SUNNY("Sunny",ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/Sunny.png")),
    WINDY("Windy",ResourcesUtil.loadImage("/com/dlsc/gemsfx/demo/fake/Windy.png"));

    private final String desc;
    private final Image image;

    WeatherCondition(String desc, Image image) {
        this.desc = desc;
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public Image getImage() {
        return image;
    }
}