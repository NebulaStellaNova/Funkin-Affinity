package com.example.fnfaffinity.novautils;

import static com.example.fnfaffinity.Main.fps;

public class NovaMath {
    public static double lerp(double start, double end, double amt) {
        return (1 - amt) * start + amt * end;
    }
    public static double getDtFinal(double num) {
        return num/fps;
    }
}
