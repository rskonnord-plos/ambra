/*
 * Copyright (c) 2006, University Of Bologna, Italy
 *
 * Contributor: Topaz, Inc. (http://www.topazproject.org)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of University Of Bologna, Italy and Topz, Inc., nor 
 *       the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY AND TOPAZ AND CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Sentence.java
 *
 * Created on 16 febbraio 2002, 8.54
 */

package it.unibo.cs.xpointer.custom;

import it.unibo.cs.xpointer.Location;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;
import java.util.Vector;

/**
 *
 * @author  root
 * @version 
 */
public class Sentence {

    private String PATTERN = "(\\w.*?)";
    
    private String []boundaries;
    private int boundSize;
    private boolean customBoundaries = true;
    private Regexp regexp;
    private int INDEX = 1;
   

    public Sentence(Location loc)
    {
        regexp = new Regexp(loc);
        init();
    }
    
    public Sentence(Node node)
    {
        regexp = new Regexp(node);
        init();
    }
    
    private String buildPattern()
    {
        String pattern = PATTERN + "(\\" + boundaries[0];
        
        for(int i=1;i<=boundSize;i++)
            pattern = pattern + "|\\" +boundaries[i];
        
        pattern = pattern + ")+";
        
        return pattern;
    }
    
    private void init() {
        
        regexp.setFlag(gnu.regexp.RE.REG_DOT_NEWLINE);
        boundSize = 2;
        boundaries = new String[3];
        
        boundaries[0]=".";
        boundaries[1]="!";
        boundaries[2]="?";
        
    }

    public void setBoundaryOutput(boolean flag)
    {
        if(flag==true)
            INDEX = 0;
        else
            INDEX = 1;
    }
    
    public void setBoundary(String s)
    {
        if(customBoundaries==false)
        {
            customBoundaries = true;
            boundSize = -1;
            boundaries = new String[255];
        }
        
        boundSize++;
        boundaries[boundSize] = s;
    }
    
    public Range [] getSentences()
    {
        //DEBUG
        System.out.println("PATTERN:"+buildPattern());
        int len = regexp.getRegexp(buildPattern()).length;
        
        Range [] retval;
        Vector temp = new Vector();
        
        for(int i=0;i<len;i++)
        {
            temp.addElement(regexp.getGroups(i)[INDEX]);
        }
        
        retval = new Range[temp.size()];
        
        for(int i=0;i<retval.length;i++)
        {
            retval[i] = (Range) temp.elementAt(i);
        }
        
        return retval;
    }
}
