package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.novahandlers.NovaAnimSprite;

public class Note extends NovaAnimSprite {
    public int direction = 0;
    public double time = 0;
    public StrumLine strumLine;
    public int strumLineID;
    public int type;

    public Note(String skin, double strumTime, int dir, StrumLine strumline, int strumlineID, int noteType) {
        super("game/notes/" + skin + "/notes", ((Strum) strumline.members.get(dir)).x, strumline.y);
        direction = dir;
        strumLine = strumline;
        time = strumTime;
        strumLineID = strumlineID;
        type = noteType;

        setScale(0.75, 0.75);
        addAnimation("idle", new String[] {"purple", "blue", "green", "red"}[direction], 24, false);
        playAnim("idle");
    }
}
