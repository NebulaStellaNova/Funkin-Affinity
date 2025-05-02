package com.example.fnfaffinity.states;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.*;

import static com.example.fnfaffinity.novahandlers.NovaMath.*;

public class TitleState extends MusicBeatState {

    private static NovaSprite bg;
    private static NovaAnimSprite logo;
    private static NovaAnimSprite logoE;
    private static NovaAnimSprite titleBF;
    private static NovaAnimSprite enterButton;
    static boolean doFade = false;
    static boolean tween = false;
    static NovaTimer daTimer;

    static int pressIndex = 0;

    public void update(){
        super.update();
        //icon.x += 1;
        camGame.zoom = lerp(camGame.zoom, 1, getDtFinal(4));

        enterButton.y = tween ? lerp(enterButton.y, 600, getDtFinal(5)) : enterButton.y;

        if (NovaKeys.ENTER.justPressed) {
            CoolUtil.playMenuSFX(CoolUtil.CONFIRM);
            enterButton.playAnim("ENTER PRESSED");
            if (pressIndex == 0) {
                daTimer = new NovaTimer(2, new Runnable() {
                    @Override
                    public void run() {
                        switchState(new MainMenuState());
                    }
                });
            } else {
                //daTimer.cancelled = true;
                switchState(new MainMenuState());
            }
            pressIndex++;
        }
    }

    public void beat() {
        super.beat();
        logo.playAnim("Main Logo");
        logoE.playAnim("Engine Logo");
        titleBF.playAnim("idle");
        camGame.zoom = 1.05;
    }

    public void create() {
        super.create();

        //doTransition("in");
        /*try {
            FileReader file = new FileReader("resources/com/example/fnfaffinity/images/logo.json");
            //String jsonString ="" ; //assign your JSON String here
            JSONObject obj = new JSONObject(file);

            System.out.println(obj.getString("frames")); //John
        } catch(Exception e) {
            System.out.print(e);
        }*/

        //background = new NovaAnimSprite("title/background", 0, 0);
        //background.addAnimation("city", 10, false);
        //background.playAnim("city");
        //background.setScale(3.0, 3.0);
        //add(background);

        bg = new NovaSprite("menus/title/bg", 0,0);
        bg.alpha = 0.5;
        add(bg);

        logo = new NovaAnimSprite("menus/title/logo", 20, 20);
        logo.addAnimation("Main Logo", 24, false);
        logo.playAnim("Main Logo");
        //logo.setScale(0.5, 0.5);
        add(logo);

        logoE = new NovaAnimSprite("menus/title/logo", globalCanvas.getWidth()-20 - 400, 20);
        logoE.addAnimation("Engine Logo", 24, false);
        logoE.playAnim("Engine Logo");
        add(logoE);

        titleBF = new NovaAnimSprite("menus/title/logo", 450, 250);
        titleBF.addAnimation("idle", "Bf Bop", 24, false);
        titleBF.playAnim("idle");
        add(titleBF);

        enterButton = new NovaAnimSprite("menus/title/titleEnter", 0, 720);
        enterButton.x = (globalCanvas.getWidth()/2) - (enterButton.img.getWidth()/2);
        enterButton.addAnimation("ENTER IDLE", 10, false);
        enterButton.addAnimation("ENTER PRESSED", 10, true);
        enterButton.playAnim("ENTER IDLE");
        add(enterButton);

        new NovaTimer(6, new Runnable() {
            @Override
            public void run() {
                tween = true;
            }
        });
    }

}
