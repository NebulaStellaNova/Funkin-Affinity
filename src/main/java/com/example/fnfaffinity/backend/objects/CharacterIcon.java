package com.example.fnfaffinity.backend.objects;
import com.example.fnfaffinity.novahandlers.NovaSprite;

public class CharacterIcon extends NovaSprite {
    public int segments = 2;
    public int frame = 0;

    public CharacterIcon(String name, double xPos, double yPos) {
        super("icons/" + name, xPos, yPos);
    }
}
