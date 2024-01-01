package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2024, Arizona State University
 * All Rights Reserved
 */


import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a dialog box window that displays a list of students in
 * a particular course section.  The dialog is called during report generation and
 * is used to exclude students who are not part of the major from the report.
 */
public class StudentSelectorDlg extends Stage {
    boolean exitOkay;

    // an internal class representation used by the table in this dialog box
    public static class StudentTableData {
        private final SimpleStringProperty userId;
        private final SimpleStringProperty userName;
        private CheckBox include;
        StudentTableData(String userId, String userName) {
            this.userId = new SimpleStringProperty(userId);
            this.userName = new SimpleStringProperty(userName);
            this.include = new CheckBox();
            this.include.setSelected(true);
        }

        public String getUserId() {
            return userId.get();
        }

        public void setUserId(String userId) {
            this.userId.set(userId);
        }

        public String getUserName() {
            return userName.get();
        }

        public void setUserName(String userName) {
            this.userName.set(userName);
        }

        public CheckBox getInclude() {
            return include;
        }

        public void setInclude(CheckBox include) {
            this.include = include;
        }
    }

    public static class EnrollmentList extends ArrayList<StudentTableData> {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * constructor - initialize the list by querying the canvas student enrollments
         *    for the specified course id
         * @param course_id - the canvas course id to query
         */
        public EnrollmentList(String course_id) {
            try {
                JsonArray jsonArray;
                jsonArray = (JsonArray) RequesterSso.apiGetRequest("courses/"+course_id+"/enrollments?type[]=StudentEnrollment");
                if (jsonArray != null) {
                    for (JsonAbstractValue obj:jsonArray) {
                        if (obj.getValue("enrollment_state").equalsIgnoreCase("active")) add(
                                new StudentTableData(
                                        obj.getValue("user_id"),
                                        obj.getValue("user.sortable_name")
                                ));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    final private EnrollmentList enrollmentList;

    /**
     * constructor - initialize the dialog box using the specified parameters.
     *
     * @param course_id      - the course id for the student enrollment.
     */
    public StudentSelectorDlg(String course_id) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Student Selector");

    	// initialize the dialog box icon
        this.getIcons().add( new Image(Objects.requireNonNull(CanvasOre.class.getResourceAsStream("app_icon.png"))));

        // add the done button
        Button doneButton = new Button(" Done ");
        doneButton.setOnAction(e -> {
            exitOkay = true;
            close();
        });

        enrollmentList = new EnrollmentList(course_id);

        // create the table view of students
        TableView<StudentTableData> tableView = new TableView<>();
        tableView.setEditable(false);
        TableColumn<StudentTableData, String> column1 = new TableColumn<>();
        column1.setText("Student Name");
        column1.setCellValueFactory(new PropertyValueFactory<>("userName"));
        TableColumn<StudentTableData, String> column2 = new TableColumn<>();
        column2.setCellValueFactory(new PropertyValueFactory<>("include"));
        column2.setText("Include");
        column2.setSortable(false);
        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);

        // add the students to the table
        for (StudentTableData data: enrollmentList) {
            tableView.getItems().add(data);
        }

        // add items to the scene
        VBox vbox = new VBox(tableView);
        vbox.getChildren().add(doneButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        setScene(new Scene(vbox));

        // show the dialog box and wait for the user response
        showAndWait();
    }

    // returns a list of canvas student ids for the students to include in the report
    public ArrayList<String> getEnrollmentList() {
        ArrayList<String> result = new ArrayList<>();
        for (StudentTableData data:enrollmentList) {
            if ((!exitOkay)||(data.getInclude().isSelected())) {
                result.add(data.getUserId());
            }
        }
        return result;
    }
}
