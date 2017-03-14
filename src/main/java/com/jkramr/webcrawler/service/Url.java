package com.jkramr.webcrawler.service;

import lombok.Data;

@Data
public class Url {
  public static final String HTTPS_PREFIX = "https://";

  private final String value;

  private Url(String url) {
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }

    if (url.startsWith("/")) {
      url = url.substring(1);
    }

    this.value = url;
  }

  public static boolean isFullPath(String url) {
    return url.startsWith(HTTPS_PREFIX);
  }

  public static Url ofFull(String domain) {
    return isFullPath(domain)
           ? new Url(domain)
           : new Url(HTTPS_PREFIX + domain);
  }

  public static Url of(String url) {
    return new Url(url);
  }

  public Url append(Url url) {
    return new Url(value + "/" + url.getValue());
  }


  public boolean hasChild(Url childUrl) {
    return childUrl != null &&
           childUrl.getValue() != null &&
           childUrl.getValue().contains(value);
  }

  public boolean isFullPath() {
    return isFullPath(value);
  }
}
