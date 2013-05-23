package org.ambraproject.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CategoryView to hold category structure and information
 */
public class CategoryView {
  private Map<String, CategoryView> parents;
  private Map<String, CategoryView> children;
  private final String name;

  public CategoryView(String name) {
    this.name = name;

    parents = new HashMap<String, CategoryView>();
    children = new HashMap<String, CategoryView>();
  }

  public Map<String, CategoryView> getParents() {
    return parents;
  }

  public Map<String, CategoryView> getChildren() {
    return children;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  public CategoryView getChild(String key) {
    return children.get(key);
  }

  public CategoryView getParent(String key) {
    return parents.get(key);
  }

  public void addParent(CategoryView categoryView) {
    parents.put(categoryView.getName(), categoryView);
    categoryView.children.put(this.name, this);
  }

  public void addChild(CategoryView categoryView) {
    children.put(categoryView.getName(), categoryView);
    categoryView.parents.put(this.name, this);
  }
}
