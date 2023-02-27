package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.FileChooser;

/**
 * Representation of a Canvas outcome report. 
 */
public class OutcomeReport {
    final int STATE_PARSE_NORMAL = 0;
    final int STATE_ASSESSMENT_LIST = 1;
    final int STATE_FLUSH_ASSESSMENT_LIST = 2;
    final int STATE_PARSE_SCORE_TABLE = 3;
    final int STATE_FLUSH_UNUSED_COLUMNS = 4;
    final int STATE_FLUSH_UNUSED_ROWS = 5;
    final int STATE_FLUSH_UNUSED_OUTCOMES = 255;
    final int MAX_ASSESSMENT = 6;
    
    AssignmentGroups assignment_groups;
    String course_id;
    CanvasStudentEnrollmentList student_list;
    CanvasOutcomes outcomes;
    TreeMap<String,String> symbolTable;
    
    /*
     * helper function to create the symbol table to be used for search and replace
     */
    private void createSymbolTable() {
        symbolTable = new TreeMap<>();
        DecimalFormat dfPercent = new DecimalFormat("##0.0%");
        DecimalFormat dfDecimal = new DecimalFormat("##,##0.0");

        // place course name in symbol table
        symbolTable.put("$+CLASSNAME$-", outcomes.getCourseName());

        // place date in the symbol table 
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date);
        symbolTable.put("$+DATE$-", strDate);

        // place outcome names and descriptions in the symbol table
        for (int outcomeNumber = 1;outcomeNumber<=outcomes.size();outcomeNumber++) {
            CanvasOutcome outcome = outcomes.get(outcomeNumber-1);
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+"$-","");            
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".NAME$-", outcome.getTitle());
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".DESCRIPTION$-", outcome.getDescription());

            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".MAX$-",dfDecimal.format(assignment_groups.getMaximumOutcomePoints(outcome)));

            // for each assessment in the outcome, create the symbol table replacements
            ArrayList<OutcomeAssociation> associations = outcome.getAssociations();
            for (int assocNum = 1; assocNum<=associations.size();assocNum++) {
                OutcomeAssociation association = associations.get(assocNum-1);
                if (association.getAssignmentName() == null) {
                    // here if the association is for an assignment group 
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".A"+Integer.toString(assocNum)+"$-",
                            "Assignment Group: "+association.getAssignmentGroupName());
                } else if ((association.getRubricCriterion() == null) && (association.getQuestionGroup() == null)) {
                    // here if the association is for a complete assignment
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".A"+Integer.toString(assocNum)+"$-",
                            association.getAssignmentName());                        
                } else if (association.getRubricCriterion() != null) {
                    // here if the association is for a single rubric item
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".A"+Integer.toString(assocNum)+"$-",
                            association.getAssignmentName()+", rubric criterion: "+association.getRubricCriterion());                                                
                } else {
                	// here if the association is for a quiz question group/bank
                	String questionBank;
                	if (association.getQuestionBank()==null) {
                		questionBank = "";
                	} else {
                		questionBank = " -- " + association.getQuestionBank();
                	}
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".A"+Integer.toString(assocNum)+"$-",
                            association.getAssignmentName()+", question group: "+association.getQuestionGroup() + questionBank);                                                
                }
                
                
                // for each student in the student list, create the symbols for the student scores
                int student_number =0;
                for (String student_id:student_list) {
                    student_number++;
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".S"+Integer.toString(student_number)+".A"+Integer.toString(assocNum)+"$-",
                            dfDecimal.format(assignment_groups.getStudentAssignmentPoints(association, student_id)));
                    
                    // Create a symbol table entry for the student's percentage score for this specific assignment.
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".S"+Integer.toString(student_number)+".A"+Integer.toString(assocNum)+"%$-",
                            dfPercent.format(assignment_groups.getStudentAssignmentPercent(association, student_id)));
                                        
                }
            }

            int total_epct=0;
            int total_apct=0;
            int total_mpct=0;
            int total_upct=0;
            int total_epts=0;
            int total_apts=0;
            int total_mpts=0;
            int total_upts=0;
            int student_number =0;
            // loop for each student to create symbols for EAMU vector and student totals
            for (String student_id:student_list) {
                    student_number++;
                    double student_outcome_avgpct = (double)Math.round(assignment_groups.getStudentAverageOutcomePercent(outcome, student_id)*1000.0)/1000.0;
                    // create the symbol table entry for the average percent score that the student earned on this outcome
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".S"+Integer.toString(student_number)+".AVGPCT$-",
                            dfPercent.format(student_outcome_avgpct));
                    
                    double student_outcome_points = assignment_groups.getStudentOutcomePoints(outcome, student_id);
                    double maximum_outcome_points = assignment_groups.getMaximumOutcomePoints(outcome);
                    double student_outcome_percent;
                    if (maximum_outcome_points != 0) {
                        // create the symbol entry for the percent score that the student earned on this assignment as long as there were points 
                        // associated with the assignment
                        student_outcome_percent = (double)Math.round(student_outcome_points*1000.0/maximum_outcome_points)/1000.0;

                    } else {
                        // If there were no points for this assignment, set the percent earned for this assignment to 100%
                        student_outcome_percent = 1;
                    }
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".S"+Integer.toString(student_number)+".PERCENT$-",
                                dfPercent.format(student_outcome_percent));

                    // create the symbol table entry for the total number of points that the student earned on this outcome
                    symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".S"+Integer.toString(student_number)+".TOTAL$-",
                            dfDecimal.format(assignment_groups.getStudentOutcomePoints(outcome, student_id)));
                    if (student_outcome_percent>=0.9) {total_epts++;
                    } else if (student_outcome_percent>=0.80) {total_apts++;
                    } else if (student_outcome_percent>=0.70) {total_mpts++;
                    } else total_upts ++;
                    if (student_outcome_avgpct>=0.9) {total_epct++;
                    } else if (student_outcome_avgpct>=0.80) {total_apct++;
                    } else if (student_outcome_avgpct>=0.70) {total_mpct++;
                    } else total_upct ++;
            }
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVE$-",Integer.toString(total_epts));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVA$-",Integer.toString(total_apts));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVM$-",Integer.toString(total_mpts));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVU$-",Integer.toString(total_upts));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVE%$-",Integer.toString(total_epct));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVA%$-",Integer.toString(total_apct));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVM%$-",Integer.toString(total_mpct));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".PVU%$-",Integer.toString(total_upct));
            symbolTable.put("$+O"+Integer.toString(outcomeNumber)+".TOTALSTUDENTS$-",Integer.toString(student_list.size()));
        }
    }
    
    /*
     * helper function to create a report using a template with the specified filename
     */
    private void createReportFromTemplate(String templatename) {
    // test code to try to read/write the output report.
        try {
            XmlTemplateReader template = new XmlTemplateReader(new InputStreamReader(CanvasOre.class.getResourceAsStream(templatename), StandardCharsets.UTF_8 ));
            
            // get the output file name 
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Outcome Report");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setInitialFileName(outcomes.getCourseId());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML File","*.xml"));
            File file = fileChooser.showSaveDialog(null);
            if (file==null) {
            	template.close();
            	return;
            }
            BufferedWriter outfile = new BufferedWriter(new FileWriter(file,StandardCharsets.UTF_8, false));

            // loop to read the file line-by-line
            String line;
            int state = STATE_PARSE_NORMAL;
            int assessment_count = 0;
            int outcome_number = 1;
            int column_count = 0;
            int row_count = 0;
            try {
                while ((line = template.readLine()) != null) {
                    if (line.trim().equals("$+O1$-</w:t>")) {
                        outcome_number = 1;
                    } else if (line.trim().equals("$+O2$-</w:t>")) {
                        outcome_number = 2;
                    } else if (line.trim().equals("$+O3$-</w:t>")) {
                        outcome_number = 3;
                    } else if (line.trim().equals("$+O4$-</w:t>")) {
                        outcome_number = 4;
                    } else if (line.trim().equals("$+O5$-</w:t>")) {
                        outcome_number = 5;
                    } else if (line.trim().equals("$+O6$-</w:t>")) {
                        outcome_number = 6;
                    } else if (line.trim().equals("$+O7$-</w:t>")) {
                        outcome_number = 7;
                    } else if (line.trim().equals("$+O8$-</w:t>")) {
                        outcome_number = 8;
                    } else if (line.trim().equals("$+O9$-</w:t>")) {
                        outcome_number = 9;
                    } else if (line.trim().equals("$+O10$-</w:t>")) {
                        outcome_number = 10;
                    } else if (line.trim().equals("$+O11$-</w:t>")) {
                        outcome_number = 11;
                    } else if (line.trim().equals("$+O12$-</w:t>")) {
                        outcome_number = 12;
                    } else if (line.trim().equals("$+O13$-</w:t>")) {
                        outcome_number = 13;
                    } else if (line.trim().equals("$+O14$-</w:t>")) {
                        outcome_number = 14;
                    } else if (line.trim().equals("$+O15$-</w:t>")) {
                        outcome_number = 15;
                    } else if (line.trim().equals("$+O16$-</w:t>")) {
                        outcome_number = 16;
                    } else if (line.trim().equals("$+O17$-</w:t>")) {
                        outcome_number = 17;
                    } else if (line.trim().equals("$+O18$-</w:t>")) {
                        outcome_number = 18;
                    } else if (line.trim().equals("$+O19$-</w:t>")) {
                        outcome_number = 19;
                    } else if (line.trim().equals("$+O20$-</w:t>")) {
                    	outcome_number = 20;
                    }
                    switch (state) {
                        case STATE_PARSE_NORMAL:
                            if (outcome_number>outcomes.size()) {
                                // change the line to close out the current text line and paragraph
                                line = "</w:t></w:r></w:p>";
                                state = STATE_FLUSH_UNUSED_OUTCOMES;
                            } else if (line.trim().equals("Assessments</w:t>")) {
                                state = STATE_ASSESSMENT_LIST;
                                assessment_count = 0;
                            } else if (line.trim().equals("Raw Data</w:t>")) {
                                state = STATE_PARSE_SCORE_TABLE;
                                column_count = 0;
                                row_count = 0;
                            }
                            break;
                        case STATE_ASSESSMENT_LIST:
                            if (line.trim().equals("</w:p>")) {
                                // this is the final specifier for an assessment item line.
                                // assessment count = 0 -> ending paragraph mark for header
                                // assessment count = 1 -> end of first assessment bullet
                                // assessment count = 2 -> end of second assessment bullet
                                if (assessment_count == MAX_ASSESSMENT) {
                                    // if we have reached the last assessment, go back to
                                    // normal processing mode
                                    state = STATE_PARSE_NORMAL;
                                } else if (assessment_count >= outcomes.get(outcome_number-1).getAssociations().size()) {
                                    state = STATE_FLUSH_ASSESSMENT_LIST;
                                }
                                assessment_count ++;
                            }
                            break;
                        case STATE_FLUSH_ASSESSMENT_LIST:
                            if (line.trim().equals("</w:p>")) {
                                // this is the final specifier for an assessment item line.
                                // assessment count = 0 -> ending paragraph mark for header
                                // assessment count = 1 -> end of first assessment bullet
                                // assessment count = 2 -> end of second assessment bullet
                                if (assessment_count == MAX_ASSESSMENT) {
                                    // if we have reached the last assessment, go back to
                                    // normal processing mode
                                    state = STATE_PARSE_NORMAL;
                                }
                                assessment_count ++;
                            }
                            continue;
                        case STATE_PARSE_SCORE_TABLE:
                            if (line.trim().equals("</w:tr>")) {
                                // this is the final specifier for a table row.
                                column_count = 0;
                                if (row_count >= student_list.size()) {
                                    state = STATE_FLUSH_UNUSED_ROWS;
                                } 
                                row_count ++;
                                break;
                            }
                            if (line.trim().equals("</w:tc>")) {
                                // this is the final specifier for a table column.
                                // column 0 = the student id column
                                // column 1 = the first assessment column
                                // column 2 = the second assessment column
                                // ...
                                if (column_count == MAX_ASSESSMENT) {
                                    column_count = -5;
                                    state = STATE_PARSE_SCORE_TABLE;
                                } else if (column_count >= outcomes.get(outcome_number-1).getAssociations().size()) {
                                    state = STATE_FLUSH_UNUSED_COLUMNS;
                                } 
                                column_count ++;
                                break;
                            }
                            break;
                        case STATE_FLUSH_UNUSED_COLUMNS:
                            if (line.trim().equals("</w:tc>")) {
                                // this is the final specifier for an column.
                                if (column_count == MAX_ASSESSMENT) {
                                    // if we have reached the last assessment, go back to
                                    // normal processing mode
                                    state = STATE_PARSE_SCORE_TABLE;
                                    column_count = -5;
                                }
                                column_count ++;
                            }
                            continue;
                        case STATE_FLUSH_UNUSED_ROWS:
                            if (line.trim().equals("</w:tbl>")) {
                                // this is the final specifier for the end of a table
                                state = STATE_PARSE_NORMAL;
                                break;
                            }
                            // otherwise, skip this line
                            continue;
                        case STATE_FLUSH_UNUSED_OUTCOMES:
                            // discard lines until the end of the document is reached.
                            if (!line.trim().equals("$+END$-</w:t>")) continue;
                            line = "<w:p><w:r><w:t></w:t>";
                            state = STATE_PARSE_NORMAL;
                            outcome_number = 0;
                            break;
                    }
                    // make replacements from symbol table (if found)
                    if (line.contains("$+")) {
                        for(Map.Entry<String,String> entry : symbolTable.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            String replaceString = line.replace(key, value);
                            line = replaceString;
                            if (!line.contains("$+")) break;
                        }
                    }
                    outfile.write(line);
                }
            } catch (IOException ex) {
                Logger.getLogger(OutcomeReport.class.getName()).log(Level.SEVERE, null, ex);
            }
            outfile.close();
            template.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OutcomeReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OutcomeReport.class.getName()).log(Level.SEVERE, null, ex);                
        }
    }
    
    /**
     * Constructor - create an outome report and save it to disk based on the input
     * parameters.
     * 
     * @param outcomes - the outcomes to run the report on
     * @param reportByPoints - true if the report will be calculated by sum of points, 
     *           otherwise, report by average percent.
     */
    public OutcomeReport(CanvasOutcomes outcomes, boolean reportByPoints) {    
        // generate the outcome report for the specified outcomes
        // create a log file for errors that are found
        this.outcomes = outcomes;
        course_id = outcomes.getCourseId();
        
        // load the student list
        student_list = new CanvasStudentEnrollmentList(course_id);

        // load the course assignment groups
        assignment_groups = new AssignmentGroups(course_id);
  
        // get the grades associated with the assignments and rubrics
        assignment_groups.loadGrades();

        // create the symbol table
        createSymbolTable();
        
        // create the report;
        if (reportByPoints) {
            createReportFromTemplate("/Outcomes By Points.xml");
        } else {
            createReportFromTemplate("/Outcomes By Percent.xml");
        }
    }
}
