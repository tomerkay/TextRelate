package main.java;

import java.util.ArrayList;
import java.util.HashMap;

public class Flow {
    private static final int NUM_OF_ITERATIONS = 20;
    private static final Double PUNISH_FACTOR = 0.4;

    private ArrayList<Document> docs;
    private HashMap<String, HashMap <String, Double>> flowMatrix;
    private HashMap<String, HashMap <String, Double>> probabilityMatrix;
    private HashMap<String, HashMap <String, Double>> initialFlowMatrix;

    public Flow(ArrayList<Document> docs, HashMap<String, HashMap<String, Double>> probabilityMatrix){
        this.docs = docs;
        this.probabilityMatrix = probabilityMatrix;
        this.flowMatrix = createFlow();
        this.initialFlowMatrix = createFlow();
    }

    public void calculateFlows() {
        updateFlow(NUM_OF_ITERATIONS);
    }

    private HashMap<String, HashMap <String, Double>> createFlow(){
        HashMap<String, HashMap <String, Double>> result = new HashMap<String, HashMap <String, Double>>();
        for(int docIdx = 0 ; docIdx < docs.size() ; docIdx++) {
            Document doc = docs.get(docIdx);

            /* go over all the sentences in the doc */
            ArrayList<Sentence> sentences = doc.getSentences();
            for(int i = 0 ; i < sentences.size() ; i++){
                Sentence cur = sentences.get(i);
                /*create the line that corresponding the current sentence in the flowMatrix*/
                HashMap<String,Double> curLine = new HashMap<String, Double>();

                /*set all the cells to be 0*/
                for (Document cur_doc : docs) {
                    for (Sentence sentence : cur_doc.getSentences()) {
                        curLine.put(sentence.toString(),0.0);
                    }
                }
                /*if the current sentence is not the last in the document, set the cell that
                 correspond to the next sentnce in the doc to be 1*/
                if(i+1 != sentences.size()) {
                    Sentence nextSentence = sentences.get(i + 1);
                    curLine.remove(nextSentence.toString());
                    curLine.put(nextSentence.toString(), 1.0);
                }

                result.put(cur.toString(),curLine);
            }
        }
        return result;
    }


    /* get 2 sentences and calculate the new flow value */
    private double calcNewFlow(String s1, String s2) {
        double similaritySum = 0.0; // denominator
        double flowSum = 0.0; // numerator

        // Calculate new flow
        for (String sentence : probabilityMatrix.keySet()) {
            similaritySum += probabilityMatrix.get(sentence).get(s1);
            flowSum += probabilityMatrix.get(sentence).get(s1) * flowMatrix.get(sentence).get(s2);
        }

        return flowSum / similaritySum;
    }

    private void iterateFlow() {
        ArrayList<Sentence> allSentences = new ArrayList<Sentence>();
        for (Document doc : docs) {
            allSentences.addAll(doc.getSentences());
        }
        /* iterate over all the lines in the flowMatrix */
        for(int i = 0 ; i < allSentences.size() ; i++) {
            Sentence s1 = allSentences.get(i);

            /* update current line */
            for (int j = 0 ; j < allSentences.size() ; j++) {
                HashMap<String, Double> curLine = flowMatrix.get(s1.toString());
                Sentence s2 = allSentences.get(j);
                double newVal = calcNewFlow(s1.toString(), s2.toString());
                double oldVal = curLine.remove(s2.toString());
                curLine.put(s2.toString(), newVal * 0.2 + oldVal * 0.8);
                flowMatrix.remove(s1.toString());
                flowMatrix.put(s1.toString(),curLine);
            }
        }
    }


    private void updateFlow(int k){
        for (int i = 0 ; i < k ; i++){
            iterateFlow();
            normalize();
        }

        punish();
    }

    private void punish() {
        ArrayList<Sentence> allSentences = new ArrayList<Sentence>();
        for (Document doc : docs) {
            allSentences.addAll(doc.getSentences());
        }
        for(int i = 0 ; i < allSentences.size() ; i++) {
            Sentence s1 = allSentences.get(i);

            /* update current line */
            for (int j = 0 ; j < allSentences.size() ; j++) {
                HashMap<String, Double> curLine = flowMatrix.get(s1.toString());
                Sentence s2 = allSentences.get(j);
                double oldVal = curLine.remove(s2.toString());

                /* we punish (s1,s2) if sentence s1 and s2 belong to the same document, and s2 originally flowed s1*/
                double newVal = oldVal;
                if(initialFlowMatrix.get(s1.toString()).get(s2.toString()) == (1.0)) {
                    newVal = (1-PUNISH_FACTOR)*oldVal;
                }
                curLine.put(s2.toString(), newVal);
                flowMatrix.remove(s1.toString());
                flowMatrix.put(s1.toString(),curLine);
            }
        }
    }

    private void normalize() {
        double maxVal = -1;
        for (String s1 : flowMatrix.keySet()) {
            for (String s2 : flowMatrix.get(s1).keySet()) {
                double curr = flowMatrix.get(s1).get(s2);

                if (maxVal < curr) maxVal = curr;
            }
        }

        for (String s1 : flowMatrix.keySet()) {
            for (String s2 : flowMatrix.get(s1).keySet()) {
                flowMatrix.get(s1).put(s2, flowMatrix.get(s1).get(s2) / maxVal);
            }
        }
    }

    public double getFlowVal(String s1, String s2){
        return flowMatrix.get(s1).get(s2);
    }
}
