package com.example.fnfaffinity.novahandlers.caches;

import javafx.scene.text.Font;

public class FontCache {
    public String path;
    public Font font;

    public FontCache(Font font, String path) {
        this.font = font;
        this.path = path;
    }
}
