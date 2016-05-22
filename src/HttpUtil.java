import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/5/22.
 */
public class HttpUtil {
    public static byte[] get(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            urlConnection.setRequestMethod("GET");
            //设置超时时间
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(3000);

            //获取响应的状态码
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream in = urlConnection.getInputStream();
                byte[] buffer = new byte[4 * 1024];
                int len = -1;
                while((len = in.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                close(in);
                byte[] result = bos.toByteArray();
                close(bos);
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
