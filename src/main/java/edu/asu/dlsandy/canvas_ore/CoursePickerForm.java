package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.util.Calendar;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * A modal dialog box that allows users to pick a course
 * from their canvas profile. Controls on the dialog box allow filtering courses
 * by semester, year and partial match on the course name.  
 */
public class CoursePickerForm extends Stage {
    CanvasCourses courses;
    TextField courseNumberField;
    ChoiceBox<String> yearChoice;
    ListView<String> coursesList;
    ChoiceBox<String> semesterChoice;
    boolean exitOkay = false;

    /**
     * constructor - initialize the dialog box
     */
    public CoursePickerForm() {
    	this.getIcons().add( new Image( CanvasOre.class.getResourceAsStream( "app_icon.png" )));

    	courses = new CanvasCourses();

        setTitle("Course Selection");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        setScene(new Scene(grid, 600, 400));
        
        Label courseNumber = new Label("Course Number");
        grid.add(courseNumber, 4, 0);

        courseNumberField = new TextField();
        courseNumberField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,String oldValue, String newValue) {
                applyFilters();
            }
        });        
        grid.add(courseNumberField, 5, 0);

        Label year = new Label("Year");
        grid.add(year, 4, 1);
        int current_year = Calendar.getInstance().get(Calendar.YEAR);
        
        yearChoice = new ChoiceBox<String>();
        for (int yr = current_year - 3; yr<=current_year+1;yr++) {
            yearChoice.getItems().add(String.valueOf(yr));
        }
        yearChoice.setValue(String.valueOf(current_year));
        yearChoice.getSelectionModel().selectedIndexProperty().addListener(new 
            ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number value, Number new_value) {
                    // make the change to the control value, then apply the filters
                    yearChoice.setValue(yearChoice.getItems().get((int)new_value));
                    applyFilters();
                }
            }
        );
        grid.add(yearChoice, 5, 1);

        Label semester = new Label("Semester");
        grid.add(semester, 4, 2);
        semesterChoice = new ChoiceBox<String>();
        semesterChoice.getItems().addAll("Fall","Spring","Summer","");
        semesterChoice.getSelectionModel().selectedIndexProperty().addListener(new 
            ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number value, Number new_value) {
                    // make the change to the control, then apply the filters
                    semesterChoice.setValue(semesterChoice.getItems().get((int)new_value));
                    applyFilters();
                }
            }
        );
        grid.add(semesterChoice, 5, 2);
        
        Button okayButton = new Button("  Ok  ");
        okayButton.setDisable(true);
        okayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                exitOkay = true;
                close();
            }
        });
        grid.add(okayButton, 1,4);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                exitOkay = false;
                close();
            }
        });
        grid.add(cancelButton, 2,4);

        coursesList = new ListView<String>();
        grid.add(coursesList, 0,0, 3, 4);
        coursesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,String oldValue, String newValue) {
                okayButton.setDisable(false);
            }
        });
        applyFilters();
    }
    
    /**
     * returns the canvas course that was selected by the user
     */
    public CanvasCourse getCourse() 
    {
        CanvasCourse course;
        exitOkay = false;
        showAndWait();
        
        if (!exitOkay) return null;
        for (int i=0;i<courses.size();i++) {
            course = courses.get(i);
            if (course.getCourseCode().compareTo((String)coursesList.getFocusModel().getFocusedItem())==0) {
                return course;
            }
        }
        return null;
    }
    
    /**
     * apply dialog box filters to the courses that are displayed in the picker
     */
    private void applyFilters()
    {
        String yrchoice = (String)yearChoice.getValue();
        int semMonth = 0; 
        if (semesterChoice.getValue() != null) {
            switch ((String)semesterChoice.getValue()) {
                case "Fall":
                    semMonth = 8;
                    break;
                case "Spring":
                    semMonth = 1;
                    break;
                case "Summer":
                    semMonth = 5;
                    break;
                default:
                    semMonth = 0;
            }
        } 

        // set the contents of the list based on the filters
        coursesList.getItems().clear();
        for (int i=0;i<courses.size();i++) {
            CanvasCourse course = courses.get(i);
            // apply filter for course keyword
            if ((courseNumberField.getText().isEmpty())||(course.getCourseCode().toUpperCase().contains(courseNumberField.getText().toUpperCase()))) {
                // apply filter for year
                if ((yrchoice.isEmpty())||(Integer.valueOf(yrchoice) == course.getStartDate().get(Calendar.YEAR))) {
                    // apply filter for semester (start month should be within +/- 1 month of semester start month
                    if ((semMonth == 0)||(Math.abs(course.getStartDate().get(Calendar.MONTH)-semMonth)<=1)) {
                        // show only courses where the user is enrolled as either an instructor or TA
                        if ((course.getEnrollments().contains("ta"))||(course.getEnrollments().contains("teacher")))
                        coursesList.getItems().add(course.getCourseCode());
                    }
                }
            }        
        }
    }
}
