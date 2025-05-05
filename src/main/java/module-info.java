module com.example.fnfaffinity {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.graalvm.js.scriptengine;
    requires org.graalvm.js;
    requires mp3agic;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.media;
    requires javafx.base;
    requires java.desktop;
    requires org.json;
    requires java.scripting;
    requires discord.rpc;
    requires javafx.swing;
    requires annotations;

    opens com.example.fnfaffinity to javafx.fxml;
    exports com.example.fnfaffinity;
    exports com.example.fnfaffinity.novahandlers;
    opens com.example.fnfaffinity.novahandlers to javafx.fxml;
}