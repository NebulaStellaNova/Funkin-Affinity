package com.example.fnfaffinity.novahandlers;

import java.util.Objects;
import java.util.Vector;

public class NovaAnimSprite extends NovaSprite {
    public String curAnim;
    public double curFrame;
    public boolean loopAnim = true;
    public double framerate = 24;
    public double frameWidth;
    public Vector<NovaAnimController> animations;

    public NovaAnimSprite(String Path, double xPos, double yPos) {
        super(Path, xPos, yPos);
        curFrame = 0;
        curAnim = "";
        animations = new Vector<NovaAnimController>(0);
    }
    public void addAnimation(String name, int frameps, boolean loop) {
        animations.add(new NovaAnimController(name, loop, frameps));
        loopAnim = loop;
        framerate = frameps;
    }
    public void addAnimation(String name, String prefix, int frameps, boolean loop) {
        animations.add(new NovaAnimController(name, prefix, loop, frameps));
        loopAnim = loop;
        framerate = frameps;
    }
    public void addAnimation(String name, int frameps, boolean loop, double offsetx, double offsety) {
        animations.add(new NovaAnimController(name, loop, frameps, offsetx, offsety));
        loopAnim = loop;
        framerate = frameps;
    }
    public void addAnimation(String name, String prefix, int frameps, boolean loop, double offsetx, double offsety) {
        animations.add(new NovaAnimController(name, prefix, loop, frameps, offsetx, offsety));
        loopAnim = loop;
        framerate = frameps;
    }

    public NovaAnimController getAnimation(String name) {
        for (NovaAnimController i : animations) {
            if (Objects.equals(i.name, name)) {
                return i;
            }
        }
        return null;
    }

    public void playAnim(String name) {
        //System.out.println(name);
        curAnim = name;
        for (NovaAnimController i : animations) {
            if (Objects.equals(i.name, name)) {
                getAnimation(name).curFrame = 0;
            }
        }

        //curFrame = 0;
    }
}
