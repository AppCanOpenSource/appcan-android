/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.platform.push;

import android.app.Service;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.push.mqttpush.MQTTService;
import org.zywx.wbpalmstar.platform.push.mqttpush.PushDataCallback;
import org.zywx.wbpalmstar.platform.push.report.PushReportAgent;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.platform.push.report.PushReportHttpClient;
import org.zywx.wbpalmstar.platform.push.report.PushReportUtility;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 为确保推送及时性，PushService 运行在单独进程中（在Manifest文件中配置），而非应用的进程，应注意数据的传递方式。
 * 
 *
 */
public class PushService extends Service implements PushDataCallback {

	private String softToken;
	// public static Status runStatus = Status.PENDING;
	// private boolean isRun = true;
	// private String appName = null;
	// private int screenType = 0;
	// private int screenOffTime = 0;
	private Timer timer = null;
	// private Timer udpTimer = null;
	private long sleepTime = 0;
	// private long startTime = 0;
	SharedPreferences preferences = null;
	private MyTimerTask myTimerTask = null;
	// private UDPTimerTask udpTimerTask = null;
	private String url_push = null;
	// private DatagramSocket mUDPSocket;
	// private String sendData = null;
	// private boolean isUDPRunning = false;
	private int type = 0;
	private boolean isSend = false;
	// private String mamPush_ip = null;
	// private String mamPush_port = null;
	private boolean isTemporary = false;
	private Object pushGetData = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// System.out.println("onStartCommand");
		// System.out.println("onStartCommand");
		// setForeground(true);
		// new Thread(){
		// @Override
		// public void run() {
		// while(true){
		// System.out.println("++++++++++++++++");
		//
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		//
		//
		// }
		// }.start();
		// start(intent,startId);
		start();
		// flags = START_REDELIVER_INTENT;
		// super.onStartCommand(intent, flags, startId);
		// System.out.println("flags ==="+flags);
		// System.out.println("rs ==="+rs);
		// return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// restart Service when the Service is stopped by user.
		Intent localIntent = new Intent();
		localIntent.setClass(this, PushService.class);
		localIntent.putExtra("type", type);
		this.startService(localIntent);
		super.onDestroy();
	}

	private void start() {
		String appKey = EUExUtil.getString("appkey");
		appKey = PushReportUtility.decodeStr(appKey);
		softToken = PushReportUtility.getSoftToken(this, appKey);
		preferences = this.getSharedPreferences(PushReportConstants.SP_APP,
				Context.MODE_PRIVATE);
		url_push = ResoureFinder.getInstance().getString(this, "push_host");
		if (TextUtils.isEmpty(url_push)) {
			Log.w("PushService", "push_host is empty");
			return;
		}
		// String host_and_port = ResoureFinder.getInstance().getString(this,
		// "mam_push_host_and_port");
		// if (!TextUtils.isEmpty(host_and_port)) {
		// mamPush_ip = host_and_port.split(":")[0];
		// mamPush_port = host_and_port.split(":")[1];
		// }
		SharedPreferences sp = this.getSharedPreferences("saveData",
				Context.MODE_PRIVATE);
		String pushMes = sp.getString("pushMes", "0");
		String localPushMes = sp.getString("localPushMes", pushMes);
		if ("1".equals(localPushMes) && "1".equals(pushMes)) {
			type = 1;
		} else {
			type = 0;
		}
		try {
			// type = intent.getIntExtra("type", 0);
			// System.out.println("res ==== "+type);
			if (type == 0) {
				if (pushGetData != null) {
					((MQTTService) pushGetData).onDestroy();
				}
				// runStatus = Status.PENDING;
				return;
			}
			// System.out.println("runStatus ==== RUNNING");
			// runStatus = Status.RUNNING;
			if (pushGetData == null) {
				String softToken = preferences.getString("softToken", null);
				pushGetData = new MQTTService(this, url_push, this, softToken);
				((MQTTService) pushGetData).init();
			} else {
				((MQTTService) pushGetData).onDestroy();
				((MQTTService) pushGetData).init();
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

		// sleepTime = 1000;
		// if (isTemporary) {
		// sleepTime = 1000 * 30;
		// } else {
		// sleepTime = 1000 * 60 * 2;
		// }
		//
		// if (timer == null) {
		// timer = new Timer();
		// }
		//
		// if (myTimerTask == null) {
		// myTimerTask = new MyTimerTask();
		// }
		//
		// timer.schedule(myTimerTask, 0, sleepTime);

		// sendData = "{\"cmd\":\"reg\",\"appid\":\""
		// + preferences.getString("appid", null) + "\",\"st\":\""
		// + preferences.getString("softToken", null) + "\"}";
		// onReceive();
		// receiveData();
		// super.onStart(intent, startId);
	}

	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {

			getPushInfo();
			if (isTemporary && sleepTime == 1000 * 60 * 2) {
				sleepTime = 1000 * 60 * 15;
				notifiTimer();
				return;
			}
			if (sleepTime == 1000 * 60 * 15) {
				sleepTime = 1000 * 60 * 30;
				notifiTimer();
			} else if (sleepTime == 1000 * 60 * 30) {
				sleepTime = 1000 * 60 * 60;
				notifiTimer();
			} else if (sleepTime == 1000 * 60 * 60) {
				sleepTime = 1000 * 60 * 120;
				notifiTimer();
			}
			// startTime = System.currentTimeMillis();
		}

	}

	// private class UDPTimerTask extends TimerTask {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// // udpReg();
	// sendUDPData();
	// }
	//
	// }

	private void getPushInfo() {
		String softToken = preferences.getString("softToken", null);
		// appName = preferences.getString("appName", null);
		String reData = PushReportHttpClient.getGetData(url_push + softToken
				+ "/listMsg", this);
		BDebug.d("debug", "push  reData = = " + reData);
		if (reData != null && reData.length() > 0) {
			try {
				JSONObject json = new JSONObject(reData);
				String status = json.getString("status");
				if ("ok".equals(status)) {
					String messageList = json.getString("messageList");
					JSONArray jsonArray = new JSONArray(messageList);
					for (int i = 0; i < jsonArray.length(); i++) {
						runningNotification(jsonArray.getJSONObject(i));
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	private void notifiTimer() {

		if (myTimerTask != null) {
			myTimerTask.cancel();
			myTimerTask = null;
		}
		myTimerTask = new MyTimerTask();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(myTimerTask, sleepTime, sleepTime);
	}

	// private void notifiUDPTimer() {
	//
	// isUDPRunning = false;
	// if (udpTimer != null) {
	// udpTimer.cancel();
	// udpTimer = null;
	// }
	// udpTimer = new Timer();
	// if (udpTimerTask != null) {
	// udpTimerTask.cancel();
	// udpTimerTask = null;
	// }
	// udpTimerTask = new UDPTimerTask();
	// udpTimer.schedule(udpTimerTask, 0, 1 * 60 * 1000);
	// }

	private void runningNotification(JSONObject text) throws JSONException {	    
		String pushMessage = text.toString();// 推送消息全部内容
        // 设置通知的事件消息
	    String tickerText = text.getString("title");
	    String value = text.getString("body");
        String packg = getPackageName();
        String widgetName = null;
        PackageManager pm = getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo(packg, PackageManager.GET_CONFIGURATIONS);
            String appName = pinfo.applicationInfo.loadLabel(
                    getPackageManager()).toString();
            widgetName = appName;
            Editor editor = preferences.edit();
            editor.putString("widgetName", widgetName);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(widgetName)) {
            widgetName = preferences.getString("widgetName", "");
        }
        
		Intent intent = new Intent(PushRecieveMsgReceiver.ACTION_PUSH);
		intent.putExtra("data", value);
		intent.putExtra("title", tickerText);
		intent.putExtra("packg", packg);
		intent.putExtra("widgetName", widgetName);
		intent.setPackage(packg);
		intent.putExtra("message", pushMessage);
		sendBroadcast(intent);//传递过去

        try {
            PushReportAgent.reportPush(value, System.currentTimeMillis() + "",
                    PushReportConstants.EVENT_TYPE_ARRIVED, softToken, this);// 推送消息到达上报
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	private void onReceive() {
		final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if ("android.intent.action.SCREEN_OFF".equals(intent
						.getAction())) {
					if (isTemporary) {
						sleepTime = 1000 * 60 * 2;
					} else {
						sleepTime = 1000 * 60 * 15;
					}

					notifiTimer();
				} else if ("android.intent.action.SCREEN_ON".equals(intent
						.getAction())) {
					if (isTemporary) {
						sleepTime = 1000 * 30;
					} else {
						sleepTime = 1000 * 60 * 2;
					}

					notifiTimer();
				}
				if (TextUtils.equals(intent.getAction(),
						CONNECTIVITY_CHANGE_ACTION)) {

					ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					if (mConnMgr != null) {

						NetworkInfo aActiveInfo = mConnMgr
								.getActiveNetworkInfo(); // 获取活动网络连接信息
						if (aActiveInfo != null
								&& aActiveInfo.isConnectedOrConnecting()) {

							if (!isSend
									&& aActiveInfo.getType() == ConnectivityManager.TYPE_WIFI) {
								// udpReg();
								// notifiUDPTimer();
								isSend = true;
							}
						} else {
							isSend = false;
						}

					} else {
						isSend = false;
					}

				}

			}
		}, filter);
	}

	public void stop() {
		if (myTimerTask != null) {
			myTimerTask.cancel();
			myTimerTask = null;
		}
		// if (udpTimerTask != null) {
		// udpTimerTask.cancel();
		// udpTimerTask = null;
		// }
		// if (udpTimer != null) {
		// udpTimer.cancel();
		// udpTimer = null;
		// }
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		// runStatus = Status.PENDING;
		// sleepTime = 0;
		// if (mUDPSocket != null) {
		// mUDPSocket.disconnect();
		// mUDPSocket.close();
		// mUDPSocket = null;
		// }
		// isUDPRunning = false;
	}

	// private void sendUDPData() {
	//
	// try {
	//
	// if (mUDPSocket != null && mUDPSocket.isConnected()) {
	// System.out.println("sendUDPData === "+sendData);
	// mUDPSocket.send(new DatagramPacket(sendData.getBytes(),
	// sendData.getBytes().length));
	// } else {
	// udpReg();
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	// private void udpReg() {
	// try {
	// if (mUDPSocket != null) {
	// mUDPSocket.disconnect();
	// mUDPSocket.close();
	// mUDPSocket = null;
	// }
	// mUDPSocket = new DatagramSocket();
	// // mUDPSocket.close();
	// // mUDPSocket = null;
	//
	// if (!mUDPSocket.isConnected()) {
	// mUDPSocket.connect(InetAddress.getByName(mamPush_ip),
	// Integer.parseInt(mamPush_port));
	// }
	//
	// if (mUDPSocket.isConnected()) {
	// receiveData();
	// }
	//
	// } catch (Exception e) {
	//
	// // TODO: handle exception
	// }
	// }

	// private void receiveData() {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// isUDPRunning = true;
	// while (isUDPRunning) {
	// try {
	// if (mUDPSocket == null) {
	// mUDPSocket = new DatagramSocket();
	// }
	// byte[] buf = new byte[10240];
	// DatagramPacket rePacket = new DatagramPacket(buf, 10240);
	// mUDPSocket.receive(rePacket);
	// String data = new String(rePacket.getData());
	// byte[] b = new byte[1];
	// data = data.replaceAll(String.valueOf(((char) b[0])),
	// "");
	// System.out.println("receiveData === "+data);
	// if ("NW".equals(data)) {
	// getPushInfo();
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// }).start();
	//
	// }

	public static enum Status {
		/**
		 * Indicates that the task has not been executed yet.
		 */
		PENDING,
		/**
		 * Indicates that the task is running.
		 */
		RUNNING,
		/**
		 * Indicates that {@link AsyncTask#onPostExecute} has finished.
		 */
		FINISHED,
	}

	@Override
	public void pushData(JSONObject text) {
		try {
			runningNotification(text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
