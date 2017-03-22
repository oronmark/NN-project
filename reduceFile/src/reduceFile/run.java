package reduceFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class run {

	
	public static void main(String[] args) {
		
	

		/**
		 * args[0] : <full-file-name> : file name and path to reduce
		 * args[1] : -n <number> : number of items to keep, default - keeps all items
		 * args[2] : -o <file-name> : output file name , default - out.csv
		 * no order of optional arguments required
		 */
					
		String inputFilePath = args[0];
		int numberOfItems = -1;
		String outputFilePath = null;
		int lineNumber = 0;
		List<String> lines = new ArrayList<String>();
		List<String> newLines = new ArrayList<String>();
		Random random = new Random();


		
		
		for (int i=1; i<args.length;i++){
			
			if (args[i].equals("-n")){
				numberOfItems = Integer.parseInt(args[i+1]);
			}
			if (args[i].equals("-o")){
				outputFilePath = args[i+1];
			}
		}
		
		if (outputFilePath == null){
			outputFilePath = "out.csv";
		}
		
		if (numberOfItems == 0){
			System.out.println("Number of items requested is 0, aborting.");
			return;
		}
		
		
		 FileReader fileReader;
		try {
			 fileReader = new FileReader(inputFilePath);
			
			 BufferedReader br = new BufferedReader(fileReader);
			 String line = null;
			 while ((line = br.readLine()) != null) {
			      lines.add(line);
			      lineNumber++;
			 }
			 
			 br.close();
			 
			 if (numberOfItems>lineNumber){
				 numberOfItems = lineNumber;
			 }
			 
		} catch (FileNotFoundException e) {
			System.out.println("Error: Invalid input file- " + inputFilePath);
			return;	
		} catch (IOException e) {
			System.out.println("Error: Invalid input file- " + inputFilePath);
			return;	
		}

		for (int i=0; i < numberOfItems; i++){
			int rand = anyRandomIntRange(random, 0, lines.size());
			newLines.add(lines.get(rand));
			lines.remove(rand);
		}	
		
		FileWriter fw = null;
		BufferedWriter bw = null;
				

			try {
				fw = new FileWriter(outputFilePath);
				bw = new BufferedWriter(fw);
				
				for (int i = 0; i < newLines.size(); i++){
					bw.write(newLines.get(i));
					bw.write("\n");
				}
				bw.close();
				
			} catch (IOException e) {
				System.out.println("Error: Invalid output file.");
				return;
			}
		
	
	}
	
	
	
	public static int anyRandomIntRange(Random random, int low, int high) {
		int randomInt = random.nextInt(high) + low;
		return randomInt;
	}
	
	
	
	
	
	
	
	
	
}
