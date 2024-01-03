package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.util.ArrayList;

/**
 *
 * A representation of a single course outcome.  The outcome specifies canvas assignments, 
 * rubrics, and question bank associations that are to be used in assessing the outcome.
 */
public class CanvasOutcome {
    private String title;
    private String description;
    private final ArrayList<OutcomeAssociation> associations;

    /*
     * constructor - initialize a blank outcome
     */
    public CanvasOutcome() {
        title = "";
        description = "";
        associations = new ArrayList<>();
    }

    /**
     * constructor - initialize from a Json object.
     * @param obj - the JSON object to initialize from.
     */
    public CanvasOutcome(JsonObject obj) {
        title = obj.getValue("title");
        description = obj.getValue("description").replace("\\u003cp\\u003e", "").replace("\\u003c/p\\u003e", "\n");
        associations = new ArrayList<>();
        if (obj.get("associations")==null) return;
        for (JsonAbstractValue associationObject:(JsonArray)obj.get("associations")) {
            OutcomeAssociation association = new OutcomeAssociation(
                    associationObject.getValue("assignment_group_name"),
                    associationObject.getValue("assignment_name"),
                    associationObject.getValue("rubric_criterion"),
                    associationObject.getValue("question_group"),
                    associationObject.getValue("question_bank"),
                    (associationObject.getValue("exceeds_threshold")!=null)?associationObject.getDouble("exceeds_threshold"):-1.0,
                    (associationObject.getValue("demonstrates_threshold")!=null)?associationObject.getDouble("demonstrates_threshold"):-1.0
            );
            associations.add(association);           
        }
    }
    
    
    /***************************************************************************
     * This function checks to make sure that all the associations in the
     * outcome object exist within a course.  This is done by checking to make
     * sure that each outcome assessment association matches an assignment 
     * within the AssignmentGroups parameter.
     * 
     * @param groups - the assignment groups object that will be scanned for 
     *                 assignments matching those in the outcome object.
     * @return true if all assignments within the outcome match assignments in 
     *         the course's assignment groups.  Otherwise, false is returned.
     */
    public boolean associationsExist(AssignmentGroups groups) {
        for (OutcomeAssociation oa:associations) {
            // for each association in the outcome, check to make sure that it 
            // exists an assignment in one of the assignment groups
            if (!groups.contains(oa.getAssignmentGroupName(), oa.getAssignmentName(), oa.getRubricCriterion())) return true;
        }
        return false;
    }

    /**
     * returns true if the specified association exists within the outcome, otherwise false.
     */
    public boolean associationExists(OutcomeAssociation oa) {
        for (OutcomeAssociation a:associations) {
            if (a.matches(oa)) return true;
        }
        return false;
    }

    public OutcomeAssociation getAssociation(OutcomeAssociation oa) {
        for (OutcomeAssociation a:associations) {
            if (a.matches(oa)) return a;
        }
        return null;
    }

    /** 
     * creates a representation of the outcome in JSON format
     * @return - the JSON representation of the outcome
     */
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        
        obj.put("title", new JsonValue(title));
        obj.put("description", new JsonValue(description.trim()));
        JsonArray array = new JsonArray();
        for (OutcomeAssociation association:associations) {
            JsonObject association_obj = new JsonObject();
            association_obj.put("assignment_group_name",new JsonValue(association.getAssignmentGroupName()));
            if (association.getAssignmentName()!=null) {
                association_obj.put("assignment_name",new JsonValue(association.getAssignmentName()));
            }
            if (association.getRubricCriterion()!=null) {
                association_obj.put("rubric_criterion",new JsonValue(association.getRubricCriterion()));
            }
            if (association.getQuestionGroup()!=null) {
                association_obj.put("question_group",new JsonValue(association.getQuestionGroup()));
            }
            if (association.getQuestionBank()!=null) {
                association_obj.put("question_bank",new JsonValue(association.getQuestionBank()));
            }
            association_obj.put("exceeds_threshold", new JsonValue(Double.toString(association.getExceedsThreshold())));
            association_obj.put("demonstrates_threshold", new JsonValue(Double.toString(association.getDemonstratesThreshold())));
            array.add(association_obj);
        }
        obj.put("associations", array);
        return obj;
    }
    
    /**
     * returns the title of the outcome
     */
    public String getTitle() {return title;}

    /** 
     * returns the description of the outcome
     */
    public String getDescription() {return description;}

    /**
     * returns an array list of the outcome associations for this outcome.
     */
    public ArrayList<OutcomeAssociation> getAssociations() {return associations;}
    
    /**
     * set the title for the outcome
     * @param title - the new title
     */
    public void setTitle(String title) {this.title = title;}
    
    /**
     * set the description for this outcome
     * @param description - the new description
     */
    public void setDescription(String description) {this .description = description;}
}
