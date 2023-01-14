package cem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class CEM_RememberTheFlowers { //懒货完全不想检查有没有问题
    public static void mergeTranslations() {
        String chineseFile = "D:\\Furry\\RemembertheFlowers\\game\\tl\\chinese\\script.rpy";
        String englishFile = "D:\\Furry\\RemembertheFlowers\\game\\script.rpy";
        String outputFile = "D:\\Furry\\RemembertheFlowers\\game\\tl\\chinese\\script1.rpy";
        CEMTool cemt = new CEMTool();
        HashMap<Integer, String> transMap = new HashMap<>();
        File f = new File(outputFile);
        try {
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            BufferedReader englishBR = cemt.buffText(englishFile);
            BufferedReader chineseBR = cemt.buffText(chineseFile);
            extractIdAndTranslation(transMap, englishBR);
            pw.println(chineseBR.readLine());
            pw.println(chineseBR.readLine());
            String readLine;
            while ((readLine = chineseBR.readLine()).contains("# game/script.rpy:")) {
                int correspondingEnglishIndex = Integer.parseInt(readLine.replace("# game/script.rpy:", ""));
                pw.println(readLine);
                pw.println(chineseBR.readLine());
                pw.println(chineseBR.readLine());
                pw.println(chineseBR.readLine());
                StringBuilder chineseText = new StringBuilder(chineseBR.readLine());
                chineseBR.readLine();
                String englishText = transMap.get(correspondingEnglishIndex);
                if (chineseText.toString().equals(englishText)) {
                    pw.println(chineseText);
                    pw.println();
                    continue;
                }
                int engStartIndex = 0;
                while (englishText.charAt(engStartIndex) != '"') {
                    engStartIndex++;
                }
                int engEndIndex = englishText.length() - 1;
                while (englishText.charAt(engEndIndex) != '"') {
                    engEndIndex--;
                }
                String english = englishText.substring(engStartIndex + 1, engEndIndex);

                int chnEndIndex = chineseText.toString().length() -1;
                while (chineseText.charAt(chnEndIndex) != '"') {
                    chnEndIndex--;
                }
                String needAppend = chineseText.substring(chnEndIndex+1, chineseText.toString().length());
                chineseText.delete(chnEndIndex, chineseText.length());
                chineseText.append("(").append(english).append(")").append("\"").append(needAppend);
                pw.println(chineseText);
                pw.println();
            }

            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void extractIdAndTranslation(HashMap<Integer, String> transMap, BufferedReader br) throws IOException {
        String readLine;
        int lineIndex = 1;
        while ((readLine = br.readLine()) != null) {
            transMap.put(lineIndex, readLine);
            lineIndex++;
        }
    }

    public static void main(String[] args) {
        mergeTranslations();
    }
}
