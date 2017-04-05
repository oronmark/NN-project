public class MetricFactory {
   public static <T> Metric<T> getMetric(String metricType){
	   if(metricType == null){
         return null;}		
      if(metricType.equalsIgnoreCase("euclidian")){
    	 euclidian metric = new euclidian();
    	 return (Metric<T>) metric;
      }
     
      return null;
  }


}