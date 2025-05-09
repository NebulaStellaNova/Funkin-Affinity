package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.novahandlers.NovaAnimSprite;

public class SustainNote extends NovaAnimSprite {
    public int direction = 0;
    public double time = 0;
    public StrumLine strumLine;
    public int strumLineID;
    public int type;
    public double length;
    public boolean isEndPiece = false;
    public double defScaleY;
    public boolean pressed = false;
    public boolean isPlayer;

    public SustainNote(String skin, double strumTime, int dir, StrumLine strumline, int strumlineID, boolean isEndPiece, int noteType, double len, boolean isPlayer) {
        super("game/notes/" + skin + "/sustains", ((Strum) strumline.members.get(dir)).x, strumline.y);
        direction = dir;
        strumLine = strumline;
        time = strumTime;
        strumLineID = strumlineID;
        type = noteType;
        length = len;
        this.isPlayer = isPlayer;
        this.isEndPiece = isEndPiece;
        setScale(0.75, 0.75);
        if (isEndPiece)
            addAnimation("idle", new String[] {
                    "purple hold end",
                    "blue hold end",
                    "green hold end",
                    "red hold end"
            }[direction], 24, false, 40, 0);
        else
            addAnimation("idle", new String[] {
                    "purple hold piece",
                    "blue hold piece",
                    "green hold piece",
                    "red hold piece"
            }[direction], 24, false, 40, 0);
        playAnim("idle");
    }
}
