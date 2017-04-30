package TextRelateProject.BingApi; /**
 * Created by Ronen on 07/11/2016.
 */

import TextRelateProject.DocumentRanking.RetrievedDocument;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;

public class NewsSearch {

    private static String getResultsJson(String query, int res_num, String Internetkey) {
        HttpClient httpclient = HttpClients.createDefault();
        HttpEntity entity = null;
        try {
            URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v5.0/news/search");

            builder.setParameter("q", query);
            builder.setParameter("count", Integer.toString(res_num));
            builder.setParameter("mkt", "en-us");

            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", Internetkey);
            request.setHeader("User-Agent", "Mozilla/5.0");

            // Request body
            //StringEntity reqEntity = new StringEntity("{body}");
            //request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static ArrayList<RetrievedDocument> getDocuments(String query, int res_num, String Internetkey) {
        String results_json = getResultsJson(query, res_num, Internetkey);
        if (results_json == null) {
            System.out.println("Couldn't retrieve results from Bing for query: "+query+".");
            return null;
        }
        return jsonParser.getFastNews(results_json);
    }

    public static ArrayList<RetrievedDocument> getQueriesResults(ArrayList<String> queries, int res_num, String InternetKey) {
        ArrayList<RetrievedDocument> results = new ArrayList<>();
        for (String query : queries) {
            ArrayList<RetrievedDocument> temp = getDocuments(query, res_num, InternetKey);
            if (temp != null) {
                results.addAll(temp);
            }
        }
        return results;
    }

}
