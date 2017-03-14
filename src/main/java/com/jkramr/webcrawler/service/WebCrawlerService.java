package com.jkramr.webcrawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Component
public class WebCrawlerService {

  private static HashMap<String, String> LINK_MATCHERS = new HashMap<>();

  @Value("${domain:google.com}")
  String   domain;
  @Value("${debug:false}")
  boolean  debug;
  @Value("${depth:1}")
  int      depth;
  @Value("${assetTypes:jpg,css,js,png}")
  String[] assetTypes;
  private WebDictionary    webDictionary;
  private RestTemplate     restTemplate;
  private AssetLogger      assetLogger;
  private Consumer<String> debugLogger;
  private Url              root;

  {
    LINK_MATCHERS.put("src=\"", "\"");
    LINK_MATCHERS.put("src=\'", "\'");
    LINK_MATCHERS.put("href=\"", "\"");
    LINK_MATCHERS.put("href=\'", "\'");
  }

  @Autowired
  public WebCrawlerService(
          RestTemplate restTemplate,
          WebDictionary webDictionary,
          AssetLogger assetLogger,
          Consumer<String> debugLogger
  ) {
    this.webDictionary = webDictionary;
    this.restTemplate = restTemplate;
    this.assetLogger = assetLogger;
    this.debugLogger = debugLogger;
  }

  public void startCrawl() {
    root = Url.ofFull(domain);

    webDictionary.add(root);

    assetLogger.startLog();

    crawl(root, 0);

    assetLogger.endLog();
  }

  private void crawl(
          Url current,
          int depth
  ) {
    if (depth <= this.depth) {
      try {
        String html = restTemplate.getForObject(
                current.getValue(),
                String.class
        );

        if (html != null && html.contains("<html")) {
          List<Url> unvisitedLinks = new ArrayList<>();

          crawlHtml(current, html, unvisitedLinks);

          if (debug) {
            debugLogger.accept("Stumbled upon new links on " +
                               current.getValue() +
                               ": " +
                               unvisitedLinks);
          }

          logAssets(current);

          unvisitedLinks.forEach(link -> crawl(link, depth + 1));
        }
      } catch (Exception ignored) {
      }
    }
  }

  private void logAssets(
          Url current
  ) {
    webDictionary.traverse(
            current,
            WebDictionary.FOR_ALL_ASSETS,
            webDictionary.consumeUrl(assetLogger.getLogger())
    );
  }

  private void addToDictionary(
          Url current,
          Url parent
  ) {
    Arrays.stream(assetTypes)
          .filter(current.getValue()::endsWith)
          .findAny()
          .map(asset ->
                       webDictionary.addAsset(
                               current,
                               asset,
                               parent
                       ))
          .orElseGet(() -> webDictionary.add(current));
  }

  private void crawlHtml(
          Url parent,
          String html,
          List<Url> links
  ) {
    Matcher matcher = matchLink(html);

    if (matcher != null) {
      String subHtml = html.substring(matcher.startIndex);

      crawlHtml(parent, subHtml, links);

      int endLinkIndex = subHtml.indexOf(matcher.end);

      if (endLinkIndex != -1) {
        Url parsedLink = Url.of(subHtml.substring(0, endLinkIndex));

        if (!parsedLink.isFullPath() || root.hasChild(parsedLink)) {
          Url child = root.hasChild(parsedLink)
                      ? parsedLink
                      : root.append(parsedLink);

          if (!webDictionary.contains(child)) {
            links.add(child);
          }

          addToDictionary(child, parent);
        }
      }
    }
  }

  private Matcher matchLink(String html) {
    return LINK_MATCHERS.keySet()
                        .stream()
                        .filter(html::contains)
                        .findAny()
                        .map((start) -> matchStart(html, start))
                        .orElse(null);
  }

  private Matcher matchStart(String html, String start) {
    Matcher matcher = new Matcher();

    matcher.startIndex = html.indexOf(start) + start.length();
    matcher.start = start;
    matcher.end = LINK_MATCHERS.get(start);

    return matcher;
  }

  private class Matcher {
    public int    startIndex;
    public String start;
    public String end;
  }
}
