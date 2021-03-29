package de.michelblank.home.mqtt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class Notification extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JFrame frame = new JFrame();
	private Calendar dateOfCreation = Calendar.getInstance();

	public Notification(String message, boolean fullscreen) {
		newNotification(message, fullscreen);
	}
	
	public Notification(String message, boolean fullscreen, int timeout) {
		this(message, fullscreen);
		setTimeout(() -> this.close(), timeout * 1000);
	}

	private void newNotification(String message, boolean fullscreen) {
		int fontSize = 20;
		if(fullscreen) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			fontSize = 30;
		}else {
			frame.setSize(300, 200);
			Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
			Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
			frame.setLocation(scrSize.width - frame.getWidth(), scrSize.height - toolHeight.bottom - frame.getHeight());
		}
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setBackground(new Color(1.0f, 0.0f, 0.0f, 0.7f));
		frame.setTitle("FF: " + message);

		frame.setLayout(new BorderLayout());

		// Elemente hinzuf�gen
		JLabel timeLabel = new JLabel(String.format("%02d:%02d:%02d", dateOfCreation.get(Calendar.HOUR_OF_DAY),
				dateOfCreation.get(Calendar.MINUTE), dateOfCreation.get(Calendar.SECOND)));
		timeLabel.setFont(new Font("Arial", Font.PLAIN, fontSize));
		timeLabel.setForeground(Color.WHITE);
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(timeLabel, BorderLayout.PAGE_START);

		JLabel messageLabel = new JLabel(message);
		messageLabel.setFont(new Font("Arial", Font.PLAIN, (int)(fontSize*1.5)));
		messageLabel.setForeground(Color.WHITE);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(messageLabel, BorderLayout.CENTER);

		JButton schliessenButton = new JButton("Schlie�en");
		schliessenButton.addActionListener(this);
		schliessenButton.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(schliessenButton, BorderLayout.PAGE_END);

		// Sichtbar machen
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

	}

	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		this.close();
	}

	private static void setTimeout(Runnable runnable, int delay) {
		new Thread(() -> {
			try {
				Thread.sleep(delay);
				runnable.run();
			} catch (Exception e) {
				System.err.println(e);
			}
		}).start();
	}
}
