module com.example.hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.desktop;
    requires java.logging;

    // TraceZero Backend Libraries
    requires metadata.extractor;
    requires org.apache.pdfbox;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires com.google.gson;

    // JNA for Native Window Styling
    requires com.sun.jna;
    requires com.sun.jna.platform;

    // ─── REFLECTION PERMISSIONS ───

    // Allows JavaFX to load your UI controllers
    opens com.example.hellofx to javafx.fxml;
    opens com.example.hellofx.controllers to javafx.fxml;

    // 🔥 THE FIX: Allows Gson to read/write your statistics data
    opens com.example.hellofx.core to com.google.gson;
    opens com.example.hellofx.utils to com.google.gson;

    // ─── EXPORTS ───
    exports com.example.hellofx;
    exports com.example.hellofx.controllers;
    exports com.example.hellofx.core;
}