package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of a single canvas quiz question
 */
public class CanvasQuizQuestion {
    String  id;            
    String  quiz_id;       
    int     position;      
    String  question_name; 
    String  question_type;
    double  points_possible;
    String  correct;
    String  quiz_group_id;
    
    /**
     * constructor - initialize the canvas quiz question from the provided JsonObject
     * @param obj - a json object (formatted like Canvas LMS response) that includes
     *              information to initialize the object instance.
     */
    CanvasQuizQuestion(JsonObject obj) {
        // construct the object from the provided json object
        id = obj.getValue("id");
        quiz_id = obj.getValue("quiz_id");
        position = obj.getInteger("position");
        question_name = obj.getValue("question_name");
        question_type = obj.getValue("question_type");
        points_possible = obj.getDouble("points_possible");
        quiz_group_id = obj.getValue("quiz_group_id");
        correct = obj.getValue("correct");
    }

    /**
     * constructor - initialize the quiz question by querying the canvas LMS api
     * @param course_id - the ID of the associated course
     * @param quiz_id - the id of the associated quiz
     * @param question_id - the id of the question
     */
    CanvasQuizQuestion(String course_id, String quiz_id, String question_id) {
        try {
            JsonObject obj;
            obj = (JsonObject) RequesterSso.apiGetRequest("courses/"+course_id+"/quizzes/"+quiz_id+"/questions/"+question_id+"?id="+question_id);
            if (obj != null) {
                id = obj.getValue("id");
                this.quiz_id = obj.getValue("quiz_id");
                position = obj.getInteger("position");
                question_name = obj.getValue("question_name");
                question_type = obj.getValue("question_type");
                points_possible = obj.getDouble("points_possible");
                correct = obj.getValue("correct");          		
            }
        } catch (IOException ex) {
            Logger.getLogger(Assignments.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * returns the position that the question appears in the quiz
     */
    public int getPosition() {return position;}

    /**
     * returns the name of the question
     */
    public String getName() {return question_name;}

    /**
     * returns the type of question (see canvas lms api documentation)
     */
    public String getType() {return question_type;}

    /**
     * returns the number of points possible for this question.
     */
    public double getPointsPossible() {return points_possible;}

    /**
     * returns if the student got the correct answer for the question
     * (this will not represent partial credit for multi-selection questions)
     */
    public String getCorrect() {return correct;}

    /**
     * returns the quiz group id that the question is associated with (if any)
     */
    public String getQuizGroupId() {return quiz_group_id;}
}
