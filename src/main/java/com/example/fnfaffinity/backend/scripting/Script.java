package com.example.fnfaffinity.backend.scripting;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.backend.utils.CoolUtil;
import com.example.fnfaffinity.backend.utils.MusicBeatState;
import com.example.fnfaffinity.novahandlers.NovaAnimSprite;
import com.example.fnfaffinity.novahandlers.NovaSprite;
import org.json.JSONObject;

import javax.script.*;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.example.fnfaffinity.backend.utils.CoolUtil.inputStreamReaderToString;

public class Script extends Main {
    public Invocable engine;
    public ScriptEngine jsEngine;

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

    public Script(String path) {
        if (!path.endsWith(".js")) {
            path += ".js";
        }
        if (!CoolUtil.checkFileExists(path)) {
            return;
        }
        System.setProperty("polyglot.js.nashorn-compat", "true");
        ScriptEngineManager mgr = new ScriptEngineManager();


        jsEngine = mgr.getEngineByName("graal.js");
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
}
