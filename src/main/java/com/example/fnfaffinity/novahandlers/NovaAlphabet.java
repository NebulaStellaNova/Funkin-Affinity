package com.example.fnfaffinity.novahandlers;

import com.example.fnfaffinity.Main;

import java.util.Vector;

public class NovaAlphabet extends NovaBasic {
    public NovaCamera camera;
    String daText;
    public double width = 320;
    public double fullWidth = 0;
    public Vector<NovaSprite> sprites = new Vector<NovaSprite>(0);
    public NovaSprite icon;
    public NovaAlphabet(String text, double X, double Y) {
        daText = text;
        x = X;
        y = Y;
        for (int i = 0; i < text.length(); i++) {
            String anim = text.charAt(i) + " bold instance 1";
            width += 45;
            boolean equals = String.valueOf(text.charAt(i)).equals(" ");
            NovaSprite letter = null;
            if (!equals)
            {
                //anim = "- bold instance 1";
                letter =  new NovaSprite("alphabet/"+anim, x, y);
                letter.x = x + (45 * i);
            }
            sprites.add(letter);
        }
        camera = Main.camGame;
    }
}
