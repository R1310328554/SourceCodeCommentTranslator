import static com.github.javaparser.Providers.provider;
import static com.github.javaparser.StaticJavaParser.*;
import static org.apache.commons.io.IOUtils.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.translate.demo.TransApi;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.SourceRoot;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import com.sun.javadoc.*;

public class SourceCodeCommentTranslator {

    // 在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
    // https://fanyi-api.baidu.com/api/trans/vip/translate
    private static final String APP_ID = "20200829000554601";
    private static final String SECURITY_KEY = "lD7F99jRulkCrRzXItoo";
    private static final String SOURCE_DIR = "D:\\git\\spring-framework-5.2.1.RELEASE\\";
    private static final String moduleName = "spring-core";

    //  todo 对于连续的 // 注释的翻译也应该连续..

    public static void main(String[] args) throws Exception {
        testConvert2line();
//        testWriteFile();

        testaa();
//        translate();
        CompilationUnit cu = parse("class X{}");
//        SOURCE_DIR = "D:\\git\\spring-framework-5.2.1.RELEASE\\spring-webflux\\src\\main\\java\\org\\springframework\\web\\reactive\\config\\PathMatchConfigurer2.java";
//        SOURCE_DIR = "D:\\git\\spring-framework-5.2.1.RELEASE\\spring-core\\src\\main\\java\\org\\springframework\\asm\\Frame.java";
//        File file    = new File(SOURCE_DIR);
//        Path path = file.toPath();
////        Paths.get(SOURCE_DIR);
//        CompilationUnit parse = parse(path);
//        List<Comment> allComments = parse.getAllComments();
//        for (int i = 0; i < allComments.size(); i++) {
//            Comment comment =  allComments.get(i);
//            String content = comment.getContent();
//            if (content.startsWith("/* Copyright")) {
//                continue;// 版权信息不需要翻译
//            }
//            System.out.println("content = " + content);
//        }

//        parseJavaFile(parse);
        SourceRoot sourceRoot = new SourceRoot(Paths.get(SOURCE_DIR + moduleName + "\\src\\main\\"));
        List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
//        System.out.println("parseResults = " + parseResults);
        Optional<CompilationUnit> result = parseResults.get(0).getResult();
//        System.out.println("result = " + result);
        System.out.println();
        System.out.println();
        List<CompilationUnit> compilationUnits = sourceRoot.getCompilationUnits();
//        System.out.println("compilationUnits = " + compilationUnits);
        for (int i = 0; i < compilationUnits.size(); i++) {
            CompilationUnit compilationUnit =  compilationUnits.get(i);
            parseJavaFile(compilationUnit);
        }
    }

    private static void testaa() {

    }

    private static void testWriteFile() {
        String s = "D:\\git\\spring-framework-5.2.1.RELEASE\\spring-tx\\src\\main\\java\\org\\springframework\\transaction\\support\\AbstractPlatformTransactionManager.java";
//        s = "D:\\git\\spring-framework-5.2.1.RELEASE\\spring-tx\\src\\main\\java\\org\\springframework\\transaction\\support\\AbstractPlatformTransactionManager.java";
        finishedSet.add(s.toString());
        if (finishedSet.contains(s)) {
            System.out.println("重复了 ！ " + s);
            return;
        }
//        finishedSet.add("D:\\git\\spring-framework-5.2.1.RELEASE\\spring-tx\\src\\main\\java\\org\\springframework\\jca\\cci\\core\\Add.java".toString());
        OutputStream fileWriter = null;
        try {
            fileWriter = new FileOutputStream("finishedSet_"+moduleName + ".txt");
//            writeLines(finishedSet, "\n", fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkStopper();
//        System.exit(-2);
    }

    static Map finishedMap = new HashMap();
    static Map<String, String> cachedMap = new HashMap();
    static Set finishedSet;
    static {
        System.out.println("APP_ID = " + APP_ID);
        finishedSet = new HashSet();
        String fname = "finishedSet_" + moduleName + ".txt";
        try {
            List<String> strings = readLines(new FileInputStream(fname));
            finishedSet.addAll(strings);
        } catch (IOException e) {
            e.printStackTrace();
//            File file = new File(fname);
//            boolean newFile = false;
//            try {
//                newFile = file.createNewFile();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            System.out.println("newFile = " + newFile);
        }
    }

    private static void parseJavaFile(CompilationUnit cu) throws InterruptedException {
        CompilationUnit parse = cu;
        Optional<CompilationUnit.Storage> storageOptional = parse.getStorage();
        CompilationUnit.Storage storage = storageOptional.get();
        String fileName = storage.getFileName();
        Path storagePath = storage.getPath();
        if (Objects.equals(fileName, "package-info.java")) {
            return;
        }
        if (fileName.endsWith("Exception.java")) {
            return;
        }
        if (fileName.endsWith("Tests.java")) {
            return;
        }
        String fullPath = storagePath.toString();
        if (finishedSet.contains(fullPath)) {
            System.out.println("重复了 ！ " + fullPath);
            return;
        }
        boolean translateErr = false;
        List<Comment> allComments = parse.getAllComments();
        for (int i = 0; i < allComments.size(); i++) {
            Comment comment = allComments.get(i);
            String content = comment.getContent();
            if (content.matches("[\\s\\S]*[\\u4e00-\\u9fa5]+[\\s\\S]*")) {
                System.err.println("包含中文， 已经翻译过了吧。。 = " + fullPath);

                finishedSet.add(fullPath);
                translateErr = write(storagePath, translateErr);
//                return;// 可能翻译了部分， 所以还是要看看后面的
                continue;//
            }
            if (content.startsWith("Copyright ")) {
                continue;// 版权信息不需要翻译
            }
            if (content.contains("Copyright ")) {
                continue;// 版权信息不需要翻译
            }
            if (content.startsWith("/* Copyright")) {
                continue;// 版权信息不需要翻译
            }
            if (content.startsWith("/*\n" + " * Copyright")) {
                continue;// 版权信息不需要翻译
            }
//            if (content.contains("/* Copyright")) {
//                continue;// 版权信息不需要翻译
//            }
//            System.out.println("comment = " + content);
            String line = convert2line(content);
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            if (line.length() < 50) {
                // 字符太少， 不翻译了吧
                continue;
            }
//            LineComment lineComment = comment.asLineComment();
//            System.out.println("lineComment = " + lineComment);
//            JavadocComment javadocComment = comment.asJavadocComment();
//            System.out.println("javadocComment = " + javadocComment.getContent());
//            BlockComment blockComment = comment.asBlockComment();
//            System.out.println("blockComment = " + blockComment);
//            Optional<Comment> comment1 = comment.getComment();
//            System.out.println("comment1 = " + comment1);
            String lineCN = null;
            String content1 = line;
            try {
                String o = cachedMap.get(line);
                if (o != null) {
                    lineCN = o;
                } else {
                    int size = cachedMap.size();
                    if (size > 1000) {
                        System.out.println("cachedMap size = " + size);
                    }
                    lineCN = translate(line);
                    if (lineCN == null) {
                        System.out.println("翻译出错: " + lineCN);
                        continue;
                    }
                    cachedMap.put(line, lineCN);

                    if (!line.endsWith("\n")) {
                        line = line + "\n";
                    }
                    content1 = line + "\t * " + lineCN;
                    System.out.println("翻译结果为: " + content1);
                }

            } catch (Exception e) {
                translateErr = true;
                e.printStackTrace();// java.net.SocketTimeoutException: Read timed out
                break;
            }
            comment.setContent(content1);
            TimeUnit.MILLISECONDS.sleep(1000);
//            Optional<Node> commentedNode = comment.getCommentedNode();
//            System.out.println("commentedNode = " + commentedNode);

//            CommentMetaModel metaModel = comment.getMetaModel();
//            String packageName = metaModel.getPackageName();
//            System.out.println("packageName = " + packageName);
//            String qualifiedClassName = metaModel.getQualifiedClassName();
//            System.out.println("qualifiedClassName = " + qualifiedClassName);
        }
//        NodeList<ImportDeclaration> imports = parse.getImports();
//        Iterator<ImportDeclaration> iterator = imports.iterator();
//        for (int i = 0; i < imports.size(); i++) {
//            ImportDeclaration importDeclaration =  imports.get(i);
//            System.out.println("importDeclaration = " + importDeclaration);
//        }
//        JavaParser javaParser = new JavaParser();
//        ParseResult result = javaParser.parse(COMPILATION_UNIT, provider("class X{}"));
//        result.ifSuccessful(cu2 ->
//                        System.out.println("cu = " + cu2)
//                // use cu
//        );
        finishedSet.add(fullPath);
//        finishedMap.put(storagePath.toString(), 123);
        storage.save();// TODO
        checkStopper();// TODO

        translateErr = write(storagePath, translateErr);

        if (translateErr) {
            System.out.println("翻译发生错误，系统退出！" + fileName);
            System.exit(-2);
        }
    }

    private static boolean write(Path storagePath, boolean translateErr) {
        OutputStream fileWriter = null;
        try {
            String fname = "finishedSet_" + moduleName + ".txt";
            fileWriter = new FileOutputStream(fname);
            writeLines(finishedSet, "\n", fileWriter);
            System.out.println("记录已翻译完成的文件 = " + storagePath);
        } catch (Exception e) {
            e.printStackTrace();
            translateErr = true;
        }
        return translateErr;
    }

    private static void checkStopper() {
        String fname = "finishedSet_" + ".txt";
        byte[] byteArr = new byte[1024];
        try {
            read(new FileInputStream(fname), byteArr);
            String s = new String(byteArr);
//            System.out.println("byteArr = " + s);
            if (s.contains("stop")) {
                System.out.println("stop !!  " + s);
                System.exit(-3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseJavaFileOld(CompilationUnit cu) throws InterruptedException {
        CompilationUnit parse = cu;
        Map map = new HashMap();
        List<Comment> allComments = parse.getAllComments();
        LinkedList list = new LinkedList();
        StringBuilder bigLine = new StringBuilder();
        for (int i = 0; i < allComments.size(); i++) {
            Comment comment =  allComments.get(i);
            String content = comment.getContent();
            if (content.startsWith("/* Copyright")) {
                continue;// 版权信息不需要翻译
            }
//            System.out.println("comment = " + content);
            String line = convert2line(content);
            if (bigLine.length() <= 6000) {
                bigLine.append(line).append("\n");
                map.put(comment, line);
            } else {
                String lineCN = translate(bigLine.toString());
                TimeUnit.SECONDS.sleep(1);
                if (!lineCN.endsWith("\n")) {
                    line = line + "\n";
                }
                String content1 = line + "\t * " + lineCN;
                System.out.println("翻译结果为：" + content1);
                comment.setContent(content1);
            }
        }
        Optional<CompilationUnit.Storage> storageOptional = parse.getStorage();
        CompilationUnit.Storage storage = storageOptional.get();
        String fileName = storage.getFileName();
        Path path = storage.getPath();
        System.out.println(path + " :  " + fileName);
        storage.save();
    }

    private static void testConvert2line() {
        String str = "\n" +
                "  // -----------------------------------------------------------------------------------------------\n" +
                "  // Instance fields\n" +
                "  // -----------------------------------------------------------------------------------------------";
        String ret = str.replaceAll("//\\s*\\-+$", "");
        System.out.println("ret = " + ret);
        str = "-----------------------------------------------------------------------------------------------";
        boolean matches = str.matches("-+$");
        System.out.println("matches = " + matches);
    }
    private static void testConvert2line2() {
        String str = "\n" +
                "\t * Configure a path prefix to apply to matching controller methods.\n" +
                "\t * <p>Prefixes are used to enrich the mappings of every {@code @RequestMapping}\n" +
                "\t * method whose controller type is matched by the corresponding\n" +
                "\t * {@code Predicate}. The prefix for the first matching predicate is used.\n" +
                "\t * <p>Consider using {@link org.springframework.web.method.HandlerTypePredicate\n" +
                "\t * HandlerTypePredicate} to group controllers.\n" +
                "\t * @param prefix the path prefix to apply\n" +
                "\t * @param predicate a predicate for matching controller types\n" +
                "\t * @since 5.1";
//        str = "www <p>Prefixes </p>are ya ewe";
        str = "Assist with configuring {@code HandlerMapping}'s with path matching options.";
        String s = convert2line(str);
        System.out.println("去掉注释字符后的结果是：" + s);

        String content = "     * Execute the interaction encapsulated by this operation object.\n" +
                "     * *执行此操作对象封装的交互。";
        content = "     * Constructor that allows use as a JavaBean.\n" +
                "     * *构造函数，它允许用作JavaBean。";
        if (content.matches("[\\s\\S]*[\\u4e00-\\u9fa5]+.*")) { // matches 是指整个字符串的匹配； find 是部分——只要 找到正则表达式能匹配的部分就算
            System.out.println("111 = " + content);
        } else {
            System.out.println("222 = " + content);
        }
    }

    private static String convert2line(String content) {
//        System.out.print("SourceCodeCommentTranslator.convert2line ++++  准备转换 ++++++++ " + content);
        int ind = content.indexOf("* @param ");
        if (ind > -1) {
            // 忽略 @param、 @return、@since、@see 之类的
            content = content.substring(0, ind);
        }
        ind = content.indexOf("* @author ");
        if (ind > -1) {
            // 忽略 @param、 @return、@since、@see 之类的
            content = content.substring(0, ind);
        }
        ind = content.indexOf("* @see");
        if (ind > -1) {
            // 忽略 @param、 @return、@since、@see 之类的
            content = content.substring(0, ind);
        }
        boolean matches = content.matches("\\-+");
        if (matches) {
            return "";
        }
        matches = content.matches("http(s)>:\\[\\w\\d\\.\\?%]-]+");// url 不应该进行翻译  https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.7-300.
        if (matches) {
            return "";
        }

        content = StringUtils.trim(content);
        content = trimBothEndsChars(content,"\n");
        content = trimBothEndsChars(content,"\\*");

        String regex = "\\n*\\s+\\*";
//        String regex = "\\r?\\n\\s+\\*";
        String ret = content.replaceAll(regex, "");
//        ret = content.replaceAll("-------------------------------------------------------------------------", "");
//        ret = ret.replaceAll("--", "");
//        ret = ret.replaceAll("//\\s*\\-+$", "");
//        ret = ret.replaceAll("---------------------------------------------------------------------", "");
        ret = ret.replaceAll("<\\/?\\w+>", "");
        ret = ret.replaceAll("\\{@\\w+ @?(\\w+)\\}", "$1"); // (\w+#?) TODO
//        ret = ret.replaceAll("\\{@\\w+ @?(.\\w)+(\\w+)\\}", "$2");
//        ret = ret.replaceAll("\\{@\\w+ ((\\w+\\.?)+)\\s?(\\w+)\\}", "$1");  TODO
//        ret = ret.replaceAll("\\{@\\w+ ((\\w+\\.)+)\\s?@?(\\w+)\\}", "$2");
        ret = ret.replaceAll("\\{@\\w+ .+\\s@?(\\w+)\\}", "$1");
//        ret = ret.replaceAll(".*", "");
//        ret = ret.replace("<\\w+>[\\s\\S]*", "");
//        System.out.println("SourceCodeCommentTranslator.convert2line ++++  转换结束 ++++++++ ");
        return ret.trim();
    }

    private static String translate(String query) {
//        System.out.print("SourceCodeCommentTranslator.translate ++++  准备翻译 Start -------- ");
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);
//        String query = "高度600米";
//        query = "l love you";
//        query = "The DeepL API is accessible through a REST interface. Unless noted otherwise, all functions support GET and POST methods, although POST requests are recommended. Request parameters are used to pass information to the API, results are returned in JSON. Request parameters must be UTF-8 encoded; results are also encoded in UTF-8. HTTP errors codes are used to signal errors. Authentication is based on fixed keys, and data transport is secured by SSL.";
        String transResult = api.getTransResult(query, "en", "zh");
        if (transResult == null) {

        }
//        ASCIIUtility
//        EncodeUtil
//        String str = "\\u6728";
//        str = ""
        String s = unicodeToString(transResult);
        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONArray trans_result = jsonObject.getJSONArray("trans_result");
        if (trans_result == null) {
            System.out.println("trans_result is null:  " + s + " " + jsonObject);
            return null;
        }
//        trans_result.getJSONObject(0)
//        JSONArray jsonObject = JSONObject.parseArray(s);
//        jsonObject.getJSONObject()
//        System.out.println("jsonObject = " + jsonObject);
//        System.out.println(s);  //木
//        System.out.println(transResult);
        String dst = trans_result.getJSONObject(0).getString("dst");
//        System.out.println(dst);
//        System.out.println("SourceCodeCommentTranslator.translate ++++  翻译 Ending -------- ");
        return dst;
    }

    /**
     * Unicode转 汉字字符串
     *
     * @param str \u6728
     * @return '木' 26408
     */
    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            //group 6728
            String group = matcher.group(2);
            //ch:'木' 26408
            ch = (char) Integer.parseInt(group, 16);
            //group1 \u6728
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;
    }

    /**
     * 去除字符串首尾两端指定的字符
     * */
    public static String trimBothEndsChars(String srcStr, String splitter) {
        String regex = "^" + splitter + "*|" + splitter + "*$";
        return srcStr.replaceAll(regex, "");
    }


}
