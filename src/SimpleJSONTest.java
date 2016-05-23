import com.google.gson.Gson;
import model.LatestNews;
import parser.Parser;

/**
 * Created by Administrator on 2016/5/22.
 */
public class SimpleJSONTest {
    public static final String urlString = "http://news-at.zhihu.com/api/4/news/latest";
    public static void main(String[] args) throws Exception {
        String jsonString = new String(HttpUtil.get(urlString));
       
        long startTime = System.currentTimeMillis();
       
        LatestNews latest = Parser.fromJson(jsonString, LatestNews.class);
       
        long endTime = System.currentTimeMillis();
        double time = (double) (endTime - startTime) / 1000.0;
        System.out.println("took " + time + "seconds.");
       
        System.out.println(latest.getDate());
        for (int i = 0; i < latest.getTop_stories().size(); i++) {
            System.out.println(latest.getTop_stories().get(i));
        }
        for (int i = 0; i < latest.getStories().size(); i++) {
            System.out.println(latest.getStories().get(i));
        }
    }
}
