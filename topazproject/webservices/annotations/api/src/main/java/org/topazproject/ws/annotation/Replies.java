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

/**
 * Annotation reply related operations. Replies are a mechanism that allows people to publish
 * replies to annotations; for example, they allow someone to reply to a comment. Replies can also
 * be made to other replies and thus promote threads of discussion. Moreover, as each reply is
 * identified with a unique URI, a client may also permit the user to annotate a reply.
 * 
 * <p>
 * An instance of a Reply will have most of the same properties that an instance of an Annotation
 * has. Two features distinguish a Reply from an Annotation; the type and the
 * <code>http://www.w3.org/2000/10/annotation-ns#annotates</code> property. Replies have RDF type
 * <code>http://www.w3.org/2001/03/thread#Reply</code> and do not use the
 * <code>http://www.w3.org/2000/10/annotation-ns#annotates</code> property.
 * </p>
 * 
 * <p>
 * They instead define 2 new properties. The
 * <code>http://www.w3.org/2001/03/thread#inReplyTo</code> and
 * <code>http://www.w3.org/2001/03/thread#root</code>
 * </p>
 *
 * @author Pradeep Krishnan
 */
public interface Replies extends Remote {
  /**
   * Permissions associated with Reply related operations.
   */
  public static interface Permissions {
    /**
     * The action that represents a createReply operation in XACML policies.
     */
    public static final String CREATE_REPLY = "replies:createReply";

    /**
     * The action that represents a deleteReply operation in XACML policies.
     */
    public static final String DELETE_REPLY = "replies:deleteReply";

    /**
     * The action that represents a getReply operation in XACML policies.
     */
    public static final String GET_REPLY_INFO = "replies:getReplyInfo";

    /**
     * The action that represents a listReplies operation in XACML policies.
     */
    public static final String LIST_REPLIES = "replies:listReplies";

    /**
     * The action that represents a listAllReplies operation in XACML policies.
     */
    public static final String LIST_ALL_REPLIES = "replies:listAllReplies";

    /**
     * The action that represents a listReplies operation in XACML policies. Note that this
     * permission is checked against the base uri of annotations.
     */
    public static final String LIST_REPLIES_IN_STATE = "replies:listRepliesInState";

    /**
     * The action that represents a setReplyState operation in XACML policies.
     */
    public static final String SET_REPLY_STATE = "replies:setReplyState";
  }

  /**
   * Creates a reply for an annotation.
   *
   * @param mediator an entity that mediates access to the created reply. Can be used by an
   *        application to identify the replies it created. Defined by
   *        <code>http://purl.org/dc/terms/mediator</code>. May be <code>null</code>.
   * @param type the type of reply or <code>null</code>. The different types of replies defined in
   *        <code>http://www.w3.org/2001/12/replyType</code>
   *        are:<ul><li><code>http://www.w3.org/2001/12/replyType#SeeAlso</code></li>
   *        <li><code>http://www.w3.org/2001/12/replyType#Agree</code></li>
   *        <li><code>http://www.w3.org/2001/12/replyType#Disagree</code></li>
   *        <li><code>http://www.w3.org/2001/12/replyType#Comment</code></li> </ul> Defaults to
   *        <code>http://www.w3.org/2001/12/replyType#Comment</code>
   * @param root the URI of the resource naming the start of a discussion. (in this case, the
   *        annotation that was first replied to) This is used to identify a given discussion
   *        thread. Every resource in the thread will have the same resource as its root.
   * @param inReplyTo the URI of the resource the user is replying to (in this case, either an
   *        annotation or reply)
   * @param anonymize a boolean to indicate that the creator wishes to remain anonymous.
   * @param title the title of this reply or <code>null</code>, Defined by
   *        <code>http://purl.org/dc/elements/1.1/title</code>
   * @param body the resource representing the content of this reply. Defined by
   *        <code>http://www.w3.org/2000/10/annotation-ns#body</code> Must be a valid
   *        <code>URL</code>
   *
   * @return Returns a unique identifier for the newly created reply
   *
   * @throws NoSuchAnnotationIdException when the <code>root</code> is not a valid annotation or
   *         when the <code>inReplyTo</code> is not a valid annotation or reply.
   * @throws RemoteException when some other error occurs
   */
  public String createReply(String mediator, String type, String root, String inReplyTo,
                            boolean anonymize, String title, String body)
                     throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Creates a reply for an annotation. A new reply body URL is created from the supplied content.
   *
   * @param mediator an entity that mediates access to the created reply. (eg. an app-id)
   * @param type the type of reply or <code>null</code>.
   * @param root the URI of the resource naming the start of a discussion.
   * @param inReplyTo the URI of the resource the user is replying to
   * @param anonymize a boolean to indicate that the creator wishes to remain anonymous.
   * @param title the title of this reply or <code>null</code>,
   * @param contentType the mime-type and optionally the character encoding of the reply body. eg.
   *        <code>text/html;charset=utf-8</code>, <code>text/plain;charset=iso-8859-1</code>,
   *        <code>text/plain</code> etc.
   * @param content the reply body content in the character encoding specified. If no character
   *        encoding is specified the interpretation will be left upto the client that later
   *        retrieves the annotation body.
   *
   * @return Returns a unique identifier for the newly created reply
   *
   * @throws NoSuchAnnotationIdException when the <code>root</code> is not a valid annotation or
   *         when the <code>inReplyTo</code> is not a valid annotation or reply.
   * @throws RemoteException when some other error occurs
   */
  public String createReply(String mediator, String type, String root, String inReplyTo,
                            boolean anonymize, String title, String contentType, byte[] content)
                     throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Deletes all replies and their descendants for a resource.
   *
   * @param root the root of this discussion thread
   * @param inReplyTo the resource whose replies are to be deleted. could be the root annotation or
   *        any other reply as part of the thread.
   *
   * @throws NoSuchAnnotationIdException if <code>inReplyTo</code> is not a valid reply in the
   *         discussion thread specified by <code>root</code>
   * @throws RemoteException if some other error occured
   */
  public void deleteReplies(String root, String inReplyTo)
                     throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Deletes a reply and its descendants.
   *
   * @param id the reply to be deleted
   *
   * @throws NoSuchAnnotationIdException if <code>id</code> is not a valid reply
   * @throws RemoteException if some other error occured
   */
  public void deleteReplies(String id) throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Gets reply details.
   *
   * @param id the id of the reply for which to get the infi
   *
   * @return Returns the reply information.
   *
   * @throws NoSuchAnnotationIdException if the reply does not exist
   * @throws RemoteException if some other error occured
   */
  public ReplyInfo getReplyInfo(String id) throws NoSuchAnnotationIdException, RemoteException;

  /**
   * List the replies of a specific resource.
   *
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   *
   * @return an array of replies or an empty list
   *
   * @throws NoSuchAnnotationIdException if <code>inReplyTo</code> is not a valid reply in the
   *         discussion thread specified by <code>root</code>
   * @throws RemoteException if some other error occured
   */
  public ReplyInfo[] listReplies(String root, String inReplyTo)
                          throws NoSuchAnnotationIdException, RemoteException;

  /**
   * List all replies that can be traced back to a resource.
   *
   * @param root the discussion thread
   * @param inReplyTo the resource whose descendant replies are to be returned
   *
   * @return an array of all descendant replies or an empty list
   *
   * @throws NoSuchAnnotationIdException if <code>inReplyTo</code> is not a valid reply in the
   *         discussion thread specified by <code>root</code>
   * @throws RemoteException if an error occured
   */
  public ReplyInfo[] listAllReplies(String root, String inReplyTo)
                             throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Get the thread of replies for a resource.
   * 
   * <p>
   * This API is experimental. No testing done with SOAP clients other than Axis.
   * </p>
   *
   * @param root the discussuon thread this resource is part of
   * @param inReplyTo the resource whose replies are to be returned
   *
   * @return Returns the sub tree of a discussion with the root node being the resource
   *         <code>inReplyTo</code>
   *
   * @throws NoSuchAnnotationIdException if <code>inReplyTo</code> is not a valid reply in the
   *         discussion thread specified by <code>root</code>
   * @throws RemoteException if some other error occured
   */
  public ReplyThread getReplyThread(String root, String inReplyTo)
                             throws NoSuchAnnotationIdException, RemoteException;

  /**
   * Sets the administrative state of a reply. (eg. flagged for review)
   *
   * @param id the reply id
   * @param state the new state or 0 to take the reply out of an administrator state
   *
   * @throws NoSuchAnnotationIdException if the reply does not exist
   * @throws RemoteException if some other error occured
   */
  public void setReplyState(String id, int state)
                     throws NoSuchAnnotationIdException, RemoteException;

  /**
   * List the set of replies in a specific administrative state.
   *
   * @param mediator if present only those replies that match this mediator are returned
   * @param state the state to filter the list of replies by or 0 to return replies in any
   *        administartive state
   *
   * @return an array of replies; if no matching replies are found, an empty array is returned
   *
   * @throws RemoteException if some error occured
   */
  public ReplyInfo[] listReplies(String mediator, int state)
                          throws RemoteException;
}
