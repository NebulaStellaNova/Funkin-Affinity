package com.example.fnfaffinity.novahandlers;

import javafx.scene.text.TextAlignment;

public class NovaText extends NovaBasic {
    public String path = "vcr.ttf";
    public int size = 50;
    public String color = "#FFFFFF";
    public String text = "";
    public TextAlignment alignment = TextAlignment.LEFT;

    public double borderSize = 1;
    public String borderColor = "#000000";
    public double borderAlpha = 1;

    public void setDefault() {
        this.defX = this.x;
        this.defY = this.y;
    }
    public NovaText(int x, int y) {
        this.x = x;
        this.y = y;
        setDefault();
    }

    public NovaText(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
        setDefault();
    }

    public NovaText(String text) {
        this.text = text;
        this.x = 0;
        this.y = 0;
        setDefault();
    }

    public NovaText() {
        this.x = 0;
        this.y = 0;
        setDefault();
    }
}
