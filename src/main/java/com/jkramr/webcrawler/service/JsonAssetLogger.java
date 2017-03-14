package com.jkramr.webcrawler.service;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;

@Data
public class JsonAssetLogger
        implements AssetLogger {
  @NonNull
  Consumer<String> logger;

  public JsonAssetLogger(Consumer<String> logger) {
    this.logger = logger;
  }

  @Override
  public void startLog() {
    logger.accept("[");
  }

  @Override
  public void endLog() {
    logger.accept("]");
  }

  @Override
  public void startForPage(Url current) {
    logger.accept("  {");
    logger.accept("    \"url\": \"" + current + "\",");
    logger.accept("    \"assets\": [");
  }

  @Override
  public void endForPage() {
    logger.accept("    ]");
    logger.accept("  }");
  }

  @Override
  public void logAsset(String asset) {
    logger.accept("      \"" + asset + "\",");
  }
}
