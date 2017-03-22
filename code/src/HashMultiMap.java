import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

// maps a metric type to an array of formats the metric can accept
public class HashMultiMap {
	private HashMap<String, ArrayList<String>> metricToFormat = new HashMap<String, ArrayList<String>>();
	public HashMultiMap(String filePath){
		final String REGEX_DATABASE = "[a-zA-Z0-9]+";
		final Pattern pattern1  = Pattern.compile(REGEX_DATABASE);
		try {
			Scanner s = new Scanner(new FileReader(filePath));
			while (s.hasNextLine()){
				String str;
				String metric = s.findInLine(pattern1);
				ArrayList<String> formats = new ArrayList<String>();
				while((str=s.findInLine(pattern1))!=null) {
					formats.add(str);
				}
				
				if (metric != null){
					metricToFormat.put(metric, formats);
				}

				if (s.hasNextLine()) {s.nextLine();}
			}
			s.close();
			} catch (FileNotFoundException e) {
			System.out.println("asdasdasd");
			e.printStackTrace();
			}
		
		}
	
	public boolean validateMetric (String metric) {
		return (this.metricToFormat.containsKey(metric));
	}
	
	public boolean validateFormat (String metric, String format) {
		if (validateMetric(metric)){
			if (this.metricToFormat.get(metric).contains(format)){
				return true;
			}
			return false;
		}
		return true;
	}

	public boolean validateMetricFormat (String metric, String format) {
		return validateMetric(metric) && (this.metricToFormat.get(metric).contains(format));
	}
	
	public void printMap(){
		
		System.err.println("");
		System.err.println("Please use the following metrics and the formats they support: \n");
		for (Map.Entry<String,  ArrayList<String>> entry : metricToFormat.entrySet()) {
			ArrayList<String> val = entry.getValue();
			System.err.println("Mertic type: " + entry.getKey());
			System.err.print("formats: ");
			
			for (int i=0;i<val.size();i++){
				System.err.print(val.get(i));
				if (i<val.size()-1){
					System.err.print(", ");
				}
			}
			System.err.println("\n");
		}
		
	}
	

	
}