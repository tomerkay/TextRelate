package TextRelateProject.StringParsing;

import TextRelateProject.Helpers.helperFunctions;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import opennlp.tools.util.Span;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Admin on 13-Dec-16.
 */
public class StringParser {

    public static String parseDocument(String path) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("can't read file: " + path);
            return null;
        }
        return new String(encoded, Charset.defaultCharset());
    }

    // sentence splitter
    public static String[] sentSplitOpenNLP(String original_text) {
        return OpenNlpModels.sentence_splitter.sentDetect(original_text);
        //TODO: new lines
    }

    public static String[] sentSplitStanford(Annotation document) {
        List<CoreMap> listSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String[] sentences = new String[listSentences.size()];
        int i = 0;
        for(CoreMap sentence: listSentences) {
            sentences[i] = sentence.toString();
            i++;
        }
        return sentences;
        //TODO: new lines
    }

    public static String[][] sentIntoTokensOpenNLP(String[] sentences) {
        String[][] tokens = new String[sentences.length][];
        for (int i=0;i<sentences.length;i++){
            tokens[i]=OpenNlpModels.tokenizer.tokenize(sentences[i]);
        }
        return tokens;
    }

    public static String[][] sentIntoTokensStanford(Annotation document) {
        List<CoreMap> listSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String[][] tokens = new String[listSentences.size()][];
        int i = 0;
        for(CoreMap sentence: listSentences) {
            String[] words = new String[sentence.get(CoreAnnotations.TokensAnnotation.class).size()];
            int j = 0;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                words[j] = word;
                j++;
            }
            tokens[i] = words;
            i++;
        }
        return tokens;
    }

    public static String[][] POStaggerOpenNLP(String[][] tokens) {
        String[][] tokenPOS = new String[tokens.length][];
        for (int i=0; i<tokens.length; i++) {
            tokenPOS[i] = OpenNlpModels.pos_tagger.tag(tokens[i]);
        }
        return tokenPOS;
    }

    public static String[][] POStaggerOpenStanford(Annotation document) {
        List<CoreMap> listSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String[][] tokensPos = new String[listSentences.size()][];
        int i = 0;
        for(CoreMap sentence: listSentences) {
            String[] words = new String[sentence.get(CoreAnnotations.TokensAnnotation.class).size()];
            int j = 0;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                words[j] = word;
                j++;
            }
            tokensPos[i] = words;
            i++;
        }
        return tokensPos;
    }


    public static String[][] chunkOpenNLPMatrix(String[][] tokens, String[][] tokenPOS) {
        String[][] chunkedTokens = new String[tokens.length][];
        for (int i=0; i< tokens.length; i++) {
            chunkedTokens[i] = OpenNlpModels.chunker.chunk(tokens[i], tokenPOS[i]);
        }
        return chunkedTokens;
    }

    public static String[] findNPtokensSentence(String[] tokens, String[] chunkedTokens) {
        ArrayList<String> np_list = new ArrayList<>();
        for (int j = 0; j < chunkedTokens.length; j++) {
            if (chunkedTokens[j].contains("NP")) {
                np_list.add(tokens[j]);
            }
        }
        return np_list.toArray(new String[0]);
    }

    public static String[][] findNPtokensMatrix(String[][] tokens, String[][] chunkedTokens) {
        String[][] NPTokens = new String[tokens.length][];
        for (int i=0; i< tokens.length; i++) {
            ArrayList<String> np_list = new ArrayList<>();
            for (int j=0; j < chunkedTokens[i].length; j++) {
                if (chunkedTokens[i][j].contains("NP")) {
                    np_list.add(tokens[i][j]);
                }
            }
            NPTokens[i] = np_list.toArray(new String[0]);
        }
        return NPTokens;
    }


    public static String[][] stemOpenNLPMatrix(String[][] in_tokens) {
        String[][] out_stemmedTokens = new String[in_tokens.length][];
        for (int i=0; i<in_tokens.length; i++) {
            out_stemmedTokens[i] = new String[in_tokens[i].length];
            for (int j=0; j<in_tokens[i].length; j++) {
                out_stemmedTokens[i][j] = OpenNlpModels.stemmer.stem(in_tokens[i][j]).toLowerCase();
            }
        }
        return out_stemmedTokens;
    }

    public static String[] stemOpenNLPSentence(String[] in_tokens) {
        String[] out_stemmedTokens = new String[in_tokens.length];
        for (int i=0; i<in_tokens.length; i++) {
            out_stemmedTokens[i] = OpenNlpModels.stemmer.stem(in_tokens[i]).toLowerCase();
        }
        return out_stemmedTokens;
    }

    public static String[][] lemmaStanford(Annotation document) {
        List<CoreMap> listSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        String[][] lemmas = new String[listSentences.size()][];
        int i = 0;
        for(CoreMap sentence: listSentences) {
            String[] words = new String[sentence.get(CoreAnnotations.TokensAnnotation.class).size()];
            int j = 0;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.LemmaAnnotation.class);
                words[j] = word;
                j++;
            }
            lemmas[i] = words;
            i++;
        }
        return lemmas;
    }

    // counts frequency of in_tokens, taking into account only words, not numbers etc.
    // also, assumes that the tokens are not capitalized (in particularly, stemmed).
    public static HashMap<String, Integer> countFrequency(String[][] in_tokens){
        HashMap<String, Integer> out_freq = new HashMap<>();
        for (int i=0; i<in_tokens.length; i++) {
            for (int j=0; j<in_tokens[i].length; j++) {
                if ((in_tokens[i][j].charAt(0) >= 'a' && in_tokens[i][j].charAt(0) <= 'z')) {
                    out_freq.putIfAbsent(in_tokens[i][j], 0);
                    out_freq.replace(in_tokens[i][j], out_freq.get(in_tokens[i][j]) + 1);
                }
            }
        }
        return out_freq;
    }


//code comment missing

    /**
     *
     * @param tokens : sentence splitted into tokens
     * @return String[]; {all_names_together[]}
     */
    public static String[] findNamesOpenNLP(String[] tokens) {
        String[] result;
        Span[] nameSpans = OpenNlpModels.person_finder.find(tokens);
        String[] names = Span.spansToStrings(nameSpans, tokens);
        names = helperFunctions.SplitStringsIntoSingles(names);
        Span[] locationsSpans = OpenNlpModels.location_finder.find(tokens);
        String[] locations = Span.spansToStrings(locationsSpans, tokens);
        locations = helperFunctions.SplitStringsIntoSingles(locations);
        Span[] organizationSpans = OpenNlpModels.organization_finder.find(tokens);
        String[] organizations = Span.spansToStrings(organizationSpans, tokens);
        organizations = helperFunctions.SplitStringsIntoSingles(organizations);
        result = (String[]) ArrayUtils.addAll(names, ArrayUtils.addAll(locations, organizations));
        OpenNlpModels.person_finder.clearAdaptiveData();
        OpenNlpModels.location_finder.clearAdaptiveData();
        OpenNlpModels.organization_finder.clearAdaptiveData();
        return result;
    }


    public static String[] findNamesStanford(CoreMap sentence) {
        if (sentence == null){
            return null;
        }
        ArrayList<String> my_list = new ArrayList<>();
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            String word = token.get(CoreAnnotations.TextAnnotation.class);
            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            if (ne.equals("PERSON")||ne.equals("ORGANIZATION")||ne.equals("LOCATION")) {
                my_list.add(word);
            }
        }
        String[] result = new String[my_list.size()];
        return my_list.toArray(result);
    }

    public static ArrayList<String> stanfordNER(Annotation text) {
        ArrayList<String> my_list = new ArrayList<>();
        List<CoreMap> sentences = text.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ne.equals("PERSON")||ne.equals("ORGANIZATION")||ne.equals("LOCATION")) {
                    my_list.add(word);
                }
            }
        }
        return my_list;
    }


}
