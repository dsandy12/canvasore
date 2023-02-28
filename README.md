# CanvasOre - Performance Indicator Data Mining Tool for Canvas LMS
## General Information
CanvasOre is a data mining tool that allows instructors to easily extract student performance against pre-defined outcome indicators at the Assignment Group, Assignment, Quiz, Rubric Criteria, or Quiz Question Bank level. While the Canvas LMS does support outcome evaluation, it is restricted to evaluation against rubrics only and does not lend itself to evaluating performance against non-rubric assessed outcomes within a typical course framework.  

### The Value Proposition
1.	CanvasOre can quickly aggregate all assessments related to a particular indicator across all course assignments.  At the end of the term, faculty can easily evaluate all scores related to each performance indicator.  This technology alleviates faculty from having to conduct tedious data extraction, and it provides error-free reporting, ensuring consistent grading criteria across multiple sections of a single course.  
2.	The tool significantly reduces grading and reporting time from several hours to just a few minutes.  This feature also saves time for TAs and graders.  Reports are comprehensive and easily interpreted.
3.	CanvasOre facilitates a uniform reporting format for all courses within an ABET-accredited program.  

### Main Features
The main features of CanvasOre include:
1. Full authentication capabilities including single-sign-on and dual-factor authentication.
2. Ability to create and edit associations between course content (Assignment Group, Assignment, Rubric Criteria, Quiz and Quiz Question Bank) and course outcomes.
3. Ability to edit existing outcomes
4. Ability to apply existing outcomes to new course sections
5. Ability to automatically generate XML report files showing anonymized student performance against each performance indicator (outcome).

# Installation
This section briefly describes how to install and build CanvasOre.  The build process uses Gradle and Java SE 13.  You will need to install these tools before you can build the program.  Currently, no pre-built images are provided on this repository.
More information about the Gradle build tool can be found at gradle.org.

Step 1: Download the repository from GitHub
Step 2: Build the and run the tool with gradle
  From the canvas_ore_gr folder in the downloaded repository, execute the gradle 'run' task.  This will build and run the utility.  Note that if you are using Microsoft Windows, you may be able to use the gradle wrapper executable provided in the repository by typing 
  gradlew run
at the command line.

# Tutorials
Please visit the CanvasOre YouTube Channel for tutorials on how to use CanvasOre.  The tutorial playlist can be found here:
https://www.youtube.com/playlist?list=PLRYQhV5yOhBf5Y2GKcaLB_l6Kkq-DJDlp
