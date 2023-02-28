package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * representation of a canvas submission history record. 
 */
public class CanvasSubmissionHistoryRecord {
    final boolean  correct;
    final double   points;
    final String   question_id;
    final String   answer_id;

    /**
     * constructor - create and initialize the instance using information 
     *    provided in the JSON object.
     * @param obj - a JSON object that contains initialization information.
     */
    CanvasSubmissionHistoryRecord(JsonObject obj) {
        // construct the Submission object from the provided object
        correct = obj.getBoolean("correct");
        points =  obj.getDouble("points");
        question_id = obj.getValue("question_id");
        answer_id = obj.getValue("answer_id");
    }
    
    /**
     * returns true of the question was answered correctly, otherwise, false
     */
    public boolean getCorrect() {return correct;}

    /**
     * returns the canvas id of the question
     */
    public String getQuestionId() {return question_id;}

    /**
     * returns the number of points the student earned (including partial credit)
     */
    public double getPoints() {return points;}

    /**
     * returns the canvas ID of the student's answer
     */
    public String getAnswerId() {return answer_id;}
}
