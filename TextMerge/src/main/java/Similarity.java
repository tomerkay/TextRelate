package main.java;

import java.util.Collections;
import java.util.HashMap;

public class Similarity {
    HashMap<String, HashMap<String, Double>> possibilitiesMatrix;
    private HashMap<String, Double> supportVector;

    public Similarity(HashMap<String, HashMap<String, Double>> possibilitiesMatrix){
        this.supportVector = new HashMap<String, Double>();
        this.possibilitiesMatrix = possibilitiesMatrix;
        initializeSupport();
    }
    private void initializeSupport(){
        for (String cur : possibilitiesMatrix.keySet()) {
            Double supportVal = 0.0;
            HashMap<String,Double> map = possibilitiesMatrix.get(cur);
            
            for(String str : map.keySet()){
            	Double currstrsim=map.get(str);
            	if(currstrsim<0.98){ //extreme similarity is a sign of a template section, we prevent the template from contributing to it own support.
            		supportVal += currstrsim;
            	}
            }
            supportVector.put(cur,supportVal);
        }

        normalize();
    }


    /* for TextBuilder, given a sentece calc its sim score */
    public Double getSupportValue(Sentence sentence){
        return supportVector.get(sentence.toString());
    }

    /* update support vector given the chosen sentence */
    public void updateChosenSentence(Sentence sentence){
        punish(sentence.toString());
    }

    private Double getSim(String a, String b){
    	if(a.equals(b)){return 1.0;}
    	
        HashMap<String,Double> map = possibilitiesMatrix.get(a);
        return map.get(b);
    }

    private void punish(String chosenString){
        HashMap<String,Double> newSupportVector = new  HashMap<String,Double>();
        for(String str : supportVector.keySet()){
            Double currentVal = supportVector.get(str);
            currentVal = (1- getSim(chosenString, str))*currentVal;
            newSupportVector.put(str,currentVal);
        }

        this.supportVector = newSupportVector;
    }

    private void normalize() {
        double maxSupport = Collections.max(supportVector.values());
        for (String s : supportVector.keySet()) {
            supportVector.put(s, supportVector.get(s) / maxSupport);
        }
    }
}
