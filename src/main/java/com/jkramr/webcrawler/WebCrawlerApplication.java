package com.jkramr.webcrawler;

import com.jkramr.webcrawler.service.AssetLogger;
import com.jkramr.webcrawler.service.JsonAssetLogger;
import com.jkramr.webcrawler.service.WebCrawlerService;
import com.jkramr.webcrawler.service.WebDictionary;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

@SpringBootApplication
public class WebCrawlerApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebCrawlerApplication.class, args);
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public AssetLogger assetLogger(WebDictionary webDictionary) {
    return new JsonAssetLogger(webDictionary);
  }

  @Bean
  public Consumer<String> debugLogger() {
    return System.out::println;
  }

  @Bean
  public CommandLineRunner run(WebCrawlerService webCrawlerService)
          throws Exception {
    return args -> webCrawlerService.startCrawl();
  }
}