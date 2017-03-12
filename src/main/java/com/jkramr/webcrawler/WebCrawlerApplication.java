package com.jkramr.webcrawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WebCrawlerApplication {


  public static final String HTTPS_PREFIX = "https://";
  public static final String HTTP_PREFIX = "http://";

  public static void main(String[] args) {
    SpringApplication.run(WebCrawlerApplication.class, args);
  }

  @Value("${domain:google.com}")
  String domain;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
    return args -> {
      String url = HTTPS_PREFIX + domain;

      String response = restTemplate.getForObject(url, String.class);

      System.out.println(response);
    };
  }
}