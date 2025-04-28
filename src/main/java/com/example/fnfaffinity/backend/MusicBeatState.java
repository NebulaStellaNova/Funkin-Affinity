package com.example.fnfaffinity.backend;
import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.novautils.NovaTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import javafx.util.Duration;
import org.xml.sax.SAXException;
import com.example.fnfaffinity.novautils.NovaKeys;

public class MusicBeatState extends Main {
    public static boolean active = false;
    public static MusicBeatState globalNextState;
    public static int curBeat = 0;
    public static int curStep = 0;
    protected static double bpm = 102;
    public static boolean canTransition = false;

    public static Script script;


    Timeline updater = new Timeline(
            new KeyFrame(Duration.millis(1000/fps),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            try {
                                update();
                                global_update();
                            } catch (ParserConfigurationException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (SAXException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }));
    Timeline beater = new Timeline(
            new KeyFrame(Duration.millis((60/bpm)*1000),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            beat();
                        }
                    }));
    Timeline stepper = new Timeline(
            new KeyFrame(Duration.millis((60/(bpm*16))*1000),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            step();
                        }
                    }));

    public MusicBeatState() {
        //
        //init();
    }

    public void updateBPM(double targetBPM) {
        bpm = targetBPM;
        for (Timeline timeline : Arrays.asList(beater, stepper)) {
            timeline.stop();
        }
        beater = new Timeline(
                new KeyFrame(Duration.millis((60/bpm)*1000),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                beat();
                            }
                        }));
        stepper = new Timeline(
                new KeyFrame(Duration.millis((60/(bpm*16))*1000),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                step();
                            }
                        }));
        for (Timeline timeline : Arrays.asList(beater, stepper)) {
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    public void init() {
        if (!canTransition) {
            canTransition = true;
            active = true;
            System.out.println("Created State");
            create();
            for (Timeline timeline : Arrays.asList(updater, beater, stepper)) {
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
            }
            postCreate();
        }
    }
    public static String getStateName() {
        String[] classParts = globalNextState.getClass().toString().split("\\.");
        return classParts[classParts.length-1];
    }
    public void switchState(MusicBeatState nextState) {
        if (canTransition) {
            canTransition = false;
            doTransition("out");
            globalNextState = nextState;

            new NovaTimer(1, new Runnable() {
                @Override
                public void run() {
                    destroy();
                    globalNextState.init();
                    doTransition("in");
                }
            });
        }
        // destroy();

        //nextState.init();
    }
    public void reloadState() {
        globalNextState.destroy();
        canTransition = false;
        globalNextState.init();
    }

    public void switchState() {
        destroy();
    }

    public void create() {
        script = new Script("data/states/" + getStateName());
        script.call("create");
    }

    public void postCreate() {
        script.call("postCreate");
        //doTransition("in");
    }

    public void update() {
        if (NovaKeys.F5.justPressed)
            reloadState();

        script.call("update");
    }
    public void beat() {
        curBeat++;
        script.set("curBeat", curBeat);
        script.call("beatHit");
    }

    public void step() {
        curStep++;
        script.set("curStep", curStep);
        script.call("stepHit");
    }

    public void destroy() {
        script.call("destroy");
        System.out.println("Destroyed State");
        active = false;
        updater.stop();
        beater.stop();
        stepper.stop();
        curBeat = 0;
        curStep = 0;
        clearObj();
        camGame.reset();
    }
}
