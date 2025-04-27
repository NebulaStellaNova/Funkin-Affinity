package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.novautils.NovaCamera;
import com.example.fnfaffinity.novautils.NovaGroup;

import java.io.IOException;
import java.util.Vector;

public class StrumLine extends NovaGroup {

    public static int spacing = 115;
    public double x;
    public double y;

    public StrumLine(int amount, double xPos, double yPos, NovaCamera camera) throws IOException {
        x = xPos;
        y = yPos;
        for (int i = 0; i < amount; i++) {
            Strum daStrum = new Strum("default", (spacing * i) + x, y, i);
            daStrum.camera = camera;
            addToGroup(daStrum);
        }
    }
}
