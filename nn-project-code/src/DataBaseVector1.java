import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DataBaseVector1 extends DataBase<double[]> {

	public DataBaseVector1(String trainingSetFilePath, String metricType, double _delta ,boolean _isUserScale, double _userScale, double _divisor
            , boolean _isLimitLables, int _labelsToConsider, int _penaltyType) throws FileNotFoundException {
				super(trainingSetFilePath, metricType, _delta, _isUserScale ,_userScale, _divisor,_isLimitLables , _labelsToConsider, _penaltyType);
	}

	@Override
	public void makeTrainingSet(String trainingSetFilePath) throws FileNotFoundException {

		/**
		 * This method parses the input file of the training set and creates 2 arrays, 1 for the point (which the class member trainingSetPoints
		 * will point to) and 1 for the labels of the input point (which the class member trainingSetLabels points to)
		 * @param trainingSetFilePath : path of the input file with the training set points
		 * information regarding the method variables:
		 * REGEX_DATABASE : regular expression for type double
		 * pattern1 : pattern of regular expression REGEX_DATABASE to extrace information from each line
		 * sizeOfTrainingSet : size of training sample according to the input file
		 * dim : dimesion of each point according to the input file
		 */
		
	 	 final String REGEX_DATABASE = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";  //regular expression for double
		 final Pattern pattern1  = Pattern.compile(REGEX_DATABASE);
		 
		 Scanner s = new Scanner(new FileReader(trainingSetFilePath));
		 sizeOfTrainingSet = Integer.parseInt(s.findInLine(pattern1));
		 dimTrainingSet = Integer.parseInt(s.findInLine(pattern1));
		 

		 trainingSetPoints = new double[sizeOfTrainingSet][dimTrainingSet];
		 trainingSetLabels = new double[sizeOfTrainingSet];
		 		
		 String inLine;
		 int currPoint=0;
		 int currAttribute=0;
		 s.nextLine();
		 
		 while (s.hasNextLine()){
			 
			 trainingSetLabels[currPoint] = Double.parseDouble(s.findInLine(pattern1));
			 			 
			 while((inLine=s.findInLine(pattern1))!=null) {
				 trainingSetPoints[currPoint][currAttribute] = Double.parseDouble(inLine);	
				 currAttribute++;
				}
			 
			 currPoint++;
			 currAttribute=0;
			 
	
			 if (s.hasNextLine()) {s.nextLine();}
		 }
		 
		 s.close();
		
	}

	@Override
	public void makeTestSet(String testSetFilePath) throws FileNotFoundException {
		 final String REGEX_DATABASE = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";  //regular expression for double
		 final Pattern pattern1  = Pattern.compile(REGEX_DATABASE);
		 Scanner s = new Scanner(new FileReader(testSetFilePath));
		 sizeOfTestSet = Integer.parseInt(s.findInLine(pattern1));
		 dimTestSet = Integer.parseInt(s.findInLine(pattern1));
		 testSetPoints = new double[sizeOfTestSet][dimTestSet];
		 testSetLabels = new double[sizeOfTestSet];
		 String inLine;
		 int currPoint=0;
		 int currAttribute=0;
		 s.nextLine();
		 
		 while (s.hasNextLine()){
			 
			 testSetLabels[currPoint] = Double.parseDouble(s.findInLine(pattern1));
			 			 
			 while((inLine=s.findInLine(pattern1))!=null) {
				 testSetPoints[currPoint][currAttribute] = Double.parseDouble(inLine);	
				 currAttribute++;
				}
			 
			 currPoint++;
			 currAttribute=0;
			 
	
			 if (s.hasNextLine()) {s.nextLine();}
		 }
		 
		 s.close();		
	}

}
