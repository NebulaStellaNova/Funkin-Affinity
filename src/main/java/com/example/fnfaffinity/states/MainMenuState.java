package com.example.fnfaffinity.states;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.example.fnfaffinity.backend.scripting.ScriptEvents;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.discord.Discord;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import org.json.JSONObject;

import java.util.Vector;

import static com.example.fnfaffinity.novahandlers.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novahandlers.NovaMath.lerp;

public class MainMenuState extends MusicBeatState {

    public static int curSelected = 0;
    private static NovaSprite bg2;
    private static NovaSprite bgMagenta;
    private static NovaAnimSprite storyButton;
    private static Vector<NovaAnimSprite> menuItems = new Vector<NovaAnimSprite>(0);
    private static String[] items = {"story mode", "freeplay", "credits", "options"};
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
        } else if (NovaKeys.BACK_SPACE.justPressed) {
            CoolUtil.playMenuSFX(CoolUtil.CANCEL);
            //switchState(new TitleState());
            CoolUtil.trace("Not returning to title because breaks transition");
        }
        camGame.y = lerp(camGame.y, -(-150 + (200* curSelected)), getDtFinal(4));
        if (doTimer && minitimer > 0) {
            minitimer -= 1;
        }
        if (minitimer == 0) {
            switch (items[curSelected]) {
                case "story mode":
                    switchState(new StoryMenuState());
                    break;
                case "freeplay":
                    switchState(new FreeplayState());
                    break;
                case "credits":
                    switchState(new CreditsMenu());
            }
            minitimer = -1;
        }
    }

    public void beat() {
        super.beat();
        //storyButton.playAnim("story mode idle");
    }

    public void preCreate() {
        // Set script vars to class vars here
        script.call("preCreate");
        // Set class vars to script vars here
    }

    public void create() {
        super.create();
        Discord.setDescription("In The Menus.");
        allowSelect = true;
        minitimer = 1;
        doTimer = false;
        CoolUtil.playMenuSong();
        //background = new NovaAnimSprite("title/background", 0, 0);
        //background.addAnimation("city", 10, false);
        //background.playAnim("city");
        //background.setScale(3.0, 3.0);
        //add(background);
        bg2 = new NovaSprite("menus/mainmenu/menuBG", 0,-50);
        bg2.alpha = 1.0;
        bg2.setScale(0.8, 0.8);
        bg2.setScrollFactor(0.3, 0.3);
        bg2.visible = true;
        bg2.angle = 5;
        add(bg2);
        bgMagenta = new NovaSprite("menus/mainmenu/menuBGMagenta", 0,-50);
        bgMagenta.alpha = 1.0;
        bgMagenta.setScale(0.8, 0.8);
        bgMagenta.setScrollFactor(0.3, 0.3);
        bgMagenta.visible = false;
        add(bgMagenta);
        test = new NovaAlphabet("Hello", 100, 300);
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            final NovaAnimSprite temp = new NovaAnimSprite("menus/mainmenu/" + item, 100, 100 + (200*i));
            temp.addAnimation(item + " idle", item + " basic", 24, true);
            temp.addAnimation(item + " selected", item + " white", 24, true);
            temp.playAnim(item + " idle");
            temp.alpha = 1.0;
            menuItems.add(temp);
            add(temp);
        }
        select(0, true);

        //trace(daScriptEvent.cancelled);
    }
    public void postCreate() {
        super.postCreate();
        //CancellableEvent daScriptEvent = (CancellableEvent) script.call("testEventCall", new CancellableEvent());
    }

    public static void pickSelection() {
        JSONObject event = ScriptEvents.SelectEvent(curSelected, items[curSelected]);
        boolean eventCancelled = script.call("selectMenu", event);
        if (eventCancelled) return;

        allowSelect = false;
        Timeline selectLoop = new Timeline(
        new KeyFrame(Duration.millis(250/2),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        bg2.visible = !bg2.visible;
                        bgMagenta.visible = !bgMagenta.visible;
                        for (int i = 0; i < items.length; i++) {
                            if (i != curSelected) {
                                if (menuItems.size() > i)
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
            int futureSelect = curSelected;
            if (futureSelect + change > items.length - 1) {
                futureSelect = 0;
            } else if (futureSelect + change < 0) {
                futureSelect = items.length - 1;
            } else
                futureSelect += change;

            // Scroll Event.
            JSONObject event = ScriptEvents.ScrollEvent(futureSelect);
            boolean eventCancelled = script.call("scrollMenu", event);
            if (eventCancelled) return;


            CoolUtil.playMenuSFX(CoolUtil.SCROLL);
            if (curSelected + change > items.length - 1) {
                curSelected = 0;
            } else if (curSelected + change < 0) {
                curSelected = items.length - 1;
            } else
                curSelected += change;
            for (int i = 0; i < items.length; i++) {
                if (i == curSelected) {
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
                CoolUtil.playMenuSFX(CoolUtil.SCROLL);
            if (curSelected + change > items.length - 1) {
                curSelected = 0;
            } else if (curSelected + change < 0) {
                curSelected = items.length - 1;
            } else
                curSelected += change;
            for (int i = 0; i < items.length; i++) {
                if (i == curSelected) {
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
