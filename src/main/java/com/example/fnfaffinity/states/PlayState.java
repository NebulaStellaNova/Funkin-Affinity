package com.example.fnfaffinity.states;

import com.example.fnfaffinity.backend.discord.Discord;
import com.example.fnfaffinity.backend.objects.*;
import com.example.fnfaffinity.backend.scripting.Script;
import com.example.fnfaffinity.backend.scripting.ScriptEvents;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.*;

import javafx.scene.media.AudioClip;
import org.json.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Objects;

import static com.example.fnfaffinity.backend.utils.CoolUtil.*;
import static com.example.fnfaffinity.novahandlers.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novahandlers.NovaMath.lerp;

public class PlayState extends MusicBeatState {
    public static String song;
    public static double scrollSpeed = 1;
    public static boolean isStoryMode = false;
    public static String difficulty = "hard";
    public static String curVariation = "";

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

    public static FunkinCharacter player;
    public static FunkinCharacter spectator;
    public static FunkinCharacter opponent;
    public static int playerResetTimer = 0;
    public static int playerHoldTimer = 0;
    public static int opponentResetTimer = 0;
    public static double defaultCamZoom = 1;

    public static AudioClip voices;
    public static AudioClip inst;

    public static long songDuration;

    private long start = 0;
    private long finish = 0;
    public long timeElapsed = 0;

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
    public AudioClip[] introSounds = {
            getSound("audio/countdown/intro3.mp3"),
            getSound("audio/countdown/intro2.mp3"),
            getSound("audio/countdown/intro1.mp3"),
            getSound("audio/countdown/introGo.mp3")
    };
    public int introLength = 5;

    public boolean hitNoteOnFrame = false;
    public void update() {
        super.update();
        callInScripts("update");

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
            start = System.currentTimeMillis();
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
        hitNoteOnFrame = false;
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
                        //note.destroy();
                        pressedNote = true;
                        if (characters[note.strumLineID].holdTimer == 0) {
                            characters[note.strumLineID].holdTimer = 10;
                        }
                        noteHit(true, note.direction, note.type, note.strumLineID, note, strumLines[note.strumLineID].members.get(i), characters[note.strumLineID].holdTimer);
                        daSusNote = note;
                    }
                    if (note.alive && note.y-75 < note.strumLine.y -100) {
                        note.destroy();
                    }
                }
                if (pressedNote) {
                    //strumLines[daSusNote.strumLineID].members.get(i).playAnim("confirm");
                }
            }
            if (keys[i].justPressed) {
                boolean pressedNote = false;
                for (Note note : notes) {
                    if (note.alive && note.direction == i && note.strumLine.type == 1 && note.y < note.strumLine.y + 200 && note.y > note.strumLine.y - 200) {
                        if (!hitNoteOnFrame) {
                            //note.destroy();
                            double noteHitDistance = Math.abs(note.y - note.strumLine.y);
                            boolean cancelled = noteHit(true, note.direction, note.type, note.strumLineID, note, strumLines[note.strumLineID].members.get(i));
                            if (!cancelled) {
                                newRating(Math.round(noteHitDistance/2), note.direction, (Strum) (strumLines[note.strumLineID].members.get(i)));
                            }
                            pressedNote = true;
                            hitNoteOnFrame = true;
                            daNote = note;
                        }
                    }

                }
                if (pressedNote) {
                    //.playAnim("confirm");
                } else {
                    for (StrumLine line : strumLines)
                        if (line.type == 1)
                            line.members.get(i).playAnim("pressed");
                }
            } else if (keys[i].justReleased) {
                for (StrumLine line : strumLines)
                    if (line.type == 1)
                        line.members.get(i).playAnim("static");
                hitNoteOnFrame = false;
            }
            for (Note note : notes) {
                if (note.alive && note.y < note.strumLine.y - 100 && note.strumLine.type == 1) {
                    note.destroy();
                    noteMiss(note.direction, note.strumLineID);
                }
            }
            for (Note note : notes) {
                if (note.direction == i && note.alive && note.strumLine.type == 0 && note.time - timeElapsed < 1) {
                    //note.destroy();
                    //strumLines[note.strumLineID].members.get(i).playAnim("confirm");
                    noteHit(false, note.direction, note.type, note.strumLineID, note, strumLines[note.strumLineID].members.get(i));

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
                    //note.destroy();
                    if (characters[note.strumLineID].holdTimer == 0) {
                        characters[note.strumLineID].holdTimer = 10;
                    }
                    noteHit(true, note.direction, note.type, note.strumLineID, note,  strumLines[note.strumLineID].members.get(i), characters[note.strumLineID].holdTimer);
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

        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        for (Note note : notes) {
            if (note.y > globalCanvas.getHeight()) {
                note.visible = false;
            } else {
                note.visible = true;
            }
            if (timeElapsed == 0 && start != 0) {
                note.respawn();
            }
            note.y = note.strumLine.y + ((note.time - timeElapsed) * scrollSpeed);
        }

        for (SustainNote note : holdNotes) {
            note.visible = note.y < globalCanvas.getHeight() + 100;
            note.y = (note.strumLine.y + ((note.time - timeElapsed) * scrollSpeed)) + 75;
        }


        if (NovaKeys.BACK_SPACE.justPressed) {
            endSong();
        }


        for (FunkinCharacter character : characters) {
            if (character.holdTimer > 0)
                character.holdTimer--;
        }

        camGame.zoom = lerp(camGame.zoom, defaultCamZoom, getDtFinal(4));


        if (NovaKeys.UP.justPressed)
            camY += 100;
        if (NovaKeys.DOWN.justPressed)
            camY -= 100;
        if (NovaKeys.LEFT.justPressed)
            camX += 100;
        if (NovaKeys.RIGHT.justPressed)
            camX -= 100;

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
        if (timeElapsed > songDuration) {
            endSong();
        }

        for (StrumLine strumLine : strumLines) {
            for (int i = 0; i < strumLine.members.size(); i++) {
                NovaAnimSprite strum = (NovaAnimSprite) strumLine.members.toArray()[i];
                strum.x = strumLine.x + (StrumLine.spacing * i);
                strum.y = strumLine.y;
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
    }

    public void runEvent(String eventName, JSONArray eventParams) {
        Object param1 = 0;
        if (!callInScripts("onEvent", ScriptEvents.SongEvent(eventName, eventParams))) return;
        switch (eventName) {
            case "Camera Movement":
                param1 = eventParams.getInt(0);
                FunkinCharacter daCharacter = characters[(int) param1];

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
        }
    }
    public boolean danceSwap = false;
    public void beat() {
        super.beat();

        for (NovaSprite sprite : introSprites) {
            if (sprite != null)
                sprite.visible = false;
        }
        switch (curBeat) {
            case -3:
                //introSprites[0].visible = true;
                introSounds[0].play();
                break;
            case -2:
                introSprites[1].visible = true;
                introSounds[1].play();
                break;
            case -1:
                introSprites[2].visible = true;
                introSounds[2].play();
                break;
            case 0:
                introSprites[3].visible = true;
                introSounds[3].play();
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
            if (curBeat % 4 == 0 && !eventBopEnabled)
                camGame.zoom += .05;

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

    public void newRating(double percent, int direction, Strum strum) {
        NovaSprite ratingSprite = new NovaSprite(player.x-300, player.y + 300);
        ratingSprite.setScale(0.5, 0.5);
        if (percent <= 25) {
            // Perfect
            ratingSprite.setImage("game/ratings/sick");
            spawnSplash(direction, strum);
        } else if (percent <= 50) {
            // Good
            ratingSprite.setImage("game/ratings/good");
        } else if (percent <= 75) {
            // Bad
            ratingSprite.setImage("game/ratings/bad");
        } else if (percent <= 100) {
            // Shit
            ratingSprite.setImage("game/ratings/shit");
        }
        ratings.addToGroup(ratingSprite);
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
        if (chart.getJSONArray("noteTypes").length() > 0)
            noteTypeName = chart.getJSONArray("noteTypes").getString(noteType);

        if (!callInScripts("onNoteHit", ScriptEvents.NoteHitEvent(
                direction,
                noteTypeName,
                noteType,
                strumLineID,
                false
        ))) return true;
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
        if (chart.getJSONArray("noteTypes").length() > 0)
            noteTypeName = chart.getJSONArray("noteTypes").getString(noteType);

        if (!callInScripts("onNoteHit", ScriptEvents.NoteHitEvent(
                direction,
                noteTypeName,
                noteType,
                strumLineID,
                true
        ))) return;
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
        } else if (holdTimer == 0){
            daCharacter.playAnim(anims[direction]);
            daCharacter.setFrame(0);
        }
    }

    public void noteMiss(int direction, int strumLineID) {
        FunkinCharacter daCharacter = characters[strumLineID];
        if (!callInScripts("onNoteMiss", ScriptEvents.NoteHitEvent(direction, strumLineID))) return;
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
            trace(difficulty);
            chart = CoolUtil.parseJson("songs/" + song.toLowerCase() + "/charts/" + difficulty + ".json");
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
        for (FunkinCharacter character : characters) {
            if (character.isSpectator) {
                //character.visible = false;
                add(character);
            }
        }
        add(ratings);
        for (FunkinCharacter character : characters) {
            if (character.isPlayer)
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
                        sustainLength = (int) Math.round(note.getInt("sLen") / (bpm / 16));
                    if (sustainLength > 0)
                        for (int e = 0; e <= sustainLength; e++) {
                            //if (strumLines[a].type == 1)
                                addSustainNote("default", note.getInt("time") + (10 * e), note.getInt("id"), strumLines[a], a, e == sustainLength, noteType);
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
            String daString = "Voices-" + curVariation;
            if (daString.endsWith("-")) {
                daString = daString.replace("-", "");
            }
            voices = CoolUtil.playSound("songs/" + song + "/song/" + daString + ".mp3", daVolume);
        } else {

            voices = CoolUtil.playSound("songs/" + song + "/song/Voices.mp3", daVolume);
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
            CoolUtil.playMusic("songs/" + song + "/song/Inst.mp3");
            music.stop();
        }
        songDuration = CoolUtil.getMP3duration("songs/" + song + "/song/Inst.mp3");
        if (curVariation != "") {
            String daString = "Inst-" + curVariation;
            if (daString.endsWith("-")) {
                daString = daString.replace("-", "");
            }
            inst = CoolUtil.playSound("songs/" + song + "/song/" + daString + ".mp3", daVolume);
        } else {

            inst = CoolUtil.playSound("songs/" + song + "/song/Inst.mp3", daVolume);
        }
        if (songMeta.has("needsVoices")) {
            if (songMeta.getBoolean("needsVoices")) {
                setVoices(daVolume);
            }
        } else {
            setVoices(daVolume);
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
    }
}
