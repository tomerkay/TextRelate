package TextRelateProject.QueryGenerator;

//import BingApi.NewsSearch;
import TextRelateProject.Helpers.BagOfWords;
import TextRelateProject.StringParsing.DocumentParser;
import TextRelateProject.StringParsing.OpenNlpModels;
import TextRelateProject.StringParsing.StopWords;
import TextRelateProject.StringParsing.StringParser;
import javafx.util.Pair;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

/**
 * Created by Ronen on 20/11/2016.
 */
public class QueryDocument extends DocumentParser {
    private HashMap<String, Double> tf_inv_log_scores;
    private HashMap<String, Double> scores;
    private double[][] specificWordScores;

//code comment missing part 1

    public QueryDocument(String url, String body, Document doc) {
        this.url = url;
        this.original_text = body;
        this.web_document = doc;
        tf_inv_log_scores = new HashMap<>();
    }

    public void parseAndGetScores() {
        HashMap p = BagOfWords.corpus_count;
//        long start1 = System.currentTimeMillis();
        basicParseTextStanford();
//        System.out.println("parse time: " + Long.toString(System.currentTimeMillis()-start1));
        retrieveNameFromDoc();
//        start1 = System.currentTimeMillis();
        calculateLLR();
//        System.out.println("LLR time: " + Long.toString(System.currentTimeMillis()-start1));
        markNERasImportant();
        penalizeSiteName();
        penalizeBadWords();
//        start1 = System.currentTimeMillis();
        getTfInvLogScores();
//        System.out.println("scores time: " + Long.toString(System.currentTimeMillis()-start1));
        getScores();
        getSpecificWordScores();
    }

    private void penalizeSiteName() {
        String[] name_tokens = OpenNlpModels.tokenizer.tokenize(name);
        String[] name_tokensStemmed = StringParser.stemOpenNLPSentence(name_tokens);
        for (String tok : name_tokensStemmed) {
            if (llr.containsKey(tok)) {
                llr.replace(tok, 0.0);
            }
        }
    }

    private void penalizeBadWords() {
        ArrayList<String> bad_words = new ArrayList<>();
        bad_words.add(OpenNlpModels.stemmer.stem("getty"));
        bad_words.add(OpenNlpModels.stemmer.stem("caption"));
        for (String tok : bad_words) {
            if (llr.containsKey(tok)) {
                llr.replace(tok, -5.0);
            }
        }
    }

    private void markNERasImportant() {
        ArrayList<String> ners = StringParser.stanfordNER(this.document);
        for (String ner : ners) {
            String stemmed = OpenNlpModels.stemmer.stem(ner).toLowerCase();
            if (llr.containsKey(stemmed)) {
                llr.replace(stemmed, 0.0, 1.0);
            }
        }
    }

    /**
     * gets the score for each word, with respect to it's specific place in the document.
     */
    public void getSpecificWordScores() {
        specificWordScores = new double[tokensStemmed.length][];
        for (int i=0; i<tokensStemmed.length; i++) {
            specificWordScores[i] = new double[tokensStemmed[i].length];
            for (int j=0; j<tokensStemmed[i].length; j++) {
                specificWordScores[i][j] = 0;
                if (scores.containsKey(tokensStemmed[i][j])) {
                    specificWordScores[i][j] += scores.get(tokensStemmed[i][j]);
                }
            }
        }
    }


    public void getTfInvLogScores() {
        for (String key : freqWordsStemmed.keySet()) {
            if (!tf_inv_log_scores.containsKey(key) &&
                    ((key.charAt(0) >= 'a' && key.charAt(0) <= 'z') ||
                            (key.charAt(0) >= 'A' && key.charAt(0) <= 'Z'))) {
                tf_inv_log_scores.put(key, (1+log(freqWordsStemmed.get(key))));
            }
        }
    }


    // the i'th element is the weight of the i'th sentence. if the sentence is less than 5 words, give 0.
    public double[] sentenceAvgScore(HashMap<String, Double> word_scores) {
        double[] sent_scores = new double[this.sentences.length];
        for (int i=0; i<sent_scores.length; i++) {
            if (tokens[i].length <= 5) {
                sent_scores[i] = 0;
            }
            else {
                sent_scores[i] = expressionAvgScore(tokensStemmed[i], word_scores);
            }
        }
        return sent_scores;
    }

    public double expressionAvgScore(String[] expression, HashMap<String, Double> word_scores) {
        double score = 0;
        int length = 0;
        for (String s : expression) {
            if (word_scores.containsKey(s)) {
                score += word_scores.get(s);
                length++;
            }
        }
        return score/length;
    }



    public LinkedHashMap<Pair<String, ArrayList<Pair<Integer, Integer>>>, Double> getBestNounPhrases() {
        HashMap<Pair<String, ArrayList<Pair<Integer, Integer>>>, Double> noun_phrases = new HashMap<>();
        //double[] sentence_scores = sentenceAvgScore()
        ArrayList<Pair<Integer, Integer>> phrase_indexes = new ArrayList<>();
        HashSet<String> phrases_only = new HashSet<>();
        for (int i=0; i<this.tokensStemmed.length; i++) {
            String phrase = "";
            phrase_indexes.clear();
            double phrase_score = 0;
            int length = 0;
            boolean finished = false;
            for (int j=0; j<this.tokensStemmed[i].length; j++, finished = false) {
                if (chunckedTokens[i][j].contains("B-NP") && length != 0){
                    finished = true;
                    j--;
                }
                else if (chunckedTokens[i][j].contains("NP")) {
                    if (StopWords.stopwords.contains(this.tokensStemmed[i][j])) {
                        continue;
                    }
                    phrase += tokens[i][j] + " ";
                    phrase_indexes.add(new Pair<>(i, j));
                    phrase_score += specificWordScores[i][j];
                    length++;
                }
                else {
                    finished = true;
                }
                if (finished && length > 0) {
                    phrase_score /= length;//max(log(length), 1); // TODO: want sqrt(length) instead?
                    //putMaxVal(noun_phrases, phrase, phrase_score);
                    if (!phrases_only.contains(phrase)) {
                        noun_phrases.put(new Pair<>(phrase, new ArrayList<>(phrase_indexes)), phrase_score);
                        phrases_only.add(phrase);
                    }
                    phrase_indexes.clear();
                    phrase = "";
                    phrase_score = 0;
                    length = 0;
                }
            }
            if (length > 0) {
                phrase_score /= length;//max(log(length), 1); // TODO: want sqrt(length) instead?
                //putMaxVal(noun_phrases, phrase, phrase_score);
                if (!phrases_only.contains(phrase)) {
                    noun_phrases.put(new Pair<>(phrase, new ArrayList<>(phrase_indexes)), phrase_score);
                    phrases_only.add(phrase);
                }
            }
        }
        // sort tf_inv_log_scores in descending order.
        return  noun_phrases.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x,y)-> {throw new AssertionError();},
                        LinkedHashMap::new
                ));
    }

//code comment missing part 3

    private double getNpScore(ArrayList<Pair<Integer, Integer>> indexes) {
        double score = 0;
        for (Pair<Integer, Integer> index : indexes) {
            score += scores.getOrDefault(tokensStemmed[index.getKey()][index.getValue()], 0.0);
        }
        return score/max(log(indexes.size()), 1); // TODO: want sqrt(length) instead?
    }

    /**
     * updates the np score map (potential queries).
     * @param np : the np map.
     */
    private void updateNpScores(LinkedHashMap<Pair<String, ArrayList<Pair<Integer, Integer>>>, Double> np) {
        for (Pair<String, ArrayList<Pair<Integer, Integer>>> key : np.keySet()) {
            np.replace(key, getNpScore(key.getValue()));
        }
    }

    /**
     * get np with highest score.
     * @param np : the np map.
     * @return
     */
    private Pair<String, ArrayList<Pair<Integer, Integer>>> getBestNp(LinkedHashMap<Pair<String, ArrayList<Pair<Integer, Integer>>>, Double> np) {
        Pair<String, ArrayList<Pair<Integer, Integer>>> curr_best = np.entrySet().iterator().next().getKey();
        double max_score = np.entrySet().iterator().next().getValue();
        for (Pair<String, ArrayList<Pair<Integer, Integer>>> key : np.keySet()) {
            if (np.get(key) > max_score) {
                curr_best = key;
                max_score = np.get(key);
            }
        }
        return curr_best;
    }

    /**
     * modifu 'scores', so that we penalize words occurring in the given indexes range.
     * @param indexes
     */
    private void penalize(ArrayList<Pair<Integer, Integer>> indexes) {
        for (Pair<Integer, Integer> index : indexes) {
            String word = tokensStemmed[index.getKey()][index.getValue()];
            if (scores.containsKey(word)) {
                scores.replace(word, 0.0); // TODO: what is the penalty?
            }
        }
    }

    /**
     * This method gets the best queries. need to be careful though: it changes 'specific_word_scores'.,
     * as it penalizes the words that have already occurred in the top queries.
     * @return
     */
    public ArrayList<String> getBestQueriesWithPenalty(int num) {
        LinkedHashMap<Pair<String, ArrayList<Pair<Integer, Integer>>>, Double> np = getBestNounPhrases();
        Pair<String, ArrayList<Pair<Integer, Integer>>> curr_key;
        ArrayList<String> top_queries = new ArrayList<>();
        int res_num = min(np.size(), num);
        for (; res_num>0; res_num--) {
            curr_key = getBestNp(np);
            top_queries.add(curr_key.getKey());
            penalize(curr_key.getValue());
            np.remove(curr_key);
            updateNpScores(np);
        }
        return top_queries;
    }


    public void getScores() {
        scores = new HashMap<>();
        for (String s : tf_inv_log_scores.keySet()) {
            scores.put(s, tf_inv_log_scores.get(s) * llr.get(s));
        }
    }

    public ArrayList<String> getUnitedQueries(int num) {
        ArrayList<String> best_queries = getBestQueriesWithPenalty(num);
        uniteTopQueries(best_queries);
        return best_queries;
    }

    private String queryUnion(String s1, String s2) {
        String[] s1_array = s1.split("\\s+");
        String[] s2_array = s2.split("\\s+");
        String[] s1_stemmed = StringParser.stemOpenNLPSentence(s1_array);
        String[] s2_stemmed = StringParser.stemOpenNLPSentence(s2_array);
        int count = 0;
        for (int i=0; i<s1_stemmed.length; i++) {
            for (int j=0; j<s2_stemmed.length; j++) {
                if (s1_stemmed[i].equals(s2_stemmed[j])) {
                    s2_array[j] = "";
                    count++;
                }
            }
        }
        if (((double)count)/s1_array.length < 0.5 && ((double)count)/s2_array.length < 0.5) {
            return null;
        }
        String new_s = s1;
        for (String s : s2_array) {
            if (!s.equals("")) {
                new_s += " ";
                new_s += s;
            }
        }
        return new_s;
    }

    private void uniteTopQueries(ArrayList<String> top_q) {
        Boolean updated = true;
        while(updated && (top_q.size() > 3)) {
            updated = false;
            String united = queryUnion(top_q.get(0), top_q.get(1));
            if (united != null) {
                updated = true;
                top_q.remove(1);
                top_q.remove(0);
                top_q.add(0, united);
            }
            else {
                united = queryUnion(top_q.get(0), top_q.get(2));
                if (united != null) {
                    updated = true;
                    top_q.remove(2);
                    top_q.remove(0);
                    top_q.add(0, united);
                }
                else {
                    united = queryUnion(top_q.get(1), top_q.get(2));
                    if (united != null) {
                        updated = true;
                        top_q.remove(2);
                        top_q.remove(1);
                        top_q.add(1, united);
                    }
                }
            }
        }
    }


}