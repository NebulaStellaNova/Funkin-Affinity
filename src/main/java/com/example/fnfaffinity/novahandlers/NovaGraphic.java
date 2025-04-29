package com.example.fnfaffinity.novahandlers;

public class NovaGraphic {
    public double x;
    public double y;
    public double width;
    public double height;
    public double scrollX;
    public double scrollY;
    public String color;
    public NovaCamera camera;

    public NovaGraphic(NovaCamera camera, double x, double y, double scrollX, double scrollY, double width, double height, String color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.camera = camera;
    }
}
