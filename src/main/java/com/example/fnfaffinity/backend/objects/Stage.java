package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.scripting.Script;
import com.example.fnfaffinity.states.PlayState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;
import java.util.Vector;

import static java.lang.Integer.parseInt;

public class Stage {
    public Document stageXML;
    public Vector<Object> sprites;

    public String name;
    public String stageFolder;
    public int startCamPosX;
    public int startCamPosY;
    public double zoom;

    public Script stageScript;

    public Stage(String stage) {
        sprites = new Vector<Object>(0);
        stageXML = CoolUtil.parseXML(PlayState.currentFolder + "data/stages/" + stage, "data/stages/stage");

        Element stageData = CoolUtil.getXMLAttribute(stageXML, "stage");

        name = stageData.getAttribute("name");
        stageFolder = stageData.getAttribute("folder");
        String startXAtt = stageData.getAttribute("startCamPosX");
        String startYAtt = stageData.getAttribute("startCamPosY");
        if (!startXAtt.isEmpty())
            startCamPosX = Integer.parseInt(startXAtt);
        if (!startYAtt.isEmpty())
            startCamPosY = Integer.parseInt(startYAtt);
        zoom = Double.parseDouble(stageData.getAttribute("zoom"));

        NodeList spriteList = stageXML.getElementsByTagName("sprite");

        for (int spriteID = 0; spriteID < spriteList.getLength(); spriteID++) {
            Node sprite = spriteList.item(spriteID);
            if (sprite.getNodeType() == Node.ELEMENT_NODE) {
                Element spriteElement = (Element) sprite;

                String spriteName = spriteElement.getAttribute("name");

                String spriteX = "0";
                String spriteY = "0";
                String spriteScroll = "1";
                String spriteType = "loop";

                if (spriteElement.hasAttribute("x"))
                    spriteX = spriteElement.getAttribute("x");
                if (spriteElement.hasAttribute("y"))
                    spriteY = spriteElement.getAttribute("y");
                if (spriteElement.hasAttribute("scroll"))
                    spriteScroll = spriteElement.getAttribute("scroll");
                if (spriteElement.hasAttribute("type"))
                    spriteType = spriteElement.getAttribute("type");

                String spritePath = spriteElement.getAttribute("sprite");

                String xmlPath = PlayState.currentFolder + "images/" + stageFolder + spritePath + ".xml";

                if (CoolUtil.checkFileExists(xmlPath)) {
                    StageAnimSprite daSprite = new StageAnimSprite(spriteName, stageFolder + spritePath, Integer.parseInt(spriteX), Integer.parseInt(spriteY), spriteType);
                    Document animationFile = CoolUtil.parseXML(xmlPath);
                    if (animationFile != null) {
                        Element daAnim = (Element) animationFile.getElementsByTagName("SubTexture").item(0);
                        boolean spriteLoop = false;
                        if (spriteType.equals("loop")) {
                            spriteLoop = true;
                        }
                        daSprite.addAnimation("idle", daAnim.getAttribute("name").replace("0000", ""), 24, spriteLoop);
                        daSprite.playAnim("idle");
                    }
                    daSprite.setScrollFactor(Double.parseDouble(spriteScroll), Double.parseDouble(spriteScroll));
                    sprites.add(daSprite);
                } else {
                    //CoolUtil.trace(spriteName);
                    StageSprite daSprite = new StageSprite(spriteName, stageFolder + spritePath, Integer.parseInt(spriteX), Integer.parseInt(spriteY));
                    daSprite.setScrollFactor(Double.parseDouble(spriteScroll), Double.parseDouble(spriteScroll));
                    sprites.add(daSprite);
                    //CoolUtil.trace("this is NOT animated");
                }
            }
        }

        stageScript = new Script("data/stages/" + stage);
        stageScript.call("create");
    }

    public Object getSprite(String name) {
        for (Object sprite : sprites) {
            if (sprite.getClass() == StageSprite.class) {
                if (Objects.equals(((StageSprite) sprite).name, name)) {
                    return sprite;
                }
            }
            if (sprite.getClass() == StageAnimSprite.class) {
                if (Objects.equals(((StageAnimSprite) sprite).name, name)) {
                    return sprite;
                }
            }

        }
        return null;
    }
}
