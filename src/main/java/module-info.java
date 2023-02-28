module edu.asu.dlsandy.canvas_ore {
    requires javafx.controls;
    requires javafx.base;
    requires transitive javafx.web;
    requires transitive javafx.graphics;
    requires java.logging;

    opens edu.asu.dlsandy.canvas_ore to javafx.fxml;
    exports edu.asu.dlsandy.canvas_ore;
}