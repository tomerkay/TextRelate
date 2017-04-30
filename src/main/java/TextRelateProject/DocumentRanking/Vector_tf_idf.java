package TextRelateProject.DocumentRanking;

import TextRelateProject.Helpers.helperFunctions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
/**
 * Created by Ronen on 27/11/2016.
 */
public class Vector_tf_idf implements StringVector {
    private HashMap<String, Double> vector;
    private double length;

    public Vector_tf_idf(HashMap<String, Double> tf_idf) {
        vector = new HashMap<>(tf_idf);
        vectorLength();
    }

    private void vectorLength() {
        double sum = 0;
        for (Double value : vector.values()) {
            sum += Math.pow(value, 2);
        }
        length = Math.sqrt(sum);
    }

    @Override
    public HashMap<String, Double> getVector() {
        return vector;
    }

    @Override
    public double getKeyValue(String key){
        if (vector.containsKey(key)){
            return vector.get(key);
        }
        return 0;
    }

    @Override
    public double getLength(){ return length;}

    /**
     * calculates cosine similarity
     * @param other_vector : the vector to find similarity with
     * @return
     */
    @Override
    public double getSimilarity(StringVector other_vector) {
        Vector_tf_idf other = (Vector_tf_idf) other_vector;
        HashMap<String, Double> other_tf_idf = other.getVector();
        Set<String> intersection = new HashSet<>(this.vector.keySet()); // use the copy constructor
        intersection.retainAll(other_tf_idf.keySet());
        double sum = 0;
        for (String key : intersection) {
            sum += other_tf_idf.get(key) * this.vector.get(key);
        }
        if (sum == 0) {
            return 0;
        }
        return helperFunctions.SameNumber(sum/(this.length * other.length),1);
    }
}
