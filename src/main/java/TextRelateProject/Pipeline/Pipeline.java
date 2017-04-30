package TextRelateProject.Pipeline;

import TextMergeProject.Main;
import TextRelateProject.BingApi.NewsSearch;
import TextRelateProject.BingApi.TextExtractor;
import TextRelateProject.DocumentRanking.ReferenceDocument;
import TextRelateProject.DocumentRanking.RetrievedDocument;
import TextRelateProject.QueryGenerator.QueryDocument;
import TextRelateProject.RSS.RSS;
import TextRelateProject.SQL.MySQLAccess;
import TextRelateProject.StringParsing.CoreNLP;
import TextRelateProject.StringParsing.OpenNlpModels;
import javafx.util.Pair;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static TextRelateProject.Helpers.helperFunctions.tryRetrieveDescriptionFromText;
import static TextRelateProject.Helpers.helperFunctions.tryRetrieveTitleFromText;
import static TextRelateProject.SQL.MySQLUtils.mysql_real_escape_string;

/**
 * Created by Admin on 03-Apr-17.
 */
public class Pipeline {
    static private int summaryIndex = 0;
    static private MySQLAccess dao = null;

    // input[0] is the url of the original page.
    // input[1] is the number of results per query.
    // input[2] is OpenNLP mode or Stanford mode (write OpenNLP or Stanford)
    // input[3] is the mode - fast , both, or accurate (0 ,1 or 2)
    // input[4] is the score equation method - avg or max
    // input[5] is how many results we want at the end
    // input[6] is the path folder where we save our results, most similar texts
    // input[7] is the path to the file where will save the summary
    // input[8] is the number of threads we want. if want don't wont write 0 (or less)
    // input[9] is the number of lines in each summary.
    // input[10] save the project results - input saveResult.
    // input[11] save the merged document - input saveMerge.
    // input[12] add to database - input saveDB.
    // input[13] the key to Bing for querying
    // input[14] if we want the title to be considered in the ranking equation
    public static void pipeline(String[] input) {
        String url_or_text = input[0];
        int res_per_query = Integer.parseInt(input[1]);
        String parse_mode = input[2];
        int speed_mode = Integer.parseInt(input[3]);
        String score = input[4];
        int num_results = Integer.parseInt(input[5]);
        String results_path = input[6];
        String summary_file_path = input[7];
        int num_threads = Integer.parseInt(input[8]);
        int num_lines = Integer.parseInt(input[9]);
        Boolean saveResults = false;
        if (input[10].equals("saveResult")) {
            saveResults = true;
        }
        Boolean saveMerge = false;
        if (input[11].equals("saveMerge")) {
            saveMerge = true;
        }
        Boolean saveDB = false;
        if (input[12].equals("saveDB")) {
            saveDB = true;
        }
        Boolean useTitle = false;
        if (input[14].equals("useTitle")) {
            useTitle = true;
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("start time of one URL: real time is " + sdf.format(cal.getTime()));
        long start = System.currentTimeMillis();
        new OpenNlpModels();
        new CoreNLP();
        long startReference = System.currentTimeMillis();

        Document doc = TextExtractor.getDocument(url_or_text);
        if (doc == null) {
            System.out.println("error while extracting the the url");
            return;
        }
        Pair<String, String> textWithBoiler = TextExtractor.getTextWithBoilerFromDocument(doc);
        if (textWithBoiler == null) {
            System.out.println("error while loading page from url");
            return;
        }
        String body = textWithBoiler.getValue();
        String description = TextExtractor.getDescriptionFromDocument(doc);
        if (description == null || description.equals("")) {
            description = tryRetrieveDescriptionFromText(body); //not supposed to happen
        }
        String title = TextExtractor.getTitleFromDocument(doc);
        if (title == null || title.equals("")) {
            title = textWithBoiler.getKey();
        }
        if (title == null || title.equals("")) {
            title = tryRetrieveTitleFromText(body); //title would be first actual sentence in the document
        }
        cal = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Reference initialize time: " + Long.toString((System.currentTimeMillis() - startReference) / 1000) + " seconds. real time is " + sdf.format(cal.getTime()));


        // get the queries.
        long startCalQuery = System.currentTimeMillis();
        QueryDocument queryDocument = new QueryDocument(url_or_text, body, doc);
        queryDocument.parseAndGetScores();
        ArrayList<String> queries = queryDocument.getUnitedQueries(20);
        ArrayList<String> best_queries = new ArrayList<>();
        best_queries.add(queries.get(0) + " , " + queries.get(1) + " , " + queries.get(2));
        best_queries.add(queries.get(0) + " , " + queries.get(1));
        best_queries.add(queries.get(0) + " , " + queries.get(2));
        best_queries.add(queries.get(1) + " , " + queries.get(2));
        best_queries.add(queries.get(0));
        best_queries.add(queries.get(1));
        best_queries.add(queries.get(2));
        cal = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("query build time: " + Long.toString((System.currentTimeMillis() - startCalQuery) / 1000) + " seconds. real time is " + sdf.format(cal.getTime()));




        // get the results.
        long startQuery = System.currentTimeMillis();
        ArrayList<RetrievedDocument> results = NewsSearch.getQueriesResults(best_queries, res_per_query, input[13]);
        cal = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("query extractor from Bing time: " + Long.toString((System.currentTimeMillis() - startQuery) / 1000) + " seconds. real time is " + sdf.format(cal.getTime()));

        // create the reference document
        ReferenceDocument referenceDocument = new ReferenceDocument(url_or_text, title, description, body, results, parse_mode);
        referenceDocument.setNumberOfCrawlers(num_threads);

        // run the project
        referenceDocument.finalProjectResults(speed_mode, score, useTitle, num_results, results_path, saveResults);

        if (saveMerge || saveDB) {
            long mergeTime = System.currentTimeMillis();
            String merged_text = "";
            // merge the documents
            try {
                merged_text = Main.mergeForTextRelate(referenceDocument.getFinalResults(), summary_file_path, num_lines, saveMerge);

            } catch (Exception e) {
                System.out.println("error while merging");
            }
            System.out.println("merge time: " + Long.toString((System.currentTimeMillis() - mergeTime) / 1000) + " seconds. real time is " + sdf.format(cal.getTime()));

            if (saveDB) {
                long addToDBTime = System.currentTimeMillis();
                try {
                    dao.addTextToDB(mysql_real_escape_string(merged_text), mysql_real_escape_string(title));
                } catch (Exception e) {
                    System.out.println("error while uploading the article to the database");
                }
                System.out.println("add to DB time: " + Long.toString((System.currentTimeMillis() - addToDBTime) / 1000) + " seconds. real time is " + sdf.format(cal.getTime()));
            }
        }

        cal = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("end time of one URL: " + Long.toString((System.currentTimeMillis() - start) / 1000) + " seconds. real time is " + sdf.format(cal.getTime()));
    }

    // input[0] is the number of results per query.
    // input[1] is OpenNLP mode or Stanford mode (write OpenNLP or Stanford)
    // input[2] is the mode - fast , both, or accurate (0 ,1 or 2)
    // input[3] is the score equation method - avg or max
    // input[4] is how many results we want at the end
    // input[5] is the path folder where we save our results, most similar texts
    // input[6] is the path to the file where will save the summary
    // input[7] is the number of threads we want. if want don't wont write 0 (or less)
    // input[8] is the number of lines in each summary.
    // input[9] save the project results - input saveResult.
    // input[10] save the merged document - input saveMerge.
    // input[11] add to database - input saveDB.
    // input[12] the key to Bing for querying
    // input[13] if we want the title to be considered in the ranking equation
    // input[14] is the number of results for the RSS feed.
    // input[15] is the url of the RSS link.
    public static void PipelineRSS(String[] input) {
        String[] input_to_pipeline_function = new String[input.length-1]; // no need for the last two, the first we input from somewhere else
        for (int i = 1; i < input_to_pipeline_function.length; i++) {
            input_to_pipeline_function[i] = input[i - 1];
        }
        ArrayList<String> links = RSS.getLinks(input[15]);
        int i = 0;
        int feed_res_num = Integer.parseInt(input[14]);
        for (String link : links) {
            input_to_pipeline_function[7] = input[6] + "/summary_" + Integer.toString(summaryIndex++) + ".txt";
            input_to_pipeline_function[0] = link;
            pipeline(input_to_pipeline_function);
            i++;
            if (i >= feed_res_num) {
                break;
            }
        }
    }



    //after extracting info from the manual text (there is a default if their isn't a path to manuel):
    // input[0] is the number of results per query.
    // input[1] is OpenNLP mode or Stanford mode (write OpenNLP or Stanford)
    // input[2] is the mode - fast , both, or accurate (0 ,1 or 2)
    // input[3] is the score equation method - avg or max
    // input[4] is how many results we want at the end
    // input[5] is the path folder where we save our results, most similar texts
    // input[6] is the path to the file where will save the summary
    // input[7] is the number of threads we want. if want don't wont write 0 (or less)
    // input[8] is the number of lines in each summary.
    // input[9] save the project results - input saveResult.
    // input[10] save the merged document - input saveMerge.
    // input[11] add to database - input saveDB.
    // input[12] the key to Bing for querying
    // input[13] if we want the title to be considered in the ranking equation
    // input[14] is the number of results for the RSS feed.
    // input[15] the name of the database.
    // input[16] if its an "URL"/"RSS".
    // input[17] the url.
    public static String[] ReadTheManuel(String[] args){
        String[] input = new String[18];
        if (args.length == 3) { //no path for manuel - do default
            int i = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(args[2]))) {
                String line = br.readLine();
                while (i < 16) { //supposed to stop before reading the end of file
                    if (line.length() > 0 && line.charAt(0) != '/') {
                        input[i] = line;
                        i++;
                    }
                    line = br.readLine();
                }
            } catch (Exception e) {
            }
        }
        else{
            // input[0] is the number of results per query.
            input[0] = "20";
            input[1] = "OpenNLP";
            input[2] = "1";
            input[3] = "avg";
            input[4] = "5";
            input[5] = "../ScoreResults";
            input[6] = "../merges";
            input[7] = "8";
            input[8] = "15";
            input[9] = "saveResult";
            input[10] = "saveMerge";
            input[11] = "saveDB";
            input[12] = "1f869dbd7b0b446398fc3c7c07c64ba9";
            input[13] = "no";
            input[14] = "5";
            input[15] = "yair_db";

        }
        input[16] = args[0];
        input[17] = args[1];
        return input;
    }




    // args[0] if its an "URL"/"RSS".
    // args[1] the url.
    // args[2] is the path to the file of the manuel
    public static void main(String[] args) {
        String[] input = ReadTheManuel(args);
        String[] new_input_to_next_function;
        if (input[10].equals("saveMerge")) {
            File dir = new File(input[6]);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File ignored : directoryListing) {
                    summaryIndex++;
                }
            }
        }
        if (input[11].equals("saveDB")) {
            dao = new MySQLAccess();
            dao.setConnection(input[15]);
        }
        if (input[16].equals("URL")){
            new_input_to_next_function = new String[15];
            new_input_to_next_function[0] = input[17];
            for (int i=1 ; i<new_input_to_next_function.length;i++){
                new_input_to_next_function[i] = input[i-1]; //7 is wrong
            }
            new_input_to_next_function[7] = input[6] + "/summary_" + Integer.toString(summaryIndex++) + ".txt";
            pipeline(new_input_to_next_function);
        }
        else{
            new_input_to_next_function = new String[16];
            for (int i=0 ; i<new_input_to_next_function.length-1;i++){ //last is the url
                new_input_to_next_function[i] = input[i];
            }
            new_input_to_next_function[15] = input[17];
            PipelineRSS(new_input_to_next_function);
        }
        if (input[11].equals("saveDB")) {
            dao.close();
        }
    }
}
