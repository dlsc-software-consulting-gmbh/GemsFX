package com.dlsc.gemsfx.demo.fake;

import java.time.LocalDate;

/**
 * A summary description of the day's weather
 */
public class WeatherData {

    /**
     * e.g. UK
     */
    private String country;
    /**
     * e.g. london
     */
    private String location;
    /**
     * e.g. SUNNY RAINY ...
     */
    private WeatherCondition weatherCondition;

    private double windSpeed;
    /**
     * SW NW N E ....
     */
    private String windDirection;

    private long updateTimestamp;

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(WeatherCondition weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    /**
     * 0.82312->82%
     */
    private double accurate;

    /**
     * temperature
     * sometimes is a temperature range 2~7.5
     * sometimes is a temperature value of 16
     */
    private String temp;

    /**
     * The date the weather forecast is for<br/>
     * e.g.<br/>
     * 2023/02/20 ---UI show---> Mon 20 <br/>
     * 2023/02/21 ---UI show---> Tue 21
     */
    private LocalDate date;

    public WeatherData() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAccurate() {
        return accurate;
    }

    public void setAccurate(double accurate) {
        this.accurate = accurate;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

}
