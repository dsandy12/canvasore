module edu.asu.dlsandy.canvas_ore {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires transitive javafx.web;
    requires transitive javafx.graphics;
    requires java.logging;
    requires javafx.swing;

    opens edu.asu.dlsandy.canvas_ore to javafx.fxml;
    exports edu.asu.dlsandy.canvas_ore;
}