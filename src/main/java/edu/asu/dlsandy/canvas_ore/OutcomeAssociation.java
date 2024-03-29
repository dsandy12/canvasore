package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


/**
 * Representation of an association (mapping) between a course outcome and a specific
 * assessed item.  Assessed items can be: 
 * 1. an assignment group in canvas
 * 2. a single assignment or quiz in canvas
 * 3. a rubric item in canvas
 * 4. a quiz group of questions in canvas
 */
    public class OutcomeAssociation {
        public static final double DEFAULT_EXCEEDS_THRESHOLD = 0.90;
        public static final double DEFAULT_DEMONSTRATES_THRESHOLD = 0.70;

        private final String assignmentGroupName;
        private String assignmentName;
        private String rubricCriterion;
        private String questionGroup;       // used for quizzes only
        private String questionBank;        // used for quizzes only

        private double exceedsThreshold;        // a percentage score at which exceptional performance is demonstrated
        private double demonstratesThreshold;   // a percentage score at which competency has been demonstrated
        
        /**
         * constructor - initialize the outcome association based on the provided parameters.
         * @param assignmentGroupName - the name of the assignment group in canvas (required)
         * @param assignmentName - the name of the assignment (optional if association is for assignment group only)
         * @param rubricCriterion - the name of the rubric criterion (optional if association is for the 
         *                          assignment group, or an entire assignment, or if the association is for a 
         *                          quiz question group)
         * @param questionGroup - the name of the quiz question group (optional if the association is not for
         *                        a specific set of questions in a quiz).  Used in conjunction with the question Bank parameter
         * @param questionBank - the name of the question bank associated with the questionGroup parameter.
         */
        public OutcomeAssociation(String assignmentGroupName, String assignmentName, String rubricCriterion, String questionGroup, String questionBank, double exceedsThreshold, double demonstratesThreshold) {
            this.assignmentGroupName = assignmentGroupName;
            if ((assignmentName!=null) && !(assignmentName.equals(""))) {
                this.assignmentName = assignmentName;
            }
            if ((rubricCriterion!=null) && !(rubricCriterion.equals(""))) {
                this.rubricCriterion = rubricCriterion;
            }
            if ((questionGroup!=null) && !(questionGroup.equals(""))) {
                this.questionGroup = questionGroup;
            }
            if ((questionBank!=null) && !(questionBank.equals(""))) {
                this.questionBank = questionBank;
            }
            this.exceedsThreshold = this.DEFAULT_EXCEEDS_THRESHOLD;
            if ((exceedsThreshold >= 0)&&(exceedsThreshold<=1.0)) {
                this.exceedsThreshold = exceedsThreshold;
            }
            this.demonstratesThreshold = this.DEFAULT_DEMONSTRATES_THRESHOLD;
            if ((demonstratesThreshold >= 0)&&(demonstratesThreshold<=1.0)) {
                this.demonstratesThreshold = demonstratesThreshold;
            }
            if (this.demonstratesThreshold>=this.exceedsThreshold) {
                this.exceedsThreshold = this.DEFAULT_EXCEEDS_THRESHOLD;
                this.demonstratesThreshold = this.DEFAULT_DEMONSTRATES_THRESHOLD;
            }

        }

        /**
         * copy constructor
         */
        public OutcomeAssociation(OutcomeAssociation oa) {
            assignmentGroupName = oa.assignmentGroupName;
            assignmentName = oa.assignmentName;
            rubricCriterion = oa.rubricCriterion;
            questionGroup = oa.questionGroup;
            questionBank = oa.questionBank;
            exceedsThreshold = oa.exceedsThreshold;
            demonstratesThreshold = oa.demonstratesThreshold;
        }
        
        /**
         * compare the association with the specified parameters to determine if they are equivalent
         * @param assignmentGroupName - the name of the assignment group in canvas (required)
         * @param assignmentName - the name of the assignment (optional if association is for assignment group only)
         * @param rubricCriterion - the name of the rubric criterion (optional if association is for the 
         *                          assignment group, or an entire assignment, or if the association is for a 
         *                          quiz question group)
         * @param questionGroup - the name of the quiz question group (optional if the association is not for
         *                        a specific set of questions in a quiz).  Used in conjunction with the question Bank parameter
         * @param questionBank - the name of the question bank associated with the questionGroup parameter.
         * @return - true if the parameters are equivalent to the output association
         */
        public boolean matches(String assignmentGroupName, String assignmentName, String rubricCriterion, String questionGroup, String questionBank) {
            if (!this.assignmentGroupName.equals(assignmentGroupName)) return false;
            if ((assignmentName==null)&&(this.assignmentName!=null)) return false;
            if ((assignmentName!=null)&&(this.assignmentName==null)) return false;
            if ((this.assignmentName!=null)&&(!this.assignmentName.equals(assignmentName))) return false;
            if ((rubricCriterion==null)&&(this.rubricCriterion!=null)) return false;
            if ((rubricCriterion!=null)&&(this.rubricCriterion==null)) return false;
            if ((this.rubricCriterion!=null)&&(!this.rubricCriterion.equals(rubricCriterion))) return false;            
            if ((this.questionGroup!=null)&&(!this.questionGroup.equals(questionGroup))) return false;
            return (this.questionBank == null) || (this.questionBank.equals(questionBank));
        }
        
        /**
         * compare to the specified outcome association
         * @param oa - the outcome association to compare to
         * @return - true if the two associations are equivalent, otherwise false.
         */
        public boolean matches(OutcomeAssociation oa) {
        	return matches(oa.getAssignmentGroupName(),oa.getAssignmentName(),oa.getRubricCriterion(),oa.getQuestionGroup(),oa.getQuestionBank());
        }
        
        /**
         * returns the assignment group name
         */
        public String getAssignmentGroupName() {return assignmentGroupName;}

        /**
         * returns the assignment name
         */
        public String getAssignmentName() {return assignmentName;}

        /**
         * returns the rubric criterion
         */
        public String getRubricCriterion() {return rubricCriterion;}

        /**
         * returns the question group
         */
        public String getQuestionGroup() {return questionGroup;}

        /**
         * returns the question bank
         */
        public String getQuestionBank() {return questionBank;}

        public double getExceedsThreshold() {
            return exceedsThreshold;
        }


    public double getDemonstratesThreshold() {
            return demonstratesThreshold;
        }

    public void setExceedsThreshold(double exceedsThreshold) {
        this.exceedsThreshold = exceedsThreshold;
    }

    public void setDemonstratesThreshold(double demonstratesThreshold) {
        this.demonstratesThreshold = demonstratesThreshold;
    }

    @Override
		/*
		  create a string representation of the object
		 */
		public String toString() {
			// create a string that represents the lowest level of the outcome
			// association
			if (rubricCriterion!=null) return rubricCriterion;
			if (questionGroup!=null) {
				if (questionBank != null) return questionGroup + " -- " + questionBank;
				return questionGroup;
			}
			if (assignmentName!=null) return assignmentName;
			return assignmentGroupName;
		}        
    }
    