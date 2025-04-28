package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.Main;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.scene.media.AudioClip;
import org.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Random;

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
    public static AudioClip playSound(String path, float daVolume) {
        AudioClip sound = new AudioClip(Main.class.getResource(path).toExternalForm());
        if (daVolume == 0) {
            sound.setVolume(0);
        } else {
            sound.setVolume((volume + daVolume)/2);
        }
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

    public static FunkinCharacter[] addToArray(FunkinCharacter arr[], FunkinCharacter x)
    {
        int n = arr.length;
        int i;
        FunkinCharacter[] newarr = new FunkinCharacter[n + 1];
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

    public static Script[] addToArray(Script arr[], Script x)
    {
        int n = arr.length;
        int i;
        Script[] newarr = new Script[n + 1];
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

    public static StrumLine[] addToArray(StrumLine arr[], StrumLine x)
    {
        int n = arr.length;
        int i;
        StrumLine[] newarr = new StrumLine[n + 1];
        for (i = 0; i < n; i++)
            newarr[i] = arr[i];

        newarr[n] = x;

        return newarr;
    }

    public static Document parseXML(String path, String fallback) {
        final String xmlPath = pathify(path + ".xml");
        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists()) {
            xmlFile = new File(pathify(fallback + ".xml"));
        }
        final DocumentBuilderFactory xmlBF = DocumentBuilderFactory.newInstance();
        final DocumentBuilder xmlDB;
        try {
            xmlDB = xmlBF.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        final Document daXML;
        try {
            daXML = xmlDB.parse(xmlFile);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        daXML.getDocumentElement().normalize();
        return daXML;
    }
    public static Document parseXML(String path) {
        try {
            if (!path.endsWith(".xml"))
                path += ".xml";
            final String xmlPath = pathify(path);
            File xmlFile = new File(xmlPath);
            if (!xmlFile.exists()) {
                trace("Could not find: " + path + ".xml");
                return null;
            }
            final DocumentBuilderFactory xmlBF = DocumentBuilderFactory.newInstance();
            final DocumentBuilder xmlDB;
            xmlDB = xmlBF.newDocumentBuilder();

            final Document daXML;
            daXML = xmlDB.parse(xmlFile);
            daXML.getDocumentElement().normalize();
            return daXML;
        } catch (ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkFileExists(String path) {
        String filePath = pathify(path);
        return new File(filePath).exists();
    }

    public static Element getXMLAttribute(Document xml, String name) {
        NodeList file = xml.getElementsByTagName(name);
        return (Element) file.item(0);
    }

    public static void trace(Object out) {
        System.out.println(out);
    }

    public static String getClassPath(Object obj) {
        return obj.toString().replace("class ", "");
    }

    public static String getClassName(Object obj) {
        String[] classParts = obj.toString().split("\\.");
        return classParts[classParts.length-1];
    }

    public static String inputStreamReaderToString(InputStreamReader reader) { // Thank you geeks for geeks
        int t;
        String read_reslt="";

        // Use of read() method
        while(true)
        {
            try {
                if (!((t = reader.read()) != -1)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            read_reslt = read_reslt+(char)t;
        }
        return read_reslt;
    }

    public int randomInt(int startRange, int endRange) {
        Random random = new Random();
        return random.nextInt(startRange, endRange);
    }

    public float randomFloat(float startRange, float endRange) {
        Random random = new Random();
        return random.nextFloat(startRange, endRange);
    }

    public static String[] listFilesInDirectory(String path) {
        final File folder = new File(path);
        String[] files = {};
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory()) {
                files = addToArray(files, fileEntry.getName());
            }
        }
        //trace(files);
        return files;
    }
    public static String[] listFilesInDirectory(String path, String suffix) {
        final File folder = new File(pathify(path));
        String[] files = {};
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(suffix)) {
                files = addToArray(files, fileEntry.getName());
            }
        }
        //trace(files);
        return files;
    }



    public static long getMP3duration(String filePath) {
        Mp3File mp3file = null;
        try {
            mp3file = new Mp3File(Path.of(pathify(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedTagException e) {
            throw new RuntimeException(e);
        } catch (InvalidDataException e) {
            throw new RuntimeException(e);
        }
        return mp3file.getLengthInMilliseconds();
    }
}
