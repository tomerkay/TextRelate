package TextRelateProject.QueryGenerator; /**
 * Created by Admin on 21-Nov-16.
 */

import TextRelateProject.StringParsing.OpenNlpModels;
import javafx.util.Pair;

import java.io.*;
import java.util.*;


/**
 * this class is used to count the frequencies of the stemmed words from the bag of words.
 */
public class CountCommonWords {
    private File file;
    private int k;
    private HashMap<String, Long> freq;

    public CountCommonWords(String path,int k) {
        file = new File(path);
        this.k=k;
        freq= new HashMap<>();
        CreateFreq();
    }
    @Deprecated
    public String[][] createTableOfSTem(){
        String[][] table = new String[k][2];
        FileReader fr = null;
        int i=0;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String line;
        try {
            while((line = br.readLine()) != null&&i<k){
                table[i][0]= OpenNlpModels.stemmer.stem(line.split("\t")[0]).toLowerCase();
                table[i][1]=line.split("\t")[1];
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    public ArrayList<Pair<String, Long>> createCountOfSTem(){
        ArrayList<Pair<String, Long>> table = new ArrayList<>();
        FileReader fr = null;
        int i=0;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String line;
        try { // TODO: add only words that are real words, not '?' etc.
            while((line = br.readLine()) != null&&i<k){
                String[] words = OpenNlpModels.stemmer.stem(line.split("\t")[0]).toLowerCase().split("\\s+");
                Long count = Long.parseLong(line.split("\t")[1]);
                for (String s : words) {
                    if ((s.charAt(0) >= 'a' && s.charAt(0) <= 'z')) {
                        table.add(new Pair<>(s, count));
                    }
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    public void CreateFreq(){
        ArrayList<Pair<String, Long>> counts = createCountOfSTem();
        for (Pair<String, Long> pair : counts) {
            if (freq.containsKey(pair.getKey())){
                freq.replace(pair.getKey(),freq.get(pair.getKey())+pair.getValue());
            }
            else{
                freq.put(pair.getKey(), pair.getValue());
            }
        }
    }

    // path should be with .ser extension
    public void serialize() {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("CommonWords_1w.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this.freq);
            out.close();
            fileOut.close();
        }catch(IOException i) {
            i.printStackTrace();
        }
    }

    public void intoCSV(){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("CommonWords_1w.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        Set<String> words=freq.keySet();
        ArrayList<Long> numsList=new ArrayList<>();
        for (String word:words){
            numsList.add(freq.get(word));
        }
        Collections.sort(numsList);
        Collections.reverse(numsList);
        //remember " makes problems in Excel...
        while (!(numsList.isEmpty())){
            Long num=numsList.remove(0);
            String word="";
            for (Map.Entry<String, Long> entry : freq.entrySet()) {
                if (Objects.equals(num, entry.getValue())) {
                    word= entry.getKey();
                    break;
                }
            }
            freq.remove(word);
            // check if first char is a letter, indicating the word is alphabetic
            if ((word.charAt(0) >= 'a' && word.charAt(0) <= 'z') || (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')) {
                sb.append(word);
                sb.append(',');
                sb.append(Long.toString(num));
                sb.append('\n');
            }
        }
        pw.write(sb.toString());
        pw.close();
    }

    public static void main(String[] argv){
        long start = System.currentTimeMillis();
        CountCommonWords CCW = new CountCommonWords(argv[0],Integer.parseInt(argv[1]));
        CCW.serialize();
        CCW.intoCSV();
        System.out.println("count time: " + Long.toString(System.currentTimeMillis()-start));
    }

}
