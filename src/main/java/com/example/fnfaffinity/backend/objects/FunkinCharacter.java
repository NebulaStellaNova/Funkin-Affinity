package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.novahandlers.NovaAnimController;
import com.example.fnfaffinity.novahandlers.NovaAnimSprite;
import com.example.fnfaffinity.states.PlayState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

public class FunkinCharacter extends NovaAnimSprite {
    public String name = "";
    public String sprite = "";
    public String icon = "";
    public String color = "";
    public Document characterData;
    public int resetTimer = 0;
    public int holdTimer = 0;
    public double camOffsetX = 0;
    public double camOffsetY = 0;
    public double offsetX = 0;
    public double offsetY = 0;
    public boolean isPlayer = false;
    public boolean isSpectator = false;
    public boolean isOpponent = false;

    public void getCharacterData(String name) {
        if (!CoolUtil.checkFileExists("data/characters/" + name + ".xml")) {
            characterData = CoolUtil.parseXML(PlayState.currentFolder + "data/characters/" + name, "data/characters/bf");
        } else {
            characterData = CoolUtil.parseXML("data/characters/" + name, "data/characters/bf");
        }

        final NodeList characterList = characterData.getElementsByTagName("character");
        final Element daCharacter = (Element) characterList.item(0);
        sprite = daCharacter.getAttribute("sprite");
        icon = daCharacter.getAttribute("icon");
        color = daCharacter.getAttribute("color");
        flipX = Boolean.parseBoolean(daCharacter.getAttribute("flipX"));

        String daOffX = daCharacter.getAttribute("camOffsetX");
        String daOffY = daCharacter.getAttribute("camOffsetY");

        String offX = daCharacter.getAttribute("x");
        String offY = daCharacter.getAttribute("y");
        //if (((Element) characterList.item(0)).hasAttribute("camOffsetX"))

        if (!daOffX.isEmpty()) {
            camOffsetX = parseInt(daOffX);
        }
        if (!daOffY.isEmpty()) {
            camOffsetY = parseInt(daOffY);
        }
        if (!offX.isEmpty()) {
            offsetX = parseInt(offX);
        }
        if (!offY.isEmpty()) {
            offsetY = parseInt(offY);
        }
        //if (((Element) characterList.item(0)).hasAttribute("camOffsetY"))
        //camOffsetY = Double.parseDouble(((Element) characterList.item(0)).getAttribute("camOffsetY"));
        //trace(name);
        //trace(flipX);
            /*if (Boolean.parseBoolean(((Element) characterList.item(0)).getAttribute("isPlayer")) && !flipX) {
                flipX = !flipX;
            }*/

        final NodeList animList = characterData.getElementsByTagName("anim");

        //System.out.println(nList);
        for (int temp = 0; temp < animList.getLength(); temp++) {
            Node nNode = animList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element animData = (Element) nNode;
                String animName = animData.getAttribute("name");
                boolean changed = false;
                String offsetX = animData.getAttribute("x");
                String offsetY = animData.getAttribute("y");
                if (!offsetX.equals("") && !offsetY.equals(""))
                    addAnimation(
                            animName,
                            animData.getAttribute("anim"),
                            parseInt(animData.getAttribute("fps")),
                            parseBoolean(animData.getAttribute("loop")),
                            -parseInt(offsetX),
                            -parseInt(offsetY)
                    );
                else
                    addAnimation(
                            animName,
                            animData.getAttribute("anim"),
                            parseInt(animData.getAttribute("fps")),
                            parseBoolean(animData.getAttribute("loop"))
                    );
            }
        }
        playAnim("idle");


    }
    public FunkinCharacter(String name, double xPos, double yPos) throws IOException, SAXException, ParserConfigurationException {
        super("characters/boyfriend/bf", xPos, yPos);
        //trace(name);
        camOffsetX = 0;
        camOffsetY = 0;
        getCharacterData(name);
        if (!CoolUtil.checkFileExists("images/characters/" + sprite + ".png")) {
            if (CoolUtil.checkFileExists(PlayState.currentFolder + "images/characters/" + sprite)) {
                setImage("characters/" + sprite, PlayState.currentFolder);
            }
        } else {
            setImage("characters/" + sprite);
        }
        //CoolUtil.trace("Loaded character \"$cyan" + name + "$reset\"");
        this.name = name;
    }
}
