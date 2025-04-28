package com.example.fnfaffinity.states;

import com.example.fnfaffinity.backend.CoolUtil;
import com.example.fnfaffinity.backend.MusicBeatState;
import com.example.fnfaffinity.novautils.*;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Vector;

import static com.example.fnfaffinity.novautils.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novautils.NovaMath.lerp;

public class FreeplayState extends MusicBeatState {

    public static int curSelected = 0;
    public static int curDifficulty = 1;
    private static NovaSprite bg2;
    private static NovaSprite bgMagenta;
    private static NovaSprite difficultySprite;
    private static NovaAnimSprite storyButton;
    private static Vector<NovaAlphabet> freeplayItems = new Vector<NovaAlphabet>(0);
    public static String[] songs = {};
    public static String[] items = {};
    public static String[] icons = {};
    private static NovaAlphabet test;
    private static boolean allowSelect = true;
    private static int transtimer = 200;
    private static int coolDown = 0;
    private static String[] difficulties = {"easy", "normal", "hard"};

    public void update() {
        super.update();

        if (NovaKeys.BACK_SPACE.justPressed) {
            switchState(new MainMenuState());
        } else if (NovaKeys.DOWN.justPressed) {
            select(1);
        } else if (NovaKeys.UP.justPressed) {
            select(-1);
        } else if (NovaKeys.RIGHT.justPressed) {
            changeDifficulty(1);
        } else if (NovaKeys.LEFT.justPressed) {
            changeDifficulty(-1);
        } else if (NovaKeys.ENTER.justPressed) {
            pickSelection();
        }

        for (int i = 0; i < items.length; i++) {
            if (i == curSelected) {
                freeplayItems.set(i, freeplayItems.get(i)).x = lerp(freeplayItems.get(i).x, 150 - 200, getDtFinal(10));
            } else {
                freeplayItems.set(i, freeplayItems.get(i)).x = lerp(freeplayItems.get(i).x, 50 - 200, getDtFinal(10));
            }
        }
        //System.out.println("do Somin");
        //transitionSprite.playAnim("In");
        camGame.y = lerp(camGame.y, -(-200 + 100 + (200* curSelected)), getDtFinal(4));
        transitionSprite.visible = false;
        if (coolDown > 0) {
            coolDown -= 1;
        }

        difficultySprite.x = globalStage.getWidth() - 200 - ((difficultySprite.img.getWidth()*difficultySprite.scaleX)/2);
    }

    public void beat() {
        super.beat();
        //storyButton.playAnim("story mode idle");
    }

    public void create() {
        super.create();

        bg2 = new NovaSprite("menus/mainmenu/menuBG", 0, -50);
        bg2.alpha = 1.0;
        bg2.setScale(0.8, 0.8);
        bg2.setScrollFactor(0, 0);
        bg2.visible = true;
        add(bg2);
        bgMagenta = new NovaSprite("menus/mainmenu/menuBGMagenta", 0, -50);
        bgMagenta.alpha = 1.0;
        bgMagenta.setScale(0.8, 0.8);
        bgMagenta.setScrollFactor(0.3, 0.3);
        bgMagenta.visible = false;
        add(bgMagenta);
        //test = new NovaAlphabet("Hello", 100, 300);
        try {

            final String filepath = pathify("data/songsList.xml");
            final File file = new File(filepath);
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document document = db.parse(file);
            document.getDocumentElement().normalize();
            final NodeList nList = document.getElementsByTagName("song");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    JSONObject songMeta = CoolUtil.parseJson("songs/" + eElement.getAttribute("name") + "/meta.json");
                    String songName = songMeta.getString("displayName");
                    String ico = songMeta.getString("icon");
                    songs = CoolUtil.addToArray(songs, eElement.getAttribute("name"));
                    items = CoolUtil.addToArray(items, songName);
                    icons = CoolUtil.addToArray(icons, ico);
                }
            }


        } catch (Exception ignore) {
        }

        //System.out.println("Root element :" + document.getDocumentElement().getNodeName());

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            final NovaAlphabet temp = new NovaAlphabet(item, 300, 100 + (100 * i));
            temp.icon = new NovaSprite("icons/" + icons[i], 0, 0);
            add(temp);
            freeplayItems.add(temp);
        }

        difficultySprite = new NovaSprite("menus/storymenu/difficulties/" + difficulties[curDifficulty], globalStage.getWidth() - 200, 100);
        difficultySprite.alpha = 1.0;
        difficultySprite.setScale(0.5, 0.5);
        difficultySprite.setScrollFactor(0, 0);
        add(difficultySprite);
        //select(0);
        //CoolUtil.playMenuSong();
        CoolUtil.playMusic("songs/" + songs[curSelected] + "/song/Inst.mp3");
    }

    public void changeDifficulty(int amt) {
        if (curDifficulty + amt > difficulties.length-1) {
            curDifficulty = 0;
        } else if (curDifficulty + amt < 0) {
            curDifficulty = difficulties.length-1;
        } else {
            curDifficulty += amt;
        }
        difficultySprite.setImage("menus/storymenu/difficulties/" + difficulties[curDifficulty]);
    }
    public void pickSelection() {
        PlayState.difficulty = difficulties[curDifficulty];
        PlayState.isStoryMode = false;
        PlayState.song = items[curSelected];
        //destroy();
        switchState(new PlayState());
    }
    public static void select(int change) {
        if (coolDown == 0) {
            coolDown = 5;
            if (allowSelect) {
                CoolUtil.playMenuSFX(CoolUtil.SCROLL);
                if (curSelected + change > items.length - 1) {
                    curSelected = 0;
                } else if (curSelected + change < 0) {
                    curSelected = items.length - 1;
                } else
                    curSelected += change;
                for (int i = 0; i < items.length; i++) {
                    if (i == curSelected) {
                        //freeplayItems.set(i, freeplayItems.get(i)).playAnim(items[i] + " selected");
                    } else {
                        //freeplayItems.set(i, freeplayItems.get(i)).playAnim(items[i] + " idle");
                    }
                }
            }
        }
        CoolUtil.playMusic("songs/" + songs[curSelected] + "/song/Inst.mp3");
    }
    public void destroy() {
        super.destroy();
        freeplayItems = new Vector<NovaAlphabet>(0);
        items = new String[] {};
        songs = new String[] {};
        icons = new String[] {};
    }
}
