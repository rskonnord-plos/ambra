package org.plos.models;

import java.net.URI;
import java.util.Date;

/**
 * ArticleAnnotation interface represents a model Class that provides the necessary information to 
 * annotate an article. It is implemented by Comment, MinorCorrection, and FormalCorrection, and may be
 * implemented by other model classes that shall represent annotation types that are overlaid on an Article. 
 * Since Comment and Correction are semantically different, and inherit from a different semantic hierarchy,
 * this interface allows the UI to process different annotation types more generically.   
 * 
 * @author Alex Worden
 *
 */
public interface ArticleAnnotation {

  public URI getId();
  public void setId(URI newId);
  
  public URI getBody();
  public void setBody(URI body);

  public String getType();
  public void setType(String type);

  public URI getAnnotates();
  public void setAnnotates(URI annotated);
  
  public String getContext();
  public void setContext(String context);

  public String getCreator();
  public void setCreator(String user);
  
  public String getAnonymousCreator();
  public void setAnonymousCreator(String user);

  public Date getCreated();
  public void setCreated(Date date);

  public Annotation getSupersedes();
  public void setSupersedes(Annotation a);
  
  public Annotation getSupersededBy();
  public void setSupersededBy(Annotation a);

  public String getTitle();
  public void setTitle(String title);

  public String getMediator();
  public void setMediator(String applicationId);

  public int getState();
  public void setState(int newState);
}