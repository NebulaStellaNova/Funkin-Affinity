package com.example.fnfaffinity.states;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.backend.CoolUtil;
import com.example.fnfaffinity.backend.MusicBeatState;
import com.example.fnfaffinity.novautils.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

import static com.example.fnfaffinity.novautils.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novautils.NovaMath.lerp;

public class MainMenuState extends MusicBeatState {

    public static int curSelcted = 0;
    private static NovaSprite bg2;
    private static NovaSprite bgMagenta;
    private static NovaAnimSprite storyButton;
    private static Vector<NovaAnimSprite> menuItems = new Vector<NovaAnimSprite>(0);
    private static String[] items = {"story mode", "freeplay", "options"};
    private static NovaAlphabet test;
    private static boolean allowSelect = true;
    private static int minitimer = 1;
    private static boolean doTimer = false;


    public void update() {
        super.update();
        if (NovaKeys.DOWN.justPressed) {
            select(1);
        } else if (NovaKeys.UP.justPressed) {
            select(-1);
        } else if (NovaKeys.ENTER.justPressed) {
            pickSelection();
        }
        camGame.y = lerp(camGame.y, -(-150 + (200*curSelcted)), getDtFinal(4));
        if (doTimer && minitimer > 0) {
            minitimer -= 1;
        }
        if (minitimer == 0) {
            switch (curSelcted) {
                case 1:
                    //doTransition("in");
                    //destroy();
                    switchState(new FreeplayState());
                    break;
            }
            minitimer = -1;
        }
    }

    public void beat() {
        super.beat();
        //storyButton.playAnim("story mode idle");
    }

    public void create() {
        super.create();
        allowSelect = true;
        CoolUtil.playMenuSong();
        //background = new NovaAnimSprite("title/background", 0, 0);
        //background.addAnimation("city", 10, false);
        //background.playAnim("city");
        //background.setScale(3.0, 3.0);
        //add(background);
        bg2 = new NovaSprite("mainmenu/menuBG", 0,-50);
        bg2.alpha = 1.0;
        bg2.setScale(0.8, 0.8);
        bg2.setScrollFactor(0.3, 0.3);
        bg2.visible = true;
        add(bg2);
        bgMagenta = new NovaSprite("mainmenu/menuBGMagenta", 0,-50);
        bgMagenta.alpha = 1.0;
        bgMagenta.setScale(0.8, 0.8);
        bgMagenta.setScrollFactor(0.3, 0.3);
        bgMagenta.visible = false;
        add(bgMagenta);
        test = new NovaAlphabet("Hello", 100, 300);
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            final NovaAnimSprite temp = new NovaAnimSprite("mainmenu/main_menu", 100, 100 + (200*i));
            temp.addAnimation(item + " idle", 24, true);
            temp.addAnimation(item + " selected", 24, true);
            temp.playAnim(item + " idle");
            temp.alpha = 1.0;
            menuItems.add(temp);
            add(temp);
        }
        select(0, true);
    }

    public static void pickSelection() {
        allowSelect = false;
        Timeline selectLoop = new Timeline(
        new KeyFrame(Duration.millis(250/2),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        bg2.visible = !bg2.visible;
                        bgMagenta.visible = !bgMagenta.visible;
                        for (int i = 0; i < items.length; i++) {
                            if (i != curSelcted) {
                                menuItems.set(i, menuItems.get(i)).visible = false;
                            }
                        }
                    }
                }));
        selectLoop.setCycleCount(8);

        EventHandler<ActionEvent> resetItems = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                /*for (int i = 0; i < items.length; i++) {
                    if (i != curSelcted) {
                        menuItems.set(i, menuItems.get(i)).visible = true;
                    }
                }*/
                //allowSelect = true;
                //doTransition("out");
                doTimer = true;
            }
        };
        selectLoop.setOnFinished(resetItems);
        selectLoop.play();
        CoolUtil.playMenuSFX(CoolUtil.CONFIRM);
    }
    public static void select(int change) {
        if (allowSelect) {
            CoolUtil.playMenuSFX(CoolUtil.SCROLL);
            if (curSelcted + change > items.length - 1) {
                curSelcted = 0;
            } else if (curSelcted + change < 0) {
                curSelcted = items.length - 1;
            } else
                curSelcted += change;
            for (int i = 0; i < items.length; i++) {
                if (i == curSelcted) {
                    menuItems.set(i, menuItems.get(i)).playAnim(items[i] + " selected");
                } else {
                    menuItems.set(i, menuItems.get(i)).playAnim(items[i] + " idle");
                }
            }
        }
    }
    public static void select(int change, boolean silent) {
        if (allowSelect) {
            if (!silent)
                scrollMenu.play();
            if (curSelcted + change > items.length - 1) {
                curSelcted = 0;
            } else if (curSelcted + change < 0) {
                curSelcted = items.length - 1;
            } else
                curSelcted += change;
            for (int i = 0; i < items.length; i++) {
                if (i == curSelcted) {
                    menuItems.set(i, menuItems.get(i)).playAnim(items[i] + " selected");
                } else {
                    menuItems.set(i, menuItems.get(i)).playAnim(items[i] + " idle");
                }
            }
        }
    }
    public void destroy() {
        super.destroy();
        menuItems = new Vector<NovaAnimSprite>(0);
        doTimer = false;
        allowSelect = true;
    }
}
