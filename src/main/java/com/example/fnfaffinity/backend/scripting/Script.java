package com.example.fnfaffinity.backend.scripting;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.NovaAnimSprite;
import com.example.fnfaffinity.novahandlers.NovaSprite;
import com.example.fnfaffinity.states.FreeplayState;
import com.example.fnfaffinity.states.MainMenuState;
import com.example.fnfaffinity.states.StoryMenuState;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.json.JSONObject;

import javax.script.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.example.fnfaffinity.backend.utils.CoolUtil.inputStreamReaderToString;

public class Script extends Main {
    public Invocable engine;
    public ScriptEngine jsEngine;
    public Bindings bindings;

    static Vector<Object> scriptObjects = new Vector<Object>(0);

    public Object[] autoImports = {
            NovaSprite.class,
            NovaAnimSprite.class,
            //MusicBeatState.class,
            Main.class//,
            //CoolUtil.class//,
            //Math.class
    };

    public String addImports(String script) {
        String importLines ="";
        for (Object classToImport : autoImports) {
            importLines += "var " + CoolUtil.getClassName(classToImport) + " = Java.type(\"" + CoolUtil.getClassPath(classToImport) + "\")\n";
        }
        return importLines + "\n" + script;
    }
    public void setSprite(String name) {
        set(name, new NovaSprite(0, 0));
        //get(name);
    }
    public void setAnimSprite(String name) {
        set(name, new NovaAnimSprite(0, 0));
        //return get(name);
    }

    public void addScriptSprite(NovaAnimSprite sprite) {
        scriptObjects.add(sprite);
        set("members", scriptObjects);
    }
    public void addScriptSprite(NovaSprite sprite) {
        scriptObjects.add(sprite);
        set("members", scriptObjects);
    }

    public Script(String path) {
        if (!path.endsWith(".js")) {
            path += ".js";
        }
        if (!CoolUtil.checkFileExists(path)) {
            return;
        }
        System.setProperty("polyglot.js.nashorn-compat", "true");
        ScriptEngineManager mgr = new ScriptEngineManager();

        //jsEngine = mgr.getEngineByName("graal.js");
        jsEngine = GraalJSScriptEngine.create(null,
                Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(s -> true)
                .option("js.ecmascript-version", "2021"));

        //jsEngine.put("engine.WarnInterpreterOnly", false);
        //jsEngine.put("NovaSprite", new NovaSprite());


        bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowAllAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
        bindings.put("javaObj", new Object());
        bindings.put("switchModState", (Consumer<String>) s -> MusicBeatState.globalNextState.switchModState(s));
        bindings.put("switchState", (Consumer<MusicBeatState>) s -> MusicBeatState.globalNextState.switchState(s));

        bindings.put("MainMenuState", new MainMenuState());
        bindings.put("FreeplayState", new FreeplayState());
        bindings.put("StoryMenuState", new StoryMenuState());

        bindings.put("setSpriteVar", (Consumer<String>) s -> setSprite(s));
        bindings.put("setAnimSpriteVar", (Consumer<String>) s -> setAnimSprite(s));
        //bindings.put("getAnimSprite", (NovaAnimSprite) (Consumer<String>) s -> new NovaAnimSprite(s, 0, 0));
        bindings.put("addSprite", (Consumer<NovaSprite>) Main::add);
        bindings.put("addAnimSprite", (Consumer<NovaAnimSprite>) s -> addScriptSprite(s));
        //jsEngine.setBindings(bindings);

        InputStream is = Main.class.getResourceAsStream(path);
        try {
            InputStreamReader reader = new InputStreamReader(is);
            jsEngine.eval(addImports(inputStreamReaderToString(reader)));
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
        engine = (Invocable) jsEngine;
        //jsEngine.put("musicbeatstate", jsEngine.get);
    }

    public Object call(String what, Object params) {
        if (this.engine == null) return null;
        try {
            engine.invokeFunction(what, params);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException ignore) {
        }
        return null;
    }
    public boolean call(String what, JSONObject params) {
        if (this.engine == null) return false;
        try {
            engine.invokeFunction(what, (JSONObject) params);
            return params.getBoolean("cancelled");
        } catch (ScriptException | NoSuchMethodException ignore) {
        }
        return false;
    }
    public void call(String what) {
        if (this.engine == null) return;
        try {
            engine.invokeFunction(what, new Object());
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException ignore) {
            //throw new RuntimeException(e);
        }
    }
    public Object get(String what) {
        if (this.jsEngine == null) return null;
        return jsEngine.get(what);
    }
    public void set(String what, Object param) {
        if (jsEngine == null) return;
        jsEngine.put(what, param);
    }
    public void update() {
        if (bindings == null) return;
        bindings.put("members", Main.objects);
        set("members", Main.objects);
    }
}
