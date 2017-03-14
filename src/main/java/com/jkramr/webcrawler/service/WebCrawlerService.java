package com.jkramr.webcrawler.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@Component
public class WebCrawlerService {

  private static List<Matcher> LINK_MATCHERS = new ArrayList<>();

  static {
    LINK_MATCHERS.add(new Matcher("https://", "\"", true));
    LINK_MATCHERS.add(new Matcher("https://", "\'", true));

    LINK_MATCHERS.add(new Matcher("href=\"", "\"", false));
    LINK_MATCHERS.add(new Matcher("href=\'", "\'", false));

    LINK_MATCHERS.add(new Matcher("src=\"", "\"", false));
    LINK_MATCHERS.add(new Matcher("src=\'", "\'", false));
  }

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
        String content = restTemplate.getForObject(
                root.getValue(),
                String.class
        );

        boolean isHtmlPage = content != null && content.contains("<html");

        if (isHtmlPage) {
          List<Url> unvisitedLinks = new ArrayList<>();

          crawlHtml(current, content, unvisitedLinks);

          if (debug) {
            debugLogger.accept("Stumbled upon new links on " +
                               current.getValue() +
                               ": " +
                               unvisitedLinks);
          }

          logAssets(current);

          unvisitedLinks.forEach(link -> crawl(
                  link,
                  depth + 1
          ));
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
    MatchedLink matchedLink = matchLink(html);

    if (matchedLink != null && matchedLink.subHtml != null) {
      String subHtml = matchedLink.subHtml;

      Url parsedLink = matchedLink.link;

      if (!parsedLink.isFullPath() || root.hasChild(parsedLink)) {
        Url child = root.hasChild(parsedLink)
                    ? parsedLink
                    : root.append(parsedLink);

        if (!webDictionary.contains(child) &&
            !links.contains(child)) {
          links.add(child);
        }

        addToDictionary(child, parent);
      }

      crawlHtml(parent, subHtml, links);
    }
  }

  private boolean visitLink(Url child) {
    try {
      String childContent = restTemplate.getForObject(
              child.getValue(),
              String.class
      );

      if (childContent != null) {
        return true;
      }

    } catch (Exception ignored) {
      return false;
    }

    return false;
  }

  private MatchedLink matchLink(String html) {
    MatchedLink matchedLink = new MatchedLink();

    Matcher matcher = LINK_MATCHERS
            .stream()
            .filter(m -> html.contains(m.start))
            .min(Comparator.comparing(m -> html.indexOf(m.start)))
            .orElse(null);

    if (matcher == null) {
      return null;
    }

    int startIndex = html.indexOf(matcher.start);

    startIndex = matcher.included
                 ? startIndex
                 : startIndex + matcher.start.length();

    String subHtml = html.substring(startIndex);

    int endIndex = subHtml.indexOf(matcher.end);

    if (endIndex == -1) {
      return matchedLink;
    }

    matchedLink.link = Url.of(subHtml.substring(0, endIndex));

    matchedLink.subHtml = subHtml.substring(endIndex);

    return matchedLink;
  }

  @Data
  private static class Matcher {
    public final String  start;
    public final String  end;
    public final boolean included;
  }

  private class MatchedLink {
    String subHtml;
    Url    link;
  }
}
