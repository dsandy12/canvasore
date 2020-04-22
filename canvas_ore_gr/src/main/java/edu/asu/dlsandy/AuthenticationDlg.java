package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * A web browser that opens the canvas login site and automatically 
 * closes when the canvas web site has been reached (following
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
     * wait for either the window to be closed or authenticatoin to complete. 
     */
    public AuthenticationDlg() {
    	this.getIcons().add( new Image( CanvasOre.class.getResourceAsStream( "/app_icon.png" )));
    	
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
        webEngine.load("https://canvas.asu.edu/login");

        // set up a listener for the webEngineLoader so that when each page loads
        // we can check if the canvas web site has been reached
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<State>() {
                  @Override public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                      if (newState == Worker.State.SUCCEEDED) {
                        // new page has loaded
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
                    }
                });       
    }
    
    /**
     * @return the webEngine object used by the mini browser
     */
    public WebEngine getEngine() {return webEngine;}
    
    /**
     * @return true if authentication completed, otherwises false
     */
    public boolean athenticationSuccess() {return done;}
}
