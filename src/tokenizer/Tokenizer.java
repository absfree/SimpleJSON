package tokenizer;

import exception.JsonParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/5/17.
 */
public class Tokenizer {
//    public static String regexPat
//            = "\\s*(([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|\\\\b|\\\\r|\\\\t|\\\\f|\\\\uhhhh|[^\"])*\")"
//            + "|\\p{Punct})?";
//    private Pattern pattern = Pattern.compile(regexPat);
    private ArrayList<Token> tokens = new ArrayList<>();
    private Reader reader;
    private boolean isUnread = false;
    private int savedChar;
    private int c; //recently read char

    public Tokenizer(Reader reader) throws IOException {
        this.reader = reader;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void tokenize() throws Exception {
        Token token;
        do {
            token = start();
            tokens.add(token);
        } while (token.getType() != TokenType.END_DOC);
    }

    private Token start() throws Exception {
        c = '?';
        Token token = null;
        do {    //先读一个字符，若为空白符（ASCII码在[0, 20H]上）则接着读，直到刚读的字符非空白符
            c = read();
        } while (isSpace(c));
        if (isNull(c)) {
            return new Token(TokenType.NULL, null);
        } else if (c == ',') {
            return new Token(TokenType.COMMA, ",");
        } else if (c == ':') {
            return new Token(TokenType.COLON, ":");
        } else if (c == '{') {
            return new Token(TokenType.START_OBJ, "{");
        } else if (c == '[') {
            return new Token(TokenType.START_ARRAY, "[");
        } else if (c == ']') {
            return new Token(TokenType.END_ARRAY, "]");
        } else if (c == '}') {
            return new Token(TokenType.END_OBJ, "}");
        } else if (isTrue(c)) {
            return new Token(TokenType.BOOLEAN, "true"); //the value of TRUE is not null
        } else if (isFalse(c)) {
            return new Token(TokenType.BOOLEAN, "false"); //the value of FALSE is null
        } else if (c == '"') {
            return readString();
        } else if (isNum(c)) {
            unread();
            return readNum();
        } else if (c == -1) {
            return new Token(TokenType.END_DOC, "EOF");
        } else {
            throw new JsonParseException("Invalid JSON input.");
        }
    }

    private int read() throws IOException {
        if (!isUnread) {
            int c = reader.read();
            savedChar = c;
            return c;
        } else {
            isUnread = false;
            return savedChar;
        }
    }

    private void unread() {
        isUnread = true;
    }

    private boolean isSpace(int c) {
        return c >= 0 && c <= ' ';
    }

//    private boolean isLetter(int c) {
//        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
//    }

    private boolean isTrue(int c) throws IOException {
        if (c == 't') {
            c = read();
            if (c == 'r') {
                c = read();
                if (c == 'u') {
                    c = read();
                    if (c == 'e') {
                        return true;
                    } else {
                        throw new JsonParseException("Invalid JSON input.");
                    }
                } else {
                    throw new JsonParseException("Invalid JSON input.");
                }
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        } else {
            return false;
        }
    }

    private boolean isFalse(int c) throws IOException {
        if (c == 'f') {
            c = read();
            if (c == 'a') {
                c = read();
                if (c == 'l') {
                    c = read();
                    if (c == 's') {
                        c = read();
                        if (c == 'e') {
                            return true;
                        } else {
                            throw new JsonParseException("Invalid JSON input.");
                        }
                    } else {
                        throw new JsonParseException("Invalid JSON input.");
                    }
                } else {
                    throw new JsonParseException("Invalid JSON input.");
                }
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        } else {
            return false;
        }
    }

    private boolean isEscape() throws IOException {
        if (c == '\\') {
            c = read();
            if (c == '"' || c == '\\' || c == '/' || c == 'b' ||
                    c == 'f' || c == 'n' || c == 't' || c == 'r' || c == 'u') {
                return true;
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        } else {
            return false;
        }
    }
    

    private boolean isNull(int c) throws IOException {
        if (c == 'n') {
            c = read();
            if (c == 'u') {
                c = read();
                if (c == 'l') {
                    c = read();
                    if (c == 'l') {
                        return true;
                    } else {
                        throw new JsonParseException("Invalid JSON input.");
                    }
                } else {
                    throw new JsonParseException("Invalid JSON input.");
                }
            } else {
                throw new JsonParseException("Invalid JSON input.");
            }
        } else {
            return false;
        }
    }

    private boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    private boolean isDigitOne2Nine(int c){
        return c >= '1' && c <= '9';
    }

    private boolean isSep(int c) {
        return c == '}' || c == ']' || c == ',';
    }

//    private boolean isNumValid(int c) throws IOException {
//        while (isSpace(c)) {
//            c = read();
//        }
//        if (isSep(c)) {
//            unread();
//            return true;
//        } else {
//            return false;
//        }
//    }

    private boolean isNum(int c) {
        return isDigit(c) || c == '-';
    }

    private Token readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            c = read();
            if (isEscape()) {    //判断是否为\", \\, \/, \b, \f, \n, \t, \r.
                if (c == 'u') {
                    sb.append('\\' + (char) c);
                    for (int i = 0; i < 4; i++) {
                        c = read();
                        if (isHex(c)) {
                            sb.append((char) c);
                        } else {
                            throw new JsonParseException("Invalid Json input.");
                        }
                    }
                } else {
                    sb.append("\\" + (char) c);
                }
            } else if (c == '"') {
                return new Token(TokenType.STRING, sb.toString());
            } else if (c == '\r' || c == '\n'){
                throw new JsonParseException("Invalid JSON input.");
            } else {
                sb.append((char) c);
            }
        }
    }

    private Token readNum() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c = read();
        if (c == '-') { //-
            sb.append((char) c);
            c = read();
            if (c == '0') { //-0
                sb.append((char) c);
                numAppend(sb);

            } else if (isDigitOne2Nine(c)) { //-digit1-9
                do {
                    sb.append((char) c);
                    c = read();
                } while (isDigit(c));
                unread();
                numAppend(sb);
            } else {
                throw new JsonParseException("- not followed by digit");
            }
        } else if (c == '0') { //0
            sb.append((char) c);
            numAppend(sb);
        } else if (isDigitOne2Nine(c)) { //digit1-9
            do {
                sb.append((char) c);
                c = read();
            } while (isDigit(c));
            unread();
            numAppend(sb);
        }
        return new Token(TokenType.NUMBER, sb.toString()); //the value of 0 is null
    }

    private void appendFrac(StringBuilder sb) throws IOException {
        c = read();
        while (isDigit(c)) {
            sb.append((char) c);
            c = read();
        }
    }

    private void appendExp(StringBuilder sb) throws IOException {
        int c = read();
        if (c == '+' || c == '-') {
            sb.append((char) c); //append '+' or '-'
            c = read();
            if (!isDigit(c)) {
                throw new JsonParseException("e+(-) or E+(-) not followed by digit");
            } else { //e+(-) digit
                do {
                    sb.append((char) c);
                    c = read();
                } while (isDigit(c));
                unread();
            }
        } else if (!isDigit(c)) {
            throw new JsonParseException("e or E not followed by + or - or digit.");
        } else { //e digit
            do {
                sb.append((char) c);
                c = read();
            } while (isDigit(c));
            unread();
        }
    }

    private void numAppend(StringBuilder sb) throws IOException {
        c = read();
        if (c == '.') { //int frac
            sb.append((char) c); //apppend '.'
            appendFrac(sb);
            if (isExp(c)) { //int frac exp
                sb.append((char) c); //append 'e' or 'E';
                appendExp(sb);
            }

        } else if (isExp(c)) { // int exp
            sb.append((char) c); //append 'e' or 'E'
            appendExp(sb);
        } else {
            unread();
        }
    }

    private boolean isHex(int c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F');
    }

    private boolean isExp(int c) throws IOException {
        //int next;
        //if (c != 'e' && c != 'E') {
        //    return false;
        //} else {
        //    next = read();
        //    unread();
        //}
        //return next == '+' || next == '-' || isDigit(next);
        return c == 'e' || c == 'E';
    }

    public Token next() {
        return tokens.remove(0);
    }

    public Token peek(int i) {
        return tokens.get(i);
    }

    public boolean hasNext() {
        return tokens.get(0).getType() != TokenType.END_DOC;
    }


    public static void main(String[] args) {
        try {
            //Tokenizer tokenizer = new Tokenizer(new BufferedReader(new FileReader(new File("E:\\jvm\\test\\json3.txt"))));
            Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader("{\"date\":\"20160522\",\"stories\":[{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/ba51ea5bd1f269d78fcadb0246f6928c.jpg\"],\"type\":0,\"id\":8156094,\"ga_prefix\":\"052214\",\"title\":\"曾有一群「愤青」大闹艺术界，像病毒一样带坏了好多人\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/30adce1f5c9d3cc76db1575b98dd5f61.jpg\"],\"type\":0,\"id\":8335861,\"ga_prefix\":\"052213\",\"title\":\"「我都已经秒回消息了，你怎么还对我这么敷衍？」\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/b3cd8ed6772ae371a88ee60a9d35ea6c.jpg\"],\"type\":0,\"id\":8336878,\"ga_prefix\":\"052212\",\"title\":\"大误 · 为了吃到它，我们付出了生命\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/ff7955aabca49646587d070994638441.jpg\"],\"type\":0,\"id\":8334928,\"ga_prefix\":\"052211\",\"title\":\"先爆炒再收汁，自制麻辣小龙虾我能吃一锅（附洗虾小窍门）\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/7d46a339bc6d865170836c55eeb07038.jpg\"],\"type\":0,\"id\":8336047,\"ga_prefix\":\"052210\",\"title\":\"PS 学会这招，做出「看着就很疼」的效果\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/2a856987652facd43543be4a75ae6460.jpg\"],\"type\":0,\"id\":8323421,\"ga_prefix\":\"052209\",\"title\":\"穷国死囚们唱歌、发专辑，还能入围格莱美，这事全靠一对夫妻\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/fe8b35c2bc85325e40e08a56832229a8.jpg\"],\"type\":0,\"id\":8334157,\"ga_prefix\":\"052208\",\"title\":\"同声传译这么累的工作，做久了会不会损伤大脑啊……\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/8e5581ce4b718edb5e836d8f6edccd01.jpg\"],\"type\":0,\"id\":8335549,\"ga_prefix\":\"052207\",\"title\":\"日式拉面没有「正宗吃法」，但我会最大程度享受这碗面\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/2ae8a5cf2585f62e50a16ee85050f975.jpg\"],\"type\":0,\"id\":8335472,\"ga_prefix\":\"052207\",\"title\":\"夏天到了，这个看着像感冒实则凶险的疾病又来威胁孩子了\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/b4c400ff2574602b8a569e8cd6b81780.jpg\"],\"type\":0,\"id\":8330053,\"ga_prefix\":\"052207\",\"title\":\"关于西方人「爱吃垃圾食品」和「健康长寿」，你可能都想错了\"},{\"images\":[\"http:\\/\\/pic4.zhimg.com\\/4f3d28dc40c5654add542b54cc23199b.jpg\"],\"type\":0,\"id\":8336193,\"ga_prefix\":\"052207\",\"title\":\"读读日报 24 小时热门 TOP 5 · 最缱绻的老式恋爱\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/dbad70b0c814117c09e689534cff19a9.jpg\"],\"type\":0,\"id\":8326437,\"ga_prefix\":\"052206\",\"title\":\"瞎扯 · 大哥那是澡堂\"}],\"top_stories\":[{\"image\":\"http:\\/\\/pic1.zhimg.com\\/72ec2b9cc72fc0382ecbebbf3845b1c8.jpg\",\"type\":0,\"id\":8334928,\"ga_prefix\":\"052211\",\"title\":\"先爆炒再收汁，自制麻辣小龙虾我能吃一锅（附洗虾小窍门）\"},{\"image\":\"http:\\/\\/pic2.zhimg.com\\/ecc370dec7486778d88aaf5fe6353fbd.jpg\",\"type\":0,\"id\":8330053,\"ga_prefix\":\"052207\",\"title\":\"关于西方人「爱吃垃圾食品」和「健康长寿」，你可能都想错了\"},{\"image\":\"http:\\/\\/pic4.zhimg.com\\/969716884a1913a496c8bc5e05fc2083.jpg\",\"type\":0,\"id\":8336193,\"ga_prefix\":\"052207\",\"title\":\"读读日报 24 小时热门 TOP 5 · 最缱绻的老式恋爱\"},{\"image\":\"http:\\/\\/pic2.zhimg.com\\/bff58b6f0a862881916531c8a7987fcd.jpg\",\"type\":0,\"id\":8323202,\"ga_prefix\":\"052117\",\"title\":\"宫保鸡丁到底加不加黄瓜青笋胡萝卜？\"},{\"image\":\"http:\\/\\/pic1.zhimg.com\\/3ac02cab0aa5eb2bbb56ac0b6fa9aa2c.jpg\",\"type\":0,\"id\":8324432,\"ga_prefix\":\"052112\",\"title\":\"大误 · 在我七岁时，父亲斩断了我的左手\"}]}")));
            tokenizer.tokenize();
            for (Token token : tokenizer.getTokens()) {
                System.out.println(token);
            }
            StringBuilder sb = new StringBuilder();
            sb.append((char) 65533);
            sb.append('遥');
            sb.append('游');
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
