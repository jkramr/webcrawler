package com.jkramr.webcrawler.model;

public class Trie {

  private int size = 0;

  public boolean isEmpty() {
    return size == 0;
  }

  public Character add(Character c) {
    size++;
    return null;
  }

  public boolean contains(Character c) {
    return !isEmpty();
  }

  public int size() {
    return size;
  }

}
