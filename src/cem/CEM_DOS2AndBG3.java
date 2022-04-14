package cem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class CEM_DOS2AndBG3 {
	/*
	 * larian框架游戏中英文本合并,适用于神界原罪2和博得之门3
	 * 对于博得之门3省略了version，但不影响使用
	 * 需要准备中英文本，可选修正文本
	 * 中英文本需预处理删除开头的<contentList>或<contentList date="14/10/2020 14:10">和结尾的</contentList>
	 * 待处理完成后再手动加上
	 * 个人修正文件需和中英文本格式相同
	 */
	public static void mergeTranslations() {
		HashMap<String, String> transmap = new HashMap<>();
		HashMap<String, String> transmap2 = new HashMap<>();
		// 英文文本文件位置
		String englishPathName = "F:\\SteamGames\\steamapps\\common\\Baldurs Gate 3\\Data\\Localization\\Chinese\\Chinese\\Localization\\Chinese\\english.xml";
		// 中文文本文件位置
		String chinesePathName = "F:\\SteamGames\\steamapps\\common\\Baldurs Gate 3\\Data\\Localization\\Chinese\\Chinese\\Localization\\Chinese\\chinese.xml";
		//写入的文件位置
		String writePathName = "F:\\SteamGames\\steamapps\\common\\Baldurs Gate 3\\Data\\Localization\\Chinese\\Chinese\\Localization\\Chinese\\ChineseEnglish.xml";
		// 个人修正文本文件位置
		String personalRevisePathName = "E:\\BG3CHNENGMergeChange.xml";
		// 是否存在个人修正文本文件
		boolean personalReviseExist = new File(personalRevisePathName).exists();
		try  {
			BufferedReader br = buffText(englishPathName);
			BufferedReader br2 = buffText(chinesePathName);
			extractIdAndTranslation(transmap, br);

			// 读取对文本内容进行自定义修正的文件
			if (personalReviseExist) { // 存在则读取
				BufferedReader br3 = buffText(personalRevisePathName);
				extractIdAndTranslation(transmap2, br3);
				br3.close();
			}

			FileWriter fw;
			File f = new File(writePathName);
			fw = new FileWriter(f, true);
			PrintWriter pw = new PrintWriter(fw);
			String readedChineseLine;
			while ((readedChineseLine = br2.readLine()) != null) { // 读取中文文本并合并英文，写入新文件
				int indexOfLine = 0;
				StringBuilder uid2 = new StringBuilder();
				StringBuilder trans2 = new StringBuilder();
				while (readedChineseLine.charAt(indexOfLine) != '\"') {
					indexOfLine++;
				} // 定位到第一个双引号前
				indexOfLine++;// 定位到第一个双引号后
				while (readedChineseLine.charAt(indexOfLine) != '\"') { // 定位到uid的第二个双引号前
					uid2.append(readedChineseLine.charAt(indexOfLine)); // 将uid存入uid2
					indexOfLine++;
				}
				while (readedChineseLine.charAt(indexOfLine) != '>') {// 定位到下一个>前
					indexOfLine++;
				}
				indexOfLine++;// 定位到下一个>后
				while (readedChineseLine.charAt(indexOfLine) != '<') { // 定位到下一个<前
					if (indexOfLine == readedChineseLine.length() - 1) { // 如果文本有换行，则读取下一行
						trans2.append(readedChineseLine.charAt(indexOfLine));
						trans2.append("\n");
						indexOfLine = 0;
						readedChineseLine = br2.readLine();
						while (readedChineseLine.length() == 0) {
							trans2.append("\n");
							readedChineseLine = br2.readLine();
						}
						trans2.append(readedChineseLine.charAt(indexOfLine));
					} else {// 否则
						trans2.append(readedChineseLine.charAt(indexOfLine));// 直接将uid对应的文本内容存入trans2
					}
					indexOfLine++;
				}

				String merge = "	<content contentuid=\"" + uid2 + "\">";
				if (personalReviseExist && transmap2.get(uid2.toString()) != null) {// 如果在修正文件中找到了此uid
					merge += transmap2.get(uid2.toString());
				} else {
					String trans4 = transmap.get(uid2.toString()); // 从map中把对应的英文文本取出
					merge += trans2 + "(" + trans4 + ")";
				}
				merge += "</content>";
//				System.out.println(merge);
				pw.println(merge);
			}
			pw.flush();
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void extractIdAndTranslation(HashMap<String, String> transmap, BufferedReader br) throws IOException {
		String readedReviseLine;
		while ((readedReviseLine = br.readLine()) != null) { // 读取修正文本
			int indexOfLine = 0;
			StringBuilder uid = new StringBuilder();
			StringBuilder trans = new StringBuilder();
			while (readedReviseLine.charAt(indexOfLine) != '\"') {
				indexOfLine++;
			} // 定位到第一个双引号前
			indexOfLine++;// 定位到第一个双引号后
			while (readedReviseLine.charAt(indexOfLine) != '\"') { // 定位到uid的第二个双引号前
				uid.append(readedReviseLine.charAt(indexOfLine)); // 将uid存入uid
				indexOfLine++;
			}
			while (readedReviseLine.charAt(indexOfLine) != '>') {// 定位到下一个>前
				indexOfLine++;
			}
			indexOfLine++;// 定位到下一个>后
			while (readedReviseLine.charAt(indexOfLine) != '<') {// 定位到下一个<前
				if (indexOfLine == readedReviseLine.length() - 1) { // 如果文本有换行，则读取下一行
					trans.append(readedReviseLine.charAt(indexOfLine));
					trans.append("\n");
					indexOfLine = 0;
					readedReviseLine = br.readLine();
					while (readedReviseLine.length() == 0) {
						trans.append("\n");
						readedReviseLine = br.readLine();
					}
					trans.append(readedReviseLine.charAt(indexOfLine));
				} else { // 否则
					trans.append(readedReviseLine.charAt(indexOfLine));// 直接将uid对应的文本内容存入trans
				}
				indexOfLine++;
			}
			transmap.put(uid.toString(), trans.toString()); // 将uid与文本存入map
		}
	}

	public static BufferedReader buffText(String pathName) throws IOException {
		FileReader reader = new FileReader(pathName);
		return new BufferedReader(reader);
	}

	public static void main(String[] args) {
		long start,end;
		start = System.currentTimeMillis();
		mergeTranslations();
		end = System.currentTimeMillis();
		System.out.println("合并完成。运行时间:" + (end - start) + "ms");
	}
}
