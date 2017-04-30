package TextMergeProject;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Classifier {
    private NaiveBayes wekaClassifier;
    private boolean isLoaded = false;

    private final String MODEL_FILE = "./project_files_aux/TextMerge_files/classifier_model.model";

    public void train() throws FileNotFoundException, Exception {
        // Parse the training documents
        InputParser trainingParser = new InputParser();
        ArrayList<Document> trainingDocs = trainingParser.parseDocuments(Globals.TRAINING_DOCS_PATHS);

        // Calculate statistics for training docs
        DataAnalyzer stats = new DataAnalyzer(trainingDocs);
        stats.calculateData();
        HashMap<String, HashMap<String, Double>> similarityMat = stats.buildSimilarityMatrix();

        Instances dataset = initDataSet();
        parseData(dataset, similarityMat);

        dataset.setClassIndex(dataset.numAttributes() - 1);
        wekaClassifier = new NaiveBayes();
        wekaClassifier.buildClassifier(dataset);

        OutputStream os = new FileOutputStream(MODEL_FILE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(wekaClassifier);
        objectOutputStream.close();
    }

    public double classifyVal(double similarity) throws Exception {
        if (!isLoaded) {
            FileInputStream is = new FileInputStream(MODEL_FILE);
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            wekaClassifier = (NaiveBayes) objectInputStream.readObject();
            objectInputStream.close();

            isLoaded = true;
        }

        Instances dataSet = initDataSet();
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        double[] dataRecord = new double[1];
        dataRecord[0] = similarity;
        Instance ins = new DenseInstance(1.0, dataRecord);
        ins.setDataset(dataSet);

        return wekaClassifier.distributionForInstance(ins)[0];
    }

    private void parseData(Instances data, HashMap<String, HashMap<String, Double>> mat) throws FileNotFoundException {
        Gson gson = new Gson();
        String[][] clusters = gson.fromJson(new JsonReader(new FileReader(Globals.CLUSTERS_FILEPATH)), String[][].class);

        // Iterate over every possible pair of sentences
        for (String[] firstcluster : clusters) {
            for (String firstsentence : firstcluster) {
                int falseCounter = 0;
                int trueCounter = 0;
                for (String[] seccluster : clusters) {
                    for (String secsentence : seccluster) {
                        if (firstsentence != secsentence) {
                            // Create a new record according to the pair
                            double[] record = new double[data.numAttributes()];

                            try {
                                record[0] = mat.get(firstsentence).get(secsentence);
                            } catch (NullPointerException e) {
                                System.out.println("Cluster sentence not found");
                                System.out.println("First is: " + firstsentence);
                                System.out.println("Second is: " + secsentence);
                                System.out.println();
                            }


                            // Check if in same cluster
                            if (firstcluster == seccluster) {
                                record[1] = data.attribute(1).indexOfValue("true");
                                trueCounter++;
                            } else {
                                record[1] = data.attribute(1).indexOfValue("false");
                                falseCounter++;
                            }

                            data.add(new DenseInstance(1.0, record));
                        }
                    }
                }

                int diffCount = falseCounter - trueCounter;
                while (diffCount > 0) {
                    for (String curr : firstcluster) {
                        double[] record = new double[data.numAttributes()];
                        record[0] = mat.get(firstsentence).get(curr);
                        record[1] = data.attribute(1).indexOfValue("true");
                        data.add(new DenseInstance(1.0, record));
                        diffCount--;
                    }
                }
            }
        }
    }

    private Instances initDataSet() {
        // Build the dataset
        FastVector classifyVal = new FastVector(2);
        classifyVal.addElement("true");
        classifyVal.addElement("false");
        Attribute classifyAttribute = new Attribute("classification", classifyVal);

        FastVector dataSet = new FastVector(2);
        dataSet.addElement(new Attribute("similarity"));
        dataSet.addElement(classifyAttribute);

        return new Instances("TrainingRel", dataSet, 0);
    }
}
