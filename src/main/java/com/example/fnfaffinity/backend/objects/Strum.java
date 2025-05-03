package com.example.fnfaffinity.backend.objects;

import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.novahandlers.NovaAnimController;
import com.example.fnfaffinity.novahandlers.NovaAnimSprite;
import com.example.fnfaffinity.states.PlayState;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class Strum extends NovaAnimSprite {

    public String[] staticAnims = {"staticLeft", "staticDown", "staticUp", "staticRight"};
    public String[] pressAnims = {"left press", "down press", "up press", "right press"};
    public String[] confirmAnims = {"left confirm", "down confirm", "up confirm", "right confirm"};

    public String skin = "";
    public JSONObject skinData;

    public int direction = 0;

    public String getDirectionName(String state) {

        if (Objects.equals(state, "static")) {
            return staticAnims[direction];
        } else if (Objects.equals(state, "pressed")) {
            return pressAnims[direction];
        } else if (Objects.equals(state, "confirm")) {
            return confirmAnims[direction];
        }
        return "";
    }


    public Strum(String skin, double xPos, double yPos, int direction) throws IOException {
        super("game/notes/" + skin + "/strums", xPos, yPos);
        this.skin = skin;
        this.skinData = CoolUtil.parseJson("images/game/notes/" + skin + "/meta");
        this.direction = direction;

        int mult = 1;
        //if (PlayState.downScroll)
            //mult = -2;
        setScale(0.75, 0.75);
        addAnimation("static", getDirectionName("static"), 24, false);
        animations.get(0).setOffsets(
                skinData.getJSONObject("offsets").getJSONArray("statics").getDouble(0),
                skinData.getJSONObject("offsets").getJSONArray("statics").getDouble(1)*mult
        );

        addAnimation("pressed", getDirectionName("pressed"), 24, false);
        animations.get(1).setOffsets(
            skinData.getJSONObject("offsets").getJSONArray("pressed").getDouble(0),
            skinData.getJSONObject("offsets").getJSONArray("pressed").getDouble(1)*mult
        );

        addAnimation("confirm", getDirectionName("confirm"), 24, false);
        animations.get(2).setOffsets(
            skinData.getJSONObject("offsets").getJSONArray("confirm").getDouble(0),
            skinData.getJSONObject("offsets").getJSONArray("confirm").getDouble(1)*mult
        );

        for (NovaAnimController i : animations) {
            //System.out.println(i.name);
        }
        playAnim("static");
    }
}
