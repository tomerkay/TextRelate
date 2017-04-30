package TextRelateProject.QueryGenerator;

import java.io.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.log10;

/**
 * Created by Ronen on 21/11/2016.
 */

/**
 * class used for scoring the stemmed words in the bag of words
 */
public class WordScore {
    private LinkedHashMap<String, Double> scores;
    public WordScore(){
        scores = new LinkedHashMap<>();
    }

    /**
     *
     * @param path : the word count file path
     */
    public void getScores(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line, word;
            double count;
            double word_score;
            while ((line = br.readLine()) != null) {
                word = line.split(",")[0];
                 try {
                     count = Long.parseLong(line.split(",")[1]);
                 } catch (NumberFormatException e) {
                     System.out.println(line);
                     continue;
                 }
                if (count == 1) {
                    count = 1.1;
                }
                word_score = 1.0 / log10(count);
                scores.put(word, word_score);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // sort scores in descending order.
        scores = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x,y)-> {throw new AssertionError();},
                        LinkedHashMap::new
                ));
    }

    public void printToCSV(String score_filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(score_filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for(String k: scores.keySet()){
            sb.append(k);
            sb.append(',');
            sb.append(scores.get(k));
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
    }

    public static void main(String[] argv){
        WordScore score = new WordScore();
        score.getScores(argv[0]);
        score.printToCSV(argv[1]);
    }
}



