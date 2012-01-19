
package org.topazproject.mulgara.resolver;

import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.mulgara.query.Answer;
import org.trippi.RDFUtil;
import org.trippi.TripleIterator;
import org.trippi.TrippiException;

/**
 * A <code>TripleIterator</code> that wraps a Mulgara Answer object containing
 * triples.
 *
 * <p>This is a straight copy of Trippi's KowariTripleIterator, with all 'Kowari'
 * replaced by 'Mulgara'.
 *
 * @author cwilper@cs.cornell.edu
 */
public class MulgaraTripleIterator extends TripleIterator {

    private Answer m_answer;
    private boolean m_hasNext;

    private boolean m_closed;

    /**
     * Initialize the iterator given a Mulgara Answer.
     *
     * @throws TrippiException if there is an error positioning to the
     *                              first result.
     */
    public MulgaraTripleIterator(Answer answer) throws TrippiException {
        m_answer = answer;
        m_closed = false;
        try {
            m_answer.beforeFirst();
            checkNext();
        } catch (Exception e) {
            String msg = e.getClass().getName();
            if (e.getMessage() != null) msg = msg + ": " + e.getMessage();
            throw new TrippiException("Error getting first result from "
                    + "Mulgara Answer" + msg, e);
        }
    }

    private void checkNext() throws TrippiException {
        try {
            m_hasNext = m_answer.next();
        } catch (Exception e) {
            throw new TrippiException("Error getting next result from Mulgara Answer.", e);
        }
        if (m_hasNext == false) {
            try {
                close();
            } catch (TrippiException e) {
                System.err.println("Error aggressively closing mulgara triple iterator: " + e.getMessage());
            }
        }
    }

    public boolean hasNext() {
        return m_hasNext;
    }

    public Triple next() throws TrippiException {
        if ( !m_hasNext ) return null;
        try {
            SubjectNode s = (SubjectNode) m_answer.getObject(0);
            PredicateNode p = (PredicateNode) m_answer.getObject(1);
            ObjectNode o = (ObjectNode) m_answer.getObject(2);
            checkNext();
            return new RDFUtil().createTriple(s, p, o);
        } catch (Exception e) {
            String msg = e.getClass().getName();
            if (e.getMessage() != null) msg = msg + ": " + e.getMessage();
            throw new TrippiException("Error getting subsequent result "
                    + "from Mulgara Answer" + msg, e);
        }
    }

    public void close() throws TrippiException {
        if (!m_closed) {
            try {
                m_answer.close();
                m_closed = true;
            } catch (Exception e) {
                String msg = e.getClass().getName();
                if (e.getMessage() != null) msg = msg + ": " + e.getMessage();
                throw new TrippiException("Error closing "
                        + "MulgaraTripleIterator: " + msg, e);
            }
        }
    }

    /**
     * Ensure close() gets called at garbage collection time.
     */
    public void finalize() throws TrippiException {
        close();
    }
}
