package com.example.fnfaffinity.novahandlers;

public class NovaKey {
    public boolean justPressed = false;
    public boolean justReleased = false;
    public boolean pressed = false;

    public int frame = -1;

    public void update() {
        if (frame > -1) {
            frame--;
        }
        if (frame == 0) {
            justPressed = false;
            justReleased = false;
        }
    }

}
