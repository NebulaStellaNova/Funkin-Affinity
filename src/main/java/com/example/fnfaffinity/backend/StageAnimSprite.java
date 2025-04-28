package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.novautils.NovaAnimSprite;

public class StageAnimSprite extends NovaAnimSprite {
    public String name;
    public String type;

    public StageAnimSprite(String Name, String Path, double xPos, double yPos, String type) {
        super(Path, xPos, yPos);
        name = Name;
        this.type = type;
    }
}
