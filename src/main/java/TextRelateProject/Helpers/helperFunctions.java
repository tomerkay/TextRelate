package TextRelateProject.Helpers;

import TextRelateProject.StringParsing.OpenNlpModels;
import TextRelateProject.StringParsing.StopWords;
import TextRelateProject.StringParsing.StringParser;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by Admin on 12-Dec-16.
 */
public class helperFunctions {
    public static double SameNumber(double num1, double num2){
        if (abs(num1-num2) < 0.0001){
            return num2;
        }
        return num1;
    }

    public static String[] SplitStringsIntoSingles(String[] strings){
        HashSet<String> set = new HashSet<>();
        for (String string : strings){
            String[] splited = string.split(" ");
            for (String word : splited) {
                set.add(word);
            }
        }
        return set.toArray(new String[0]);
    }

    public static HashMap<String,Integer> MakeSentenceIntoSimpleMapCountStemmedWords(String sentence){
        String[] tokens = OpenNlpModels.tokenizer.tokenize(sentence);
        String[] StemmedTokens = StringParser.stemOpenNLPSentence(tokens);
        HashMap<String,Integer> result = new HashMap<>();
        for (String word : StemmedTokens) {
            if (!(StopWords.stopwords.contains(word))) {
                result.putIfAbsent(word, 0);
                result.replace(word, result.get(word) + 1);
            }
        }
        return result;
    }

    public static void resultOfProjectIntoCSV(List<Pair<Double,String>> result, String pathToFolder){

        File dir = new File(pathToFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        PrintWriter pw = null;
        try {
            int i=0;
            while (new File(pathToFolder + "/score_" + Integer.toString(i) + ".txt").exists()) {
                i++;
            }
            pw = new PrintWriter(new File(pathToFolder + "/score_" + Integer.toString(i) + ".txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (Pair<Double,String> pair : result) {
            Double value = pair.getKey();
            String word = pair.getValue();
            word = word.substring(0, word.indexOf(','));
            sb.append(String.valueOf(value));
            sb.append('\n');
            sb.append(word);
            sb.append('\n');
            sb.append('\n');
        }

        pw.write(sb.toString());
        pw.close();
    }


    public static String eightToTwelveWordsString(String sentence){
    	int end = sentence.indexOf('\n',0);  //suppossed to be -1
    	String sentence_with_out_enter = end == -1 ? sentence : sentence.substring(0,end);
        String[] finalWords  = sentence_with_out_enter.split(" ", 12);
        finalWords[finalWords.length-1] = finalWords[finalWords.length-1].split(" ")[0];
        String result = "";
        for (int i=0; i< finalWords.length-1;i++){
            result = result.concat(finalWords[i]).concat(" ");
        }
        result = result.concat(finalWords[finalWords.length-1]);
        return result;
    }

    public static boolean isItAGoodTitle(String sent){
        if (sent.split(" ").length < 8){
            return false;
        }
        if (sent.contains("|")||sent.contains("@")||sent.contains("#")||sent.contains("$")||sent.contains("%")||sent.contains("^")||sent.contains("&")){
            return false;
        }
        return true;
    }

    public static String tryRetrieveTitleFromText(String body){
        String result = "";
        int start = 0;
        int end = body.indexOf('\n',start);
        while (end != -1 && !isItAGoodTitle(result)){
            result = body.substring(start, end);
            start = end + 1;
            end = body.indexOf('\n',start);
        }
        if (end == -1){
        	return eightToTwelveWordsString(body);
        }
        return eightToTwelveWordsString(result);
    }
    public static String tryRetrieveDescriptionFromText(String body) {
        boolean found = false;
        String result = "";
        int start_of_result = 0;
        while (!found){
            int end_of_result = body.indexOf("\n", start_of_result);
            result = end_of_result == -1 ? body : body.substring(start_of_result,end_of_result);
            if (result.split(" ").length>=15){
                found = true;
            }
            if (result.equals(body)) { //whole body is larger than 100, so we'll we are to stop anyhow, but to be sure
                found = true;
            }
            start_of_result = end_of_result+1;
        }
        return result;
    }


}
