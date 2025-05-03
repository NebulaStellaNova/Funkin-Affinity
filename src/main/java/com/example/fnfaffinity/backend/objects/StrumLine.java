package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.novahandlers.NovaCamera;
import com.example.fnfaffinity.novahandlers.NovaGroup;

import java.io.IOException;

public class StrumLine extends NovaGroup {

    public static int spacing = 115;
    public double defX;
    public double defY;
    public double x;
    public double y;
    public int type;
    public boolean visible = true;
    public String skin = "default";

    public StrumLine(int amount, double xPos, double yPos, NovaCamera camera, int daType, boolean daVisible, String skin) throws IOException {
        x = xPos;
        defX = xPos;
        y = yPos;
        defY = yPos;
        type = daType;
        visible = daVisible;
        for (int i = 0; i < amount; i++) {
            Strum daStrum = new Strum(skin, (spacing * i) + x, y, i);
            daStrum.camera = camera;
            addToGroup(daStrum);
        }
    }
}
