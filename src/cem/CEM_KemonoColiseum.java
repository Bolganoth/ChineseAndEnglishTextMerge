package cem;

import csvreader.CsvReader;
import csvreader.CsvWriter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CEM_KemonoColiseum {

    public static void mergeTranslations() {
        String csvFilePath = "D:\\Furry\\kemo-coliseum-patrons\\new\\ss\\";
        String csvWriteFilePath = "D:\\Furry\\kemo-coliseum-patrons\\new\\sw\\";
        CEMTool cemt = new CEMTool();
        File file = new File(csvFilePath);
        String[] fileList = file.list();
        CsvReader csvr;
        CsvWriter csvw;
        try {
            for (String csvFile : Objects.requireNonNull(fileList)) {
                csvr = new CsvReader(csvFilePath+csvFile, ',', StandardCharsets.UTF_8);
                new File(csvWriteFilePath+csvFile);
                csvw = new CsvWriter(csvWriteFilePath+csvFile, ',', StandardCharsets.UTF_8);
                csvr.readRecord();
                String[] value=csvr.getValues();
                csvw.writeRecord(value); //写标题
                while (csvr.readRecord()) {
                    String[] line = csvr.getValues();
                    if (cemt.textIsAllAsciiCode(line[3]) && line[3].length() != 0) {
                        line[3] = cemt.merge(line[3], line[2]);
                    }
                    csvw.writeRecord(line);
                }
                csvw.close();
                csvr.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mergeTranslations();
    }
}
