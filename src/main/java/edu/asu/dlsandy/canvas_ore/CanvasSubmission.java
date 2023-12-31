package edu.asu.dlsandy.canvas_ore;
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
    final String  id;
    double  score;
    final String  user_id;
    boolean grade_matches; 
    double  entered_score; 
    final boolean late;
    final int     attempt;
    // This tree map has the rubric row id as the key and the user score for the value
    final TreeMap<String, Double> rubric_scores;

    // This tree map holds the rubric rating IDs associated with each rubric row
    final TreeMap<String, String> rubric_rating_ids;
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
        rubric_rating_ids = new TreeMap<>();

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
            for(Map.Entry<String, JsonAbstractValue> entry : json_rubric_scores.entrySet()) {
               String criterion_id = entry.getKey();
               JsonObject rubric_obj = (JsonObject)entry.getValue();
               if (rubric_obj==null) continue;
               rubric_scores.put(criterion_id, rubric_obj.getDouble("points"));
               rubric_rating_ids.put(criterion_id, rubric_obj.getValue("rating_id"));
            }
        }
    }
    
    /**
     * returns true if this grade matches the one that is used in the grade book, otherwise, false
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
     * returns the rubric scores for this assignment.  Each key represents the rubric criteria
     * (row) id and the value represents the points earned for that row.
     */
    public TreeMap<String,Double> getRubricScores() {return rubric_scores;}

    /**
     * returns the rubric ratings for this assignment.  Each key represents the rubric criteria
     * (row) id and the value represents the id of the rating that was selected for that row.
     */
    public TreeMap<String,String> getRubricRatingIds() {return rubric_rating_ids;}

}
