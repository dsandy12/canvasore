package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.util.Map;
import java.util.TreeMap;

/**
 *  Representation of a single assignment submission
 */
public class CanvasSubmission {
    String  id;            
    double  score;         
    String  user_id;       
    boolean grade_matches; 
    double  entered_score; 
    boolean late;         
    int     attempt;
    // This tree map has the rubric id as the key  
    TreeMap<String, Double> rubric_scores;

    /**
     * Constructor - initialize the instance from information contained in the 
     *               specified JsonObject.
     * @param obj - JsonObject that contains the initialization information
     */
    CanvasSubmission(JsonObject obj) {
        id = obj.getValue("id");
        score = obj.getDouble("score");
        user_id = obj.getValue("user_id");
        attempt = obj.getInteger("attempt");
        grade_matches = true;
        if (obj.containsKey("grade_matches_current_submission")) {
            grade_matches = obj.getBoolean("grade_matches_current_submission");
        }
        entered_score = score;
        rubric_scores = new TreeMap<>();
        if (obj.containsKey("entered_score"))
        {
            // this line takes care of grade overrides
            score = entered_score;
            entered_score = obj.getDouble("entered_score");
        }
        late = obj.getBoolean("late");
        JsonObject json_rubric_scores = (JsonObject)obj.get("rubric_assessment");
        if (json_rubric_scores != null) {
            // the json rubric scores will be an object where each key is the ID of 
            // a rubric assessment item, and the value will be an object that 
            // contains rating_id, comments and points.
            for(Map.Entry<String,JsonAbstractValue> entry : json_rubric_scores.entrySet()) {
               String criterion_id = entry.getKey();
               JsonObject rubric_obj = (JsonObject)entry.getValue();
               if (rubric_obj==null) continue;
               rubric_scores.put(criterion_id, rubric_obj.getDouble("points"));
            }            
        }
    }
    
    /**
     * returns true if this grade matches the one that is used in the gradebook, otherwise, false
     */
    public boolean getGradeMatches() {return grade_matches;}

    /**
     * returns the canvas id of this submission
     */
    public String getId() {return id;}

    /**
     * returns the score for this submission
     */
    public double getScore() {return score;}

    /**
     * returns the canvas user id associated with this submission
     */
    public String getUserId() {return user_id;}

    /**
     * returns the score entered for this submission (see Canvas LMS API for more information)
     */
    public double getEnteredScore() {return entered_score;}

    /**
     * returns the number of this attempt (first, second, etc)
     */
    public int getAttempt() {return attempt;}

    /**
     * returns the rubric scores for this assignment.  Each key represents the rubric row
     * id and the value represents the points earned for that row.
     */
    public TreeMap<String,Double> getRubricScores() {return rubric_scores;}    
}
