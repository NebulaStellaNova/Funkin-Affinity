package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.Main;
import javafx.scene.media.AudioClip;
import org.json.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CoolUtil extends Main  {
    public static int SCROLL = 0;
    public static int CONFIRM = 1;

    public static void playMenuSong() {
        //System.out.println(music.getSource());
        if (!music.getSource().endsWith("freakyMenu.mp3")) {
            MusicBeatState.globalNextState.updateBPM(102);
            music.stop();
            music = new AudioClip(Main.class.getResource("audio/freakyMenu.mp3").toExternalForm());
            music.setVolume(volume);
            music.setCycleCount(AudioClip.INDEFINITE);
            music.play();
        }
    }
    public static void playMusic(String path) {
        Main.music.stop();
        music = new AudioClip(Main.class.getResource(path).toExternalForm());
        music.setVolume(volume);
        music.setCycleCount(AudioClip.INDEFINITE);
        music.play();
    }
    public static void playMusic(String path, double bpm) {
        Main.music.stop();
        MusicBeatState.globalNextState.updateBPM(bpm);
        music = new AudioClip(Main.class.getResource(path).toExternalForm());
        music.setVolume(volume);
        music.setCycleCount(AudioClip.INDEFINITE);
        music.play();
    }
    public static AudioClip playSound(String path) {
        AudioClip sound = new AudioClip(Main.class.getResource(path).toExternalForm());
        sound.setVolume(volume);
        sound.play();
        return sound;
    }
    public static void playMenuSFX(int sound) {
        switch (sound) {
            case 0:
                scrollMenu.play();
                break;
            case 1:
                confirm.play();
                break;
        }
    }

    public static String readFile(BufferedReader reader) throws IOException {
        String line;
        String text = "";
        while ((line = reader.readLine()) != null) {
            text += line + "\n";
        }
        return text;
    }

    public static JSONObject parseJson(String path) throws IOException {
        if (!path.endsWith(".json"))
            path += ".json";
        final String filepath = pathify(path);
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String text = readFile(reader);
        JSONObject jsonObj = new JSONObject(text);
        return jsonObj;
    }

    public static String[] addToArray(String arr[], String x)
    {
        int n = arr.length;
        int i;
        String[] newarr = new String[n + 1];
        for (i = 0; i < n; i++)
            newarr[i] = arr[i];

        newarr[n] = x;

        return newarr;
    }
    public static Note[] addToArray(Note arr[], Note x)
    {
        int n = arr.length;
        int i;
        Note[] newarr = new Note[n + 1];
        for (i = 0; i < n; i++)
            newarr[i] = arr[i];

        newarr[n] = x;

        return newarr;
    }
    public static SustainNote[] addToArray(SustainNote arr[], SustainNote x)
    {
        int n = arr.length;
        int i;
        SustainNote[] newarr = new SustainNote[n + 1];
        for (i = 0; i < n; i++)
            newarr[i] = arr[i];

        newarr[n] = x;

        return newarr;
    }

    public static void trace(Object out) {
        System.out.println(out);
    }
}
