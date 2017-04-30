package main.java;

import java.util.*;

public class DataAnalyzer {
    private ArrayList<Document> documents;

    /* HashMap that includes a mapping from a token to his index
        in a lexicographically ordered vector
     */
    private HashMap<String, Integer> indexedTerms;

    /**
     * A vector of all words, that counts in how many sentences the word is present in
     */
    private Vector<Double> globalDFvec;
    private Similarity similarity;
    private Flow flow;
    public  DocumentUniqueness documentUniqueness; //less code, instead of introducing special functions

    public DataAnalyzer(ArrayList<Document> documents) {
        this.documents = documents;
        indexedTerms = new HashMap<String,Integer>();
    }

    public void calculateData() throws Exception {
        // Create a map from token -> index
        createMap(createSet());

        // Create a count vector of words for each sentence
        createTFSentenceVector();

        // Initialize the global DF
        initGlobalDF();

        // Initialize TF-IDF of each sentence
        setTF_IDFVector();
        
        // Initialize the document uniqueness vector
        createDocumentUniqueness();

        // Build probability matrix
        HashMap<String, HashMap<String, Double>> probabilityMatrix = buildProbabilityMatrix(buildSimilarityMatrix());

        similarity = new Similarity(probabilityMatrix);
        flow = new Flow(documents, probabilityMatrix);
        long flowTime = System.currentTimeMillis();
        flow.calculateFlows();
        System.out.println("merge time: " + Long.toString((System.currentTimeMillis() - flowTime) / 1000) + " seconds.");
    }

    private void createTFSentenceVector() {
        // Create a vector for each token in each sentence
        for (Document doc : documents) {
            for (Sentence sentence : doc.getSentences()) {
                Vector<Integer> countVector = initVecInteger(indexedTerms.size());

                for (Term term : sentence.getTerms()) {
                    int index = indexedTerms.get(term.toString());
                    int currentScore = countVector.get(index);
                    countVector.setElementAt(currentScore + 1, index);
                }

                sentence.setCountVector(countVector);
            }
        }
    }
    
    
    private void createDocumentUniqueness(){
    	HashSet<String> docs = new HashSet<String>();
    	  for (Document doc : documents) {
    		  if(doc.getSentences().size()>0 && doc.getSentences().iterator().hasNext()){
    			  docs.add(doc.getSentences().iterator().next().getDocID());
    		  }
    	  }
    	  
    	  documentUniqueness = new DocumentUniqueness(docs);
    } 

    /**
     * Creates a sorted set of every token (across all documents without duplications)
     * @return
     */
    private SortedSet<String> createSet() {
        SortedSet<String> allTermsSet = new TreeSet<String>();
        for (Document doc : documents) {
            allTermsSet.addAll(doc.getSortedTerms());
        }
        return allTermsSet;
    }

    /**
     * Creates a map from term -> index in an ordered vector
     * @param allTermsSet
     */
    private void createMap(SortedSet<String> allTermsSet) {
        Integer counter = 0;
        for (String term : allTermsSet) {
            indexedTerms.put(term,counter);
            counter++;
        }
    }

    private void initGlobalDF() {
        globalDFvec = initVecDouble(indexedTerms.size());

        // Iterate over all sentence's scores
        for (Document doc : documents) {
            for (Sentence sentence : doc.getSentences()) {
                Vector<Integer> countVec = sentence.getCountVector();

                // We update the global DF vector, using the sentence's vectors
                // If a word appears more than once in a sentence we only count the first.
                for (int i = 0; i < indexedTerms.size(); i++) {
                    if (countVec.get(i) > 0) {
                        Double currentScore = this.globalDFvec.get(i);
                        this.globalDFvec.setElementAt(currentScore + 1, i);
                    }
                }
            }
        }

        int numOfSentences = getNumOfSentences();
        for (int i = 0; i < indexedTerms.size(); i++){
            Double currentScore = this.globalDFvec.get(i);
            currentScore = Math.log((currentScore)/numOfSentences);
            this.globalDFvec.setElementAt(currentScore, i);
        }
    }

    private int getNumOfSentences(){
        int res = 0;
        for (Document doc : documents) {
            res = res + doc.getSentences().size();
        }
        return res;
    }

    private void setTF_IDFVector(){
        for (Document doc : documents) {
            for (Sentence sentence : doc.getSentences()) {
               Vector<Double> TfIdfVector = multiplyVecElements(globalDFvec,sentence.getCountVector());
                sentence.setTfIdfVector(TfIdfVector);
            }
        }
    }

    private Vector<Double> multiplyVecElements(Vector<Double> v1, Vector<Integer> v2) {
        if (v1.size() != v2.size()) {
            throw new AssertionError();
        }
        Vector<Double> res =  initVecDouble(indexedTerms.size());
        for (int i = 0; i < v1.size(); i++) {
            res.setElementAt(v1.get(i) * v2.get(i), i);
        }
        return res;
    }

    private Vector<Integer> initVecInteger(int size) {
        Vector<Integer> vec = new Vector<Integer>(size);

        for (int i = 0; i < size; i++) {
            vec.add(i, 0);
        }

        return vec;
    }

    private Vector<Double> initVecDouble(int size) {
        Vector<Double> vec = new Vector<Double>(size);

        for (int i = 0; i < size; i++) {
            vec.add(i, 0.0);
        }

        return vec;
    }

    private double cosineSimilarity(Vector<Double> v1, Vector<Double> v2) {
        if (v1.size() != v2.size()) {
            throw new AssertionError();
        }
        double res = 0;
        // Multiply the vec elements
        for (int i = 0; i < v1.size(); i++) {
            res += v1.get(i) * v2.get(i);
        }
        double v1_len = getVecLength(v1);
        double v2_len = getVecLength(v2);
        return res / (v1_len * v2_len);
    }

    private double getVecLength(Vector<Double> v1) {
        double res = 0;
        for (Double d : v1) {
            res += d * d;
        }
        return Math.sqrt(res);
    }

    public HashMap<String, HashMap<String, Double>> buildSimilarityMatrix() {
        ArrayList<Sentence> allSentences = new ArrayList<Sentence>();

        // Calculate number of sentences
        for (Document doc : documents) {
            allSentences.addAll(doc.getSentences());
        }

        // Create the similarity matrix
        HashMap<String, HashMap<String, Double>> matrix = new HashMap<String, HashMap<String, Double>>();

        // Loop over all sentences (using indices)
        for (int i = 0; i < allSentences.size(); i++) {
            Sentence first = allSentences.get(i);
            HashMap<String, Double> firstMap = new HashMap<String, Double>();
            matrix.put(first.toString(), firstMap);

            for (int j = 0; j < allSentences.size(); j++) {
                Sentence second = allSentences.get(j);
                double similarity = cosineSimilarity(first.getTfIdfVector(), second.getTfIdfVector());
                firstMap.put(second.toString(), similarity);
            }
        }

        return matrix;
    }

    private HashMap<String, HashMap<String, Double>> buildProbabilityMatrix(HashMap<String, HashMap<String, Double>> similarityMatrix) throws Exception {
        HashMap<String, HashMap<String, Double>> probabilityMatrix = new HashMap<String, HashMap<String, Double>>();
        Classifier classifier = new Classifier();

        for (String str1 : similarityMatrix.keySet()) {
            HashMap<String, Double> newMap = new HashMap<String, Double>();
            probabilityMatrix.put(str1, newMap);

            for (String str2 : similarityMatrix.get(str1).keySet()) {
                newMap.put(str2, classifier.classifyVal(similarityMatrix.get(str1).get(str2)));
            }
        }

        return probabilityMatrix;
    }

    public double getFlowVal(String s1, String s2) {
        return flow.getFlowVal(s1, s2);
    }

    public double getSupportValue(Sentence sentence) {
        return similarity.getSupportValue(sentence);
    }

    public void updateChosenSentence(Sentence sentence) {
        similarity.updateChosenSentence(sentence);
        documentUniqueness.punishDocument(sentence.getDocID());
    }
}