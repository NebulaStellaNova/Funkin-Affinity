package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.novahandlers.NovaSprite;
import com.example.fnfaffinity.states.PlayState;

public class StageSprite extends NovaSprite {
    public String name;
    /**
     * @param Name Name of the stage asset.
     * @param Path Path at which the image is in "images/"
     * @param xPos
     * @param yPos
     */
    public StageSprite(String Name, String Path, double xPos, double yPos) {
        super(Path, xPos, yPos);
        name = Name;
        //setImage(Path, PlayState.currentFolder);
    }
}
