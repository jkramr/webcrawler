package com.jkramr.webcrawler.service;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
@Component
public class WebDictionary {

  TrieNode head;

  public boolean isEmpty() {
    return head == null;
  }

  public String add(String url) {
    if (url == null) {
      return null;
    }

    if (head == null) {
      head = new TrieNode(0);
    }

    insert(url);

    return url;
  }

  public boolean contains(String url) {
    if (head == null ||
        !head.hasChild(url.charAt(0))
            ) {
      return false;
    }

    TrieNode node = searchTreeDown(head, url);

    return node.depth == url.length();
  }

  public String addAsset(String parent, String assetType, String asset) {
    TrieNode assetNode = insert(asset);

    TrieNode parentNode = insert(parent);

    parentNode.addAsset(assetType, assetNode);

    return asset;
  }

  public boolean hasAssets(String url) {
    TrieNode trieNode = searchTreeDown(head, url);

    return trieNode.assets != null;
  }

  public List<String> getAssets(String parent) {
    List<String> assets = new ArrayList<>();

    TrieNode parentNode = searchTreeDown(head, parent);

    if (parentNode.assets == null) {
      return assets;
    }

    parentNode.assets.forEach((assetType, nodes) -> {
      nodes.forEach(node -> assets.add(buildUrlUp(node)));
    });

    return assets;
  }

  private TrieNode insert(String url) {
    TrieNode lastFound = searchTreeDown(head, url);

    if (lastFound.depth == url.length()) {
      return lastFound;
    }

    return insertAtLastFound(lastFound, url);
  }

  private TrieNode insertAtLastFound(TrieNode current, String url) {

    if (current.depth == url.length()) {
      return current;
    }

    char character = url.charAt(current.depth);

    TrieNode node = new TrieNode(current.depth + 1);

    current.add(character, node);

    return insertAtLastFound(node, url);
  }

  private TrieNode searchTreeDown(TrieNode current, String url) {

    if (current.depth == url.length() ||
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

    stringBuilder.insert(0, current.parent.get(current.value));

    return buildUrlUp(current.parent, stringBuilder);
  }

  @Data
  private class TrieNode {
    final int depth;

    char value;
    TrieNode parent;

    HashMap<Character, TrieNode>    children;
    HashMap<String, List<TrieNode>> assets;

    public boolean hasChild(char c) {
      return children.containsKey(c);
    }

    public TrieNode get(char c) {
      return children.get(c);
    }

    public void add(char c, TrieNode childNode) {
      if (children == null) {
        children = new HashMap<>();
      }

      children.put(c, childNode);
    }

    public void addAsset(String assetType, TrieNode assetNode) {
      if (assets == null) {
        assets = new HashMap<>();
      }

      assets.putIfAbsent(assetType, new ArrayList<>());

      assets.get(assetType).add(assetNode);
    }
  }
}