package TextRelateProject.DocumentRanking;

import TextRelateProject.Helpers.helperFunctions;
import TextRelateProject.MultiThread.CrawlThread;
import TextRelateProject.StringParsing.StopWords;
import javafx.util.Pair;
import org.jsoup.nodes.Document;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static TextRelateProject.BingApi.TextExtractor.getDocument;
import static TextRelateProject.BingApi.TextExtractor.getTextWithBoilerFromDocument;
import static TextRelateProject.Helpers.helperFunctions.SameNumber;
import static TextRelateProject.Helpers.helperFunctions.resultOfProjectIntoCSV;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

/**
 * Created by Admin on 27-Nov-16.
 */

public final class ReferenceDocument extends RetrievedDocument{
    private String pathToFolder; //for the training data of the Classifier
    private String pathToOriginalText; //for the training data of the Classifier
    private ArrayList<RetrievedDocument> documentList;
    private HashMap<String,Double> idfMap;
    // each element in list corresponds to a document in documentList, the element i,j in each matrix is the similarity
    // between the i'th sentence in the reference and the j'th sentence in the retrieved document, and k is the type of similarity.
    private ArrayList<double[][][]> sentences_similarities;
    //private ArrayList<ArrayList<Double>> docs_similarities; //for every doc, has list for each vector of feature //no need any more
    private ArrayList<ArrayList<Double>> title_similarities; //for every doc, has list for each vector of feature
    private double[] coverage;
    private double[] scores; //score of each sentence of the refrenced document
    private double totalScoreSum;
    private double title_score;
    private BuildClassifier classifier;
    public int numOfAttributes;
    private LinkedHashMap<String,RetrievedDocument> RetrievedDocumentsBeforeFilter; //key is title
    private String parseWay; //"Stanford" or "OpenNLP", Case doesn't matter we lower them
    private int number_of_crawlers;
    private ArrayList<Pair<String, String>> finalResults;


    public ReferenceDocument(String url, String title, String description, String body, ArrayList<RetrievedDocument> allRetreivedDocumentsBeforeFilter, String parseWay) {
        super(url, title, description);
        finalResults = new ArrayList<>();
        original_text = body;
        this.number_of_crawlers = 1;
        this.parseWay = parseWay.toLowerCase();
        classifier = new BuildClassifier();
        if (parseWay.equals("stanford")) {
            classifier.unSerializeBuildClassifier("./project_files_aux/TextRelate_files/BuildClassifierNaiveBaseStanford6");
        }
        else{
            classifier.unSerializeBuildClassifier("./project_files_aux/TextRelate_files/BuildClassifierNaiveBaseOpenNLP6");
        }
        //classifier.unSerializeBuildClassifier("BuildClassifierOld");
        InitializeMembersOfRetrievedDocument(); //this is on the reference document
        allBuildersInitialize();
        documentList = allRetreivedDocumentsBeforeFilter;

    }


    //for the training data of the Classifier
    public ReferenceDocument(String pathToFolder, String pathToOriginalText, String parseWay) {
        this.pathToFolder = pathToFolder;
        finalResults = new ArrayList<>();
        this.number_of_crawlers = 1;
        this.pathToOriginalText = pathToOriginalText;
        this.parseWay = parseWay.toLowerCase();
        classifier = new BuildClassifier();
        allBuildersInitialize();
    }

    public void setNumberOfCrawlers(int n) {
        this.number_of_crawlers = n;
    }

    @Override
    public void addBody(String body){
        original_text = body;
    }

    // code comment missing here part 1

    public static Comparator<RetrievedDocument> ComparatorRetrievedDocumentByTitleSimilarity = new Comparator<RetrievedDocument>() {
        @Override
        public int compare(RetrievedDocument rd1, RetrievedDocument rd2) {
            return (-rd1.get_titleSimilarityToReferenceTitle().compareTo(rd2.get_titleSimilarityToReferenceTitle()));
        }
    };

    public static Comparator<RetrievedDocument> ComparatorRetrievedDocumentByDescriptionSimilarity = new Comparator<RetrievedDocument>() {
        @Override
        public int compare(RetrievedDocument rd1, RetrievedDocument rd2) {
            return (-rd1.get_descriptionSimilarityToReferenceDescription().compareTo(rd2.get_descriptionSimilarityToReferenceDescription()));
        }
    };

    public static Comparator<RetrievedDocument> ComparatorRetrievedDocumentByCoverageSimilarity = new Comparator<RetrievedDocument>() {
        @Override
        public int compare(RetrievedDocument rd1, RetrievedDocument rd2) {
            return (-rd1.getCoverageWithReference().compareTo(rd2.getCoverageWithReference()));
        }
    };

    private void allBuildersInitialize(){
        //docs_similarities = new ArrayList<>(); // no need anymore
        sentences_similarities = new ArrayList<>();
        numOfAttributes = classifier.numOfAttributes;
        if (title != null) {
            title_similarities = new ArrayList<>();
        }
        RetrievedDocumentsBeforeFilter = new LinkedHashMap<>();
        documentList = new ArrayList<>();
    }

    public void addRetrievedDocumentToMap(RetrievedDocument rd){
        String title = rd.getTitle();
        if (RetrievedDocumentsBeforeFilter.containsKey(title)){
            RetrievedDocumentsBeforeFilter.get(title).addOneToCountOfRetrieval();
        }
        else{
            rd.InitializeMembersOfRetrievedDocument();
            rd.CalculateSimilaritiesToReference(this);
            RetrievedDocumentsBeforeFilter.put(title,rd);
        }
    }
    public void keepOnlyDocumentsThatWereRetrievedSeveralTimes(int divider) {
        Integer biggestCount = 0;
        for (Map.Entry<String, RetrievedDocument> entry : RetrievedDocumentsBeforeFilter.entrySet()) {
            Integer rd_count = entry.getValue().get_countOfRetrieval();
            if (biggestCount < rd_count) {
                biggestCount = rd_count;
            }
        }
        final Integer finalOfBiggestCountAfterDivision = biggestCount / divider;

        RetrievedDocumentsBeforeFilter = RetrievedDocumentsBeforeFilter.entrySet().stream()
                .filter(map -> finalOfBiggestCountAfterDivision.compareTo(map.getValue().get_countOfRetrieval()) <= 0)
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new));
    }

    public void sortRetrievedDocumentsBeforeFilter(Comparator<RetrievedDocument> comperator){
        RetrievedDocumentsBeforeFilter = RetrievedDocumentsBeforeFilter.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(comperator))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x,y)-> {throw new AssertionError();},
                        LinkedHashMap::new
                ));
    }

    //the bound is how many retrived documents we want after title-similarity sort
    public void keepOnly_N_ValuesOfSortedRetrievedDocumentsBeforeFilter(int howMuchLeft){
        List<String> keyList = new ArrayList<>(RetrievedDocumentsBeforeFilter.keySet());
        if (howMuchLeft < keyList.size()) {
            keyList = keyList.subList(0, howMuchLeft);
            LinkedHashMap<String, RetrievedDocument> filterdMap = new LinkedHashMap<>();
            for (String title : keyList) {
                filterdMap.put(title, RetrievedDocumentsBeforeFilter.get(title));
            }
            RetrievedDocumentsBeforeFilter = filterdMap;
        }
    }


    public void FilterRetrievedDocumentsFromMapIntoTheList(){
        //leave only pages that were retrived at least the max times divided by @param
        keepOnlyDocumentsThatWereRetrievedSeveralTimes(3);


        if (description != null && (!description.equals(""))) {
            //sort by description similarity
            sortRetrievedDocumentsBeforeFilter(ComparatorRetrievedDocumentByDescriptionSimilarity);
            keepOnly_N_ValuesOfSortedRetrievedDocumentsBeforeFilter(50); //the number is how many documents we keep
        }

        if (title != null && (!title.equals(""))) {
            //sort by title similarity
            sortRetrievedDocumentsBeforeFilter(ComparatorRetrievedDocumentByTitleSimilarity);
            keepOnly_N_ValuesOfSortedRetrievedDocumentsBeforeFilter(30); //the number is how many documents we keep
        }

        documentList.clear();
        documentList.addAll(RetrievedDocumentsBeforeFilter.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));

    }

    public void initializeAfterRetrievedDocumentListIsSet(){
        long startParse = System.currentTimeMillis();
        BasicParseOriginalReference();
        parseRetrievedDocuments();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Parse time: " + Long.toString((System.currentTimeMillis()-startParse)/1000) + " seconds. real time is "+sdf.format(cal.getTime()));

        calculateIdf();
        calculateLLR();

        VectorIntializeSentences();
        calculateSimilaritiesForSentances();

        calScoresOfEachSentenceInReferencedDocument();
    }

    public RetrievedDocument getIthDocument(int i) {
        if ( (i < 0) || (i >= documentList.size()) ) {
            return null;
        }
        return documentList.get(i);
    }

    public void BasicParseOriginalReference() {
        if (original_text == null) {
            //for training data of the classifier
            assert (pathToOriginalText != null);
            parseDocumentTextWithPath(pathToOriginalText);
        }
        if (sentences == null) {
            if (parseWay.equals("stanford")) {
                basicParseTextStanford();
            }
            else{
                basicParseTextOpenNLP();
            }
        }

    }

    public void parseRetrievedDocuments(){
        for (RetrievedDocument retrievedDocument : documentList){
            if (retrievedDocument.getSentences() == null) {
                if (parseWay.equals("stanford")) {
                    retrievedDocument.basicParseTextStanford();
                }
                else{
                    retrievedDocument.basicParseTextOpenNLP();
                }
            }
        }
    }

    public void CreateRetrievedDocumentFromFolder() {
        assert (documentList == null);
        documentList = new ArrayList<>();
        File dir = new File(pathToFolder);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                RetrievedDocument retrievedDocument = new RetrievedDocument();
                retrievedDocument.parseDocumentTextWithPath(file.getAbsolutePath());
                if (parseWay.equals("stanford")){
                    retrievedDocument.basicParseTextStanford();
                }
                else{
                    retrievedDocument.basicParseTextOpenNLP();
                }
                documentList.add(retrievedDocument);
            }
        }
    }

    // TODO: add cases of SentenceType
    public void calculateIdf() {
        idfMap = new HashMap<>();
        int numOfDocumentsInTotal = documentList.size() + 1;
        //counting for each word how many documents has it
        for (HashMap.Entry<String, Integer> entry : this.getFreqWordsStemmed().entrySet()) {
            String word = entry.getKey();
            if (!(StopWords.stopwords.contains(word))) {
                idfMap.putIfAbsent(word, 0.0);
                idfMap.replace(word, idfMap.get(word) + 1);
            }
        }
        for (RetrievedDocument retrievedDocument : documentList) {
            if (original_text.compareTo(retrievedDocument.getText()) != 0) {
                for (HashMap.Entry<String, Integer> entry : retrievedDocument.getFreqWordsStemmed().entrySet()) {
                    String word = entry.getKey();
                    if (!(StopWords.stopwords.contains(word))) {
                        idfMap.putIfAbsent(word, 0.0);
                        idfMap.replace(word, idfMap.get(word) + 1);
                    }
                }
            } else {
                numOfDocumentsInTotal--; //means we found our site!
            }
        }
        //do the logarithm math to calculate idf
        for (HashMap.Entry<String, Double> entry : idfMap.entrySet()) {
            String word = entry.getKey();
            idfMap.replace(word, log((numOfDocumentsInTotal) / idfMap.get(word)));
        }
    }

    //code comment missing part 2

    public void vectorInitializeTitle(){
        assert (title_tokens != null);
        tfForTitle(this);

        //create Vector for List in the list
        for (RetrievedDocument retrievedDocument : documentList) {
            if (retrievedDocument.getTitleTokens() != null) {
                tfForTitle(retrievedDocument);
            }

        }
    }

    public void VectorIntializeSentences(){
        //create Vector for referenced sentences
        tfForSentence(this);
        //create Vector for each sentence in each Documents in the list
        for (RetrievedDocument retrievedDocument : documentList) {
            tfForSentence(retrievedDocument);
        }
    }


    private double log_frequency(int tf) { //log 2
        if (helperFunctions.SameNumber(tf,0) > 0) {
            return 1 + log(tf);
        }
        return 0;
    }

    public Vector_tf_idf createTF_IDF_ForSpecificSentence(String[] sentence, int j){
        HashMap<String, Double> tf_idf = new HashMap<>(); // first as a counter, and then computes the true value of tf-idf
        for (String word : sentence) {
            if (idfMap.containsKey(word)) {
                tf_idf.putIfAbsent(word, 0.0);
                tf_idf.replace(word, tf_idf.get(word) + 1);
            }
        }
        for (String word : sentence) {
            if (idfMap.containsKey(word)) {
                double mult =  1; //when not calculating with idf
                if (j <= 2) { //not generic
                    mult = idfMap.get(word);
                }
                tf_idf.replace(word, (log_frequency(tf_idf.get(word).intValue())) * mult);
            }
        }
        return (new Vector_tf_idf(tf_idf));
    }

    public void tfForTitle(RetrievedDocument current) {
        assert (title_tokens != null);
        current.title_vector_features = new Vector_tf_idf[numOfAttributes]; // several different featurs!
        for (int j = 0; j < numOfAttributes; j++) {
            String[] sentence = null;
            switch (j) { //not generic
                case 0:
                case 3:
                    sentence = current.getTokensStemmedOfTitle();
                    break;
                case 1:
                case 4:
                    sentence = current.getNPTokensStemmedOfTitle();
                    break;
                case 2:
                case 5:
                    sentence = current.getNamesTokensStemmedOfTitle();
                    break;
                default:
                    break;
            }
            current.title_vector_features[j] = createTF_IDF_ForSpecificSentence(sentence, j);
        }
    }

    public void tfForSentence(RetrievedDocument current){
        current.sentence_vectors = new Vector_tf_idf[current.getTokensStemmed().length][numOfAttributes]; // several different featurs!
        for (int j=0; j<numOfAttributes; j++) {
            String[][] currentTokensStemmed = null;
            switch (j) { //not generic
                case 0:
                case 3:
                    currentTokensStemmed = current.getTokensStemmed();
                    break;
                case 1:
                case 4:
                    currentTokensStemmed = current.getNPTokensStemmed();
                    break;
                case 2:
                case 5:
                    currentTokensStemmed = current.getNamesTokensStemmed();
                    break;
                default:
                    break;
            }
            int i = 0;
            for (String[] sentence : currentTokensStemmed) {
                current.sentence_vectors[i][j] = createTF_IDF_ForSpecificSentence(sentence, j);
                i++;
            }
        }
    }

//code comment missing part 3

    public void calculateSimilaritiesForSentances() {
        for (int retrieved_index=0; retrieved_index < documentList.size(); retrieved_index++) {
            RetrievedDocument retrieved_doc = documentList.get(retrieved_index);
            documentSimilarityForSentencesSpecificDocument(retrieved_index, retrieved_doc);
        }
    }

    public void calculateSimilaritiesForTitles() {
        for (int retrieved_index=0; retrieved_index < documentList.size(); retrieved_index++) {
            RetrievedDocument retrieved_doc = documentList.get(retrieved_index);
            documentSimilarityForTitles(retrieved_index, retrieved_doc);
        }
        double[] avgTitleFeature = new double[numOfAttributes];
        for (int k=0; k<numOfAttributes; k++) {
            avgTitleFeature[k] = 0;
        }
        int count =0;
        for (int retrieved_index=0; retrieved_index < documentList.size(); retrieved_index++) {
            for (int k=0; k<numOfAttributes; k++) {
                double similar = title_similarities.get(retrieved_index).get(k);
                if (similar != -1) {
                    count ++;
                    avgTitleFeature[k] += similar;
                }
            }
        }
        count /= numOfAttributes; //weve counted for each feature
        for	(int k=0; k<numOfAttributes; k++) {
            if (count !=0) {
                avgTitleFeature[k] /= count; //now it is the average
            }
            else{
                avgTitleFeature[k] = 0;
            }
        }
        for (int retrieved_index=0; retrieved_index < documentList.size(); retrieved_index++) {
            double similar = title_similarities.get(retrieved_index).get(0); //it doesnt matter wich feature, if one of them is -1 than all of them is -1
            if (similar == -1){
                ArrayList<Double> title_sim = new ArrayList<>();
                for (int k=0; k<numOfAttributes; k++) {
                    title_sim.add(avgTitleFeature[k]);
                }
                title_similarities.set(retrieved_index, title_sim);
            }
        }
    }

    //    /* if we want sentences scores with llr
    //calculates for each sentence the average (semi, normalized with root of length) of the llr value of the stemmed tokens in it
    public void calScoresOfEachSentenceInReferencedDocument() {
        scores = new double[sentences.length];
        totalScoreSum = 0;
        for (int i = 0; i < scores.length; i++) {
            if (tokensStemmed[i].length == 0) {
                scores[i]=0;
            }
            else {
                double beforeNormalization = 0;
                for (String word : tokensStemmed[i]) {
                    if (freqWordsStemmed.containsKey(word)) { //only words are in freqwordstemmed, there could be numbers in  tokensStemmed[i]
                        beforeNormalization += llr.get(word)*sentence_vectors[i][0].getKeyValue(word); //not generic
                    }
                }
                scores[i] = beforeNormalization; /// pow(tokensStemmed[i].length,0.5); //if we want to normalize with the root of the length
                totalScoreSum += scores[i];
            }
        }
    }
//*/

//code comment missing part 4

    ///* if we want title just the score we give it
    public void calScoresOfTitleInReferencedDocument(double valueOfTitle) {
        assert (title_tokens != null);
        if (title_tokensStemmed.length == 0) {
            title_score = 0;
        } else {
            title_score = valueOfTitle;
        }
    }
    //*/

//code comment missing part 5

    public void documentSimilarityForTitles(int retrieved_index, RetrievedDocument retrieved_doc) {
        assert (title_tokens != null);
        ArrayList<Double> title_sim = new ArrayList<>();
        if (retrieved_doc.getTitleTokens() != null) {
            for (int j = 0; j < numOfAttributes; j++) {
                title_sim.add(title_vector_features[j].getSimilarity(retrieved_doc.title_vector_features[j]));
            }
        }
        else{
            for (int j = 0; j < numOfAttributes; j++) {
                title_sim.add(-1.0);
            }
        }
        title_similarities.add(retrieved_index, title_sim);
    }

//code comment missing part 6

    public void documentSimilarityForSentencesSpecificDocument(int retrieved_index, RetrievedDocument retrieved_doc) {
        double[][][] similarities = new double[sentence_vectors.length][][];

        for (int i=0; i < sentence_vectors.length; i++) {
            similarities[i] = new double[retrieved_doc.sentence_vectors.length][numOfAttributes];
            for (int j=0; j < retrieved_doc.sentence_vectors.length; j++) {
                for (int k=0; k<numOfAttributes; k++) {
                    similarities[i][j][k] = sentence_vectors[i][k].getSimilarity(retrieved_doc.sentence_vectors[j][k]);
                }
            }
        }
        sentences_similarities.add(retrieved_index, similarities);
    }



    //This is the title check. No need for complement
    public double TitleCheckSimilarity(int retrieved_index){
        assert(title_tokens != null);
        double[] features = new double[numOfAttributes];
        for (int k=0; k<numOfAttributes; k++){
            features[k]= title_similarities.get(retrieved_index).get(k);
        }
        return classifier.getProbabiltyOfSimilarity(features);
    }


    public double normalizer(double num, int num_of_multiplied, String normalizer){
        if (normalizer == null){
            return num;
        }
        if (normalizer.toLowerCase().contains("geo")){ //this is the geometric average
            return pow(num, new Double(1/num_of_multiplied));
        }
        else if (normalizer.toLowerCase().contains("log")){ //this is log 2
            return pow(num, new Double(1/log(num_of_multiplied)));
        }
        else if (normalizer.toLowerCase().contains("root")){
            return pow(num, new Double(1/pow(num_of_multiplied,0.5)));
        }
        return num;
    }

    /**
     * calculates the following:
     * Coverage(ref) = AVGֹ_{s in ref}(1-MULT_{s_retrieved in retrieved}(1-(sim(s,s_retrieved)))
     * @param retrieved_index : index of the document in documentList.
     * @return
     */
    public double coverageMult(int retrieved_index, String normalizer) {
        if (totalScoreSum == 0){
            return 0;
        }
        double[][][] similarities = sentences_similarities.get(retrieved_index);
        Double sum = 0.0;
        for (int i=0; i < sentence_vectors.length; i++) {
            if (scores[i] != 0) {
                double mult = 1;
                if (similarities[i].length != 0) {
                    for (int j = 0; j < similarities[i].length; j++) {
                        if (SameNumber(mult, 0) == 0) {
                            break;
                        }
                        double[] features = new double[numOfAttributes];
                        for (int k = 0; k < numOfAttributes; k++) {
                            features[k] = similarities[i][j][k];
                        }
                        mult *= 1 - classifier.getProbabiltyOfSimilarity(features);
                    }
                    if (SameNumber(mult, 0) != 0) {
                        mult = normalizer(mult, similarities[i].length, normalizer);
                    }
                }

                sum += scores[i] * (1 - mult);
            }
        }
        double result = SameNumber(sum/totalScoreSum,1); //we checked already that totalScoreSum is not zero
        result = SameNumber(result,0);
        return result;
    }

    public double coverageMax(int retrieved_index) {
        if (totalScoreSum == 0){
            return 0;
        }
        double[][][] similarities = sentences_similarities.get(retrieved_index);
        double sum = 0;
        for (int i=0; i < sentence_vectors.length; i++) {
            if (scores[i] != 0) {
                double max = -1;
                for (int j = 0; j < similarities[i].length; j++) {
                    double[] features = new double[numOfAttributes];
                    for (int k = 0; k < numOfAttributes; k++) {
                        features[k] = similarities[i][j][k];
                    }
                    double val = classifier.getProbabiltyOfSimilarity(features);
                    if (max < val) {
                        max = val;
                    }
                }
                sum += scores[i] * max;
            }
        }

        double result = SameNumber(sum/totalScoreSum,1); //we checked already that totalScoreSum is not zero
        result = SameNumber(result,0);
        return result;
    }

    public boolean initializeReferenceAndRetrivedTitle(){
        if (parseWay.equals("stanford")){
            parseTitleStanford();
        }
        else{
            parseTitleOpenNLP();
        }
        if (title_tokens == null){
            return false;
        }
        for (int retrieved_index=0; retrieved_index<documentList.size(); retrieved_index++){
            if (parseWay.equals("stanford")){
                documentList.get(retrieved_index).parseTitleStanford();
            }
            else{
                documentList.get(retrieved_index).parseTitleOpenNLP();
            }
        }
        vectorInitializeTitle();
        return true;
    }

    public void addTitleScoreToDocumentScoreCalculation(int valueOfTitle){
        if (!initializeReferenceAndRetrivedTitle()){
            return;
        }
        calculateSimilaritiesForTitles();
        calScoresOfTitleInReferencedDocument(valueOfTitle);

        for (int retrieved_index=0; retrieved_index<documentList.size(); retrieved_index++){
            coverage[retrieved_index] *= totalScoreSum;
            coverage[retrieved_index] += title_score*TitleCheckSimilarity(retrieved_index);
            totalScoreSum += title_score;
            if (totalScoreSum == 0) {
                coverage[retrieved_index] = 0;
            }
            else {
                coverage[retrieved_index] /= totalScoreSum;
            }
        }
    }

    public void calCoverageMult() {
        coverage = new double[documentList.size()];
        for (int i=0; i < documentList.size(); i++) {
            coverage[i] = coverageMult(i, "root");
            documentList.get(i).SetCoverageWithReference(coverage[i]);
        }
    }

    public void calCoverageMax() {
        coverage = new double[documentList.size()];
        for (int i=0; i < documentList.size(); i++) {
            coverage[i] = coverageMax(i);
            documentList.get(i).SetCoverageWithReference(coverage[i]);
        }
    }


    public ArrayList<Pair<Double,String>> getCoverageWithURLs() {
        ArrayList<Pair<Double,String>> result = new ArrayList<>();
        for (int i=0; i<documentList.size(); i++){
            result.add(new Pair<>(documentList.get(i).coverageWithReference,documentList.get(i).getURL()));
        }
        return result;
    }


    public void calScoresOfDocuments(String type){
        if (type.toLowerCase().contains("av")){
            calCoverageMult();
        }
        else if (type.toLowerCase().contains("max")){
            calCoverageMax();
        }
    }

    public void removeDuplicatesFromDocumentList(){
        Set<String> setOfTitles = new LinkedHashSet<>();
        ArrayList<RetrievedDocument> noDuplicates = new ArrayList<>();
        for (RetrievedDocument rd : documentList){
            String title = rd.getTitle();
            if (!setOfTitles.contains(title)){
                noDuplicates.add(rd);
                setOfTitles.add(title);
            }
        }
        documentList = noDuplicates;
    }

    public void setCoverageWithTitleSimilarity(){
        coverage = new double[documentList.size()];
        for (int i=0; i < documentList.size(); i++) {
            coverage[i] = documentList.get(i).get_titleSimilarityToReferenceTitle();
            documentList.get(i).SetCoverageWithReference(coverage[i]);
        }
    }

    public void getTextForDocumentsOnDocumentList(){
        long startCrawl = System.currentTimeMillis();
        ArrayList<RetrievedDocument> withOutNullBodies = new ArrayList<>();
        for (RetrievedDocument rd : documentList){
            Document doc = getDocument(rd.getURL());
            rd.setWeb_document(doc);
            rd.retrieveNameFromDoc();
            Pair<String, String> title_body = getTextWithBoilerFromDocument(doc);
            if (title_body != null ){
                String body = title_body.getValue();
                if (body != null && (!body.equals(""))) {
                    rd.addBody(body);
                    withOutNullBodies.add(rd);
                }
            }
        }
        documentList = withOutNullBodies;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("crawl time: " + Long.toString((System.currentTimeMillis()-startCrawl)/1000) + " seconds. real time is "+sdf.format(cal.getTime())); //boiler is insignificant
    }

    public void getTextForDocumentsOnDocumentListParallel() {
        long startCrawl = System.currentTimeMillis();
        documentList = CrawlThread.crawl(this.number_of_crawlers, documentList);
        ArrayList<RetrievedDocument> withOutNullBodies = new ArrayList<>();
        for (RetrievedDocument rd : documentList){
            Pair<String, String> title_body = getTextWithBoilerFromDocument(rd.getWeb_document()); //The function is different than the one in the other similar function
            if (title_body != null ){
                String body = title_body.getValue();
                if (body != null && (!body.equals(""))) {
                    rd.addBody(body);
                    withOutNullBodies.add(rd);
                }
            }
        }
        documentList = withOutNullBodies;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("crawl time: " + Long.toString((System.currentTimeMillis()-startCrawl)/1000) + " seconds. real time is "+sdf.format(cal.getTime())); //boiler is insignificant
    }

    //version is 0 if only fast, 1 if both, 2 if only slow.
    //type is 'avg' or 'max' for the formula version
    //withTitle is true iff we add the title to the formula (avg or max)
    // returns number of results we got.
    public void finalProjectResults(int version, String type, boolean withTitle, int numOfResults, String folderScorePath, Boolean saveResultsOfProject){
        if (version == 2){
            removeDuplicatesFromDocumentList();
        }
        else {
            documentList.forEach(this::addRetrievedDocumentToMap);
            FilterRetrievedDocumentsFromMapIntoTheList();
        }
        if (version == 0){
            setCoverageWithTitleSimilarity();
        }
        else {
            if (this.number_of_crawlers > 0) {
                getTextForDocumentsOnDocumentListParallel();
            } else {
                getTextForDocumentsOnDocumentList();
            }
            long startSimilarity = System.currentTimeMillis();
            initializeAfterRetrievedDocumentListIsSet();
            calScoresOfDocuments(type);
            if (withTitle) {
                addTitleScoreToDocumentScoreCalculation(5); //score given to title
            }
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            System.out.println("Similarity calculation time (includes parsing): " + Long.toString((System.currentTimeMillis()-startSimilarity)/1000) + " seconds. real time is "+sdf.format(cal.getTime()));
        }
        Collections.sort(documentList, ComparatorRetrievedDocumentByCoverageSimilarity);
        int numOfResultsFinal = documentList.size()>numOfResults ? numOfResults : documentList.size();
        documentList = new ArrayList<>(documentList.subList(0,numOfResultsFinal));
        List<Pair<Double,String>> scoresWithURLs = getCoverageWithURLs();
        //Collections.sort(scoresWithURLs, ComparatorDoubleString);
        //scoresWithURLs = scoresWithURLs.subList(0,numOfResultsFinal);
        if (saveResultsOfProject) {
            resultOfProjectIntoCSV(scoresWithURLs, folderScorePath);
        }
        setFinalResults(documentList);
    }

    private void setFinalResults(ArrayList<RetrievedDocument> docs) {
        for (RetrievedDocument ret : docs) {
            this.finalResults.add(new Pair<>(ret.getName(), ret.getText()));
        }
    }



    //for the training of classifier
    public ArrayList<double[][][]> getSentenceSimilaritiesFromScratch() {
        BasicParseOriginalReference();
        CreateRetrievedDocumentFromFolder();
        calculateIdf();
        VectorIntializeSentences();
        calculateSimilaritiesForSentances();
        return sentences_similarities;
    }

    public ArrayList<Pair<String,String>> getFinalResults() {
        return finalResults;
    }

//code comment missing part 7

}


