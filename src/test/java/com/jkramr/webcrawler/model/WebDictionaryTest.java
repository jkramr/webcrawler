package com.jkramr.webcrawler.model;

import com.jkramr.webcrawler.service.WebDictionary;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class WebDictionaryTest {

  private WebDictionary empty;
  private WebDictionary one;
  private WebDictionary nesting;
  private WebDictionary nestingWithAssets;

  @Before
  public void setUp()
          throws Exception {
    empty = new WebDictionary();

    one = new WebDictionary();
    one.add("https://example.com");

    nesting = new WebDictionary();
    nesting.add("https://example.com/images");

    nestingWithAssets = new WebDictionary();
    nestingWithAssets.add("https://example.com/images");
    nestingWithAssets.addAsset(
            "https://example.com",
            "png",
            "https://example.com/images/123.png"
    );

    nestingWithAssets.addAsset(
            "https://example.com",
            "png",
            "https://example.com/images/1234.png"
    );
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
            empty.contains("https://example.com")
    );

    assertEquals(
            true,
            one.contains("https://example.com")
    );
    assertEquals(
            false,
            one.contains("https://example.com/images")
    );
  }

  @Test
  public void testNestingContains()
          throws Exception {
    assertEquals(
            true,
            nesting.contains("https://example.com")
    );
    assertEquals(
            false,
            nesting.contains("https://example.com/images")
    );
    assertEquals(
            false,
            nesting.contains("https://example.com/images/logos")
    );
  }

  @Test
  public void testHasAssets()
          throws Exception {

    assertEquals(
            false,
            nesting.hasAssets("https://example.com")
    );

    assertEquals(
            false,
            nestingWithAssets.hasAssets("https://example.com/images")
    );

    assertEquals(
            true,
            nestingWithAssets.hasAssets("https://example.com")
    );
  }

  @Test
  public void testGetAssets()
          throws Exception {

    assertEquals(
            Arrays.asList(
                    "https://example.com/images/123.png",
                    "https://example.com/images/1234.png"
            ),
            nestingWithAssets.getAssets("https://example.com")
    );

  }
}