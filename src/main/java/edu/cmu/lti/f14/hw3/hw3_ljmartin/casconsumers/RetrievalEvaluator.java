package edu.cmu.lti.f14.hw3.hw3_ljmartin.casconsumers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems.Cosine;
import edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_ljmartin.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_ljmartin.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

  /** query id number **/
  public ArrayList<Integer> qIdList;

  /** query and text relevant values **/
  public ArrayList<Integer> relList;

  private ArrayList<Map<String, Integer>> tokList;

  private Map<Integer, Integer> rank;

  private ArrayList<String> sentences;

  File reportFile;

  FileWriter fileWriter;

  public void initialize() throws ResourceInitializationException {

    qIdList = new ArrayList<Integer>();

    relList = new ArrayList<Integer>();

    tokList = new ArrayList<Map<String, Integer>>();

    rank = new HashMap<Integer, Integer>();

    sentences = new ArrayList<String>();

    String oPath = (String) getUimaContext().getConfigParameterValue("report");

    if (oPath == null) {
      throw new ResourceInitializationException(
              ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] { "report" });
    }
    reportFile = new File(oPath.trim());
    if (reportFile.getParentFile() != null && !reportFile.getParentFile().exists()) {
      if (!reportFile.getParentFile().mkdirs())
        throw new ResourceInitializationException(
                ResourceInitializationException.RESOURCE_DATA_NOT_VALID, new Object[] { oPath,
                    "report" });
    }
    try {
      // And open the file to be written to
      fileWriter = new FileWriter(reportFile);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

  }

  /**
   * 1. construct the global word dictionary 2. keep the word frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();

      // Make sure that your previous annotators have populated this in CAS
      FSList fsTokenList = doc.getTokenList();
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);

      qIdList.add(doc.getQueryID());
      relList.add(doc.getRelevanceValue());
      sentences.add(doc.getText());

      // convert ArrayList to Map
      Map<String, Integer> tokens = new HashMap<String, Integer>();
      for (Token t : tokenList) {
        String s = t.getText();
        int i = t.getFrequency();
        tokens.put(s, i);
      }
      tokList.add(tokens);

    }

  }

  /**
   * 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @SuppressWarnings("unchecked")
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);

    Map<String, Integer> query;
    Map<String, Integer> doc;
    ArrayList<Cosine> cosSort;
    // Loop through qids
    for (int j = 0; j < qIdList.size(); j++) {
      cosSort = new ArrayList<Cosine>();
      int rel = relList.get(j);
      if (rel == 99) { // if it's a query
        query = tokList.get(j);
        int qid = qIdList.get(j);

        // find answers for this query
        for (int k = 0; k < qIdList.size(); k++) {
          rel = relList.get(k);
          if (qIdList.get(k) == qid && rel != 99) {
            doc = tokList.get(k);
            // compute the cosine similarity measure
            double value = computeCosineSimilarity(query, doc);
            Cosine c = new Cosine(value, doc, rel, sentences.get(k));
            // put into list to be sorted
            cosSort.add(c);
          }
        }
        // compute the rank of retrieved sentences
        // sort by cos sim and look which one is the relevant one
        Collections.sort(cosSort);
        for (int r = 0; r < cosSort.size(); r++) {
          if (cosSort.get(r).getRel() == 1) {
            rank.put(qid, (r + 1));
            String s = String.format("cosine=%.4f", cosSort.get(r).getCos());
            fileWriter.write(s+ "\trank=" + (r + 1) + "\tqid=" + qid + "\trel=1\t"
                    + cosSort.get(r).getSent() + "\n");
            break;
          }
        }
      }
    }

    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
    String mrr = String.format("MRR=%.4f", metric_mrr);
    fileWriter.write(mrr);
    if (fileWriter != null) {
      try {
        fileWriter.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Calculate the cosine similarity between the query and a document
   * 
   * @param queryVector
   *          a Map<String, Integer> which holds words as keys and frequency counts as values for
   *          the query
   * @param docVector
   *          a Map<String, Integer> which holds words as keys and frequency counts as values for
   *          the document
   * @return cosine_similarity
   */
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    double cosine_similarity = 0.0;
    ArrayList<Double> ans = new ArrayList<Double>();

    // compute cosine similarity between two sentences
    double denom_x = 0.0;
    double denom_y = 0.0;
    // denominator, document
    Iterator it = docVector.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      String word = pair.getKey().toString();
      denom_y += Math.pow(docVector.get(word), 2);
    }
    // denominator, query
    it = queryVector.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      String word = pair.getKey().toString();
      denom_x += Math.pow(queryVector.get(word), 2);
    }
    // numerator
    it = docVector.entrySet().iterator();
    double denom = Math.sqrt(denom_x) * Math.sqrt(denom_y);
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      String word = pair.getKey().toString();
      if (queryVector.containsKey(word)) {
        cosine_similarity += (queryVector.get(word) * docVector.get(word));
      }
    }
    cosine_similarity /= denom;
    return cosine_similarity;
  }

  /**
   * Computes the mrr value
   * 
   * @return mrr
   */
  private double compute_mrr() {
    double metric_mrr = 0.0;

    // compute Mean Reciprocal Rank (MRR) of the text collection
    for (int i = 1; i <= rank.size(); i++) {
      metric_mrr += (1 / (double) rank.get(i));
    }
    metric_mrr *= (1 / (double) rank.size());

    return metric_mrr;
  }

}