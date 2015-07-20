package org.zywx.wbpalmstar.platform.push.mqttpush;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zywx.wbpalmstar.platform.push.report.PushReportUtility;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttPersistence;
import com.ibm.mqtt.MqttPersistenceException;
import com.ibm.mqtt.MqttSimpleCallback;

/*
 * An example of how to implement an MQTT client in Android, able to receive
 *  push notifications from an MQTT message broker server.
 *
 *  Dale Lane (dale.lane@gmail.com)
 *    28 Jan 2011
 */
public class MQTTService implements MqttSimpleCallback {
	/************************************************************************/
	/* CONSTANTS */
	/************************************************************************/

	// something unique to identify your app - used for stuff like accessing
	// application preferences
	// public static final String APP_ID = "org.mosquitto.android.powerviewer";

	// constants used to notify the Activity UI of received messages
	// public static final String MQTT_MSG_RECEIVED_INTENT =
	// "org.mosquitto.android.powerviewer.MSGRECVD";
	// public static final String MQTT_MSG_RECEIVED_TOPIC =
	// "org.mosquitto.android.powerviewer.MSGRECVD_TOPIC";
	// public static final String MQTT_MSG_RECEIVED_MSG =
	// "org.mosquitto.android.powerviewer.MSGRECVD_MSGBODY";

	// constants used to tell the Activity UI the connection status
	// public static final String MQTT_STATUS_INTENT =
	// "org.mosquitto.android.powerviewer.STATUS";
	// public static final String MQTT_STATUS_MSG =
	// "org.mosquitto.android.powerviewer.STATUS_MSG";

	// constant used internally to schedule the next ping event
	public static final String MQTT_PING_ACTION = "org.mosquitto.android.powerviewer.PING";

	// constants used by status bar notifications
	public static final int MQTT_NOTIFICATION_ONGOING = 1;
	public static final int MQTT_NOTIFICATION_UPDATE = 2;
	public static final String ACTION_MDM = "zywx.mdm";
	private Context _context;
	// private String _url;
	private String _softToken;
	private PushDataCallback pushDataCallback = null;

	public MQTTService(Context context, String url, PushDataCallback callback,
			String softToken) {
		_context = context;
		brokerHostName = url;
		pushDataCallback = callback;
		_softToken = softToken;
	}

	// constants used to define MQTT connection status
	public enum MQTTConnectionStatus {
		INITIAL, // initial status
		CONNECTING, // attempting to connect
		CONNECTED, // connected
		NOTCONNECTED_WAITINGFORINTERNET, // can't connect because the phone
											// does not have Internet access
		NOTCONNECTED_USERDISCONNECT, // user has explicitly requested
										// disconnection
		NOTCONNECTED_DATADISABLED, // can't connect because the user
									// has disabled data access
		NOTCONNECTED_UNKNOWNREASON // failed to connect for some reason
	}

	// MQTT constants
	public static final int MAX_MQTT_CLIENTID_LENGTH = 22;

	/************************************************************************/
	/* VARIABLES used to maintain state */
	/************************************************************************/

	// status of MQTT client connection
	private MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;

	/************************************************************************/
	/* VARIABLES used to configure MQTT connection */
	/************************************************************************/

	// taken from preferences
	// host name of the server we're receiving push notifications from
	private String brokerHostName = "";
	// taken from preferences
	// topic we want to receive messages about
	// can include wildcards - e.g. '#' matches anything
	private String topicName = "";

	// defaults - this sample uses very basic defaults for it's interactions
	// with message brokers
	// private int brokerPortNumber = 1883;
	private MqttPersistence usePersistence = null;
	private boolean cleanStart = false;
	private int[] qualitiesOfService = { 2, 2 };

	// how often should the app ping the server to keep the connection alive?
	//
	// too frequently - and you waste battery life
	// too infrequently - and you wont notice if you lose your connection
	// until the next unsuccessfull attempt to ping
	//
	// it's a trade-off between how time-sensitive the data is that your
	// app is handling, vs the acceptable impact on battery life
	//
	// it is perhaps also worth bearing in mind the network's support for
	// long running, idle connections. Ideally, to keep a connection open
	// you want to use a keep alive value that is less than the period of
	// time after which a network operator will kill an idle connection
	private short keepAliveSeconds = 20 * 60;

	// This is how the Android client app will identify itself to the
	// message broker.
	// It has to be unique to the broker - two clients are not permitted to
	// connect to the same broker using the same client ID.
	// private String mqttClientId = null;

	/************************************************************************/
	/* VARIABLES - other local variables */
	/************************************************************************/
	// connection to the message broker
	private IMqttClient mqttClient = null;

	// receiver that notifies the Service when the phone gets data connection
	private NetworkConnectionIntentReceiver netConnReceiver;

	// receiver that notifies the Service when the user changes data use
	// preferences
	private BackgroundDataChangeIntentReceiver dataEnabledReceiver;

	// receiver that wakes the Service up when it's time to ping the server
	private PingSender pingSender;

	/************************************************************************/
	/* METHODS - core Service lifecycle methods */
	/************************************************************************/

	// see http://developer.android.com/guide/topics/fundamentals.html#lcycles

	public void init() {

		// reset status variable to initial state
		connectionStatus = MQTTConnectionStatus.INITIAL;

		// create a binder that will let the Activity UI send
		// commands to the Service
		mBinder = new LocalBinder<MQTTService>(this);

		// get the broker settings out of app preferences
		// this is not the only way to do this - for example, you could use
		// the Intent that starts the Service to pass on configuration values

		// brokerHostName = settings.getString("broker", "");
		// topicName = "push/" + mAppId, "push/" + softToken;

		// register to be notified whenever the user changes their preferences
		// relating to background data use - so that we can respect the current
		// preference
		dataEnabledReceiver = new BackgroundDataChangeIntentReceiver();
		_context.registerReceiver(dataEnabledReceiver, new IntentFilter(
				ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED));

		// define the connection to the broker
		defineConnectionToBroker(brokerHostName);
		onStart();
	}

	public void onStart() {
		// This is the old onStart method that will be called on the pre-2.0
		// platform. On 2.0 or later we override onStartCommand() so this
		// method will not be called.

		new Thread(new Runnable() {
			@Override
			public void run() {
				handleStart();
			}
		}, "MQTTservice").start();
	}

	// @Override
	// public int onStartCommand(final Intent intent, int flags, final int
	// startId) {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// handleStart(intent, startId);
	// }
	// }, "MQTTservice").start();
	//
	// // return START_NOT_STICKY - we want this Service to be left running
	// // unless explicitly stopped, and it's process is killed, we want it to
	// // be restarted
	// return START_STICKY;
	// }

	// @SuppressLint("NewApi")
	synchronized void handleStart() {
		// before we start - check for a couple of reasons why we should stop

		if (mqttClient == null) {
			// we were unable to define the MQTT client connection, so we stop
			// immediately - there is nothing that we can do
			// stopSelf();
			return;
		}

		ConnectivityManager cm = (ConnectivityManager) _context
				.getSystemService(Service.CONNECTIVITY_SERVICE);
		if (cm.getBackgroundDataSetting() == false) // respect the user's
													// request not to use data!
		{
			// user has disabled background data
			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_DATADISABLED;

			// update the app to show that the connection has been disabled
			broadcastServiceStatus("Not connected - background data disabled");

			// we have a listener running that will notify us when this
			// preference changes, and will call handleStart again when it
			// is - letting us pick up where we leave off now
			return;
		}

		// the Activity UI has started the MQTT service - this may be starting
		// the Service new for the first time, or after the Service has been
		// running for some time (multiple calls to startService don't start
		// multiple Services, but it does call this method multiple times)
		// if we have been running already, we re-send any stored data
		rebroadcastStatus();
		rebroadcastReceivedMessages();

		// if the Service was already running and we're already connected - we
		// don't need to do anything
		if (isAlreadyConnected() == false) {
			// set the status to show we're trying to connect
			connectionStatus = MQTTConnectionStatus.CONNECTING;

			// we are creating a background service that will run forever until
			// the user explicity stops it. so - in case they start needing
			// to save battery life - we should ensure that they don't forget
			// we're running, by leaving an ongoing notification in the status
			// bar while we are running
			// NotificationManager nm = (NotificationManager)
			// getSystemService(NOTIFICATION_SERVICE);
			// Notification notification = new Notification(R.drawable.icon,
			// "MQTT", System.currentTimeMillis());
			// notification.flags |= Notification.FLAG_ONGOING_EVENT;
			// notification.flags |= Notification.FLAG_NO_CLEAR;
			// Intent notificationIntent = new Intent(this, PowerViewer.class);
			// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
			// notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			// notification.setLatestEventInfo(this, "MQTT",
			// "MQTT Service is running", contentIntent);
			// nm.notify(MQTT_NOTIFICATION_ONGOING, notification);

			// before we attempt to connect - we check if the phone has a
			// working data connection
			if (isOnline()) {
				// we think we have an Internet connection, so try to connect
				// to the message broker
				if (connectToBroker()) {
					// we subscribe to a topic - registering to receive push
					// notifications with a particular key
					// in a 'real' app, you might want to subscribe to multiple
					// topics - I'm just subscribing to one as an example
					// note that this topicName could include a wildcard, so
					// even just with one subscription, we could receive
					// messages for multiple topics
					subscribeToTopic(topicName);
				}
			} else {
				// we can't do anything now because we don't have a working
				// data connection
				connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;

				// inform the app that we are not connected
				broadcastServiceStatus("Waiting for network connection");
			}
		}

		// changes to the phone's network - such as bouncing between WiFi
		// and mobile data networks - can break the MQTT connection
		// the MQTT connectionLost can be a bit slow to notice, so we use
		// Android's inbuilt notification system to be informed of
		// network changes - so we can reconnect immediately, without
		// haing to wait for the MQTT timeout
		if (netConnReceiver == null) {
			netConnReceiver = new NetworkConnectionIntentReceiver();
			_context.registerReceiver(netConnReceiver, new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION));

		}

		// creates the intents that are used to wake up the phone when it is
		// time to ping the server
		if (pingSender == null) {
			pingSender = new PingSender();
			_context.registerReceiver(pingSender, new IntentFilter(
					MQTT_PING_ACTION));
		}
	}

	public void onDestroy() {

		// disconnect immediately
		disconnectFromBroker();

		// inform the app that the app has successfully disconnected
		broadcastServiceStatus("Disconnected");

		// try not to leak the listener
		if (dataEnabledReceiver != null) {
			_context.unregisterReceiver(dataEnabledReceiver);
			dataEnabledReceiver = null;
		}

		if (mBinder != null) {
			mBinder.close();
			mBinder = null;
		}
	}

	/************************************************************************/
	/* METHODS - broadcasts and notifications */
	/************************************************************************/

	// methods used to notify the Activity UI of something that has happened
	// so that it can be updated to reflect status and the data received
	// from the server

	private void broadcastServiceStatus(String statusDescription) {
		// inform the app (for times when the Activity UI is running /
		// active) of the current MQTT connection status so that it
		// can update the UI accordingly
		// Intent broadcastIntent = new Intent();
		// broadcastIntent.setAction(MQTT_STATUS_INTENT);
		// broadcastIntent.putExtra(MQTT_STATUS_MSG, statusDescription);
		// _context.sendBroadcast(broadcastIntent);
		PushReportUtility.log("broadcastServiceStatus: " + statusDescription);
	}

	private void broadcastReceivedMessage(String topic, String message) {
		// pass a message received from the MQTT server on to the Activity UI
		// (for times when it is running / active) so that it can be displayed
		// in the app GUI
		// Intent broadcastIntent = new Intent();
		// broadcastIntent.setAction(MQTT_MSG_RECEIVED_INTENT);
		// broadcastIntent.putExtra(MQTT_MSG_RECEIVED_TOPIC, topic);
		// broadcastIntent.putExtra(MQTT_MSG_RECEIVED_MSG, message);
		// _context.sendBroadcast(broadcastIntent);
	}

	// methods used to notify the user of what has happened for times when
	// the app Activity UI isn't running

	private void notifyUser(String alert, String title, String body) {
		// NotificationManager nm = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
		// Notification notification = new Notification(R.drawable.icon, alert,
		// System.currentTimeMillis());
		// notification.defaults |= Notification.DEFAULT_LIGHTS;
		// notification.defaults |= Notification.DEFAULT_SOUND;
		// notification.defaults |= Notification.DEFAULT_VIBRATE;
		// notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// notification.ledARGB = Color.MAGENTA;
		// Intent notificationIntent = new Intent(this, PowerViewer.class);
		// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		// notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		// notification.setLatestEventInfo(this, title, body, contentIntent);
		// nm.notify(MQTT_NOTIFICATION_UPDATE, notification);
		PushReportUtility.log("alert: " + alert + " title: " + title + "body: "
				+ body);
	}

	/************************************************************************/
	/* METHODS - binding that allows access from the Actitivy */
	/************************************************************************/

	// trying to do local binding while minimizing leaks - code thanks to
	// Geoff Bruckner - which I found at
	// http://groups.google.com/group/cw-android/browse_thread/thread/d026cfa71e48039b/c3b41c728fedd0e7?show_docid=c3b41c728fedd0e7

	private LocalBinder<MQTTService> mBinder;

	// @Override
	// public IBinder onBind(Intent intent) {
	// return mBinder;
	// }

	public class LocalBinder<S> extends Binder {
		private WeakReference<S> mService;

		public LocalBinder(S service) {
			mService = new WeakReference<S>(service);
		}

		public S getService() {
			return mService.get();
		}

		public void close() {
			mService = null;
		}
	}

	//
	// public methods that can be used by Activities that bind to the Service
	//

	public MQTTConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public void rebroadcastStatus() {
		String status = "";

		switch (connectionStatus) {
		case INITIAL:
			status = "Please wait";
			break;
		case CONNECTING:
			status = "Connecting...";
			break;
		case CONNECTED:
			status = "Connected";
			break;
		case NOTCONNECTED_UNKNOWNREASON:
			status = "Not connected - waiting for network connection";
			break;
		case NOTCONNECTED_USERDISCONNECT:
			status = "Disconnected";
			break;
		case NOTCONNECTED_DATADISABLED:
			status = "Not connected - background data disabled";
			break;
		case NOTCONNECTED_WAITINGFORINTERNET:
			status = "Unable to connect";
			break;
		}

		//
		// inform the app that the Service has successfully connected
		broadcastServiceStatus(status);
	}

	public void disconnect() {
		disconnectFromBroker();

		// set status
		connectionStatus = MQTTConnectionStatus.NOTCONNECTED_USERDISCONNECT;

		// inform the app that the app has successfully disconnected
		broadcastServiceStatus("Disconnected");
	}

	/************************************************************************/
	/* METHODS - MQTT methods inherited from MQTT classes */
	/************************************************************************/

	/*
	 * callback - method called when we no longer have a connection to the
	 * message broker server
	 */
	public void connectionLost() throws Exception {
		// we protect against the phone switching off while we're doing this
		// by requesting a wake lock - we request the minimum possible wake
		// lock - just enough to keep the CPU running until we've finished
		System.out.println("connectionLost");
		PowerManager pm = (PowerManager) _context
				.getSystemService(Service.POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
		wl.acquire();

		//
		// have we lost our data connection?
		//

		if (isOnline() == false) {
			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;

			// inform the app that we are not connected any more
			broadcastServiceStatus("Connection lost - no network connection");

			//
			// inform the user (for times when the Activity UI isn't running)
			// that we are no longer able to receive messages
			notifyUser("Connection lost - no network connection", "MQTT",
					"Connection lost - no network connection");

			//
			// wait until the phone has a network connection again, when we
			// the network connection receiver will fire, and attempt another
			// connection to the broker
		} else {
			//
			// we are still online
			// the most likely reason for this connectionLost is that we've
			// switched from wifi to cell, or vice versa
			// so we try to reconnect immediately
			//

			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;

			// inform the app that we are not connected any more, and are
			// attempting to reconnect
			broadcastServiceStatus("Connection lost - reconnecting...");

			// try to reconnect
			if (connectToBroker()) {
				subscribeToTopic(topicName);
			}
		}

		// we're finished - if the phone is switched off, it's okay for the CPU
		// to sleep now
		wl.release();
	}

	/*
	 * callback - called when we receive a message from the server
	 */
	@Override
	public void publishArrived(String topic, byte[] payloadbytes, int qos,
			boolean retained) {
		// we protect against the phone switching off while we're doing this
		// by requesting a wake lock - we request the minimum possible wake
		// lock - just enough to keep the CPU running until we've finished
		PushReportUtility.log("publishArrived, topic: " + topic);
		PowerManager pm = (PowerManager) _context
				.getSystemService(Service.POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
		wl.acquire();
		//
		// I'm assuming that all messages I receive are being sent as strings
		// this is not an MQTT thing - just me making as assumption about what
		// data I will be receiving - your app doesn't have to send/receive
		// strings - anything that can be sent as bytes is valid
		String reData = new String(payloadbytes);
		if (reData != null && reData.length() > 0) {

			SharedPreferences sp = _context.getSharedPreferences("app",
					Context.MODE_PRIVATE);
			String mAppId = sp.getString("appid", null);

			JSONObject json;
			try {
				String decryptedData = Rc4Encrypt.decry_RC4(reData, mAppId);
				json = new JSONObject(decryptedData);

				if (json.has("mdm") && json.getString("mdm") != null) {
					Intent intent = new Intent();
					intent.setAction(ACTION_MDM);
					intent.putExtra("mdmtoken", json.getString("mdm"));
					_context.sendBroadcast(intent);

				} else {

					String status = json.getString("status");
					if ("ok".equals(status)) {
						String messageList = json.getString("messageList");
						JSONArray jsonArray = new JSONArray(messageList);
						for (int i = 0; i < jsonArray.length(); i++) {
							// runningNotification(jsonArray.getJSONObject(i));
							if (pushDataCallback != null) {
								pushDataCallback.pushData(jsonArray
										.getJSONObject(i));
							}

						}

					}
				}

			} catch (Exception e) {
				PushReportUtility.oe("publishArrived", e);
				e.printStackTrace();
			}

		}

		// receiving this message will have kept the connection alive for us, so
		// we take advantage of this to postpone the next scheduled ping
		scheduleNextPing();

		// we're finished - if the phone is switched off, it's okay for the CPU
		// to sleep now
		wl.release();
	}

	/************************************************************************/
	/* METHODS - wrappers for some of the MQTT methods that we use */
	/************************************************************************/

	/*
	 * Create a client connection object that defines our connection to a
	 * message broker server
	 */
	private void defineConnectionToBroker(String brokerHostName) {
		String mqttConnSpec = "tcp://" + brokerHostName;
		PushReportUtility.log("defineConnectionToBroker: " + mqttConnSpec);
		try {
			// define the connection to the broker
			mqttClient = MqttClient.createMqttClient(mqttConnSpec,
					usePersistence);

			// register this client app has being able to receive messages
			mqttClient.registerSimpleHandler(this);
		} catch (MqttException e) {
			// something went wrong!
			mqttClient = null;
			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;
			//
			// inform the app that we failed to connect so that it can update
			// the UI accordingly
			broadcastServiceStatus("Invalid connection parameters");

			//
			// inform the user (for times when the Activity UI isn't running)
			// that we failed to connect
			notifyUser("Unable to connect", "MQTT", "Unable to connect");
		}
	}

	private String getMacAddress() {
		String macSerial = null;
		try {
			WifiManager wifi = (WifiManager) _context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			macSerial = info.getMacAddress().replaceAll(":", "");
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (macSerial == null) {
			macSerial = getMac().replaceAll(":", "");
			;
		}
		return macSerial;
	}

	private String getMac() {
		String macSerial = "";
		String str = "";
		try {
			Process pp = Runtime.getRuntime().exec(
					"cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);

			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macSerial = str.trim();// 去空格
					break;
				}
			}
		} catch (IOException ex) {
			// 赋予默认值
			ex.printStackTrace();
		}
		return macSerial;
	}

	/*
	 * (Re-)connect to the message broker
	 */
	private boolean connectToBroker() {
		try {
			// try to connect
			SharedPreferences sp = _context.getSharedPreferences("app",
					Context.MODE_PRIVATE);
			String mAppId = sp.getString("appid", null);
			mqttClient.connect(getMacAddress() + mAppId, cleanStart,
					keepAliveSeconds);

			//
			// inform the app that the app has successfully connected
			broadcastServiceStatus("Connected");

			// we are connected
			connectionStatus = MQTTConnectionStatus.CONNECTED;
			System.out.println("connectToBroker");
			// we need to wake up the phone's CPU frequently enough so that the
			// keep alive messages can be sent
			// we schedule the first one of these now
			scheduleNextPing();

			return true;
		} catch (Exception e) {
			// something went wrong!

			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;

			//
			// inform the app that we failed to connect so that it can update
			// the UI accordingly
			broadcastServiceStatus("Unable to connect");

			//
			// inform the user (for times when the Activity UI isn't running)
			// that we failed to connect
			notifyUser("Unable to connect", "MQTT",
					"Unable to connect - will retry later");

			// if something has failed, we wait for one keep-alive period before
			// trying again
			// in a real implementation, you would probably want to keep count
			// of how many times you attempt this, and stop trying after a
			// certain number, or length of time - rather than keep trying
			// forever.
			// a failure is often an intermittent network issue, however, so
			// some limited retry is a good idea
			scheduleNextPing();

			return false;
		}
	}

	/*
	 * Send a request to the message broker to be sent messages published with
	 * the specified topic name. Wildcards are allowed.
	 */
	private void subscribeToTopic(String topicName) {
		boolean subscribed = false;

		if (isAlreadyConnected() == false) {
			// quick sanity check - don't try and subscribe if we
			// don't have a connection

			Log.w("mqtt", "Unable to subscribe as we are not connected");
			PushReportUtility
					.log("subscribeToTopic: Unable to subscribe as we are not connected");
		} else {
			try {
				SharedPreferences sp = _context.getSharedPreferences("app",
						Context.MODE_PRIVATE);
				String mAppId = sp.getString("appid", null);
				String[] topics = new String[] { "push/" + mAppId,
						"push/" + _softToken };
				;
				// 此处因为上面的topic默认两个，若变更个数，此处log信息应注意处理
				PushReportUtility.log("subscribeToTopic: " + topics[0]);
				PushReportUtility.log("subscribeToTopic: " + topics[1]);
				mqttClient.subscribe(topics, qualitiesOfService);

				subscribed = true;
			} catch (Exception e) {
				PushReportUtility.oe("MQTT subscribeToTopic", e);
			}
		}

		if (subscribed == false) {
			//
			// inform the app of the failure to subscribe so that the UI can
			// display an error
			broadcastServiceStatus("Unable to subscribe");

			//
			// inform the user (for times when the Activity UI isn't running)
			notifyUser("Unable to subscribe", "MQTT", "Unable to subscribe");
		}
	}

	/*
	 * Terminates a connection to the message broker.
	 */
	private void disconnectFromBroker() {
		// if we've been waiting for an Internet connection, this can be
		// cancelled - we don't need to be told when we're connected now
		try {
			if (netConnReceiver != null) {
				_context.unregisterReceiver(netConnReceiver);
				netConnReceiver = null;
			}

			if (pingSender != null) {
				_context.unregisterReceiver(pingSender);
				pingSender = null;
			}
		} catch (Exception e) {
			// probably because we hadn't registered it
			PushReportUtility.oe("disconnectFromBroker", e);
		}

		try {
			if (mqttClient != null) {
				mqttClient.disconnect();
			}
		} catch (MqttPersistenceException e) {
			PushReportUtility.oe("disconnectFromBroker", e);
		} finally {
			mqttClient = null;
		}

		// we can now remove the ongoing notification that warns users that
		// there was a long-running ongoing service running
		NotificationManager nm = (NotificationManager) _context
				.getSystemService(Service.NOTIFICATION_SERVICE);
		nm.cancelAll();
	}

	/*
	 * Checks if the MQTT client thinks it has an active connection
	 */
	private boolean isAlreadyConnected() {
		return ((mqttClient != null) && (mqttClient.isConnected() == true));
	}

	private class BackgroundDataChangeIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			// we protect against the phone switching off while we're doing this
			// by requesting a wake lock - we request the minimum possible wake
			// lock - just enough to keep the CPU running until we've finished
			PowerManager pm = (PowerManager) _context
					.getSystemService(Service.POWER_SERVICE);
			WakeLock wl = pm
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
			wl.acquire();

			ConnectivityManager cm = (ConnectivityManager) _context
					.getSystemService(Service.CONNECTIVITY_SERVICE);
			if (cm.getBackgroundDataSetting()) {
				// user has allowed background data - we start again - picking
				// up where we left off in handleStart before
				defineConnectionToBroker(brokerHostName);
				// handleStart(intent, 0);
			} else {
				// user has disabled background data
				connectionStatus = MQTTConnectionStatus.NOTCONNECTED_DATADISABLED;

				// update the app to show that the connection has been disabled
				broadcastServiceStatus("Not connected - background data disabled");

				// disconnect from the broker
				disconnectFromBroker();
			}

			// we're finished - if the phone is switched off, it's okay for the
			// CPU
			// to sleep now
			wl.release();
		}
	}

	/*
	 * Called in response to a change in network connection - after losing a
	 * connection to the server, this allows us to wait until we have a usable
	 * data connection again
	 */
	private class NetworkConnectionIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			// we protect against the phone switching off while we're doing this
			// by requesting a wake lock - we request the minimum possible wake
			// lock - just enough to keep the CPU running until we've finished
			PowerManager pm = (PowerManager) _context
					.getSystemService(Service.POWER_SERVICE);
			WakeLock wl = pm
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
			wl.acquire();

			if (isOnline()) {
				// we have an internet connection - have another try at
				// connecting
				if (connectToBroker()) {
					// we subscribe to a topic - registering to receive push
					// notifications with a particular key
					subscribeToTopic(topicName);
				}
			}

			// we're finished - if the phone is switched off, it's okay for the
			// CPU
			// to sleep now
			wl.release();
		}
	}

	/*
	 * Schedule the next time that you want the phone to wake up and ping the
	 * message broker server
	 */
	private void scheduleNextPing() {
		// When the phone is off, the CPU may be stopped. This means that our
		// code may stop running.
		// When connecting to the message broker, we specify a 'keep alive'
		// period - a period after which, if the client has not contacted
		// the server, even if just with a ping, the connection is considered
		// broken.
		// To make sure the CPU is woken at least once during each keep alive
		// period, we schedule a wake up to manually ping the server
		// thereby keeping the long-running connection open
		// Normally when using this Java MQTT client library, this ping would be
		// handled for us.
		// Note that this may be called multiple times before the next scheduled
		// ping has fired. This is good - the previously scheduled one will be
		// cancelled in favour of this one.
		// This means if something else happens during the keep alive period,
		// (e.g. we receive an MQTT message), then we start a new keep alive
		// period, postponing the next ping.

		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(_context, 0, new Intent(MQTT_PING_ACTION),
						PendingIntent.FLAG_UPDATE_CURRENT);

		// in case it takes us a little while to do this, we try and do it
		// shortly before the keep alive period expires
		// it means we're pinging slightly more frequently than necessary
		Calendar wakeUpTime = Calendar.getInstance();
		wakeUpTime.add(Calendar.SECOND, keepAliveSeconds);

		AlarmManager aMgr = (AlarmManager) _context
				.getSystemService(Service.ALARM_SERVICE);
		aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(),
				pendingIntent);
	}

	/*
	 * Used to implement a keep-alive protocol at this Service level - it sends
	 * a PING message to the server, then schedules another ping after an
	 * interval defined by keepAliveSeconds
	 */
	public class PingSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Note that we don't need a wake lock for this method (even though
			// it's important that the phone doesn't switch off while we're
			// doing this).
			// According to the docs, "Alarm Manager holds a CPU wake lock as
			// long as the alarm receiver's onReceive() method is executing.
			// This guarantees that the phone will not sleep until you have
			// finished handling the broadcast."
			// This is good enough for our needs.

			try {
				mqttClient.ping();
			} catch (MqttException e) {
				// if something goes wrong, it should result in connectionLost
				// being called, so we will handle it there
				PushReportUtility.oe("PingSender ping failed", e);

				// assume the client connection is broken - trash it
				try {
					mqttClient.disconnect();
				} catch (MqttPersistenceException e1) {
					PushReportUtility.oe("PingSender disconnect failed", e);
				}

				// reconnect
				if (connectToBroker()) {
					subscribeToTopic(topicName);
				}
			}

			// start the next keep alive period
			scheduleNextPing();
		}
	}

	/************************************************************************/
	/* APP SPECIFIC - stuff that would vary for different uses of MQTT */
	/************************************************************************/

	// apps that handle very small amounts of data - e.g. updates and
	// notifications that don't need to be persisted if the app / phone
	// is restarted etc. may find it acceptable to store this data in a
	// variable in the Service
	// that's what I'm doing in this sample: storing it in a local hashtable
	// if you are handling larger amounts of data, and/or need the data to
	// be persisted even if the app and/or phone is restarted, then
	// you need to store the data somewhere safely
	// see http://developer.android.com/guide/topics/data/data-storage.html
	// for your storage options - the best choice depends on your needs

	// stored internally

	private Hashtable<String, String> dataCache = new Hashtable<String, String>();

	private boolean addReceivedMessageToStore(String key, String value) {
		String previousValue = null;

		if (value.length() == 0) {
			previousValue = dataCache.remove(key);
		} else {
			previousValue = dataCache.put(key, value);
		}

		// is this a new value? or am I receiving something I already knew?
		// we return true if this is something new
		return ((previousValue == null) || (previousValue.equals(value) == false));
	}

	// provide a public interface, so Activities that bind to the Service can
	// request access to previously received messages

	public void rebroadcastReceivedMessages() {
		Enumeration<String> e = dataCache.keys();
		while (e.hasMoreElements()) {
			String nextKey = e.nextElement();
			String nextValue = dataCache.get(nextKey);

			broadcastReceivedMessage(nextKey, nextValue);
		}
	}

	/************************************************************************/
	/* METHODS - internal utility methods */
	/************************************************************************/

	// private String generateClientId() {
	// // generate a unique client id if we haven't done so before, otherwise
	// // re-use the one we already have
	//
	// if (mqttClientId == null) {
	// // generate a unique client ID - I'm basing this on a combination of
	// // the phone device id and the current timestamp
	// String timestamp = "" + (new Date()).getTime();
	// String android_id = Settings.System.getString(getContentResolver(),
	// Secure.ANDROID_ID);
	// mqttClientId = timestamp + android_id;
	//
	// // truncate - MQTT spec doesn't allow client ids longer than 23
	// // chars
	// if (mqttClientId.length() > MAX_MQTT_CLIENTID_LENGTH) {
	// mqttClientId = mqttClientId.substring(0,
	// MAX_MQTT_CLIENTID_LENGTH);
	// }
	// }
	//
	// return mqttClientId;
	// }

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) _context
				.getSystemService(Service.CONNECTIVITY_SERVICE);
		// 防止下面条件语句中的低几率null异常崩溃
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()
				&& networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
