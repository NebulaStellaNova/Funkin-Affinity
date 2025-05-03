package com.example.fnfaffinity.states;

import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.discord.Discord;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.*;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sound.sampled.Clip;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Objects;
import java.util.Vector;

import static com.example.fnfaffinity.backend.utils.CoolUtil.trace;
import static com.example.fnfaffinity.novahandlers.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novahandlers.NovaMath.lerp;

public class FreeplayState extends MusicBeatState {

    public static int curSelected = 0;
    public static int curDifficulty = 1;
    private static NovaSprite bg2;
    private static NovaSprite bgMagenta;
    private static NovaSprite difficultySprite;
    private static NovaSprite variationSprite;
    private static NovaAnimSprite storyButton;
    private static Vector<NovaAlphabet> freeplayItems = new Vector<NovaAlphabet>(0);
    public static Vector<Element> songDataList = new Vector<Element>(0);
    public static Vector<JSONObject> songMetas = new Vector<JSONObject>(0);
    public static String[] songs = {};
    public static String[] items = {};
    public static String[] icons = {};
    private static NovaAlphabet test;
    private static boolean allowSelect = true;
    private static int transtimer = 200;
    private static int coolDown = 0;
    private static String[] difficulties = {"easy", "normal", "hard"};
    private static String curVariation = "";
    
    private static Clip daSong;

    public void update() {
        super.update();

        if (NovaKeys.BACK_SPACE.justPressed) {
            daSong.stop();
            CoolUtil.playMenuSFX(CoolUtil.CANCEL);
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
        difficultySprite.x = (globalCanvas.getWidth()/2) + 450 - ((difficultySprite.img.getWidth()*difficultySprite.scaleX)/2);
        variationSprite.x = (globalCanvas.getWidth()/2) + 450 - ((variationSprite.img.getWidth()*variationSprite.scaleX)/2);
        variationSprite.y = difficultySprite.y - 40;
    }

    public void beat() {
        super.beat();
        //storyButton.playAnim("story mode idle");
    }

    public static String getVariationSprite() {
        switch (curVariation) {
            case "":
                return "normal";
            default:
                return curVariation;
        }
    }

    public void create() {
        super.create();
        curVariation = "";
        Discord.setDescription("Choosing a song.");

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
                    songDataList.add(eElement);
                    JSONObject songMeta = CoolUtil.parseJson("songs/" + eElement.getAttribute("name") + "/meta.json");
                    songMetas.add(songMeta);
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

        difficultySprite = new NovaSprite("menus/freeplaymenu/difficulties/hard", globalStage.getWidth() - 200, 100);
        difficultySprite.alpha = 1.0;
        difficultySprite.setScale(0.75, 0.75);
        difficultySprite.setScrollFactor(0, 0);
        add(difficultySprite);
        variationSprite = new NovaSprite("menus/storymenu/variations/" + getVariationSprite(), globalStage.getWidth() - 200, 100);
        variationSprite.alpha = 1.0;
        variationSprite.setScale(0.75, 0.75);
        variationSprite.setScrollFactor(0, 0);
        add(variationSprite);
        //select(0);
        //CoolUtil.playMenuSong();
        select(0);
        changeDifficulty(0);
    }

    public static void changeDifficulty(int amt) {
        if (daSong!=null) daSong.stop();
        if (curDifficulty + amt > difficulties.length-1) {
            curDifficulty = 0;
        } else if (curDifficulty + amt < 0) {
            curDifficulty = difficulties.length-1;
        } else {
            curDifficulty += amt;
        }
        JSONObject thisSong = songMetas.get(curSelected);
        if (thisSong.has("variations")) {
            for (Object variation : thisSong.getJSONArray("variations")) {
                JSONObject daVariation = (JSONObject) variation;
                //trace(daVariation.getString("name"));
                if (difficulties[curDifficulty].startsWith(daVariation.getString("name"))) {
                    curVariation = daVariation.getString("name");
                }
            }
            //trace(difficulties[curDifficulty].replace(curVariation+ "-", ""));
            difficultySprite.setImage("menus/freeplaymenu/difficulties/" + difficulties[curDifficulty].replace(curVariation+ "-", ""));
            //if (prevVar != curVariation) {
                prevVar = curVariation;
                if (curVariation == "none") {
                    curVariation = "";
                }
                music.stop();

                if (curVariation != "") {
                    String daString = curVariation + "/Inst";
                    if (daString.endsWith("-")) {
                        daString = daString.replace("-", "");
                    }
                    //daSong.stop();
                    if (daSong!=null) daSong.stop();
                    daSong = CoolUtil.getClip("songs/" + songs[curSelected] + "/song/" + daString + ".wav");
                    daSong.start();
                } else {
                    //daSong.stop();
                    if (daSong!=null) daSong.stop();
                    daSong = CoolUtil.getClip("songs/" + songs[curSelected] + "/song/Inst.wav");
                    daSong.start();
                }
            //}
        } else {
            difficultySprite.setImage("menus/freeplaymenu/difficulties/" + difficulties[curDifficulty]);
        }
        variationSprite.setImage("menus/storymenu/variations/" + getVariationSprite());
    }
    public void pickSelection() {
        String daFolder = difficulties[curDifficulty];
        if (!Objects.equals(curVariation, "")) {
            daFolder = curVariation + "/" + difficulties[curDifficulty].replace(curVariation+"-", "");
        }
        File songChart = new File(pathify("songs/" + items[curSelected].toLowerCase() + "/charts/" + daFolder + ".json"));
        if (!songChart.exists()) {
            trace("Chart does not exist. Not entering PlayState.");
            CoolUtil.playMenuSFX(CoolUtil.CANCEL);
            return;
        }
        daSong.stop();
        PlayState.difficulty = difficulties[curDifficulty];
        if (PlayState.difficulty.startsWith("-")) {
            PlayState.difficulty = PlayState.difficulty.replace("-", "");
        }
        PlayState.curVariation = curVariation;
        PlayState.isStoryMode = false;
        PlayState.song = items[curSelected];
        //destroy();
        switchState(new PlayState());
    }
    private static String prevVar = "";
    public static void select(int change) {
        curVariation = "none";
        if (coolDown == 0) {
            coolDown = 5;
            if (allowSelect) {
                if (change != 0)
                    CoolUtil.playMenuSFX(CoolUtil.SCROLL);
                if (curSelected + change > items.length - 1) {
                    curSelected = 0;
                } else if (curSelected + change < 0) {
                    curSelected = items.length - 1;
                } else
                    curSelected += change;
                for (int i = 0; i < items.length; i++) {
                    if (i == curSelected) {
                        difficulties = songDataList.get(curSelected).getAttribute("difficulties").split(",");
                        curVariation = "";
                        changeDifficulty(0);
                        //freeplayItems.set(i, freeplayItems.get(i)).playAnim(items[i] + " selected");
                    } else {
                        //freeplayItems.set(i, freeplayItems.get(i)).playAnim(items[i] + " idle");
                    }
                }
            }
        }
        if (curVariation == "") {
            daSong = CoolUtil.getClip("songs/" + songs[curSelected] + "/song/Inst.wav");
            daSong.start();
            //CoolUtil.playMusic("songs/" + songs[curSelected] + "/song/Inst.mp3");
        }
    }
    public void destroy() {
        super.destroy();
        freeplayItems = new Vector<NovaAlphabet>(0);
        items = new String[] {};
        songs = new String[] {};
        icons = new String[] {};
    }
}
