package com.jkramr.webcrawler.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrieTest {

  private Trie empty;
  private Trie one;
  private Trie many;

  @Before
  public void setUp()
          throws Exception {
    empty = new Trie();

    one = new Trie();
    one.add('a');

    many = new Trie();
    many.add('a');
    many.add('b');
  }

  @Test
  public void isEmpty()
          throws Exception {

    assertEquals(true, empty.isEmpty());
    assertEquals(false, one.isEmpty());
    assertEquals(false, many.isEmpty());
  }

  @Test
  public void testContains()
          throws Exception {

    assertEquals(false, empty.contains('a'));
    assertEquals(true, one.contains('a'));
    assertEquals(true, many.contains('a'));
    assertEquals(true, many.contains('a'));
  }

  @Test
  public void testSize()
          throws Exception {
    assertEquals(0, empty.size());
    assertEquals(1, one.size());
    assertEquals(2, many.size());
  }
}