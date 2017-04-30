package TextRelateProject.StringParsing;

import TextRelateProject.Helpers.BagOfWords;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.log;


/**
 * Created by Ronen on 27/11/2016.
 */
public class DocumentParser {
    protected String name;
    protected String url;
    protected String title;
    protected String description; //not from boilerAndParse pipe, from Document
    protected String original_text;

    protected Document web_document;

    protected Annotation titleAnnotation;
    protected CoreMap titleStanford;
    protected String[] title_tokens;
    protected String[] title_tokensStemmed;
    protected String[] title_tokenPos;
    protected String[] title_chuckedTokens;
    protected String[] title_NPTokens;
    protected String[] title_NPTokensStemmed;
//code comment missing part 1
    protected String[] title_NamesTokens;
    protected String[] title_NamesTokensStemmed;


    protected Annotation document;
    protected List<CoreMap> stanfordSentences;
    protected String[] sentences;
    protected String[][] tokens;
    protected String[][] tokenPOS;
    protected String[][] tokensStemmed;
    protected String[][] tokensLemmatized;
    protected HashMap<String, Integer> freqWordsStemmed;
    protected String[][] chunckedTokens;
    protected String[][] NPTokens;
    protected String[][] NPTokensStemmed;
    protected String[][] NamesTokens;
    protected String[][] NamesTokensStemmed;
//code comment missing part 2

    public HashMap<String, Double> llr;

    public DocumentParser(String url, String title, String description) {
        this.url = url;
        this.title = title; //this is not boilerAndParse pipe title but the "Document" one
        this.description = description;
    }

    public DocumentParser() {}

    public void setTitle(String title) { this.title = title; }

    public void addBody(String body){
        original_text = body;
    }

    public void parseDocumentTextWithPath(String path) {
        original_text = StringParser.parseDocument(path);
    }

    public void basicParseTextOpenNLP() {
        sentences = StringParser.sentSplitOpenNLP(original_text);
        tokens = StringParser.sentIntoTokensOpenNLP(sentences);
        tokenPOS = StringParser.POStaggerOpenNLP(tokens);
        tokensStemmed = StringParser.stemOpenNLPMatrix(tokens);
        freqWordsStemmed = StringParser.countFrequency(tokensStemmed);
        chunckedTokens = StringParser.chunkOpenNLPMatrix(tokens, tokenPOS);
        NPTokens = StringParser.findNPtokensMatrix(tokens, chunckedTokens);
        NPTokensStemmed = StringParser.stemOpenNLPMatrix(NPTokens);
        findNamesTokensOpenNLP();
        //NameTokensStemmed = StringParser.stemOpenNLPMatrix(NameTokens); //no need
        //LocationTokensStemmed = StringParser.stemOpenNLPMatrix(LocationTokens); //no need
        //OrganizationTokensStemmed = StringParser.stemOpenNLPMatrix(OrganizationTokens); //no need
        NamesTokensStemmed = StringParser.stemOpenNLPMatrix(NamesTokens);
        //freqNP = StringParser.countFrequency(NPTokensStemmed); //no need
    }



    public void basicParseTextStanford() {
        document = CoreNLP.parseText(original_text);
        stanfordSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        sentences = StringParser.sentSplitStanford(document);
        tokens = StringParser.sentIntoTokensStanford(document);
        tokenPOS = StringParser.POStaggerOpenStanford(document);
        tokensStemmed = StringParser.stemOpenNLPMatrix(tokens);
        freqWordsStemmed = StringParser.countFrequency(tokensStemmed);
        chunckedTokens = StringParser.chunkOpenNLPMatrix(tokens, tokenPOS);
        NPTokens = StringParser.findNPtokensMatrix(tokens, chunckedTokens);
        NPTokensStemmed = StringParser.stemOpenNLPMatrix(NPTokens);
        findNamesTokensStanford();
        NamesTokensStemmed = StringParser.stemOpenNLPMatrix(NamesTokens);
        tokensLemmatized = StringParser.lemmaStanford(document);
    }

    public void parseTitleOpenNLP() {
        if (title == null || title.equals("") || title.contains("Unclear whether truck")) {
            return;
        }
        title_tokens = OpenNlpModels.tokenizer.tokenize(title);
        title_tokensStemmed = StringParser.stemOpenNLPSentence(title_tokens);
        title_tokenPos = OpenNlpModels.pos_tagger.tag(title_tokens);
        title_chuckedTokens = OpenNlpModels.chunker.chunk(title_tokens, title_tokenPos);
        title_NPTokens = StringParser.findNPtokensSentence(title_tokens, title_chuckedTokens);
        title_NPTokensStemmed = StringParser.stemOpenNLPSentence(title_NPTokens);
        title_NamesTokens = StringParser.findNamesOpenNLP(title_tokens);
        title_NamesTokensStemmed = StringParser.stemOpenNLPSentence(title_NamesTokens);

//code comment missing part 3
    }



    public void parseTitleStanford() {
        if (title == null || title.equals("")) {
            return;
        }
        titleAnnotation = CoreNLP.parseText(title);
        titleStanford = titleAnnotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        title_tokens = StringParser.sentIntoTokensStanford(titleAnnotation)[0];
        title_tokensStemmed = StringParser.stemOpenNLPSentence(title_tokens);
        title_tokenPos = StringParser.POStaggerOpenStanford(titleAnnotation)[0];
        title_chuckedTokens = OpenNlpModels.chunker.chunk(title_tokens, title_tokenPos);
        title_NPTokens = StringParser.findNPtokensSentence(title_tokens, title_chuckedTokens);
        title_NPTokensStemmed = StringParser.stemOpenNLPSentence(title_NPTokens);
        title_NamesTokens = StringParser.findNamesStanford(titleStanford);
        title_NamesTokensStemmed = StringParser.stemOpenNLPSentence(title_NamesTokens);

    }

    public void findNamesTokensOpenNLP() {
        //code comment missing part 4

        NamesTokens = new String[sentences.length][];
        for (int i=0; i< sentences.length; i++) {
            //code comment missing part 5
            NamesTokens[i] = StringParser.findNamesOpenNLP(tokens[i]);
        }
    }

    public void findNamesTokensStanford() {
        NamesTokens = new String[sentences.length][];
        int i = 0;
        for (CoreMap sentence : stanfordSentences) {
            NamesTokens[i] = StringParser.findNamesStanford(sentence);
            i++;
        }
        NamesTokensStemmed = StringParser.stemOpenNLPMatrix(NamesTokens);
    }




//code comment missing part 6



    public String getName() {
        return name;
    }

    public void retrieveNameFromDoc() {
        name = "couldn't_retrieve"; //not supposed to stay, we call this function only after web_doc is set
        if (web_document == null) {
            return;
        }
        Elements metaOgSiteName = web_document.select("meta[property=og:site_name]");
        if (metaOgSiteName!=null && !metaOgSiteName.attr("content").equals("")) {
            name = metaOgSiteName.attr("content");
            Pattern p = Pattern.compile( "www.(.*)");
            Matcher m = p.matcher(name);
            if (m.find() && !Objects.equals(m.group(1), "")) {
                name = m.group(1);
            }
            String regex2 = "(.*?)" + Pattern.quote(".");
            p = Pattern.compile(regex2);
            m = p.matcher(name);
            if (m.find() && !Objects.equals(m.group(1), "")) {
                name = m.group(1);
            }
        }
        else {
            Elements metaOgUrl = web_document.select("meta[property=og:url]");
            if (metaOgUrl!=null && !metaOgUrl.attr("content").equals("")) {
                String url = metaOgUrl.attr("content");
                String regex = Pattern.quote("www.") + "(.*?)" + Pattern.quote(".");
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(url);
                if (m.find() && !Objects.equals(m.group(1), "")) {
                    name = m.group(1);
                }
            }
        }
    }

    public String getURL() {return url;}

    public void setURL(String url) { this.url = url; }

    public String getText() { return original_text; }

    public Document getWeb_document() { return web_document; }

    public void setWeb_document(Document doc) {
        this.web_document = doc;
    }

    public String getTitle() {
        return this.title;
    }

    public String[] getSentences() { return sentences; }

    public String[][] getTokens() {
        return tokens;
    }

    public String[] getTitleTokens() { return title_tokens;}

    public String[][] getTokensStemmed(){ return tokensStemmed;}

    public String[] getTokensStemmedOfTitle(){ return title_tokensStemmed;}

    public HashMap<String, Integer> getFreqWordsStemmed(){return freqWordsStemmed;}

    public String[][] getNPTokens() {
        return NPTokens;
    }

    public String[][] getNPTokensStemmed() {
        return NPTokensStemmed;
    }

    public String[] getNPTokensStemmedOfTitle() {
        return title_NPTokensStemmed;
    }

//code comment missing part 7

    public String[][] getNamesTokensStemmed() {
        return NamesTokensStemmed;
    }

    public String[] getNamesTokensStemmedOfTitle() {
        return title_NamesTokensStemmed;
    }

    public void calculateLLR() {
        llr = new HashMap<>();
        for (String key : freqWordsStemmed.keySet()) {
            llr.put(key, (double) getWeightLLR(key));
        }
    }

    /**
     * finds the log likelihood ratio of a given word.
     * relies on the formula given in http://delivery.acm.org/10.1145/980000/972454/p61-dunning.pdf?ip=213.57.109.111&id=972454&acc=OPEN&key=4D4702B0C3E38B35%2E4D4702B0C3E38B35%2E4D4702B0C3E38B35%2E6D218144511F3437&CFID=875320129&CFTOKEN=90277798&__acm__=1481585547_f6e5605d0bc4e87295b8aee78b6de165
     * (dunning, 1993)
     * and the threshold taken from Jurafski & Martin, 2nd edition, formula 23.26.
     */
    public int getWeightLLR(String word) {
        int k1 = this.freqWordsStemmed.get(word);
        long k2 = BagOfWords.corpus_count.getOrDefault(word, 0L);
        int n1 = this.freqWordsStemmed.values().stream().mapToInt(Number::intValue).sum();
        long n2 = BagOfWords.num_words_in_corpus;
        double p1 = ((double)k1)/n1;
        double p2 = ((double)k2)/n2;
        double p = (((double)k1)+k2)/(n1+n2);
        if (2*(getLogLikelihood(p1,k1,n1) + getLogLikelihood(p2,k2,n2) - getLogLikelihood(p,k1,n1) -
                getLogLikelihood(p,k2,n2)) > 10) {
            return 1;
        }
        return 0;
    }

    private double getLogLikelihood(double p, long k, long n) {
        return k*log(p) + (n-k)*log(1-p);
    }



//code comment missing part 8


}
