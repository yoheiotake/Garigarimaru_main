package com.example.garigarimaru_main;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.example.garigarimaru_main.GraphWeightActivity.CalorieButtonClickListener;
import com.example.garigarimaru_main.GraphWeightActivity.SpinnerSelectedListener;
import com.example.garigarimaru_main.MainActivity.ButtonClickListener;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
//import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class GraphCalorieActivity extends Activity {

	CreateProductHelper helper = null;
	SQLiteDatabase db = null;
	protected GraphicalView mChartView;
	private String table_name = "";
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphcalorie_main);

		Button WeightBtn = (Button)findViewById(R.id.button_graphcalorie_weight); // Weight_Button
		WeightBtn.setTag("Weight_Main");
		WeightBtn.setOnClickListener(new WeightButtonClickListener());

		Button StepBtn = (Button)findViewById(R.id.button_graphcalorie_step); // Step_Button
		StepBtn.setTag("Step_Main");
		StepBtn.setOnClickListener(new StepButtonClickListener());

		Button BackBtn = (Button)findViewById(R.id.button_graphcalorie_back); // Back_Button
		BackBtn.setTag("Back_Main");
		BackBtn.setOnClickListener(new ButtonClickListener());

		Spinner SpinnerBtn = (Spinner)findViewById(R.id.spinner2);
		SpinnerBtn.setOnItemSelectedListener(new SpinnerSelectedListener());

		helper = new CreateProductHelper(GraphCalorieActivity.this);
	}

	protected void onStart (){
		super.onStart();
		AllocDB();
		//テーブルの閲覧履歴を取得
		Intent data = getIntent();
		Bundle extras = data.getExtras();
		String tag = extras.getString("Table_Name");
		String columns[] = {"_id","table_name"};
		Log.v("Test_Output", "Graph1");
		db.beginTransaction();
		Cursor cursor = db.query("personal_table", columns, null, null, null, null, "_id" + " DESC");
		cursor.moveToFirst();
		String[] array = new String[cursor.getInt(0)];
		int default_selected = 0;//スピナーボタンの規定要素番号
		Log.v("Test_Output", "Graph2");
		do{//全てのテーブルの作成日を取得
			//array[i] = cursor.getString(1);
			Log.v("Test_Output", cursor.getString(1));
			if(tag.equals(cursor.getString(1)))//閲覧履歴と一致している場合
				default_selected = cursor.getInt(0) - 1;
			array[cursor.getInt(0) - 1] = JPFormat(cursor.getString(1));//タイムスタンプを日本語フォーマットに変換
		}while(cursor.moveToNext());
		Log.v("Test_Output", "Graph3");
		db.setTransactionSuccessful();
		db.endTransaction();
		Log.v("Test_Output", "Graph4");
		//setSpinner((Spinner)findViewById(R.id.spinner1),array);
		setSpinner((Spinner)findViewById(R.id.spinner2),array);//スピナーに表示するコンテンツをセット
		Spinner SpinnerBt = (Spinner)findViewById(R.id.spinner2);
		SpinnerBt.setSelection(default_selected);
		Log.v("Test_Output", "Graph5");

		context = GraphCalorieActivity.this;
	}

	public void onStop(){
		super.onStop();
		//if(db != null)
		//db.close();
		Log.v("LifeCycle", "onStop");
	}

	class WeightButtonClickListener implements OnClickListener {
		public void onClick(View v){
			String item = table_name;
			Intent intent = new Intent(GraphCalorieActivity.this, GraphWeightActivity.class);
			intent.putExtra("Table_Name",item);
			startActivity(intent);
		}
	}

	class StepButtonClickListener implements OnClickListener {
		public void onClick(View v){
			String item = table_name;
			Intent intent = new Intent(GraphCalorieActivity.this, GraphStepActivity.class);
			intent.putExtra("Table_Name",item);
			startActivity(intent);
		}
	}

	class ButtonClickListener implements OnClickListener {
		public void onClick(View v){
			String item = "";
			Intent intent = new Intent(GraphCalorieActivity.this, MainActivity.class);
			intent.putExtra("GraphCalorie",item);
			startActivity(intent);
			//finish();
		}
	}

	public class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener{
		public void onItemSelected(AdapterView parent,View view, int position,long id) { 
			// Spinner を取得   
			Spinner spinner = (Spinner) parent;   
			// 選択されたアイテムのテキストを取得   
			String str = spinner.getSelectedItem().toString();
			Log.v("Test_Spinner1", str);
			table_name = USFormat(str);//テーブル名に変換
			Log.v("Test_Spinner2", table_name);
			//----コメントの初期化と挿入----
			Set_Msg(Get_State(table_name));
			//----グラフの初期化と設定----
			RelativeLayout layout = (RelativeLayout) findViewById(R.id.chart2);
			layout.removeAllViews();
			mChartView = ChartFactory.getBarChartView(context, getBarChartDataset(),getRenderer(), BarChart.Type.DEFAULT);
			layout.addView(mChartView);
			//----テーブルの初期化とレイアウトの設定----
			TableLayout tablelayout = (TableLayout)findViewById(R.id.table_layout2);
			tablelayout.removeAllViews();
			tablelayout.setStretchAllColumns(true);
			//----テーブルのヘッダーの設定----
			TableRow headrow = new TableRow(GraphCalorieActivity.this);
			TextView headtxt1 = new TextView(GraphCalorieActivity.this);
			headtxt1.setText("ID");
			headtxt1.setGravity(Gravity.CENTER_HORIZONTAL);
			headtxt1.setWidth(30);

			TextView headtxt2 = new TextView(GraphCalorieActivity.this);
			headtxt2.setText("体重");
			headtxt2.setGravity(Gravity.CENTER_HORIZONTAL);
			headtxt2.setWidth(30);

			TextView headtxt3 = new TextView(GraphCalorieActivity.this);
			headtxt3.setText("BMI");
			headtxt3.setGravity(Gravity.CENTER_HORIZONTAL);
			headtxt3.setWidth(60);

			TextView headtxt4 = new TextView(GraphCalorieActivity.this);
			headtxt4.setText("キロカロリー");
			headtxt4.setGravity(Gravity.CENTER_HORIZONTAL);
			headtxt4.setWidth(120);

			TextView headtxt5 = new TextView(GraphCalorieActivity.this);
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
				TableRow row = new TableRow(GraphCalorieActivity.this);
				//ID取得
				TextView id_txt = new TextView(GraphCalorieActivity.this);
				id_txt.setGravity(Gravity.CENTER_HORIZONTAL);
				id_txt.setText(cursor.getString(0));
				//体重取得
				TextView weight_txt = new TextView(GraphCalorieActivity.this);
				weight_txt.setGravity(Gravity.CENTER_HORIZONTAL);
				weight_txt.setText(cursor.getString(1));
				//BMI取得
				TextView bmi_txt = new TextView(GraphCalorieActivity.this);
				bmi_txt.setGravity(Gravity.CENTER_HORIZONTAL);
				bmi_txt.setText(cursor.getString(2));
				//カロリー取得
				TextView calorie_txt = new TextView(GraphCalorieActivity.this);
				calorie_txt.setGravity(Gravity.CENTER_HORIZONTAL);
				calorie_txt.setText(cursor.getString(3));
				//時刻取得
				TextView time_txt = new TextView(GraphCalorieActivity.this);
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

		// 何も選択されなかった時の動作   
		public void onNothingSelected(AdapterView parent) {
			Log.v("Test_Spinner", "Nothing");
		}    
	}

	private void setSpinner(Spinner spinner,String[] arr){
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arr);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	public XYMultipleSeriesDataset getBarChartDataset() {
		XYMultipleSeriesDataset myData = new XYMultipleSeriesDataset();
		XYSeries dataSeries = new XYSeries("data");

		String columns[] = {"_id","calorie"};
		Cursor cursor = db.query(table_name, columns, null, null, null, null, "_id");
		while(cursor.moveToNext()){
			dataSeries.add(cursor.getInt(0), cursor.getInt(1));
		}

		myData.addSeries(dataSeries);
		return myData;
	}

	public XYMultipleSeriesRenderer getRenderer() {
		XYSeriesRenderer renderer = new XYSeriesRenderer();

		String query = "SELECT COUNT(*) FROM " + table_name + " ;";
		Cursor cursor = db.rawQuery(query, null); 
		cursor.moveToFirst();
		int sum = cursor.getInt(0);

		query = "SELECT calorie FROM " + table_name + " WHERE _id = 1;";
		cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		int first_calorie = cursor.getInt(0);
		int min_calorie = first_calorie - 7000*10;
		int max_calorie = first_calorie + 7000*10;
		if(min_calorie < 0 )
			min_calorie = 0;
		if(max_calorie > 7000*635){
			max_calorie = 7000*635;
		}

		// 棒グラフの色
		renderer.setColor(Color.parseColor("#158aea"));

		XYMultipleSeriesRenderer myRenderer = new XYMultipleSeriesRenderer(); 
		myRenderer.addSeriesRenderer(renderer);

		// XY（初期表示の？）最大最小値
		myRenderer.setXAxisMin(0);
		myRenderer.setXAxisMax(7);
		myRenderer.setYAxisMin(min_calorie);
		myRenderer.setYAxisMax(max_calorie);

		// データ値の表示
		//myRenderer.setDisplayChartValues(true);
		//myRenderer.setChartValuesTextSize(18);
		myRenderer.getSeriesRendererAt(0).setDisplayChartValues(true);
		myRenderer.getSeriesRendererAt(0).setChartValuesTextSize(18);

		// グリッド表示
		myRenderer.setShowGrid(true);
		// グリッド色
		myRenderer.setGridColor(Color.parseColor("#c9c9c9"));

		// スクロール許可(X,Y)
		myRenderer.setPanEnabled(true, true);
		// スクロール幅（X最少, X最大, Y最少, Y最大
		myRenderer.setPanLimits(new double[] {0, sum + 1, 0, 7000*635});

		// 凡例表示
		myRenderer.setShowLegend(false);

		// ラベル表示
		myRenderer.setXLabels(10);
		myRenderer.setYLabels(20);
		myRenderer.setLabelsTextSize(18);//20
		myRenderer.setYLabelsAlign(Align.RIGHT);

		// XY軸表示
		myRenderer.setShowAxes(false);
		// バー間の間隔
		myRenderer.setBarSpacing(0.5);
		// ズーム許可
		myRenderer.setZoomEnabled(false, false);
		// 余白
		int[] margin = {20, 80, 50, 30};
		myRenderer.setMargins(margin);
		// 余白背景色
		myRenderer.setMarginsColor(Color.parseColor("#FFFFFF"));

		return myRenderer;
	}

	public void AllocDB(){
		if(db == null){
			db = helper.getWritableDatabase();
		}
	}
	
	public int Get_Lines(String table){
		String query = "SELECT COUNT(*) FROM " + table + " ;";
		Cursor cursor = db.rawQuery(query, null); 
		cursor.moveToFirst();
		return cursor.getInt(0);
	}
	
	public double Get_Avg(String table,String column){
		String query = "SELECT COUNT(*),SUM(" + column + ") FROM  " + table + " ;";
		Cursor cursor = db.rawQuery(query, null); 
		cursor.moveToFirst();
		return cursor.getInt(1)/cursor.getInt(0);
	}
	
	public int Get_Rest(String table,String column){
		String query = "SELECT " + column + " FROM  " + table + " WHERE _id = 1;";
		Cursor cursor = db.rawQuery(query, null); 
		cursor.moveToFirst();
		int first = cursor.getInt(0);
		query = "SELECT " + column + " FROM  " + table + " WHERE _id = " + String.valueOf(Get_Lines(table_name)) + ";";
		cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		return first - cursor.getInt(0);
	}
	
	public boolean Get_State(String table){
		String query = "SELECT calorie FROM  " + table + " WHERE _id = " + String.valueOf(Get_Lines(table_name)) + ";";
		Cursor cursor = db.rawQuery(query, null); 
		cursor.moveToFirst();
		if (cursor.getDouble(0) == 0)
			return true;
		return false;
	}
	
	public void Set_Msg(boolean result){
		
		int x = 0,y = 0,z = 0;
		int lines = Get_Lines(table_name);
		int rest = Get_Rest(table_name,"calorie");
		
		if(result) x = 1;//減量成功
		if(lines > 7) y = 1;//7日以上継続
		if(rest > 5*7000) z = 1;//減量35000kcal以上(減量できている)
		
		String[][][] Message = new String[2][2][2];
		Message[0][0][0] = "目標未達成です。消費カロリーは" + rest + "kcalです。成功の秘訣は継続と運動量です。";
		Message[0][0][1] = "目標未達成です。消費カロリーは" + rest + "kcalです。成功の秘訣は継続です。";
		Message[0][1][0] = "目標未達成です。消費カロリーは" + rest + "kcalです。成功の秘訣は運動量です。";
		Message[0][1][1] = "目標未達成です。消費カロリーは" + rest + "kcalです。この調子で頑張りましょう。";
		Message[1][0][0] = "おめでとうございます!!目標達成です。所用日数:" + lines + "日。消費カロリー:" + rest + "kcalです。成功の理由は減量の取り組みが早かったことです。";
		Message[1][0][1] = "おめでとうございます!!目標達成です。所用日数:" + lines + "日。消費カロリー:" + rest + "kcalです。成功の理由は運動量です。";
		Message[1][1][0] = "おめでとうございます!!目標達成です。所用日数:" + lines + "日。消費カロリー:" + rest + "kcalです。成功の理由は継続的な努力です。";
		Message[1][1][1] = "おめでとうございます!!目標達成です。所用日数:" + lines + "日。消費カロリー:" + rest + "kcalです。成功の理由は継続的な努力と運動量です。";
		
        TextView tv = (TextView)findViewById(R.id.textView_graphcalorie_comment_text);
        tv.setText(Message[x][y][z]);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}