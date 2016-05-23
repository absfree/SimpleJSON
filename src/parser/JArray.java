package parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/20.
 */
public class JArray implements  Json, Value {
    private List<Json> list = new ArrayList<>();

    public JArray(List<Json> list) {
        this.list = list;
    }

    public int length() {
        return list.size();
    }

    public void add(Json element) {
        list.add(element);
    }

    public Json get(int i) {
        return list.get(i);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i =0; i < list.size(); i++) {
            sb.append(list.get(i).toString());
            if (i != list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public Object value() {
        return this;
    }
}
