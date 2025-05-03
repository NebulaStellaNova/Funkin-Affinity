package com.example.fnfaffinity.states;

import com.example.fnfaffinity.backend.objects.CreditsObject;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.*;
import javafx.scene.text.TextAlignment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import static com.example.fnfaffinity.backend.utils.CoolUtil.trace;
import static com.example.fnfaffinity.novahandlers.NovaMath.getDtFinal;
import static com.example.fnfaffinity.novahandlers.NovaMath.lerp;

public class CreditsMenu extends MusicBeatState {

    private NovaSprite bg;
    private NovaGraphic fg;
    private Vector<NovaAlphabet> creditsItems = new Vector<NovaAlphabet>(0);
    private Vector<NovaSprite> icons = new Vector<NovaSprite>(0);
    private Vector<String> urls = new Vector<String>(0);
    private int curSelected = 1;

    public CreditsObject[] credits = {
            new CreditsObject("Affinity Engine", "Who made the engine.", null, null, null, true),
                new CreditsObject("Nebula S Nova", "Coded EVERYTHING.", "https://www.youtube.com/@ChinosModded", "nebula", null, null),

            new CreditsObject("Special Thanks", "Who made the engine.", null, null, null, true),
                new CreditsObject("Racist Green", "Helped me figure out why gf was buggin.", "https://www.youtube.com/@r_acistgreen", null, null, null),
                new CreditsObject("Kingdrgree", "Emotional Support LMFAO.", "https://www.youtube.com/@Kingdrgree", null, null, null)

    };
    public NovaText description;

    public boolean daSwitch = false;

    public void update() {
        super.update();
        if (canTransition && !daSwitch) {
            doTransition("in");
            daSwitch = true;
        }

        if (NovaKeys.BACK_SPACE.justPressed) {
            CoolUtil.playMenuSFX(CoolUtil.CANCEL);
            switchState(new MainMenuState());
        }

        description.camera = camGame;
        description.text = credits[curSelected].desc;

        for (int i = 0; i < creditsItems.size(); i++) {
            if (i == curSelected) {
                creditsItems.set(i, creditsItems.get(i)).x = lerp(creditsItems.get(i).x, 150 - 200, getDtFinal(10));
            } else {
                creditsItems.set(i, creditsItems.get(i)).x = lerp(creditsItems.get(i).x, 50 - 200, getDtFinal(10));
            }
            creditsItems.get(i).x += 40;

            NovaAlphabet item = creditsItems.get(i);
            if (i < icons.size())
                if (icons.get(i) != null) {
                    NovaSprite daIcon = icons.get(i);
                    daIcon.x = item.x + item.fullWidth + 50;
                    daIcon.y = item.y - 50;
                }
        }

        int index = 0;
        for (NovaAlphabet alphabet : creditsItems) {
            alphabet.y = lerp(alphabet.y, (120 * index) + 50, 0.1);

            if ((boolean) alphabet.getExtra("skipMe")) {
                alphabet.x = (globalCanvas.getWidth()/2) - (alphabet.fullWidth/2);
            }
            index++;
        }

        if (NovaKeys.DOWN.justPressed) {
            if (curSelected + 1 < creditsItems.size()-1) {
                if ((boolean) creditsItems.get(curSelected + 1).getExtra("skipMe")) {
                    switchSelection(2);
                } else {
                    switchSelection(1);
                }
            } else {
                switchSelection(1);
            }
        }
        if (NovaKeys.UP.justPressed) {
            if (curSelected - 1 >= 0) {
                if ((boolean) creditsItems.get(curSelected - 1).getExtra("skipMe")) {
                    switchSelection(-2);
                } else {
                    switchSelection(-1);
                }
            } else {
                switchSelection(-1);
            }
        }

        description.size = 20;
        description.y = fg.y + 35;
        description.x = globalCanvas.getWidth()/2;

        if (NovaKeys.ENTER.justPressed) {
            if (!urls.get(curSelected).isEmpty())
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(urls.get(curSelected)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
    }

    public void switchSelection(int amt) {
        if (amt != 0) CoolUtil.playMenuSFX(CoolUtil.SCROLL);
        if (curSelected + amt > creditsItems.size()-1) {
            curSelected = 0;
            if (credits[curSelected].skipMe) {
                curSelected = 1;
            }
        } else if (curSelected + amt < 0) {
            curSelected = creditsItems.size()-1;
        } else {
            curSelected += amt;
        }
        //trace(curSelected);
    }

    public void create() {
        super.create();
        bg = new NovaSprite("menus/mainmenu/menuBG", 0, -50);
        bg.alpha = 1.0;
        bg.setScale(0.8, 0.8);
        bg.setScrollFactor(0, 0);
        bg.visible = true;
        add(bg);

        for (CreditsObject object : credits) {
            NovaAlphabet temp = new NovaAlphabet(object.name, 0, 0);
            temp.setExtra("skipMe", object.skipMe);
            creditsItems.add(temp);

            if(object.icon != "") {
                NovaSprite tempInner = new NovaSprite("credits/" + object.icon, 0, 0);
                icons.add(tempInner);
                add(tempInner);
            } else {
                icons.add(null);
            }
            if (object.url != null) {
                urls.add(object.url);
            } else {
                urls.add(null);
            }
        }

        for (NovaAlphabet alphabet : creditsItems) {
            add(alphabet);
        }

        fg = new NovaSprite(0, 200).makeGraphic(globalCanvas.getWidth(), 50, "#000000");
        fg.alpha = 0.5;
        fg.scrollX = 0;
        fg.scrollY = 0;
        fg.y = globalCanvas.getHeight()-100;
        add(fg);

        description = new NovaText("test", 0, 0);
        description.alignment = TextAlignment.CENTER;
        add(description);
    }

    public void destroy() {
        super.destroy();

        creditsItems = new Vector<NovaAlphabet>(0);
        icons = new Vector<NovaSprite>(0);
    }
}
