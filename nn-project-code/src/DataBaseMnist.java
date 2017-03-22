import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author Daniel Geyshis
 *
 */
public class DataBaseMnist extends DataBase<double[]> {
	public DataBaseMnist(String trainingSetFilePath, String metricType, double _delta ,boolean _isUserScale, double _userScale, double _divisor) throws FileNotFoundException {
		super(trainingSetFilePath, metricType, _delta, _isUserScale ,_userScale, _divisor);
	}


	public void makeTrainingSet(String trainingSetFilePath) throws FileNotFoundException {
		final String REGEX_DATABASE = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";  //regular expression for double
		final Pattern pattern1  = Pattern.compile(REGEX_DATABASE);
		this.setTrainDimAndSize(trainingSetFilePath);
		Scanner s = new Scanner(new FileReader(trainingSetFilePath)); 
	   // sizeOfTrainingSet = sizeOfTrainingSet/60;
		trainingSetPoints = new double[sizeOfTrainingSet][dimTrainingSet];
		trainingSetLabels = new double[sizeOfTrainingSet];
		 		
		String inLine;
		int currPoint=0;
		int currAttribute=0;
		s.nextLine();
		int curr = sizeOfTrainingSet;
		/** in the while we should write s.hasNext()**/
		while (curr != 0){
			trainingSetLabels[currPoint] = Double.parseDouble(s.findInLine(pattern1)); 			 
			while((inLine=s.findInLine(pattern1))!=null) {
				trainingSetPoints[currPoint][currAttribute] = Double.parseDouble(inLine);	
				currAttribute++;
			} 
			currPoint++;
			currAttribute = 0;
			if (s.hasNextLine()) {
				s.nextLine();}
			else { break; }
			curr--;
		}
		 s.close();
	}

	@Override
	public void makeTestSet(String testSetFilePath) throws FileNotFoundException {
		final String REGEX_DATABASE = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";  //regular expression for double
		final Pattern pattern1  = Pattern.compile(REGEX_DATABASE);
		this.setTestDimAndSize(testSetFilePath);
		Scanner s = new Scanner(new FileReader(testSetFilePath)); 
	//	sizeOfTestSet = sizeOfTestSet/60;
		testSetPoints = new double[sizeOfTestSet][dimTestSet];
		testSetLabels = new double[sizeOfTestSet];
		 		
		String inLine;
		int currPoint=0;
		int currAttribute=0;
		s.nextLine();
		int curr = sizeOfTestSet;
		/** in the while we should write s.hasNext()**/
		while (curr != 0){
			testSetLabels[currPoint] = Double.parseDouble(s.findInLine(pattern1)); 			 
			while((inLine=s.findInLine(pattern1))!=null) {
				testSetPoints[currPoint][currAttribute] = Double.parseDouble(inLine);	
				currAttribute++;
			} 
			currPoint++;
			currAttribute = 0;
			if (s.hasNextLine()) {s.nextLine();}
			else { break; }
			curr--;
		}
		 s.close();
		
	}
	
	private void setTrainDimAndSize(String trainingSetFilePath) throws FileNotFoundException {
		FileReader file = new FileReader(trainingSetFilePath);
		Scanner s = new Scanner(file);
		String[] parts = s.nextLine().split(",");
		dimTrainingSet = parts.length - 1; 
		sizeOfTrainingSet = 0;
		while (s.hasNextLine()) {
		 sizeOfTrainingSet++;
		    s.nextLine();
		}
		s.close();
	} 
	
	private void setTestDimAndSize(String testSetFilePath) throws FileNotFoundException {
		FileReader file = new FileReader(testSetFilePath);
		Scanner s = new Scanner(file);
		String[] parts = s.nextLine().split(",");
		dimTestSet = parts.length - 1; 
		sizeOfTestSet = 0;
		while (s.hasNextLine()) {
		 sizeOfTestSet++;
		    s.nextLine();
		}
		s.close();
	} 

}
