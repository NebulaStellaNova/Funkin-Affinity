module com.example.fnfaffinity {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

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
    requires mp3.wav;
    requires mp3agic;
    requires java.scripting;

    opens com.example.fnfaffinity to javafx.fxml;
    exports com.example.fnfaffinity;
    exports com.example.fnfaffinity.novautils;
    opens com.example.fnfaffinity.novautils to javafx.fxml;
}