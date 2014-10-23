package edu.cmu.lti.f14.hw3.hw3_ljmartin.annotators;

import java.util.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_ljmartin.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Document doc = (Document) iter.get();
      createTermFreqVector(jcas, doc);
    }

  }

  /**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   *
   * @param doc
   *          input text
   * @return a list of tokens.
   */

  List<String> tokenize0(String doc) {
    List<String> res = new ArrayList<String>();

    for (String s : doc.split("\\s+"))
      res.add(s);
    return res;
  }

  /**
   * Tokenizing and putting term-frequency counts into the CAS
   * @param jcas The cas we will be putting things in
   * @param doc The document we will be annotating
   */

  private void createTermFreqVector(JCas jcas, Document doc) {

    String docText = doc.getText();
    // construct a vector of tokens and update the tokenList in CAS
    // use tokenize0 from above
    List<String> ls = tokenize0(docText);
    Map<String, Integer> map = new HashMap<String, Integer>();
    Collection<Token> token_collection = new ArrayList<Token>();
    for (String d : ls) {
      if (map.containsKey(d)) {
        int inc = map.get(d) + 1;
        map.put(d, inc);
      } else {
        map.put(d, 1);
      }
    }
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      Token t = new Token(jcas);
      String k = pairs.getKey().toString();
      int v = Integer.parseInt(pairs.getValue().toString());
      t.setFrequency(v);
      t.setText(k);
      token_collection.add(t);
      it.remove(); // avoids a ConcurrentModificationException
    }
    // use util tool to convert to FSList
    doc.setTokenList(Utils.fromCollectionToFSList(jcas, token_collection));
    doc.addToIndexes(jcas);

  }

}
