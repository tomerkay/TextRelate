package TextMergeProject;

import java.util.ArrayList;
import java.util.Vector;

public class Sentence {
    private String text;
    private ArrayList<Term> terms;
    private Vector<Integer> wordCountVector;
    private Vector<Double> TfIdfVector;
    private Boolean firstInParagraph;
    private double posInDoc;
    private String docID;

    public Sentence(String text, ArrayList<Term> terms, Boolean firstInParagraph, double posInDoc, String docID) {
        this.terms = terms;
        this.text = text;
        this.firstInParagraph = firstInParagraph;
        this.posInDoc = posInDoc;
        this.docID = docID;
    }

    public String getDocID() {return docID;}
    public boolean isFirstInParagraph() {
        return firstInParagraph;
    }
    public ArrayList<Term> getTerms() {
        return this.terms;
    }
    public double getPosInDoc() { return posInDoc; }
    public void setCountVector(Vector<Integer> vec) {
        wordCountVector = vec;
    }
    public void setTfIdfVector(Vector<Double> vec) {
        TfIdfVector = vec;
    }
    public Vector<Double> getTfIdfVector() {
        return TfIdfVector;
    }

    public Vector<Integer> getCountVector() { return wordCountVector; }
    public String toString() {
        return text;
    }
}
