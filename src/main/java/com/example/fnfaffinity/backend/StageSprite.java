package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.novautils.NovaSprite;

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
    }
}
