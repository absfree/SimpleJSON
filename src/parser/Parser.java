package parser;

import com.google.gson.JsonParseException;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/20.
 */
public class Parser {
    private Tokenizer tokenizer;

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    private JObject object() {
        tokenizer.next(); //consume '{'
        Map<String, Value> map = new HashMap<>();
        if (isToken(TokenType.END_OBJ)) {
            tokenizer.next(); //consume '}'
            return new JObject(map);
        } else if (isToken(TokenType.STRING)) {
            map = key(map);
        }
        return new JObject(map);
    }

    private Map<String, Value> key(Map<String, Value> map) {
        String key = tokenizer.next().getValue();
        if (!isToken(TokenType.COLON)) {
            throw new JsonParseException("Invalid JSON input.");
        } else {
            tokenizer.next(); //consume ':'
            if (isPrimary()) {
                Value primary = new Primary(tokenizer.next().getValue());
                map.put(key, primary);
            } else if (isToken(TokenType.START_ARRAY)) {
                Value array = array();
                map.put(key, array);
            }
            if (isToken(TokenType.COMMA)) {
                tokenizer.next(); //consume ','
                if (isToken(TokenType.STRING)) {
                    map = key(map);
                }
            } else if (isToken(TokenType.END_OBJ)) {
                tokenizer.next(); //consume '}'
                return map;
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        }
        return map;
    }

    private JArray array() {
        tokenizer.next(); //consume '['
        List<Json> list = new ArrayList<>();
        JArray array = null;
        if (isToken(TokenType.START_ARRAY)) {
            array = array();
            list.add(array);
            if (isToken(TokenType.COMMA)) {
                tokenizer.next(); //consume ','
                list = element(list);
            }
        } else if (isPrimary()) {
            list = element(list);
        } else if (isToken(TokenType.START_OBJ)) {
            list.add(object());
            while (isToken(TokenType.COMMA)) {
                tokenizer.next(); //consume ','
                list.add(object());
            }
        } else if (isToken(TokenType.END_ARRAY)) {
            tokenizer.next(); //consume ']'
            array =  new JArray(list);
            return array;
        }
        tokenizer.next(); //consume ']'
        array = new JArray(list);
        return array;
    }

    private List<Json> element(List<Json> list) {
        list.add(new Primary(tokenizer.next().getValue()));
        if (isToken(TokenType.COMMA)) {
            tokenizer.next(); //consume ','
            if (isPrimary()) {
                list = element(list);
            } else if (isToken(TokenType.START_OBJ)) {
                list.add(object());
            } else if (isToken(TokenType.START_ARRAY)) {
                list.add(array());
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        } else if (isToken(TokenType.END_ARRAY)) {
            return list;
        } else {
            throw new JsonParseException("Invalid JSON input.");
        }
        return list;
    }

    private Json json() {
        TokenType type = tokenizer.peek(0).getType();
        if (type == TokenType.START_ARRAY) {
            return array();
        } else if (type == TokenType.START_OBJ) {
            return object();
        } else {
            throw new JsonParseException("Invalid JSON input.");
        }
    }

    private boolean isToken(TokenType tokenType) {
        Token t = tokenizer.peek(0);
        return t.getType() == tokenType;
    }

    private boolean isToken(String name) {
        Token t = tokenizer.peek(0);
        return t.getValue().equals(name);
    }

    private boolean isPrimary() {
        TokenType type = tokenizer.peek(0).getType();
        return type == TokenType.BOOLEAN || type == TokenType.NULL  ||
                type == TokenType.NUMBER || type == TokenType.STRING;
    }

    public Json parse() throws Exception {
        Json result = json();
        return result;
    }

    public static JObject parseJSONObject(String s) throws Exception {
        Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader(s)));
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        return parser.object();
    }

    public static JArray parseJSONArray(String s) throws Exception {
        Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader(s)));
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        return parser.array();
    }

    public static <T> T fromJson(String jsonString, Class<T> classOfT) throws Exception {
        Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader(jsonString)));
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        JObject result = parser.object();

        Constructor<T> constructor = classOfT.getConstructor();
        Object latestNews = constructor.newInstance();
        Field[] fields = classOfT.getDeclaredFields();
        int numField = fields.length;
        String[] fieldNames = new String[numField];
        String[] fieldTypes = new String[numField];
        for (int i = 0; i < numField; i++) {
            String type = fields[i].getType().getTypeName();
            String name = fields[i].getName();
            fieldTypes[i] = type;
            fieldNames[i] = name;
        }
        for (int i = 0; i < numField; i++) {
            if (fieldTypes[i].equals("java.lang.String")) {
                fields[i].setAccessible(true);
                fields[i].set(latestNews, result.getString(fieldNames[i]));
            } else if (fieldTypes[i].equals("java.util.List")) {
                fields[i].setAccessible(true);
                JArray array = result.getJArray(fieldNames[i]);
                ParameterizedType pt = (ParameterizedType) fields[i].getGenericType();
                Type elementType = pt.getActualTypeArguments()[0];
                String elementTypeName = elementType.getTypeName();
                Class<?> elementClass = Class.forName(elementTypeName);

                fields[i].set(latestNews, inflateList(array, elementClass));//Type Capture

            } else if (fieldTypes[i].equals("int")) {
                fields[i].setAccessible(true);
                fields[i].set(latestNews, result.getString(fieldNames[i]));
            }
        }
        return (T) latestNews;
    }

    public static <T> List<T> inflateList(JArray array, Class<T> clz) throws Exception {
        int size = array.length();

        List<T> list = new ArrayList<T>();
        Constructor<T> constructor = clz.getConstructor();
        String className = clz.getName();
        if (className.equals("java.lang.String")) {
            for (int i = 0; i < size; i++) {
                String element = (String) ((Primary) array.get(i)).value();
                list.add((T) element);
                return list;
            }
        }
        Field[] fields = clz.getDeclaredFields();
        int numField = fields.length;
        String[] fieldNames = new String[numField];
        String[] fieldTypes = new String[numField];

        for (int i = 0; i < numField; i++) {
            String type = fields[i].getType().getTypeName();
            String name = fields[i].getName();
            fieldTypes[i] = type;
            fieldNames[i] = name;
        }
        for (int i = 0; i < size; i++) {
            T element = constructor.newInstance();
            JObject object = (JObject) array.get(i);
            for (int j = 0; j < numField; j++) {
                if (fieldTypes[j].equals("java.lang.String")) {
                    fields[j].setAccessible(true);
                    fields[j].set(element, (object.getString(fieldNames[j])));
                } else if (fieldTypes[j].equals("java.util.List")) {
                    fields[j].setAccessible(true);
                    JArray nestArray = object.getJArray(fieldNames[j]);
                    ParameterizedType pt = (ParameterizedType) fields[j].getGenericType();
                    Type elementType = pt.getActualTypeArguments()[0];
                    String elementTypeName = elementType.getTypeName();
                    Class<?> elementClass = Class.forName(elementTypeName);
                    String value = null;

                    fields[j].set(element, inflateList(nestArray, elementClass));//Type Capture
                } else if (fieldTypes[j].equals("int")) {
                    fields[j].setAccessible(true);
                    fields[j].set(element, object.getInt(fieldNames[j]));
                }
            }
            list.add(element);
        }
        return list;
    }

}
