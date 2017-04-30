package TextMergeProject;

import javafx.util.Pair;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Main {

    public static String mergeForTextRelate(ArrayList<Pair<String, String>> text_list, String out_filename,
                                          int num_of_lines, Boolean saveMerge) throws Exception {
        TextBuilder builder = new TextBuilder(text_list);
        String text = builder.build(num_of_lines);
        if (saveMerge) {
            PrintWriter writer = new PrintWriter(out_filename, "UTF-8");
            writer.println(text);
            writer.close();
        }
        return text;
    }


    private static void validateInput(String[] args) {
        if (args.length < 3) {
            throw new RuntimeException("Insufficient number of arguments. Please enter:\n" +
                    "1. An absolute folder path with text documents\n" +
                    "2. The number of sentences to be in the summary\n" +
                    "3. -a for a summary of all files or -i for an individual summary for each separate file in the folder");
        }
        try {
            Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid number of lines.");
        }

        File folder = new File(args[0]);
        File[] docList = folder.listFiles();

        if (docList == null) {
            throw new RuntimeException("Invalid directory path");
        }

        if (!(args[2].equals("-i") || args[2].equals("-a")||args[2].equals("-ia"))) {
            throw new RuntimeException("Unknown summary mode.\nPlease make sure your third parameter is either:\n-a for a summary of all files\n-i for an individual summary for each separate file in the folder");
        }
    }

    public static void main(String[] args) throws Exception {
        validateInput(args);

        File folder = new File(args[0]);
        File[] docList = folder.listFiles();
        ArrayList<String> paths = new ArrayList<String>();
        for (File currFile : docList) {
            if (currFile.isFile() && currFile.canRead()) {
                paths.add(currFile.getAbsolutePath());
            }
        }

        if (args[2].equals("-a")||args[2].equals("-ia")) {
            TextBuilder builder = new TextBuilder(paths.toArray(new String[paths.size()]));
            PrintWriter writer = new PrintWriter(args[0]+"/all.txt", "UTF-8");
            writer.println(builder.build(Integer.parseInt(args[1])));
            writer.close();
            
        }
        
        
        if(args.length==5){
        	double portions = (double)Integer.parseInt(args[args.length-2]);
        	double curr = (double)Integer.parseInt(args[args.length-1]);
        	TextBuilder.PORTIONS_COUNT=portions;
        	TextBuilder.CURRENT_PORTION=curr;
        }
        
        
        if (args[2].equals("-i")||args[2].equals("-ia")) {
            for (String currFile : paths) {
                System.out.println("Summary for " + currFile +":\n");
                TextBuilder builder = new TextBuilder(new String[] { currFile });
                PrintWriter writer = new PrintWriter(currFile.replace(".txt", ".sum.txt"), "UTF-8");
                
                if(args[2].equals("-ia")&&args.length>3){
                    writer.println(builder.build(Integer.parseInt(args[3])));
                }else{
                	writer.println(builder.build(Integer.parseInt(args[1])));
                }
                
                writer.close();
            }
        }
    }
}
