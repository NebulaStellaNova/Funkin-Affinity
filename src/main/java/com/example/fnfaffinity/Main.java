package com.example.fnfaffinity;
import java.lang.reflect.Field;
import com.almasb.fxgl.net.Server;
import com.example.fnfaffinity.backend.*;
import com.example.fnfaffinity.novautils.*;
import com.example.fnfaffinity.states.TitleState;
import com.example.fnfaffinity.states.MainMenuState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import javax.sound.sampled.Clip;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;


import java.awt.*;
import java.util.Vector;

import static com.example.fnfaffinity.novautils.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novautils.NovaMath.lerp;

public class Main extends Application {
    public static Stage globalStage;
    public static Scene globalScene;
    public static Canvas globalCanvas;
    public static GraphicsContext globalContext;
    public static double volume = 0.1;

    static Vector<FileCache> cachedFiles = new Vector<FileCache>(0);


    static Vector<Object> objects = new Vector<Object>(0);
    /*static Vector<FunkinCharacter> characters = new Vector<FunkinCharacter>(0);
    static Vector<NovaAnimSprite> animObjects = new Vector<NovaAnimSprite>(0);
    static Vector<NovaGroup> groupObjects = new Vector<NovaGroup>(0);
    static Vector<NovaAlphabet> alphaObjects = new Vector<NovaAlphabet>(0);*/
    static Vector<Object> objectsGlobal = new Vector<Object>(0);
    //static Vector<NovaAnimSprite> animObjectsGlobal = new Vector<NovaAnimSprite>(0);
    public static double globalAlpha = 1.0;
    public static double transitionTime = 1.0;
    public static AudioClip music;
    public static AudioClip confirm;
    public static AudioClip scrollMenu;
    public static double fps = 60;
    Boolean borderless = false;
    static boolean transitionOutActive = false;
    static boolean transitionInActive = false;
    static Runnable onTransitionCompleted;
    public static NovaCamera camGame = new NovaCamera(0, 0);
    public static NovaSprite transitionSprite = new NovaSprite("transition", 0, 0);

    public static double globalSpriteOffsetX;
    public static double globalSpriteOffsetY;

    public static void print(String daText) {
        System.out.println(daText);
    }
    public static void print(int daText) {
        System.out.println(daText);
    }
    public static void print(double daText) {
        System.out.println(daText);
    }
    public static void print(float daText) {
        System.out.println(daText);
    }
    public static void print(boolean daText) {
        System.out.println(daText);
    }

    @Override
    public void start(Stage stage) throws IOException, NoSuchFieldException, IllegalAccessException {
        transitionSprite.setScrollFactor(0, 0);
        transitionSprite.y = -(720 * 4) - (720.0 / 2);
        globalStage = stage;
        globalStage.setTitle("Friday Night Funkin': Affinity Engine");
        globalStage.initStyle(StageStyle.DECORATED);

        System.out.println(stage);
        music = new AudioClip(this.getClass().getResource("audio/freakyMenu.mp3").toExternalForm());
        music.setVolume(volume);
        music.setCycleCount(AudioClip.INDEFINITE);
        music.play();
        confirm = new AudioClip(this.getClass().getResource("audio/confirmMenu.mp3").toExternalForm());
        confirm.setVolume(volume);
        scrollMenu = new AudioClip(this.getClass().getResource("audio/scrollMenu.mp3").toExternalForm());
        scrollMenu.setVolume(volume);
        final double W = globalStage.getWidth();
        final double H = globalStage.getHeight();
        globalCanvas = new Canvas(1280, 720);
        globalContext = globalCanvas.getGraphicsContext2D();
        globalStage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/iconOG.png"))));
        Dimension size
                = Toolkit.getDefaultToolkit().getScreenSize();

        // width will store the width of the screen
        int width = (int)size.getWidth();

        // height will store the height of the screen
        int height = (int)size.getHeight();


        if (OldWindow.width == 0)
        {
            OldWindow.width = 1280;
            OldWindow.height = 720;
        }


        System.out.println("Current Screen resolution : "
                + "width : " + width
                + " height : " + height);
        globalStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (borderless)
                    globalStage.setAlwaysOnTop(observable.getValue());

            }
        });
        globalStage.setScene(new Scene(new Group(globalCanvas)));
        globalStage.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case KeyCode.F11:
                    System.out.println("The 'F11' key was pressed");
                    borderless = !borderless;
                    if (borderless) {
                        OldWindow.width = globalStage.getWidth();
                        OldWindow.height = globalStage.getHeight();
                        OldWindow.x = globalStage.getX();
                        OldWindow.y = globalStage.getY();
                        globalStage.setAlwaysOnTop(true);
                        globalStage.setHeight(width+40);
                        globalStage.setWidth(height+17);
                        globalStage.setX(-8);
                        globalStage.setY(-32);
                    } else {
                        globalStage.setAlwaysOnTop(false);
                        globalStage.setHeight(OldWindow.height);
                        globalStage.setWidth(OldWindow.width);
                        globalStage.setX(OldWindow.x);
                        globalStage.setY(OldWindow.y);
                        globalStage.show();
                    }
                    break;
                case KeyCode.PLUS:
                    volume += 0.1;
                    music.setVolume(volume);
                case KeyCode.MINUS:
                    volume -= 0.1;
                    music.setVolume(volume);
            }
        });
        music.setVolume(volume);
        globalStage.show();
        doThing();
        TitleState titleState = new TitleState();
        MusicBeatState.globalNextState = titleState;
        titleState.init();
        addGlobal(transitionSprite);


        globalStage.getScene().setOnKeyPressed(e -> {
            for (String i : NovaKeys.keyList) {
                try {
                    NovaKey key = (NovaKey) NovaKeys.class.getDeclaredField(i).get(null);
                    KeyCode code = (KeyCode) KeyCode.class.getDeclaredField(i).get(null);
                    if (e.getCode() == code) {
                        if (!key.pressed && key.frame == -1) {
                            key.pressed = true;
                            key.justPressed = true;
                            key.frame = 1;
                            // Debug Code
                            //System.out.println("Pressed: " + i);
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        globalStage.getScene().setOnKeyReleased(e -> {
            for (String i : NovaKeys.keyList) {
                try {
                    NovaKey key = (NovaKey) NovaKeys.class.getDeclaredField(i).get(null);
                    KeyCode code = (KeyCode) KeyCode.class.getDeclaredField(i).get(null);
                    if (e.getCode() == code) {
                        if (key.pressed && key.frame == -1) {
                            key.pressed = false;
                            key.justReleased = true;
                            key.frame = 1;
                            // Debug Code
                            //System.out.println("Released: " + i);
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        /*globalStage.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case KeyCode.ENTER:
                    if (!NovaKeys.ENTER.pressed && NovaKeys.ENTER.frame == -1) {
                        NovaKeys.ENTER.pressed = true;
                        NovaKeys.ENTER.justPressed = true;
                        NovaKeys.ENTER.frame = 1;
                    }
            }
        });
        globalStage.getScene().setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case KeyCode.ENTER:
                    if (NovaKeys.ENTER.pressed && NovaKeys.ENTER.frame == -1) {
                        NovaKeys.ENTER.pressed = false;
                        NovaKeys.ENTER.justReleased = true;
                        NovaKeys.ENTER.frame = 1;
                    }
            }
        });*/
    }

    /*private void doThing() {
    }*/

    //public void create() {

    //}


    public static void add(NovaSprite sprite) {
        objects.add(sprite);
        return;
    }
    public static void add(NovaGroup group) {
        objects.add(group);
        return;
    }
    public static void add(NovaAnimSprite sprite) {
        objects.add(sprite);
        return;
    }
    public static void add(FunkinCharacter sprite) {
        objects.add(sprite);
        return;
    }
    public static void add(NovaAlphabet sprite) {
        objects.add(sprite);
        return;
    }
    public static void addGlobal(NovaSprite sprite) {
        objectsGlobal.add(sprite);
        return;
    }
    public static void addGlobal(NovaAnimSprite sprite) {
        objectsGlobal.add(sprite);
        return;
    }
    public static String pathify(String str) {
        return "src/main/resources/com/example/fnfaffinity/" + str;
    }
    private static String lastError;
    private static int errcount = 0;
    public static void global_update() throws ParserConfigurationException, IOException, SAXException {
        /*if (transitionOutTimer != null)
        {
            if (globalAlpha < 0.01) {
                transitionOutTimer.stop();
                System.out.println("Transition Finished");
            }
        }*/

        if (transitionOutActive) {
            transitionTime = lerp(transitionTime, 0, getDtFinal(10));
            transitionSprite.y = lerp(transitionSprite.y,   -(720 * 2), getDtFinal(10));
            //System.out.println(globalAlpha);
            if (transitionTime < 0.01) {
                transitionOutActive = false;
                if (onTransitionCompleted != null) {
                    onTransitionCompleted.run();
                    onTransitionCompleted = null;
                }
            }
        }
        if (transitionInActive) {
            final int transitionInLength = 5;
            transitionTime = lerp(transitionTime, transitionInLength, getDtFinal(10/transitionInLength));
            //System.out.println(globalAlpha);
            if (transitionTime >= transitionInLength-1) {
                transitionSprite.y = lerp(transitionSprite.y, 720, getDtFinal(10));
            }
            if (transitionTime > transitionInLength-0.01) {
                transitionTime = 1;
                transitionInActive = false;
                if (onTransitionCompleted != null) {
                    onTransitionCompleted.run();
                    onTransitionCompleted = null;
                }
            }
        }
        //System.out.println(globalAlpha);
        double canvasScaleX = globalStage.getWidth()/1280;
        double canvasScaleY = globalStage.getHeight()/720;
        double finalScale = Math.max(canvasScaleX, canvasScaleY);
        //globalCanvas.setWidth(globalStage.getWidth() + (1280*2));
        globalCanvas.setTranslateX((globalStage.getWidth()/2)-(1280/2));
        globalCanvas.setTranslateY(((globalStage.getHeight()-30)/2)-(720/2));
        globalCanvas.setScaleX(finalScale*camGame.zoom);
        globalCanvas.setScaleY(finalScale*camGame.zoom);
        globalContext.setFill(Paint.valueOf("#000000"));
        globalContext.fillRect(globalStage.getWidth(), 0, globalCanvas.getWidth(), globalCanvas.getHeight());

        //globalSpriteOffsetX = global.getWidth();

        for (Object object : objects) {
            if (object.getClass() == NovaSprite.class || object.getClass() == StageSprite.class) {
                if (((NovaSprite) object).alive) {
                    drawSprite((NovaSprite) object);
                }
            }
            if (object.getClass() == Note.class || object.getClass() == SustainNote.class || object.getClass() == Strum.class || object.getClass() == StageAnimSprite.class) {
                if (((NovaAnimSprite) object).alive) {
                    drawSprite((NovaAnimSprite) object);
                }
            }
            if (object.getClass() == NovaAnimSprite.class || object.getClass() == FunkinCharacter.class) {
                if (((NovaAnimSprite) object).alive) {
                    drawSprite((NovaAnimSprite) object);
                }
            }
            if (object.getClass() == NovaAlphabet.class) {
                drawSprite((NovaAlphabet) object);
            }
            if (object.getClass() == NovaGroup.class || object.getClass() == StrumLine.class) {
                if (object.getClass() == StrumLine.class) {
                    if (((StrumLine) object).visible) {
                        for (Object member : ((NovaGroup) object).members) {
                            //System.out.println(member);
                            if (member.getClass() == Strum.class) {
                                if (((NovaAnimSprite) member).alive) {
                                    drawSprite((NovaAnimSprite) member);
                                }
                            }
                        }
                    }
                } else {
                    for (Object member : ((NovaGroup) object).members) {
                        //System.out.println(member);
                        if (member.getClass() == NovaAnimSprite.class || member.getClass() == FunkinCharacter.class || member.getClass() == Strum.class) {
                            if (((NovaAnimSprite) member).alive) {
                                drawSprite((NovaAnimSprite) member);
                            }
                        }
                        if (member.getClass() == NovaSprite.class) {
                            if (((NovaSprite) member).alive) {
                                drawSprite((NovaSprite) member);
                            }
                        }
                    }
                }
            }
            if (object.getClass() == NovaSprite.class) {
                if (((NovaSprite) object).alive) {
                    drawSprite((NovaSprite) object);
                }
            }
        }

        for (Object object : objectsGlobal) {
            if (object.getClass() == NovaSprite.class) {
                if (((NovaSprite) object).alive) {
                    drawSprite((NovaSprite) object);
                }
            }
            if (object.getClass() == NovaAnimSprite.class || object.getClass() == FunkinCharacter.class) {
                if (((NovaAnimSprite) object).alive) {
                    drawSprite((NovaAnimSprite) object);
                }
            }
            if (object.getClass() == NovaAlphabet.class) {
                drawSprite((NovaAlphabet) object);
            }
            if (object.getClass() == NovaSprite.class) {
                if (((NovaSprite) object).alive) {
                    drawSprite((NovaSprite) object);
                }
            }
        }


        for (String i : NovaKeys.keyList) {
            try {
                NovaKey key = (NovaKey) NovaKeys.class.getDeclaredField(i).get(null);
                key.update();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
        }
        //NovaKeys.update();
    }
    public static void drawSprite(NovaSprite object) {
        if (object.visible) {
            final double daAlpha = (object.alpha*globalAlpha);
            globalContext.setGlobalAlpha(daAlpha);
            globalContext.drawImage(object.img, 0, 0, object.img.getWidth(), object.img.getHeight(), object.x + (object.camera.x*object.scrollX) + globalSpriteOffsetX, object.y + (object.camera.y*object.scrollY) + globalSpriteOffsetY, object.img.getWidth()*object.scaleX, object.img.getHeight()*object.scaleY);
            globalContext.setGlobalAlpha(1);
        }
        return;
    }
    public static void drawSprite(NovaAlphabet objectFull) {
        globalContext.drawImage(objectFull.icon.img, 0, 0, objectFull.icon.img.getWidth()/2, objectFull.icon.img.getHeight(), objectFull.width + objectFull.camera.x + objectFull.x + globalSpriteOffsetX, (-20 + (objectFull.camera.y) + objectFull.y*2) + globalSpriteOffsetY, (objectFull.icon.img.getWidth()/2) * objectFull.icon.scaleX, objectFull.icon.img.getHeight() * objectFull.icon.scaleY);
        //drawSprite(objectFull.icon);
        for (NovaSprite object : objectFull.sprites) {
            if (object.visible) {
                final double daAlpha = (object.alpha * globalAlpha);
                globalContext.setGlobalAlpha(daAlpha);
                globalContext.drawImage(object.img, 0, 0, object.img.getWidth(), object.img.getHeight(), object.x + (object.camera.x * object.scrollX) + objectFull.x + globalSpriteOffsetX, object.y + (object.camera.y * object.scrollY) + objectFull.y + globalSpriteOffsetY, object.img.getWidth() * object.scaleX, object.img.getHeight() * object.scaleY);
                globalContext.setGlobalAlpha(1);
            }
        }
        return;
    }
    public static void drawSprite(NovaAnimSprite object) {
        if (!object.visible) return;
        //final Image tempImg = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("images/" + object.path + ".png")));
        try {
            final String filepath = pathify("images/" + object.path + ".xml");

            File file = null;
            Document document = null;

            boolean fileCached = false;
            for (FileCache fileCache : cachedFiles) {
                if (Objects.equals(fileCache.path, filepath)) {
                    file = fileCache.fileData;
                    document = fileCache.document;
                    fileCached = true;
                }
            }

            if (!fileCached) {
                //System.out.println("File not cached! Caching.");
                file = new File(filepath);
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                document = db.parse(file);
                cachedFiles.add(new FileCache(filepath, file, document));
            } else {
                //System.out.println("File is cached!");
            }
            document.getDocumentElement().normalize();
            //System.out.println("Root element :" + document.getDocumentElement().getNodeName());
            final NodeList nList = document.getElementsByTagName("SubTexture");
            int FramRef = 0;
            int animLength = 0;
            String prefix = "";
            for (NovaAnimController controller : object.animations) {
                if (Objects.equals(controller.name, object.curAnim)) {
                    prefix = controller.prefix;
                    //System.out.println(controller.prefix);
                }
            }
            final String curAnim = object.curAnim;
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String frameName = eElement.getAttribute("name");
                    if (frameName.startsWith(prefix)) {
                        animLength += 1;
                    }
                }
            }
            for (int i = 0; i < object.animations.size(); i++) {
                if (object.animations.get(i).curFrame > animLength - 1) {
                    if (object.animations.get(i).loop) {
                        object.animations.set(i, object.animations.get(i)).curFrame = 0;
                        object.curAnim = curAnim;
                    } else {
                        object.animations.set(i, object.animations.get(i)).curFrame = animLength - 1;
                    }
                }
                if (object.animations.get(i).loop) {
                    object.animations.set(i, object.animations.get(i)).curFrame += object.animations.get(i).fps / fps;
                } else {
                    if (object.animations.get(i).curFrame != animLength - 1) {
                        object.animations.set(i, object.animations.get(i)).curFrame += object.animations.get(i).fps / fps;
                    }
                }
                FramRef = (int) Math.round(object.animations.get(i).curFrame);
            }
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Node prevFrame = null;
                try {
                    prevFrame = nList.item(temp - 1);
                } catch (Exception ignore) {
                }

                //System.out.println("\nCurrent Element: " + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element eElement = (Element) nNode;
                    final Element eElementPrev = (Element) prevFrame;
                    final String frameName = eElement.getAttribute("name");
                    //System.out.println(FramRef);
                    int frameFix = (int) Math.round(Double.parseDouble(Double.toString(FramRef).substring(0, 2)));
                    

                    //System.out.println(frameName);
                    if (frameName.startsWith(prefix) && Math.round(Double.parseDouble(frameName.substring(frameName.length() - 4))) == frameFix) {
                        double frameX = 0;
                        double frameY = 0;
                        double frameW = 0;
                        double frameH = 0;
                        double X = 0;
                        double Y = 0;
                        double W = 0;
                        double H = 0;
                        try {
                            frameX = Integer.parseInt(eElement.getAttribute("frameX"));
                            frameY = Integer.parseInt(eElement.getAttribute("frameY"));
                            frameW = Integer.parseInt(eElement.getAttribute("frameWidth"));
                            frameH = Integer.parseInt(eElement.getAttribute("frameHeight"));
                            X = Integer.parseInt(eElement.getAttribute("x"));
                            Y = Integer.parseInt(eElement.getAttribute("y"));
                            W = Integer.parseInt(eElement.getAttribute("width"));
                            H = Integer.parseInt(eElement.getAttribute("height"));
                        } catch (Exception e) {
                            try {
                                //assert eElementPrev != null;
                                frameX = Integer.parseInt(eElementPrev.getAttribute("frameX"));
                                frameY = Integer.parseInt(eElementPrev.getAttribute("frameY"));
                                frameW = Integer.parseInt(eElementPrev.getAttribute("frameWidth"));
                                frameH = Integer.parseInt(eElementPrev.getAttribute("frameHeight"));
                                X = Integer.parseInt(eElementPrev.getAttribute("x"));
                                Y = Integer.parseInt(eElementPrev.getAttribute("y"));
                                W = Integer.parseInt(eElementPrev.getAttribute("width"));
                                H = Integer.parseInt(eElementPrev.getAttribute("height"));
                            } catch (Exception ignore) {
                                X = Integer.parseInt(eElement.getAttribute("x"));
                                Y = Integer.parseInt(eElement.getAttribute("y"));
                                W = Integer.parseInt(eElement.getAttribute("width"));
                                H = Integer.parseInt(eElement.getAttribute("height"));
                                frameX = 0;
                                frameY = 0;
                                frameW = H;
                                frameH = W;
                            }
                        }
                        if (object.visible) {
                            final double daAlpha = (object.alpha * globalAlpha);
                            globalContext.setGlobalAlpha(daAlpha);
                            double offsetX = 0;
                            double offsetY = 0;
                            for (NovaAnimController anim : object.animations) {
                                if (Objects.equals(anim.name, object.curAnim)) {
                                    offsetX = anim.offsetX;
                                    offsetY = anim.offsetY;
                                }
                            }
                            int flipInt = 0;
                            if (object.flipX) {
                                flipInt = -1;
                            } else {
                                flipInt = 1;
                            }
                            object.frameWidth = frameW;
                            globalContext.drawImage(object.img, X, Y, W, H, (object.x - (frameX * (object.scaleX * flipInt)) + (object.camera.x * object.scrollX)) + (offsetX*flipInt) + globalSpriteOffsetX, (object.y - (frameY * object.scaleX) + (object.camera.y * object.scrollY)) + offsetY + globalSpriteOffsetY, (W * (object.scaleX * flipInt)), H * object.scaleY);
                            globalContext.setGlobalAlpha(1);
                        }
                        //break;
                    }
                }
            }
        } catch (Exception ignore) {}
        return;
    }
    public static void clearObj() {
        objects = new Vector<Object>(0);
        //animObjects = new Vector<NovaAnimSprite>(0);
        //alphaObjects = new Vector<NovaAlphabet>(0);
        //characters = new Vector<FunkinCharacter>(0);

        for (Object object : objects)
            if (object.getClass() == NovaGroup.class)
                for (NovaAnimSprite member : ((NovaGroup) object).members) {
                    member.destroy();
                }
        //groupObjects = new Vector<NovaGroup>(0);
    }
    public static void doTransition(String type) {
        if (Objects.equals(type, "out")) {
            transitionTime = 1;
            new NovaTimer(1000, new Runnable() {
                @Override
                public void run() {
                    transitionSprite.y = -(720 * 4) - (720.0 / 2);
                    transitionOutActive = true;
                }
            });
        }
        if (Objects.equals(type, "in")) {
            // = 0;
            new NovaTimer(10000, new Runnable() {
                @Override
                public void run() {
                    transitionSprite.y =  -(720 * 2);
                    transitionInActive = true;
                }
            });
        }
    }
    public static void doTransition(String type, Runnable callback) {
        if (Objects.equals(type, "out")) {
            transitionTime = 1;
            new NovaTimer(1000, new Runnable() {
                @Override
                public void run() {
                    transitionSprite.y = -(720 * 4) - (720.0 / 2);
                    transitionOutActive = true;
                }
            });
        }
        if (Objects.equals(type, "in")) {
            //globalAlpha = 0;
            new NovaTimer(100000, new Runnable() {
                @Override
                public void run() {
                    transitionSprite.y =  -(720 * 2);
                    transitionInActive = true;
                }
            });
        }
        onTransitionCompleted = callback;
    }
    public static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }
    public static void main(String[] args) {
        launch();
    }

    public static void addScriptSprite(Object object) {
        CoolUtil.trace(object);
        objects.add((NovaSprite) object);
    }

    void doThing() {

    }
}


