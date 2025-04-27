package com.example.fnfaffinity.novautils;

public class NovaAnimController {
    public String name;
    public String prefix = "none";
    public boolean loop = false;
    public double curFrame = 0;
    public double fps = 0;
    public double offsetX = 0;
    public double offsetY = 0;

    NovaAnimController (String animName, boolean loopAnim, int frame, double offsetx, double offsety) {
        name = animName;
        loop = loopAnim;
        fps = frame;
        offsetX = offsetx;
        offsetY = offsety;
        prefix = animName;
    }

    NovaAnimController (String animName, String animPrefix, boolean loopAnim, int frame, double offsetx, double offsety) {
        name = animName;
        loop = loopAnim;
        fps = frame;
        offsetX = offsetx;
        offsetY = offsety;
        prefix = animPrefix;
    }

    NovaAnimController (String animName, boolean loopAnim, int frame) {
        name = animName;
        loop = loopAnim;
        fps = frame;
        prefix = animName;
    }
    NovaAnimController (String animName, String animPrefix, boolean loopAnim, int frame) {
        name = animName;
        loop = loopAnim;
        fps = frame;
        prefix = animPrefix;
    }

    public void setOffsets(double x, double y) {
        offsetX = x;
        offsetY = y;
    }

    public double[] getOffsets() {
        return new double[] {offsetX, offsetY};
    }
}
