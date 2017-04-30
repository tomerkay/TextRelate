package TextRelateProject.DocumentRanking;


import TextRelateProject.Helpers.PrettyPrinterOfMatrix;
import TextRelateProject.Helpers.helperFunctions;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.core.*;

import java.io.*;

/**
 * Created by Admin on 05-Dec-16.
 */
public class BuildClassifier {
    //not generic
    private static Attribute All_tf_idf;
    private static Attribute NP_tf_idf;
    private static Attribute Names_tf_idf;
    private static Attribute All_tf;
    private static Attribute NP_tf;
    private static Attribute Names_tf;
    private static FastVector fvClassVal;
    private static Attribute ClassAttribute;
    private static FastVector fvWekaAttributes;
    public static int numOfAttributes;
    private Instances isTrainingSet;
    private Instances isTestSet;
    private Classifier cModel;

    static {
        //not generic
        All_tf_idf = new Attribute("All_tf_idf");
        NP_tf_idf = new Attribute("NP_tf_idf");
        Names_tf_idf = new Attribute("Names_tf_idf");
        All_tf = new Attribute("All_tf");
        NP_tf = new Attribute("NP_tf");
        Names_tf = new Attribute("Names_tf");
        fvClassVal = new FastVector(2);
        fvClassVal.addElement("similar");
        fvClassVal.addElement("not_similar");
        ClassAttribute = new Attribute("theClass", fvClassVal);
        //not generic
        fvWekaAttributes = new FastVector(7); //notice the number!
        fvWekaAttributes.addElement(All_tf_idf);
        fvWekaAttributes.addElement(NP_tf_idf);
        fvWekaAttributes.addElement(Names_tf_idf);
        fvWekaAttributes.addElement(All_tf);
        fvWekaAttributes.addElement(NP_tf);
        fvWekaAttributes.addElement(Names_tf);
        fvWekaAttributes.addElement(ClassAttribute);
        numOfAttributes=fvWekaAttributes.size()-1;
    }

    //for the serialization process
    public BuildClassifier() {
    }

    public BuildClassifier(String pathForTrain, String pathForTest) {
        BuildTrainingSet(pathForTrain, true);
        isTestSet = null;
        if (pathForTest != null) {
            BuildTrainingSet(pathForTest, false);
        }
        try {
            //choose classifier

//            cModel = new Logistic();
//            String[] options = new String[2];
//            options[0] = "-R";
//            options[1] = "2";
//            ((Logistic)cModel).setOptions(options);

            cModel = new NaiveBayes();
            //((NaiveBayes)cModel).setOptions(new String[] {"-K"}); //for NaiveBayes
            //((NaiveBayes)cModel).setOptions(new String[] {"-D"}); //for NaiveBayes
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cModel.buildClassifier(isTrainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //if train is true than it is for training set, and false for test set
    public void BuildTrainingSet(String path, Boolean train){
        if (train) {
            isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
            isTrainingSet.setClassIndex(numOfAttributes);
        }
        else {
            isTestSet = new Instances("Rel", fvWekaAttributes, 10);
            isTestSet.setClassIndex(numOfAttributes);
        }
        BufferedReader br = readFromCsv(path);
        String line = "";
        String cvsSplitBy = ",";
        try {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] values = line.split(cvsSplitBy);

                Instance instanceLine = new DenseInstance(numOfAttributes+1);
                for (int k=0; k<numOfAttributes; k++){
                    instanceLine.setValue((Attribute)fvWekaAttributes.elementAt(k), Double.valueOf(values[k]));
                }
                String classValue = (Integer.valueOf(values[numOfAttributes]) == 1 ? "similar" : "not_similar");
                instanceLine.setValue((Attribute)fvWekaAttributes.elementAt(numOfAttributes), classValue);
                if (train) {
                    isTrainingSet.add(instanceLine);
                }
                else{
                    isTestSet.add(instanceLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader readFromCsv(String path) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return br;
    }

    public void printForEachInstance(){
        int numTestInstances = isTestSet.numInstances();
        System.out.printf("There are %d test instances\n", numTestInstances);
        // Loop over each test instance.
        for (int i = 0; i < numTestInstances; i++)
        {
            // Get the true class label from the instance's own classIndex.
            String trueClassLabel =
                    isTestSet.instance(i).toString(isTestSet.classIndex());
            double[] attributes = new double[numOfAttributes];
            for (int k=0; k<numOfAttributes;k++){
                attributes[k] = isTestSet.instance(i).value(isTestSet.attribute(k));
            }
            // Make the prediction here.
            double predictionIndex = 0;
            try {
                predictionIndex = cModel.classifyInstance(isTestSet.instance(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Get the predicted class label from the predictionIndex.
            String predictedClassLabel =
                    isTestSet.classAttribute().value((int) predictionIndex);
            String answer = trueClassLabel.compareTo(predictedClassLabel) == 0 ? "correct" : "wrong";
            // Get the prediction probability distribution.
            double[] predictionDistribution =
                    new double[0];
            try {
                predictionDistribution = cModel.distributionForInstance(isTestSet.instance(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Print out the true label, predicted label, and features valus.
            System.out.printf("%5d: true=%-11s, predicted=%-11s, ", i, trueClassLabel, predictedClassLabel);

            // Print the features valus.
            for (int k=0; k<numOfAttributes;k++) {
                if (k < numOfAttributes - 1) {
                    System.out.printf("%-11s=%6.3f, ", isTestSet.attribute(k).toString().split(" ")[1] ,attributes[k]);
                } else {
                    System.out.printf("%-11s=%6.3f\n", isTestSet.attribute(k).toString().split(" ")[1], attributes[k]);
                }
            }

            // Print the distribution.
            System.out.printf("       distribution= ", predictedClassLabel);

            // Loop over all the prediction labels in the distribution.
            for (int predictionDistributionIndex = 0;
                 predictionDistributionIndex < predictionDistribution.length;
                 predictionDistributionIndex++)
            {
                // Get this distribution index's class label.
                String predictionDistributionIndexAsClassLabel =
                        isTestSet.classAttribute().value(
                                predictionDistributionIndex);

                // Get the probability.
                double predictionProbability =
                        predictionDistribution[predictionDistributionIndex];

                System.out.printf("[%11s : %6.3f]",
                        predictionDistributionIndexAsClassLabel,
                        predictionProbability );
            }
            System.out.print(" - "+answer+"!");

            System.out.printf("\n");
        }
    }
    public void printEvaluationOfClassifer() {
        Evaluation eTest = null;
        try {
            eTest = new Evaluation(isTestSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            eTest.evaluateModel(cModel, isTestSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String strSummary = eTest.toSummaryString();
        System.out.println(strSummary);

        System.out.println("the confusion matrix:");
        double[][] cmMatrix = eTest.confusionMatrix();
        String[][] FullConfusionMatrix = new String[cmMatrix.length+1][cmMatrix.length+1];
        FullConfusionMatrix[0][0] = new String("Actual\\Predicted");
        for (int j=1;j<cmMatrix[0].length+1; j++){
            FullConfusionMatrix[0][j] = new String(isTestSet.classAttribute().value(j-1));
        }
        for (int i=1;i<cmMatrix.length+1; i++){
            FullConfusionMatrix[i][0] = new String(isTestSet.classAttribute().value(i-1));
        }
        for (int i = 0; i < cmMatrix.length; i++) {
            for (int j = 0; j < cmMatrix[i].length; j++) {
                FullConfusionMatrix[i+1][j+1] = String.valueOf(cmMatrix[i][j]);
            }
        }
        final PrettyPrinterOfMatrix printer = new PrettyPrinterOfMatrix(System.out);
        printer.print(FullConfusionMatrix);
        double precision = cmMatrix[0][0]/(cmMatrix[0][0]+cmMatrix[1][0]);
        double recall = cmMatrix[0][0]/(cmMatrix[0][0]+cmMatrix[0][1]);
        double f1 = 2*(precision*recall)/(precision+recall);
        System.out.println("Precision: "+(int)cmMatrix[0][0]+"/("+(int)cmMatrix[0][0]+"+"+(int)cmMatrix[1][0]+") = "+precision);
        System.out.println("Recall: "+(int)cmMatrix[0][0]+"/("+(int)cmMatrix[0][0]+"+"+(int)cmMatrix[0][1]+") = "+recall);
        System.out.println("F1: 2*(precision*recall)/(precision+recall) = "+f1);
    }

    public void printDistribution(){
        Instance iUse = new DenseInstance(numOfAttributes+1);
        iUse.setDataset(isTestSet);
        // Get the likelihood of each classes
        // fDistribution[0] is the probability of being “similar”
        // fDistribution[1] is the probability of being “not_similar”
        double[] fDistribution = new double[0];
        try {
            fDistribution = cModel.distributionForInstance(iUse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("the distribution for each cluster:");
        for (int i = 0; i < fDistribution.length; i++) {
            System.out.print(fDistribution[i] + " ");
            System.out.println();
        }
    }

    //returns the probability to be similar - not static
    public Double getProbabiltyOfSimilarity(double[] features){
        Instances checkData = new Instances("Rel", fvWekaAttributes, 1);
        checkData.setClassIndex(numOfAttributes);
        Instance instanceLine = new DenseInstance(numOfAttributes+1);
        for (int k=0; k<numOfAttributes; k++){
            instanceLine.setValue((Attribute)fvWekaAttributes.elementAt(k), features[k]);
        }
        checkData.add(instanceLine);
        Double result = new Double(-1);
        try {
            result = cModel.distributionForInstance(checkData.instance(0))[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return helperFunctions.SameNumber(result,1);
    }

    public void serializeBuildClassifier(String path){
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.writeObject(cModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unSerializeBuildClassifier(String path){
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //choose classifier
//            cModel = (Logistic) ois.readObject();
            cModel = (NaiveBayes) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] getCoefficients(){
        double[][] fromWekaCoefficients = ((Logistic)cModel).coefficients();
        double[] result = new double[numOfAttributes];
        for (int i=0; i<numOfAttributes; i++){
            result[i] = fromWekaCoefficients[i+1][0];
        }
        return result;
    }

//    /* only for Logistic
    public void printCoeffisiontsLogistic(){
        //the value in cell 0 is the bias!
        final PrettyPrinterOfMatrix printer = new PrettyPrinterOfMatrix(System.out);
        String[][] doPrint = new String[((Logistic)cModel).coefficients().length][((Logistic)cModel).coefficients()[1].length];
        for (int i=0; i<doPrint.length; i++){
            for (int j=0; j<doPrint[0].length; j++){
                doPrint[i][j] = String.valueOf(((Logistic)cModel).coefficients()[i][j]);
            }
        }
        printer.print(doPrint);
    }
//    */

    public static void main(String[] argv){
        BuildClassifier buildedCalssifier = new BuildClassifier(argv[0],argv[1]);
        //buildedCalssifier.serializeBuildClassifier(argv[2]);    //only if we want to serialize!


        //BuildClassifier unserilizedClassifier = new BuildClassifier();
        //unserilizedClassifier.unSerializeBuildClassifier(argv[2]);
        //unserilizedClassifier.BuildTrainingSet(argv[1],false);

//        buildedCalssifier.printForEachInstance();
        //unserilizedClassifier.printForEachInstance();

        buildedCalssifier.printEvaluationOfClassifer();
        //unserilizedClassifier.printEvaluationOfClassifer();

        buildedCalssifier.printDistribution();
       //unserilizedClassifier.printDistribution();

        /* for logistic only
        buildedCalssifier.printCoeffisiontsLogistic();
        //unserilizedClassifier.printCoeffisiontsLogistic();
        */

    }

}
