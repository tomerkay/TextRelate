package TextMergeProject;

import javafx.util.Pair;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class InputParser {
    private static final String PARAGRAPH_SPLIT_REGEX = "(?x)  (?: [ \\t\\r\\f\\v]*? \\n ){2}  [ \\t\\r\\f\\v]*?";
    // OpenNLP Utilities
    private SentenceDetectorME sentenceDetector;
    private Tokenizer tokenizer;

    public InputParser(){
        try {
            // Initialize sentence detector
            File sentenceIS = new File("./project_files_aux/TextMerge_files/en-sent.bin");
            SentenceModel sentenceModel = new SentenceModel(sentenceIS);
            this.sentenceDetector = new SentenceDetectorME(sentenceModel);
            this.sentenceDetector = new SentenceDetectorME(sentenceModel);

            // Initialize tokenizer
            File tokenizerIS = new File("./project_files_aux/TextMerge_files/en-token.bin");
            TokenizerModel tokenizerModel = new TokenizerModel(tokenizerIS);
            this.tokenizer = new TokenizerME(tokenizerModel);
        }catch (IOException e){
            throw new RuntimeException("Could not load OpenNLP.\n" + e.getMessage());
        }
    }

    /**
     * Returns an ArrayList of document object according to the given paths
     * @param paths
     * @return
     */
    public ArrayList<Document> parseDocuments(String[] paths) {
        // Return an ArrayList of parsed Document objects
        return createDocuments(paths);
    }

    /**
     * Returns an ArrayList of document object according to the given texts
     * @param text_list : list of texts with their names. key: name, value: text.
     * @return same as before. the path will be null.
     */
    public ArrayList<Document> parseDocuments(ArrayList<Pair<String, String>> text_list) {
        // Return an ArrayList of parsed Document objects
        return createDocuments(text_list);
    }

    private ArrayList<Document> createDocuments(String[] paths) {
        ArrayList<Document> documentArrayList = new ArrayList<Document>();
        PorterStemmer stemmer = new PorterStemmer();

        try {
            int docCounter = 0;

            // Iterate over all file paths
            for (String filepath : paths) {
                // Parse each sentence into term strings and build the document objects
                ArrayList<Sentence> sentencesArrayList = new ArrayList<Sentence>();
                int sentenceIndexInDoc = 0;

                // Read each file into a string and split it into strings of sentences
                File documentFile = new File(filepath);
                String documentText = deserializeString(documentFile);
                int numOfSentencesInDoc = sentenceDetector.sentDetect(documentText).length;

                // Changed split logic to recognize first sentences
                String[] paragraphs = documentText.split(PARAGRAPH_SPLIT_REGEX);
                for (String currParagraph : paragraphs) {
                    String sentences[] = sentenceDetector.sentDetect(currParagraph);
                    boolean isFirstInParagraph = true;

                    for (String sentence : sentences) {
                        String tokens[] = tokenizer.tokenize(sentence);
                        ArrayList<Term> termsArrayList = new ArrayList<Term>();

                        for (String token : tokens) {
                            termsArrayList.add(new Term(stemmer.stem(token)));
                        }

                        sentencesArrayList.add(new Sentence(sentence, termsArrayList,isFirstInParagraph,
                                (sentenceIndexInDoc / (double)numOfSentencesInDoc), documentFile.getName()));
                        isFirstInParagraph = false;
                        sentenceIndexInDoc++;
                    }

                }

                documentArrayList.add(new Document(filepath, sentencesArrayList));
                docCounter++;
            }

        } catch (IOException e) {
            return null;
        }

        return documentArrayList;
    }

    /**
     * same as original, but with strings instead of files.
     * @param text_list : list of texts with their names. key: name, value: text.
     * @return same as before. the path will be null.
     */
    private ArrayList<Document> createDocuments(ArrayList<Pair<String, String>> text_list) {
        ArrayList<Document> documentArrayList = new ArrayList<Document>();
        PorterStemmer stemmer = new PorterStemmer();

        int docCounter = 0;

        // Iterate over all file paths
        for (Pair<String, String> text_pair : text_list) {
            String name = text_pair.getKey();

            // Parse each sentence into term strings and build the document objects
            ArrayList<Sentence> sentencesArrayList = new ArrayList<Sentence>();
            int sentenceIndexInDoc = 0;

            // YF: split text into strings of sentences
            String documentText = text_pair.getValue();
            int numOfSentencesInDoc = sentenceDetector.sentDetect(documentText).length;

            // Changed split logic to recognize first sentences
            String[] paragraphs = documentText.split(PARAGRAPH_SPLIT_REGEX);
            for (String currParagraph : paragraphs) {
                String sentences[] = sentenceDetector.sentDetect(currParagraph);
                boolean isFirstInParagraph = true;

                for (String sentence : sentences) {
                    String tokens[] = tokenizer.tokenize(sentence);
                    ArrayList<Term> termsArrayList = new ArrayList<Term>();

                    for (String token : tokens) {
                        termsArrayList.add(new Term(stemmer.stem(token)));
                    }

                    sentencesArrayList.add(new Sentence(sentence, termsArrayList,isFirstInParagraph,
                            (sentenceIndexInDoc / (double)numOfSentencesInDoc), name));
                    isFirstInParagraph = false;
                    sentenceIndexInDoc++;
                }

            }

            documentArrayList.add(new Document(null, sentencesArrayList));
            docCounter++;
        }

        return documentArrayList;
    }

    /**
     * Gets a file object and returns a string of its contents
     * @param file
     * @return
     * @throws IOException
     */
    private String deserializeString(File file)
            throws IOException {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        //final FileReader reader = new FileReader(file);
        try {
            // Keep reading the file until done
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }

            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + file.getPath());
        }

        // Return the content string
        return buffer.toString();
    }
}
