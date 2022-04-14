package cem;

import java.io.*;

public class CEM_PathfinderWofRttt { //ttt中英文本合并
    public static void mergeTranslations() {
        CEMTool cem = new CEMTool();
        String pathName = "E:\\SteamGames\\steamapps\\common\\Pathfinder Second Adventure\\Mods\\TabletopTweaks\\Localization\\LocalizationPack.json";
        String writePathName = "E:\\SteamGames\\steamapps\\common\\Pathfinder Second Adventure\\Mods\\TabletopTweaks\\Localization\\LocalizationPack1.json";
        try {
            BufferedReader br = cem.buffText(pathName);
            FileWriter fw;
            File f = new File(writePathName);
            fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            String readLine;
            String english = "";
            String merge;
            while ((readLine = br.readLine()) != null) {
                if (readLine.length() >= 12) {
                    String idf = readLine.substring(7, 11);
                    if ("enGB".equals(idf)) {
                        english = readLine.substring(15, readLine.length() - 2);
                    }
                    if ("zhCN".equals(idf)) {
//                        System.out.println(readLine.substring(14, readLine.length() - 1));
                        if (!readLine.substring(14, readLine.length() - 1).equals("null")) {
                            merge = readLine.substring(0, readLine.length() - 2);
                            if (merge.contains("\\n")) { //如果文本中有换行，英文开始的一行也要换行
                                merge += "\\n";
                            }
                            merge += "(" + english + ")\",";
                            merge = merge.replaceAll("（", "(");
                            merge = merge.replaceAll("）", ")");
                            merge = merge.replaceAll("此变体来自Mod: TableTopTweaks，译者：1onepower。", "");
                            readLine = merge;
                        }
                    }
                }
                pw.println(readLine);
            }
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mergeTranslations();
    }
}
