package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.Main;
import com.example.fnfaffinity.novautils.NovaAnimSprite;
import com.example.fnfaffinity.novautils.NovaSprite;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Math;

import static com.example.fnfaffinity.backend.CoolUtil.inputStreamReaderToString;
import static com.example.fnfaffinity.backend.CoolUtil.trace;

public class Script extends Main {
    public Invocable engine;
    public ScriptEngine jsEngine;

    public Object[] autoImports = {
            NovaSprite.class,
            NovaAnimSprite.class,
            Main.class,
            CoolUtil.class//,
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
        ScriptEngineManager mgr = new ScriptEngineManager();
        jsEngine = mgr.getEngineByName("nashorn");
        InputStream is = Main.class.getResourceAsStream(path);
        try {
            InputStreamReader reader = new InputStreamReader(is);
            jsEngine.eval(addImports(inputStreamReaderToString(reader)));
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
        engine = (Invocable) jsEngine;
    }

    public void call(String what, Object params) {
        if (this.engine == null) return;
        try {
            engine.invokeFunction(what, params);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException ignore) {
        }
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
        return jsEngine.get(what);
    }
    public void set(String what, Object param) {
        if (jsEngine == null) return;
        jsEngine.put(what, param);
    }
}
