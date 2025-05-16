package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_SERIALNUMBER = "売上ファイル名が連番になっていません";
	private static final String OVER_DIGITS = "合計金額が10桁を超えました";
	private static final String FILE_CODE_INVALID = "の支店コードが不正です";
	private static final String VALUE_FILE_INVALID_FORMAT = "のフォーマットが不正です";


	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 配列filesに、下記パスのファイルをすべて格納
		File[] files = new File(args[0]).listFiles();
		// 売上ファイルのみ保持するリストrcdFilesを作成
		List<File> rcdFiles = new ArrayList<>();
		// 配列filesに入っているファイルの数だけ繰り返す  ファイル名がマッチした売上ファイルをリストrcdFilesに格納
		for(int i = 0; i < files.length ; i++) {
			if(files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		// エラー処理2-1
		// rcdFiles内の売上ファイルを昇順にソート
		Collections.sort(rcdFiles);
		// rcdFiles内の
		for(int i = 0; i < rcdFiles.size() - 1; i++) {
			// 比較する２つのファイルを変数（former,latter）に代入し、ファイル名の数字をint型に変換
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));
			// ファイル名の数字を比較
			if((latter - former) != 1) {
				// メッセージ「売上ファイル名が連番になっていません」を表示し処理終了
				System.out.println(FILE_NOT_SERIALNUMBER);
				return;
			}
		}

		// ここから処理内容2-2
		// 売上ファイルの数だけ繰り返す
		for(int i = 0; i < rcdFiles.size(); i++) {
			List<String> rcdValue = new ArrayList<>();
			BufferedReader br = null;

			try {
				// rcdFiles内、i番目のファイルを開く
//				File file = new File("C:\\Users\\trainee1214\\workspace\\売上集計課題",rcdFiles.get(i).getName());
				File file = rcdFiles.get(i);
				// ファイルの情報をfr,brに渡していく
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
				String line;
				// 一行ずつ読み込む
				while((line = br.readLine()) != null) {
					rcdValue.add(line);
				}
				// エラー処理2-3 売上ファイルの支店コードが、支店定義ファイルに該当しない場合、エラーメッセージ
				if (!branchNames.containsKey(rcdValue.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + FILE_CODE_INVALID);
					return;
				}
				// エラー処理2-4 売上ファイルの中身が2行ではなかった場合,エラーメッセージ
				if(rcdValue.size() != 2) {
					System.out.println(rcdFiles.get(i).getName() + VALUE_FILE_INVALID_FORMAT);
					return;
				}

				// 型変換。売上金額（rcdValueの2行目の要素）をString型→Long型に変換。
				long fileSale = Long.parseLong(rcdValue.get(1));
				// マップ：branchSalesから売上⾦額を取得し、型変換した金額を加算。変数saleAmountに代入
				Long saleAmount = branchSales.get(rcdValue.get(0)) + fileSale;
				// 加算した売上金額をbranchSalesに追加。
				branchSales.put(rcdValue.get(0), saleAmount);

				// エラー処理2-2 売上金額が10桁を超えた場合、エラーメッセージ「合計金額が10桁を超えました」を表示し、処理を終了する。
				if(saleAmount >= 10000000000L) {
					System.out.println(OVER_DIGITS);
					return;
				}

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}

		}
		// ここまで２－２

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			// ファイルを開く
			File file = new File(path, fileName);
			// エラー処理1-1
			// 支店定義ファイルが存在しない場合、コンソールにメッセージを表示(支店定義ファイルが存在しません)
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			// 読み込み処理再開
			// brを作成するためにfrを作成
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {

//				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				// 支店定義ファイル読み込み、コードと店名をMapに追加
				String[] items = line.split(",");
				// エラー処理1-2
				//支店定義ファイルの仕様が満たされていない場合、コンソールにメッセージを表示（支店定義ファイルのフォーマットが不正です）
				if((items.length != 2) || (!items[0].matches("[0-9]{3}"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			// keyの数だけ繰り返す
			for (String key : branchNames.keySet()) {
				// Mapからvalueの値を取得
				// writeメソッドでファイルに書き込み(支店コード、支店名、合計金額  その後改行bw.newLine();）
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;

	}

}
