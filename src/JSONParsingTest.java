import model.LatestNews;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import parser.JArray;
import parser.JObject;
import parser.Parser;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/17.
 */
public class JSONParsingTest {
    public static final String urlString = "http://news-at.zhihu.com/api/4/news/latest";
    public static void main(String[] args) throws Exception {
        long startTime = 0;
        try {
            String jsonString = new String(HttpUtil.get(urlString));
            startTime = System.currentTimeMillis();

            JSONObject latestNewsJSON = new JSONObject(jsonString);
            String date = latestNewsJSON.getString("date");
            JSONArray top_storiesJSON = latestNewsJSON.getJSONArray("top_stories");
            LatestNews latest = new LatestNews();
            List<LatestNews.TopStory> stories = new ArrayList<>();
            for (int i = 0; i < top_storiesJSON.length(); i++) {
                LatestNews.TopStory story = new LatestNews.TopStory();
                story.setId(((JSONObject) top_storiesJSON.get(i)).getInt("id"));
                story.setType(((JSONObject) top_storiesJSON.get(i)).getInt("type"));
                story.setImage(((JSONObject) top_storiesJSON.get(i)).getString("image"));
                story.setTitle(((JSONObject) top_storiesJSON.get(i)).getString("title"));
                stories.add(story);
            }

            long endTime = System.currentTimeMillis();
            double time = (double) (endTime - startTime) / 1000.0;
            System.out.println("took " + time + "seconds.");

            latest.setDate(date);
            System.out.println("date: " + latest.getDate());
            for (int i = 0; i < stories.size(); i++) {
                System.out.println(stories.get(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
