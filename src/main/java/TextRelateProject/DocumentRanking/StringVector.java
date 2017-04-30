package TextRelateProject.DocumentRanking;

/**
 * Created by Ronen on 27/11/2016.
 */
public interface StringVector {
    Object getVector();

    double getSimilarity(StringVector other_vector);

    double getLength();

    double getKeyValue(String word);
}
