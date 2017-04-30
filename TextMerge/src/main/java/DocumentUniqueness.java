package main.java;

import java.util.HashMap;
import java.util.HashSet;

public class DocumentUniqueness {
	
	public static double documentUniquenessWeight=0.12; // not recommended at all to use values higher than 0.5;
	
	public HashMap<String,Double> uniquelevels = new HashMap<String,Double>();
	public DocumentUniqueness(HashSet<String> docIds){
		for(String doc:docIds){
			uniquelevels.put(doc,1.0);
		}
	}
	
	public Double getDocumentUniqueness(String docId){
		return uniquelevels.get(docId);
	}
	
	public void punishDocument(String docId){
		Double docvalue = uniquelevels.get(docId);
		uniquelevels.put(docId, docvalue*(1-documentUniquenessWeight*.9)); //must never be zero
	}

}
