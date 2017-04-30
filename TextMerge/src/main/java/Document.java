package main.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Document {
    private String path;
    private String name;
    private ArrayList<Sentence> sentences;

    public Document(String path, ArrayList<Sentence> sentences){
        this.path = path;
        this.sentences = sentences;
    }

    public ArrayList<Sentence> getSentences() {
        return this.sentences;
    }

    public SortedSet<String> getSortedTerms() {
        SortedSet<String> termsInDoc = new TreeSet<String>();
        for (Sentence sentence : sentences) {
            for (Term term : sentence.getTerms()) {
                termsInDoc.add(term.toString());
            }
        }
        return termsInDoc;
    }
}
