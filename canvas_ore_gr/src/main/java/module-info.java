module canvas_ore_gr {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.media;
	requires transitive javafx.web;
	requires transitive javafx.graphics;
	requires java.logging;
	requires javafx.swing;
	
	opens edu.asu.dlsandy to javafx.fxml;
	exports edu.asu.dlsandy;
}