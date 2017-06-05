package noiseMaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class run {

	
	public static void main(String[] args) {
		
		/**
		 * args[0] : <full-file-name> : file name and path to reduce
		 * args[1] : -n <number> : number of items to keep, default - keeps all items
		 * args[2] : -o <file-name> : output file name , default - out.csv
		 * no order of optional arguments required
		 */
		
		int changeLabelAmount;
		double per = 5;
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
			 
			 changeLabelAmount = (int) ((double)(lineNumber + 1) * per)/100;
			 
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

		
		//change tag for <changeLabelAmount> points and add to ouput array
		String currLine;
		String newLine;
		int newTag;
		int chosenString;
		while (changeLabelAmount != 0){
			chosenString = anyRandomIntRange(random, 0, lines.size());
			currLine = (lines.get(chosenString)); 
			newTag = anyRandomIntRange(random, 0, 9);			
			newLine =  Integer.toString(newTag) + currLine.substring(1, currLine.length());
			lines.remove(chosenString);
			newLines.add(newLine);
			changeLabelAmount--;
		}	
		
	
		while (lines.size() != 0){
			currLine = (lines.get(0)); 
			newLines.add(lines.get(0));
			lines.remove(0);
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
		
			System.out.println("done.");
	
	}
	
		
	public static int anyRandomIntRange(Random random, int low, int high) {
		int randomInt = random.nextInt(high) + low;
		return randomInt;
	}
	
	
	public static int getDim(String str){
		
		int comma = 0;
		for (int i = 0 ; i<str.length(); i++){
			if (str.charAt(i) == ',')
				comma++;
		}
		
		return comma + 1;
	}
	
}
