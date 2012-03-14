package org.comu.advertiseme;

/* references
 * 	http://www.hardill.me.uk/wordpress/?p=204
 *  http://dalelane.co.uk/blog/?p=1599 - for more complete example using a service for MQTT
 *  http://saeedsiam.blogspot.com/2009/02/first-look-into-android-thread.html - handler example
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.NoSuchPaddingException;

import org.comu.advertiseme.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;

public class HelloMQTTActivity extends Activity {
	/** Called when the activity is first created. */
	private String android_id;
	private MqttClient client;
	private TextView topicView;
	private TextView messageView;
	private String iv = "fedcba9876543210";// Dummy iv (CHANGE IT!)
	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;
	private Cipher dcipher;
	private String Key = "1234567812345678";
	final static String broker = "tcp://10.0.2.2:1883";

	private ArrayList<HashMap<String, String>> applicaationAtribute;
	static String name = "name";
	static String dataType = "dataType";
	static String url = "url";
	static String appType = "appType";
	static String time = "time";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// to do fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);
		topicView = (TextView) findViewById(R.id.topic);
		messageView = (TextView) findViewById(R.id.message);
		android_id = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		connect();
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			topicView.setText(msg.getData().getString("topic"));
			messageView.setText(msg.getData().getString("message"));
		}
	};

	private boolean connect() {
		try {
			topicView.setText("waiting for message");
			messageView.setText(android_id);

			client = (MqttClient) MqttClient.createMqttClient(broker, null);
			client.registerSimpleHandler(new MessageHandler());
			client.connect("HM" + android_id, true, (short) 240);

			String topics[] = { "aaa" };
			int qos[] = { 1 };
			client.subscribe(topics, qos);
			return true;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unused")
	private class MessageHandler implements MqttSimpleCallback {

		public void publishArrived(String _topic, byte[] payload, int qos,
				boolean retained) throws Exception {
			String _message = new String(payload);
			Bundle b = new Bundle();
			String decryptMessage = new String(decrypt(_message));

			// we got encryted data and decrypted now we have xml data to
			// parsing

			XMLfunctions parser = new XMLfunctions();
			Document doc = parser.getDomElement(decryptMessage);// getting DOM
																// element
			NodeList nl = doc.getElementsByTagName("ApplicationAttributes");
			// looping through all item nodes <item>
			applicaationAtribute = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < nl.getLength(); i++) {

				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();
				Element e = (Element) nl.item(i);

				// adding each child node to HashMap key => value
				map.put(name, parser.getValue(e, name));
				Log.d("126", parser.getValue(e, name));
				map.put(dataType, parser.getValue(e, dataType));
				Log.d("127", parser.getValue(e, dataType));
				map.put(url, parser.getValue(e, url));
				Log.d("128", parser.getValue(e, url));
				map.put(time, parser.getValue(e, time));
				Log.d("129", parser.getValue(e, time));

				// adding HashList to ArrayList
				applicaationAtribute.add(map);

			}

			Message msg = handler.obtainMessage();
			Log.d("MQTT", _message);

			appType = applicaationAtribute.get(0).get("dataType");

			b.putString("topic", appType);
			b.putString("message", applicaationAtribute.get(0).get("url"));

			Message msg1 = handler.obtainMessage();
			msg.setData(b);

			handler.sendMessage(msg1);
			Log.d("MQTT", _message);

			if (appType.equals("video")) {

				// Starting a new Intent
				Intent nextScreen = new Intent(getApplicationContext(),
						videoStreaming.class);

				// Sending data to another Activity
				nextScreen.putExtra("url",
						applicaationAtribute.get(0).get("url"));
				startActivity(nextScreen);

				// test edilecek
				// moveTaskToBack (true);
			}
			if (appType.equals("web")) {

				// Starting a new Intent
				Intent nextScreen1 = new Intent(getApplicationContext(),
						WebViewActivity.class);

				// Sending data to another Activity
				nextScreen1.putExtra("url",
						applicaationAtribute.get(0).get("url"));
				startActivity(nextScreen1);

				// test edilecek
				// moveTaskToBack (true);
			}
			
			
			if (appType.equals("image")) {

				// Starting a new Intent
				Intent nextScreen2 = new Intent(getApplicationContext(),
						ImageManager.class);

				// Sending data to another Activity
				nextScreen2.putExtra("url",
						applicaationAtribute.get(0).get("url"));
				startActivity(nextScreen2);

				// test edilecek
				// moveTaskToBack (true);
			}

		}

		public void connectionLost() throws Exception {
			client = null;
			Log.v("HelloMQTT", "connection dropped");
			Thread t = new Thread(new Runnable() {

				public void run() {
					do {// pause for 5 seconds and try again;
						Log.v("HelloMQTT",
								"sleeping for 10 seconds before trying to reconnect");
						try {
							Thread.sleep(10 * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} while (!connect());
					System.err.println("reconnected");
				}
			});
		}

		// to turn byte incomming hex
		public byte[] hexToBytes(String str) {
			if (str == null) {
				return null;
			} else if (str.length() < 2) {
				return null;
			} else {
				int len = str.length() / 2;
				byte[] buffer = new byte[len];
				for (int i = 0; i < len; i++) {
					buffer[i] = (byte) Integer.parseInt(
							str.substring(i * 2, i * 2 + 2), 16);
				}
				System.out.println(buffer.toString());
				return buffer;
			}
		}

		// decrypting incomming encoded hex string
		public byte[] decrypt(String code) throws Exception {
			if (code == null || code.length() == 0)
				throw new Exception("Empty string");
			byte[] decrypted = null;

			try {
				ivspec = new IvParameterSpec(iv.getBytes());
				keyspec = new SecretKeySpec(Key.getBytes(), "AES");
				dcipher = Cipher.getInstance("AES/CBC/NoPadding");
				dcipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
				Log.v("131", code);

				decrypted = dcipher.doFinal(hexToBytes(code));
				Log.v("133", code);
			} catch (Exception e) {
				throw new Exception("[decrypt] " + e.getMessage());
			}
			return decrypted;
		}
	}
}