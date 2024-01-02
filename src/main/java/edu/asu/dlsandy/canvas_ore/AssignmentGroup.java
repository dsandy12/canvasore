package edu.asu.dlsandy.canvas_ore;
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
    final String id;
    final String name;
    Assignments assignments;
    final int drop_lowest;
    final int drop_highest;

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
    public void loadGrades() {
        assignments.loadGrades();
    }

    /**
     * get the outcome points for the specified student and outcome association.  Apply
     * assignment group rules for this group if they exist.
     * 
     * @param oa - the outcome association (eg.. this group, an assignment, rubric item or question bank)
     *             to get points for
     * @param student_id - the canvas student id to get the points for
     * @return - the number of points the student scored toward the specific outcome 
     *           related to this assignment group.
     */
    public double getStudentOutcomePoints(OutcomeAssociation oa, String student_id) {
        double outcome_points_max = 0.0;
        double outcome_points_min = 0.0;
        double max_possible_points = getMaximumOutcomePoints(oa);

        // if this evaluation is for a specific assignment, process the assignment
        if (oa.getAssignmentName() != null) {
            return assignments.getAssignmentByName(oa.getAssignmentName()).getStudentOutcomePoints(oa, student_id); 
        }
        
        // otherwise, return the points for this assignment group, applying any rules that have
        // been specified.
        // Note that if the student did not submit one or more assignments, in the group, then the determination
        // of the grade for the group will be determined by:
        // Assume the unsubmitted assignments are worth 0 points - if after applying rules, the result is >= 70 %
        //      return the result
        // Assume the unsubmitted assignments are worth 100 points - if after applying the rules, the result is <70%
        //      return the result as calculated using unsubmitted assignments worth 0;
        // Otherwise, return Nan -> result cannot be determined
        ArrayList<Double> results_min = new ArrayList<>();
        ArrayList<Double> results_max = new ArrayList<>();
        double unknownPointsMax = 0;
        for (Assignment assignment:assignments) {
            double assignmentResult = assignment.getStudentOutcomePoints(null,student_id);
            if (Double.isNaN(assignmentResult)) {
                results_min.add(0.0);
                results_max.add(assignment.getPointsPossible());
            } else {
                results_min.add(assignment.getStudentOutcomePoints(null, student_id));
                results_max.add(assignment.getStudentOutcomePoints(null, student_id));
            }
        }
        results_min.sort(null);
        results_max.sort(null);
        int i = 0;
        for (Double d:results_min) {
            if ((i>=drop_lowest)&&(i<results_min.size()-drop_highest)) {
                outcome_points_min += d;
            }
            i++;
        }
        i = 0;
        for (Double d:results_max) {
            if ((i>=drop_lowest)&&(i<results_min.size()-drop_highest)) {
                outcome_points_max+=d;
            }
            i++;
        }
        // student demonstrated competency even with missing assignments
        if (outcome_points_min/max_possible_points >= 0.70) return outcome_points_min;

        // student could not demonstrate competency even if all remaining assignments were 100%
        if (outcome_points_max/max_possible_points < 0.70)  return outcome_points_min;

        // the result is indeterminate - student did not turn in enough assignments
        return Double.NaN;
    }

    /**
     * get the maximum outcome points that a student could earn for the specified outcome association.
     * 
     * @param oa - the outcome association (eg.. this group, an assignment, rubric item or question bank)
     *             to get points for
     * @return - the number of points that could be earned toward the specific outcome 
     *           related to this assignment.
     */
    public double getMaximumOutcomePoints(OutcomeAssociation oa) {
        double outcome_points = 0;
        
        // if this evaluation is for a specific assignment, process the assignment
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
