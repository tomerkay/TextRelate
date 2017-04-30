package TextMergeProject;

import javafx.util.Pair;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;

public class TextBuilder {
    private static final int NUM_OF_SENTENCES_IN_PARAGRAPH  = 8;
    private static final boolean DEBUG_FLAG  = true; // print the source of a sentence
    
    public static double PORTIONS_COUNT=1;
    public static double CURRENT_PORTION=1;

    private DataAnalyzer analyzer;
    private ArrayList<Paragraph> paragraphs;
    private ArrayList<Document> documents;

    public TextBuilder(String[] paths){
        InputParser parser = new InputParser();
        this.documents = parser.parseDocuments(paths);
        paragraphs = new ArrayList<Paragraph>();
        analyzer = new DataAnalyzer(documents);
    }

    public TextBuilder(ArrayList<Pair<String, String>> text_list) {
        InputParser parser = new InputParser();
        this.documents = parser.parseDocuments(text_list);
        paragraphs = new ArrayList<Paragraph>();
        analyzer = new DataAnalyzer(documents);
    }

    /**
     *
     * @param i - paragraph's number
     * @return - normalized score between 0 to 1
     */
    private Pair<Double,Sentence> calculateScore(int i, int totalLines){
        double bestScore = -1;
        Sentence bestSentence = null;
        /* can be calculated only once, sentence to be added is not relevant for this score */
        double capacityScore = calcCapacityScore(i);

        for (Document cur_doc : documents) {
            for (Sentence sentence : cur_doc.getSentences()) {
                double flowScore = calcFlowScore(i,sentence, totalLines);
                double simScore = calcSimScore(sentence);
                double lengthScore = calcLengthScore(sentence.getTerms().size());
                double documentUnqiunessScore = analyzer.documentUniqueness.getDocumentUniqueness(sentence.getDocID());
                double res = Math.pow(lengthScore, 0.13) *
                             Math.pow(flowScore, 0.47) *
                             Math.pow(simScore, 0.30) *
                             Math.pow(capacityScore, 0.10)*
                             Math.pow(documentUnqiunessScore, analyzer.documentUniqueness.documentUniquenessWeight);
                
                if (res > bestScore) {
                    bestScore = res;
                    bestSentence = sentence;
                }
            }
        }
        Pair<Double,Sentence> finalScore = new Pair<Double,Sentence>(bestScore,bestSentence);
        return  finalScore;
    }

    /**
     * each paragraph has
     * @param i - paragraph's number
     * @return - score between 0 to 1. The more sentences the paragraph has, the lower score will be
     */
    private double calcCapacityScore(int i){
        int currNumOfSentences = paragraphs.get(i).getCurrSize();
        double result =  1 - ((double)currNumOfSentences/paragraphs.get(i).getCapacity());
        return  0.5 + 0.5 * (result-Math.random());
    }

    private double calcLengthScore(int length) {
    	if(length<3){return 0;}
        final double MEAN = 25;
        final double STANDARD_DEVIATION = 9;
        NormalDistribution dist = new NormalDistribution(MEAN, STANDARD_DEVIATION);
        int interval = 3;
        return dist.probability(length - interval, length + interval) / dist.probability(MEAN - interval, MEAN + interval);
    }

    /**
     * if paragraph is empty, sentence should be added only if he is the first in his original paragraph also
     *  else return the flow val, the the probability that the given sentence will follow the last sentence in the paragraph.
     * @param i - paragraph's number
     * @param sentence - the sentence we consider to add to the i'th paragraph,
     * @return score between 0 to 1
     */
    private double calcFlowScore(int i, Sentence sentence, int totalLines) {
    	
    	double currStart = CURRENT_PORTION/PORTIONS_COUNT;
    	double currEnd = (CURRENT_PORTION+1)/PORTIONS_COUNT;
    	
        Sentence lastInParagraph = paragraphs.get(i).getLastSentence();
        double result;
        if (lastInParagraph != null){
            result = analyzer.getFlowVal(lastInParagraph.toString(), sentence.toString());
            // Calculate approximated distance from wanted paragraph according to position in the document
            double originalLoc = sentence.getPosInDoc();
            double newLoc = currStart+(currEnd-currStart)*((i * NUM_OF_SENTENCES_IN_PARAGRAPH + paragraphs.get(i).getCurrSize()) / (double)totalLines);
            NormalDistribution currND = new NormalDistribution(originalLoc, 0.8);
            result = result*(currND.probability(newLoc - 0.1, newLoc + 0.1) / currND.probability(originalLoc - 0.1, originalLoc + 0.1));
        }
        else if (!sentence.isFirstInParagraph()) {
            result = 0;
        }else{
            // Calculate approximated distance from wanted paragraph according to position in the document
            double originalLoc = sentence.getPosInDoc();
            double newLoc = currStart+(currEnd-currStart)*((i * NUM_OF_SENTENCES_IN_PARAGRAPH + paragraphs.get(i).getCurrSize()) / (double)totalLines);
            NormalDistribution currND = new NormalDistribution(originalLoc, 0.3);
            result = currND.probability(newLoc - 0.1, newLoc + 0.1) / currND.probability(originalLoc - 0.1, originalLoc + 0.1);
        }

        return result;
    }

    /**
     * @param sentence - the sentece we consider to add to the i'th paragraph,
     * @return score between 0 to 1, witch is the support value of this sentence
     * (the more different the current sentece is from the rest of the chosen sentences,
     * the higher score will be.
     */
    private double calcSimScore(Sentence sentence){
        return analyzer.getSupportValue(sentence);
    }


    private void insertNextSentence(int totalLines){
        int bestParagraph = -1;
        double bestScore = -1;
        Sentence bestSentence = null;
        Pair<Double, Sentence> score = null;
        for(int i = 0 ; i < paragraphs.size() ; i ++){
           score = calculateScore(i, totalLines);
            if(score.getKey() > bestScore){
                bestScore = score.getKey();
                bestSentence = score.getValue();
                bestParagraph = i;
            }
        }
        paragraphs.get(bestParagraph).addSentence(bestSentence);
        analyzer.updateChosenSentence(bestSentence);
    }

    public String build(int numOfLines) throws Exception {
        analyzer.calculateData();

        // Get upper bound of size
        int numOfParagraphs = (numOfLines + (NUM_OF_SENTENCES_IN_PARAGRAPH - 1)) / NUM_OF_SENTENCES_IN_PARAGRAPH;

        for(int i = 0 ; i < numOfParagraphs ; i++) {
            paragraphs.add(new Paragraph(NUM_OF_SENTENCES_IN_PARAGRAPH));
        }

        for (int i = 0; i < numOfLines; i++) {
            insertNextSentence(numOfLines);
        }

        StringBuilder builder = new StringBuilder();
        for (Paragraph curr : this.paragraphs) {
            if(DEBUG_FLAG) {
                builder.append(curr.toStringDebugMode());
            } else {
                builder.append(curr.toString());
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}
