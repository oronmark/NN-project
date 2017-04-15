import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class DataBase<T> {
	
	/**
	 * trainingSetPoints : a collection of training set points such that dataSetPoints[i] is the i'th point
	 * trainingSetLabels : training set labels such that trainingSetLabels[i] is the label of trainingSetPoints[i]
	 * gammaNetPoints : gamma net point
	 * gammaNetPoints : gamma net labels such that gammaNetPoints[i] is the label of gammaNetPoints[i]
	 * metric : distance function 
	 * dimTrainingSetTrainingSet : dimTrainingSetension of each point (gammaNet point, trainingSet point etc)
	 * delta : a value in range [0..1], is used to calculate the penalty of some scale, input by user
	 * sizeOfTrainingSet
	 * sizeOfGammaNet
	 * scale : the scale which is used to build the gamma net
	 * globalLabelToCountArray :  globalLabelToCountArray[i] is the count of labels counted for the majority decision for gammaNetPoints[i]
	 */
	T[] testSetPoints;
	protected double[] testSetLabels;
	protected T[] trainingSetPoints;
	protected double[] trainingSetLabels;
	protected ArrayList<T> gammaNetPoints;
	protected double[] gammaNetLabels;
	protected int[] cellSize; //cellSize[i] is the number of points in gammaNetPoints[i]'s cell
	Metric<T> metric;
	int dimTrainingSet;
	int dimTestSet;
	int sizeOfTrainingSet;
	int sizeOfTestSet;
	int sizeOfGammaNet;
	double scale;
	double delta;
	boolean isUserScale;
	double userScale;
	double minPenaltyForOptimalScale;
	double errorOnTrainingSet;
	double divisor;
	boolean isLimitLabels = false;
	int labelsToConsider = 0;
	int penaltyType;
	int diffLabelsInTrainingSet;
	
	
	public DataBase (String trainingSetFilePath, String metricType, double _delta, boolean _isUserScale, double _userScale, double _divisor ,
			         boolean _isLimitLabels, int _labelsToConsider, int _penaltyType) throws FileNotFoundException{		
	   /**
	   * This method is the constructor for dataBase
	   * @param trainingSetFilePath : The path to the input file with the training sample information
	   * @param metricType : Metric type
	   * @param format : The format of the input file
	   * @param _delta : a Value between 0 and 1, this is a user input
	   */
		isLimitLabels = _isLimitLabels;
		labelsToConsider = _labelsToConsider;
		divisor = _divisor;
		isUserScale = _isUserScale;
		userScale = _userScale;
		this.delta = _delta; 
		penaltyType = _penaltyType;		
		makeTrainingSet(trainingSetFilePath);	
		diffLabelsInTrainingSet = calcDiffLabelCount();
		shuffleTrainSet();
		makeMetric(metricType);
		makeClassifier();

	}
	
	public int getTrainingSetSize(){
		return sizeOfTrainingSet;
	}
	
	public int getTestSetSize(){
		return sizeOfTestSet;
	}
		
	public double getPenaltyOfOptimalScale(){
		return minPenaltyForOptimalScale;
	}
	
	public double getErrorOnTrainingSet(){
		return errorOnTrainingSet;		
	}
	
	public double getScale(){
		return scale;		
	}
	
	public int getGammaNetSize(){
		return sizeOfGammaNet;
	}
	
	  
	private void shuffleTrainSet() {
	 Random rnd = ThreadLocalRandom.current();
	    for (int i = trainingSetPoints.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      T currPoint = trainingSetPoints[index];
	      double currLabel = trainingSetLabels[index];
	      trainingSetPoints[index] = trainingSetPoints[i];
	      trainingSetLabels[index] = trainingSetLabels[i];
	      trainingSetLabels[i] = currLabel;
	      trainingSetPoints[i] = currPoint;
	    }
				
	}


	double[] classify(T[] dataSetPoints , int dataSetSize){
		
		/**
		 * This method calculates labels to an array of unlabeled points using the gammaNet class member and scale
		 * @param dataSetPoints : a collection of points such that dataSetPoints[i] is the i'th point
		 * @param dataSetSize : number of point is dataSetPoints
		 * @return double[] such that the i'th point is the returned array is the calculated label for the point dataSetPoints[i]
		 */
		
		double[] dataSetLabels = new double[dataSetSize];
		double minDistance;
		int minIndex;
		double currDistance;
		for (int i = 1; i < dataSetSize; i++){
			minDistance = metric.calcDistance(dataSetPoints[i], gammaNetPoints.get(0));
			minIndex = 0;
			for (int j = 0; j < sizeOfGammaNet; j++){
				currDistance = metric.calcDistance(dataSetPoints[i], gammaNetPoints.get(j));
				if (currDistance<minDistance){
					minDistance = currDistance;
					minIndex = j;
				}
			}
			
			dataSetLabels[i] = gammaNetLabels[minIndex];
		}
	return dataSetLabels;
	}

	
	double calcClassifierLooError(double[] originalLabels, double[] assignedLabels){
		
 		/**
		 * This method calculates the looError of the current classifier (current gamma net) on the classified training set points.
		 * @param originalLabels, the original labels for the classified data set (in this case the data set is always trainingSetPoints),
		 * meaning originalLabels is always trainingSetLabels.
		 * @param originalLabels, the assigned labels for trainingSetPoints.
		 */

		double dis;
		int closestGammaPointIndex;
		double error = 0;
		int indexInGammaNet;
		for (int i = 0; i < trainingSetPoints.length; i++){
			if (((indexInGammaNet=checkIfGammaNetElem(trainingSetPoints[i])) == -1) || cellSize[indexInGammaNet] == 1){
				if (originalLabels[i] != assignedLabels[i]){
					error++;
				}
			}
			else{ //special case for gamma net points with cellSize = 1
				
				dis = metric.calcDistance(gammaNetPoints.get(indexInGammaNet), gammaNetPoints.get(0));
				closestGammaPointIndex = 0;
				for (int j = 0 ; j<gammaNetPoints.size() ; j++){
					if (metric.calcDistance(gammaNetPoints.get(indexInGammaNet), gammaNetPoints.get(j))<dis){
						dis = metric.calcDistance(gammaNetPoints.get(indexInGammaNet), gammaNetPoints.get(j));
						closestGammaPointIndex = j;
					}
				}
				
				
				if (assignedLabels[i] != gammaNetLabels[closestGammaPointIndex]){
					error++;
				}				
			}			
		}		
		error = error / originalLabels.length;		
		return error;
	}
	
	
	double calcPenalty1(double currScale, double currMinPenalty){

 		/**
		 * This method calculates the penalty (type 1) of some scale (and the gamma net created using that scale).
		 * @param currScale, the scale being tested in makeClassifier, (not necessarily the optimal).
		 * @param currMinPenalty, the lowest penalty found for the current iteration of makeClassifier.
		 * the scale used to achieve this penalty is the class member scale.  
		 */
		
		double alpha;
		double arg1;
		double arg2;
		double pen;
		double[] assignedPointsToTrainingSetByGammaNet = new double[sizeOfTrainingSet];
		double epsilon;
		
		if (sizeOfGammaNet == sizeOfTrainingSet)
			return Double.MAX_VALUE;
		
		assignedPointsToTrainingSetByGammaNet = classify(trainingSetPoints, sizeOfTrainingSet);
		epsilon = calcClassifierError(trainingSetLabels,assignedPointsToTrainingSetByGammaNet);		
		alpha = ((double)sizeOfTrainingSet)/(sizeOfTrainingSet-sizeOfGammaNet);
		
		arg1 = alpha * epsilon;
		arg2 = (((sizeOfGammaNet + 1)*Math.log(sizeOfTrainingSet*diffLabelsInTrainingSet) + Math.log(1/delta))/(sizeOfTrainingSet - sizeOfGammaNet));
		pen = arg1 + (2/3)*arg2 + (3/Math.sqrt(2))*Math.sqrt(arg1*arg2);
		
        System.err.println("currScale = " + currScale + " ,alpha = " + alpha + ", epsilon = " + epsilon + ", gammaNetSize = " + sizeOfGammaNet + ", "
        					+ "trainingSetSize = " + sizeOfTrainingSet + ", delta = " + delta + ", diffLabels = "+ diffLabelsInTrainingSet + ""
        							+ " arg1 = " + arg1 + ", arg2 = " + arg2 + ", pen = " + pen + "\n");
        
    	System.out.println("current scale, " + currScale);
    	System.out.println("error on training set , " + epsilon);
    	System.out.println("gamma net size , " + sizeOfGammaNet);
    	System.out.println("training set size , " + sizeOfTrainingSet);
    	System.out.println("alpha , " + alpha);
    	System.out.println("penalty for current scale , " + pen + "\n");
        
        
		if (arg2>currMinPenalty)
			return -1;
        
		return pen;
	}
	
	
	double calcPenalty2(double currScale){	
		
 		/**
		 * This method calculates the penalty (type 2) of some scale (and the gamma net created using that scale).
		 * @param currScale, the scale what was used to make the current gamma net.
		 */
	
		double[] assignedPointsToTrainingSetByGammaNet = new double[sizeOfTrainingSet];
		double looError;
		
		assignedPointsToTrainingSetByGammaNet = classify(trainingSetPoints, sizeOfTrainingSet);
		looError = calcClassifierLooError(trainingSetLabels, assignedPointsToTrainingSetByGammaNet);
		
        System.err.println("currScale = " + currScale + ", gammaNetSize = " + sizeOfGammaNet + ", "
				+ "trainingSetSize = " + sizeOfTrainingSet + ", looError = " + looError + "\n");
        
    	System.out.println("current scale, " + currScale);
    	System.out.println("error on training set (looError) , " + looError);
    	System.out.println("gamma net size , " + sizeOfGammaNet);
    	System.out.println("training set size , " + sizeOfTrainingSet);
    	System.out.println("penalty for current scale , " + looError + "\n");

				
		return looError;
	}
	

	double calcPenalty(double currScale, double currMinPenalty){				
 		/**
		 * This method calculates the penalty of some scale (and the gamma net created using that scale) on the training set.
		 * the penalty type is according to the class member penaltyType.
		 * @param currScale, the scale being tested in makeClassifier, (not necessarily the optimal).
		 * @param currMinPenalty, the lowest penalty found for the current iteration of makeClassifier.
		 * the scale used to achieve this penalty is the class member scale.  
		 */

				
		double pen;		
		switch (penaltyType){
		
		   case 1: pen = calcPenalty1(currScale, currMinPenalty);
		   		   break;
		   default : pen = calcPenalty2(currScale);
		   		   break;		
		}		
		return pen;				
	}
		
	
	
	int checkIfGammaNetElem(T element){
 		/**
		 * This method checks whether element is one of the elements in the currnent gammaNet. 
		 */
		for (int i = 0; i<gammaNetPoints.size(); i++){
			if (element == gammaNetPoints.get(i))  //compares addresses 
				return i;
		}		
		return -1;
	}




	public void makeClassifier(){
		double maxDistance = getMaxDistance();
		double currScale = maxDistance * 2;
		double minPenalty = Double.MAX_VALUE;
		double currPenalty;
		double[] assignedPointsToTrainingSetByGammaNet = new double[sizeOfTrainingSet];
		double alpha;
		int iter=0;
		
		
		if (isUserScale){
			scale = userScale;
			makeGammaNet(scale);
			assignedPointsToTrainingSetByGammaNet = classify(trainingSetPoints, sizeOfTrainingSet);
			errorOnTrainingSet = calcClassifierError(trainingSetLabels,assignedPointsToTrainingSetByGammaNet);
			alpha = ((double)sizeOfTrainingSet)/(sizeOfTrainingSet-sizeOfGammaNet);
		//	System.err.println("alpha = " + alpha+ " sizeOfTrainingSet = " + sizeOfTrainingSet + " sizeOfGammaNet = " + sizeOfGammaNet); 
			minPenaltyForOptimalScale = calcPenalty(scale, minPenalty);	
			return;
		}
		
		System.out.println("Scale attempts-\n");
		
		
		scale = currScale;
		while (sizeOfGammaNet != sizeOfTrainingSet ){
		    
			System.out.println(iter + ":");
			makeGammaNet(currScale);							
			currPenalty = calcPenalty(currScale, minPenalty);
			if (currPenalty == -1)
				break;
						
			if (currPenalty < minPenalty){
				minPenalty = currPenalty;
				scale = currScale;
			}
			
			currScale /= divisor;
			iter  += 1;
			
			
	//		System.out.println(iter + ":");
	//		System.out.println("current scale, " + currScale);
	//		System.out.println("error on training set , " + epsilon);
	//		System.out.println("gamma net size , " + sizeOfGammaNet);
	//		System.out.println("penalty for current scale , " + currPenalty + "\n");
			
						
		}
	
		makeGammaNet(scale);
		assignedPointsToTrainingSetByGammaNet = classify(trainingSetPoints, sizeOfTrainingSet);
		errorOnTrainingSet = calcClassifierError(trainingSetLabels,assignedPointsToTrainingSetByGammaNet);
		alpha = ((double)sizeOfTrainingSet)/(sizeOfTrainingSet-sizeOfGammaNet);
	//	System.err.println("alpha = " + alpha+ " sizeOfTrainingSet = " + sizeOfTrainingSet + " sizeOfGammaNet = " + sizeOfGammaNet); 
		minPenaltyForOptimalScale = minPenalty;
		
		
	}
	

	double calcClassifierError(double[] originalLabels, double[] assignedLabels){
		
		/**
		 * This method calculates the error of the classifier
		 * @param  dataSetPoints : points that were labeled using the classifier (the gamma net)
		 * @param originalLbels : the labels for each point in dataSetPoints such that the i'th label is the label of dataSetPoints[i]
		 * (this labels are the true labels for each point, they are not calculated, rather they are given as user input
		 * @param assignedLabels : the labels that were assigned by the classifier to the point in dataSetPoints.
		 * the i'th label in assignedLabels is the calculated label for the point dataSetPoints[i]
		 * @param size : number of points in dataSetPoints
		 * @return double, the error of the labels in assignedLabels on the points in dataSetPoints
		 * the formula for the error is - sum of I[originalLbels[i]!=assignedLabels[i]]\size, 0<=i<=size 
		 */
		int size = originalLabels.length;
		double error = 0;
		for (int i = 0; i < size; i++){
			if (originalLabels[i] != assignedLabels[i]){
				error++;
			}			
		}
		error = error / size;
		
		return error;
	}
	

	
	double getMaxDistance(){
		
		/**
		 * @return double, The maximal distance (according to metric) between trainingSetPoints[0] and any other point trainingSetPoints[i]
		 * in trainingSetPoints, 0<=i<=sizeOfTrainingSet
		 */
		
		double max = 0;
		for (int i=0; i < sizeOfTrainingSet; i++){
			double currDistance = metric.calcDistance(trainingSetPoints[0], trainingSetPoints[i]);
			if (max < currDistance) {
				max = currDistance;
				}
		}
		return max;
	}
	
	int calcDiffLabelCount(){		
		/**
		 * @return int , number of different labels used to label the points in trainingSetPoints
		 */		
		Map<Double,Boolean> lableToCount = new HashMap<Double,Boolean>();
		for (int i=0;i<sizeOfTrainingSet;i++){
			double currLabel = trainingSetLabels[i];
			if (!lableToCount.containsKey(currLabel)){
				lableToCount.put(currLabel, true);
			}
		}
	return lableToCount.size();		
	}
	
	public void makeMetric(String metricType){	
		
		/**
		 * This method creates an instance of the required metric based on user input
		 * @param metricType : the name of the required metric
		 */
		
		metric = MetricFactory.getMetric(metricType);
	}
	

	public abstract void makeTrainingSet(String trainingSetFilePath) throws FileNotFoundException;
	public abstract void makeTestSet(String testSetFilePath) throws FileNotFoundException;
	
	public double[] clasifyTestSet(){
		return classify(testSetPoints, sizeOfTestSet);
	}
	
	public enum data {
	    TRAINING_SET,GAMMA_NET
	}	
	
	
	public double getDistance(int i, int j, data db){

		/**
		 * This method calculates the distance between point i and point j according to the class member metric
		 * @param i : index of the first point
		 * @param j : index of the second point
		 * @param db : an enum with the value TRAINING_SET or GAMMA_NET
		 * used to differentiate between the data base with the requested points 
		 */
		
		T[] currDB = null;
		switch (db){
		case TRAINING_SET:
			currDB = trainingSetPoints;
			break;
		case GAMMA_NET:
			currDB = (T[]) gammaNetPoints.toArray();
			break;
		default:
			System.out.println("wrong db choise");
		}
		
		if (trainingSetPoints == null){
			System.out.println("wrong db choise");
			return -1;
		}
		return metric.calcDistance(currDB[i], currDB[j]);

	}
	
	
	public void makeGammaNet(double scale){
		
	/**
	 * This method creates a gamma net according to the training set and scale
	 * after using this method:
	 * The class member gammaNetPoints will point to an array of points which are the gamma net elements
	 * The class member gammaNetLabels will point to an array of labels which are the gamma net labels
	 * The class member sizeOfGammaNet will indicate the number of elements in the gamma net created
	 * @param scale
	 * information regarding the method variables:
	 * tmpPoint : temporary array for the gamma net elements
	 * tmpLabels : labels for tmpPoins elements
	 * labelToCountArray: labelToCountArray[i] is the mapping of all the labels in tmpPoint[i]'s cell to there count 
	 * countOfTotalLabels: countOfTotalLabels[i] is the count of labels counted for the majority decision for tmpPoint[i]
	 * restPoints : temporary array for all of the elements of the training set
	 * restLabels: labels for restPoint elements
	 * 
	*/
		ArrayList<T> tmpPoints = new ArrayList<T>();
		ArrayList<Double> tmpLables = new ArrayList<Double>(); //extension of tmpPoints
		ArrayList<T> restPoints = new ArrayList<T>();
		ArrayList<Double> restLables = new ArrayList<Double>();
		boolean gammaElem = true;
		Map<Double,Integer> lableToCount;
		ArrayList<Map<Double,Integer>> labelToCountArray = new ArrayList<Map<Double,Integer>>(); //extension of tmpPoints
//		ArrayList<Map<Double,Integer>> globalGabelToCountArray = new ArrayList<Map<Double,Integer>>(); //extension of tmpPoints
		ArrayList<Integer> countOfTotalLabels = new ArrayList<Integer>(); //extension of tmpPoints
		double minDistanceFromGammaPoint;
		double currDistance;
		int minDistanceFromGammaPointIndex;
		sizeOfGammaNet = 0;
		int currCount;
		//sets which points are in the gamma net
		for (int i = 0; i < sizeOfTrainingSet; i++){
			for (int j = 0; j < sizeOfGammaNet; j++){

				if (metric.calcDistance(trainingSetPoints[i], tmpPoints.get(j))<scale){
					gammaElem = false;
					break;
				}
			}
			
			if (gammaElem == true){
				tmpPoints.add(trainingSetPoints[i]);
				labelToCountArray.add(new HashMap<Double,Integer>());
				sizeOfGammaNet++;
			}
			
			countOfTotalLabels.add(0);
			restPoints.add(trainingSetPoints[i]);
			restLables.add(trainingSetLabels[i]);
			gammaElem = true;
			
		}
				
		//sets labels for gammaNet elements
		// resTPoints = all of the training set elements
		// tmpPoints = gammaNet elements
		//create label count for each element in the gamma net
		for (int i=0;i<restPoints.size();i++){
			
			minDistanceFromGammaPointIndex = 0;
			minDistanceFromGammaPoint = metric.calcDistance(restPoints.get(i), tmpPoints.get(0));		
			for (int j=0;j<sizeOfGammaNet;j++){
				currDistance = metric.calcDistance(restPoints.get(i), tmpPoints.get(j));
				if (currDistance<minDistanceFromGammaPoint){
					minDistanceFromGammaPointIndex = j;
					minDistanceFromGammaPoint = currDistance;
				}
			}
			
			if ( isLimitLabels == false || (isLimitLabels == true && countOfTotalLabels.get(minDistanceFromGammaPointIndex)<labelsToConsider)){		
			
				countOfTotalLabels.set(minDistanceFromGammaPointIndex, (countOfTotalLabels.get(minDistanceFromGammaPointIndex) + 1)); 
				lableToCount = labelToCountArray.get(minDistanceFromGammaPointIndex);

				if (lableToCount.containsKey(restLables.get(i))){
					currCount = lableToCount.get(restLables.get(i));				
					lableToCount.put(restLables.get(i) , ++currCount);
											
				}
				else{
					lableToCount.put(restLables.get(i) , 1);
				}				
			}
			
		
		
		}		
	
		//assign majority label for each gamma net element
		for (int i=0;i<sizeOfGammaNet;i++){
			lableToCount = labelToCountArray.get(i);
			
			Double maxLable = (Double)null;
			Integer maxCount = (Integer)null; ;

			for(Map.Entry<Double, Integer> entry : lableToCount.entrySet()) {
				maxLable = entry.getKey();
				maxCount = entry.getValue();
				break;
			}

			for (Map.Entry<Double, Integer> currEntry : lableToCount.entrySet()) {
			    if (maxCount<currEntry.getValue()){
			    	maxLable = currEntry.getKey();
			    	maxCount = currEntry.getValue();
			    }
			}
			
			tmpLables.add(maxLable);		
		}
		
	//	create gammaNet arrays
		gammaNetPoints = new ArrayList<T>(tmpPoints.size());
		gammaNetLabels = new double[tmpPoints.size()];
		cellSize = new int[tmpPoints.size()];
		for (int i=0; i < tmpPoints.size(); i++){
			gammaNetPoints.add(tmpPoints.get(i));
			if ( tmpLables.get(i) == null){
				gammaNetLabels[i] = 1;
			}
			else{
				gammaNetLabels[i] = tmpLables.get(i);
			}
			cellSize[i] = countOfTotalLabels.get(i);
			
		}	
		

		
}
	
	
///////////////////////////////////////////////////////////////
/////////////////// Prints (for debugging) ////////////////////
///////////////////////////////////////////////////////////////
	
public void printDataBase(data db){
		
	/**
	 * This method prints the points and there labels of db, for debbuging purposes
	 * @param db, an enum which indicates what db to print, the training set or the gamme net
	 */
		
	    
	    switch (db){
		
			case TRAINING_SET:
				System.out.println();
				System.out.println("training set- ");
				System.out.println("size: " + sizeOfTrainingSet);
			    System.out.println("dimTrainingSet: " + dimTrainingSet);
				 for (int i = 0; i < sizeOfTrainingSet; i++){
				 System.out.println("point " + i + " : " + Arrays.toString((double[])trainingSetPoints[i]) + ", lable: " + trainingSetLabels[i]);				 
				 }
				break;
				
			case GAMMA_NET:
				 System.out.println();
				 System.out.println("gamma net- ");
				 System.out.println("scale: " + scale);
				 System.out.println("size: " + sizeOfGammaNet);
				 System.out.println("dimTrainingSet: " + dimTrainingSet);
				 for (int i = 0; i < sizeOfGammaNet; i++){
				 System.out.println("point " + i + " : " + Arrays.toString((double[])gammaNetPoints.get(i)) + ", lable: " + gammaNetLabels[i]);				 
				 }
				break;
				
			default:
				System.out.println("wrong db choise");
			}	    

	}

public void printAllDistances(){
	
	/**
	 * This method print the distance from point i to j (for each 0<=i,j<=sizeOfTrainingSet) according to class memeber metric
	 * for debugging purposes
	 */
	
	System.out.println();
	System.out.println("Distances: ");
	for (int i=0; i<sizeOfTrainingSet;i++){
		for (int j=0; j<sizeOfTrainingSet;j++){
			System.out.print(i + " to " + j + ": ");
			System.out.println(metric.calcDistance(trainingSetPoints[i], trainingSetPoints[j]));
						
		}
		
	}
}
		
}




