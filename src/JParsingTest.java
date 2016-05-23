import model.LatestNews;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import parser.JArray;
import parser.JObject;

import java.util.ArrayList;
import java.util.List;

import static parser.Parser.parseJSONObject;

/**
 * Created by Administrator on 2016/5/22.
 */
public class JParsingTest {
    public static final String urlString = "http://news-at.zhihu.com/api/4/news/latest";
    public static void main(String[] args) throws Exception {
        long startTime = 0;
        try {
            String jsonString = new String(HttpUtil.get(urlString));
            startTime = System.currentTimeMillis();
            
            JObject latestNewsJSON = parseJSONObject(jsonString);
            String date = latestNewsJSON.getString("date");
            JArray top_storiesJSON = latestNewsJSON.getJArray("top_stories");
            LatestNews latest = new LatestNews();
            List<LatestNews.TopStory> stories = new ArrayList<>();
            for (int i = 0; i < top_storiesJSON.length(); i++) {
                LatestNews.TopStory story = new LatestNews.TopStory();
                story.setId(((JObject) top_storiesJSON.get(i)).getInt("id"));
                story.setType(((JObject) top_storiesJSON.get(i)).getInt("type"));
                story.setImage(((JObject) top_storiesJSON.get(i)).getString("image"));
                story.setTitle(((JObject) top_storiesJSON.get(i)).getString("title"));
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
