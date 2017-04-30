package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Summarizer {
    HashMap<String, HashMap<String, Double>> similarityMatrix;
    HashMap<String, HashMap<String, Double>> possibilitiesMatrix;
    private HashMap<String, Double> supportVector;
    private int allSentences;
    private int size;

    public Summarizer(HashMap<String, HashMap<String, Double>> similarityMatrix,
               HashMap<String, HashMap<String, Double>> possibilitiesMatrix, int allSentences, int size){
        this.supportVector = new HashMap<String, Double>();
        this.possibilitiesMatrix = possibilitiesMatrix;
        this.similarityMatrix = similarityMatrix;
        this.allSentences = allSentences;
        this.size = Math.min(size,allSentences);
    }
    private void initializeSupport(){
        for (String cur : possibilitiesMatrix.keySet()) {
            Double supportVal = 0.0;
            HashMap<String,Double> map = possibilitiesMatrix.get(cur);
            for(String str : map.keySet()){
                supportVal += map.get(str);
            }
            supportVector.put(cur,supportVal);
        }
    }

    private String findMaxSupport(){
        Double maxVal = -1.0; // lower than any possible value
        String maxString = null;
        for(String str : supportVector.keySet()){
            Double val = supportVector.get(str);
            if (val > maxVal){
                maxVal = val;
                maxString = str;
            }
        }
        return maxString;
    }

    private Double getSim(String a, String b){
        HashMap<String,Double> map = similarityMatrix.get(a);
        return map.get(b);
    }

    private void punish(String chosenString){
        HashMap<String,Double> map = possibilitiesMatrix.get(chosenString);
        HashMap<String,Double> newSupportVector = new  HashMap<String,Double>();
        for(String str : supportVector.keySet()){
            Double currentVal = supportVector.get(str);
            currentVal = (1- getSim(chosenString, str))*currentVal;
            newSupportVector.put(str,currentVal);
        }
        this.supportVector = newSupportVector;

    }

    private ArrayList<String> rank(){
     ArrayList<String> chosenSentences  = new ArrayList<String>();
        for(int i = 0; i < size ; i++){
            String cur = findMaxSupport();
            punish(cur);
            supportVector.remove(cur);
            chosenSentences.add(cur);
        }
        return chosenSentences;
    }

    public void summarize() throws IOException{
        initializeSupport();
        ArrayList<String> lines = rank();
        PrintWriter os = new PrintWriter("summary.txt" , "UTF-8");
        for(int i = 0 ; i < lines.size(); i++) {

            os.println(lines.get(i));

        }
        os.flush();
        os.close();
    }

}


