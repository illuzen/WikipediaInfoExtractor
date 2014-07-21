package webCrawlers;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.util.*;

public class DefinitionConnector {

	// I would like to store definitionNeighborMap in a database 
	public static LinkedHashMap<String, ArrayList<String>> definitionNeighborMap = new LinkedHashMap<String, ArrayList<String>>();
	public static LinkedHashMap<String, String> rhymeNeighborMap      = new LinkedHashMap<String, String>();
	public static LinkedHashMap<String, String> thesaurusNeighborMap  = new LinkedHashMap<String, String>();
	
	// filter out function words
	public static boolean stringIsGood(String thisString){
		if(thisString.length() < 2) return false;
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
		for(int i = 0; i < functionWords.length; i++)
		{
			if(thisString.equalsIgnoreCase(functionWords[i])) return false;
		}
		return thisString.matches("[a-zA-Z0" + "-9]*");
	}
	
	// basically an endless search of english words and definitions. 
	// I think its breadth-first not positive
	public static void main (String[] args) throws Exception
	{
		String startingWord;
		if (args.length < 1)
		{
			startingWord = "start";
		}
		else
		{
			startingWord = args[0];			
		}
		getDefinitionKeywords(startingWord);
		ArrayList<String> visitedWords = new ArrayList<String>();
		visitedWords.add(startingWord);
		String currentWord = "";
		while (!visitedWords.containsAll(definitionNeighborMap.keySet()))
		{
			for(String word : definitionNeighborMap.keySet())
			{
				if(!visitedWords.contains(word))
				{
					currentWord = word;
					break;
				}
			}
			System.out.println(currentWord);
			getDefinitionKeywords(currentWord);
			visitedWords.add(currentWord);
		}
	}
	
	// collects the keywords in the definition of thisWord and stores them in a definition neighbor map
	public static ArrayList<String> getDefinitionKeywords(String thisWord) throws Exception
	{
		if (!definitionNeighborMap.containsKey(thisWord))
		{
			definitionNeighborMap.put(thisWord, new ArrayList<String>());
		}
		
		ArrayList<String> defKeywords = new ArrayList<String>();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://www.merriam-webster.com/dictionary/" + thisWord);
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		
		String htmlText = "";
		try {
		    HttpEntity entity1 = response1.getEntity();
		    htmlText = EntityUtils.toString(entity1);
		    //Integer defIdx = htmlText.indexOf("<div class=\"ld_on_collegiate\">");
		    Integer defIdx = htmlText.indexOf("<span>Full Definition of");		    
		    if (defIdx < 0) 
		    {
		    	return defKeywords;
		    }
		  
		    Integer defEndIdx = htmlText.indexOf("</div><style type=\"text/css\">", defIdx);
		    if (defEndIdx < 0)
		    {
		    	return defKeywords;
		   	}
		    //String defText = htmlText.substring(defIdx, htmlText.indexOf("</div>", defIdx));
		    String defText = htmlText.substring(defIdx, defEndIdx);
		    defText = defText.replaceAll("<[^>]*>", " ");
		    defText = defText.replaceAll("[::(),.]", "").toLowerCase();
		    String[] candidates = defText.split(" ");
		    for (int i = 0; i < candidates.length; i++)
		    {
		    	if (stringIsGood(candidates[i]) && !defKeywords.contains(candidates[i]))
		    	{
		    		defKeywords.add(candidates[i]);
		    		
		    		if(!definitionNeighborMap.get(thisWord).contains(candidates[i]))
		    		{
		    			definitionNeighborMap.get(thisWord).add(candidates[i]);
		    		}
		    		if(!definitionNeighborMap.containsKey(candidates[i]))
		    		{
		    			definitionNeighborMap.put(candidates[i], new ArrayList<String>());
		    		}
		    		
		    		//System.out.println(thisWord + "---->" + candidates[i]);
		    	}
		    }
		    
		    
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}
		return defKeywords;
	}
}
