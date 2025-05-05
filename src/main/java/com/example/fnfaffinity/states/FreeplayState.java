package com.example.fnfaffinity.states;

import com.almasb.fxgl.core.collection.Array;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.discord.Discord;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.*;
import javafx.scene.text.TextAlignment;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sound.sampled.Clip;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

import static com.example.fnfaffinity.backend.utils.CoolUtil.checkFileExists;
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

    public static String currentFolder = "";

    public static Vector<String[]> songLists = new Vector<>();
    public static Vector<String[]> itemLists = new Vector<>();
    public static Vector<String[]> iconLists = new Vector<>();
    public static int startingIndex = 0;


    private static NovaAlphabet test;
    private static boolean allowSelect = true;
    private static int transtimer = 200;
    private static int coolDown = 0;
    private static String[] difficulties = {"easy", "normal", "hard"};
    private static String curVariation = "";

    private int folderThingSize = 1;

    private NovaGraphic fg;
    public NovaText description;
    
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

        description.y = fg.y + 32;
        description.x = globalCanvas.getWidth()/2;

        if (NovaKeys.TAB.justReleased) {
            FreeplayState nextFreeplay = new FreeplayState();
            nextFreeplay.folderThingSize = folderThingSize;
            //startingIndex = folderThingSize;
            if (startingIndex + 1 >= folderThingSize) {
                startingIndex = 0;
            } else {
                startingIndex++;
            }
            switchState(nextFreeplay);
        }
        if (modFolders.length == 0) {
            description.visible = false;
            fg.alpha = 0;
        }
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
        folderThingSize = 1;
        for (String folder : modFolders) {
            folderThingSize++;
        }

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

        songLists.add(new String[]{});
        itemLists.add(new String[]{});
        iconLists.add(new String[]{});
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
                    songLists.set(0, CoolUtil.addToArray(songLists.get(0), eElement.getAttribute("name")));
                    itemLists.set(0, CoolUtil.addToArray(itemLists.get(0), songName));
                    iconLists.set(0, CoolUtil.addToArray(iconLists.get(0), ico));
                }
            }


        } catch (Exception ignore) {
        }



        int folderIndex = 1;
        for (String folderName : modFolders) {
            trace(folderName);
            songLists.add(new String[]{});
            itemLists.add(new String[]{});
            iconLists.add(new String[]{});
            try {
                final String filepath = pathify("mods/" + folderName + "/data/songsList.xml");
                trace(filepath);
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
                        JSONObject songMeta = CoolUtil.parseJson("mods/" + folderName + "/songs/" + eElement.getAttribute("name") + "/meta.json");
                        songMetas.add(songMeta);
                        String songName = songMeta.getString("displayName");
                        String ico = songMeta.getString("icon");
                        trace(songName);
                        songLists.set(folderIndex, CoolUtil.addToArray(songLists.get(folderIndex), eElement.getAttribute("name")));
                        itemLists.set(folderIndex, CoolUtil.addToArray(itemLists.get(folderIndex), songName));
                        iconLists.set(folderIndex, CoolUtil.addToArray(iconLists.get(folderIndex), ico));
                    }
                }
            } catch (ParserConfigurationException | IOException | SAXException ignore) {
            }
        }

        songs = songLists.get(startingIndex);
        items = itemLists.get(startingIndex);
        icons = iconLists.get(startingIndex);
        if (startingIndex != 0) {
            currentFolder = "mods/" + modFolders[startingIndex-1] + "/";
        } else {
            currentFolder = "";
        }

        for (String[] list : songLists) {
            for (String item : list) {
                trace("Song: " + item);
            }
        }

        //System.out.println("Root element :" + document.getDocumentElement().getNodeName());

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            final NovaAlphabet temp = new NovaAlphabet(item, 300, 100 + (100 * i));
            temp.icon = new NovaSprite("icons/" + icons[i], 0, 0, currentFolder);
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

        fg = new NovaSprite(0, 200).makeGraphic(globalCanvas.getWidth(), 50, "#000000");
        fg.alpha = 0.5;
        fg.scrollX = 0;
        fg.scrollY = 0;
        fg.y = globalCanvas.getHeight()-100;
        add(fg);

        description = new NovaText("Press TAB to switch mod.", 0, 0);
        description.camera = camGame;
        description.alignment = TextAlignment.CENTER;
        description.scrollX = 0;
        description.scrollY = 0;
        description.size = 20;
        add(description);
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
                    daSong = CoolUtil.getClip(currentFolder + "songs/" + songs[curSelected] + "/song/" + daString + ".wav");
                    daSong.start();
                } else {
                    //daSong.stop();
                    if (daSong!=null) daSong.stop();
                    daSong = CoolUtil.getClip(currentFolder + "songs/" + songs[curSelected] + "/song/Inst.wav");
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
        if (daFolder.startsWith("-")) {
            daFolder = daFolder.replace("-", "");
        }
        trace(currentFolder + "songs/" + items[curSelected].toLowerCase() + "/charts/" + daFolder + ".json");
        if (!checkFileExists(currentFolder + "songs/" + items[curSelected].toLowerCase() + "/charts/" + daFolder + ".json")) {
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
        PlayState.currentFolder = currentFolder;

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
            daSong = CoolUtil.getClip(currentFolder + "songs/" + songs[curSelected] + "/song/Inst.wav");
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
        songLists = new Vector<>();
        itemLists = new Vector<>();
        iconLists = new Vector<>();
    }
}
