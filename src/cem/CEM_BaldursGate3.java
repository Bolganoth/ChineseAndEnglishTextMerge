package cem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CEM_BaldursGate3 {
    /*
     * larian框架游戏中英文本合并,适用于博得之门3
     * 尽量不要用于神界原罪2
     * 需要准备中英文本，可选修正文本
     * 相对于上一个版本的代码，此处读取xml采用了dom的方式
     * 并且开头结尾的contentList标签不需手动删除添加
     * 博得之门3的version属性也得以保留，且优化了代码，运行速度加快
     * 个人修正文件需和中英文本格式相同
     */

    public static void mergeTranslations() {
        //英文文本文件位置
        String englishPathName = "F:\\Baldur's Gate 3 Backup\\english.xml";
        //中文文本文件位置
        String chinesePathName = "F:\\Baldur's Gate 3 Backup\\chinese.xml";
        //写入的文件位置
        String writePathName = "F:\\Baldur's Gate 3 Backup\\chinese_override.xml";
        //个人修正文本文件位置
        String personalRevisePathName = "src\\data\\BG3CHNENGMergeChange.xml";
        //是否存在个人修正文本文件
        boolean personalReviseExist = new File(personalRevisePathName).exists();
        FileWriter fw;
        PrintWriter pw;
        CEMTool cem = new CEMTool();
        String[] parameters = {"[1]", "[2]", "[3]", "[4]", "[5]", "[6]", "[7]"};
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            HashMap<String, String> transMap = new HashMap<>(); //英文文本保存的map
            HashMap<String, String> reviseMap = null; //个人修正文本保存的map
            Document chineseDocument = builder.parse(chinesePathName); //读取后的中文文件
            Document englishDocument = builder.parse(englishPathName); //读取后的英文文件
            NodeList chineseDocumentList = chineseDocument.getElementsByTagName("content"); //读取后的中文内容list
            NodeList englishDocumentList = englishDocument.getElementsByTagName("content"); //读取后的英文内容list
            for (int e = 0; e < englishDocumentList.getLength(); e++) { //将英文uid和文本存入map便于查询
                Element englishElement = (Element) englishDocumentList.item(e);
                transMap.put(englishElement.getAttribute("contentuid"), englishElement.getTextContent());
            }
            if (personalReviseExist) { //存在个人修正文本则读取并存入修正的map
                reviseMap = new HashMap<>();
                Document reviseDocument = builder.parse(personalRevisePathName);
                NodeList reviseDocumentList = reviseDocument.getElementsByTagName("content");
                for (int r = 0; r < reviseDocumentList.getLength(); r++) {
                    Element reviseElement = (Element) reviseDocumentList.item(r);
                    reviseMap.put(reviseElement.getAttribute("contentuid"), reviseElement.getTextContent());
                }
            }
            File f = new File(writePathName);
            fw = new FileWriter(f, true);
            pw = new PrintWriter(fw);
            String englishText;
            String chineseText;
            String mergedText;
            String mergedCoreText;
            pw.println("<contentList>"); //开头写入一个<contentList>
            for (int i = 0; i < chineseDocumentList.getLength(); i++) { //将中文文本取出与英文文本合并再存入文件
                Element chineseElement = (Element) chineseDocumentList.item(i); //取出i位置的一行元素
                String contentuid = chineseElement.getAttribute("contentuid"); //取出当前行的contentuid的属性值
                englishText = transMap.get(contentuid); //从英文map中根据uid取出对应的英文文本
                mergedText = "	<content contentuid=\"" + contentuid + "\""; //合并的文本
                mergedText += " version=\"" + chineseElement.getAttribute("version") + "\"";
                mergedText += ">";
                if (personalReviseExist && reviseMap.get(contentuid) != null) { //如果有个人修正文本则进行修正
                    mergedCoreText = reviseMap.get(contentuid);
                    mergedCoreText = htmlCharChange(mergedCoreText);
                    mergedText += mergedCoreText;
                } else { //无则将原来的中英文本组合
                    boolean containsModifierParameters = cem.containsModifierParameters(englishText, parameters);
                    chineseText = chineseElement.getTextContent();
                    if (containsModifierParameters) {
                        mergedCoreText = cem.dealWithStringsWithParameters(chineseText, englishText, parameters, false);
                        mergedCoreText = cem.chinesePunctuationToEnglish(mergedCoreText);
                        if (mergedCoreText.equals("ChnAndEngGrammarOrderFail")) {
                            chineseText = cem.chinesePunctuationToEnglish(chineseText);
                            mergedCoreText = cem.optimizedMerge(chineseText, englishText);
                            System.out.println(contentuid);
                        }
                    } else {
                        if (englishText == null) {
                            englishText = chineseText;
                        }
                        chineseText = cem.chinesePunctuationToEnglish(chineseText);
                        mergedCoreText = cem.optimizedMerge(chineseText, englishText);
                    }
                    mergedCoreText = cem.htmlTagsOptimize(mergedCoreText);
                    mergedCoreText = cem.chinesePunctuationToEnglish(mergedCoreText);
                    mergedCoreText = htmlCharChange(mergedCoreText);
                    mergedText += mergedCoreText;
                    mergedText = cem.colonOptimize(mergedText);
                    mergedText = cem.doubleBracketOptimize(mergedText);
                    mergedText = cem.htmlTagsOptimize(mergedText);
                }
                mergedText += "</content>";
//                System.out.println(mergedText); //此处解除注释可以输出合并的文本检查
                pw.println(mergedText); //写入进xml
                pw.flush();
                fw.flush();
            }
            pw.println("</contentList>"); //最后加上</contentList>
            pw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String htmlCharChange(String text) {
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        return text;
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        long start, end;
        start = System.currentTimeMillis();
        mergeTranslations();
        end = System.currentTimeMillis();
        System.out.println("合并完成。运行时间:" + (end - start) + "ms");
    }
}