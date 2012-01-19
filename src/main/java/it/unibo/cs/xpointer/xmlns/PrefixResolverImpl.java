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

package it.unibo.cs.xpointer.xmlns;

import java.util.Hashtable;

/**
 * 
 */
public class PrefixResolverImpl implements it.unibo.cs.org.apache.xml.utils.PrefixResolver {

    private Hashtable table = new Hashtable();
    
    public void setNamespace(String prefix,String URI)
    {
        table.put(prefix,URI);
    }
    
    /** Creates new PrefixResolverImpl */
    public PrefixResolverImpl() {
    }

    /**
     * Return the base identifier.
     *
     * @return The base identifier from where relative URIs should be absolutized, or null
     * if the base ID is unknown.
     */
    public String getBaseIdentifier() {
        return null;
    }
    
    /**
     * Given a namespace, get the corrisponding prefix.  This assumes that
     * the PrevixResolver hold's it's own namespace context, or is a namespace
     * context itself.
     *
     * @param prefix The prefix to look up, which may be an empty string ("") for the default Namespace.
     *
     * @return The associated Namespace URI, or null if the prefix
     *        is undeclared in this context.
     */
    public String getNamespaceForPrefix(String prefix) {
        
        String namespace = (String) table.get(prefix);
        
        if(prefix.equals("xml"))
            namespace = "http://www.w3.org/XML/1998/namespace";
        
        return namespace;
    }
    
    /**
     * Given a namespace, get the corrisponding prefix, based on the node context.
     *
     * @param prefix The prefix to look up, which may be an empty string ("") for the default Namespace.
     * @param context The node context from which to look up the URI.
     *
     * @return The associated Namespace URI, or null if the prefix
     *        is undeclared in this context.
     */
    public String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context) {
        
        return getNamespaceForPrefix(prefix);
    }
    
}
