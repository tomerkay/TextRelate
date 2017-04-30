package TextRelateProject.RSS;

import java.util.ArrayList;

/**
 * Created by Yair on 24/01/2017.
 */
public class RSS {
    public static ArrayList<String> getLinks(String rss_feed) {
        RSSFeedParser parser = new RSSFeedParser(rss_feed);
        Feed feed = parser.readFeed();
        ArrayList<String> links = new ArrayList<>();
        for (FeedMessage message : feed.getMessages()) {
            links.add(message.getLink());
        }
        return links;
    }
}
