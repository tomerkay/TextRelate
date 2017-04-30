package TextRelateProject.DocumentRanking;

import TextRelateProject.StringParsing.DocumentParser;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ronen on 06/12/2016.
 */

// this class is used for parsing the json file containing the clustered training data

public class TrainingDataParser {
    // each pair is a training example, in the form (similarity_score, [1 if in same cluster, 0 otherwise])
    private ArrayList<Pair<ArrayList<Double>, Integer>> training_data;
    private ArrayList<Pair<ArrayList<Double>,Pair<Integer,Pair<String, String>>>> training_data_info;
    private String json_clustered_path;
    private String folder_path;
    private String[][] clusters;
    private ArrayList<String> doc_paths;
    private String parseWay;
    private boolean trainOrTest; //true for train, false for test - when train adds multiple to training data

    public TrainingDataParser(String folder_path, String json_cluster_path, String parseWay, String trainOrTest) {
        this.folder_path = folder_path;
        this.json_clustered_path = json_cluster_path;
        doc_paths = new ArrayList<>();
        training_data = new ArrayList<>();
        training_data_info = new ArrayList<>();
        this.parseWay = parseWay.toLowerCase();
        if (trainOrTest.toLowerCase().equals("train")) {
            this.trainOrTest = true;
        }
        else{
            this.trainOrTest = false;
        }
    }

    private void getPaths() {
        File dir = new File(folder_path);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                doc_paths.add(file.getAbsolutePath());
            }
        }
    }

    private void getClusters() throws FileNotFoundException {
        Gson gson = new Gson();
        clusters = gson.fromJson(new JsonReader(new FileReader(json_clustered_path)), String[][].class);
    }

    public void clustersToTrainingData() throws FileNotFoundException {
        getPaths();
        getClusters();
        // for each document, mark it as the reference document.
        for (int ref_num = 0; ref_num < doc_paths.size(); ref_num++) {
            ReferenceDocument ref_doc = new ReferenceDocument(folder_path, doc_paths.get(ref_num), parseWay);
            final ArrayList<double[][][]> similarities = ref_doc.getSentenceSimilaritiesFromScratch();
            String[] reference_sentences = ref_doc.getSentences();
            // for each document, compare the similarities with the reference document.
            for (int doc_num = 0; doc_num < similarities.size(); doc_num++) {
                double[][][] curr_similarities = similarities.get(doc_num);
                DocumentParser curr_doc = ref_doc.getIthDocument(doc_num);
                if (curr_doc == null) {
                    throw new NullPointerException();
                }
                String[] curr_sentences = curr_doc.getSentences();
                // for every 2 sentences, add them to the training data.
                for (int i=0; i < reference_sentences.length; i++) {
                    for (int j=0; j < curr_sentences.length; j++) {
                        ArrayList<Double> listFeaturs = new ArrayList<>();
                        for (int k=0 ;k<ref_doc.numOfAttributes; k++) {
                            listFeaturs.add(curr_similarities[i][j][k]);
                        }
                        if (areInSameCluster(reference_sentences[i], curr_sentences[j])) {
                            training_data.add(new Pair<>(listFeaturs, 1));
                            training_data_info.add(new Pair<>(listFeaturs, new Pair<>(1,new Pair<>(reference_sentences[i], curr_sentences[j]))));
                            if (trainOrTest) { //if it is test data don't add multiples
                                for (int n = 0; n < 15; n++) {
                                    training_data.add(new Pair<>(listFeaturs, 1));
                                }
                            }
                        }
                        else {
                            training_data.add(new Pair<>(listFeaturs, 0));
                            training_data_info.add(new Pair<>(listFeaturs, new Pair<>(0,new Pair<>(reference_sentences[i], curr_sentences[j]))));

                        }
                    }
                }
            }
        }
    }

    private boolean areInSameCluster(String sent1, String sent2) {
        for (String[] cluster : clusters) {
            if (Arrays.asList(cluster).contains(sent1) && Arrays.asList(cluster).contains(sent2)) {
                return true;
            }
        }
        return false;
    }

    public void trainingToCSV(String path_to_csv, String path_to_csv2) {
        PrintWriter pw = null;
        PrintWriter pw2 = null;
        try {
            pw = new PrintWriter(new File(path_to_csv));
            pw2 = new PrintWriter(new File(path_to_csv2));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for(Pair<ArrayList<Double>, Integer> example : training_data){
            for (Double value : example.getKey()) {
                sb.append(value);
                sb.append(',');
            }
            sb.append(example.getValue());
            sb.append('\n');
        }
        for(Pair<ArrayList<Double>,Pair<Integer,Pair<String, String>>> example : training_data_info){
            sb2.append("next sentence:\n");
            int i = 0;
            for (Double value : example.getKey()) {
                sb2.append(value);
                sb2.append(", ");
                i++;
            }
            sb2.append(example.getValue().getKey());
            sb2.append('\n');
            sb2.append(example.getValue().getValue().getKey());
            sb2.append('\n');
            sb2.append(example.getValue().getValue().getValue());
            sb2.append('\n');
            sb2.append('\n');

        }
        pw.write(sb.toString());
        pw.close();
        pw2.write(sb2.toString());
        pw2.close();
    }

    public static void main(String[] argv) throws FileNotFoundException {
        // argv[0] is the documents' folder path, argv[1] is the json file path (clustered data)
        // argv[2] is the parse technique, argv[3] is to train or to test
        // argv[4] and argv[5] are the csv file paths, for the output.
        TrainingDataParser my_parser = new TrainingDataParser(argv[0], argv[1], argv[2], argv[3]);
        my_parser.clustersToTrainingData();
        my_parser.trainingToCSV(argv[4], argv[5]);
    }
}
