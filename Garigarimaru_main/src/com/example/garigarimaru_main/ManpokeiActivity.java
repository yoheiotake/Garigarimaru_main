package com.example.garigarimaru_main;

import com.example.garigarimaru_main.MainActivity.ButtonClickListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.util.Log;
//import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class ManpokeiActivity extends Activity {

	private BindServiceAIDL bindserviceIf = null;//サービス連携用インターフェース
	private ServiceConnection conn = null;//接続用オブジェクト
	private static Activity activityObj;//ハンドラ用オブジェクト

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manpokei_main);

		Button StartBtn = (Button)findViewById(R.id.button_manpokei_start); // スタートボタン
		StartBtn.setTag("Back_Main");
		StartBtn.setOnClickListener(new StartButtonClickListener());

		Button StopBtn = (Button)findViewById(R.id.button_manpokei_stop); // ストップボタン
		StopBtn.setTag("Back_Main");
		StopBtn.setOnClickListener(new StopButtonClickListener());

		Button BackBtn = (Button)findViewById(R.id.button_manpokei_back); // 戻るボタン
		BackBtn.setTag("Back_Main");
		BackBtn.setOnClickListener(new BackButtonClickListener());

		activityObj = this;
	}

	//スタートボタンのクリックリスナー
	class StartButtonClickListener implements OnClickListener {
		public void onClick(View v) {
			Toast.makeText(ManpokeiActivity.this,"サービスを開始します",Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(BindServiceAIDL.class.getName());
			conn = new BindServiceConnection();
			bindService(intent, conn, BIND_AUTO_CREATE);
		}
	}

	//ストップボタンのクリックリスナー
	class StopButtonClickListener implements OnClickListener {
		public void onClick(View v) {
			try {
				bindserviceIf.unregisterCallback(bindactivityIf);
			}catch(RemoteException e) {
				Log.e("ERROR", e.getMessage());
			}

			Toast.makeText(ManpokeiActivity.this,"サービスを終了します",Toast.LENGTH_SHORT).show();
			unbindService(conn);
			Intent intent = new Intent(BindServiceAIDL.class.getName());
			stopService(intent);
		}
	}

	//戻るボタンのクリックリスナー
	class BackButtonClickListener implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}
	
	//サービス接続、切断クラス
	class BindServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName compName, IBinder binder) {
			bindserviceIf = BindServiceAIDL.Stub.asInterface(binder);
			try {
				bindserviceIf.registerCallback(bindactivityIf);
			}catch (RemoteException e) {
				Log.e("ERROR", e.getMessage());
			}
		}
		
		public void onServiceDisconnected(ComponentName arg0) {
			bindserviceIf = null;
		}
	}
	
	private BindActivityAIDL bindactivityIf = new BindActivityAIDL.Stub() {
		public void displayTime(String msg) throws RemoteException {
			handler.sendMessage(Message.obtain(handler, 0, msg));
		}
	};
	
	private static Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			Toast.makeText(activityObj,(String)msg.obj,Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}