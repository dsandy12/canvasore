package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A dialog box (form) that allows the user to edit outcome information and modify outcome associations
 */
public class OutcomeEditForm extends Stage {
    //TreeView<String> assignmentList;
    TreeView<OutcomeAssociation> assignmentList;
	TextField titleField;
    TextArea descriptionField;
    CanvasOutcome outcome;
    AssignmentGroups assignmentGroups;
    boolean exitOkay;
    final Image warnImage = new Image(getClass().getResourceAsStream("warning.bmp"));
    
    /**
     * constructor - initialize using list of assignment groups.  This constructor should be
     * used for new outcomes
     */
    public OutcomeEditForm(AssignmentGroups assignmentGroups) {
        initModality(Modality.APPLICATION_MODAL);
        this.outcome = new CanvasOutcome();
        this.assignmentGroups = assignmentGroups;
        createForm();
    }    

    /**
     * constructor - initialize from outcome information and assignment groups.  If outcome
     * associations exist for assignments that don't exist, mark the association with a warning
     * icon.  Use this constructor for editing an existing outcome 
     */
    public OutcomeEditForm(CanvasOutcome outcome, AssignmentGroups assignmentGroups) {
        initModality(Modality.APPLICATION_MODAL);
        this.outcome = outcome;
        this.assignmentGroups = assignmentGroups;
    	this.getIcons().add( new Image( CanvasOre.class.getResourceAsStream( "app_icon.png" )));
        createForm();      
        titleField.setText(outcome.getTitle());
        descriptionField.setText(outcome.getDescription());
    }    
    
    /**
     * Helper function to add the associations to the dialog box's picker tree.  
     */
    private void addCanvasAssociationsToTree(CheckBoxTreeItem<OutcomeAssociation> root) {
      // loop for each assignment group
      for (AssignmentGroup group:assignmentGroups) {
    	  // create the new association and tree element
    	  CheckBoxTreeItem<OutcomeAssociation> groupItem = new CheckBoxTreeItem<>(new OutcomeAssociation(group.getName(),null,null,null,null));
    	  groupItem.setExpanded(true);

    	  // loop for every assignment associated with the group
    	  Assignments assignments = group.getAssignments();
          if (assignments!=null) {
              for (Assignment assignment:assignments) {
            	  // create a new assignment item
                  CheckBoxTreeItem<OutcomeAssociation> item = new CheckBoxTreeItem<>(new OutcomeAssociation(group.getName(),assignment.getName(),null,null,null));
                  if ((!assignment.isQuiz())&&(assignment.getRubric()!=null)) {
                      CanvasRubric rubric = assignment.getRubric();
                      if (rubric.getRowCount()>1) {
                          for (int j=0;j<rubric.getRowCount();j++) {
                              CheckBoxTreeItem<OutcomeAssociation> sub_item = new CheckBoxTreeItem<>(new OutcomeAssociation(group.getName(),assignment.getName(),rubric.getRowDescription(j),null,null));
                              sub_item.setIndependent(true);
                              item.getChildren().add(sub_item);
                              item.setExpanded(true);
                          }
                      }
                  } else {
                	  if ((assignment.isQuiz())&&(assignment.getQuiz().getQuestionGroups().size()>0)) {
                		  // assignment has question groups - add the 3rd level association for the group/bank
                		  for (CanvasQuestionGroup qgroup:assignment.getQuiz().getQuestionGroups()) {
                			  String bankTitle = null;
                			  if (qgroup.getBank()!=null) {
                				  bankTitle = qgroup.getBank().getTitle();
                			  }
                              CheckBoxTreeItem<OutcomeAssociation> sub_item = new CheckBoxTreeItem<OutcomeAssociation>(new OutcomeAssociation(group.getName(),assignment.getName(),null,qgroup.getName(),bankTitle));
                              sub_item.setIndependent(true);
                              item.getChildren().add(sub_item);
                              item.setExpanded(true);                			  
                		  }
                	  }
                  }
                  item.setIndependent(true);
                  groupItem.getChildren().add(item);
              }
          }
          groupItem.setIndependent(true);
          root.getChildren().add(groupItem);
      }    	
    }

    /*
     * helper function to add the missing association with a warning icon if it is not found in the tree
     * if it is found, return  with no action taken
     */
    private void addMissingAssociation(CheckBoxTreeItem<OutcomeAssociation> root, OutcomeAssociation oa) {
            
        // attempt to find the association group in the treeview
        for (TreeItem<OutcomeAssociation> treegroup:root.getChildren()) {
            if (treegroup.getValue().getAssignmentGroupName().equals(oa.getAssignmentGroupName())) {
            	// the group matches
            	if (oa.getAssignmentName() == null) {
                    // if this is a assessment group - do nothing
                	return;
                } 
                
                // if this outcome association has an assignment, see if 
                // a matching assignment can be found as a sub-item for this 
                // assignment group
                for (TreeItem<OutcomeAssociation> treeassignment: treegroup.getChildren()) {
                    if (treeassignment.getValue().getAssignmentName().equals(oa.getAssignmentName())) {
                        // An assignment with a matching name has been found
                        if ((oa.getRubricCriterion()==null)&&(oa.getQuestionGroup()==null)) {
                        	// there is no rubric or question group - this is a match - just exit
                        	return;
                        }
                        
                        // This outcome association has a rubric/group/bank criterion,
                        // check to see if a matching one exists in the tree
                        for (TreeItem<OutcomeAssociation> treeLevel3: treeassignment.getChildren()) {
                            if (treeLevel3.getValue().matches(oa)) {
                            	// a match has been found
                                return;
                            }
                        }

                        // here if a level 3 match has not been found - add the new association to the tree
                        CheckBoxTreeItem<OutcomeAssociation> item = new CheckBoxTreeItem<OutcomeAssociation>(oa);
                        item.setIndependent(true);
                        ImageView rubricWarningIcon = new ImageView(warnImage);
                        item.setGraphic(rubricWarningIcon);
                        treeassignment.getChildren().add(0,item);                                                                
                        
                        return;
                    } 
                }
            
                // here if the assignment is not found - add the association to the top of this
                // assignment group
            	OutcomeAssociation oa2 = new OutcomeAssociation(oa.getAssignmentGroupName(),oa.getAssignmentName(),null,null,null);
                CheckBoxTreeItem<OutcomeAssociation> item = new CheckBoxTreeItem<OutcomeAssociation>(oa2);
                item.setExpanded(true);
                item.setIndependent(true);
                ImageView assignmentWarningIcon = new ImageView(warnImage);
                item.setGraphic(assignmentWarningIcon);
                treegroup.getChildren().add(0,item);                        
                return;
            }
        }
        
    	// Here if the top-level association is not found.  Add it to the tree
    	OutcomeAssociation oa1 = new OutcomeAssociation(oa.getAssignmentGroupName(),null,null,null,null);
	    CheckBoxTreeItem<OutcomeAssociation> item = new CheckBoxTreeItem<OutcomeAssociation>(oa1);
	    item.setIndependent(true);
	    ImageView rubricWarningIcon = new ImageView(warnImage);
	    item.setGraphic(rubricWarningIcon);
	    root.getChildren().add(0,item);                                                                
    }

    /* 
     * helper function to create the form
     */
    private void createForm() {
    	setTitle("Add/Edit Outcome");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        setScene(new Scene(grid, 600, 500));
        
        titleField = new TextField();
        titleField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,String oldValue, String newValue) {
            }
        });        
        grid.add(titleField, 0, 1, 4, 1);

        descriptionField = new TextArea();
        descriptionField.setWrapText(true);
        grid.add(descriptionField, 0, 3, 4, 3);
        
        Button doneButton = new Button(" Done ");
        doneButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                CanvasOutcome newOutcome = new CanvasOutcome();
                newOutcome.setTitle(titleField.getText());
                newOutcome.setDescription(descriptionField.getText());
                setOutcomeAssociationsFromTree(newOutcome);
                if (!newOutcome.associationsExist(assignmentGroups)) {
                    // here the outcome has errors - there are some associations
                    // that are invalid - show an error dialog box.
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Invalid Outcome Specification");
                    alert.setHeaderText("Outccome is linked to invalid assignments");
                    alert.setContentText("Please remove the invalid assignment links (shown with warning icon) and try again.");
                    alert.showAndWait();
                    return;
                }
                exitOkay = true;
                outcome = newOutcome;
                close();
            }
        });
        grid.add(doneButton, 0,15);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                close();                
            }
        });
        grid.add(cancelButton, 1,15);

        Label title = new Label("Title");
        grid.add(title, 0, 0);

        CheckBoxTreeItem<OutcomeAssociation> root = new CheckBoxTreeItem<>(new OutcomeAssociation("",null,null,null,null));
        root.setExpanded(true);
        assignmentList = new TreeView<OutcomeAssociation>(root);
        assignmentList.setCellFactory(CheckBoxTreeCell.forTreeView());
        addCanvasAssociationsToTree(root);
        
        // loop to add outcome associations that are not present in the 
        // course assessments - these will be marked with a warning icon.
        ArrayList<OutcomeAssociation> outcomeAssociations = outcome.getAssociations();
        for (OutcomeAssociation oa:outcomeAssociations) {
        	// check L1
        	OutcomeAssociation oa1 = new OutcomeAssociation(oa.getAssignmentGroupName(),null,null,null,null);
       		addMissingAssociation(root,oa1);
       		OutcomeAssociation oa2 = new OutcomeAssociation(oa.getAssignmentGroupName(),oa.getAssignmentName(),null,null,null);
       		addMissingAssociation(root,oa2);
    		addMissingAssociation(root,oa);
        }      

        assignmentList.setRoot(root);
        assignmentList.setShowRoot(false);
        grid.add(assignmentList, 0, 8, 4, 6);
        setCheckBoxes(root, outcome);
    }    

    /*
     * helper function to set check boxes based on outcome association object
     */
    private void setCheckBoxes(CheckBoxTreeItem<OutcomeAssociation>localroot, CanvasOutcome outcome) {
        // set the check boxes based on the outcome object's associations
    	if (outcome.associationExists(localroot.getValue())) {
    		localroot.setSelected(true);
    	}
    	for (TreeItem<OutcomeAssociation> child:localroot.getChildren()) {
    		setCheckBoxes((CheckBoxTreeItem<OutcomeAssociation>)child,outcome);
    	}
    }
    
    /*
     * helper function to set the outcome associations from the tree
     */
    private void setOutcomeAssociationsFromTree(CanvasOutcome oc) {
        // add outcome associations for each box checked in the tree view
        int i = 0;
        oc.getAssociations().clear();
        while (assignmentList.getTreeItem(i)!=null) {
            CheckBoxTreeItem<OutcomeAssociation> item = (CheckBoxTreeItem<OutcomeAssociation>)assignmentList.getTreeItem(i);
            if (item.isSelected()) {
            	// here if the group is checked.  Update the outcome list
                oc.getAssociations().add(new OutcomeAssociation(item.getValue()));
            }
            i++;
        }
    }
    
    /* 
     * helper function to show the form and wait for it to be closed
     */
    public CanvasOutcome editOutcome() {
        showAndWait();

        if (exitOkay) {
            return outcome;
        } else {
            return null;
        }
    }
}
