package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A dialog box that provides a progress bar status display as well as 
 * two levels of text description of the current progress.
 * 
 */
public class ProgressDlg extends Stage {
Label statusText1 = new Label("");
Label statusText2 = new Label("");
ProgressBar progressBar = new ProgressBar();
GridPane grid = new GridPane();
LoadingStatus ls = null;
Timeline updater = new Timeline(new KeyFrame(Duration.millis(100),new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {
        synchronized (ls) {
        	if (ls.isChanged()) {
        		statusText1.setText(ls.getMainOperationDescription());
        		statusText2.setText(ls.getSubOperationDescription());
        		progressBar.setProgress(ls.getPercentDone());
        		if (ls.getPercentDone()>=1.0) {
        			close();
        		}
        	}
        }
    }
}));

/**
 * Default constructor for the dialog.  Initialize the internal structure 
 * but don't display the dialog box
 */
public ProgressDlg(LoadingStatus ls) {
	this.getIcons().add( new Image( CanvasOre.class.getResourceAsStream( "/app_icon.png" )));
	this.setAlwaysOnTop(true);
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

    synchronized (ls) {
		statusText1.setText(ls.getMainOperationDescription());
		statusText2.setText(ls.getSubOperationDescription());
		progressBar.setProgress(ls.getPercentDone());
    }
    
    // set the update for the dialog box
    updater.setCycleCount(Timeline.INDEFINITE);
    updater.play();
}

}
