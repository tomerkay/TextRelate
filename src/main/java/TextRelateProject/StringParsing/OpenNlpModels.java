package TextRelateProject.StringParsing;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Ronen on 12/12/2016.
 */
public class OpenNlpModels {
    static public final SentenceDetectorME sentence_splitter;
    static public final Tokenizer tokenizer;
    static public final POSTaggerME pos_tagger;
    static public final ChunkerME chunker;
    static public final NameFinderME person_finder;
    static public final NameFinderME location_finder;
    static public final NameFinderME organization_finder;
    static public final PorterStemmer stemmer;



    static {
        long startOpenNLP = System.currentTimeMillis();
        sentence_splitter = new SentenceDetectorME(getSentenceModel());
        tokenizer = new TokenizerME(getTokenModel());
        pos_tagger = new POSTaggerME(getPosModel());
        chunker = new ChunkerME(getChunkerModel());
        person_finder = new NameFinderME(getNameFinder("./project_files_aux/TextRelate_files/OpenNLP_models/en-ner-person.bin"));
        location_finder = new NameFinderME(getNameFinder("./project_files_aux/TextRelate_files/OpenNLP_models/en-ner-location.bin"));
        organization_finder = new NameFinderME(getNameFinder("./project_files_aux/TextRelate_files/OpenNLP_models/en-ner-organization.bin"));
        stemmer = new PorterStemmer();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("OpenNLP Modules loader: " + Long.toString((System.currentTimeMillis()-startOpenNLP)/1000) + " seconds. real time is "+sdf.format(cal.getTime()));

    }

    private static SentenceModel getSentenceModel() {
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream("./project_files_aux/TextRelate_files/OpenNLP_models/en-sent.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SentenceModel model = null;
        try {
            model = new SentenceModel(modelIn);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return model;
    }

    private static TokenizerModel getTokenModel() {
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream("./project_files_aux/TextRelate_files/OpenNLP_models/en-token.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        TokenizerModel model=null;
        try {
            model = new TokenizerModel(modelIn);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
        return model;
    }

    private static POSModel getPosModel() {
        InputStream modelIn = null;
        POSModel model = null;
        try {
            modelIn = new FileInputStream("./project_files_aux/TextRelate_files/OpenNLP_models/en-pos-maxent.bin");
            model = new POSModel(modelIn);
        }
        catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return model;
    }

    private static ChunkerModel getChunkerModel() {
        InputStream modelIn = null;
        ChunkerModel model = null;

        try {
            modelIn = new FileInputStream("./project_files_aux/TextRelate_files/OpenNLP_models/en-chunker.bin");
            model = new ChunkerModel(modelIn);
        } catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
        return model;
    }

    private static TokenNameFinderModel getNameFinder(String path_to_model) {
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(path_to_model);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        TokenNameFinderModel model = null;
        try {
            model = new TokenNameFinderModel(modelIn);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return model;
    }

}
