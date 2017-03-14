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

  public JsonAssetLogger(WebDictionary webDictionary) {
    this.logger =
            url -> poorMansJsonOutput(
                    url,
                    webDictionary.getAssets(
                            Url.of(url))
            );
  }

  @Override
  public void startLog() {
    System.out.println("[");
  }

  @Override
  public void endLog() {
    System.out.println("]");
  }

  private void poorMansJsonOutput(
          String url,
          List<String> assets
  ) {
    System.out.println("  {");
    System.out.println("    \"url\": \"" + url + "\",");
    System.out.println("    \"assets\": [");

    String offset = "      ";

    assets.forEach(asset -> System.out.println(offset + "\"" + asset + "\","));

    System.out.println("    ]");
    System.out.println("  }");
  }
}
