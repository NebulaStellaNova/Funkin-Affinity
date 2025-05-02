package com.example.fnfaffinity.novahandlers;

public class NovaGraphic extends NovaBasic {
    public double width;
    public double height;
    public String color;
    public NovaCamera camera;

    public NovaGraphic(NovaCamera camera, double x, double y, double scrollX, double scrollY, double width, double height, String color) {
        this.x = x;
        this.defX = x;
        this.y = y;
        this.defY = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.camera = camera;
    }
}
