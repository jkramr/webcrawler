package com.jkramr.webcrawler.service;

import java.util.function.Consumer;

public interface AssetLogger {

  void startLog();

  Consumer<String> getLogger();

  void endLog();

  void startForPage(Url current);

  void endForPage();

  void logAsset(String asset);
}
