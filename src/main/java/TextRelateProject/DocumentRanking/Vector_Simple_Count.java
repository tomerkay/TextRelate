package TextRelateProject.DocumentRanking;

import TextRelateProject.Helpers.helperFunctions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Admin on 08-Jan-17.
 */

public class Vector_Simple_Count implements StringVector {
    private HashMap<String, Integer> vector;
    private double length;

    public Vector_Simple_Count(HashMap<String, Integer> vector_map) {
        vector = new HashMap<>(vector_map);
        vectorLength();
    }

    private void vectorLength() {
        double sum = 0;
        for (Integer value : vector.values()) {
            sum += Math.pow(value, 2);
        }
        length = Math.sqrt(sum);
    }

    @Override
    public HashMap<String, Integer> getVector() {
        return vector;
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
        Vector_Simple_Count other = (Vector_Simple_Count) other_vector;
        HashMap<String, Integer> other_Vector_Simple_Count = other.getVector();
        Set<String> intersection = new HashSet<>(this.vector.keySet()); // use the copy constructor
        intersection.retainAll(other_Vector_Simple_Count.keySet());
        double sum = 0;
        for (String key : intersection) {
            sum += other_Vector_Simple_Count.get(key) * this.vector.get(key);
        }
        if (sum == 0) {
            return 0;
        }
        return helperFunctions.SameNumber(sum/(this.length * other.length),1);
    }

    @Override
    public double getKeyValue(String key){
        if (vector.containsKey(key)){
            return vector.get(key);
        }
        return 0;
    }
}