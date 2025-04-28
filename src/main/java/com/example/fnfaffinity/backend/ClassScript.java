package com.example.fnfaffinity.backend;

import com.example.fnfaffinity.Main;

import javax.script.*;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ClassScript extends Main {
    public Invocable engine;
    public ScriptEngine jsEngine;
    Compilable compiler;

    public ClassScript(String path) {
        if (!path.endsWith(".java")) {
            path += ".java";
        }
        if (!CoolUtil.checkFileExists(path)) {
            return;
        }
        ScriptEngineManager mgr = new ScriptEngineManager();
        jsEngine = mgr.getEngineByName("java");
        InputStream is = Main.class.getResourceAsStream(path);
        compiler = (Compilable) jsEngine;

        InputStreamReader reader = new InputStreamReader(is);
        try {
            Object eval = jsEngine.eval(reader);
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
        //engine = (Invocable) jsEngine;
    }

    public void call(String what, Object params) {
        if (this.engine == null) return;
        try {
            engine.invokeFunction(what, params);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
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
    public void set(String what, Object param) {
        if (jsEngine == null) return;
        jsEngine.put(what, param);
    }
}
