package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

/**
 * A dialog box that provides a progress bar status display as well as 
 * two levels of text description of the current progress.
 * 
 */
public class ProgressDlg extends Stage {
final Label statusText1 = new Label("");
final Label statusText2 = new Label("");
final ProgressBar progressBar = new ProgressBar();
final GridPane grid = new GridPane();
@SuppressWarnings("CanBeFinal")
private LoadingStatus ls;

final Timeline updater = new Timeline(new KeyFrame(Duration.millis(100), event -> {
    if (ls == null) return;
    if (this.ls.isChanged()) {
        statusText1.setText(ls.getMainOperationDescription());
        statusText2.setText(ls.getSubOperationDescription());
        progressBar.setProgress(ls.getPercentDone());
        if (this.ls.getPercentDone() >= 1.0) {
            close();
        }
    }
}));

/**
 * Default constructor for the dialog.  Initialize the internal structure 
 * but don't display the dialog box
 */
public ProgressDlg(LoadingStatus ls) {
	this.getIcons().add( new Image(Objects.requireNonNull(CanvasOre.class.getResourceAsStream("app_icon.png"))));
	this.setAlwaysOnTop(false);
	this.ls = ls;
	
    setTitle("Progress");
    
    grid.setAlignment(Pos.TOP_CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(5, 5, 5, 5));
    setScene(new Scene(grid, 400, 200));
    
    grid.add(statusText1, 0, 0);

    progressBar.setPrefWidth(300.0);
    grid.add(progressBar, 0, 1);

    grid.add(statusText2, 0, 2);
 
    setOnCloseRequest(event -> {
        // stop the updater
    	updater.stop();
    });

    statusText1.setText(ls.getMainOperationDescription());
    statusText2.setText(ls.getSubOperationDescription());
    progressBar.setProgress(ls.getPercentDone());

    // set the update for the dialog box
    updater.setCycleCount(Timeline.INDEFINITE);
    updater.play();
}

}
