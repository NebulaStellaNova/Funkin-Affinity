package com.example.fnfaffinity.states;

import com.example.fnfaffinity.backend.discord.Discord;
import com.example.fnfaffinity.backend.objects.*;
import com.example.fnfaffinity.backend.scripting.Script;
import com.example.fnfaffinity.backend.scripting.ScriptEvents;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.backend.utils.WindowUtil;
import com.example.fnfaffinity.novahandlers.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.text.TextAlignment;
import org.json.*;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Vector;

import static com.example.fnfaffinity.backend.utils.CoolUtil.*;
import static com.example.fnfaffinity.novahandlers.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novahandlers.NovaMath.lerp;

public class PlayState extends MusicBeatState {
    public static String song;
    public static double scrollSpeed = 1;
    public static boolean isStoryMode = false;
    public static String difficulty = "hard";
    public static String curVariation = "";
    public static boolean downScroll = false;
    public static boolean botPlayEnabled = false;

    public static int healthBarWidth = 600;

    // -- Player Stats -- \\
    public static double accuracy = 100;
    public static double health = 1; // out of 2
    public static int score = 0;
    public static int misses = 0;
    // ------------------ \\

    public boolean eventBopEnabled = false;
    public float eventBopIntensity = 0;
    public int eventBopRate = 0;
    public int eventBopStepOffset = 0;

    public static NovaCamera camHUD = new NovaCamera(0, 0);

    public JSONObject songMeta;
    public JSONObject chart;
    public JSONArray events;

    public static Integer camX = 0;
    public static Integer camY = 0;

    public static NovaAnimSprite[] splashes = {};
    public static Note[] notes = {};
    public static SustainNote[] holdNotes = {};
    public static FunkinCharacter[] characters = {};
    public static StrumLine[] strumLines = {};
    public static Script[] scripts = {};
    public static int[] camOffsetsX = {};
    public static int[] camOffsetsY = {};
    public static NovaSpriteGroup ratings = new NovaSpriteGroup();

    public Vector<FunkinCharacter> precachedCharacters = new Vector<FunkinCharacter>(0);
    public FunkinCharacter getPrecachedCharacter(String name) {
        for (FunkinCharacter character : precachedCharacters) {
            trace(character.name);
            if (Objects.equals(character.name, name)) {
                return character;
            }
        }
        return null;
    }

    public static FunkinCharacter player;       // For stage player position
    public static FunkinCharacter spectator;    // For stage spectator position
    public static FunkinCharacter opponent;     // For stage opponent position
    public static double defaultCamZoom = 1;

    public static Vector<Object> vocalTracks = new Vector<Object>(0);
    public static Clip voices;
    public static Clip inst;

    public static long songDuration;

    private long start = 0;
    private long finish = 0;
    public long timeElapsed = 0;

    public boolean paused = false;

    public boolean leavingState = false;
    public boolean songEnded = false;

    public boolean readyUpped = false;

    public Stage stage;

    public NovaSprite readySprite = new NovaSprite("game/countdown/ready", 0, 0);

    public NovaSprite[] introSprites = {
            null,
            new NovaSprite("game/countdown/ready", 0, 0),
            new NovaSprite("game/countdown/set", 0, 0),
            new NovaSprite("game/countdown/go", 0, 0)
    };
    public Clip[] introSounds = {
            getClip("audio/countdown/intro3.wav"),
            getClip("audio/countdown/intro2.wav"),
            getClip("audio/countdown/intro1.wav"),
            getClip("audio/countdown/introGo.wav")
    };
    public int introLength = 5;

    //public JSONObject options;
    //public boolean directionalAudio = true; // Not gonna happen in this engine :<


    // UI Stuff
    public NovaText scoreTxt;
    public NovaGraphic healthBarBG;
    public NovaGraphic healthBarLeft;
    public NovaGraphic healthBarRight;
    public CharacterIcon iconPlayer;
    public CharacterIcon iconOpponent;

    private long clipTimePosition;

    private PauseMenuSubState pauseMenu = new PauseMenuSubState();

    public void resetAudioTracks() {
        JSONArray strumLineArray = chart.getJSONArray("strumLines");
        vocalTracks = new Vector<>(0);
        for (int i = 0; i < strumLineArray.length(); i++) {
            JSONObject obj = (JSONObject) strumLineArray.get(i);
            if (obj.has("vocalsSuffix")) {
                String suffix = obj.getString("vocalsSuffix");
                if (curVariation != "") {
                    String daString = curVariation + "/Voices" + suffix;
                    if (daString.endsWith("-")) {
                        daString = daString.replace("-", "");
                    }

                    vocalTracks.add(CoolUtil.getClip("songs/" + song + "/song/" + daString + ".wav"));

                } else {

                    vocalTracks.add(CoolUtil.getClip("songs/" + song + "/song/Voices" + suffix + ".wav"));
                }
            }
        }
    }

    boolean enableDebug = false;

    public boolean[] hitNoteOnFrames = {false, false, false, false};
    public boolean hitNoteOnFrame = false;
    public boolean prevDownScroll = false;

    public boolean allowPause = true;
    public double targetLeftPan = -1;
    public double targetRightPan = 1;
    public boolean facingRight = false;

    public void update() {
        super.update();
        callInScripts("update");
        /*try {
            //options = CoolUtil.parseJson("data/options");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        for (Object obj : options.getJSONArray("sections")) {
            JSONObject daObj = (JSONObject) obj;
            if (Objects.equals(daObj.getString("title"), "Gameplay"))
                downScroll = daObj.getJSONObject("options").getBoolean("downScroll");
        }

        if (NovaKeys.NUMPAD1.justPressed)
            botPlayEnabled = !botPlayEnabled;

        for (boolean bool : hitNoteOnFrames) {
            bool = false;
        }

        for (NovaAnimSprite splash : splashes) {
            if (splash.getAnimation("idle").curFrame == 3) {
                splash.destroy();
            }
        }

        if (!readyUpped) {
            curBeat = -introLength;
            curStep = (-introLength)*4;
            if (NovaKeys.SPACE.justPressed) {
                readyUpped = true;
            }
        }
        for (NovaSprite sprite : introSprites) {
            if (sprite != null) {
                if (sprite.visible) {
                    sprite.alpha = lerp(sprite.alpha, 0, 0.1);
                } else {
                    sprite.alpha = 1;
                }
            }
        }
        if (curBeat <= -1) {
            start = 0;
        }

        if (!allowPause && NovaKeys.BACK_SPACE.justPressed) {
            for (Object track : vocalTracks) {
                ((AudioClip) track).stop();
            }
            endSong();
        }

        readySprite.visible = !readyUpped;

        readySprite.setScrollFactor(0, 0);
        readySprite.setScale(0.5, 0.5);
        readySprite.x = (globalCanvas.getWidth()/2) - ((readySprite.img.getWidth()*readySprite.scaleX)/2);
        readySprite.y = (globalCanvas.getHeight()/2) - ((readySprite.img.getHeight()*readySprite.scaleY)/2);

        for (Object sprite : ratings.members) {
            if (sprite.getClass() == NovaSprite.class) {
                NovaSprite daSprite = ((NovaSprite) sprite);
                int travel = 50;
                daSprite.y = lerp(daSprite.y, daSprite.defY-travel, 0.1);
                if (daSprite.y < daSprite.defY-(travel/2))
                    daSprite.alpha = lerp(daSprite.alpha, 0, 0.2);
                if (daSprite.alpha == 0) {
                    daSprite.destroy();
                }
            }

            //sprite.y = lerp(sprite.y, sprite.defY-100, 0.1);
            //sprite.alpha = lerp(sprite.alpha, 0, 0.1);
            //sprite.y--;
            /*if (sprite.alpha == 0) {
                sprite.destroy();
            }*/
        }

        for (Object obj : stage.sprites) {
            if (obj.getClass() == StageAnimSprite.class) {
                stage.stageScript.set(((StageAnimSprite) obj).name, obj);
            }
            if (obj.getClass() == StageSprite.class) {
                stage.stageScript.set(((StageSprite) obj).name, obj);
            }
            stage.stageScript.set("characters", characters);
            stage.stageScript.call("update", 0.14);
        }
        if (leavingState)
            start = 0;
        //hitNoteOnFrame = false;
        camGame.y = lerp(camGame.y, camY, getDtFinal(4));
        camGame.x = lerp(camGame.x, camX, getDtFinal(4));

        try {
            NovaKey w = (NovaKey) NovaKeys.class.getDeclaredField("W").get(null);
            //trace(w.justPressed);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (NovaKeys.ENTER.justPressed && curBeat > 0 && allowPause) {
            paused = !paused;
            if (paused) {
                pauseMenu.open();
                clipTimePosition = inst.getMicrosecondPosition();
                inst.stop();
                if (voices != null)
                    voices.stop();
                for (Object track : vocalTracks) {
                    ((Clip) track).stop();
                }
            } else {
                switch (pauseMenu.pauseOptions[pauseMenu.curSelected]) {
                    case "resume":
                        pauseMenu.close();
                        inst.setMicrosecondPosition(clipTimePosition);
                        inst.start();
                        if (voices != null) {
                            voices.setMicrosecondPosition(clipTimePosition);
                            voices.start();
                        }
                        for (Object track : vocalTracks) {
                            ((Clip) track).setMicrosecondPosition(clipTimePosition);
                            ((Clip) track).start();
                        }
                        break;
                    case "restart":
                        pauseMenu.close();
                        switchState(new PlayState());
                        break;
                    case "exit to menu":
                        pauseMenu.close();
                        endSong();
                        break;
                }
            }
        }

        if (inst.isRunning()) {
            timeElapsed = inst.getMicrosecondPosition()/1000;
        }

        for (Note note : notes) {
            note.y = note.strumLine.y + ((note.time - timeElapsed) * scrollSpeed);
            note.visible = note.y < globalCanvas.getHeight();
            if (timeElapsed == 0 && start != 0 && !leavingState) {
                note.respawn();
            }
            if (leavingState) {
                note.destroy();
            }
        }

        NovaKey[][] keys = {
                {},
                {},
                {},
                {}
        };

        JSONArray optionSections = options.getJSONArray("sections");
        for (Object obj : optionSections) {
            JSONObject section = (JSONObject) obj;
            if (Objects.equals(section.getString("title"), "Controls")) {
                JSONArray leftKeys = section.getJSONObject("options").getJSONArray("left");
                JSONArray downKeys = section.getJSONObject("options").getJSONArray("down");
                JSONArray upKeys = section.getJSONObject("options").getJSONArray("up");
                JSONArray rightKeys = section.getJSONObject("options").getJSONArray("right");

                JSONArray[] keyList = {
                        leftKeys,
                        downKeys,
                        upKeys,
                        rightKeys
                };
                for (int i = 0; i < keyList.length; i++) {
                    for (Object key : keyList[i]) {
                        String daKey = (String) key;
                        try {
                            NovaKey keyToAdd = (NovaKey) NovaKeys.class.getDeclaredField(daKey).get(null);
                            keys[i] = CoolUtil.addToArray(keys[i], keyToAdd);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        int holdNoteIndex = 0;
        for (SustainNote note : holdNotes) {
            boolean isJustReleased = false;
            for (NovaKey key : keys[note.direction]) {
                if (key.justReleased) {
                    isJustReleased = true;
                }
            }
            note.visible = note.y < globalCanvas.getHeight() + 100;
            if (curBeat < 1) {
                note.pressed = false;
            }
            if (!note.isEndPiece)
                if (!note.pressed)
                    note.y = ((note.strumLine.y + ((note.time - timeElapsed) * scrollSpeed)) + 75);
                else {
                    note.y = note.strumLine.y + 75;
                    if (isJustReleased && note.isPlayer) {
                        note.destroy();
                    }
                    if (!paused) {
                        if (note.scaleY - scrollSpeed >= 0) {
                            note.scaleY-= scrollSpeed;
                        } else {
                            note.scaleY = lerp(note.scaleY, 0, 0.4);
                        }
                        if (note.scaleY < 0.05) {
                            note.destroy();
                        }
                    }
                }

            else {
                note.y = (holdNotes[holdNoteIndex-1].y+(holdNotes[holdNoteIndex-1].frameHeight*holdNotes[holdNoteIndex-1].scaleY)-2)-20;
                note.alive = holdNotes[holdNoteIndex-1].alive;
            }

            if (leavingState) {
                note.destroy();
            }
            holdNoteIndex++;
        }
        boolean pressedABloodNote = false;
        for (int i = 0; i < 4; i++) {
            Note daNote = null;
            SustainNote daSusNote = null;

            boolean isPressed = false;
            boolean isJustPressed = false;
            boolean isJustReleased = false;
            for (NovaKey key : keys[i]) {
                if (key.justReleased) {
                    isJustReleased = true;
                }
            }
            for (NovaKey key : keys[i]) {
                if (key.justPressed) {
                    isJustPressed = true;
                }
                if (key.pressed) {
                    isPressed = true;
                }
            }
            if (isPressed) {
                boolean pressedNote = false;
                for (SustainNote note : holdNotes) {
                    if (note.alive && note.direction == i && note.strumLine.type == 1 && note.y-50 < note.strumLine.y + 5) {
                        //note.destroy();
                        pressedNote = true;
                        if (characters[note.strumLineID].holdTimer == 0) {
                            characters[note.strumLineID].holdTimer = 10;
                        }
                        noteHit(true, note.direction, note.type, note.strumLineID, note, strumLines[note.strumLineID].members.get(i), characters[note.strumLineID].holdTimer);
                        note.pressed = true;
                        daSusNote = note;
                    }
                    if (note.alive && note.y-50 < note.strumLine.y -100) {
                        note.destroy();
                    }
                }
                if (pressedNote) {
                    //strumLines[daSusNote.strumLineID].members.get(i).playAnim("confirm");
                }
            }
            if (isJustPressed) {
                boolean pressedNote = false;
                for (Note note : notes) {
                    if (note.alive && note.direction == i && note.strumLine.type == 1 && note.y < note.strumLine.y + 200 && note.y > note.strumLine.y - 200) {
                        if (!hitNoteOnFrames[i]) {
                            //note.destroy();
                            double noteHitDistance = Math.abs(note.y - note.strumLine.y);
                            boolean cancelled = noteHit(true, note.direction, note.type, note.strumLineID, note, strumLines[note.strumLineID].members.get(i));
                            if (!cancelled) {
                                newRating(((double) (Math.round((noteHitDistance/2)*100)))/100, note.direction, (Strum) (strumLines[note.strumLineID].members.get(i)));
                            }
                            pressedNote = true;
                            pressedABloodNote = true;
                            hitNoteOnFrames[i] = true;
                            daNote = note;
                        }
                    }

                }
                if (pressedNote) {
                    //.playAnim("confirm");
                } else {
                    for (StrumLine line : strumLines) {
                        if (line.type == 1) {
                            line.members.get(i).playAnim("pressed");
                        }
                    }
                }
            } else if (isJustReleased) {
                for (StrumLine line : strumLines)
                    if (line.type == 1)
                        line.members.get(i).playAnim("static");
                hitNoteOnFrames[i] = false;
            }
            for (Object obj : options.getJSONArray("sections")) {
                JSONObject daObj = (JSONObject) obj;
                if (Objects.equals(daObj.getString("title"), "Gameplay")) {
                    boolean ghostTapping = daObj.getJSONObject("options").getBoolean("ghostTapping");
                    if (!ghostTapping && isJustPressed && !pressedABloodNote && curBeat > 0) {
                        noteMiss(i, 1);
                    }
                    //trace(ghostTapping);
                }
            }
            for (Note note : notes) {
                if (note.alive && note.y < note.strumLine.y - 100 && note.strumLine.type == 1) {
                    note.destroy();
                    noteMiss(note.direction, note.strumLineID);
                }
            }
            for (Note note : notes) {
                if (note.direction == i && note.alive && (note.strumLine.type == 0 || botPlayEnabled) && note.time - timeElapsed < 1) {
                    //note.destroy();
                    //strumLines[note.strumLineID].members.get(i).playAnim("confirm");
                    noteHit(note.strumLine.type == 1, note.direction, note.type, note.strumLineID, note, strumLines[note.strumLineID].members.get(i));
                    if (note.strumLine.type == 1) {
                        double noteHitDistance = Math.abs(note.y - note.strumLine.y);
                        newRating(((double) (Math.round((noteHitDistance/2)*100)))/100, note.direction, (Strum) (strumLines[note.strumLineID].members.get(i)));
                    }

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
                if (note.alive && note.direction == i && (note.strumLine.type == 0 || botPlayEnabled) && note.y-50 < note.strumLine.y + 5) {
                    //note.destroy();
                    if (characters[note.strumLineID].holdTimer == 0) {
                        characters[note.strumLineID].holdTimer = 10;
                    }
                    noteHit(true, note.direction, note.type, note.strumLineID, note,  strumLines[note.strumLineID].members.get(i), characters[note.strumLineID].holdTimer);
                    note.pressed = true;
                    //strumLines[note.strumLineID].members.get(i).playAnim("confirm");
                    int finalI = i;
                    new NovaTimer(6, new Runnable() {
                        @Override
                        public void run() {
                            strumLines[note.strumLineID].members.get(finalI).playAnim("static");
                        }});
                }
            }

        }

        /*for (NovaKey key : keys) {
            key.justPressed = false;
        }*/
        if (NovaKeys.BACK_SPACE.justPressed) {
            //endSong();
        }

        for (Object obj : options.getJSONArray("sections")) {
            JSONObject daObj = (JSONObject) obj;
            if (Objects.equals(daObj.getString("title"), "Gameplay")) {
                botPlayEnabled = daObj.getJSONObject("options").getBoolean("botplay");
                //trace(ghostTapping);
            }
        }

        for (FunkinCharacter character : characters) {
            if (character.holdTimer > 0)
                character.holdTimer--;
        }

        camGame.zoom = lerp(camGame.zoom, defaultCamZoom, getDtFinal(4));


        if (!paused && enableDebug) {
            if (NovaKeys.UP.justPressed)
                camY += 100;
            if (NovaKeys.DOWN.justPressed)
                camY -= 100;
            if (NovaKeys.LEFT.justPressed)
                camX += 100;
            if (NovaKeys.RIGHT.justPressed)
                camX -= 100;
        }

        if (chart.has("events"))
            for (Object event : events) {
                JSONObject daEvent = (JSONObject) event;
                JSONArray eventParams = daEvent.getJSONArray("params");
                String eventName = daEvent.getString("name");
                int eventTime = (int) Math.round(daEvent.getDouble("time"));

                boolean ranEvent = daEvent.has("ran");
                if (timeElapsed > eventTime - 20 && timeElapsed < eventTime + 20) {
                    if (!ranEvent) {
                        daEvent.append("ran", "");
                        runEvent(eventName, eventParams);
                    }
                }
            }
        if (!inst.isRunning() && curBeat > 3 && !paused) {
            endSong();
        }

        for (StrumLine strumLine : strumLines) {
            for (int i = 0; i < strumLine.members.size(); i++) {
                for (Object obj : options.getJSONArray("sections")) {
                    JSONObject daObj = (JSONObject) obj;
                    if (Objects.equals(daObj.getString("title"), "Gameplay"))
                        downScroll = daObj.getJSONObject("options").getBoolean("downScroll");
                }
                NovaAnimSprite strum = (NovaAnimSprite) strumLine.members.toArray()[i];
                strum.x = strumLine.x + (StrumLine.spacing * i);
                //strum.scaleY = 0.75;

                if (downScroll) {
                    strum.y = strumLine.y - strum.getAnimation(strum.curAnim).offsetY;
                    //trace(strum.y);

                    //strum.scaleY = -0.75;
                    //prevDownScroll = downScroll;
                }
            }
        }
        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            note.x = note.strumLine.members.get(note.direction).x;
            if (note.strumLine.visible)
                note.alpha = 1;
            else
                note.alpha = 0;
        }
        stage.stageScript.call("postUpdate", 0.14);
        callInScripts("postUpdate");

        String daAccuracy = "";
        int index = 0;
        for (String chars : (accuracy+"").split("")) {
            if (index < 5)
                daAccuracy+=chars;
            index++;
        }
        String[] scoreParts = {
                "Accuracy: " + daAccuracy + "%",
                "Misses: " + misses,
                "Score: " + score
        };
        index = 0;
        scoreTxt.text = "";
        for (String part : scoreParts) {
            scoreTxt.text += part;
            if (index != scoreParts.length-1) {
                scoreTxt.text += " | ";
            }
            index++;
        }
        if (botPlayEnabled) {
            scoreTxt.text = "BOTPLAY";
        }

        int xOffset = -25;

        healthBarLeft.x = healthBarBG.x + 6;
        healthBarLeft.y = healthBarBG.y + 5;
        healthBarLeft.width = lerp(healthBarLeft.width, ((healthBarWidth-13)/2)*(Math.floor((2-health)*10)/10), 0.1);
        healthBarRight.x = healthBarLeft.x + healthBarLeft.width;
        healthBarRight.width = (healthBarWidth-13)-healthBarLeft.width;
        healthBarLeft.height = 20;
        healthBarRight.height = 20;
        healthBarRight.y = healthBarLeft.y;

        iconPlayer.setScale(
                lerp(iconPlayer.scaleX, 1, 0.2),
                lerp(iconPlayer.scaleY, 1, 0.2)
        );
        iconOpponent.setScale(
                lerp(iconOpponent.scaleX, 1, 0.2),
                lerp(iconOpponent.scaleY, 1, 0.2)
        );

        iconPlayer.x = healthBarLeft.x + healthBarLeft.width + xOffset;
        iconOpponent.x = healthBarLeft.x + healthBarLeft.width - ((iconOpponent.img.getWidth()/iconOpponent.segments)*iconOpponent.scaleX) - xOffset;

        iconPlayer.y = healthBarBG.y + (healthBarBG.height/2) - ((iconPlayer.img.getHeight()*iconPlayer.scaleY)/2);
        iconOpponent.y = healthBarBG.y + (healthBarBG.height/2) - ((iconOpponent.img.getHeight()*iconOpponent.scaleY)/2);

        for (Object object : objects) {
            if (object.getClass() == NovaAnimSprite.class) {
                NovaAnimSprite daObject = (NovaAnimSprite) object;
                if (daObject.camera == camHUD) {
                    if (downScroll) {
                        daObject.y = globalCanvas.getHeight()-daObject.defY-daObject.frameHeight;
                    }
                }
            }
            if (object.getClass() == Strum.class) {
                Strum daObject = (Strum) object;
                if (daObject.camera == camHUD) {
                    if (downScroll) {
                        if (prevDownScroll != downScroll) {
                            daObject.y *= -1;
                            daObject.y += globalCanvas.getHeight();
                            prevDownScroll = downScroll;
                        }
                    }
                }
            }
            if (object.getClass() == NovaText.class) {
                NovaText daObject = (NovaText) object;
                if (downScroll) {
                    daObject.y = daObject.defY*-1;
                    daObject.y += globalCanvas.getHeight();
                }
            }
            if (object.getClass() == NovaGroup.class || object.getClass() == StrumLine.class) {
                NovaGroup daGroup = (NovaGroup) object;
                for (NovaAnimSprite sprite : daGroup.members) {
                    NovaAnimSprite daObject = (NovaAnimSprite) sprite;
                    if (daObject.camera == camHUD) {
                        if (downScroll) {
                            daObject.y = globalCanvas.getHeight() - daObject.defY - daObject.frameHeight;
                        }
                    }
                }
            }
        }
        for (Note note : notes) {
            if (downScroll) {
                note.y = globalCanvas.getHeight() - note.y - note.frameHeight;
            }
        }
        for (SustainNote note : holdNotes) {
            if (downScroll) {
                note.y = globalCanvas.getHeight() - note.y - note.frameHeight;
            }
        }
        for (NovaAnimSprite splash : splashes) {
            if (downScroll) {
                splash.y = splash.defY-(splash.frameHeight/2);
            }
        }

        if (downScroll) {
            scoreTxt.y = 50;
            healthBarBG.y = 100;
        } else {
            healthBarBG.y = healthBarBG.defY;
            scoreTxt.y = healthBarBG.y + 70;
        }
        //trace(curVariation);
        //trace(song);
        if (Objects.equals(song, "Stress") && Objects.equals(curVariation, "pico")) {
            //trace("ya");
            if (Objects.equals(characters[0].name, "tankman")) {
                characters[0].y = opponent.y + 50;
            } else {
                characters[0].y = opponent.y;
            }
        } else
            if (stage.getSprite("speakers") != null)
                ((NovaAnimSprite) stage.getSprite("speakers")).visible = false;

        pauseMenu.update();
        //scoreTxt.text = "Accuracy: " + daAccuracy + "% | Misses: " + misses + " | Score: " + score;

        //camX=100;
    }

    public void runEvent(String eventName, JSONArray eventParams) {
        Object param1 = 0;
        if (!callInScripts("onEvent", ScriptEvents.SongEvent(eventName, eventParams))) return;
        switch (eventName) {
            case "Camera Movement":
                param1 = eventParams.getInt(0);
                FunkinCharacter daCharacter = characters[(int) param1];

                facingRight = (int) param1 == 1;

                if ((int) param1 == 1)
                    camX = (int) (((-daCharacter.x) + daCharacter.camOffsetX) - daCharacter.offsetX);
                else
                    camX = (int) ((daCharacter.x - daCharacter.camOffsetX) - daCharacter.offsetX);
                camY = (int) ((daCharacter.y - daCharacter.camOffsetY) - daCharacter.offsetY);

                camX -= camOffsetsX[(int) param1];
                camY -= camOffsetsY[(int) param1];

                break;
            case "Play Animation":
                param1 = eventParams.getInt(0);
                String param2 = eventParams.getString(1);
                trace(param2);
                characters[(int) param1].playAnim(param2);
                characters[(int) param1].setFrame(0);
                characters[(int) param1].resetTimer = 500;
                break;
            case "Toggle Bop":
                eventBopEnabled = eventParams.getBoolean(0);
                eventBopIntensity = eventParams.getFloat(1);
                eventBopRate = eventParams.getInt(2);
                eventBopStepOffset = eventParams.getInt(3);
                break;
            case "Add Camera Zoom":
                camGame.zoom += eventParams.getFloat(0);
                break;
            case "Change Character":
                trace("ran event");
                int daParam1 = (int) eventParams.getInt(0);
                param2 = eventParams.getString(1);
                FunkinCharacter daCharacterChange = getPrecachedCharacter(param2);
                daCharacterChange.x = characters[daParam1].x;
                daCharacterChange.y = characters[daParam1].y;
                daCharacterChange.flipX = characters[daParam1].flipX;
                characters[daParam1].visible = false;
                daCharacterChange.visible = true;
                characters[daParam1] = daCharacterChange;
                characters[daParam1].playAnim("idle");
                characters[daParam1].setFrame(0);
                trace(characters[daParam1].name);
        }
    }
    public boolean danceSwap = false;
    public void beat() {
        super.beat();

        iconPlayer.setScale(1.3, 1.3);
        iconOpponent.setScale(1.3, 1.3);

        for (NovaSprite sprite : introSprites) {
            if (sprite != null)
                sprite.visible = false;
        }
        switch (curBeat) {
            case -3:
                //introSprites[0].visible = true;
                introSounds[0].setMicrosecondPosition(0);
                introSounds[0].start();
                break;
            case -2:
                introSprites[1].visible = true;
                introSounds[1].setMicrosecondPosition(0);
                introSounds[1].start();
                break;
            case -1:
                introSprites[2].visible = true;
                introSounds[2].setMicrosecondPosition(0);
                introSounds[2].start();
                break;
            case 0:
                introSprites[3].visible = true;
                introSounds[3].setMicrosecondPosition(0);
                introSounds[3].start();
                startSong(1);
                break;
        }

        setInScripts("curBeat", curBeat);
        callInScripts("beatHit");

        if (readyUpped) {
            if (curBeat % 2 == 0) {
                for (Object obj : stage.sprites) {
                    if (obj.getClass() == StageAnimSprite.class) {
                        StageAnimSprite daSprite = (StageAnimSprite) obj;
                        if (Objects.equals(daSprite.type, "onbeat")) {
                            daSprite.playAnim("idle");
                        }
                    }
                }
            }
            if (curBeat % 4 == 0 && !eventBopEnabled) {
                camGame.zoom += .05;
            }

            for (FunkinCharacter character : characters) {
                if (curBeat % 2 == 0 && character.resetTimer == 0) {
                    if (character.getAnimation("danceLeft") != null && character.getAnimation("danceRight") != null) {
                        if (!danceSwap) {
                            character.playAnim("danceRight");
                            character.setFrame(0);
                        } else {
                            character.playAnim("danceLeft");
                            character.setFrame(0);
                        }
                        danceSwap = !danceSwap;
                    } else if (character.getAnimation("idle") != null) {
                        character.playAnim("idle");
                        character.setFrame(0);
                    }

                }
            }
        }
    }
    public void step() {
        super.step();
        setInScripts("curStep", curStep);
        callInScripts("stepHit");
        for (FunkinCharacter character : characters) {
            if (character.resetTimer > 0)
                character.resetTimer--;
        }
        if (eventBopEnabled) {
            if (curStep % eventBopRate == eventBopStepOffset) {
                camGame.zoom += eventBopIntensity;
                camHUD.zoom += eventBopIntensity;
            }
        }
    }

    public void spawnSplash(int direction, Strum strum) {
        String[] colors = {"purple", "blue", "green", "red"};
        String directionColor = colors[direction];
        int which = CoolUtil.randomInt(1, 2);

        NovaAnimSprite splash = new NovaAnimSprite("game/splashes/" + strum.skin + "-" + directionColor, strum.x, strum.y);
        //splash.addAnimation("idle", "note impact " + which + " " + directionColor, 24, false);
        splash.addAnimation("idle", "note impact", 24, false);
        splash.camera = camHUD;
        splash.x = strum.x - (strum.frameWidth/2) + (splash.frameWidth/2);
        splash.y = strum.y - (strum.frameHeight/2) + (splash.frameHeight/2);
        splash.playAnim("idle");
        splash.setFrame(0);
        splashes = addToArray(splashes, splash);
        add(splash);
    }

    public double[] accuracies = {};
    public void newRating(double percent, int direction, Strum strum) {
        NovaSprite ratingSprite = new NovaSprite(player.x-300, player.y + 300);
        ratingSprite.setScale(0.5, 0.5);
        for (Object obj : options.getJSONArray("sections")) {
            JSONObject daObj = (JSONObject) obj;
            if (Objects.equals(daObj.getString("title"), "Gameplay"))
                ratingSprite.visible = daObj.getJSONObject("options").getBoolean("showRating");
        }
        if (percent <= 25) {
            // Perfect
            ratingSprite.setImage("game/ratings/sick");
            spawnSplash(direction, strum);
            score += 300;
        } else if (percent <= 50) {
            // Good
            ratingSprite.setImage("game/ratings/good");
            score += 200;
        } else if (percent <= 75) {
            // Bad
            ratingSprite.setImage("game/ratings/bad");
            score += 100;
        } else if (percent <= 100) {
            // Shit
            ratingSprite.setImage("game/ratings/shit");
            score += 50;
        }
        accuracies = addToArray(accuracies, percent);
        accuracy = 0;
        for (double acc : accuracies) {
            accuracy += acc;
        }
        accuracy /= accuracies.length-1;
        accuracy = (double) Math.round(accuracy * 100) /100;
        accuracy = 100 - accuracy;
        ratings.addToGroup(ratingSprite);
    }

    public void reEnableVocals(int strumLineID) {
        if (curBeat <= 0) return;
        //for (Object track : vocalTracks) {
            if (vocalTracks.size() >= strumLineID) {
                Clip daTrack = (Clip) vocalTracks.get(strumLineID);
                if (!daTrack.isRunning()) {
                    daTrack.setMicrosecondPosition(inst.getMicrosecondPosition());
                    daTrack.start();
                }
            }
        //}
    }

    public boolean noteHit(boolean isPlayer, int direction, int noteType, int strumLineID, Note note, NovaAnimSprite strum) {
        FunkinCharacter daCharacter = characters[strumLineID];
        daCharacter.resetTimer = 8;
        String[] anims;

        if (daCharacter.flipX) {
            anims = new String[]{"singRIGHT", "singDOWN", "singUP", "singLEFT"};
        } else {
            anims = new String[]{"singLEFT", "singDOWN", "singUP", "singRIGHT"};
        }
        String noteTypeName = "";
        if (!chart.getJSONArray("noteTypes").isEmpty())
            noteTypeName = chart.getJSONArray("noteTypes").getString(noteType);

        if (paused) return true;
        if (!callInScripts("onNoteHit", ScriptEvents.NoteHitEvent(
                direction,
                noteTypeName,
                noteType,
                strumLineID,
                false
        ))) return true;

        reEnableVocals(strumLineID);
        if (isPlayer)
            if (health + 0.05 <= 2) {
                health += 0.05;
            }

        note.destroy();
        strum.playAnim("confirm");
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
        return false;
    }
    public void noteHit(boolean isPlayer, int direction, int noteType, int strumLineID, SustainNote note, NovaAnimSprite strum, int holdTimer) {
        FunkinCharacter daCharacter = characters[strumLineID];
        daCharacter.resetTimer = 8;
        String[] anims;



        if (daCharacter.flipX) {
            anims = new String[]{"singRIGHT", "singDOWN", "singUP", "singLEFT"};
        } else {
            anims = new String[]{"singLEFT", "singDOWN", "singUP", "singRIGHT"};
        }
        String noteTypeName = "";
        if (!chart.getJSONArray("noteTypes").isEmpty())
            noteTypeName = chart.getJSONArray("noteTypes").getString(noteType);

        if (paused) return;
        if (!callInScripts("onNoteHit", ScriptEvents.NoteHitEvent(
                direction,
                noteTypeName,
                noteType,
                strumLineID,
                true
        ))) return;

        reEnableVocals(strumLineID);
        //note.destroy();
        strum.playAnim("confirm");
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
        } else if (holdTimer == 0){
            daCharacter.playAnim(anims[direction]);
            daCharacter.setFrame(0);
        }
    }

    public void noteMiss(int direction, int strumLineID) {
        FunkinCharacter daCharacter = characters[strumLineID];
        if (!callInScripts("onNoteMiss", ScriptEvents.NoteHitEvent(direction, strumLineID))) return;
        if (vocalTracks.size() >= strumLineID) {
            Clip daTrack = (Clip)vocalTracks.get(strumLineID);
            if (daTrack.isRunning()) {
                daTrack.stop();
            }
            Clip missSound = CoolUtil.getClip("audio/miss/missnote" + CoolUtil.randomInt(1,3));
            missSound.start();
        }
        daCharacter.resetTimer = 8;
        String[] anims = null;
        if (daCharacter.flipX) {
            anims = new String[]{"singRIGHT", "singDOWN", "singUP", "singLEFT"};
        } else {
            anims = new String[]{"singLEFT", "singDOWN", "singUP", "singRIGHT"};
        }
        daCharacter.playAnim(anims[direction] + "miss");
        misses++;
        if (health - 0.05 >= 0) {
            health -= 0.05;
        }
    }

    public void addNote(String skin, int strumTime, int direction, StrumLine strumLine, int strumLineID, int type) {
        Note daNote = new Note(skin, strumTime, direction, strumLine, strumLineID, type);
        daNote.visible = false;
        notes = CoolUtil.addToArray(notes, daNote);
    }
    public void addSustainNote(String skin, int strumTime, int direction, StrumLine strumLine, int strumLineID, boolean isEnd, int type, double len) {
        SustainNote daNote = new SustainNote(skin, strumTime, direction, strumLine, strumLineID, isEnd, type, len, strumLine.type == 1);
        if (!isEnd) {
            daNote.setScale(0.75, len);
            daNote.defScaleY = len;
        }
        daNote.visible = false;
        //daNote.visible = false;
        holdNotes = CoolUtil.addToArray(holdNotes, daNote);
    }

    public void callInScripts(String name) {
        for (Script script : scripts) {
            script.call(name);
        }
    }
    public boolean callInScripts(String name, JSONObject params) {
        var eventCancelled = false;
        for (Script script : scripts) {
            if (!script.call(name, (JSONObject) params))
                eventCancelled = true;
        }
        return eventCancelled;
    }
    public void setInScripts(String name, Object param) {
        for (Script script : scripts) {
            script.set(name, param);
        }
    }

    public void create() {
        super.create();

        Discord.setDescription("Loading song...");

        print(song);
        for (String scriptName : CoolUtil.listFilesInDirectory("songs", ".js")) {
            Script daScript = new Script("songs/" + scriptName);
            daScript.call("create");
            scripts = addToArray(scripts, daScript);
        }
        try {
            String daFolder = difficulty;
            if (!Objects.equals(curVariation, "")) {
                daFolder = curVariation + "/" + difficulty.replace(curVariation+"-", "");
            }
            //trace(difficulty + "printing now");
            chart = CoolUtil.parseJson("songs/" + song.toLowerCase() + "/charts/" + daFolder + ".json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (chart.has("stage")) {
            stage = new Stage(chart.getString("stage"));
        } else {
            stage = new Stage("stage");
        }
        camX = stage.startCamPosX - (1280/4);
        camY = stage.startCamPosY - (720/2);
        for (Object obj : stage.sprites) {
            //trace(obj);
            if (obj.getClass() == StageAnimSprite.class) {
                add((StageAnimSprite) obj);
                stage.stageScript.set(((StageAnimSprite) obj).name, obj);
            }
            if (obj.getClass() == StageSprite.class) {
                add((StageSprite) obj);
                stage.stageScript.set(((StageSprite) obj).name, obj);
            }
        }
        //defaultCamZoom = stage.zoom;
        if (chart.has("events"))
            if (!Objects.equals(chart.getJSONArray("events"), new JSONArray())) {
                events = chart.getJSONArray("events");
                for (Object event : events) {
                    JSONObject daEvent = (JSONObject) event;
                    //daEvent.append("run", "false");
                }
            }

        JSONArray strumLineArray = chart.getJSONArray("strumLines");
        for (int i = 0; i < strumLineArray.length(); i++) {
            JSONObject obj = (JSONObject) strumLineArray.get(i);
            String name = "";
            while (name == "") {
                name = obj.getJSONArray("characters").getString(0);
            }
            try {
                options = CoolUtil.parseJson("data/options");
            } catch (IOException ignore) {}


            try {
                int strumlineXpos = 0;
                if (obj.getInt("type") == 0)
                    strumlineXpos = 100;
                if (obj.getInt("type") == 1)
                    strumlineXpos = 700;
                //if (obj.getBoolean("visible"))
                boolean isVisible = true;
                if (obj.has("visible"))
                    isVisible = obj.getBoolean("visible");
                strumLines = CoolUtil.addToArray(strumLines, new StrumLine(4, strumlineXpos, 50, camHUD, obj.getInt("type"), isVisible));

                FunkinCharacter daCharacter = new FunkinCharacter(name, 500, 0);
                if (i == 0) {
                    iconOpponent = new CharacterIcon(daCharacter.icon, 500, 500);
                    healthBarLeft = new NovaSprite((globalCanvas.getWidth()/2)-((healthBarWidth-3)/2), 577).makeGraphic((healthBarWidth-3), 25, daCharacter.color);
                } else if (i == 1) {
                    trace(daCharacter.icon);
                    iconPlayer = new CharacterIcon(daCharacter.icon, 600, 500);
                    healthBarRight = new NovaSprite((globalCanvas.getWidth()/2)-((healthBarWidth-3)/2), 577).makeGraphic((healthBarWidth-3), 25, daCharacter.color);
                }

                switch (obj.getInt("type")) {
                    case 0:
                        daCharacter.isOpponent = true;
                        int daX = 0;
                        int daY = 0;
                        String daXString = CoolUtil.getXMLAttribute(stage.stageXML, "dad").getAttribute("camOffsetX");
                        String daYString = CoolUtil.getXMLAttribute(stage.stageXML, "dad").getAttribute("camOffsetY");
                        if (!daXString.isEmpty())  {
                            daX = Integer.parseInt(daXString);
                        }
                        if (!daYString.isEmpty()) {
                            daY = Integer.parseInt(daYString);
                        }
                        camOffsetsX = CoolUtil.addToArray(camOffsetsX, daX);
                        camOffsetsY = CoolUtil.addToArray(camOffsetsY, daY);
                        break;
                    case 1:
                        daCharacter.isPlayer = true;
                        int daX2 = 0;
                        int daY2 = 0;
                        String daXString2 = CoolUtil.getXMLAttribute(stage.stageXML, "boyfriend").getAttribute("camOffsetX");
                        String daYString2 = CoolUtil.getXMLAttribute(stage.stageXML, "boyfriend").getAttribute("camOffsetY");
                        if (!daXString2.isEmpty())  {
                            daX2 = Integer.parseInt(daXString2);
                        }
                        if (!daYString2.isEmpty()) {
                            daY2 = Integer.parseInt(daYString2);
                        }
                        camOffsetsX = CoolUtil.addToArray(camOffsetsX, daX2);
                        camOffsetsY = CoolUtil.addToArray(camOffsetsY, daY2);
                        break;
                    case 2:
                        daCharacter.isSpectator = true;
                        int daX3 = 0;
                        int daY3 = 0;
                        String daXString3 = CoolUtil.getXMLAttribute(stage.stageXML, "girlfriend").getAttribute("camOffsetX");
                        String daYString3 = CoolUtil.getXMLAttribute(stage.stageXML, "girlfriend").getAttribute("camOffsetY");
                        if (!daXString3.isEmpty())  {
                            daX3 = Integer.parseInt(daXString3);
                        }
                        if (!daYString3.isEmpty()) {
                            daY3 = Integer.parseInt(daYString3);
                        }
                        camOffsetsX = CoolUtil.addToArray(camOffsetsX, daX3);
                        camOffsetsY = CoolUtil.addToArray(camOffsetsY, daY3);
                }
                if (obj.getInt("type") == 1)
                    daCharacter.flipX = !daCharacter.flipX;
                characters = CoolUtil.addToArray(characters, daCharacter);
                if (i == 0) {
                    String oppX = CoolUtil.getXMLAttribute(stage.stageXML, "dad").getAttribute("x");
                    String oppY = CoolUtil.getXMLAttribute(stage.stageXML, "dad").getAttribute("y");
                    if (oppX.isEmpty())
                        oppX = "0";
                    if (oppY.isEmpty())
                        oppY = "0";

                    opponent = new FunkinCharacter(name,
                        Double.parseDouble(oppX),
                        Double.parseDouble(oppY)
                    );
                    opponent.visible = false;
                    //opponent.flipX = true;
                } else if (i == 1) {
                    //trace(name);
                    String playX = CoolUtil.getXMLAttribute(stage.stageXML, "boyfriend").getAttribute("x");
                    String playY = CoolUtil.getXMLAttribute(stage.stageXML, "boyfriend").getAttribute("y");
                    if (playX.isEmpty())
                        playX = "0";
                    if (playY.isEmpty())
                        playY = "0";
                    player = new FunkinCharacter(name,
                            Double.parseDouble(playX),
                            Double.parseDouble(playY)
                    );
                    //player = new FunkinCharacter(name, 700, 250);
                    player.flipX = !player.flipX;
                    player.visible = false;
                } else if (i == 2) {
                    //trace(name);
                    String playX = CoolUtil.getXMLAttribute(stage.stageXML, "girlfriend").getAttribute("x");
                    String playY = CoolUtil.getXMLAttribute(stage.stageXML, "girlfriend").getAttribute("y");
                    if (playX.isEmpty())
                        playX = "0";
                    if (playY.isEmpty())
                        playY = "0";
                    spectator = new FunkinCharacter(name,
                            Double.parseDouble(playX),
                            Double.parseDouble(playY)
                    );
                    spectator.visible = false;
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
                    case "girlfriend":
                        if (spectator == null) {
                            daCharacter.x = opponent.x;
                            daCharacter.y = opponent.y;
                        } else {
                            daCharacter.x = spectator.x;
                            daCharacter.y = spectator.y;
                        }
                        break;
                }
                if (daCharacter.flipX) {
                    daCharacter.x += (daCharacter.frameWidth);
                }
                daCharacter.x += daCharacter.offsetX;
                daCharacter.y += daCharacter.offsetY;
            } catch (IOException | SAXException | ParserConfigurationException ignore) {
            }
        }

        if (chart.has("events")) {
            for (Object event : events) {
                JSONObject daEvent = (JSONObject) event;
                JSONArray eventParams = daEvent.getJSONArray("params");
                String eventName = daEvent.getString("name");
                if (Objects.equals(eventName, "Change Character")) {
                    trace(eventParams.getString(1));
                    try {
                        FunkinCharacter daCharacter = new FunkinCharacter(eventParams.getString(1), 0, 0);
                        if (eventParams.getInt(0) == 0) {
                            daCharacter.isOpponent = true;
                        } else if (eventParams.getInt(0) == 1) {
                            daCharacter.isPlayer = true;
                        } else if (eventParams.getInt(0) == 2) {
                            daCharacter.isSpectator = true;
                        }
                        daCharacter.visible = false;
                        precachedCharacters.add(daCharacter);
                        //add(daCharacter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    } catch (ParserConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        for (FunkinCharacter character : characters) {
            if (character.isSpectator) {
                //character.visible = false;
                if (character.getAnimation("danceLeft") != null) {
                    character.playAnim("danceLeft");
                } else {
                    character.playAnim("idle");
                }
                add(character);
            }
        }
        add(ratings);
        for (FunkinCharacter character : precachedCharacters) {
            if (character.isPlayer)
                add(character);
        }
        for (FunkinCharacter character : characters) {
            if (character.isPlayer)
                add(character);
        }
        for (FunkinCharacter character : precachedCharacters) {
            if (character.isOpponent)
                add(character);
        }
        for (FunkinCharacter character : characters) {
            if (character.isOpponent)
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

        scoreTxt = new NovaText("test", (int) (globalCanvas.getWidth()/2), 500);
        scoreTxt.camera = camHUD;
        scoreTxt.borderSize = 5;
        scoreTxt.size = 30;
        scoreTxt.alignment = TextAlignment.CENTER;
        scoreTxt.y = globalCanvas.getHeight() - 50;

        healthBarBG = new NovaSprite((globalCanvas.getWidth()/2)-(healthBarWidth/2), 615).makeGraphic(healthBarWidth, 30, "#000000");
        healthBarBG.camera = camHUD;

        healthBarLeft.camera = camHUD;

        healthBarRight.camera = camHUD;

        iconOpponent.camera = camHUD;

        iconPlayer.flipX = true;
        iconPlayer.camera = camHUD;

        add(scoreTxt);
        add(healthBarBG);
        add(healthBarLeft);
        add(healthBarRight);
        add(iconOpponent);
        add(iconPlayer);

        for (StrumLine strumLine : strumLines) {
            add(strumLine);
        }



        scrollSpeed = chart.getFloat("scrollSpeed")/2;
        JSONArray leftNotes = chart.getJSONArray("strumLines").getJSONObject(0).getJSONArray("notes");
        JSONArray rightNotes = chart.getJSONArray("strumLines").getJSONObject(1).getJSONArray("notes");

        JSONArray daStrumLines = chart.getJSONArray("strumLines");
        for (int a = 0; a < daStrumLines.length(); a++) {
            JSONObject daStrumLine = daStrumLines.getJSONObject(a);
            if (daStrumLine.has("notes"))
                for (int i = 0; i < daStrumLine.getJSONArray("notes").length(); i++) {
                    JSONObject note = daStrumLine.getJSONArray("notes").getJSONObject(i);
                    int noteType = 0;
                    if (note.has("noteType")) {
                        noteType = note.getInt("type");
                    }
                    addNote("default", note.getInt("time"), note.getInt("id"), strumLines[a], a, noteType);
                    //System.out.println(note.getDouble("time"));
                    int sustainLength = 0;
                    if (note.has("sLen"))
                        sustainLength = (int) Math.round(note.getInt("sLen") / (bpm / 3)); // def = 16
                    if (sustainLength > 0)
                        for (int e = 0; e <= sustainLength; e++) {
                            //if (strumLines[a].type == 1)
                                addSustainNote("default", note.getInt("time") + (10 * e), note.getInt("id"), strumLines[a], a, e == sustainLength, noteType, note.getInt("sLen") / bpm );
                        }
                }
        }
        for (SustainNote note : holdNotes) {
            note.camera = camHUD;
            add(note);
        }
        for (Note note : notes) {
            note.camera = camHUD;
            add(note);
        }
        callInScripts("postCreate");
        startSong(0);

        for (NovaSprite sprite : introSprites) {
            if (sprite != null) {
                sprite.setScrollFactor(0, 0);
                sprite.setScale(0.5, 0.5);
                sprite.x = (globalCanvas.getWidth()/2) - ((sprite.img.getWidth()*sprite.scaleX)/2);
                sprite.y = (globalCanvas.getHeight()/2) - ((sprite.img.getHeight()*sprite.scaleY)/2);
                sprite.visible = false;
                add(sprite);
            }
        }



        add(readySprite);

        pauseMenu.create();

        String trueDifficulty = difficulty;
        if (songMeta.has("variations")) {
            for (Object variation : songMeta.getJSONArray("variations")) {
                JSONObject daVariation = (JSONObject) variation;
                if (Objects.equals(curVariation, daVariation.getString("name"))) {
                    trueDifficulty = trueDifficulty.replace(curVariation + "-", "");
                }
            }
        }
        if (checkFileExists("images/"+iconOpponent.path.replace("icons", "window") + "/" + trueDifficulty + ".png"))
            WindowUtil.setWindowIcon(iconOpponent.path.replace("icons", "window") + "/" + trueDifficulty);
        //WindowUtil.setWindowIcon(combinedImage);
        //WindowUtil.setWindowIcon(1, difficultyImage);

        if (songMeta.has("variations")) {
            switch (curVariation) {
                case "pico":
                    Discord.setDescription("Playing Song: " + song + " (Pico Mix)", "Difficulty: " + difficulty.toUpperCase().replace("PICO-", ""));
                    break;
                default:
                    Discord.setDescription("Playing Song: " + song, "Difficulty: " + difficulty.replace(curVariation + "-", "").toUpperCase());
            }
        } else {
            Discord.setDescription("Playing Song: " + song, "Difficulty: " + difficulty.toUpperCase());
        }
    }

    public void setVoices(float daVolume) {
        if (curVariation != "") {
            String daString = curVariation + "/Voices";
            if (daString.endsWith("-")) {
                daString = daString.replace("-", "");
            }
            voices = CoolUtil.getClip("songs/" + song + "/song/" + daString + ".wav");
        } else {

            voices = CoolUtil.getClip("songs/" + song + "/song/Voices.wav");
        }
        if (daVolume != 0) {
            voices.start();
        }
    }
    public void startSong(int daVolume) {
        if (inst != null) {
            inst.stop();
        }
        if (voices != null) {
            voices.stop();
        }
        if (daVolume != 0) {
            //CoolUtil.playMusic("songs/" + song + "/song/Inst.mp3");
            music.stop();
        }
        if (curVariation != "") {
            String daString = curVariation + "/Inst";
            if (daString.endsWith("-")) {
                daString = daString.replace("-", "");
            }
            songDuration = CoolUtil.getWAVduration("songs/" + song + "/song/" + daString + ".wav");
            inst = CoolUtil.getClip("songs/" + song + "/song/" + daString + ".wav");
        } else {
            songDuration = CoolUtil.getWAVduration("songs/" + song + "/song/Inst.wav");
            inst = CoolUtil.getClip("songs/" + song + "/song/Inst.wav");
        }
        if (daVolume != 0) {
            inst.start();
        }
        resetAudioTracks();
        if (songMeta.has("needsVoices")) {
            if (songMeta.getBoolean("needsVoices")) {
                setVoices(daVolume);
            }
        } else {
            setVoices(daVolume);
        }

        if (daVolume != 0) {
            for (Object track : vocalTracks) {
                if (allowPause) {
                    ((Clip) track).setMicrosecondPosition(0);
                    ((Clip) track).start();
                } else {
                    ((AudioClip) track).play();
                }
            }
        }
        //if (songMeta.getBoolean("needsVoices"))

        start = System.currentTimeMillis();

        if (songMeta.has("variations")) {
            for (Object variation : songMeta.getJSONArray("variations")) {
                JSONObject daVariation = (JSONObject) variation;
                if (Objects.equals(curVariation, daVariation.getString("name"))) {
                    trace(daVariation.getString("name"));
                    trace(daVariation.getFloat("bpm"));
                    updateBPM(daVariation.getFloat("bpm"));
                }
            }
        } else {
            updateBPM(songMeta.getFloat("bpm"));
        }
    }

    public void endSong() {
        if (songEnded) return;
        songEnded = true;

        inst.stop();
        if (voices != null)
            voices.stop();

        leavingState = true;
        if (!isStoryMode)
            switchState(new FreeplayState());
        else
            switchState(new StoryMenuState());
    }

    public void destroy() {
        super.destroy();
        notes = new Note[] {};
        holdNotes = new SustainNote[] {};
        characters = new FunkinCharacter[] {};
        strumLines = new StrumLine[] {};
        WindowUtil.setWindowIcon(WindowUtil.defaultImage);
        score = 0;
        misses = 0;
        accuracy = 100;
        health = 1;
    }
}
