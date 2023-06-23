package cem;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CEM_AHellishJourney {
    public static void mergeTranslations(String chineseFile, String englishFile, String outputFile) {
        CEMTool cemTool = new CEMTool();
        HashMap<String, String> transMap = new HashMap<>();
        File f = new File(outputFile);
        try {
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            BufferedReader englishBR = cemTool.buffText(englishFile);
            BufferedReader chineseBR = cemTool.buffText(chineseFile);
            extractIdAndTranslation(transMap, englishBR);

            String regexId = "^ *translate schinese (\\S+): *$";
            Pattern patternId = Pattern.compile(regexId);
            String regexChineseText = "^ {4}(.*)\"(.*)\" *$";
            Pattern patternChineseText = Pattern.compile(regexChineseText);
            Matcher matcher;
            String id;
            String readLine;
            while ((readLine = chineseBR.readLine()) != null) { //碰到translate schinese strings:，说明要开始单独执行old和new部分的双语合并，结束此循环
                if (readLine.matches("^ *translate schinese strings: *$")) {
                    pw.println("translate schinese_english strings:");
                    pw.println();
                    break;
                }

                if (!(matcher = patternId.matcher(readLine)).find()) {//寻找下一个id
                    continue;
                }
                String chineseEnglishTranslateIdText = readLine.replaceAll("schinese", "schinese_english");//将id中的schinese替换为schinese_english，让其识别为另一种语言
                pw.println(chineseEnglishTranslateIdText); //写入translate schinese_english xxx:
                id = matcher.group(1);
                while ((readLine = chineseBR.readLine()) != null) { //寻找此id对应的文本
                    matcher = patternChineseText.matcher(readLine);
                    if (matcher.find()) {
                        break;
                    } else {
                        pw.println();
                    }
                }
                assert readLine != null;
                String chineseText = cemTool.chinesePunctuationToEnglish(matcher.group(2)); //中文标点符号英文化
                String merged;
                String englishText = transMap.get(id); //从英文map中根据id获取英文文本
                if (chineseText.equals(englishText) || cemTool.textIsAllAsciiCode(chineseText)) { //中英文本相同则不处理
                    pw.println("    " + matcher.group(1) + "\"" + chineseText + "\"");
                    pw.println();
                    pw.println();
                    continue;
                }
                merged = "    " + matcher.group(1) + "\"" + cemTool.merge(chineseText, englishText) + "\""; //中英文本不同则合并
                merged = cemTool.htmlTagsOptimize(merged); //优化html标签
                merged = cemTool.doubleBracketOptimize(merged); //优化双重括号
                pw.println(merged);
                pw.println();
                pw.println();
            }

            //处理余下的单独用Old和New记录的String
            String regexOld = "^.*old.*\"(.*)\" *$";
            Pattern patternOld = Pattern.compile(regexOld);
            String regexNew = "^.*new.*\"(.*)\" *$";
            Pattern patternNew = Pattern.compile(regexNew);
            String oldText;
            String newText;
            String merged;
            while ((readLine = chineseBR.readLine()) != null) {
                matcher = patternOld.matcher(readLine);
                if (!matcher.find()) { //寻找下一个Old
                    continue;
                }
                oldText = matcher.group(1);
                pw.println(readLine);
                readLine = chineseBR.readLine();
                matcher = patternNew.matcher(readLine);
                while (!matcher.find()) { //寻找下一个new
                    readLine = chineseBR.readLine();
                    matcher = patternNew.matcher(readLine);
                }
                newText = cemTool.chinesePunctuationToEnglish(matcher.group(1)); //中文标点符号英文化

                //临时加的，目前只看到这一个地方有参数调用，所以目前没有新增个人修正文件，目前看来这游戏的文本含参量不需要
                if (oldText.equals("Page {}")) {
                    merged = "    new \"页(Page) {}\"";
                    pw.println(merged);
                    pw.println();
                    pw.println();
                    continue;
                }


                if (oldText.equals(newText) || cemTool.textIsAllAsciiCode(newText)) { //中英重复不处理，不重复合并
                    merged = "    new \"" + oldText + "\"";
                } else {
                    merged = "    new \"" + cemTool.merge(newText, oldText) + "\"";
                }
                merged = cemTool.htmlTagsOptimize(merged); //优化html标签
                merged = cemTool.doubleBracketOptimize(merged);
                pw.println(merged);
                pw.println();
                pw.println();
            }

            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void extractIdAndTranslation(HashMap<String, String> transMap, BufferedReader br) throws IOException { //提取英文文本到map中
        String readLine;
        Pattern pattern;
        Matcher matcher;
        String regexId = "^ *translate schinese_english (\\S+): *$";
        String regexText = "^ *#.*\"(.*)\" *$";
        String id;
        String text;
        while ((readLine = br.readLine()) != null) {
            if (readLine.matches("^ *translate schinese_english strings: *$")) {
                break;
            }
            pattern = Pattern.compile(regexId);
            matcher = pattern.matcher(readLine);
            if (matcher.find()) {
                id = matcher.group(1);  // 组提取字符串
            } else {
                continue;
            }
            pattern = Pattern.compile(regexText);
            while ((readLine = br.readLine()) != null) {
                matcher = pattern.matcher(readLine);
                if (matcher.find()) {
                    text = matcher.group(1);
                    transMap.put(id, text);
                    break;
                }

            }


        }
    }

    public static void main(String[] args) {
        String chineseFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese\\script.rpy";
        String englishFile = "D:\\Furry\\A Hellish Journey Backup\\english\\script.rpy";
        String outputFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese_english\\script.rpy";
        mergeTranslations(chineseFile, englishFile, outputFile);
        chineseFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese\\extras.rpy";
        englishFile = "D:\\Furry\\A Hellish Journey Backup\\english\\extras.rpy";
        outputFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese_english\\extras.rpy";
        mergeTranslations(chineseFile, englishFile, outputFile);
        chineseFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese\\options.rpy";
        englishFile = "D:\\Furry\\A Hellish Journey Backup\\english\\options.rpy";
        outputFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese_english\\options.rpy";
        mergeTranslations(chineseFile, englishFile, outputFile);
        chineseFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese\\screens.rpy";
        englishFile = "D:\\Furry\\A Hellish Journey Backup\\english\\screens.rpy";
        outputFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese_english\\screens.rpy";
        mergeTranslations(chineseFile, englishFile, outputFile);
        chineseFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese\\common.rpy";
        englishFile = "D:\\Furry\\A Hellish Journey Backup\\english\\common.rpy";
        outputFile = "D:\\Furry\\A Hellish Journey\\game\\tl\\schinese_english\\common.rpy";
        mergeTranslations(chineseFile, englishFile, outputFile);
    }
}
