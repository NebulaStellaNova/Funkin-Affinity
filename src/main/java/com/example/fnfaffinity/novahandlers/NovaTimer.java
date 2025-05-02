package com.example.fnfaffinity.novahandlers;

import com.example.fnfaffinity.Main;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class NovaTimer {
    public static double duration;
    public boolean cancelled = false;
    public Runnable onCompleted;

    Timeline timer;
    Timeline triggerTrigger;
    public NovaTimer(double dur, Runnable callback, Runnable trigger) {
        duration = dur;
        onCompleted = callback;
        triggerTrigger = new Timeline(
                new KeyFrame(Duration.millis(1000/(dur/ Main.fps)),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                trigger.run();
                            }
                        }));
        timer = new Timeline(
                new KeyFrame(Duration.millis(1000/dur),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                if (!cancelled)
                                    onCompleted.run();
                                triggerTrigger.stop();
                            }
                        }));

        timer.play();
        triggerTrigger.setCycleCount(Timeline.INDEFINITE);
        triggerTrigger.play();
    }
    public NovaTimer(double dur, Runnable callback) {
        duration = dur;
        onCompleted = callback;
        timer = new Timeline(
                new KeyFrame(Duration.millis(1000/dur),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                if (!cancelled)
                                    onCompleted.run();
                            }
                        }));
        timer.play();
    }


}
