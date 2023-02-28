package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Representation of a list of all Canvas question groups associated with a specific quiz 
 */
public class CanvasQuestionGroups extends ArrayList<CanvasQuestionGroup> {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor - initialize the question groups by retrieving the information from Canvas LMS
	 * @param course_id - the ID of the course the question groups are associated with
	 * @param quiz - the ID of the quiz the question groups are associated with.
	 */
	public CanvasQuestionGroups(String course_id, CanvasQuiz quiz)  {
		// Get the quiz submissions.  
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
			CanvasQuizSubmissions quizSubmissions = new CanvasQuizSubmissions(course_id, quiz.getId());

		// Find a submission that has every question answered
		for (CanvasQuizSubmission qs:quizSubmissions) {
		    // for each submission, get the questions asked
			@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
				CanvasQuizQuestions questions = new CanvasQuizQuestions(qs.getId());
		
			if (questions.size() == quiz.getQuestionCount()) {
				// create a set of unique question group Ids
				HashSet<String> groupIds = new HashSet<>();
				for (CanvasQuizQuestion qq:questions) {
					if (qq.getQuizGroupId() != null) {
						groupIds.add(qq.getQuizGroupId());
					}
				}
				
				// for each group id identified, create a quiz group object
				for (String groupId:groupIds) {
					CanvasQuestionGroup qg = new CanvasQuestionGroup(course_id,quiz.getId(),groupId);
					add(qg);
				}
				break;
			}
		}
    }  
    
	/**
	 * returns the question group with the matching id, otherwise null.
	 * @param id - the id of the question group to return
	 * @return the question group with an id that matches the input parameter.  Null if no match
	 *    is found.
	 */
    public CanvasQuestionGroup getById(String id) {
    	for (CanvasQuestionGroup qg:this) {
    		if (qg.getId().equals(id)) {
    			return qg;
    		}
    	}
    	return null;
    }
    
    /**
     * load the grades for each question group in the list
     *
     * @param submissions - all student submissions for the associated quiz
     */
	public void loadGrades(CanvasQuizSubmissions submissions) {
    	boolean result = true;
		for (CanvasQuestionGroup g:this) {
    		result = result & g.loadGrades(submissions);
    	}
    }
	
    /**
     * returns the number of points that a student earned against the specified outcome
     *    association (in this case, the oa should always be a quiz group/bank pair).
     * @param oa - the outcome association to use when computing the student score
     * @param student_id - the id of the student
     * @return the number of points the student scored associated with the outcome
     */
    public double getStudentOutcomePoints(OutcomeAssociation oa, String student_id) {
        // Find the Group that matches the association
        for (CanvasQuestionGroup g:this) {
        	if (g.getBankId()==null) {
        		if (g.getName().equals(oa.getQuestionGroup())) {
        			// bank has been found - return the result
        			return g.getStudentOutcomePoints(student_id);
        		} 
        	} else {
            	if ((g.getName().equals(oa.getQuestionGroup())) && (g.getBank().getTitle().equals(oa.getQuestionBank()))) {
            		// bank has been found - return the result
            		return g.getStudentOutcomePoints(student_id);
            	}         		
        	}
        }
    	return 0;
    }

    /**
     * returns the maximum number of points that a student can earn against a specified outcome
     *    association (in this case, the oa should always be a quiz group/bank pair).
     * @param oa - the outcome association to use when computing the max score.
     * @return the maximum points a student can score associated with the outcome
     */
    public double getMaximumOutcomePoints(OutcomeAssociation oa) {
        // Find the Group that matches the association
        for (CanvasQuestionGroup g:this) {
        	if (g.getBankId()==null) {
        		if (g.getName().equals(oa.getQuestionGroup())) {
        			// bank has been found - return the result
        			return g.getMaximumOutcomePoints();
        		} 
        	} else {
            	if ((g.getName().equals(oa.getQuestionGroup())) && (g.getBank().getTitle().equals(oa.getQuestionBank()))) {
            		// bank has been found - return the result
            		return g.getMaximumOutcomePoints();
            	}         		
        	}
        }
    	return 0;
    }    
}
