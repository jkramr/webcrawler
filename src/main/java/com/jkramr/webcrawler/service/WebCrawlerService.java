package com.jkramr.webcrawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Component
public class WebCrawlerService {

  public static final  HashMap<String, String> LINK_MATCHERS = new HashMap<>();
  private static final int                     MAX_DEPTH     = 2;

  static {
    LINK_MATCHERS.put("href=\"", "\"");
    LINK_MATCHERS.put("href=\'", "\'");
  }

  private final RestTemplate restTemplate;
  @Value("${domain:google.com}")
  String domain;

  @Autowired
  public WebCrawlerService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public void startCrawl() {
    Url root = Url.ofFull(domain);

    Dictionary dictionary = new Dictionary();

    crawl(root, root, dictionary, 0);

    System.out.println(dictionary);
  }

  private boolean crawl(Url root, Url current, Dictionary dictionary, int depth) {
    if (depth <= MAX_DEPTH) {
      try {
        String html = restTemplate.getForObject(
                current.getValue(),
                String.class
        );

        if (html == null) {
          dictionary.add(current.getValue());

          System.out.println("404: Not found");
          return false;
        }

        if (!dictionary.contains(current.getValue())) {
          System.out.println(current);

          dictionary.add(current.getValue());

          return crawlHtml(root, html, dictionary, depth);
        }

        return false;
      } catch (Exception e) {
        return false;
      }
    } else {
      return true;
    }
  }

  private boolean crawlHtml(
          Url root,
          String html,
          Dictionary dictionary,
          int depth
  ) {
    LINK_MATCHERS.forEach((startMatcher, endMatcher) -> {
      int startLinkIndex = getLinkIndex(html, startMatcher);

      if (startLinkIndex != -1) {
        String subHtml = html.substring(startLinkIndex);

        crawlHtml(root, subHtml, dictionary, depth);

        int endLinkIndex = subHtml.indexOf(endMatcher);

        if (endLinkIndex != -1) {
          Url url = Url.of(subHtml.substring(0, endLinkIndex));

          if (!url.isFullPath() || root.hasChild(url)) {
            Url childUrl = root.hasChild(url)
                           ? url
                           : root.append(url);

            crawl(root, childUrl, dictionary, depth + 1);
          }
        }
      }
    });

    return true;
  }

  private int getLinkIndex(String html, String startMatcher) {
    int linkIndex = html.indexOf(startMatcher);

    return linkIndex == -1
           ? -1
           : linkIndex + startMatcher.length();
  }
}
