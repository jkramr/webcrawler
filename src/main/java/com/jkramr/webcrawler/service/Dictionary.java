package com.jkramr.webcrawler.service;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;

@Data
public class Dictionary {

  //todo STUB, implement real TRIE
  HashSet<String> temp = new HashSet<>();

  TrieNode head;

  public boolean isEmpty() {
    return temp.isEmpty();
  }

  public String add(String word) {
    if (word == null) {
      return null;
    }

    if (head == null) {
      head = new TrieNode(0);
    }

    temp.add(word);
    return word;
  }

  public boolean contains(String word) {
    return temp.contains(word);
  }

  @Data
  private class TrieNode {
    final int depth;
    HashMap<Character, TrieNode> children = new HashMap<>();
  }
}