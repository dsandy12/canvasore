package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * The main application class.  Responsible for managing the application window and 
 * launching the application workflows based on user menu selections
 * 
 */
public class CanvasOre extends Application {
    
	/**
	 * Execute the user workflow to create a new set of outcomes
	 *
     */
    private void createNewOutcomes() {
    	// ask the user to pick the course to create the outcomes for
        CoursePickerForm cpf = new CoursePickerForm();
        CanvasCourse course = cpf.getCourse();
        if (course == null) return;
        
        // load the assignments associated with the course
        AssignmentGroups assignmentGroups = new AssignmentGroups(course.getId());
        
        // load any outcomes that already exist in canvas
        CanvasOutcomes outcomes = new CanvasOutcomes(course.getId(),course.getName());
        
        // launch the outcome dialog to allow the user to create and edit outcomes
        new OutcomeDlg(outcomes,assignmentGroups);
    }

    /**
     * Execute the user workflow to clone an existing set of outcomes
     * to a new course or section.
     *
     */
    private void cloneOutcomes() {
        // open a file picker to get the outcome file to edit
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Outcome File to Clone");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Outcome Data File","*.ocm"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;
        
        // here if an outcome file has been chosen.  Attempt to read it
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file)); 
            String st;
            while ((st = br.readLine()) != null) sb.append(st);
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(CanvasOre.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // convert the string that was read into a JsonObject
        JsonAbstractValue outcomes_json = new JsonResultFactory().build(sb.toString());
        
        // initialize our outcome object
        if (outcomes_json == null) return;
        CanvasOutcomes outcomes = new CanvasOutcomes((JsonObject)outcomes_json);
        
        // get the new course to apply the outcomes to
        CoursePickerForm cpf = new CoursePickerForm();
        CanvasCourse course = cpf.getCourse();
        if (course == null) return;

        // load the assignment groups for the selected course
        AssignmentGroups assignmentGroups = new AssignmentGroups(course.getId());
        outcomes.setCourseId(course.getId());
        outcomes.setCourseName(course.getName());
        
        // launch the outcome dialog to allow the user to review/edit the outcomes
        new OutcomeDlg(outcomes,assignmentGroups);
    }

	/**
	 * Execute the user workflow to edit a new set of outcomes
	 *
     */
    private void outcomeEdit() {
        // open a file picker to get the outcome file to edit
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Outcome File to Edit");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Outcome Data File","*.ocm"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;
        
        // here if an outcome file has been chosen.  Attempt to read it
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file)); 
            String st;
            while ((st = br.readLine()) != null) sb.append(st);
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(CanvasOre.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // convert the string that was read into a JsonObject
        JsonAbstractValue outcomes_json = new JsonResultFactory().build(sb.toString());
        
        // initialize our outcome object
        if (outcomes_json == null) return;
        CanvasOutcomes outcomes = new CanvasOutcomes((JsonObject)outcomes_json);
        
        // get the assignment groups for the class
        AssignmentGroups assignmentGroups = new AssignmentGroups(outcomes.getCourseId());
        
        // initialize and open the outcome dialog box for editing
        new OutcomeDlg(outcomes,assignmentGroups);       
    }

	/**
	 * Execute the user workflow to edit a new set of outcomes
	 * 
	 * @param reportByPoints - true if the report generated should use the "sum of points"
	 *    calculation  method.  Otherwise, use the "average percentage" calculation method.
     */
    private void runReport(boolean reportByPoints) {
    	// prompt the user to select an outcome file to report
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Outcome File to Use");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Outcome Data File","*.ocm"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;
        
        // here if an outcome file has been chosen.  Attempt to read it
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file)); 
            String st;
            while ((st = br.readLine()) != null) sb.append(st);
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(CanvasOre.class.getName()).log(Level.SEVERE, null, ex);
        }

        // convert the string that was read into a JsonObject
        JsonAbstractValue outcomes_json = new JsonResultFactory().build(sb.toString());

        // initialize our outcome object
        if (outcomes_json == null) return;
        CanvasOutcomes outcomes = new CanvasOutcomes((JsonObject)outcomes_json);

        // create and save the new report
        new OutcomeReport(outcomes, reportByPoints);
    }

    /**
     * Called automatically by the javaFx framework when the application is launched.
     * Initialize the main application window and menus.
     *  
     * @param primaryStage - the primary javaFX Stage object for the application
     */
    @Override
    public void start(Stage primaryStage) {
    	// set the application icon
    	primaryStage.getIcons().add( new Image(Objects.requireNonNull(CanvasOre.class.getResourceAsStream("app_icon.png"))));

    	VBox root = new VBox();
        Scene scene = new Scene(root, 800, 600);

        // create the menu bar
        MenuBar menuBar = new MenuBar();
 
        // --- Menu File
        Menu menuFile = new Menu("File");
        MenuItem fileNew = new MenuItem("New Outcomes...");
        fileNew.setOnAction(t -> createNewOutcomes());
        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(t -> Platform.exit());
        menuFile.getItems().addAll(fileNew);
        menuFile.getItems().addAll(fileExit);
        
        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");
        MenuItem editModify = new MenuItem("Edit Outcomes...");
        MenuItem editApply = new MenuItem("Clone to New Course...");      
        editModify.setOnAction(t -> outcomeEdit());
        editApply.setOnAction(t -> cloneOutcomes());
        menuEdit.getItems().addAll(editModify);
        menuEdit.getItems().addAll(editApply);
 
        // --- Menu Report
        Menu menuReport = new Menu("Report");
        MenuItem reportAvgPct = new MenuItem("Report by Average Percent...");
        MenuItem reportPoints = new MenuItem("Report by Points...");      
        reportAvgPct.setOnAction(t -> runReport(false));
        reportPoints.setOnAction(t -> runReport(true));
        menuReport.getItems().addAll(reportAvgPct);
        menuReport.getItems().addAll(reportPoints);

        // --- Menu Help
        Menu menuHelp = new Menu("Help");
 
        menuBar.getMenus().addAll(menuFile, menuEdit, menuReport, menuHelp);
        
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar);
        
        // finish configuring the stage, and make it visible
        primaryStage.setTitle("Canvas ORE");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // initiate canvas login by reading some dummy data
        try {
			RequesterSso.httpGetRequest("https://canvas.asu.edu/api/v1/users/self");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * The main program entry point
     * 
     * @param args - the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
