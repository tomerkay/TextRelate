package TextRelateProject.BingApi; /**
 * Created by Ronen on 06/11/2016.
 */

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.HashMap;

import static TextRelateProject.Helpers.helperFunctions.tryRetrieveTitleFromText;

public class TextExtractor {




    public static Document getDocument(String link) {
        Document doc = null;
        //long start1 = System.currentTimeMillis();
        try {
            doc = Jsoup.connect(link).timeout(3000).followRedirects(true).userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36").get();
            //System.out.println("crawl time: " + Long.toString(System.currentTimeMillis()-start1));
        } catch (IOException e) {
            //System.out.println("crawl time: " + Long.toString(System.currentTimeMillis()-start1));
            System.out.println("error loading html: "+link);
//            e.printStackTrace();
            return null;
        }
        return doc;
    }


    public static Pair<String, String> getTextWithBoilerFromDocument(Document doc) {
        String html = doc.html();
        try {
            final HTMLDocument htmlDoc = new HTMLDocument(html);
            final TextDocument textDocument = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
            String article = (new ArticleExtractor()).getText(html);
            if ( article == null || article == ""){
                return null;
            }
            String title = null;//textDocument.getTitle();
            if (title == null || title == ""){
                title = tryRetrieveTitleFromText(article);
            }
            article = FilterTextFromNonsense(article, title);
            if ( article == null){
                return null;
            }
            return new Pair<>(title, article);
        } catch (BoilerpipeProcessingException e) {
            System.out.println("cannot extract text");
        } catch (SAXException e) {
            System.out.println("cannot extract title");
        }
        return null;
    }


    public static String getTitleFromDocument(Document doc) {
        Elements metaOgTitle = doc.select("meta[property=og:title]");
        if (metaOgTitle!=null) {
            return metaOgTitle.attr("content");
        }
        return null;
    }



    public static String getDescriptionFromDocument(Document doc) {
        Elements metaOgTitle = doc.select("meta[property=og:description]");
        if (metaOgTitle!=null) {
            return metaOgTitle.attr("content");
        }
        return null;
    }

    private static void cancelAuthorizationOfMistakenCuts(Boolean[] splited_clean,Boolean[] cut, int i){
        while (i>=0 && splited_clean[i] && cut[i]){
            splited_clean[i] = false;
            i--;
        }
    }


    private static String FilterTextFromNonsense(String article, String title/*, String link*/){
        //code comment missing part 2
        String[] splited = article.split("\n");
        Boolean[] splited_clean = new Boolean[splited.length];
        Boolean[] cut = new Boolean[splited.length];
        for (int i=0;i<splited.length;i++){
            splited_clean[i] = false;
            cut[i] = false;
        }
        HashMap<String,Integer> seen_sentence = new HashMap<>();
        int splited_count =0;
        for (String paragraph : splited) {
            splited_count++;
            if (paragraph == null) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            String[] wordsInParagraph = paragraph.split(" ");
            String paragraphLowerCase = paragraph.toLowerCase();

            if ((paragraphLowerCase.contains("facebook") && !title.toLowerCase().contains("facebook"))) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            if ((paragraphLowerCase.contains("instagram") && !title.toLowerCase().contains("instagram"))) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            if (paragraphLowerCase.contains("sign") && (paragraphLowerCase.contains("twitter") || paragraphLowerCase.contains("facebook") || paragraphLowerCase.contains("instagram") || paragraphLowerCase.contains("mail") || paragraphLowerCase.contains("sign in") || paragraphLowerCase.contains("sign now") || paragraphLowerCase.contains("sign here") || paragraphLowerCase.contains("sign up"))) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            if ((paragraphLowerCase.contains("mail") ||paragraphLowerCase.contains("news")) && (paragraphLowerCase.contains("get") || paragraphLowerCase.contains("receive")|| paragraphLowerCase.contains("free"))) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            if (paragraphLowerCase.contains("click here") || paragraphLowerCase.contains("click below") || paragraphLowerCase.contains("click on")) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            if (paragraphLowerCase.contains("read more") || paragraphLowerCase.contains("read here") || paragraphLowerCase.contains("read this")) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }

            if (paragraph.contains("|") || paragraph.contains("@") || paragraph.contains("#") || paragraph.contains("^")) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }



            int check_for_troubled_symbol = paragraph.indexOf("%");
            boolean stop = false;
            while (check_for_troubled_symbol != -1){
                if (check_for_troubled_symbol == 0 || !(paragraph.charAt(check_for_troubled_symbol-1) >=0 && paragraph.charAt(check_for_troubled_symbol-1) <=9)){
                    stop = true;
                    break;
                }
                check_for_troubled_symbol = paragraph.indexOf("%", check_for_troubled_symbol+1);
            }
            check_for_troubled_symbol = paragraph.indexOf("$");
            while (check_for_troubled_symbol != -1){
                if (check_for_troubled_symbol == 0 || !(paragraph.charAt(check_for_troubled_symbol-1) >=0 && paragraph.charAt(check_for_troubled_symbol-1) <=9)){
                    stop = true;
                    break;
                }
                check_for_troubled_symbol = paragraph.indexOf("$", check_for_troubled_symbol+1);
            }

            check_for_troubled_symbol = paragraph.indexOf("&");
            while (check_for_troubled_symbol != -1){
                check_for_troubled_symbol++;
                while (check_for_troubled_symbol < paragraph.length() && (paragraph.charAt(check_for_troubled_symbol) == ' ' || paragraph.charAt(check_for_troubled_symbol) == '\t')) {
                    check_for_troubled_symbol++;
                }
                if (check_for_troubled_symbol == paragraph.length()) {
                    stop = true;
                    break;
                }
                if (!(paragraph.charAt(check_for_troubled_symbol) >='A' && paragraph.charAt(check_for_troubled_symbol) <='Z')){
                    stop = true;
                    break;
                }
                check_for_troubled_symbol = paragraph.indexOf("&", check_for_troubled_symbol);
            }
            if (stop){
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }





            if (wordsInParagraph.length < 11 && (paragraphLowerCase.contains("photo") || paragraphLowerCase.contains("image"))) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }


            int lastLetterPosition = paragraph.length() - 1;
            while (lastLetterPosition >= 0 && (paragraph.charAt(lastLetterPosition) == ' ' || paragraph.charAt(lastLetterPosition) == '\t')) {
                lastLetterPosition--;
            }
            if (lastLetterPosition == -1) {
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }
            char lastLetter = paragraph.charAt(lastLetterPosition);

            if ((lastLetter == '.' || lastLetter == '!') && wordsInParagraph.length < 11 && (splited_count-1 ==0 || cut[splited_count-2] == false)){
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                continue;
            }

            if (lastLetter >= 'a' && lastLetter <= 'z' || lastLetter >= 'A' && lastLetter <= 'Z' || lastLetter >= '0' && lastLetter <= '9' || lastLetter == '(' || lastLetter == ')' || lastLetter == '[' || lastLetter == ']' || lastLetter == '{' || lastLetter == '}') {
                if ((splited_count) == splited.length) {
                    cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                    continue;
                }
                int firstLetterPosition = 0;
                while (firstLetterPosition < splited[(splited_count)].length() && (splited[(splited_count)].charAt(firstLetterPosition) == ' ' || splited[(splited_count)].charAt(firstLetterPosition) == '\t')) {
                    firstLetterPosition++;
                }
                if (firstLetterPosition == splited[(splited_count)].length()) {
                    cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                    continue;
                }
                if (((splited[(splited_count)].charAt(firstLetterPosition) >= 'A' && splited[(splited_count)].charAt(firstLetterPosition) <= 'Z'))) {
                    cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                    continue;
                }
                if ((splited[(splited_count)].toLowerCase().contains("by") && splited[(splited_count)].split(" ").length < 7)) {
                    cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                    continue;
                }
                cut[splited_count-1] = true;
            }

            if (!seen_sentence.containsKey(paragraph)) {
                splited_clean[splited_count-1] = true;
            } else {
                int not_staying = seen_sentence.get(paragraph);
                splited_clean[not_staying] = false;//probably spam
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, splited_count-2);
                cancelAuthorizationOfMistakenCuts(splited_clean, cut, not_staying-1);
            }
        }


        String newArticle = "";
        for (int i=0 ;i<splited.length;i++) {
            if (splited_clean[i]) {
                if (cut[i]) {
                    newArticle += splited[i] + " ";
                } else {
                    newArticle += splited[i] + "\n";
                }
            }
        }
        //code missing comment part 3
        if (newArticle.split(" ").length < 100){
             return null;
        }
        int end_of_string = newArticle.indexOf("\n",3500) == -1 ? newArticle.length() : newArticle.indexOf("\n",3500);
        return newArticle.substring(0,end_of_string);  //don't wont too long articles
    }
}
