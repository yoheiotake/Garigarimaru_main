package com.example.garigarimaru_main;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

public class BindService extends Service implements SensorEventListener{
	private final RemoteCallbackList<BindActivityAIDL> callbackList = new RemoteCallbackList<BindActivityAIDL>();
	private Timer timer = null;
	private int countTime;
	public static final int DAY_SECONDS = 86400;
	private SensorManager manager;
	private int step;
	private float last_step;

	@Override
	public void onCreate() {
		timer = new Timer();
		countTime = 0;
		step = 0;
		last_step = 0;
		timer.schedule(task, 1000, 1000);//(実行関数,開始時刻,周期)単位はミリ秒
		manager = (SensorManager)getSystemService(SENSOR_SERVICE);//追加
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (BindServiceAIDL.class.getName().equals(intent.getAction())) {
			List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if(sensors.size() > 0) {
				Sensor s = sensors.get(0);
				manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
				Log.v("Acceleration1","Acceleration1");
			}
			return serviceCallbackIf;
		}
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.v("Unbind","Unbind");//正常終了でなくてもコールされる
		timer.cancel();
		timer.purge();
		manager.unregisterListener(this);//追加
		SaveStep();
		step = 0;
		last_step = 0;
		return super.onUnbind(intent);
	}

	private BindServiceAIDL.Stub serviceCallbackIf = new BindServiceAIDL.Stub() {
		public void registerCallback(BindActivityAIDL callback) {
			callbackList.register(callback);
		}
		public void unregisterCallback(BindActivityAIDL callback) {
			callbackList.unregister(callback);
		}
	};

	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			countTime += 1;
			if(GetTime() == 0){
				SaveStep();
				step = 0;
				countTime = 0;
			}

			//動作確認用の出力処理
			if(countTime % 10 == 0){
				int n = callbackList.beginBroadcast();
				for (int i = 0; i < n; i++) {
					try {
						callbackList.getBroadcastItem(i).displayTime(countTime/360 + "時間" + countTime/60 + "分" + countTime % 60 + "秒経過しました!");
					} catch (RemoteException e) {
						Log.e("ERROR", e.getMessage());
					}
				}
				callbackList.finishBroadcast();
			}
		}
	};

	public int GetTime(){
		Time time = new Time("Asia/Tokyo");
		time.setToNow();
		return time.hour + time.minute + time.second;
	}

	private void SaveStep(){
		//***歩数をデータベースに保存する***
		//日付が同じなら加算して保存、異なるなら新規作成　=> 動作確認が難しくなるので中止
		//歩数用のテーブルを新規作成し、連続入力を可能にする
		//id,歩数,所属テーブル
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.v("Acceleration2","SensorChanged");
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if(Math.abs(last_step - event.values[SensorManager.DATA_X]) - event.values[SensorManager.DATA_Y] - event.values[SensorManager.DATA_Z] > 0.2){
				step++;
			}
			last_step = event.values[SensorManager.DATA_X] + event.values[SensorManager.DATA_Y] + event.values[SensorManager.DATA_Z];
		}
	}
}