
public class euclidian implements Metric<double[]> {

	
	public double calcDistance(double[] p1, double[] p2) {
		int len = p1.length;
		double sum = 0;
		
		for (int i=0;i<len;i++){
			sum += Math.pow((p1[i] - p2[i]),2);			
		}		
		sum = Math.sqrt(sum);	
		return sum;
	}



}


