package com.example.fnfaffinity.backend.scripting;

import com.example.fnfaffinity.backend.objects.StrumLine;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScriptEvents {
    public static JSONObject CancellableEvent() {
        JSONObject obj = new JSONObject();
        obj.put("cancelled", false);
        return obj;
    }

    public static JSONObject SongEvent(String name, JSONArray params) {
        JSONObject obj = CancellableEvent();
        obj.put("name", name);
        obj.put("params", params);
        return obj;
    }

    public static JSONObject ScrollEvent(int id) {
        JSONObject obj = CancellableEvent();
        obj.put("id", id);
        return obj;
    }

    public static JSONObject SelectEvent(int id, String name) {
        JSONObject obj = CancellableEvent();
        obj.put("id", id);
        obj.put("name", name);
        return obj;
    }

    public static JSONObject NoteHitEvent(int direction, String noteType, int noteTypeID, int strumLineID, boolean isSustainNote) {
        JSONObject obj = CancellableEvent();
        obj.put("direction", direction);
        obj.put("strumLineID", strumLineID);
        obj.put("noteType", noteType);
        obj.put("noteTypeID", noteTypeID);
        obj.put("isSustainNote", isSustainNote);
        return obj;
    }
    public static JSONObject NoteHitEvent(int direction, int strumLineID) {
        JSONObject obj = CancellableEvent();
        obj.put("direction", direction);
        obj.put("strumLineID", strumLineID);
        return obj;
    }
}
