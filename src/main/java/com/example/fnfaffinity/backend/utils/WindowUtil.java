package com.example.fnfaffinity.backend.utils;

import com.example.fnfaffinity.Main;
import javafx.scene.image.Image;

import java.util.Objects;

public class WindowUtil extends Main {
    public static Image defaultImage = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/iconOG.png")));

    public static void setWindowIcon(Image img) {
        globalStage.getIcons().set(0, img);
    }

    public static void setWindowIcon(String img) {
        if (!img.endsWith(".png")) {
            img += ".png";
        }
        CoolUtil.trace("Changed Window Icon to: $green images/" + img + "$reset");
        globalStage.getIcons().set(0, new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + img))));
    }
    public static void setWindowIcon(int index, String img) {
        if (!img.endsWith(".png")) {
            img += ".png";
        }
        CoolUtil.trace("images/" + img);
        if (globalStage.getIcons().size()-1 < index) {
            globalStage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + img))));
        } else {
            globalStage.getIcons().set(index, new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + img))));
        }
    }
}
