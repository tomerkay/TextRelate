package TextRelateProject.BingApi; /**
 * Created by Ronen on 04/11/2016.
 */

import TextRelateProject.DocumentRanking.RetrievedDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class jsonParser {


    public static ArrayList<RetrievedDocument> getFastNews(String json_format) {
        ArrayList<RetrievedDocument> docs = new ArrayList<>();
        JSONObject json = new JSONObject(json_format);
        JSONArray newsArr = new JSONArray();
        try {
            newsArr = json.getJSONArray("value");
        } catch (org.json.JSONException e) {
            System.out.println("problem with json: " + json_format);
        }
        for (int i = 0; i < newsArr.length(); i++) {
            String url = newsArr.getJSONObject(i).getString("url");
            String title = newsArr.getJSONObject(i).getString("name");
            String description = newsArr.getJSONObject(i).getString("description");
            if ( url != null && (!url.equals("")) && title != null && (!title.equals("")) && description != null && (!description.equals(""))) {
                docs.add(new RetrievedDocument(url, title, description));
            }
        }
        return docs;
    }


}
