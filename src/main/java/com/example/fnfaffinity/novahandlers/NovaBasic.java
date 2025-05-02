package com.example.fnfaffinity.novahandlers;

import com.example.fnfaffinity.Main;
import org.json.JSONObject;

public class NovaBasic {
    public boolean alive = true;
    public double x;
    public double y;
    public double defX;
    public double defY;
    public double scaleX = 1;
    public double scaleY = 1;
    public double scrollX = 1;
    public double scrollY = 1;
    public double alpha = 1;
    public boolean visible = true;
    public double angle = 0;
    public NovaCamera camera;
    public JSONObject extra = new JSONObject();

    public void setExtra(String name, Object what) {
        extra.put(name, what);
    }
    public Object getExtra(String name) {
        if (extra.has(name))
            return extra.get(name);
        else return null;
    }
}
