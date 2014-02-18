package com.example.garigarimaru_main;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.garigarimaru_main.GraphWeightActivity.SpinnerSelectedListener;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {

	CreateProductHelper helper = null; // ここでオブジェクトを生成するとエラーになる
	SQLiteDatabase db = null;
	//Boolean first_flag = false;//初回入力画面の表示フラグ Falseのとき表示 画面がスタックされるたびに初期化される
	private String table_name = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button InsertBtn = (Button)findViewById(R.id.button_weight_dicision); // 確定ボタン
		InsertBtn.setTag("insert_weight");
		InsertBtn.setOnClickListener(new ButtonClickListener());

		Button ResetBtn = (Button)findViewById(R.id.button_data_reset); // リセットボタン
		ResetBtn.setTag("reset_data");
		ResetBtn.setOnClickListener(new ResetClickListener());

		Button AllResetBtn = (Button)findViewById(R.id.button_all_reset); // 全消去ボタン
		AllResetBtn.setTag("all_reset");
		AllResetBtn.setOnClickListener(new AllResetClickListener());

		Button ManpokeiBtn = (Button)findViewById(R.id.button_manpokei); // 万歩計ボタン
		ManpokeiBtn.setTag("manpokei");
		ManpokeiBtn.setOnClickListener(new StepClickListener());

		Button GraphBtn = (Button)findViewById(R.id.button_graph); // グラフボタン
		GraphBtn.setTag("graph");
		GraphBtn.setOnClickListener(new GraphClickListener());

		Spinner SpinnerBtn = (Spinner)findViewById(R.id.main_spinner);
		SpinnerBtn.setOnItemSelectedListener(new SpinnerSelectedListener());

		helper = new CreateProductHelper(MainActivity.this);
		// ここでdbにオブジェクトを確保するとエラー
	}

	protected void onStart (){
		super.onStart();
		Log.v("Start", "onStart");
		AllocDB();
		if(!IsStandTable()){//テーブルが存在しない場合は初回入力画面に遷移
			WildStream();
		}
		//onStart()ではインテントが待ち行列に追加され関数内の処理が優先されるためelseで実行を回避する必要がある
		//テーブルが存在する場合、テーブルの表示処理
		else{
			String columns[] = {"_id","table_name"};
			Log.v("Test_Output", "Graph1");
			db.beginTransaction();
			Cursor cursor = db.query("personal_table", columns, null, null, null, null, "_id" + " DESC");
			cursor.moveToFirst();
			String[] array = new String[cursor.getInt(0)];
			int i = 0;
			Log.v("Test_Output", "Graph2");
			do{//全てのテーブルの作成日を取得
				//array[i] = cursor.getString(1);
				Log.v("Test_Output", cursor.getString(1));
				array[i] = JPFormat(cursor.getString(1));//タイムスタンプを日本語フォーマットに変換
				i++;
			}while(cursor.moveToNext());
			Log.v("Test_Output", "Graph3");
			db.setTransactionSuccessful();
			db.endTransaction();
			Log.v("Test_Output", "Graph4");
			//setSpinner((Spinner)findViewById(R.id.main_spinner),array);
			setSpinner((Spinner)findViewById(R.id.main_spinner),array);//スピナーに表示するコンテンツをセット
			Log.v("Test_Output", "Graph5");
		}
	}

	// 確定ボタンがクリックされた場合の処理
	class ButtonClickListener implements OnClickListener {
		public void onClick(View v){
			if(IsStandTable()){
				//テーブル作成済みの場合：現在の体重をテーブルに挿入する
				//適性体重＝身長^2×22
				//BMI＝現在体重÷（身長^2）
				//消費すべきカロリー＝（現在体重ー適性体重）×7000KCal
				Log.v("Test_Output", "Insert1");
				EditText get_weight = (EditText)findViewById(R.id.editText_input_weight);
				if(!get_weight.getText().toString().equals("")){
					SpannableStringBuilder sp_weight = (SpannableStringBuilder)get_weight.getText();
					String st_weight = sp_weight.toString();
					int new_weight = Integer.parseInt(st_weight);
					if(6 < new_weight && new_weight <= 635){
						ContentValues val = new ContentValues();
						String columns[] = {"height","table_name"};
						db.beginTransaction();
						Log.v("Test_Output", "Insert2");
						//降順に整列し最新のテーブルを取得
						Cursor cursor = db.query("personal_table", columns, null, null, null, null, "_id" + " DESC");
						cursor.moveToFirst();//カーソルを先頭のレコードに移動
						double suitable_weight = Math.pow(cursor.getInt(0), 2)*22/10000;//適性体重 pow(a,b)はa^b
						double bmi = new_weight/Math.pow(cursor.getInt(0), 2)*10000;//BMI
						double calorie = (new_weight - suitable_weight)*7000;//消費すべきカロリー。値が大きいのでキロカロリーとして保存
						if(bmi <= 22)//BMIが22以下のとき消費すべきカロリーは0cal
							calorie = 0;

						val.put("weight", new_weight);
						val.put("BMI", bmi);
						val.put("calorie", calorie);
						val.put("time", GetTimeStamp());
						Log.v("Test_Output", "Insert3");
						db.insert(cursor.getString(1), null, val);
						db.setTransactionSuccessful();
						db.endTransaction();
						Log.v("Test_Output", st_weight);
						//Spinner WildSpinner = (Spinner)findViewById(R.id.main_spinner);
						//WildSpinner.performClick();
						SetTableView();
					}
				}
			}
			else{
				//AllocDB();
				WildStream();
			}
		}
	}

	// リセットボタンがクリックされた場合の処理
	class ResetClickListener implements OnClickListener {
		public void onClick(View v){
			//AllocDB();
			if(IsStandTable()){
				String item = "Reset";
				Intent intent = new Intent(MainActivity.this, FirstActivity.class);
				intent.putExtra("Wild",item);
				startActivityForResult(intent, 0);
			}
			else{
				WildStream();
			}
		}
	}

	// 全消去ボタンがクリックされた場合の処理
	class AllResetClickListener implements OnClickListener {
		public void onClick(View v){
			//AllocDB();
			if(IsStandTable()){//テーブル作成済みの場合
				try{
					Log.v("Test_Output", "Erase1");
					db.beginTransaction();//トランザクション開始
					String columns[] = {"table_name"};//カーソルによって値を取得する列名
					Cursor cursor = db.query("personal_table", columns, null, null, null, null, "_id");//テーブル名の取得
					String sql = "drop table personal_table";//SQL実行文（テーブルを破棄）
					Log.v("Test_Output", "Erase2");
					while(cursor.moveToNext()){//テーブルの全消去 レコードは1番目から保存されているので1つ進めた状態で開始
						Log.v("Test_Output", "Erase3");
						String delete_table = "drop table " + cursor.getString(0);//columns[0]で示す列から取得
						//String delete_table = "drop table " + cursor.getString(cursor.getColumnIndex("table_name"));
						db.execSQL(delete_table);
					}
					Log.v("Test_Output", "Erase4");
					db.execSQL(sql);//SQL実行命令
					db.setTransactionSuccessful();//コミット
					db.endTransaction();//トランザクション終了
					//first_flag = false;//初回入力画面の再表示用フラグ
					Log.v("Test_Output", "Erase5");
					WildStream();
				}catch(Exception e){
					//デバッグ用のテストフィールド
					Log.v("Test_Output", "Erase6");
					//String sql = "drop table personal_table";
					//db.execSQL(sql);
					//Log.v("Test_Output", "Enter7");
				}
			}
			else{
				//テーブルが作成されていない場合
				WildStream();
			}
		}
	}

	// 万歩計ボタンがクリックされた場合の処理
	class StepClickListener implements OnClickListener {
		public void onClick(View v){
			if(IsStandTable()){
				String item = "";
				Intent intent = new Intent(MainActivity.this, ManpokeiActivity.class);
				intent.putExtra("SELECTED_PICT",item);
				startActivity(intent);
			}
			else{
				//AllocDB();
				WildStream();
			}
		}
	}

	// グラフボタンがクリックされた場合の処理
	class GraphClickListener implements OnClickListener {
		public void onClick(View v){
			if(IsStandTable()){
				String str = table_name;
				Intent intent = new Intent(MainActivity.this, GraphWeightActivity.class);
				intent.putExtra("Table_Name",str);
				Log.v("Test_Output", "Graph_BT_Click");
				startActivity(intent);
			}
			else{
				//AllocDB();
				WildStream();
			}
		}
	}


	public class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener{
		public void onItemSelected(AdapterView parent,View view, int position,long id) {   
			// Spinner を取得   
			Spinner spinner = (Spinner) parent;   
			// 選択されたアイテムのテキストを取得   
			String str = spinner.getSelectedItem().toString();
			table_name = USFormat(str);//テーブル名に変換
			SetTableView();
		} 

		// 何も選択されなかった時の動作   
		public void onNothingSelected(AdapterView parent) {
			Log.v("Test_Spinner", "Nothing");
		}    
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//初回入力終了後の処理内容。データベースへの入力
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==0){
			if(resultCode==RESULT_OK){//遷移先で決定ボタンがクリックされた場合のみ実行

			}
		}
	}


	private void setSpinner(Spinner spinner,String[] arr){
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arr);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	private void SetTableView(){
		//----テーブルの初期化とレイアウトの設定----
		TableLayout tablelayout = (TableLayout)findViewById(R.id.main_table);
		tablelayout.removeAllViews();
		tablelayout.setStretchAllColumns(true);
		//----テーブルのヘッダーの設定----
		TableRow headrow = new TableRow(MainActivity.this);
		TextView headtxt1 = new TextView(MainActivity.this);
		headtxt1.setText("ID");
		headtxt1.setGravity(Gravity.CENTER_HORIZONTAL);
		headtxt1.setWidth(30);

		TextView headtxt2 = new TextView(MainActivity.this);
		headtxt2.setText("体重");
		headtxt2.setGravity(Gravity.CENTER_HORIZONTAL);
		headtxt2.setWidth(30);

		TextView headtxt3 = new TextView(MainActivity.this);
		headtxt3.setText("BMI");
		headtxt3.setGravity(Gravity.CENTER_HORIZONTAL);
		headtxt3.setWidth(60);

		TextView headtxt4 = new TextView(MainActivity.this);
		headtxt4.setText("キロカロリー");
		headtxt4.setGravity(Gravity.CENTER_HORIZONTAL);
		headtxt4.setWidth(120);

		TextView headtxt5 = new TextView(MainActivity.this);
		headtxt5.setText("時刻");
		headtxt5.setGravity(Gravity.CENTER_HORIZONTAL);
		headtxt5.setWidth(100);

		headrow.addView(headtxt1);
		headrow.addView(headtxt2);
		headrow.addView(headtxt3);
		headrow.addView(headtxt4);
		headrow.addView(headtxt5);
		tablelayout.addView(headrow);

		//----データベースのデータの取得処理-----
		String columns[] = {"_id","weight","BMI","calorie","time"};
		Cursor cursor = db.query(table_name, columns, null, null, null, null, "_id");
		while(cursor.moveToNext()){
			TableRow row = new TableRow(MainActivity.this);
			//ID取得
			TextView id_txt = new TextView(MainActivity.this);
			id_txt.setGravity(Gravity.CENTER_HORIZONTAL);
			id_txt.setText(cursor.getString(0));
			//体重取得
			TextView weight_txt = new TextView(MainActivity.this);
			weight_txt.setGravity(Gravity.CENTER_HORIZONTAL);
			weight_txt.setText(cursor.getString(1));
			//BMI取得
			TextView bmi_txt = new TextView(MainActivity.this);
			bmi_txt.setGravity(Gravity.CENTER_HORIZONTAL);
			bmi_txt.setText(cursor.getString(2));
			//カロリー取得
			TextView calorie_txt = new TextView(MainActivity.this);
			calorie_txt.setGravity(Gravity.CENTER_HORIZONTAL);
			calorie_txt.setText(cursor.getString(3));
			//時刻取得
			TextView time_txt = new TextView(MainActivity.this);
			time_txt.setGravity(Gravity.CENTER_HORIZONTAL);
			time_txt.setText(JPFormat(cursor.getString(4)));

			row.addView(id_txt);
			row.addView(weight_txt);
			row.addView(bmi_txt);
			row.addView(calorie_txt);
			row.addView(time_txt);
			tablelayout.addView(row);
		}
	}

	// テーブル作成のモジュール
	public boolean CreateTable(){
		//first_flag = true;
		try{
			//全テーブルの消去を行った場合
			String sql
			= "create table personal_table (" +
					"_id integer primary key autoincrement," +
					"height integer not null," +
					"weight integer not null," +
					"table_name text not null)";
			db.execSQL(sql);
			return true;
		}catch(Exception e){
			//再起動などでテーブルが存在していてフラグのみ初期化されている場合。ここの処理が行われることはない
			return false;
		}
	}

	//体重テーブル作成モジュール
	public void CreateWeightTable(String table_name){
		Log.v("Time_Output",table_name);// テスト出力
		String sql
		= "create table " + table_name + " (" +
				"_id integer primary key autoincrement," +
				"weight integer not null," +
				"BMI double," +
				"calorie double," + 
				"time text)";
		db.execSQL(sql);
	}

	//データベースのメモリ割り当て
	public void AllocDB(){
		if(db == null){
			db = helper.getWritableDatabase();
		}
	}

	//初回入力画面へ導く関数
	public void WildStream(){
		String item = "Wild";
		Intent intent = new Intent(MainActivity.this, FirstActivity.class);
		intent.putExtra("Wild",item);
		startActivityForResult(intent, 0);//遷移先の処理終了でActivity関数を実行.0は終了の判定フラグ
	}

	//テーブルの存在を確認
	public boolean IsStandTable(){
		String query = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='personal_table';";//パーソナルテーブルの総数を出力
		Cursor cursor = db.rawQuery(query, null); 
		cursor.moveToFirst();
		String result = cursor.getString(0);//コンソール出力用に文字列で取得
		int sum = cursor.getInt(0);
		Log.v("IsStandTable",result);
		if(sum == 1)//Trueのときテーブルが存在
			return true;
		Log.v("IsStandTable = 0",result);
		return false;
	}

	//タイムスタンプの取得  SQLiteではテーブル名を数字から始められない
	public String GetTimeStamp(){
		Time time = new Time("Asia/Tokyo");
		time.setToNow();
		//String date = "Y" + time.year + "M" + (time.month+1) + "D" + time.monthDay + "H" + time.hour + "M" + time.minute + "S" + time.second;
		String date = "AC" + time.year + "Y" + (time.month+1) + "M" + time.monthDay + "D" + time.hour + "H" + time.minute + "m" + time.second + "S";
		return date;
	}

	public String JPFormat(String str){
		//String buf;
		//buf = str.substring(1,1+4) + "年" + str.substring(6,6+1) + "月" + str.substring(8,8+1) + "日" + str.substring(10,2) + "時" + str.substring(13,2) + "分" + str.substring(15,2) + "秒";
		int index = str.indexOf("C");//ACのインデックス番号を取得
		str = str.substring(index+1);//AC以降の文字列を取得
		str = str.replaceAll("Y", "年");//文字列置換("置換前","置換後")
		str = str.replaceAll("M", "月");
		str = str.replaceAll("D", "日");
		str = str.replaceAll("H", "時");
		str = str.replaceAll("m", "分");
		str = str.replaceAll("S", "秒");
		return str;
	}

	public String USFormat(String str){
		str = str.replaceAll("年", "Y");//文字列置換("置換前","置換後")
		str = str.replaceAll("月", "M");
		str = str.replaceAll("日", "D");
		str = str.replaceAll("時", "H");
		str = str.replaceAll("分", "m");
		str = str.replaceAll("秒", "S");
		str = "AC" + str;
		return str;
	}

	public int StringToInt(String str){
		return Integer.parseInt(str);
	}

}