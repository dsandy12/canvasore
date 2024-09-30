module edu.asu.dlsandy.canvas_ore {
    requires javafx.controls;
    requires javafx.base;
    requires transitive javafx.web;
    requires transitive javafx.graphics;
    requires java.logging;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens edu.asu.dlsandy.canvas_ore to javafx.fxml;
    exports edu.asu.dlsandy.canvas_ore;
}