package com.example.fnfaffinity.novahandlers;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.novahandlers.caches.ImageCache;
import javafx.scene.image.Image;

import java.util.Objects;

import static com.example.fnfaffinity.Main.camGame;

public class NovaSprite extends NovaBasic {
    public String path;
    public Image img;
    public boolean flipX = false;
    private Image getImage(String path) {
        String daPath = "images/" + path + ".png";
        for (ImageCache cachedImage : Main.cachedImages) {
            if (cachedImage.path == daPath) {
                return cachedImage.img;
            }
        }
        Image daImage = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + path + ".png")));
        Main.cachedImages.add(new ImageCache(daImage, daPath));
        return daImage;
    }
    private Image getImage(String path, String folder) {
        String daPath = folder + "images/" + path + ".png";
        CoolUtil.trace(daPath);
        for (ImageCache cachedImage : Main.cachedImages) {
            if (cachedImage.path == daPath) {
                return cachedImage.img;
            }
        }
        Image daImage = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(daPath)));
        Main.cachedImages.add(new ImageCache(daImage, daPath));
        return daImage;
    }

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
        img = getImage(path);
        camera = camGame;
    }

    public NovaSprite (String Path, double xPos, double yPos, String folder) {
        path = Path;
        x = xPos;
        y = yPos;
        defX = xPos;
        defY = yPos;
        img = getImage(path, folder);
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
        img = getImage(path);
    }
    public void setImage(String Path, String folder) {
        path = Path;
        img = getImage(path, folder);
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
