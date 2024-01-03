package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents a dialog box window that displays a list of outcomes associated
 * with a course and lets the user select outcomes to edit, delete, or add.
 */
public class ThresholdDlg extends Stage {
    double exceedsThreshold;
    double demonstratesThreshold;
    OutcomeAssociation outcomeAssociation;
    boolean exitOkay=false;

    private static boolean areThresholdsValid(double exceeds, double demonstrates) {
        if ((exceeds>1.0)||(exceeds<0.0)) return false;
        if ((demonstrates>1.0)||(demonstrates<0.0)) return false;
        if (demonstrates>=exceeds) return false;
        return true;
    }

    /**
     * constructor - initialize the dialog box using the specified parameters.
     * @param association - the outcome association to initialize thresholds from.
     *
     */
    public ThresholdDlg(OutcomeAssociation association) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Set Thresholds");
        this.outcomeAssociation = association;
        if (areThresholdsValid(association.getExceedsThreshold(), association.getDemonstratesThreshold())) {
            this.exceedsThreshold = association.getExceedsThreshold();
            this.demonstratesThreshold = association.getDemonstratesThreshold();
        } else {
            this.exceedsThreshold = OutcomeAssociation.DEFAULT_EXCEEDS_THRESHOLD;
            this.demonstratesThreshold = OutcomeAssociation.DEFAULT_DEMONSTRATES_THRESHOLD;
        }

    	// initialize the dialog box icon
        this.getIcons().add( new Image(Objects.requireNonNull(CanvasOre.class.getResourceAsStream("app_icon.png"))));

        // create the scene
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        setScene(new Scene(grid));
        
        // add the done button
        Button doneButton = new Button(" Done ");
        doneButton.setOnAction(e -> {
            // check to see if there are any problems with the outcomes
            // if so, prompt for an action.
            if (!areThresholdsValid(exceedsThreshold,demonstratesThreshold)) {
                Alert alert = new Alert(AlertType.ERROR, "", ButtonType.OK);
                alert.setTitle("Error - Invalid Value");
                alert.setHeaderText("The threshold values must be between 0.0 and 1.0 and the 'Exceeds' threshold must be greater than the 'Demonstrates' threshold.");
                alert.setContentText("To fix this problem, click Ok");
                alert.showAndWait();
                return;
            }
            exitOkay = true;
            close();
        });
        grid.add(doneButton, 1,4);

        // add the cancel button
        Button defaultsButton = new Button("Use Defaults");
        defaultsButton.setOnAction(e -> {
            exceedsThreshold = OutcomeAssociation.DEFAULT_EXCEEDS_THRESHOLD;
            demonstratesThreshold = OutcomeAssociation.DEFAULT_EXCEEDS_THRESHOLD;
            exitOkay = true;
            close();
        });

        grid.add(defaultsButton, 0,4);

        // create the dialog box labels
        TextField exceedsTextField = new TextField(Double.toString(exceedsThreshold));
        exceedsTextField.setAlignment(Pos.BOTTOM_LEFT );
        exceedsTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                @Override
                public void changed(ObservableValue<? extends Boolean> arg, Boolean oldValue, Boolean newValue)
                {
                    if (!newValue) {
                        // lost focus
                        try {
                            exceedsThreshold = Double.parseDouble(exceedsTextField.getText());
                        } catch (Exception ignored) {
                            exceedsThreshold = 0;
                        }
                        exceedsTextField.setText(Double.toString(exceedsThreshold));
                    }
                }
            });
        grid.add(exceedsTextField, 1, 1);
        grid.add(new Label("Exceeds Threshold:"),0,1);
        // create the dialog box labels
        TextField demonstratesTextField = new TextField(Double.toString(demonstratesThreshold));
        demonstratesTextField.setAlignment(Pos.BOTTOM_LEFT );
        demonstratesTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg, Boolean oldValue, Boolean newValue)
            {
                double d;
                if (!newValue) {
                    // lost focus
                    try {
                        demonstratesThreshold = Double.parseDouble(demonstratesTextField.getText());
                    } catch (Exception ignored) {
                        demonstratesThreshold = 0;
                    }
                    demonstratesTextField.setText(Double.toString(demonstratesThreshold));
                }
            }
        });
        grid.add(new Label("Demonstrates Threshold:"),0,2);
        grid.add(demonstratesTextField, 1, 2);

        // show the dialog box and wait for the user response
        showAndWait();

        if (!exitOkay) {
            // "Cancelling" restore values to originals
            if (areThresholdsValid(association.getExceedsThreshold(), association.getDemonstratesThreshold())) {
                this.exceedsThreshold = association.getExceedsThreshold();
                this.demonstratesThreshold = association.getDemonstratesThreshold();
            } else {
                this.exceedsThreshold = OutcomeAssociation.DEFAULT_EXCEEDS_THRESHOLD;
                this.demonstratesThreshold = OutcomeAssociation.DEFAULT_DEMONSTRATES_THRESHOLD;
            }
        }
    }

    public double getExceedsThreshold() {
        return exceedsThreshold;
    }

    public double getDemonstratesThreshold() {
        return demonstratesThreshold;
    }
}
