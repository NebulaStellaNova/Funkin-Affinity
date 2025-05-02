package com.example.fnfaffinity.states;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.novahandlers.*;

import javax.sound.sampled.Clip;
import java.util.Vector;

import static com.example.fnfaffinity.novahandlers.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novahandlers.NovaMath.lerp;

public class PauseMenuSubState extends Main {
    public boolean isOpen = false;
    public NovaGraphic background;
    public Vector<NovaAlphabet> pauseTexts = new Vector<NovaAlphabet>(0);
    public String[] pauseOptions = {
            "resume",
            "restart",
            "exit to menu"
    };
    public int curSelected = 0;

    public Clip breakFast = CoolUtil.getClip("audio/pause/breakfast.wav");

    public void create() {
        background = new NovaSprite(0, 0).makeGraphic(globalCanvas.getWidth(), globalCanvas.getHeight(), "#000000");
        background.scrollX = 0;
        background.scrollY = 0;
        background.alpha = 0.2;
        add(background);
        for (int i = 0; i < pauseOptions.length; i++) {
            String item = pauseOptions[i];
            final NovaAlphabet temp = new NovaAlphabet(item, 300, 100 + (75 * i));
            add(temp);
            pauseTexts.add(temp);
        }
    }
    public void update() {
        for (NovaAlphabet text : pauseTexts) {
            for (NovaSprite character : text.sprites) {
                if (character != null) {
                    character.setScrollFactor(0, 0);
                    //character.visible = isOpen;
                    if (isOpen)
                        character.alpha = lerp(character.alpha, 1, 0.1);
                    else
                        character.alpha = lerp(character.alpha, 0, 0.1);
                }
            }
        }
        if (isOpen) {
            background.alpha = lerp(background.alpha, 0.4, 0.1);
        } else {
            background.alpha = lerp(background.alpha, 0, 0.1);
        }

        for (int i = 0; i < pauseOptions.length; i++) {
            if (isOpen) {
                if (i == curSelected) {
                    pauseTexts.set(i, pauseTexts.get(i)).x = lerp(pauseTexts.get(i).x, 150 - 200, getDtFinal(10));
                } else {
                    pauseTexts.set(i, pauseTexts.get(i)).x = lerp(pauseTexts.get(i).x, 50 - 200, getDtFinal(10));
                }
            } else {
                pauseTexts.set(i, pauseTexts.get(i)).x = lerp(pauseTexts.get(i).x, 300, getDtFinal(10));
            }
        }

        if (!isOpen) return;

        if (!breakFast.isRunning()) {
            breakFast.setMicrosecondPosition(0);
            breakFast.start();
        }

        if (NovaKeys.UP.justPressed) {
            CoolUtil.playMenuSFX(CoolUtil.SCROLL);
            if (curSelected - 1 >= 0) {
                curSelected--;
            } else curSelected = pauseOptions.length-1;
        } else if (NovaKeys.DOWN.justPressed) {
            CoolUtil.playMenuSFX(CoolUtil.SCROLL);
            if (curSelected + 1 <= pauseOptions.length-1) {
                curSelected++;
            } else curSelected = 0;
        }


    }
    public void open() {
        isOpen = true;
        breakFast.setMicrosecondPosition(0);
        breakFast.start();
    }

    public void close() {
        isOpen = false;
        breakFast.stop();
    }
}
