package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */

import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.web.*;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * A web browser that opens the canvas login site and automatically 
 * closes when the canvas website has been reached (following
 * authentication).  This class is meant to be used in conjunction
 * with a means of tracking cookies in order to gather the required
 * Canvas session and authentication information.
 * 
 */
public class AuthenticationDlg extends Stage {
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    boolean done = false;

    /**
     * Create the mini browser (authentication) dialog window and 
     * wait for either the window to be closed or authentication to complete.
     */
    public AuthenticationDlg() {
    	this.getIcons().add( new Image(Objects.requireNonNull(CanvasOre.class.getResourceAsStream("app_icon.png"))));
    	
    	// initialize the scene for displaying the web view
    	GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        setScene(new Scene(grid,600,600)); 
              
        //add the web view to the scene
        grid.add(browser,0,0);

        // begin loading the canvas login web page
        webEngine.setJavaScriptEnabled(true);
        webEngine.load("https://canvas.asu.edu/login");

        // set up a listener for the webEngineLoader so that when each page loads
        // we can check if the canvas website has been reached
        webEngine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == State.SUCCEEDED) {
                        // new page has loaded
                        setTitle(webEngine.getLocation());
                        if (!webEngine.getLocation().contains("https://canvas.asu.edu/?")) {
                            // here if the authentication is not yet complete - make the scene visible
                            browser.setVisible(true);
                        } else {
                            // Authentication is complete - close the window
                            done = true;
                            hide();
                        }
                    } else {
                        // the page load is in progress - don't show window
                        browser.setVisible(false);
                    }
                });
    }
    
    /**
     * @return the webEngine object used by the mini browser
     */
    public WebEngine getEngine() {return webEngine;}
    
    /**
     * @return true if authentication completed, otherwise false
     */
    public boolean authenticationSuccess() {return done;}
}
