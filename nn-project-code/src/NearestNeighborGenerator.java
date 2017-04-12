import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class NearestNeighborGenerator {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		/**
		 * args[0] : path of the training set
		 * args[1] : path of the test set
		 * args[2] : type of data
		 * args[3] : metric type
		 * args[4] : delta
		 * 
		 * not mendatory:
		 * -s <scale> : scale user input (if user scale was not provided, the scale will be calculated)
		 * -d <divisor> : divisor user input (default - 2)
		 * -l <lebelNumber> : number of labels to consider when assigning lebels to gamma net points 
		 * -p <penalty> : which penalty formula will be used, defualt = 1
		 */
		
		
		final String trainingSetFilePath = args[0];
		final String testSetFilePath = args[1];
		final String dataType = args[2];
		final String metricType = args[3];
		final String deltaString = args[4];
		double divisor = 2;
	    boolean isUserScale = false;
		double userScale = 0;
		int labelsToConsider = 0;
		boolean isLimitLabels = false;
		int penaltyType = 1;
		int penaltyOptions = 2;
		
		if (args.length<5){
			
			System.out.println("Error: incorrect number of mandatory arguments");
			return;
		}
		
		for (int i=5;i<args.length;i++){
			
			if (args[i].equals("-s")){
				
				try{
					userScale = Double.parseDouble(args[i+1]);
					isUserScale = true;
				}
				catch(NumberFormatException nfe){
					System.out.println("Error: scale input is not of type double");
					return;
					
				}
			}
			if (args[i].equals("-d")){
				try{
					divisor = Double.parseDouble(args[i+1]);
					if (divisor<=1){
						System.out.println("Error: divisor must be larger then 1");
						return;
					}
				}
				catch(NumberFormatException nfe){
					System.out.println("Error: divisor input is not of type double");
					return;
				}
			}
			if (args[i].equals("-l")){
				try{
					labelsToConsider = Integer.parseInt(args[i+1]);
					isLimitLabels = true;
					if (labelsToConsider<=0){
						System.out.println("Error: number of lebels to consider must be greater then 0");
						return;
					}
				}
				catch(NumberFormatException nfe){
					System.out.println("Error: number of labels to concider is not of type Integer");
					return;
				}
			}
			
			if (args[i].equals("-p")){
				try{
					penaltyType = Integer.parseInt(args[i+1]);
					if (penaltyType<penaltyOptions || penaltyType>penaltyOptions){
						System.out.println("Error: The penalty type you entered does not exist");
						return;
					}
				}
				catch(NumberFormatException nfe){
					System.out.println("Error: penalty type is not of type Integer");
					return;
				}
			}
			
		}
		
		if (args.length<5){
			System.out.println("Error: Incorrect number of mandatory arguments");
			return;
		}

		
		final double delta;
		try{
			delta = Double.parseDouble(deltaString);
		}
		catch(NumberFormatException nfe){
			System.out.println("Error: delta input is not of type double");
			return;
			
		}
		
	    HashMultiMap map = null;
		try{	
			map = new HashMultiMap("METRIC_TO_FORMAT.txt");
		}
		catch(FileNotFoundException e){
			System.out.println("Error: Please palce to file METRIC_TO_FORMAT.txt in the folder nn-project-code");
			return;
			
		}
		
		
		DataBase db = makeDataBase(trainingSetFilePath, isUserScale, userScale, map, dataType, metricType, delta, divisor,
									isLimitLabels, labelsToConsider, penaltyType);
		if (db == null){
			return;
		}
		
			
		try{
			db.makeTestSet(testSetFilePath);
		}
		catch(FileNotFoundException e){
			System.out.println("Error: Invalid test set file.");
			return;
		}
		
		double[] ans = db.clasifyTestSet();
		double errorRate = db.calcClassifierError(db.testSetLabels, ans) * 100;
		
		ConfusionMatrix cm = new ConfusionMatrix(ans, db.testSetLabels);
	

		BufferedWriter bw = openOutputFile();
		if (bw == null){
			System.out.println("Error: Invalid output file.");
			return;
		}
		
		System.out.println("");
		System.out.println("Stats for chosen gamma net- ");
		bw.write("Scale, " + db.getScale() + "\n");
		bw.write("Error on test set, " + errorRate + "%" + "\n" );
		bw.write("Error on training set, " + db.getErrorOnTrainingSet() + "\n" );
		bw.write("Scale penalty, " + db.getPenaltyOfOptimalScale() + "\n");
		bw.write("Training set size, " + db.getTrainingSetSize() + "\n");
		bw.write("Gamma net size, " + db.getGammaNetSize() + "\n");
		bw.write("Test set size, " + db.getTestSetSize() + "\n");
		
		if (isLimitLabels){
			bw.write("Labels to consider when assignning labels to gamma net, " + labelsToConsider + "\n");
		}
		else{
			bw.write("Labels to consider when assignning labels to gamma net, " + "all" + "\n");
		}
		
		bw.write("\n");
		bw.write("\n");
		cm.exportToFile(bw);
		bw.close();
	
		System.out.println("done.");
		
		
	}


	public static BufferedWriter openOutputFile(){
		
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		return bw;
				
	}
	
	
	public static DataBase makeDataBase(String trainingSetFilePath, boolean isUserScale, double userScale, HashMultiMap map, String dataType, 
			                            String metricType, double delta, double divisor, boolean isLimitLabels, int labelsToConsider, int penaltyType){
		
		DataBase db = null;
		
		if (!map.validateMetric(metricType)){
			System.err.println("Error: Unknown metric.");
			map.printMap();
			return null;
		}
		if (!map.validateFormat(metricType, dataType)){
			System.err.println("Error: Unknown format.");
			map.printMap();
			return null;
		}
		
		if (map.validateMetricFormat(metricType, dataType)){
			switch (dataType){
				case "vector1" :
				try {
					db = new DataBaseVector1(trainingSetFilePath, metricType, delta, isUserScale, userScale, divisor , isLimitLabels, labelsToConsider, penaltyType);
				} catch (FileNotFoundException e) {
					System.err.println("Error: Training set file " + trainingSetFilePath + " not found");
					return null;
				}					
					break;
				case "MNIST" :
				try {
					db = new DataBaseMnist(trainingSetFilePath, metricType, delta, isUserScale, userScale, divisor ,  isLimitLabels, labelsToConsider, penaltyType);
				} catch (FileNotFoundException e) {
					System.err.println("Error: Training set file " + trainingSetFilePath + " not found");
					return null;
				}
					break;
				default:
		             //throw new IllegalArgumentException("Invalid data type: " + dataType);
					System.err.println("Error: Unknown fomrmat.");
					return null;
			}
		}
		else {
			System.err.println("Error: The metric \"" + metricType + "\" does not support the format \"" + dataType + "\"");
			map.printMap();
			return null;
		}
		
		return db;
	}
	
	
	
	
}
