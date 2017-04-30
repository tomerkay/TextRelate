package TextRelateProject.StringParsing;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by Yair on 14/12/2016.
 */
public class CoreNLP {
    public static final StanfordCoreNLP pipeline;

    static {
        // this is your print stream, store the reference
        PrintStream err = System.err;

        // now make all writes to the System.err stream silent
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
        long startStanford = System.currentTimeMillis();
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        pipeline = new StanfordCoreNLP(props);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Stanford Modules loader: " + Long.toString((System.currentTimeMillis()-startStanford)/1000) + " seconds. real time is "+sdf.format(cal.getTime()));
        // set everything bck to its original state afterwards
        System.setErr(err);
    }

    public static Annotation parseText(String text) {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        //long start = System.currentTimeMillis();
        pipeline.annotate(document);
        //System.out.println("CoreNLP parse time: " + Long.toString(System.currentTimeMillis()-start));
        return document;
    }

}
