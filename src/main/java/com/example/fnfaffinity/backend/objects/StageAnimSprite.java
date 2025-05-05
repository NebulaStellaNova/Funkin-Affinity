package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.novahandlers.NovaAnimSprite;
import com.example.fnfaffinity.states.PlayState;

public class StageAnimSprite extends NovaAnimSprite {
    public String name;
    public String type;

    public StageAnimSprite(String Name, String Path, double xPos, double yPos, String type) {
        super(Path, xPos, yPos);
        name = Name;
        this.type = type;
        setImage(Path, PlayState.currentFolder);
    }
}
