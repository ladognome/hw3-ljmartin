package edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems;

import java.util.ArrayList;
import java.util.Map;

public class Cosine implements Comparable{
  double value;

  Map<String, Integer> map;
  int r;

  public Cosine(double cos, Map<String, Integer> words, int rel) {
    map = words;
    value = cos;
    r=rel;
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

  public void setWords(Map<String, Integer> m) {
    map = m;
  }

  public void setCos(double v) {
    value = v;
  }
  public void setRel(int rel) {
    r = rel;
  }

  public ArrayList<Cosine> sort(ArrayList<Cosine> a) {

    return null;
  }

  @Override
  public int compareTo(Object comparestu) {
    double compareCos = ((Cosine) comparestu).getCos();
    /* For Ascending order */
    // return this.value-compareCos;

    // highest is best

    /* For Descending order */
    return (int)(compareCos - this.value);
  }



}
