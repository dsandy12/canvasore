package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  The representation of the assignment groups in a course as stored in the Canvas LMS.
 */
public class AssignmentGroups extends ArrayList<AssignmentGroup> {
	private static final long serialVersionUID = 1L;

    /**
     * Constructor for the assignment groups.  Reads the information from Canvas
     * @param course_id - The Canvas LMS course ID to read the assignment groups from
     */
    public AssignmentGroups(String course_id)  {
        try {
            // perform the request to get the assignment groups information from canvas
        	JsonArray assignmentGroups;
            assignmentGroups = (JsonArray)RequesterSso.apiGetRequest("courses/"+course_id+"/assignment_groups?include[]=assignments");
            if (assignmentGroups != null) {
                for (JsonAbstractValue obj:assignmentGroups) {
                    // create and add the courses, configuring them from the canvas request data
                    AssignmentGroup assignmentGroup = new AssignmentGroup((JsonObject)obj);
                    add(assignmentGroup);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
    /**
     * Loads the grades for all the assignment groups.
     * Returns true on success, otherwise false.
     */
    public boolean loadGrades() {
        boolean result = true;
        for (AssignmentGroup ag:this) {
            result |= ag.loadGrades();
        } 
        return result;
    }
    
    /**
     * get the outcome points for all assignments for the specified student and outcome. 
     * 
     * @param outcome - the outcome to measure
     * @param student_id - the canvas student id to get the points for
     * @return - the number of points the student scored toward the specific outcome.
     */
    public double getStudentOutcomePoints(CanvasOutcome outcome, String student_id) {
        double outcome_points = 0;
        for (AssignmentGroup assignment_group:this) {
            for (OutcomeAssociation association:outcome.getAssociations()) {
                if (assignment_group.getName().equals(association.getAssignmentGroupName())) {
                    // here the outcome assignment group matches the current assignment group
                    outcome_points += assignment_group.getStudentOutcomePoints(association,student_id);
                }
            }
        }
        return outcome_points;
    }

    /**
     * get the outcome points for the specified student and outcome association.   
     * 
     * @param oa - the outcome association (eg. group, assignment, rubric item or question bank) 
     *             to get points for
     * @param student_id - the canvas student id to get the points for
     * @return - the number of points the student scored toward the specific outcome.
     */
    public double getStudentAssignmentPoints(OutcomeAssociation oa, String student_id) {
        AssignmentGroup assignmentGroup = getFromName(oa.getAssignmentGroupName());
        if (assignmentGroup == null) return -1;
        return assignmentGroup.getStudentOutcomePoints(oa,student_id);
    }

    /**
     * get the percent score for the specified student and outcome association.   
     * 
     * @param oa - the outcome association (eg. group, assignment, rubric item or question bank) 
     *             to get points for
     * @param student_id - the canvas student id to get the points for
     * @return - the percent the student scored toward the specific outcome.
     */
    public double getStudentAssignmentPercent(OutcomeAssociation oa, String student_id) {
        AssignmentGroup assignmentGroup = getFromName(oa.getAssignmentGroupName());
        if (assignmentGroup == null) return -1;
        // get the maximum number of points
        double max_points =  assignmentGroup.getMaximumOutcomePoints(oa);
        if (max_points == 0) return 0;
        // return student points divided by the maximum points
        return assignmentGroup.getStudentOutcomePoints(oa,student_id)/max_points;
    }

    /**
     * get the outcome points for all assignments for the specified student and outcome. 
     * 
     * @param outcome - the outcome to measure
     * @param student_id - the canvas student id to get the points for
     * @return - percentage the student scored toward the specific outcome.
     */
    public double getStudentAverageOutcomePercent(CanvasOutcome outcome, String student_id) {
        double percent_sum = 0;
        double count = 0;
        for (AssignmentGroup assignment_group:this) {
            for (OutcomeAssociation association:outcome.getAssociations()) {
                if (assignment_group.getName().equals(association.getAssignmentGroupName())) {
                    // here the outcome assignment group matches the current assignment group
                    double outcome_points = assignment_group.getStudentOutcomePoints(association,student_id);
                    double max_points =  assignment_group.getMaximumOutcomePoints(association);
                    if (max_points!=0) {
                        percent_sum += outcome_points/max_points;
                        count ++;
                    }
                }
            }
        }
        if (count!=0) return percent_sum/count;
        return 0;
    }
    
    /**
     * get the maximum outcome points for all assignments. 
     * 
     * @param outcome - the outcome to measure
     * @return - the maximum number of outcome points that can be achieved.
     */
    public double getMaximumOutcomePoints(CanvasOutcome outcome) {
        double outcome_points = 0;
        for (AssignmentGroup assignment_group:this) {
            for (OutcomeAssociation association:outcome.getAssociations()) {
                if (assignment_group.getName().equals(association.getAssignmentGroupName())) {
                    // here the outcome assignment group matches the current assignment group
                    outcome_points += assignment_group.getMaximumOutcomePoints(association);
                }
            }
        }
        return outcome_points;
    }
    
    /**
     * Returns the assignment group with the specified name, otherwise null.
     */
    public AssignmentGroup getFromName(String assignmentGroupName) {
        for (AssignmentGroup assignment_group:this) {
            if (assignment_group.getName().equals(assignmentGroupName)) return assignment_group;
        }
        return null;        
    } 
    
    /**
     * Returns true if an assignment (and optionally assignment rubric) exists within a specified
     * assignment group, otherwise, returns false.
     */
    public boolean contains(String assignmentGroupName, String assignmentName, String rubricCriterionName) {
        for (AssignmentGroup assignment_group:this) {
            if (assignment_group.getName().equals(assignmentGroupName)) {
                if (assignmentName == null) {
                    return true;
                }
                return assignment_group.contains(assignmentName, rubricCriterionName);
            }
        }
        return false;                
    }
}
