package TextRelateProject.Helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * Created by Yair on 08/01/2017.
 */
public class BagOfWords {
    public static HashMap<String, Long> corpus_count;
    public static long num_words_in_corpus;

    static {
        try {
//            long start1 = System.currentTimeMillis();
            FileInputStream fileIn = new FileInputStream("CommonWords_1w.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            corpus_count = (HashMap<String, Long>) in.readObject();
            in.close();
            fileIn.close();
            num_words_in_corpus = corpus_count.values().stream().mapToLong(Number::longValue).sum();
//            System.out.println("load time: " + Long.toString(System.currentTimeMillis()-start1));
        }catch(IOException | ClassNotFoundException i) {
            i.printStackTrace();
        }
    }
}
