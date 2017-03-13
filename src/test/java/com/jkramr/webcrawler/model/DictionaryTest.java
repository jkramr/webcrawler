package com.jkramr.webcrawler.model;

import com.jkramr.webcrawler.service.Dictionary;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DictionaryTest {

  private Dictionary empty;
  private Dictionary one;
  private Dictionary google;

  @Before
  public void setUp()
          throws Exception {
    empty = new Dictionary();

    one = new Dictionary();
    one.add("https://google.com");

    google = new Dictionary();
    google.add("https://google.com/mail/search");
  }

  @Test
  public void isEmpty()
          throws Exception {
    assertEquals(true, empty.isEmpty());
    assertEquals(false, one.isEmpty());
  }

  @Test
  public void testContains()
          throws Exception {
    assertEquals(
            false,
            empty.contains("https://google.com")
    );

    assertEquals(
            true,
            one.contains("https://google.com")
    );
    assertEquals(
            false,
            one.contains("https://google.com/mail")
    );
  }

  @Test
  public void testNesting()
          throws Exception {
    //TODO implement TRIE for this to work
//    assertEquals(
//            true,
//            google.contains("https://google.com")
//    );
    assertEquals(
            false,
            google.contains("https://google.com/mail")
    );
//    assertEquals(
//            false,
//            google.contains("https://google.com/search")
//    );
  }
}