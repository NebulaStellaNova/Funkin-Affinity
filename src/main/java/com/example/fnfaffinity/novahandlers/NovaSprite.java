package com.example.fnfaffinity.novahandlers;

import com.example.fnfaffinity.Main;
import javafx.scene.image.Image;

import java.util.Objects;

import static com.example.fnfaffinity.Main.camGame;

public class NovaSprite {
    public boolean alive = true;
    public double x;
    public double y;
    public double defX;
    public double defY;
    public double scaleX = 1;
    public double scaleY = 1;
    public double scrollX = 1;
    public double scrollY = 1;
    public double alpha = 1;
    public double angle = 0;
    public boolean visible = true;
    public String path;
    public Image img;
    public NovaCamera camera;
    public boolean flipX = false;

    /**
     * @param Path Path at which the image is in "images/"
     * @param xPos
     * @param yPos
     */
    public NovaSprite (String Path, double xPos, double yPos) {
        path = Path;
        x = xPos;
        y = yPos;
        defX = xPos;
        defY = yPos;
        img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + path + ".png")));
        camera = camGame;
    }

    public NovaSprite (String Path, double xPos, double yPos, String folder) {
        path = Path;
        x = xPos;
        y = yPos;
        defX = xPos;
        defY = yPos;
        img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(folder + "/images/" + path + ".png")));
        camera = camGame;
    }

    public NovaSprite (double xPos, double yPos) {
        x = xPos;
        y = yPos;
        defX = xPos;
        defY = yPos;
        camera = camGame;
    }
    public void setImage(String Path) {
        path = Path;
        img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + path + ".png")));
    }

    public void setScale(double scalex, double scaley){
        scaleX = scalex;
        scaleY = scaley;
    }
    public void setScrollFactor(double scrollx, double scrolly){
        scrollX = scrollx;
        scrollY = scrolly;
    }

    public void destroy() {
        alive = false;
    }

    public void respawn() {
        alive = true;
    }

    public NovaGraphic makeGraphic(double width, double height, String color) {
        return new NovaGraphic(this.camera, this.x, this.y, this.scrollX, this.scrollY, width, height, color);
    }
}
