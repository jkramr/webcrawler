package com.jkramr.webcrawler.service;

import java.util.function.Consumer;

public interface AssetLogger {

  void startLog();

  Consumer<String> getLogger();

  void endLog();
}
