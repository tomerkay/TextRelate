package TextRelateProject.DocumentRanking;

import TextRelateProject.StringParsing.DocumentParser;
import TextRelateProject.StringParsing.StringParser;

import java.util.Arrays;

import static TextRelateProject.Helpers.helperFunctions.MakeSentenceIntoSimpleMapCountStemmedWords;

/**
 * Created by Admin on 27-Nov-16.
 */
public class RetrievedDocument extends DocumentParser {
    //protected StringVector[] document_vector; //not needed any more.
    protected Integer countOfRetrieval; //needed for first filter of retrived documents
    protected StringVector title_vector_simple; //needed for first filter of retrived documents
    protected StringVector description_vector_simple; //needed for first filter of retrived documents
    protected Double titleSimilarityToReferenceTitle; //needed for first filter of retrived documents
    protected Double descriptionSimilarityToReferenceDescription; //needed for first filter of retrived documents
    protected StringVector[][] sentence_vectors; //for every sentence, has cell for each feature
    protected StringVector[] title_vector_features; //for each feature
    protected Double coverageWithReference; //only used when we want to save best files


    public RetrievedDocument(String url, String title, String description) {
        super(url, title, description);
    }

    public void InitializeMembersOfRetrievedDocument(){
        countOfRetrieval = 1;
        title_vector_simple = new Vector_Simple_Count(MakeSentenceIntoSimpleMapCountStemmedWords(title));
        description_vector_simple = new Vector_Simple_Count(MakeSentenceIntoSimpleMapCountStemmedWords(description));
    }

    @Override
    public void addBody(String body) {
        String[] temp_sents = StringParser.sentSplitOpenNLP(body);
        if (temp_sents.length > 40) {
            temp_sents = Arrays.copyOfRange(temp_sents, 0, 40);
            body = String.join("\n", temp_sents);
        }
        original_text = body;
    }

    public RetrievedDocument() {}

    public void addOneToCountOfRetrieval(){
        countOfRetrieval++;
    }

    public Integer get_countOfRetrieval(){
        return countOfRetrieval;
    }

    public void CalculateSimilaritiesToReference(ReferenceDocument rd){
        titleSimilarityToReferenceTitle = title_vector_simple.getSimilarity(rd.title_vector_simple);
        descriptionSimilarityToReferenceDescription = description_vector_simple.getSimilarity(rd.description_vector_simple);
    }

    public Double get_titleSimilarityToReferenceTitle(){
        return titleSimilarityToReferenceTitle;
    }

    public Double get_descriptionSimilarityToReferenceDescription(){
        return descriptionSimilarityToReferenceDescription;
    }

    public Double getCoverageWithReference(){
        return coverageWithReference;
    }

    public void SetCoverageWithReference(double coverageWithReference){
        this.coverageWithReference = coverageWithReference;
    }
}
