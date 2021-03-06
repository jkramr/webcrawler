package com.jkramr.webcrawler.service;

import lombok.Data;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Data
@Component
public class WebDictionary {

  public static Predicate<TrieNode> FOR_ALL_ASSETS =
          node -> node.getAssets() != null;
  TrieNode head;

  public Consumer<TrieNode> consumeUrl(Consumer<String> consumer) {
    return node -> consumer.accept(getUrl(node));
  }

  public Url add(Url url) {
    if (url == null) {
      return null;
    }

    if (head == null) {
      head = new TrieNode(0);
    }

    insert(url.getValue());

    return url;
  }

  public Url addAsset(Url asset, String assetType, Url parent) {
    if (parent == null || asset == null) {
      return null;
    }

    String assetUrl  = asset.getValue();
    String parentUrl = parent.getValue();

    TrieNode parentNode = insert(parentUrl);

    TrieNode assetNode = insert(assetUrl);

    parentNode.addAsset(assetType, assetNode);

    return asset;
  }

  public boolean contains(Url url) {
    if (url == null) {
      return false;
    }

    String word = url.getValue();

    if (head == null ||
        !head.hasChild(word.charAt(0))
            ) {
      return false;
    }

    TrieNode node = searchTreeDown(head, word);

    return node.depth == word.length();
  }

  boolean isEmpty() {
    return head == null;
  }

  boolean hasAssets(Url url) {
    if (url == null) {
      return false;
    }

    TrieNode trieNode = searchTreeDown(head, url.getValue());

    return trieNode.assets != null;
  }

  List<String> getAssets(Url parent) {
    List<String> assets = new ArrayList<>();

    if (parent == null) {
      return assets;
    }

    TrieNode parentNode = searchTreeDown(head, parent.getValue());

    if (parentNode.assets == null) {
      return assets;
    }

    parentNode.assets.forEach((assetType, nodes) -> {
      nodes.forEach(node -> assets.add(buildUrlUp(node)));
    });

    return assets;
  }

  public void traverse(
          Url fromUrl,
          Predicate<TrieNode> predicate,
          Consumer<TrieNode> consumer
  ) {
    if (fromUrl == null) {
      return;
    }

    TrieNode lastNode = searchTreeDown(head, fromUrl.getValue());

    traverse(lastNode, predicate, consumer);
  }

  private String getUrl(TrieNode node) {
    if (node == null) {
      return null;
    }

    return buildUrlUp(node);
  }

  private void traverse(
          TrieNode current,
          Predicate<TrieNode> predicate,
          Consumer<TrieNode> consumer
  ) {
    if (current != null && current.children != null) {
      if (predicate.test(current)) {
        consumer.accept(current);
      }

      current.children.forEach((character, child) ->
                                       traverse(
                                               child,
                                               predicate,
                                               consumer
                                       ));
    }
  }

  private TrieNode insert(String url) {
    TrieNode lastFound = searchTreeDown(head, url);

    if (lastFound.depth == url.length()) {
      return lastFound;
    }

    return insertAtLastFound(lastFound, url);
  }

  private TrieNode insertAtLastFound(TrieNode current, String url) {

    if (current.depth >= url.length()) {
      return current;
    }

    char character = url.charAt(current.depth);

    TrieNode node = new TrieNode(current.depth + 1);

    node.parent = current;
    node.value = character;

    current.add(character, node);

    return insertAtLastFound(node, url);
  }

  private TrieNode searchTreeDown(TrieNode current, String url) {
    if (current.depth >= url.length() ||
        !current.hasChild(url.charAt(current.depth))
            ) {
      return current;
    }

    char character = url.charAt(current.depth);

    return searchTreeDown(current.get(character), url);
  }

  private String buildUrlUp(TrieNode edgeNode) {

    StringBuilder stringBuilder = new StringBuilder();

    return buildUrlUp(edgeNode, stringBuilder).toString();
  }

  private StringBuilder buildUrlUp(
          TrieNode current,
          StringBuilder stringBuilder
  ) {
    if (current.parent == null) {
      return stringBuilder;
    }

    stringBuilder.insert(0, current.value);

    return buildUrlUp(current.parent, stringBuilder);
  }

  @Data
  @ToString(of = {"depth", "value"})
  private class TrieNode {
    final int depth;

    Character value;
    TrieNode  parent;

    HashMap<Character, TrieNode>    children;
    HashMap<String, List<TrieNode>> assets;

    boolean hasChild(char c) {
      return children != null && children.containsKey(c);
    }

    TrieNode get(char c) {
      if (children == null) {
        return null;
      }

      return children.get(c);
    }

    void add(char c, TrieNode childNode) {
      if (children == null) {
        children = new HashMap<>();
      }

      children.put(c, childNode);
    }

    void addAsset(String assetType, TrieNode assetNode) {
      if (assets == null) {
        assets = new HashMap<>();
      }

      assets.putIfAbsent(assetType, new ArrayList<>());

      assets.get(assetType).add(assetNode);
    }
  }
}