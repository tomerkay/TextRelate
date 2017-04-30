package main.java;

import java.util.ArrayList;

public class Paragraph {
    private ArrayList<Sentence> sentences;
    private Integer capacity; // Max num of sentences

    public Paragraph(Integer capacity){
        this.capacity = capacity;
        this.sentences = new ArrayList<Sentence>();
    }

    /* get last sentece in the paragraph if exists */
    public Sentence getLastSentence() {
        if (sentences.isEmpty()){
            return null;
        }
        return sentences.get(sentences.size()-1);
    }

    public void addSentence(Sentence sentence){
        sentences.add(sentence);
    }

    public Integer getCapacity(){
        return this.capacity;
    }

    public Integer getCurrSize(){
        return sentences.size();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Sentence curr : this.sentences) {
            builder.append(curr.toString());
            builder.append("\n");
        }

        return builder.toString();
    }

    public String toStringDebugMode() {
        StringBuilder builder = new StringBuilder();
        for (Sentence curr : this.sentences) {
            builder.append(curr.toString());
            builder.append("\t\t[");
            builder.append(curr.getDocID().toString());
            builder.append("]\n");
        }

        return builder.toString();
    }
}
