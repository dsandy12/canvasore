package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    final int STATE_ATTAINMENT_SUMMARY_TABLE = 6;
    final int STATE_FLUSH_UNUSED_OUTCOMES = 255;
    final int MAX_ASSESSMENT = 6;
    
    final AssignmentGroups assignment_groups;
    final String course_id;
    final ArrayList<String> student_list;
    final CanvasOutcomes outcomes;
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
            symbolTable.put("$+O"+ outcomeNumber +"$-","");
            symbolTable.put("$+O"+ outcomeNumber +".NAME$-", outcome.getTitle());
            symbolTable.put("$+O"+ outcomeNumber +".DESCRIPTION$-", outcome.getDescription());

            symbolTable.put("$+O"+ outcomeNumber +".MAX$-",dfDecimal.format(assignment_groups.getMaximumOutcomePoints(outcome)));

            // for each assessment in the outcome, create the symbol table replacements
            ArrayList<OutcomeAssociation> associations = outcome.getAssociations();
            for (int assocNum = 1; assocNum<=associations.size();assocNum++) {
                OutcomeAssociation association = associations.get(assocNum-1);
                if (association.getAssignmentName() == null) {
                    // here if the association is for an assignment group 
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"$-",
                            "Assignment Group: "+association.getAssignmentGroupName());
                } else if ((association.getRubricCriterion() == null) && (association.getQuestionGroup() == null)) {
                    // here if the association is for a complete assignment
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"$-",
                            association.getAssignmentName());                        
                } else if (association.getRubricCriterion() != null) {
                    // here if the association is for a single rubric item
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"$-",
                            association.getAssignmentName()+", rubric criterion: "+association.getRubricCriterion());                                                
                } else {
                	// here if the association is for a quiz question group/bank
                	String questionBank;
                	if (association.getQuestionBank()==null) {
                		questionBank = "";
                	} else {
                		questionBank = " -- " + association.getQuestionBank();
                	}
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"$-",
                            association.getAssignmentName()+", question group: "+association.getQuestionGroup() + questionBank);                                                
                }
                symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KET$-",
                        dfPercent.format(association.getExceedsThreshold()));
                symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KMT$-",
                        dfPercent.format(association.getDemonstratesThreshold()));
                symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KET_$-",
                        dfDecimal.format(association.getExceedsThreshold()));
                symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KMT_$-",
                        dfDecimal.format(association.getDemonstratesThreshold()));


                // for each student in the student list, create the symbols for the student scores
                int student_number =0;
                for (String student_id:student_list) {
                    student_number++;
                    if (Double.isNaN(assignment_groups.getStudentAssignmentPoints(association, student_id))) {
                        // here if assignment was not attempted or outcome is unknown
                        symbolTable.put("$+O"+ outcomeNumber +".S"+ student_number +".A"+ assocNum +"$-", "-");
                        symbolTable.put("$+O"+ outcomeNumber +".S"+ student_number +".A"+ assocNum +"%$-", "-");
                        symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "_KPI$-", "X");
                    } else {
                        // Create a symbol table entry for the student's numeric score for this specific assignment.
                        symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "$-",
                                dfDecimal.format(assignment_groups.getStudentAssignmentPoints(association, student_id)));

                        // Create a symbol table entry for the student's percentage score for this specific assignment.
                        double percent = assignment_groups.getStudentAssignmentPercent(association, student_id);
                        symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "%$-",
                                dfPercent.format(percent));

                        // Create a symbol table entry for the student's kpi attainment for this specific assignment.
                        if (Double.isNaN(percent)) {
                            symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "_KPI$-", "X");
                        } else if (percent>=association.getExceedsThreshold()) {
                            symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "_KPI$-", "E");
                        } else if (percent>=association.getDemonstratesThreshold()) {
                            symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "_KPI$-", "A");
                        } else {
                            symbolTable.put("$+O" + outcomeNumber + ".S" + student_number + ".A" + assocNum + "_KPI$-", "I");
                        }
                    }
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
            int total_attained_kpi=0;
            int total_not_attained_kpi=0;
            int total_unknown_kpi=0;
            int student_number =0;

            // For this outcome, loop for each student to create symbols for EAMU vector and student totals
            for (String student_id:student_list) {
                    student_number++;

                    // calculate the average of the percent scores that the student earned across all assessments related
                    // to this outcome (rounded to two decimal positions).
                    double student_outcome_avgpct = (double)Math.round(assignment_groups.getStudentAverageOutcomePercent(outcome, student_id)*1000.0)/1000.0;

                    // create the symbol table entry for the average percent score that the student earned on this outcome
                    symbolTable.put("$+O"+ outcomeNumber +".S"+ student_number +".AVGPCT$-",
                            dfPercent.format(student_outcome_avgpct));
                    
                    // calculate the kpi - related metrics for this student
                    int assessmentNumber = 1;
                    double demonstratedCount = 0;
                    double notDemonstratedCount = 0;
                    double unknownCount = 0;
                    double totalCount = 0;
                    while (symbolTable.keySet().contains("$+O" + outcomeNumber + ".S" + student_number + ".A" + assessmentNumber + "_KPI$-")) {
                        String kpi_value = symbolTable.get("$+O" + outcomeNumber + ".S" + student_number + ".A" + assessmentNumber + "_KPI$-");
                        switch (kpi_value) {
                            case "E", "A" -> {
                                totalCount++;
                                demonstratedCount++;
                            }
                            case "I" -> {
                                totalCount++;
                                notDemonstratedCount++;
                            }
                            default -> {
                                totalCount++;
                                unknownCount++;
                            }
                        }
                        assessmentNumber ++;
                    }
                    String student_attainment_kpi = "-";
                    if (demonstratedCount/totalCount >= .70)  student_attainment_kpi = "Attained";
                    if ((demonstratedCount+unknownCount)/totalCount < .70)  student_attainment_kpi = "Not Attained";

                    // calculate the points earned by the student across all the assignments related to this
                    // outcome
                    double student_outcome_points = assignment_groups.getStudentOutcomePoints(outcome, student_id);
                    double maximum_outcome_points = assignment_groups.getMaximumOutcomePoints(outcome);
                    double student_outcome_percent;
                    if (maximum_outcome_points != 0) {
                        // if there were points associated with the outcome, calculate the percent of points that the student
                        // earned toward this outcome rounded to two decimal positions.
                        student_outcome_percent = (double)Math.round(student_outcome_points*1000.0/maximum_outcome_points)/1000.0;
                    } else {
                        // If there were no points for this outcome, set the percent earned for this assignment to 100%
                        student_outcome_percent = 1;
                    }
                    // create the symbol entry for the percent score that the student earned on this assignment as long as there were points
                    // associated with the assignment
                    symbolTable.put("$+O"+ outcomeNumber +".S"+ student_number +".PERCENT$-",
                                dfPercent.format(student_outcome_percent));

                    // create the symbol table entry for the total number of points that the student earned on this outcome
                    symbolTable.put("$+O"+ outcomeNumber +".S"+ student_number +".TOTAL$-",
                            dfDecimal.format(assignment_groups.getStudentOutcomePoints(outcome, student_id)));

                    // create the symbol table entry for the kpi attainment for the student
                    symbolTable.put("$+O"+ outcomeNumber +".S"+ student_number +".ATTAINED$-", student_attainment_kpi);

                    switch (student_attainment_kpi) {
                        case "Attained" -> total_attained_kpi++;
                        case "Not Attained" -> total_not_attained_kpi++;
                        default -> total_unknown_kpi++;
                    }

                    // update class percent attainment statistics for this outcome
                    if (student_outcome_percent>=0.9) {
                        total_epts++;
                    } else if (student_outcome_percent>=0.80) {
                        total_apts++;
                    } else if (student_outcome_percent>=0.70) {
                        total_mpts++;
                    } else total_upts ++;

                    // update class average percent attainment statistics for this outcome
                    if (student_outcome_avgpct>=0.9) {
                        total_epct++;
                    } else if (student_outcome_avgpct>=0.80) {
                        total_apct++;
                    } else if (student_outcome_avgpct>=0.70) {
                        total_mpct++;
                    } else total_upct ++;
            }

            // loop for each assignment/kpi to collect stats for them
            for (int assocNum = 1; assocNum<=associations.size();assocNum++) {
                OutcomeAssociation association = associations.get(assocNum - 1);
                int total_count = 0;
                int exceeds_count = 0;
                int meets_count = 0;
                int insufficient_count = 0;
                int unknown_count = 0;
                int snum = 0;
                for (String student_id : student_list) {
                    snum++;
                    String attainment = symbolTable.get("$+O" + outcomeNumber + ".S" + snum + ".A" + assocNum + "_KPI$-");
                    switch (attainment) {
                        case "E" -> {
                            total_count++;
                            exceeds_count++;
                        }
                        case "A" -> {
                            total_count++;
                            meets_count++;
                        }
                        case "I" -> {
                            total_count++;
                            insufficient_count++;
                        }
                        default -> {
                            unknown_count++;
                        }
                    }
                }
                // create the kpi statistics for this assignment/kpi
                if (total_count == 0) {
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKE$-", "-");
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKM$-", "-");
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKI$-", "-");
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKU$-", "-");
                } else {
                    double exceeds_percent = (double)Math.round(exceeds_count*1000.0/total_count)/10.0;
                    double meets_percent = (double)Math.round(meets_count*1000.0/total_count)/10.0;
                    double insufficient_percent = (double)Math.round(insufficient_count*1000.0/total_count)/10.0;
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKE$-", dfDecimal.format(exceeds_percent));
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKM$-", dfDecimal.format(meets_percent));
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"PKI$-", dfDecimal.format(insufficient_percent));
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KE$-", Integer.toString(exceeds_count));
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KM$-", Integer.toString(meets_count));
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KI$-", Integer.toString(insufficient_count));
                    symbolTable.put("$+O"+ outcomeNumber +".A"+ assocNum +"KU$-", Integer.toString(unknown_count));
                }
            }

            symbolTable.put("$+O"+ outcomeNumber +".PVE$-",Integer.toString(total_epts));
            symbolTable.put("$+O"+ outcomeNumber +".PVA$-",Integer.toString(total_apts));
            symbolTable.put("$+O"+ outcomeNumber +".PVM$-",Integer.toString(total_mpts));
            symbolTable.put("$+O"+ outcomeNumber +".PVU$-",Integer.toString(total_upts));
            symbolTable.put("$+O"+ outcomeNumber +".PVE%$-",Integer.toString(total_epct));
            symbolTable.put("$+O"+ outcomeNumber +".PVA%$-",Integer.toString(total_apct));
            symbolTable.put("$+O"+ outcomeNumber +".PVM%$-",Integer.toString(total_mpct));
            symbolTable.put("$+O"+ outcomeNumber +".PVU%$-",Integer.toString(total_upct));
            if (total_attained_kpi+total_not_attained_kpi==0) {
                symbolTable.put("$+O"+ outcomeNumber +".PMET$-","UNKNOWN");
                symbolTable.put("$+O"+ outcomeNumber +".TMET$-","UNKNOWN");
                symbolTable.put("$+O"+ outcomeNumber +".TNMET$-","UNKNOWN");
                symbolTable.put("$+O"+ outcomeNumber +".TUNK$-","UNKNOWN");
            } else {
                symbolTable.put("$+O"+ outcomeNumber +".PMET$-",
                        dfDecimal.format(100.0 * total_attained_kpi/(total_attained_kpi + total_not_attained_kpi)));
                symbolTable.put("$+O"+ outcomeNumber +".TMET$-", Integer.toString(total_attained_kpi));
                symbolTable.put("$+O"+ outcomeNumber +".TNMET$-", Integer.toString(total_not_attained_kpi));
                symbolTable.put("$+O"+ outcomeNumber +".TUNK$-", Integer.toString(total_unknown_kpi));
            }
            symbolTable.put("$+O"+ outcomeNumber +".TOTALSTUDENTS$-",Integer.toString(student_list.size()));
        }
    }
    
    /*
     * helper function to create a report using a template with the specified filename
     */
    private void createReportFromTemplate(String templatename) {
    // test code to try to read/write the output report.
        try {
            XmlTemplateReader template = new XmlTemplateReader(new InputStreamReader(Objects.requireNonNull(CanvasOre.class.getResourceAsStream(templatename)), StandardCharsets.UTF_8 ));
            
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
            boolean start_row_counting = false;
            try {
                while ((line = template.readLine()) != null) {
                    switch (line.trim()) {
                        case "$+O1$-</w:t>" -> outcome_number = 1;
                        case "$+O2$-</w:t>" -> outcome_number = 2;
                        case "$+O3$-</w:t>" -> outcome_number = 3;
                        case "$+O4$-</w:t>" -> outcome_number = 4;
                        case "$+O5$-</w:t>" -> outcome_number = 5;
                        case "$+O6$-</w:t>" -> outcome_number = 6;
                        case "$+O7$-</w:t>" -> outcome_number = 7;
                        case "$+O8$-</w:t>" -> outcome_number = 8;
                        case "$+O9$-</w:t>" -> outcome_number = 9;
                        case "$+O10$-</w:t>" -> outcome_number = 10;
                        case "$+O11$-</w:t>" -> outcome_number = 11;
                        case "$+O12$-</w:t>" -> outcome_number = 12;
                        case "$+O13$-</w:t>" -> outcome_number = 13;
                        case "$+O14$-</w:t>" -> outcome_number = 14;
                        case "$+O15$-</w:t>" -> outcome_number = 15;
                        case "$+O16$-</w:t>" -> outcome_number = 16;
                        case "$+O17$-</w:t>" -> outcome_number = 17;
                        case "$+O18$-</w:t>" -> outcome_number = 18;
                        case "$+O19$-</w:t>" -> outcome_number = 19;
                        case "$+O20$-</w:t>" -> outcome_number = 20;
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
                            } else if (line.trim().equals("Key Performance Indicators</w:t>")) {
                                state = STATE_ASSESSMENT_LIST;
                                assessment_count = 0;
                            } else if (line.trim().equals("KPI Threshold Table</w:t>")) {
                                state = STATE_ATTAINMENT_SUMMARY_TABLE;
                                row_count = 0;
                                start_row_counting = false;
                                assessment_count = 0;
                            } else if (line.trim().equals("Attainment Totals</w:t>")) {
                                    state = STATE_ATTAINMENT_SUMMARY_TABLE;
                                    row_count = 0;
                                    start_row_counting = false;
                                    assessment_count = 0;
                            } else if (line.trim().equals("Attainment by Percent of Population</w:t>")) {
                                state = STATE_ATTAINMENT_SUMMARY_TABLE;
                                row_count = 0;
                                start_row_counting = false;
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
                        case STATE_ATTAINMENT_SUMMARY_TABLE:
                            if (line.trim().equals ("A1.</w:t>")) {
                                start_row_counting = true;
                            }
                            if (line.trim().equals("</w:tr>")) {
                                // this is the final specifier for a table row.
                                column_count = 0;
                                if (start_row_counting) row_count ++;
                                if (row_count >= outcomes.get(outcome_number-1).getAssociations().size()) {
                                    state = STATE_FLUSH_UNUSED_ROWS;
                                }
                                break;
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
                                // column 0 = the student id column.
                                // column 1 = the first assessment column.
                                // column 2 = the second assessment column.
                                // ...
                                if (column_count == MAX_ASSESSMENT) {
                                    column_count = -5;
                                    // state = STATE_PARSE_SCORE_TABLE;
                                } else if (column_count >= outcomes.get(outcome_number-1).getAssociations().size()) {
                                    state = STATE_FLUSH_UNUSED_COLUMNS;
                                } 
                                column_count ++;
                                break;
                            }
                            break;
                        case STATE_FLUSH_UNUSED_COLUMNS:
                            if (line.trim().equals("</w:tc>")) {
                                // this is the final specifier for a column.
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
                            line = line.replace(key, value);
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
        } catch (IOException ex) {
            Logger.getLogger(OutcomeReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // find a spreadsheet row that starts with the specified text.  If not found, return -1
    private int findRowWith(XSSFSheet sheet, String findText) {
        for (int rowpos = 0; rowpos<1000; rowpos++) {
            if (sheet.getRow(rowpos) == null) {
                continue;
            }
            if (sheet.getRow(rowpos).getCell(0) == null) {
                continue;
            }
            if (Objects.equals(sheet.getRow(rowpos).getCell(0).getStringCellValue(), findText)) {
                return rowpos;
            }
        }
        return -1;
    }

    private void setCell(XSSFSheet sheet, int row, int column, String value) {
        try {
            // set as double
            sheet.getRow(row).getCell(column).setCellValue(Double.parseDouble(value));
        } catch (Exception ignored) {
            // set as string
            sheet.getRow(row).getCell(column).setCellValue(value);
        }
    }

    /*
     * helper function to create a report using a template with the specified filename
     */
    private void createExcelKpiReport() {
        try {
            // Reading file from local directory
            InputStream inputstream = CanvasOre.class.getResourceAsStream("template.xlsx");

            // Create Workbook instance
            XSSFWorkbook workbook = new XSSFWorkbook(inputstream);
            inputstream.close();

            // update the main sheet
            XSSFSheet sheet = workbook.getSheet("Methodology");
            sheet.getRow(1).getCell(0).setCellValue(symbolTable.get("$+CLASSNAME$-"));
            sheet.getRow(2).getCell(0).setCellValue("Generated on "+ symbolTable.get("$+DATE$-"));

            // Create a copy of the template sheet for each outcome
            for (int outcomeNumber = 1; outcomeNumber <= outcomes.size();outcomeNumber++) {
                CanvasOutcome outcome = outcomes.get(outcomeNumber - 1);
                String sheetName = "Outcome " + outcomeNumber;
                workbook.cloneSheet(1, sheetName);
                sheet = workbook.getSheet(sheetName);
                String outcomePrefix = "$+O"+outcomeNumber;
                sheet.getRow(0).getCell(0).setCellValue(sheetName + " - " + symbolTable.get(outcomePrefix+".NAME$-"));
                sheet.getRow(2).getCell(0).setCellValue(symbolTable.get(outcomePrefix+".DESCRIPTION$-"));

                // add the attainment descriptions
                int headerRow = findRowWith(sheet, "Key Performance Indicators");
                if (headerRow >= 0) {
                    int insertRow = headerRow + 1;
                    for (int i = 0; i<outcome.getAssociations().size(); i++) {
                        String assocNum = "A"+(i+1);
                        String key = outcomePrefix+"."+assocNum+"$-";
                        if (i != 0) {
                            // insert a new row
                            sheet.shiftRows(insertRow, sheet.getLastRowNum(),1);
                            sheet.createRow(insertRow);
                            // copy the cell formatting from the first row
                            sheet.copyRows(insertRow-1, insertRow-1, insertRow, new CellCopyPolicy());
                        }
                        sheet.getRow(insertRow).getCell(0).setCellValue(assocNum+". "+symbolTable.get(key));
                        insertRow ++;
                    }
                }

                // add the attainment summary
                headerRow = findRowWith(sheet, "KPI Threshold Table");
                if (headerRow >= 0) {
                    int insertRow = headerRow + 2;
                    for (int i = 0; i<outcome.getAssociations().size(); i++) {
                        String assocNum = "A"+(i+1);
                        String key1 = outcomePrefix+"."+assocNum+"KET_$-";
                        String key2 = outcomePrefix+"."+assocNum+"KMT_$-";
                        if (i != 0) {
                            // insert a new row
                            sheet.shiftRows(insertRow, sheet.getLastRowNum(),1);
                            sheet.createRow(insertRow);
                            // copy the cell formatting from the first row
                            sheet.copyRows(insertRow-1, insertRow-1, insertRow, new CellCopyPolicy());
                        }
                        sheet.getRow(insertRow).getCell(0).setCellValue(assocNum+". ");
                        setCell(sheet, insertRow,1, symbolTable.get(key1));
                        setCell(sheet, insertRow,3, symbolTable.get(key2));
                        insertRow ++;
                    }
                }

                // add the attainment percentages
                headerRow = findRowWith(sheet, "Attainment by Percent of Population");
                if (headerRow >= 0) {
                    // set the total percentage attained
                    setCell(sheet, headerRow+1, 3,symbolTable.get(outcomePrefix+".PMET$-"));
                    int insertRow = headerRow + 3;
                    for (int i = 0; i<outcome.getAssociations().size(); i++) {
                        String assocNum = "A"+(i+1);
                        String key1 = outcomePrefix+"."+assocNum+"PKE$-";
                        String key2 = outcomePrefix+"."+assocNum+"PKM$-";
                        String key3 = outcomePrefix+"."+assocNum+"PKI$-";
                        if (i != 0) {
                            // insert a new row
                            sheet.shiftRows(insertRow, sheet.getLastRowNum(),1);
                            sheet.createRow(insertRow);
                            // copy the cell formatting from the first row
                            sheet.copyRows(insertRow-1, insertRow-1, insertRow, new CellCopyPolicy());
                        }
                        sheet.getRow(insertRow).getCell(0).setCellValue(assocNum+". ");
                        setCell(sheet, insertRow,1, symbolTable.get(key1));
                        setCell(sheet, insertRow,3, symbolTable.get(key2));
                        setCell(sheet, insertRow,5, symbolTable.get(key3));
                        insertRow ++;
                    }
                }

                // add the attainment totals
                headerRow = findRowWith(sheet, "Attainment Totals");
                if (headerRow >= 0) {
                    // set the total percentage attained
                    setCell(sheet, headerRow+1,4,symbolTable.get(outcomePrefix+".TMET$-"));
                    setCell(sheet, headerRow+2,4,symbolTable.get(outcomePrefix+".TNMET$-"));
                    setCell(sheet, headerRow+3,4,symbolTable.get(outcomePrefix+".TUNK$-"));
                    int insertRow = headerRow + 5;
                    for (int i = 0; i<outcome.getAssociations().size(); i++) {
                        String assocNum = "A"+(i+1);
                        String key1 = outcomePrefix+"."+assocNum+"KE$-";
                        String key2 = outcomePrefix+"."+assocNum+"KM$-";
                        String key3 = outcomePrefix+"."+assocNum+"KI$-";
                        String key4 = outcomePrefix+"."+assocNum+"KU$-";
                        if (i != 0) {
                            // insert a new row
                            sheet.shiftRows(insertRow, sheet.getLastRowNum(),1);
                            sheet.createRow(insertRow);
                            // copy the cell formatting from the first row
                            sheet.copyRows(insertRow-1, insertRow-1, insertRow, new CellCopyPolicy());
                        }
                        sheet.getRow(insertRow).getCell(0).setCellValue(assocNum+". ");
                        setCell(sheet, insertRow,1,symbolTable.get(key1));
                        setCell(sheet, insertRow,3,symbolTable.get(key2));
                        setCell(sheet, insertRow,5,symbolTable.get(key3));
                        setCell(sheet, insertRow,7,symbolTable.get(key4));
                        insertRow ++;
                    }
                }

                // add the raw data
                headerRow = findRowWith(sheet, "Raw Data");
                if (headerRow >= 0) {
                    // Create the proper number of columns in the table
                    for ( int i = 0; i < outcome.getAssociations().size(); i++) {
                        // copy the cell formatting from the first row

                        Cell dest = sheet.getRow(headerRow+2).createCell(i+2);
                        Cell src = sheet.getRow(headerRow+2).getCell(1);
                        CellUtil.copyCell(src, dest, new CellCopyPolicy(), null);
                        if (i == outcome.getAssociations().size()-1) {
                            dest.setCellValue("Attained");
                        } else {
                            String assocNum = "A" + (i + 2);
                            dest.setCellValue(assocNum);
                        }

                        dest = sheet.getRow(headerRow+3).createCell(i+2);
                        src = sheet.getRow(headerRow+3).getCell(1);
                        CellUtil.copyCell(src, dest, new CellCopyPolicy(), null);
                    }

                    // set the total percentage attained
                    int insertRow = headerRow + 3;
                    for (int snum = 0; snum<student_list.size();snum++) {
                        if (snum != 0) {
                            // insert a new row
                            if (insertRow<sheet.getLastRowNum()) {
                                sheet.shiftRows(insertRow, sheet.getLastRowNum(), 1);
                            }
                            sheet.createRow(insertRow);
                            // copy the cell formatting from the first row
                            sheet.copyRows(insertRow - 1, insertRow - 1, insertRow, new CellCopyPolicy());
                        }
                        // place the student number in the leftmost column
                        sheet.getRow(insertRow).getCell(0).setCellValue("S"+(snum+1));

                        // place the individual attainment values
                        for (int i = 0; i < outcome.getAssociations().size()+1; i++) {
                            String assocNum = "A" + (i + 1);
                            String key = outcomePrefix + ".S" + (snum+1) + "." + assocNum + "_KPI$-";
                            if (i == outcome.getAssociations().size()) {
                                key = outcomePrefix + ".S" + (snum+1) + ".ATTAINED$-";
                            }
                            sheet.getRow(insertRow).getCell(i+1).setCellValue(symbolTable.get(key));
                        }
                        insertRow++;
                    }
                }
            }
            // remove the template sheet
            workbook.removeSheetAt(1);

            // get the output file name
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Outcome Report");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setInitialFileName(outcomes.getCourseId());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX File","*.xlsx"));
            File outfile = fileChooser.showSaveDialog(null);
            if (outfile==null) {
                return;
            }
            FileOutputStream outfilestream = new FileOutputStream(outfile);
            workbook.write(outfilestream);
            workbook.close();
            outfilestream.close();
        } catch (Exception ex) {
            Logger.getLogger(OutcomeReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Constructor - create an outome report and save it to disk based on the input
     * parameters.
     * 
     * @param outcomes - the outcomes to run the report on
     * @param reportType - "points" if the report generated should use the "sum of points"
     *    calculation  method.  "percent" to use the "average percentage" calculation method.
     *    "kpi" to generate a report by key performance indicators.
     */
    public OutcomeReport(CanvasOutcomes outcomes, String reportType) {
        // generate the outcome report for the specified outcomes
        // create a log file for errors that are found
        this.outcomes = outcomes;
        course_id = outcomes.getCourseId();
        
        // load the student list
        //student_list = new CanvasStudentEnrollmentList(course_id);

        // remove students who are not part of the major //
        StudentSelectorDlg selectorDlg = new StudentSelectorDlg(course_id);
        student_list = selectorDlg.getEnrollmentList();

        // load the course assignment groups
        assignment_groups = new AssignmentGroups(course_id);
  
        // get the grades associated with the assignments and rubrics
        assignment_groups.loadGrades();

        // create the symbol table
        createSymbolTable();
        
        // create the report;
        switch (reportType) {
            case "points":
                createReportFromTemplate("Outcomes By Points.xml");
                break;
            case "percent":
                createReportFromTemplate("Outcomes By Percent.xml");
                break;
            case "kpi":
                // createReportFromTemplate("Outcomes By KPI.xml");
                createExcelKpiReport();
                break;
        }
    }
}
