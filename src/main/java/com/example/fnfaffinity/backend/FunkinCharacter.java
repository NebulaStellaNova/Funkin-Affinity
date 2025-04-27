package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.novautils.NovaAnimController;
import com.example.fnfaffinity.novautils.NovaAnimSprite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static com.example.fnfaffinity.Main.pathify;
import static com.example.fnfaffinity.backend.CoolUtil.trace;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

public class FunkinCharacter extends NovaAnimSprite {
    public String name = "";
    public String sprite = "";
    public Element characterData;

    public void getCharacterData(String name) {
        final String filepath = pathify("data/characters/" + name + ".xml");
        File file = new File(filepath);
        if (!file.exists()) {
            file = new File(pathify("data/characters/bf.xml"));
        }

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db;
        Document document = null;

        try {
            db = dbf.newDocumentBuilder();
            document = db.parse(file);
            document.getDocumentElement().normalize();
            final NodeList characterList = document.getElementsByTagName("character");
            sprite = ((Element) characterList.item(0)).getAttribute("sprite");
            flipX = Boolean.parseBoolean(((Element) characterList.item(0)).getAttribute("flipX"));
            trace(name);
            trace(flipX);
            /*if (Boolean.parseBoolean(((Element) characterList.item(0)).getAttribute("isPlayer")) && !flipX) {
                flipX = !flipX;
            }*/

            final NodeList animList = document.getElementsByTagName("anim");

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
        } catch (ParserConfigurationException | IOException | SAXException ignore) {
        }


    }
    public FunkinCharacter(String name, double xPos, double yPos) throws IOException, SAXException, ParserConfigurationException {
        super("characters/boyfriend/bf", xPos, yPos);
        //trace(name);
        getCharacterData(name);
        setImage("characters/" + sprite);
        this.name = name;
    }

    public void setFrame(int frame) {
        for (NovaAnimController anim : animations) {
            anim.curFrame = frame;
        }
    }
}
