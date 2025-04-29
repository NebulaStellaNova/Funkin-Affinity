package com.example.fnfaffinity.novahandlers;

import com.example.fnfaffinity.Main;

import java.util.Vector;

public class NovaAlphabetDefault extends Main {
    String daText;
    public double x = 0;
    public double y = 0;
    public Vector<NovaSprite> sprites = new Vector<NovaSprite>(0);
    public NovaAlphabetDefault(String text, double X, double Y) {
        daText = text;
        x = X;
        y = Y;
        for (int i = 0; i < text.length(); i++) {
            String anim = text.charAt(i) + " bold instance 1";
            boolean equals = String.valueOf(text.charAt(i)).equals(" ");
            if (equals)
            {
                anim = "- bold instance 1";
            }
            NovaSprite letter =  new NovaSprite("alphabet/"+anim, x, y);
            if (equals)
            {
                letter.visible = false;
            }
            letter.x = x + (45 * i);
            sprites.add(letter);
        }
    }
}
