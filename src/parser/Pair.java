package parser;

/**
 * Created by Administrator on 2016/5/20.
 */
public class Pair extends Json {
    private String key;
    private Value value;

    public Pair(String key, Value value) {
        this.key = key;
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append(" : ");
        sb.append(value);
        return sb.toString();
    }
}
