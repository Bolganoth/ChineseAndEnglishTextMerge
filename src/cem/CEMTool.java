package cem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 中英合并工具类，用于辅助中英文本的合并
 */
public class CEMTool {

    /**
     * 基础合并
     */
    public String merge(String translation, String originalText) {
        return translation + "(" + originalText + ")";
    }

    /**
     * 优化合并：
     * 文本相同不合并
     * 文本中含有换行则合并后中英文本之间也有换行
     */
    public String optimizedMerge(String translation, String originalText) {
        if (translation.equals(originalText)) {
            return translation;
        }
        if(translation.contains("\\n")){
            return translation + "\\n(" + originalText + ")";
        }
        return merge(translation, originalText);
    }

    /**
     * 中英冒号和括号优化
     * 如 他说：(He said:)（XXX)
     * 将会替换为 他说(He said):（XXX)
     */
    public String colonOptimize(String mergedText) {
        if (mergedText.matches(".*：\\(.*:\\).*")) {
            mergedText = mergedText.replaceAll("：\\(", "(");
            mergedText = mergedText.replaceAll(":\\)", "):");
        }
        return mergedText;
    }

    /**
     * html标签优化，目前只有b和i
     */
    public String chinesePunctuationToEnglish(String mergedText) {
        mergedText = mergedText.replaceAll("！", "!");
        mergedText = mergedText.replaceAll("—", "-");
        mergedText = mergedText.replaceAll("？", "?");
        mergedText = mergedText.replaceAll("…", "...");
        return mergedText;
    }

    /**
     * html标签优化，目前只有b和i
     */
    public String htmlTagsOptimize(String mergedText){
        mergedText = mergedText.replaceAll("\\{/b}\\(\\{b}", "(");
        mergedText = mergedText.replaceAll("\\{/i}\\(\\{i}", "(");
        mergedText = mergedText.replaceAll("\\{/b}\\)", "){/b}");
        mergedText = mergedText.replaceAll("\\{/i}\\)", "){/i}");
        return mergedText;
    }

    /**
     * 双重括号优化
     */
    public String doubleBracketOptimize(String mergedText){
        mergedText = mergedText.replaceAll("\\(\\(", "(");
        mergedText = mergedText.replaceAll("\\)\\)", ")");
        return mergedText;
    }

    /**
     * 判断文本是否是全Ascii码
     */
    public boolean textIsAllAsciiCode(String text) {
        boolean tiaac = true;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 128) {
                tiaac = false;
                break;
            }
        }
        return tiaac;
    }

    public BufferedReader buffText(String pathName) throws IOException {
        FileReader reader = new FileReader(pathName);
        return new BufferedReader(reader);
    }

    /**
     * 去除HTML标签
     */
    public String removeHTMLTags(String input) {
        String regex = "<[^>]+>";
        return input.replaceAll(regex, "");
    }

    public boolean containsModifierParameters(String text, String[] InputParameters) {
        boolean contains = false;
        for (String modifierParameter : InputParameters) {
            if (text.contains(modifierParameter)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * 处理带有参数的文本
     */
    public String dealWithStringsWithParameters(String translation, String originalText, String[] InputParameters,
                                                boolean paramsHaveBoldTag) {
        int parametersCount = 0; //文本中包含的预设参数的数量
        HashMap<Integer, String> parameterMap = new HashMap<>(); //映射
        String[] parameters = new String[InputParameters.length];
        int[] parametersIndex = new int[InputParameters.length];
        for (String parameter : InputParameters) { //在当前英文文本中查找有哪些预设参数
            if (originalText.contains(parameter)) {
                parametersIndex[parametersCount] = originalText.indexOf(parameter);
                parameterMap.put(parametersIndex[parametersCount], parameter);
                parametersCount++;
            }
        }
        Arrays.sort(parametersIndex, 0, parametersCount);
        for (int i = 0; i < parametersCount; i++) {
            parameters[i] = parameterMap.get(parametersIndex[i]);
        }
        //以上，将参数按在文本中出现的顺序排好，放入parameters数组中
        String[] splitTrans = new String[2 * parametersCount + 1];
        for (int i = 0; i < parametersCount; i++) {
            String sp;
            if (i == 0) {
                sp = originalText;
            } else {
                sp = splitTrans[2 * i];
            }
            int preTextEnd = sp.indexOf(parameters[i]);
            preTextEnd--;
            int nextTextStart = preTextEnd + parameters[i].length();
            nextTextStart++;
            while (preTextEnd >= 0 && sp.charAt(preTextEnd) == ' ') {
                preTextEnd--;
            }
            while (nextTextStart < sp.length() && sp.charAt(nextTextStart) == ' ') {
                nextTextStart++;
            }
            splitTrans[i * 2] = sp.substring(0, preTextEnd + 1);
            String boldTagedParameter = "<b>" + parameters[i] + "</b>";
            if (paramsHaveBoldTag && translation.contains(boldTagedParameter)) { //如果有<b>标签则算入
                splitTrans[i * 2 + 1] = boldTagedParameter;
            } else {
                splitTrans[i * 2 + 1] = parameters[i];
            }
            splitTrans[i * 2 + 2] = sp.substring(nextTextStart);
        }
        StringBuilder merge = new StringBuilder();
        int cStart = 0, cEnd, j;
        //以上，将英文文本按照参数切分成文本参数文本参数间隔的形式存入splitTrans
        for (j = 0; j < parametersCount; j++) {
            cEnd = translation.indexOf(splitTrans[2 * j + 1]);
            if (cEnd < cStart) {
                return "ChnAndEngGrammarOrderFail";
            }
            merge.append(translation, cStart, cEnd).append("(").append(splitTrans[2 * j]).append(")").append(splitTrans[2 * j + 1]);
            cStart = cEnd + splitTrans[2 * j + 1].length();
        }
        //以上，按顺序读中文文本，遇到参数，在之前添加英文翻译
        merge.append(translation.substring(cStart)).append("(").append(splitTrans[2 * j]).append(")"); //文本尾部处理
        merge = new StringBuilder(merge.toString().replaceAll("\\(\\)", ""));
        return merge.toString().replaceAll("\\(.\\)", ""); //删除不必要的元素
    }
}
