import java.util.ArrayList;
import java.util.regex.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import java.lang.String;

public class PageCleaner {

	// fill this out!
	public static String wikiDirectory = "";
	public static String keywordDirectory = "";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static boolean stringIsGood(String thisString){
		String functionWords[] = {"be", "is", "did", "able", "to", "can", "could", "dare", "had", "have", "may", "might", 
				"must", "need", "ought", "shall", "should", "used", "will", "would", "accordingly", "after", "at", "was",
				"albeit", "although", "and", "as", "because", "before", "both", "but", "consequently", "either", "with",
				"for", "hence", "however", "if", "neither", "nevertheless", "of", "in", "nor", "once", "or", "since", "by", "are",
				"so", "not", "out", "than", "that", "then", "thence", "therefore", "tho", "though", "thus", "till", "from",
				"unless", "until", "when", "whenever", "where", "whereas", "wherever", "whether", "while", "whilst", "on", "were",
				"yet", "a", "all", "an", "another", "any", "both", "each", "either", "every", "her", "his", "its", "my", "neither", 
				"no", "other", "our", "per", "some", "the", "their", "these", "this", "those", "whatever", "whichever", "your",
				"all", "another", "any", "anybody", "anyone", "anything", "both", "each", "either", "everybody", "everyone", 
				"everything", "few", "he", "her", "hers", "herself", "him", "himself", "his", "I", "it", "itself", "many", "into",
				"me", "mine", "myself", "neither", "nobody", "none", "nothing", "one", "other", "ours", "ourselves", "several", 
				"she", "some", "somebody", "someone", "something", "such", "theirs", "them", "themselves", "these", "they", "us",
				"we", "what", "which", "whichever", "you", "whose", "who", "whom", "whomever", "whose", "yours", "yourself", "yourselves",
				"all", "another", "any", "both", "certain", "each", "either", "enough", "few", "fewer", "less", "little", "loads", "lots",
				"many", "more", "most", "much", "neither", "none", "part", "plenty", "quantities", "several", "some",
				"various", "has", "also", " ", ""};
		for(int i = 0; i < functionWords.length; i++){
			if(thisString.equalsIgnoreCase(functionWords[i])) return false;
			if(thisString.length() == 1) return false;
		}
		return thisString.matches("[a-zA-Z0" + "-9]*");
	}

	public static ArrayList<String> getKeyWordList(File thisDir){
		if(!thisDir.isDirectory()){
			return null;
		}
		File thisChild;
		String thisContent;
		String[] splits;
		ArrayList<String> theseKeywords = new ArrayList<String>();
		for(int i = 0; i < thisDir.listFiles().length; i++){
			thisChild = thisDir.listFiles()[i];
			try {
				thisContent = FileUtils.readFileToString(thisChild);
				splits = thisContent.split("\n");
				for(int j = 1; j < splits.length; j++){
					if(!theseKeywords.contains(splits[j])){
						theseKeywords.add(splits[j]);
					}
				}	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return theseKeywords;	
	}
	
	public static boolean[][] getAdjacencyMatrix(File thisDir){
		if(!thisDir.isDirectory()){
			return null;
		}
		File thisChild;
		int numFiles = thisDir.listFiles().length;
		boolean[][] returnMatrix = new boolean[numFiles][numFiles];
		Pattern pattern = Pattern.compile("href=\"[^\"]*");
		String content, group;
		String[] fileNames = new String[numFiles];
		
		for(int i = 0; i < numFiles; i++){
			for(int j = 0; j < numFiles; j++){
				returnMatrix[i][j] = false;
			}
		}
		
		
		for(int i = 0; i < numFiles; i++){
			fileNames[i] = thisDir.listFiles()[i].getName().substring(0, thisDir.listFiles()[i].getName().length() - 5);
		}
		
		for(int i = 0; i < numFiles; i++){
			thisChild = thisDir.listFiles()[i];
			try {
				content = FileUtils.readFileToString(thisChild);
				Matcher matcher = pattern.matcher(content);
				while(matcher.find()){
					group = matcher.group().split("/")[matcher.group().split("/").length - 1];
					for(int j = 0; j < numFiles; j ++){
						if(fileNames[j].equals(group)){
							returnMatrix[i][j] = true;
							//System.out.println(thisChild.getName() + " links to " + fileNames[j]);
						}
					}
					//System.out.println(group);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return returnMatrix;
	}

	public static float[][] computeL2MetricMatrix(int[][] wordCounts){
		float[][] returnMatrix = new float[wordCounts.length][wordCounts.length];
		//System.out.println("wordCounts.length = " + wordCounts.length + " wordCounts[0].length = " + wordCounts[0].length);
		float squareSum;
		for(int i = 0; i < wordCounts.length; i++){
			returnMatrix[i][i] = 0;
			for(int j = i+1; j < wordCounts.length; j++){
				squareSum = 0;
				for(int k = 0; k < wordCounts[j].length; k++){
					squareSum += (wordCounts[j][k] - wordCounts[i][k])*(wordCounts[j][k] - wordCounts[i][k]);
				}
				returnMatrix[i][j] = (float) Math.sqrt(squareSum);
				returnMatrix[j][i] = (float) Math.sqrt(squareSum);
			}
		}
		
		return returnMatrix;
	}
	
	public static void createKeywordFiles(File thisDir){
		File child, keywordFile;
		String keywordFileName, content, content2;
		String[] splits;
		FileWriter fstream;
		BufferedWriter out;
		Pattern pattern = Pattern.compile("<[^>]*>");
		
		for(int i = 0; i < thisDir.listFiles().length; i++){
			//wordList.add(new ArrayList());
			child = thisDir.listFiles()[i];
			keywordFileName = "/Users/ganesha/Documents/CMPS242/CMPSProject/WikiProject/KeyWordPages/" + child.getName().substring(0, child.getName().length() - 5);
			keywordFile = new File(keywordFileName);
			if(!keywordFile.exists()){
				try {
					keywordFile.createNewFile();
					fstream = new FileWriter(keywordFile);
					out = new BufferedWriter(fstream);
					out.write(child.getName() + "\n");
					content = FileUtils.readFileToString(child);
					Matcher matcher = pattern.matcher(content);
					content2 = matcher.replaceAll(" ");
					content2 = content2.replace("\n", "");
					content2 = content2.toLowerCase();
					splits = content2.split(" ");
					for(int j = 0; j < splits.length; j++){
						if(stringIsGood(splits[j])){	
							out.write(splits[j] + "\n");
						}
					}
					out.close();
					fstream.close();					
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		
	}
	
	public static float[][] normalizeDistanceMatrix(float[][] distMat){
		int matSize = distMat.length;
		float[][] normMat = new float[matSize][matSize]	;
		float max= 0;
		for(int i = 0; i < matSize; i++){
			for(int j = 0; j < matSize; j++){
				if(distMat[i][j] > max) max = distMat[i][j];
			}
		}
		for(int i = 0; i < matSize; i++){
			for(int j = 0; j < matSize; j++){
				if(distMat[i][j] >= 0) normMat[i][j] = distMat[i][j]/max;
				else normMat[i][j] = -1;
			}
		}
		
		return normMat;
	}
	
	public static float[][] addMatrices(float[][] first, float[][] second){
		float[][] result = new float[first.length][first[0].length];
		for(int i = 0; i < first.length; i++){
			for(int j = 0; j < first[0].length; j++){
				result[i][j] = first[i][j] + second[i][j];
			}
		}
		return result;
	}
	
	public static float[][] addMatrices(boolean[][] first, boolean[][] second){
		float[][] result = new float[first.length][first[0].length];
		for(int i = 0; i < first.length; i++){
			for(int j = 0; j < first[0].length; j++){
				if(first[i][j] && second[i][j]) result[i][j] = 2;
				else if(!first[i][j] && !second[i][j]) result[i][j] = 0;
				else result[i][j] = 1;
			}
		}
		return result;
	}
	
	public static float[][] transposeMatrix(float[][] thisMatrix){
		int matSize = thisMatrix.length;
		float[][] returnMat = new float[matSize][matSize];
		for(int i = 0; i < matSize; i++){
			for(int j = 0; j < matSize; j++){
				returnMat[i][j] = thisMatrix[j][i];
			}
		}
		return returnMat;
	}
	
	public static boolean[][] transposeMatrix(boolean[][] thisMatrix){
		int matSize = thisMatrix.length;
		boolean[][] returnMat = new boolean[matSize][matSize];
		for(int i = 0; i < matSize; i++){
			for(int j = 0; j < matSize; j++){
				returnMat[i][j] = thisMatrix[j][i];
			}
		}
		return returnMat;
	}
	
	
	public static ArrayList<ArrayList<Integer>> cluster(float[][] distMat, float resolution){
		ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();
		int matSize = distMat.length;
		ArrayList<Integer> currentCluster = new ArrayList<Integer>();
		int[] clusterIndex = new int[matSize];
		for(int i = 0; i < matSize; i++){
			clusterIndex[i] = -1;
		}
		/*
		for(int i = 0; i < matSize; i++){
			if(clusterIndex[i] == -1){
				clusters.add(new ArrayList<Integer>());
				clusters.get(clusters.size() - 1).add(i);
				clusterIndex[i] = clusters.size() - 1;
			}
			for(int j = i + 1; j < matSize; j++){
				if(distMat[i][j] <= resolution && distMat[i][j] > 0 && clusterIndex[j] == -1){
					clusters.get(clusterIndex[i]).add(j);
					clusterIndex[j] = clusterIndex[i];
				}
			}
		}
		*/
		for(int i = 0; i < matSize; i++){
			if(clusterIndex[i] == -1){
				clusters.add(new ArrayList<Integer>());
				clusters.get(clusters.size() - 1).add(i);
				clusterIndex[i] = clusters.size() - 1;

				currentCluster = clusters.get(clusterIndex[i]);
				for(int j = 0; j < currentCluster.size(); j++){
					for(int k = 0; k < matSize; k++){
						if(clusterIndex[k] == -1 && distMat[currentCluster.get(j)][k] <= resolution){
							currentCluster.add(k);
							clusterIndex[k] = clusterIndex[i];
						}
					}
				}
			}
		}
		
		/*
		for(int i = 0; i < clusters.size(); i++){
			System.out.println("Cluster " + i + ": " + clusters.get(i).size() + " files");
		}
		*/
		return clusters;
	}
	
	
	public static void printClusters(ArrayList<ArrayList<Integer>> thisCluster, File thisDir){
		for(int i = 0; i < thisCluster.size(); i++){
			System.out.println("\n\nCluster " + i);
			for(int j = 0; j < thisCluster.get(i).size(); j++){
				System.out.println(thisDir.listFiles()[thisCluster.get(i).get(j)].getName());
			}
		}
	}
	
	
	public static float[][] dijkstraDistanceMatrix(boolean[][] adjMat){
		int matSize = adjMat.length;
		float[][] distMat = new float[matSize][matSize];
		boolean[] visited = new boolean[matSize];
		int current = 0, counter;
		float least;
		
		for(int i = 0; i < matSize; i++){
			for(int j = 0; j < matSize; j++){
				if(i == j){
					distMat[i][j] = 0;
				}else{
					distMat[i][j] = -1;
				}
			}
		}
		
		
		for(int i = 0; i < matSize; i++){ //find distance from i to
			//System.out.println("i = " + i);
			for(int k = 0; k < matSize; k++){
				visited[k] = false;
			}
			counter = 0;
			current = i;
			while(counter < matSize){
			//for(int j = 0; j < matSize; j++){
				//if(!adjMat[i][j]) continue;
				least = matSize + 1;

				//System.out.println("current = " + current);
				for(int  k = 0; k < matSize; k++){    //calculate tentative distances between i and k 
					if(!visited[k] && adjMat[current][k] && current != k && (distMat[i][k] < 0 || distMat[i][k] > distMat[i][current] + 1)){
						distMat[i][k] = distMat[i][current] + 1;
						//System.out.println("distMat[" + i + "][" + k + "] = " + distMat[i][k]);
					}
				}
				

				for(int k = 0 ; k < matSize; k++){    // find nearest neighbor;
					if(distMat[i][k] < least  && !visited[k] && distMat[i][k] > 0){
						least = distMat[i][k];
						current = k;
					}
				}
				visited[current] = true;

				counter++;
			}
			
		}
		
		return distMat;
	}
	
	public static void printMatrix(float[][] thisMat){
		for(int i = 0; i < thisMat.length; i++){
			for(int j = 0; j < thisMat[i].length; j++){
				if(thisMat[i][j] < 0) System.out.format("%08f ", thisMat[i][j]);
				else System.out.format(" %08f ",thisMat[i][j]);
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}
	
	public static void printMatrix(boolean[][] thisMat){
		for(int i = 0; i < thisMat.length; i++){
			for(int j = 0; j < thisMat[i].length; j++){
				if(thisMat[i][j]) System.out.print(1 + " ");
				else System.out.print(0 + " ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}
	
	public static float[][] positivizeDijkstra(float[][] distMat){

		int matSize = distMat.length;
		float largest = 0;
		float[][] pos = new float[matSize][matSize];
		for(int i = 0; i < matSize; i++)
		{
			for(int j = 0; j < matSize; j++){
				if(distMat[i][j] > largest) largest = distMat[i][j];
			}
		}
		for(int i = 0; i < matSize; i++){
			for(int j = 0; j < matSize; j++){
				if(distMat[i][j] < 0) pos[i][j] = largest + 1;
				else pos[i][j] = distMat[i][j];
			}
		}	
		return pos;
	}
	
	// example usage
	public static void main(String[] args) throws IOException {
		File dir = new File(wikiDirectory);
		File child, dir2;
		String content;
		String splits[];
		ArrayList<String> keyWords;
		ArrayList<ArrayList<Integer>> clusters;
		int[][] wordCounter;
		float[][] L2Metric, DJMetric, totalMetric;
		boolean[][] adjacencyMatrix;
	
		if(!dir.isDirectory()){
			System.out.println("Usage: DirectoryName");
			return;
		}
		
		dir2 = new File(keywordDirectory);
		if(!dir2.exists()) dir2.mkdir();
		createKeywordFiles(dir);
		keyWords = getKeyWordList(new File(keywordDirectory));
		
		wordCounter = new int[dir.listFiles().length][keyWords.size()];
		
		//zero out wordCounter
		for(int i = 0; i < wordCounter.length; i++)
		{
			for(int j = 0; j < wordCounter[i].length; j++){
				wordCounter[i][j] = 0;
			}
		}
		
		//count keywords per file
		

		for(int i = 0; i < dir2.listFiles().length; i++)
		{
			child = dir2.listFiles()[i];
			//System.out.println(child.getName() + ":  \n\n\n");
			content = FileUtils.readFileToString(child);
			splits = content.split("\n");
			for(int j = 1; j < splits.length; j++){
				//System.out.println(splits[j]);
				(wordCounter[i][keyWords.indexOf(splits[j])])++;	
			}
		}
		
		L2Metric = computeL2MetricMatrix(wordCounter);
		L2Metric = normalizeDistanceMatrix(L2Metric);
		//printMatrix(L2Metric);
		adjacencyMatrix = getAdjacencyMatrix(dir);
		//printMatrix(addMatrices(adjacencyMatrix, transposeMatrix(adjacencyMatrix)));
		DJMetric = dijkstraDistanceMatrix(adjacencyMatrix);
		//printMatrix(DJMetric);
		//printMatrix(transposeMatrix(DJMetric));
		DJMetric = positivizeDijkstra(DJMetric);
		//printMatrix(DJMetric);
		DJMetric = addMatrices(DJMetric, transposeMatrix(DJMetric));
		//printMatrix(DJMetric);
		DJMetric = normalizeDistanceMatrix(DJMetric);
		//printMatrix(DJMetric);
		totalMetric = addMatrices(L2Metric, DJMetric);
		printMatrix(totalMetric);
		
		
		

		int lastClusterNumber = 1000;
		for(int i = 0; i < 1000; i++){
			clusters = cluster(totalMetric, (float)i/1000);
			if(clusters.size() < lastClusterNumber){
				lastClusterNumber = clusters.size();
				System.out.println("\nResolution = " + (float)i/1000);
				printClusters(clusters,dir);
			}
			if(lastClusterNumber == 1)	break;
		
		}
			
			
		//print everything
		/*
		for(int i = 0; i < wordCounter.length; i++){
			System.out.print(dir.listFiles()[i].getName() + ":\n\n");
			for(int j = 0; j < wordCounter[i].length; j++){
				if(wordCounter[i][j] >= 1) System.out.println(keyWords.get(j) + ": " + wordCounter[i][j]);
			}
		}
		
		for(int i = 0; i < L2Metric.length; i++){
			for(int j = 0; j < L2Metric[i].length; j++){
				System.out.print(L2Metric[i][j] + "  ");
			}
			System.out.println();
		}
		
		for(int i = 0; i < DJMetric.length; i++){
			for(int j = 0; j < DJMetric.length; j++){
				System.out.print(DJMetric[i][j] + " ");
			}
			System.out.println();
		}
		*/
	}

}
