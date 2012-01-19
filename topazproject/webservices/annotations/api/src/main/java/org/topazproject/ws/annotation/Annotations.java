/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

/**
 * Annotation related operations.
 *
 * @author Pradeep Krishnan
 */
public interface Annotations extends Remote {
  /**
   * Permissions associated with annotation related operations.
   */
  public static interface Permissions {
    /**
     * The action that represents a createAnnotation operation in XACML policies.
     */
    public static final String CREATE_ANNOTATION = "annotations:createAnnotation";

    /**
     * The action that represents a deleteAnnotation operation in XACML policies.
     */
    public static final String DELETE_ANNOTATION = "annotations:deleteAnnotation";

    /**
     * The action that represents a getAnnotation operation in XACML policies.
     */
    public static final String GET_ANNOTATION_INFO = "annotations:getAnnotationInfo";

    /**
     * The action that represents a supersede operation in XACML policies.
     */
    public static final String SUPERSEDE = "annotations:supersede";

    /**
     * The action that represents a listAnnotations operation in XACML policies. Note that this
     * permission is checked against the a:annotates resource.
     */
    public static final String LIST_ANNOTATIONS = "annotations:listAnnotations";

    /**
     * The action that represents a listAnnotations operation in XACML policies. Note that this
     * permission is checked against the base uri of annotations.
     */
    public static final String LIST_ANNOTATIONS_IN_STATE = "annotations:listAnnotationsInState";

    /**
     * The action that represents a listAnnotations operation in XACML policies.
     */
    public static final String SET_ANNOTATION_STATE = "annotations:setAnnotationState";
  }

  /**
   * Creates a new annotation.
   *
   * @param mediator an entity that mediates access to the created annotation. Can be used by an
   *        application to identify the annotations it created. Defined by
   *        <code>http://purl.org/dc/terms/mediator</code>. May be <code>null</code>.
   * @param type An annotation type or <code>null</code>. The different types of annotations
   *        defined in <code>http://www.w3.org/2000/10/annotationType#</code> are:
   *        <ul><li><code>http://www.w3.org/2000/10/annotationType#Advice</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Change</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Comment</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Example</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Explantion</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Question</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#SeeAlso</code></li>
   *        <li><code>http://www.w3.org/2000/10/annotationType#Annotation</code></li> </ul>
   *        Defaults to <code>http://www.w3.org/2000/10/annotationType#Annotation</code>
   * @param annotates the resource to which this annotation applies. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#annotates</code> and the inverse
   *        <code>http://www.w3.org/2000/10/annotation-ns#hasAnnotation</code>. Must be a valid
   *        <code>URI</code>.
   * @param context the context within the resource named in <code>annotates</code> to which this
   *        annotation applies or <code>null</code>. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#context</code>
   * @param supersedes the annotation that this supersedes or <code>null</code>. Defined by
   *        <code>http://purl.org/dc/terms/replaces</code> and the inverse relation
   *        <code>http://purl.org/dc/terms/isReplacedBy</code> both of which are sub properties of
   *        <code>http://purl.org/dc/elements/1.1/relation</code>. Defaults to
   *        <code>http://www.w3.org/1999/02/22-rdf-syntax-ns#nil</code>
   * @param anonymize a boolean to indicate that the creator wishes to remain anonymous.
   * @param title the annotation title or <code>null</code>. Defined by
   *        <code>http://purl.org/dc/elements/1.1/title</code>
   * @param body the resource representing the content of an annotation. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#body</code>, a sub property of
   *        <code>http://www.w3.org/2000/10/annotation-ns#related</code>. Must be a valid
   *        <code>URL</code>
   *
   * @return Returns a unique identifier for the newly created annotation
   *
   * @throws NoSuchAnnotationIdException if <code>supersedes</code> is not a valid annotation id
   * @throws RemoteException if some other error occurred
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title, String body)
                          throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Creates a new annotation. A new resource URL is created for the annotation body from the
   * supplied content.
   *
   * @param mediator an entity that mediates access to the created annotation. (eg. an app-id)
   * @param type An annotation type or <code>null</code>.
   * @param annotates the resource to which this annotation applies.
   * @param context the context within the resource named in <code>annotates</code> to which this
   *        annotation applies or <code>null</code>.
   * @param supersedes the annotation that this supersedes or <code>null</code>.
   * @param anonymize a boolean to indicate that the creator wishes to remain anonymous.
   * @param title the annotation title or <code>null</code>
   * @param contentType the mime-type and optionally the character encoding of the annotation body.
   *        eg. <code>text/html;charset=utf-8</code>, <code>text/plain;charset=iso-8859-1</code>,
   *        <code>text/plain</code> etc.
   * @param content the annotation body content in the character encoding specified. If no
   *        character encoding is specified the interpretation will be left up to the client that
   *        later retrieves the annotation body.
   *
   * @return Returns a unique identifier for the newly created annotation
   *
   * @throws NoSuchAnnotationIdException if <code>supersedes</code> is not a valid annotation id
   * @throws RemoteException if some other error occurred
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title,
                                 String contentType, byte[] content)
                          throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Deletes an annotation. Deletes all triples for which this annotation is the subject.
   * Additionally if the <code>deletePreceding</code> is <code>true</code>  then all preceding
   * annotations to this are deleted.
   *
   * @param id the id of the annotation to remove
   * @param deletePreceding whether to delete all annotations that are superseded by this
   *        annotation.
   *
   * @throws NoSuchAnnotationIdException if the annotation does not exist
   * @throws RemoteException if some other error occurred
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Retrieve the annotation meta-data. Note that there may be other annotations that supersede
   * this. To always get the latest version(s), use {@link #getLatestAnnotations}.
   *
   * @param id the id of the annotation for which to get the meta-data
   *
   * @return Returns the annotation meta data as an xml document.
   *
   * @throws NoSuchAnnotationIdException if the annotation does not exist
   * @throws RemoteException if some other error occurred
   */
  public AnnotationInfo getAnnotationInfo(String id)
                                   throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Gets the latest version(s) of this annotation. The latest version(s) are the ones that are not
   * superseded by other annotations and therefore could just be this annotation itself.
   *
   * @param id the annotation id.
   *
   * @return an array of annotation metadata; the array will at least contain one element
   *
   * @throws NoSuchAnnotationIdException if the annotation does not exist
   * @throws RemoteException if an error occurred
   */
  public AnnotationInfo[] getLatestAnnotations(String id)
                                        throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Gets the set of annotations of the given type on a resource. Matching annotations are further
   * filtered out if they are superseded by other annotations. Note that this returns only those
   * annotations that the caller has permissions to view.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param annotates the resource for which annotations are to be looked-up
   * @param type the annotation type to use in filtering the annotations or <code>null</code>  to
   *        include all
   *
   * @return an array of annotation metadata for matching annotations; if no annotations have been
   *         defined, an empty array is returned
   *
   * @throws RemoteException if an error occurred
   */
  public AnnotationInfo[] listAnnotations(String mediator, String annotates, String type)
                                   throws RemoteException;

  /**
   * Gets the chain of annotations that precede this to give a history of changes.
   *
   * @param id the annotation id
   *
   * @return an array of annotation metadata; if this annotation does not supersede any other
   *         annotation, then an empty array is returned
   *
   * @throws NoSuchAnnotationIdException if the annotation does not exist
   * @throws RemoteException if an error occurred
   */
  public AnnotationInfo[] getPrecedingAnnotations(String id)
                                           throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Sets the administrative state of an annotation. (eg. flagged for review)
   *
   * @param id the annotation id
   * @param state the new state or 0 to take the annotation out of an administrator state
   *
   * @throws NoSuchAnnotationIdException if the annotation does not exist
   * @throws RemoteException if some other error occurred
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchAnnotationIdException, RemoteException;

  /**
   * List the set of annotations in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *        administrative state
   *
   * @return an array of annotation metadata; if no matching annotations are found, an empty array
   *         is returned
   *
   * @throws RemoteException if some error occurred
   */
  public AnnotationInfo[] listAnnotations(String mediator, int state)
                                   throws RemoteException;

  /**
   * Gets the annotated content for a resource.
   * 
   * <p>
   * This is useful since xpointer libraries are not widely available. It makes use of the
   * unibo-xpointer and dom-ranges-helper library.
   * </p>
   * 
   * <p>
   * For java clients direct use of these libraries on the client side is recommended since that
   * will give more flexibility in the markup and also avoids the double serialization of the
   * annotated resource.
   * </p>
   * 
   * <p>
   * Also note that there is no filtering done on annotation state. So the application may have to
   * remove unwanted annotations from the markup.
   * </p>
   * 
   * <p>
   * Also note that this method does not need additional access control permissions. The user must
   * have permissions to execute a {@link
   * #listAnnotations(java.lang.String,java.lang.String,java.lang.String) listAnnotations} on the
   * resource for this call to succeed.
   * </p>
   * 
   * <p>
   * The content body is marked up with additional tags as in this example:
   * <pre>
   *  &lt;?xml version=&quot;1.0&quot;?&gt;
   *  &lt;doc xmlns:aml=&quot;http://topazproject.org/aml/&quot;&gt;
   *    &lt;chapter&gt;
   *      &lt;title&gt;Chapter I&lt;/title&gt;
   *      &lt;para&gt;&lt;aml:annotated aml:id=&quot;1&quot;&gt;Hello &lt;/aml:annotated&gt;&lt;aml:annotated aml:id=&quot;2&quot;&gt;world&lt;/aml:annotated&gt;&lt;aml:annotated aml:id=&quot;3&quot;&gt;, &lt;/aml:annotated&gt;&lt;aml:annotated aml:id=&quot;4&quot;&gt;indeed&lt;/aml:annotated&gt;&lt;aml:annotated aml:id=&quot;5&quot;&gt;, &lt;/aml:annotated&gt;&lt;em&gt;&lt;aml:annotated aml:id=&quot;5&quot;&gt;wonderful&lt;/aml:annotated&gt;&lt;/em&gt; world&lt;/para&gt;
   *    &lt;/chapter&gt;
   *    &lt;aml:regions&gt;
   *      &lt;aml:region aml:id=&quot;1&quot;&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/83&quot;/&gt;
   *      &lt;/aml:region&gt;
   *      &lt;aml:region aml:id=&quot;2&quot;&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/83&quot;/&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/85&quot;/&gt;
   *      &lt;/aml:region&gt;
   *      &lt;aml:region aml:id=&quot;3&quot;&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/85&quot;/&gt;
   *      &lt;/aml:region&gt;
   *      &lt;aml:region aml:id=&quot;4&quot;&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/85&quot;/&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/84&quot;/&gt;
   *      &lt;/aml:region&gt;
   *      &lt;aml:region aml:id=&quot;5&quot;&gt;
   *        &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/84&quot;/&gt;
   *      &lt;/aml:region&gt;
   *    &lt;/aml:regions&gt;
   *    &lt;aml:annotations xmlns:a=&quot;http://www.w3.org/2000/10/annotation-ns#&quot; xmlns:d=&quot;http://purl.org/dc/elements/1.1/&quot; xmlns:dt=&quot;http://purl.org/dc/terms/&quot; xmlns:r=&quot;http://www.w3.org/1999/02/22-rdf-syntax-ns#&quot; xmlns:topaz=&quot;http://rdf.topazproject.org/RDF/&quot;&gt;
   *      &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/83&quot;&gt;
   *        &lt;r:type r:resource=&quot;http://www.w3.org/2000/10/annotationType#Annotation&quot;/&gt;
   *        &lt;a:annotates r:resource=&quot;foo:bar&quot;/&gt;
   *        &lt;a:context&gt;foo:bar#xpointer(string-range(/,'Hello+world'))&lt;/a:context&gt;
   *        &lt;d:creator r:resource=&quot;anonymous:user/&quot;/&gt;
   *        &lt;a:created&gt;2006-09-21T02:11:31Z&lt;/a:created&gt;
   *        &lt;a:body r:resource=&quot;http://gandalf.topazproject.org&quot;/&gt;
   *        &lt;d:title&gt;Title&lt;/d:title&gt;
   *        &lt;dt:mediator&gt;integration-test&lt;/dt:mediator&gt;
   *        &lt;topaz:state&gt;0&lt;/topaz:state&gt;
   *      &lt;/aml:annotation&gt;
   *      &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/84&quot;&gt;
   *        &lt;r:type r:resource=&quot;http://www.w3.org/2000/10/annotationType#Annotation&quot;/&gt;
   *        &lt;a:annotates r:resource=&quot;foo:bar&quot;/&gt;
   *        &lt;a:context&gt;foo:bar#xpointer(string-range(/,'indeed,+wonderful'))&lt;/a:context&gt;
   *        &lt;d:creator r:resource=&quot;anonymous:user/&quot;/&gt;
   *        &lt;a:created&gt;2006-09-21T02:11:32Z&lt;/a:created&gt;
   *        &lt;a:body r:resource=&quot;http://gandalf.topazproject.org&quot;/&gt;
   *        &lt;d:title&gt;Title&lt;/d:title&gt;
   *        &lt;dt:mediator&gt;integration-test&lt;/dt:mediator&gt;
   *        &lt;topaz:state&gt;0&lt;/topaz:state&gt;
   *      &lt;/aml:annotation&gt;
   *      &lt;aml:annotation aml:id=&quot;doi:10.1371/annotation/85&quot;&gt;
   *        &lt;r:type r:resource=&quot;http://www.w3.org/2000/10/annotationType#Annotation&quot;/&gt;
   *        &lt;a:annotates r:resource=&quot;foo:bar&quot;/&gt;
   *        &lt;a:context&gt;foo:bar#xpointer(string-range(/,'world,+indeed'))&lt;/a:context&gt;
   *        &lt;d:creator r:resource=&quot;anonymous:user/&quot;/&gt;
   *        &lt;a:created&gt;2006-09-21T02:11:32Z&lt;/a:created&gt;
   *        &lt;a:body r:resource=&quot;http://gandalf.topazproject.org&quot;/&gt;
   *        &lt;d:title&gt;Title&lt;/d:title&gt;
   *        &lt;dt:mediator&gt;integration-test&lt;/dt:mediator&gt;
   *        &lt;topaz:state&gt;0&lt;/topaz:state&gt;
   *      &lt;/aml:annotation&gt;
   *    &lt;/aml:annotations&gt;
   *  &lt;/doc&gt;
   * </pre>
   * Note that in the above example, region[5] appears twice in the annotated document. This is how
   * partially selected nodes will be marked up.
   * </p>
   *
   * @param resource the resource for which annotations are to be looked-up
   * @param resourceURL the URL to use to access the resource content or <code>null</code> if same
   *        as <code>resource</code> or <code>content</code> is specified.
   * @param content the content body corresponding to the resource or <code>null</code>. If
   *        <code>null</code>, then the <code>resourceURL</code> or <code>resource</code> will be
   *        used to fetch the content.
   * @param mediator if present only those annotations that match this mediator are returned
   * @param type the annotation type to use in filtering the annotations or <code>null</code>  to
   *        include all
   *
   * @return the resource content body with annotations.
   *
   * @throws RemoteException if an error occurred
   */
  public DataHandler getAnnotatedContent(String resource, String resourceURL, DataHandler content,
                                         String mediator, String type)
                                  throws RemoteException;
}
