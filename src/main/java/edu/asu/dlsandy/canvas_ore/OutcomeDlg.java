package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This class represents a dialog box window that displays a list of outcomes associated
 * with a course and lets the user select outcomes to edit, delete, or add.
 */
public class OutcomeDlg extends Stage {   
    TreeView<String> outcomeList;
    boolean exitOkay;
    CanvasOutcomes outcomes;
    AssignmentGroups assignmentGroups;
    final Image warnImage = new Image(getClass().getResourceAsStream("warning.bmp"));
    
    /**
     * constructor - initialize the dialog box using the specified parameters.
     * @param outcomes - any existing outcomes to be added.  This parameter can be null,
     *                   or empty.
     * @param assignmentGroups - the assignment groups for the class. 
     */
    public OutcomeDlg(CanvasOutcomes outcomes, AssignmentGroups assignmentGroups) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Add/Edit Outcomes");
        this.outcomes = outcomes;
        this.assignmentGroups = assignmentGroups;

    	// initialize the dialog box icon
        this.getIcons().add( new Image( CanvasOre.class.getResourceAsStream( "app_icon.png" )));

        // create the scene
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        setScene(new Scene(grid, 600, 400));
        
        // add the done button
        Button doneButton = new Button(" Done ");
        doneButton.setDisable(true);
        doneButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                // check to see if there are any problems with the outcomes
                // if so, prompt for an action.
                if (!checkOutcomes()) {
                    Alert alert = new Alert(AlertType.WARNING,"",ButtonType.OK,ButtonType.CANCEL);
                    alert.setTitle("Warning - Invalid Assignments");
                    alert.setHeaderText("Some outcomes are linked to assignments that don't exist for this course. ");
                    alert.setContentText("Press ok to continue, or cancel to go back and fix the problem");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() != ButtonType.OK){
                        // ... user chose CANCEL or closed the dialog
                        return;
                    }
                }
                exitOkay = true;
                close();
            }
        });
        grid.add(doneButton, 0,4);

        // add the cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                exitOkay = false;
                close();
            }
        });      
        grid.add(cancelButton, 0,5);
        
        // add the "add" button
        Button addButton = new Button("  Add  ");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                // open the add/edit dialog box
                OutcomeEditForm form = new OutcomeEditForm(assignmentGroups);
                CanvasOutcome new_outcome =form.editOutcome();
                if (new_outcome!=null) {
                    outcomes.add(new_outcome);
                    doneButton.setDisable(false);
                    updateList();
                }
            }
        });
        grid.add(addButton, 1,4);

        // Add the "edit" button
        Button editButton = new Button(" Edit ");
        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                // open the add/edit dialog box
                int idx = outcomeList.getSelectionModel().getSelectedIndex();
                if ((idx>=0) && (idx<outcomes.size())) {
                    OutcomeEditForm form = new OutcomeEditForm(outcomes.get(idx),assignmentGroups);
                    CanvasOutcome new_outcome = form.editOutcome();
                    if (new_outcome!=null) {
                        outcomes.remove(idx);
                        outcomes.add(idx,new_outcome);
                        doneButton.setDisable(false);
                        
                        // clear any warning icons for this outcome
                        updateList();
                    }
                }              
            }
        });
        grid.add(editButton, 2,4);
        
        // create the dialog box labels
        Label courseNumber = new Label("Course Outcomes");
        grid.add(courseNumber, 0, 0);

        // create the list view of outcomes
        outcomeList = new TreeView<String>();
        TreeItem<String> root = new TreeItem<>();
        outcomeList.setRoot(root);
        outcomeList.setShowRoot(false);
        updateList();
        grid.add(outcomeList, 0,0, 3, 4);
        outcomeList.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.DELETE)) {
                    int idx = outcomeList.getSelectionModel().getSelectedIndex();
                    if ((idx>=0) && (idx<outcomes.size())) {
                        outcomes.remove(idx);
                        doneButton.setDisable(false);
                        updateList();
                    }
                }
            }
        });
        
        // show the dialog box and wait for the user response
        showAndWait();
        
        if (exitOkay) {
            // Save the outcomes
            JsonObject obj = outcomes.toJson();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Outcomes");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setInitialFileName(outcomes.getCourseId());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Outcome Data File","*.ocm"));
            File file = fileChooser.showSaveDialog(this.getOwner());
            BufferedWriter bw; 
            if (file != null) {
                try {
                    bw = new BufferedWriter(new FileWriter(file));
                    obj.writeToFile(bw);
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
    }
            
    /*
     * helper function to update the outcome list.  If an outcome association does not exist
     * in the course, mark the outcome with a warning icon.
     */
    private void updateList() {
        TreeItem<String> root = outcomeList.getRoot();
        root.getChildren().clear();
        for (int i=0;i<outcomes.size();i++) {
            TreeItem<String> item = new TreeItem<String>(outcomes.get(i).getTitle());
            root.getChildren().add(item);
            if (!outcomes.get(i).associationsExist(assignmentGroups)) {
                ImageView warningIcon = new ImageView(warnImage);
                item.setGraphic(warningIcon);
            }
        }
    }
    
    /*
     * helper function to verify that the outcome association exists in the course.
     */
    private boolean checkOutcomes() {
        for (CanvasOutcome outcome:outcomes) {
            if (!outcome.associationsExist(assignmentGroups)) return false;
        }
        return true;
    }
}
