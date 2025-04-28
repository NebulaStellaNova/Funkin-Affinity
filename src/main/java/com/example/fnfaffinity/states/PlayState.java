package com.example.fnfaffinity.states;

import com.example.fnfaffinity.backend.*;
import com.example.fnfaffinity.backend.FunkinCharacter;
import com.example.fnfaffinity.novautils.*;

import javafx.scene.media.AudioClip;
import org.json.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Objects;

import static com.example.fnfaffinity.backend.CoolUtil.addToArray;
import static com.example.fnfaffinity.backend.CoolUtil.trace;
import static com.example.fnfaffinity.novautils.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novautils.NovaMath.lerp;

public class PlayState extends MusicBeatState {
    public static String song;
    public static double scrollSpeed = 1;

    public static NovaCamera camHUD = new NovaCamera(0, 0);

    public JSONObject songMeta;
    public JSONObject chart;
    public JSONArray events;

    public static Integer camX = 0;
    public static Integer camY = 0;

    public static StrumLine playerStrumline;
    public static StrumLine opponentStrumline;
    public static Note[] notes = {};
    public static SustainNote[] holdNotes = {};
    public static FunkinCharacter[] characters = {};
    public static StrumLine[] strumLines = {};

    public static FunkinCharacter player;
    public static FunkinCharacter opponent;
    public static int playerResetTimer = 0;
    public static int playerHoldTimer = 0;
    public static int opponentResetTimer = 0;

    public static AudioClip voices;
    public static AudioClip inst;

    private long start = 0;
    private long finish = 0;
    public long timeElapsed = 0;

    public boolean leavingState = false;

    public boolean hitNoteOnFrame = false;
    public void update() {
        super.update();

        if (leavingState)
            start = System.currentTimeMillis();
        hitNoteOnFrame = false;
        camGame.y = lerp(camGame.y, camY, getDtFinal(4));
        camGame.x = lerp(camGame.x, camX, getDtFinal(4));

        NovaKey[] keys = {
            NovaKeys.W,
            NovaKeys.F,
            NovaKeys.J,
            NovaKeys.O,
        };
        for (int i = 0; i < 4; i++) {
            Note daNote = null;
            SustainNote daSusNote = null;

            if (keys[i].pressed) {
                boolean pressedNote = false;
                for (SustainNote note : holdNotes) {
                    if (note.alive && note.direction == i && note.strumLine.type == 1 && note.y-75 < note.strumLine.y + 5) {
                        note.destroy();
                        pressedNote = true;
                        if (characters[note.strumLineID].holdTimer == 0) {
                            characters[note.strumLineID].holdTimer = 10;
                            noteHit(true, note.direction, note.type, note.strumLineID);
                        }
                        daSusNote = note;
                    }
                    if (note.alive && note.y-75 < note.strumLine.y -100) {
                        note.destroy();
                    }
                }
                if (pressedNote) {
                    strumLines[daSusNote.strumLineID].members.get(i).playAnim("confirm");
                }
            }
            if (keys[i].justPressed) {
                boolean pressedNote = false;
                for (Note note : notes) {
                    if (note.direction == i && note.strumLine.type == 1 && note.y < note.strumLine.y + 100 && note.y > note.strumLine.y - 100) {
                        if (!hitNoteOnFrame) {
                            note.destroy();
                            noteHit(true, note.direction, note.type, note.strumLineID);
                            pressedNote = true;
                            hitNoteOnFrame = true;
                            daNote = note;
                        }
                    }

                }
                if (pressedNote) {
                    strumLines[daNote.strumLineID].members.get(i).playAnim("confirm");
                } else {
                    for (StrumLine line : strumLines)
                        if (line.type == 1)
                            line.members.get(i).playAnim("pressed");
                }
            } else if (keys[i].justReleased) {
                for (StrumLine line : strumLines)
                    if (line.type == 1)
                        line.members.get(i).playAnim("static");
            }
            for (Note note : notes) {
                if (note.alive && note.y < note.strumLine.y - 100 && note.strumLine.type == 1) {
                    note.destroy();
                    noteMiss(note.direction, note.strumLineID);
                }
            }
            for (Note note : notes) {
                if (note.direction == i && note.alive && note.strumLine.type == 0 && note.time - timeElapsed < 1) {
                    note.destroy();
                    strumLines[note.strumLineID].members.get(i).playAnim("confirm");
                    noteHit(false, note.direction, note.type, note.strumLineID);

                    int finalI = i;
                    new NovaTimer(6, new Runnable() {
                        @Override
                        public void run() {
                            strumLines[note.strumLineID].members.get(finalI).playAnim("static");
                        }});
                    //pressedNote = true;
                }
            }

            for (SustainNote note : holdNotes) {
                if (note.alive && note.direction == i && note.strumLine.type == 0 && note.time - timeElapsed < 1) {
                    note.destroy();
                    if (characters[note.strumLineID].holdTimer == 0) {
                        characters[note.strumLineID].holdTimer = 10;
                        noteHit(true, note.direction, note.type, note.strumLineID);
                    }
                    strumLines[note.strumLineID].members.get(i).playAnim("confirm");
                    int finalI = i;
                    new NovaTimer(6, new Runnable() {
                        @Override
                        public void run() {
                            strumLines[note.strumLineID].members.get(finalI).playAnim("static");
                        }});
                }
            }

        }

        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        for (Note note : notes) {
            if (note.time - timeElapsed > 600/scrollSpeed) {
                note.visible = false;
            } else {
                note.visible = true;
            }
            if (timeElapsed == 0) {
                note.respawn();
            }
            note.y = note.strumLine.y + ((note.time - timeElapsed) * scrollSpeed);
        }

        for (SustainNote note : holdNotes) {
            /*if (note.time - timeElapsed > 600/scrollSpeed) {
                note.visible = false;
            } else {
                note.visible = true;
            }*/
            note.visible = note.y < globalCanvas.getHeight();
            note.y = (note.strumLine.y + ((note.time - timeElapsed) * scrollSpeed)) + 75;
            //note.y = 200;
            //note.visible = true;
        }


        if (NovaKeys.BACK_SPACE.justPressed) {
            inst.stop();
            if (voices != null)
                voices.stop();
            //CoolUtil.playMenuSong();
            leavingState = true;
            switchState(new FreeplayState());
        }


        for (FunkinCharacter character : characters) {
            if (character.holdTimer > 0)
                character.holdTimer--;
        }
        if (playerHoldTimer > 0)
            playerHoldTimer--;

        camGame.zoom = lerp(camGame.zoom, 1, getDtFinal(4));


        if (NovaKeys.UP.justPressed)
            camY += 100;
        if (NovaKeys.DOWN.justPressed)
            camY -= 100;
        if (NovaKeys.LEFT.justPressed)
            camX += 100;
        if (NovaKeys.RIGHT.justPressed)
            camX -= 100;

        if (chart.has("events"))
            for (Object event : events = chart.getJSONArray("events")) {
                JSONObject daEvent = (JSONObject) event;
                JSONArray eventParams = daEvent.getJSONArray("params");
                String eventName = daEvent.getString("name");
                int eventTime = (int) Math.round(daEvent.getDouble("time"));

                if (timeElapsed > eventTime - 10 && timeElapsed < eventTime + 10) {
                    Object param1 = 0;
                    switch (eventName) {
                        case "Camera Movement":
                            param1 = eventParams.getInt(0);
                            FunkinCharacter daCharacter = characters[(int) param1];

                            camX = (int) (daCharacter.x - daCharacter.camOffsetX - daCharacter.frameWidth);
                            camY = (int) (daCharacter.y - daCharacter.camOffsetY);

                            break;
                        case "Play Animation":
                            param1 = eventParams.getInt(0);
                            String param2 = eventParams.getString(1);
                            if ((int) param1 == 0) {
                                opponent.playAnim(param2);
                                opponent.setFrame(0);
                                opponentResetTimer = 500;
                            } else if ((int) param1 == 1) {
                                player.playAnim(param2);
                                player.setFrame(0);
                            }
                            break;
                    }
                }
            }
    }

    public void beat() {
        super.beat();

        if (curBeat % 2 == 0 && playerResetTimer == 0) {
            player.playAnim("idle");
            player.setFrame(0);
            //player.curFrame = 0;
        }
        if (curBeat % 2 == 0 && opponentResetTimer == 0) {
            opponent.playAnim("idle");
            opponent.setFrame(0);
            //player.curFrame = 0;
        }
        if (curBeat % 4 == 0)
            camGame.zoom = 1.05;

        for (FunkinCharacter character : characters) {
            if (curBeat % 2 == 0 && character.resetTimer == 0) {
                character.playAnim("idle");
                character.setFrame(0);
            }
        }
    }
    public void step() {
        super.step();

        for (FunkinCharacter character : characters) {
            if (character.resetTimer > 0)
                character.resetTimer--;
        }
    }

    public void noteHit(boolean isPlayer, int direction, int noteType, int strumLineID) {
        FunkinCharacter daCharacter = characters[strumLineID];
        daCharacter.resetTimer = 8;
        String[] anims;

        if (daCharacter.flipX) {
            anims = new String[]{"singRIGHT", "singDOWN", "singUP", "singLEFT"};
        } else {
            anims = new String[]{"singLEFT", "singDOWN", "singUP", "singRIGHT"};
        }

        if (noteType != 0) {
            switch (chart.getJSONArray("noteTypes").getString(noteType - 1)) {
                case "No Anim Note":
                    trace("Do Nothing LOL");
                    break;
                case "Alt Anim Note":
                    daCharacter.playAnim(anims[direction] + "-alt");
                    daCharacter.setFrame(0);
                    break;
            }
        } else {
            daCharacter.playAnim(anims[direction]);
            daCharacter.setFrame(0);
        }
    }

    public void noteMiss(int direction, int strumLineID) {
        FunkinCharacter daCharacter = characters[strumLineID];
        daCharacter.resetTimer = 8;
        String[] anims = null;
        if (daCharacter.flipX) {
            anims = new String[]{"singRIGHT", "singDOWN", "singUP", "singLEFT"};
        } else {
            anims = new String[]{"singLEFT", "singDOWN", "singUP", "singRIGHT"};
        }
        daCharacter.playAnim(anims[direction] + "miss");
    }

    public void addNote(String skin, int strumTime, int direction, StrumLine strumLine, int strumLineID, int type) {
        Note daNote = new Note(skin, strumTime, direction, strumLine, strumLineID, type);
        daNote.visible = false;
        notes = CoolUtil.addToArray(notes, daNote);
    }
    public void addSustainNote(String skin, int strumTime, int direction, StrumLine strumLine, int strumLineID, boolean isEnd, int type) {
        SustainNote daNote = new SustainNote(skin, strumTime, direction, strumLine, strumLineID, isEnd, type);
        if (!isEnd)
            daNote.setScale(0.75, 0.2);
        //daNote.visible = false;
        holdNotes = CoolUtil.addToArray(holdNotes, daNote);
    }

    public void create() {
        super.create();
        print(song);
        try {
            chart = CoolUtil.parseJson("songs/" + song.toLowerCase() + "/charts/hard.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (chart.has("events"))
            if (!Objects.equals(chart.getJSONArray("events"), new JSONArray())) {
                events = chart.getJSONArray("events");
            }


        JSONArray strumLineArray = chart.getJSONArray("strumLines");
        for (int i = 0; i < strumLineArray.length(); i++) {
            JSONObject obj = (JSONObject) strumLineArray.get(i);
            String name = "";
            while (name == "") {
                name = obj.getJSONArray("characters").getString(0);
            }

            try {
                int strumlineXpos = 0;
                if (obj.getInt("type") == 0)
                    strumlineXpos = 100;
                if (obj.getInt("type") == 1)
                    strumlineXpos = 700;
                //if (obj.getBoolean("visible"))
                strumLines = CoolUtil.addToArray(strumLines, new StrumLine(4, strumlineXpos, 50, camHUD, obj.getInt("type"), obj.getBoolean("visible")));

                FunkinCharacter daCharacter = new FunkinCharacter(name, 500, 0);
                if (obj.getInt("type") == 1)
                    daCharacter.flipX = !daCharacter.flipX;
                characters = CoolUtil.addToArray(characters, daCharacter);
                if (i == 0) {
                    opponent = new FunkinCharacter(name, 600, 100);
                    opponent.visible = false;
                    //opponent.flipX = true;
                } else if (i == 1) {
                    //trace(name);
                    player = new FunkinCharacter(name, 700, 250);
                    player.flipX = !player.flipX;
                    player.visible = false;
                }
                switch (obj.getString("position")) {
                    case "boyfriend":
                        daCharacter.x = player.x;
                        daCharacter.y = player.y;
                        break;
                    case "dad":
                        daCharacter.x = opponent.x;
                        daCharacter.y = opponent.y;
                        break;
                }
            } catch (IOException | SAXException | ParserConfigurationException ignore) {
            }
        }
        for (FunkinCharacter character : characters) {
           add(character);
        }
        //player = new FunkinCharacter( 500, 300);
        add(opponent);
        add(player);

        try {
            songMeta = CoolUtil.parseJson("songs/" + song.toLowerCase() + "/meta.json");
            music.stop();

        } catch (NullPointerException ignored) {

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        startSong(0);

        for (StrumLine strumLine : strumLines) {
            add(strumLine);
        }
        /*try {
            playerStrumline = new StrumLine(4, 700, 50, camHUD);
            opponentStrumline = new StrumLine(4, 100, 50, camHUD);
            add(playerStrumline);
            add(opponentStrumline);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        scrollSpeed = chart.getFloat("scrollSpeed")/2;
        JSONArray leftNotes = chart.getJSONArray("strumLines").getJSONObject(0).getJSONArray("notes");
        JSONArray rightNotes = chart.getJSONArray("strumLines").getJSONObject(1).getJSONArray("notes");

        JSONArray daStrumLines = chart.getJSONArray("strumLines");
        for (int a = 0; a < daStrumLines.length(); a++) {
            JSONObject daStrumLine = daStrumLines.getJSONObject(a);
            for (int i = 0; i < daStrumLine.getJSONArray("notes").length(); i++) {
                JSONObject note = daStrumLine.getJSONArray("notes").getJSONObject(i);
                addNote("default", note.getInt("time"), note.getInt("id"), strumLines[a], a, note.getInt("type"));
                //System.out.println(note.getDouble("time"));
                int sustainLength = (int) Math.round(note.getInt("sLen") / (bpm / 16));
                if (sustainLength > 0)
                    for (int e = 0; e <= sustainLength; e++) {
                        //if (strumLines[a].type == 1)
                            addSustainNote("default", note.getInt("time") + (10 * e), note.getInt("id"), strumLines[a], a, e == sustainLength, note.getInt("type"));
                    }
            }
            /*for (int i = 0; i < leftNotes.length(); i++) {
                JSONObject note = leftNotes.getJSONObject(i);
                addNote("default", note.getInt("time"), note.getInt("id"), opponentStrumline, 0, note.getInt("type"));
                int sustainLength = note.getInt("sLen");
                for (int e = 0; e <= sustainLength; e++) {
                    //addSustainNote("default", note.getInt("time") + (10*e), note.getInt("id"), opponentStrumline, 0, e == sustainLength);
                }
                //System.out.println(note.getDouble("time"));
            }*/
        }
        for (SustainNote note : holdNotes) {
            note.camera = camHUD;
            add(note);
        }
        for (Note note : notes) {
            note.camera = camHUD;
            add(note);
        }
        startSong(1);
        /*try {

            final String filepath = pathify("songs/" + song + "/meta.xml");
            final File file = new File(filepath);
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document document = db.parse(file);
            document.getDocumentElement().normalize();
            final NodeList nList = document.getElementsByTagName("stage");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String stage = eElement.getAttribute("name");
                    print(stage);

                    final String stagePath = pathify("stages/" + stage + ".xml");
                    final File stageFile = new File(stagePath);
                    final DocumentBuilderFactory stagedbf = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder stagedb = stagedbf.newDocumentBuilder();
                    final Document daStage = stagedb.parse(stageFile);
                    daStage.getDocumentElement().normalize();
                    final NodeList cameraNodes = daStage.getElementsByTagName("camera");
                    Node cameraNode = cameraNodes.item(0);
                    Element cameraElement = (Element) cameraNode;
                    camX = Integer.parseInt(cameraElement.getAttribute("offsetX"));
                    camY = Integer.parseInt(cameraElement.getAttribute("offsetY"));
                    camGame.zoom = Float.parseFloat(cameraElement.getAttribute("defZoom"));
                    final NodeList stageAssets = daStage.getElementsByTagName("stage");
                    final NodeList stageSprites = daStage.getElementsByTagName("staticsprite");
                    print(stageSprites.getLength());
                    for (int spriteID = 0; spriteID < stageSprites.getLength(); spriteID++) {
                        Node sprite = stageSprites.item(spriteID);
                        if (sprite.getNodeType() == Node.ELEMENT_NODE) {
                            Element spriteElement = (Element) sprite;
                            print(spriteElement.getAttribute("image"));
                            NovaSprite daSprite;
                            daSprite = new NovaSprite("stages/" + stage + "/" + spriteElement.getAttribute("image"), Integer.parseInt(spriteElement.getAttribute("x"))/globalStage.getWidth(), Integer.parseInt(spriteElement.getAttribute("y"))/globalStage.getHeight());
                            if (!spriteElement.getAttribute("scrollX").isEmpty() && !spriteElement.getAttribute("scrollY").isEmpty()) {
                                daSprite.setScrollFactor(Float.parseFloat(spriteElement.getAttribute("scrollX")), Float.parseFloat(spriteElement.getAttribute("scrollY")));
                            }
                            if (!spriteElement.getAttribute("scaleX").isEmpty() && !spriteElement.getAttribute("scaleY").isEmpty()) {
                                daSprite.setScale(Float.parseFloat(spriteElement.getAttribute("scaleX")), Float.parseFloat(spriteElement.getAttribute("scaleX")));
                            }
                            add(daSprite);
                        }
                    }
                    //String ico = eElement.getAttribute("icon");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        */
    }

    public void startSong(int daVolume) {
        if (inst != null) {
            inst.stop();
        }
        if (voices != null) {
            voices.stop();
        }
        if (daVolume != 0) {
            CoolUtil.playMusic("songs/" + song + "/song/Inst.mp3");
            music.stop();
        }
        inst = CoolUtil.playSound("songs/" + song + "/song/Inst.mp3", daVolume);
        if (songMeta.has("needsVoices")) {
            if (songMeta.getBoolean("needsVoices")) {
                voices = CoolUtil.playSound("songs/" + song + "/song/Voices.mp3", daVolume);
            }
        } else {
            voices = CoolUtil.playSound("songs/" + song + "/song/Voices.mp3", daVolume);
        }
        //if (songMeta.getBoolean("needsVoices"))

        start = System.currentTimeMillis();
        updateBPM(songMeta.getFloat("bpm"));
    }

    public void destroy() {
        super.destroy();
        notes = new Note[] {};
        holdNotes = new SustainNote[] {};
        characters = new FunkinCharacter[] {};
        strumLines = new StrumLine[] {};
    }
}
