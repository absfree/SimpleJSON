package exception;

import java.io.IOException;

/**
 * Created by Administrator on 2016/5/22.
 */
public class JsonParseException extends IOException {
    public JsonParseException(String msg) {
        super(msg);
    }
}
