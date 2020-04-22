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
 * represents a list of all outcomes for a course
 */
public class CanvasOutcomes extends ArrayList<CanvasOutcome> {
	private static final long serialVersionUID = 1L;
	String course_id;
    String course_name;
    
    /**
     * constructor - initialize the outcome list by loading any outcomes that exist within
     * the specified course in canvas.
     * 
     * @param course_id - the canvas course id
     * @param course_name - the name of the course as specified in canvas
     */
    public CanvasOutcomes(String course_id, String course_name)  {
        this.course_id = course_id;
        this.course_name = course_name;
        try {
            JsonArray outcome_links;
            outcome_links = (JsonArray)RequesterSso.apiGetRequest("courses/"+course_id+"/outcome_group_links?per_page=100");
            if (outcome_links != null) {
                for (int i=0;i<outcome_links.size();i++) {
                    JsonObject json_outcome = (JsonObject)RequesterSso.apiGetRequest("outcomes/"+outcome_links.get(i).getValue("outcome.id")+"?per_page=100");
                    CanvasOutcome outcome = new CanvasOutcome(json_outcome);
                    add(outcome);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  

    /**
     * constructor - initialize the outcome list from the specified json object
     *
     * @param json_obj - the json object to initialize from
     */
    public CanvasOutcomes(JsonObject json_obj)  {
        if (json_obj==null) return;
        this.course_id = json_obj.getValue("course_id");
        this.course_name = json_obj.getValue("course_name");

        JsonArray json_outcomes;
        if ((json_outcomes = (JsonArray)json_obj.get("outcomes"))==null) return;
        for (int i=0;i<json_outcomes.size();i++) {
            JsonObject json_outcome = (JsonObject)json_outcomes.get(i);
            CanvasOutcome outcome = new CanvasOutcome(json_outcome);
            add(outcome);
        }
    }  
    
    /**
     * returns the canvas course id that the outcomes are related to
     */
    public String getCourseId() {return course_id;}

    /**
     * returns the course name that the outcomes are related to
     */
    public String getCourseName() {return course_name;}

    /**
     * set the canvas course id for the outcomes
     */
    public void setCourseId(String course_id) {this.course_id = course_id;}

    /**
     * set the course name for the outcomes
     */
    public void setCourseName(String course_name) {this.course_name = course_name;}
    
    /**
     * return a JSON representation of this object
     */
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.put("course_id",new JsonValue(course_id));
        obj.put("course_name",new JsonValue(course_name));
        JsonArray arry = new JsonArray();
        for (CanvasOutcome outcome:this) {
            JsonObject outcome_obj = outcome.toJson();
            arry.add(outcome_obj);
        }
        obj.put("outcomes",arry);
        return obj;
    }
}
