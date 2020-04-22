package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.util.ArrayList;

/**
 *  The representation of an assignment group as stored in the Canvas LMS.
 */
public class AssignmentGroup {
    String id;
    String name;
    Assignments assignments;
    int drop_lowest;
    int drop_highest;

    /**
     * Constructor for the assignment group.
     * @param obj - a JsonObject of an assignment group as received from the Canvas LMS
     */
    public AssignmentGroup(JsonObject obj) {
        
        // initialize the AssignmentGroup from the given canvas json object.
        name = obj.getValue("name");
        id = obj.getValue("id");
        drop_lowest = obj.getInteger("rules.drop_lowest");
        drop_highest = obj.getInteger("rules.drop_highest");
        JsonArray jsonArray = (JsonArray)obj.get("assignments");
        if (jsonArray!=null) assignments = new Assignments(jsonArray,name);
    }        
    
    /**
     * returns the name of the assignment group
     */
    public String getName() {return name;}

    /**
     * returns an Assignments object that contains all the assignments in the group
     */
    public Assignments getAssignments() {return assignments;}

    /**
     * Loads the grades for all the assignments in the assignment group.
     * Returns true on success, otherwise false.
     */
    public boolean loadGrades() {return assignments.loadGrades();} 
        
    /**
     * get the outcome points for the specified student and outcome association.  Apply
     * assignment group rules for this group if they exist.
     * 
     * @param oa - the outcome association (eg. this group, an assignment, rubric item or question bank) 
     *             to get points for
     * @param student_id - the canvas student id to get the points for
     * @return - the number of points the student scored toward the specific outcome 
     *           related to this assignment group.
     */
    public double getStudentOutcomePoints(OutcomeAssociation oa, String student_id) {
        double outcome_points = 0;
        
        // if this evaluation is for a specific assignment, process the assignment
        if (oa.getAssignmentName() != null) {
            return assignments.getAssignmentByName(oa.getAssignmentName()).getStudentOutcomePoints(oa, student_id); 
        }
        
        // otherwise, return the points for this assignment group, applying any rules that have
        // been specified.
        ArrayList<Double> results = new ArrayList<>();
        for (Assignment assignment:assignments) {
            results.add(assignment.getStudentOutcomePoints(null,student_id));
        }
        results.sort(null);
        int i = 0;
        for (Double d:results) {
            if ((i>=drop_lowest)&&(i<results.size()-drop_highest)) outcome_points+=d;
            i++;
        }
        return outcome_points;
    }

    /**
     * get the maximum outcome points that a student could earn for the specified outcome association.
     * 
     * @param oa - the outcome association (eg. this group, an assignment, rubric item or question bank) 
     *             to get points for
     * @return - the number of points that could be earned toward the specific outcome 
     *           related to this assignment.
     */
    public double getMaximumOutcomePoints(OutcomeAssociation oa) {
        double outcome_points = 0;
        
        // if this evaluation is for a specific assigment, process the assignment
        if (oa.getAssignmentName() != null) {
            return assignments.getAssignmentByName(oa.getAssignmentName()).getMaximumOutcomePoints(oa); 
        }
        
        // otherwise, return the points for this assignment group, applying any rules that have
        // been specified.
        ArrayList<Double> results = new ArrayList<>();
        for (Assignment assignment:assignments) {
            results.add(assignment.getMaximumOutcomePoints(null));
        }
        int i = 0;
        results.sort(null);
        for (Double d:results) {
            if ((i>=drop_lowest)&&(i<results.size()-drop_highest)) outcome_points+=d;
            i++;
        }
        return outcome_points;
    }
    
    /**
     * Returns true if the project group contains an assignment with a specified name
     * and specified rubric criterion name (if given).  Otherwise, returns false
     */
    public boolean contains(String assignmentName, String rubricCriterionName) {
        for (Assignment assignment:assignments) {
            if (assignment.getName().equals(assignmentName)) {
                if (rubricCriterionName == null) {
                    return true;
                }
                return assignment.contains(rubricCriterionName);
            }
        }
        return false;                
    }
}
