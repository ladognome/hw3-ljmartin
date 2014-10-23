package edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems;

import java.util.ArrayList;
import java.util.Map;

/*
 * New object to hold cosine values for documents
 */

public class Cosine implements Comparable<Cosine> {
  double value;

  Map<String, Integer> map;

  int r;

  String sent;

  public Cosine(double cos, Map<String, Integer> words, int rel, String sentence) {
    map = words;
    value = cos;
    r = rel;
    sent = sentence;
  }

  public Map<String, Integer> getWords() {
    return map;
  }

  public double getCos() {
    return value;
  }

  public int getRel() {
    return r;
  }

  public String getSent() {
    return sent;
  }

  public void setWords(Map<String, Integer> m) {
    map = m;
  }

  public void setCos(double v) {
    value = v;
  }

  public void setRel(int rel) {
    r = rel;
  }

  public void setSent(String sentence) {
    sent = sentence;
  }

  public ArrayList<Cosine> sort(ArrayList<Cosine> a) {

    return null;
  }

  @Override
  public int compareTo(Cosine o) {
    double compareCos = o.getCos();
    if (compareCos > this.value)
      return 1;
    else if (compareCos < this.value)
      return -1;
    else
      return 0;
  }

}
