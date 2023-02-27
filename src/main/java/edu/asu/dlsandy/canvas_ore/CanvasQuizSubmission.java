package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * Representation of a student's submission for a canvas quiz.  This object
 * includes additional information that is not part of the related assignment
 * submission.  For more information, consult the Canvas LMS API
 */
public class CanvasQuizSubmission {
    String  id;            
    String  quiz_id;
    String  user_id;
    int     attempt; 
    double  score;
    double  kept_score;

    /**
     * constructor - initialize the instance from the provided JsonObject
     * @param obj - a JsonObject that includes information about the quiz submission
     */
    CanvasQuizSubmission(JsonObject obj) {
        // construct the object from the provided json object
        id = obj.getValue("id");
        quiz_id = obj.getValue("quiz_id");
        user_id = obj.getValue("user_id");
        attempt = obj.getInteger("attempt");
        score = obj.getDouble("score");
        kept_score = obj.getDouble("kept_score");
    }
    
    /**
     * Returns the attempt number that this submission represents.  
     */
    public int getAttempt() {return attempt;}

    /**
     * returns the id of this quiz submission
     */
    public String getId() {return id;}
    
    /**
     * returns the id of the student that this submission is related to  
     */
    public String getUserId() {return user_id;}
    
    /**
     * returns the id of the quiz that this submission is related to 
     */
    public String getQuizId() {return quiz_id;}
    
    /**
     * returns the score that the student earned for this submission 
     */
    public double getScore() {return score;}
    
    /**
     * returns true if this score was kept for the student's grade, otherwise false.
     */
    public double getKeptScore() {return kept_score;}
}
