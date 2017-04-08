package com.example.vinoth.googlemap.model;

/**
 * Created by vinoth on 7/10/16.
 */

public class Vehicle {
    private String name;
    private Boolean enable;

    public Vehicle() {
    }

    public Vehicle(String name, Boolean enable) {
        this.name = name;
        this.enable = enable;
    }
    public String getName() {
        return name;
    }
    public Boolean getEnable() {
        return enable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
