package com.example.fnfaffinity.novahandlers.caches;

import javafx.scene.image.Image;

public class ImageCache {
    public String path;
    public Image img;

    public ImageCache(Image img, String path) {
        this.img = img;
        this.path = path;
    }
}
