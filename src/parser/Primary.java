package parser;

/**
 * Created by Administrator on 2016/5/20.
 */
public class Primary implements Json, Value {
    private String value;

    public Primary(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    @Override
    public Object value() {
        return value;
    }
}
