package edu.asu.dlsandy.canvas_ore;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */



import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Class to read an xml template file from disk 
 */
public class XmlTemplateReader extends BufferedReader {
	
	/**
	 * constructor
	 */
    public XmlTemplateReader(Reader in, int sz) {
        super(in, sz);
    }

	/**
	 * constructor
	 */
    public XmlTemplateReader(Reader in) {
        super(in);
    }

    @Override
    /**
     * This function will read a line until the ">" character or end of file
     * is found.  In order for the ">" to count as the end of the line, the 
     * ">" cannot be found in a quoted string within the file.
     */
    public String readLine() throws IOException {
        StringBuilder result = new StringBuilder();
        char[] cbuf = new char[1];
        boolean inQuotes = false;
        
        while (read(cbuf,0,1)>0) {
            // read a character at a time, building the result as we go.
            result.append(cbuf[0]);
            switch (cbuf[0]) {
                case '>':
                    if (!inQuotes) {
                        return result.toString();
                    }
                case '"':
                    inQuotes = !inQuotes;
                    break;
                default:
            }
        }
        // here if end of file is reached
        return null;
   }    
}
