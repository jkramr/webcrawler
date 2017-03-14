package com.jkramr.webcrawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;

@Component
public class WebCrawlerService {

  private static final HashMap<String, String> LINK_MATCHERS = new HashMap<>();

  static {
    LINK_MATCHERS.put("href=\"", "\"");
    LINK_MATCHERS.put("href=\'", "\'");
  }

  private final RestTemplate restTemplate;

  @Value("${crawler.domain:google.com}")
  String domain;

  @Value("${crawler.depth:2}")
  int depth;

  @Value("${crawler.asset_types:[jpg,css,js,png]}")
  String[] asset_types;

  private Url           root;
  private WebDictionary webDictionary;

  @Autowired
  public WebCrawlerService(
          RestTemplate restTemplate,
          WebDictionary webDictionary
  ) {
    root = Url.ofFull(domain);

    this.webDictionary = webDictionary;
    this.restTemplate = restTemplate;
  }

  public void startCrawl() {
    crawl(root, root, 0);

    System.out.println(webDictionary);
  }

  private void crawl(
          Url parent,
          Url current,
          int depth
  ) {
    if (depth <= this.depth) {
      try {
        String html = restTemplate.getForObject(
                current.getValue(),
                String.class
        );

        if (html == null) {
          webDictionary.add(current);

          System.out.println("404: Not found");
        }

        if (!webDictionary.contains(current)) {
          System.out.println(current);

          addToDictionary(parent, current);

          crawlHtml(parent, html, depth);
        }

      } catch (Exception ignored) {
      }
    }
  }

  private void addToDictionary(
          Url parent,
          Url current
  ) {
    Arrays.stream(asset_types)
          .filter(current.getValue()::endsWith)
          .findAny()
          .map(asset -> webDictionary.addAsset(
                  parent,
                  asset,
                  current
          ))
          .orElseGet(() -> webDictionary.add(current));
  }

  private void crawlHtml(
          Url parent,
          String html,
          int depth
  ) {
    LINK_MATCHERS.forEach((startMatcher, endMatcher) -> crawlMatchingLinks(
            parent,
            html,
            depth,
            startMatcher,
            endMatcher
    ));
  }

  private void crawlMatchingLinks(
          Url parent,
          String html,
          int depth,
          String startMatcher,
          String endMatcher
  ) {
    int startLinkIndex = getLinkIndex(html, startMatcher);

    if (startLinkIndex != -1) {
      String subHtml = html.substring(startLinkIndex);

      crawlHtml(parent, subHtml, depth);

      int endLinkIndex = subHtml.indexOf(endMatcher);

      if (endLinkIndex != -1) {
        Url parsedLink = Url.of(subHtml.substring(0, endLinkIndex));

        if (!parsedLink.isFullPath() || root.hasChild(parsedLink)) {
          Url child = root.hasChild(parsedLink)
                      ? parsedLink
                      : root.append(parsedLink);

          crawl(parent, child, depth + 1);
        }
      }
    }
  }

  private int getLinkIndex(String html, String startMatcher) {
    int linkIndex = html.indexOf(startMatcher);

    return linkIndex == -1
           ? -1
           : linkIndex + startMatcher.length();
  }
}
