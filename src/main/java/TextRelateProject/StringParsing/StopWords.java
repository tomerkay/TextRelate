package TextRelateProject.StringParsing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by Ronen on 11/12/2016.
 */
public class StopWords {
    public static final HashSet<String> stopwords;

    static {
        stopwords = new HashSet<>();
        getStopWords("stopwords_nltk.txt");
    }

    private static void getStopWords(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = OpenNlpModels.stemmer.stem(line);
                stopwords.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
