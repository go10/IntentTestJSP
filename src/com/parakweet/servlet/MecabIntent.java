package com.parakweet.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.parakweet.nlp.Language;
import com.parakweet.parser.Intent;
import com.parakweet.parser.Phrase;
import com.parakweet.parser.Token;
import com.parakweet.parser.match.MatchResult;
import com.parakweet.parser.match.MatchResultItem;



/**
 * Servlet implementation class MecabIntent
 */
public class MecabIntent extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MecabIntent() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Not implemented
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sTerm = request.getParameter("searchterm");
		if (sTerm != null)  sTerm = new String(sTerm.getBytes("8859_1"), "UTF8");

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>Twitter Intent Results</title></head>");
		out.println("<body>");
		
		out.println("<form action=\"MecabIntent\" method=\"POST\">");
		out.println("Search Twitter: <input type=\"text\" name=\"searchterm\" size=\"20\"><br>");
		out.println("<input type=\"submit\" value=\"Submit\">");
		out.println("</form>");
		
		out.println("<h1>Twitter Intent Results" + request.getContextPath () + "</h1>");
		out.println("<p>Input: "+ sTerm + "</p>");
	    
		getIntentData(out, sTerm);
		
        out.println("</body>");
        out.println("</html>");
        out.close();
	}
	
	
	/**
	 * 
	 * @param out
	 * @param sTerm
	 */
	private void getIntentData(PrintWriter out, String sTerm)
	{
		Twitter twitter = new TwitterFactory().getInstance();                                     
        try {                          
        	Query twitQuery = new Query(sTerm);
            QueryResult result1 = twitter.search(twitQuery);
            List<Tweet> tweets1 = result1.getTweets();
        	twitQuery.setPage(2);
            QueryResult result2 = twitter.search(twitQuery);
            List<Tweet> tweets2 = result2.getTweets();
            tweets1.addAll(tweets2);
            System.out.println("Found "+ tweets1.size() + " results for "+ sTerm);
            for (Tweet tweet : tweets1) {
            	final String sTweetText = tweet.getText();
            	out.println("<p>@" + tweet.getFromUser() + " - " + sTweetText + "</p>");
            	MatchResult matchResult = Intent.findIntentWithResult(sTweetText, false, false, Language.JAPANESE);            	
            	if (matchResult.hasResultItems()) {
            		MatchResult mergedMatchResult = matchResult.mergeSimilarItems();
            		MatchResult postprocessedMatchResult = mergedMatchResult.postprocess();
            		Intent.normalizeTags(postprocessedMatchResult);
            		out.println("<p>Normalized tags:");
            		printResults(postprocessedMatchResult, out);
            		out.println("</p>");
            	} else {
            		out.println("<p>Intent: Not detected. </p>");
            	}
            }
        } catch (TwitterException te) {                                                           
            te.printStackTrace();                                                                 
            System.out.println("Failed to search tweets: " + te.getMessage());                    
        }
	}
	
	
	/**
	 * 
	 * @param result
	 * @param out
	 */
	public static void printResults(MatchResult result, PrintWriter out) 
	{
		List<MatchResultItem> resultItems = result.getResultItems();
		boolean first = true;
		for (MatchResultItem resultItem : resultItems) {
			
			if (!first) {
				out.println("    ------");
			}
			out.println("  Rules: " + resultItem.getMatchedRules());
			out.println("  Tags:  " + resultItem.getMatchTags());
			out.println("  Intent class: " + Intent.getIntentClass(resultItem));
			out.println("  Intent verb:  " + Intent.getIntentVerb(resultItem));
			
			Phrase intent = resultItem.getMatchedPhrase();
			Token[] mainNouns = resultItem.getMainNouns();
			
			out.println("    IP: " + intent.concatenate());
			out.println("    IP: " + intent.concatenateWithFullData());
			out.println("    MN: " + Token.concatenate(mainNouns));
			
			first = false;
		}
	}
}

