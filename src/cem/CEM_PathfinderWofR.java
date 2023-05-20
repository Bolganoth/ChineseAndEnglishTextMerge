package cem;

import java.io.*;
import java.util.HashMap;

/*
 * 开拓者正义之怒中英文本合并
 * 需要准备中英文本，可选修正文本
 * 个人修正文件需和中英文本格式相同
 */

public class CEM_PathfinderWofR {
    public static void mergeTranslations() {

        CEMTool cemTool = new CEMTool();
        HashMap<String, String> transmapEng = new HashMap<>();
        HashMap<String, String> transmapRev = new HashMap<>();
        //可能包含的游戏读取的参数
        String[] modifierParameters = {"{0}", "{1}", "{2}", "{source}", "{target}", "{count}", "{d20}", "{text}", "{description}", "{dc}", "{d100}"};
        // 英文文本文件位置
        String englishPathName = "E:\\SteamGames\\steamapps\\common\\Pathfinder Second Adventure\\Wrath_Data\\StreamingAssets\\enGB.json";
        // 中文文本文件位置
        String chinesePathName = "E:\\SteamGames\\steamapps\\common\\Pathfinder Second Adventure\\Wrath_Data\\StreamingAssets\\zhCN.json";
        //写入的文件位置
        String writePathName = "E:\\SteamGames\\steamapps\\common\\Pathfinder Second Adventure\\Wrath_Data\\StreamingAssets\\Localization\\zhCN1.json";
        // 个人修正文本文件位置，不存在则随意填写
        String personalRevisePathName = "E:\\SteamGames\\steamapps\\common\\Pathfinder Second Adventure\\Wrath_Data\\StreamingAssets\\personalRevisedText.json";

//        // 英文文本文件位置
//        String englishPathName = System.getProperty("user.dir") + "\\enGB.json";
//        // 中文文本文件位置
//        String chinesePathName = System.getProperty("user.dir") + "\\zhCN.json";
//        //写入的文件位置
//        String writePathName = System.getProperty("user.dir") + "\\zhCN1.json";
//        // 个人修正文本文件位置，不存在则随意填写
//        String personalRevisePathName = System.getProperty("user.dir") + "\\pRT.json";

        try {
            BufferedReader br = cemTool.buffText(englishPathName);
            BufferedReader br2 = cemTool.buffText(chinesePathName);
            // 读取对文本内容进行自定义修正的文件
            extractIdAndTranslation(transmapEng, br); //将英文文本提取到map中
            System.out.println("已将英文文件存入map");
            if (new File(personalRevisePathName).exists()) { //如果存在个人修正文件
                System.out.println("修正文件存在...");
                BufferedReader br3 = cemTool.buffText(personalRevisePathName);
                extractIdAndTranslation(transmapRev, br3); //将修正文本提取到map2中
                System.out.println("已将修正文件存入map");
                br3.close();
            }

            File wf = new File(writePathName); // 创建合并文件
            FileWriter fileWriter = new FileWriter(wf);
            PrintWriter pw = new PrintWriter(fileWriter);
            //写文本开头
            pw.println("{");
            pw.println("  \"$id\": \"1\",");
            pw.println("  \"strings\": {");

            String readChineseLine;
            br2.readLine();
            br2.readLine();
            br2.readLine(); //排除前三行
            boolean existGrammarProblem = false; //全局：是否存在语法问题
            System.out.println("正在处理中文文件...");
            while ((readChineseLine = br2.readLine()) != null) { // 读取中文文本并合并英文，写入新文件
                if ("  }".equals(readChineseLine) || "}".equals(readChineseLine)) { //排除结尾大括号
                    continue;
                }
                String chineseUid;
                String chineseTranslation;
                String[] chineseTranslationArray = breakUidAndTranslation(readChineseLine);
                chineseUid = chineseTranslationArray[0];
                chineseTranslation = chineseTranslationArray[1];
                String merge = "    \"" + chineseUid + "\": \"";
                if (transmapRev.get(chineseUid) != null) { // 修正文本中能找到
                    merge += transmapRev.get(chineseUid);
                } else { //否则正常合并
                    boolean containsModifierParameters = cemTool.containsModifierParameters(chineseTranslation, modifierParameters);
                    boolean grammarProblem = false;
                    if (containsModifierParameters) {
                        String result = cemTool.dealWithStringsWithParameters(chineseTranslation, cemTool.removeHTMLTags(transmapEng.get(chineseUid)), modifierParameters, true);
                        if ("ChnAndEngGrammarOrderFail".equals(result)) {
                            grammarProblem = true;
                            if(!existGrammarProblem){
                                existGrammarProblem = true;
                                System.out.println("中英语法顺序不同导致参数处理错误，已按基础合并方式合并，其uid为: ");
                            }
                            System.out.println(chineseUid);
                        } else {
                            merge += result;
                        }
                    }
                    if (!containsModifierParameters || grammarProblem) { // 如果不包含参数或有语法错误，则正常合并
                        merge += cemTool.optimizedMerge(chineseTranslation, transmapEng.get(chineseUid)); //使用优化的合并
                    }
                }
                if ("54007774-0ba7-4bf5-89a7-7edd63264d15".equals(chineseUid)) { //json最后一项不加逗号
                    merge += "\"";
                } else {
                    merge += "\",";
                }

                //处理标点符号的问题
                merge = cemTool.colonOptimize(merge);
                merge = merge.replaceAll("（", "(");
                merge = merge.replaceAll("）", ")");
                merge = merge.replaceAll(":\\{0}", ": {0}");
                merge = merge.replaceAll("\\):\\{", "): {");
                merge = merge.replaceAll("\\):<", "): <");

//                System.out.println(merge);
                pw.println(merge);
            }
            if(!existGrammarProblem){
                System.out.println("未找到中英语法问题");
            }
            pw.println("  }");
            pw.print("}");
            pw.flush();
            fileWriter.flush();
            pw.close();
            fileWriter.close();
            System.out.println("合并完成");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void extractIdAndTranslation(HashMap<String, String> transmap, BufferedReader br) throws IOException { //提取id和翻译到map中去
        br.readLine();
        br.readLine();
        br.readLine();
        String readLine;
        while ((readLine = br.readLine()) != null) { // 读取文本
            if ("  }".equals(readLine) || "}".equals(readLine)) { //排除结尾大括号
                continue;
            }
            String[] uidAndTrans = breakUidAndTranslation(readLine);
            transmap.put(uidAndTrans[0], uidAndTrans[1]); // 将uid与文本存入map
        }
    }

    public static String[] breakUidAndTranslation(String text){ //分离uid和翻译
        String[] result = new String[2];
        result[0] = text.substring(5, 41);
        result[1] = text.substring(45, text.length() - 2);
        return result;
    }

    public static void main(String[] args) {
        long start, end;
        start = System.currentTimeMillis();
        mergeTranslations();
        end = System.currentTimeMillis();
        System.out.println("运行时间:" + (end - start) + "ms");
    }
}
