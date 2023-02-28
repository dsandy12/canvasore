package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of Canvas quiz object
 */
public class CanvasQuiz {
    String  id;  
    String  title;
    int     question_count;
    CanvasQuestionGroups groups;
    
    /**
     * constructor - initialize the quiz object from the Canvas LMS api
     * @param course_id - the course id associated with the quiz
     * @param quiz_id - the ID of the quiz
     */
    CanvasQuiz(String course_id,String quiz_id) {
    	// Run the query to get the quiz info
        try {
            JsonObject obj;
            obj = (JsonObject) RequesterSso.apiGetRequest("courses/"+course_id+"/quizzes/"+quiz_id);
        	id = Objects.requireNonNull(obj).getValue("id");
        	title = obj.getValue("title");
        	question_count = obj.getInteger("question_count");
        	groups = new CanvasQuestionGroups(course_id, this);
        } catch (IOException ex) {
            Logger.getLogger("Quiz").log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * load the quiz group results from canvas
     *
     * @param course_id - the id of the course associated with the quiz
     */
	public void loadGrades(String course_id) {
    	CanvasQuizSubmissions quizSubmissions = new CanvasQuizSubmissions(course_id, id);
        groups.loadGrades(quizSubmissions);
    }
	
	/**
	 * returns the id for the quiz
	 */
    public String getId() {return id;}

    /**
     * returns the title of the quiz
     */
    public String getTitle() {return title;}

    /**
     *  returns the number of questions in the quiz
     */
    public int getQuestionCount() {return question_count;}

    /**
     * returns a list of question groups associated with the quiz
     */
    public CanvasQuestionGroups getQuestionGroups() {return groups;}
}