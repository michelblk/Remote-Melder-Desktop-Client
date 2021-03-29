package de.michelblank.home.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;

public class Main {
	static final String topic = "/feuerwehr/alarm";
	static int qos = 1;
	static final String broker = "tcp://192.168.0.200:1883";
	static final String brokerUsername = "Username";
	static final String brokerPassword = "Password";
	
	
	private static Notification fenster = null;
	private static Notification statusBenachrichtigung = null;
	private static SystemTray tray = null;
	private static TrayIcon trayIcon = null;
	private static PopupMenu trayMenu = null;
	static int verbunden = -1;
	static String clientId = null;
	static MemoryPersistence persistence = new MemoryPersistence();
	static MqttClient client = null;
	static MqttCallback mqttCallback = new MqttCallback() {

		@Override
		public void connectionLost(Throwable t) {
			System.out.println("Verbindungsfehler");
			waitForConnection();
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
		}

		@Override
		public void messageArrived(String topic, MqttMessage arg1) throws Exception {
			// nicht benutzt
		}
	};

	public static void main(String[] args) {
		systemTrayIcon(false);
		waitForConnection();

		while (true) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {

			}
		}
	}

	private static synchronized void reconnect() {
		clientId = ;

		try {
			client = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setAutomaticReconnect(true);
			connOpts.setConnectionTimeout(5);
			connOpts.setKeepAliveInterval(30);
			connOpts.setUserName(brokerUsername);
			connOpts.setPassword(brokerPassword.toCharArray());
			client.setCallback(mqttCallback);

			client.connect(connOpts);

			try {
				client.subscribe(topic, qos, new IMqttMessageListener() {
					@Override
					public void messageArrived(String topic, MqttMessage message) throws Exception {
						System.out.println("Neue Nachricht in \"" + topic + "\": " + new String(message.getPayload()));
						if (new String(message.getPayload()).equals("1")) {
							fenster = new Notification("ALARM", true);
						}
					}
				});
				verbunden = 1;
				statusBenachrichtigung("Verbunden");
			} catch (MqttException e) {
				e.printStackTrace();
			}
		} catch (MqttException me) {
			/*System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me); */
			// me.printStackTrace();
			// new Notification("FF: " + me.getMessage(), 2);
		}
	}

	private static void waitForConnection() {
		if (verbunden != 0) {
			systemTrayIcon(false);
			statusBenachrichtigung("Keine Verbindung");
			verbunden = 0;
		}
		while (client == null || !client.isConnected()) {
			reconnect();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {

			}
		}
		systemTrayIcon(true);
	}
	
	private static void statusBenachrichtigung (String status) {
		if(statusBenachrichtigung != null) {
			statusBenachrichtigung.close();
		}
		statusBenachrichtigung = new Notification(status, false, 4);
	}
	
	private static void systemTrayIcon (boolean connected) {
		if(!SystemTray.isSupported()) {
			// Nicht unterstuetzt
			System.out.println("System Train nicht unterstuetzt");
			return;
		}
		
		if(tray == null) {
			tray = SystemTray.getSystemTray();
			trayMenu = new PopupMenu();
			
			MenuItem exitItem = new MenuItem("Beenden");
			exitItem.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					exit();
				}
			});
			trayMenu.add(exitItem);
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/img/default.png")), "FF Service", trayMenu);
			
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				
			}
		}
		
		if(connected) {
			trayIcon.setImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/img/connected.png")));
			trayIcon.setToolTip("FF Service Verbunden");
		}else {
			trayIcon.setImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/img/disconnected.png")));
			trayIcon.setToolTip("FF Service Getrennt");
		}
		trayIcon.setImageAutoSize(true);
		System.out.println(connected);
		
		
	}
	
	public static void exit () {
		if(fenster != null) fenster.close();
		if(statusBenachrichtigung != null) statusBenachrichtigung.close();
		try {
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			
		}
		System.exit(0);
	}
}
