package org.plos.article.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArticleDeleteException extends Exception {
	List<Exception> exceptions = new ArrayList<Exception>();
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nWraps "+exceptions.size()+" Exceptions:\n");
		Iterator<Exception> iter = exceptions.iterator();
		while(iter.hasNext()) {
			Exception ex = iter.next();
			sb.append(ex);
			if (iter.hasNext()) {
				sb.append("\n\n======================== next exception =======================\n\n");
			} else {
				sb.append("\n\n================== end of wrapped exceptions ==================\n\n");
			}
		}
		return sb.toString();
	}

	public void addException(Exception e) {
		exceptions.add(e);
	}
	
	public List<Exception> getExceptionList() {
		return exceptions;
	}
	
  /**
   * Prints this throwable, its stacktrace  and the stacktraces of any wrapped exceptions
   * to the specified print stream.
   * 
   * @param prtStrm <code>PrintStream</code> to use for output
   */
	@Override 
  public void printStackTrace(PrintStream prtStrm) {
    synchronized (prtStrm) {
      super.printStackTrace(prtStrm);
      prtStrm.print("\nWraps " + exceptions.size() + " Exceptions:\n");
      Iterator<Exception> iter = exceptions.iterator();
      while (iter.hasNext()) {
        Exception ex = iter.next();
        ex.printStackTrace(prtStrm);
        if (iter.hasNext()) {
          prtStrm.print("\n\n======================== next exception =======================\n\n");
        } else {
          prtStrm.print("\n\n================== end of wrapped exceptions ==================\n\n");
        }
      }
      prtStrm.print("\n\n======================== next exception =======================\n\n");
    }
  }
}
