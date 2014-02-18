package com.example.garigarimaru_main;

import com.example.garigarimaru_main.MainActivity.ResetClickListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.text.SpannableStringBuilder;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;



public class FirstActivity extends Activity {

	CreateProductHelper helper = null;
	SQLiteDatabase db = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_main);

		Button DecideBtn = (Button)findViewById(R.id.button_first_decide); // 決定ボタン
		DecideBtn.setTag("decide_data");
		DecideBtn.setOnClickListener(new DecideClickListener());
		
		Button ResetBtn = (Button)findViewById(R.id.button_first_cancel); // キャンセルボタン
		ResetBtn.setTag("cancel_data");
		ResetBtn.setOnClickListener(new CancelClickListener());
		
		helper = new CreateProductHelper(FirstActivity.this);
	}
	
	
	
	//決定ボタンがクリックされた場合の処理
	class DecideClickListener implements OnClickListener {
		public void onClick(View v){
			EditText in_height = (EditText)findViewById(R.id.editText_first_input_height);//入力値の取得
			EditText in_weight = (EditText)findViewById(R.id.editText_first_input_weight);
			
			if(!in_height.getText().toString().equals("") && !in_weight.getText().toString().equals("")){//空入力判定
				SpannableStringBuilder sb1 = (SpannableStringBuilder)in_height.getText();//string型に変換するために必要な仲介作業
				SpannableStringBuilder sb2 = (SpannableStringBuilder)in_weight.getText();
				//String[] str = {sb1.toString(),sb2.toString()};//文字列に変換
				int height = StringToInt(sb1.toString());//int型に変換
				int weight = StringToInt(sb2.toString());
				// 不正な値のチェック [56 < 身長 <= 272] and [6 < 体重 <= 635]
				if( (56 < height) && (height <= 272) 
						&& (6 < weight) && (weight <= 635)){
					String str ="";
					String time_stamp = GetTimeStamp();//タイムスタンプを取得
					boolean reset_flag = false;//リセットボタンのクリックによる画面遷移の判定フラグ
					Intent data = getIntent();
					Bundle extras = data.getExtras();
					Log.v("Test_Output1", "CreateTable");//LogCatテスト出力
					String tag = extras != null ? extras.getString("Wild") : "";//インテントの引数がNULLのとき空文字を格納し、それ以外のときタグを取得
					Log.v("Test_Output2", tag);
					if(tag.equals("Reset")){//リセットボタンのクリックによる画面遷移の場合の処理
						Log.v("Test_Output3", "Reset True");
						reset_flag = true;
					}
					db = helper.getWritableDatabase();
					if(reset_flag || CreateTable()){//リセットボタンによる画面遷移の場合はテーブルが存在しているので新規テーブル作成は行わない
						ContentValues val = new ContentValues();
						ContentValues val2 = new ContentValues();
						//適性体重＝身長^2×22
						//BMI＝現在体重÷（身長^2）
						//消費すべきカロリー＝（現在体重ー適性体重）×7000KCal
						double suitable_weight = Math.pow(height, 2)*22/10000;//適性体重 pow(a,b)はa^b
						double bmi = weight/Math.pow(height, 2)*10000;//BMI
						double calorie = (weight - suitable_weight)*7000;//消費すべきカロリー。値が大きいのでキロカロリーとして保存
						if(bmi <= 22)//BMIが22以下のとき消費すべきカロリーは0cal
							calorie = 0;
						
						val.put("height", height);
						val.put("weight", weight);
						val.put("table_name",time_stamp);
						val2.put("weight", weight);
						val2.put("BMI", bmi);
						val2.put("calorie", calorie);
						val2.put("time",time_stamp);
						db.beginTransaction();
						db.insert("personal_table", null, val);
						CreateWeightTable(time_stamp);
						db.insert(time_stamp, null, val2);//利便性を考慮して体重テーブルにも初期体重を挿入
						db.setTransactionSuccessful();
						db.endTransaction();
					}
					db.close();
					Intent intent = new Intent(FirstActivity.this, MainActivity.class);
					intent.putExtra("GET_DATA",str);
					setResult(RESULT_OK, intent);//決定ボタンのクリックによる画面遷移のフラグ。戻る操作と区別
				}
			}
			finish();//現在のアクティビティを終了
		}
	}
	
	class CancelClickListener implements OnClickListener {
		public void onClick(View v){
			String item = "";
			Intent intent = new Intent(FirstActivity.this, MainActivity.class);
			intent.putExtra("SELECTED_PICT10",item);
			//startActivity(intent);
			finish();
		}
	}
	
	public boolean CreateTable(){
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
			//再起動などでテーブルが存在していてフラグのみ初期化されている場合
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
	
	public int StringToInt(String str){
		return Integer.parseInt(str);
	}
	
	//タイムスタンプの取得  SQLiteではテーブル名を数字から始められない
	public String GetTimeStamp(){
		Time time = new Time("Asia/Tokyo");
		time.setToNow();
		//String date = "Y" + time.year + "M" + (time.month+1) + "D" + time.monthDay + "H" + time.hour + "M" + time.minute + "S" + time.second;
		String date = "AC" + time.year + "Y" + (time.month+1) + "M" + time.monthDay + "D" + time.hour + "H" + time.minute + "m" + time.second + "S";
		return date;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
