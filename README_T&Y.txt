Hello All!
We made this README file so you will know what the project is about and how to run it.
Our project receives a news article and output 5 news articles selected from different sources relating to the same topics as the input. The output of our project is input to another project that synthesizes an article based on its input articles. Together, the two projects give us desired goal– a news article not biased by one source. The synthesized articles can be seen on a website we created with WordPress.

How our project works:
	Part 1:
		Construct queries relating to the same topics that appear in an input news article and use Bing News search engine to search for additional articles relating to the same topics.

		And in stages:
		Stage 1: Receive News Article
		Stage 2: Extract Topics and Construct Queries
		Stage 3: Run Bing News Search Engine


	Part 2:
		Rank the additional articles by how similar their topics are to the topics of the input article for selecting the 5 most relevant.

		And in stages:
		Stage 1:  Quick Filter
		Stage 2 : Sentences into Vectors, and their Similarities
		Stage 3: Weighting Input Article Sentences
		Stage 4: Ranking Additional Articles
		


Our project has three inputs:
RSS/URL
url link
not mandatory - path to the "manual.txt" (the name is as you desire)

You need to run the jar from the file that contains the jar.
Notice that the .jar file must be in a specific relation to "java libraries" folder - inside a folder "X", when "X" is in the same folder as "java libraries".

Example for a run:
java -jar TextRelate.jar RSS http://feeds.reuters.com/news/artsculture manual.txt

For the simple case, the URL, the project takes the news article that is in the link. If it is a RSS link than it holds several links of articles, and for each one the project will do the process as if they would have ran with URL in the input.

The "manual.txt" contains 16 additional inputs to the project you can change. If you don't insert a path to a manual file, there are default arguments built in the project, but it is clearly not recommended to use it.

The arguments inside the manual:
input[0] is the number of results per query.
input[1] is OpenNLP mode or Stanford mode for analyzing the text (write OpenNLP or Stanford)
input[2] is the mode - fast (minimal analyzing of the article from the internet), both, or accurate (no initail filter to the articles from the internet) - 0 ,1 or 2 accordingly
input[3] is the score equation method - avg or max (avg is more recommended)
input[4] is how many results we want at the end (the output of our project, the number of top ranks articles)
input[5] is the path folder where we save our results, most similar texts
input[6] is the path to the file where will save the summary, the synthesized text
input[7] is the number of threads we want for crawling the web, recommended 8. if want don't wont write 0 (or less)
input[8] is the number of lines in each summary.
input[9] save the project results - input saveResult.
input[10] save the merged document - input saveMerge.
input[11] add to database - input saveDB.
input[12] the key to Bing for querying
input[13] if we want the title to be considered in the ranking equation - input useTitle
input[14] is the number of results for the RSS feed.
input[15] the name of the database.


Yours Truly,
Tomer and Yair
