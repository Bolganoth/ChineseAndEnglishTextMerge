package cem;

import csvtools.CsvReader;
import csvtools.CsvWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CEM_Armello {

    public static void mergeTranslations() {
        String chineseFile = "F:\\SteamGames\\steamapps\\common\\Armello\\armello_Data\\Export\\TextAsset\\SimplifiedChinese.txt";
        String englishFile = "F:\\SteamGames\\steamapps\\common\\Armello\\armello_Data\\Export\\TextAsset\\English.txt";
        String outputFile = "F:\\SteamGames\\steamapps\\common\\Armello\\armello_Data\\Unity_Assets_Files\\resources\\merge.txt";
        CEMTool cemt = new CEMTool();
        HashMap<String, String> transMap = new HashMap<>();
        File f = new File(outputFile);
        CsvReader csvrChinese, csvrEnglish;
        CsvWriter csvw;
        try {
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            csvrChinese = new CsvReader(chineseFile, ',', StandardCharsets.UTF_8);
            csvrEnglish = new CsvReader(englishFile, ',', StandardCharsets.UTF_8);
            csvrEnglish.readRecord();
            while (csvrEnglish.readRecord()) { // 读取英文
                String[] line = csvrEnglish.getValues();
                line[1] = line[1].replaceAll("\"", "\"\"");
                transMap.put(line[0], line[1]);
            }
            csvw = new CsvWriter(outputFile, ',', StandardCharsets.UTF_8);
            csvrChinese.readRecord();
            String[] value = csvrChinese.getValues();
            csvw.writeRecord(value); //写标题
            while (csvrChinese.readRecord()) { //读取中文
                String[] line = csvrChinese.getValues();
                if (transMap.containsKey(line[0])) {
                    line[1] = line[1].replaceAll("\"", "\"\"");
                    line[1] = "\"" + cemt.optimizedMerge(line[1], transMap.get(line[0])) + "\"";
                }
                String merge = line[0] + "," + line[1];
                pw.println(merge);
            }
            csvrChinese.close();
            csvrEnglish.close();
            csvw.close();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mergeTranslations();
    }
}
