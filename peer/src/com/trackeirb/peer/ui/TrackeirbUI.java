package com.trackeirb.peer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.domain.Peer;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.exceptions.InvalidResponseException;
import com.trackeirb.peer.network.PeerClientConnector;
import com.trackeirb.peer.network.PeerClientsBundle;
import com.trackeirb.peer.protocol.ActionExecutor;

/**
 * Trackeirb UI Class
 * 
 * @author Elian ORIOU
 * 
 */

public class TrackeirbUI extends JFrame {

	private static TrackeirbUI _INSTANCE;
	private static final long serialVersionUID = 1L;

	private JPanel rootPanel;
	private JPanel buttonPanel;
	private JPanel progressPanel;

	private JList peersList;
	private DefaultListModel model;

	private JButton searchFilesButton;
	private JButton settingsButton;
	private JButton quitButton;

	private Map<String, JProgressBar> displayedFiles;

	private TrackeirbUI() {
		super(ConfigurationManager.getInstance().getProperty(
				Constants._PEER_HOST_CONF_KEY, "localhost")
				+ ":"
				+ ConfigurationManager.getInstance().getPropertyAsInt(
						Constants._PEER_PORT_CONF_KEY, 60001));
		displayedFiles = new HashMap<String, JProgressBar>();
		render();
		pack();
	}

	public static TrackeirbUI getInstance() {
		if (null == _INSTANCE) {
			_INSTANCE = new TrackeirbUI();
		}
		return _INSTANCE;
	}

	public void restart() {

		displayedFiles.clear();
		setVisible(false);
		render();
		pack();
		setVisible(true);
	}

	private void render() {

		rootPanel = new JPanel();
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

		buttonPanel = new JPanel();
		searchFilesButton = new JButton("Search files");
		settingsButton = new JButton("Settings");
		quitButton = new JButton("Quit");

		buttonPanel.add(searchFilesButton);
		buttonPanel.add(settingsButton);
		buttonPanel.add(quitButton);

		progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

		model = new DefaultListModel();
		peersList = new JList(model);

		rootPanel.add(buttonPanel);
		rootPanel.add(new JLabel("Leeched files : "));
		rootPanel.add(progressPanel);
		rootPanel.add(new JLabel("Connected peers :"));
		rootPanel.add(peersList);

		setListeners();
		setContentPane(rootPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void setListeners() {

		searchFilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestTracker();
			}
		});

		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});

		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				System.exit(0);
			}
		});
	}

	private void requestTracker() {

		String criterions = JOptionPane.showInputDialog("Criterions ?");
		if (criterions == null || criterions.isEmpty()) {
			return;
		}
		PeerClientConnector connector = new PeerClientConnector(
				ConfigurationManager.getInstance().getProperty(
						Constants._TRACKER_HOST_CONF_KEY, "localhost"),
				ConfigurationManager.getInstance().getPropertyAsInt(
						Constants._TRACKER_PORT_CONF_KEY, 60001));
		try {
			List<PeerFile> files = ActionExecutor.getInstance().getFileList(
					connector, criterions);
			if (files == null) {
				return;
			}

			// 1. Let the user choose files into the proposed list
			StringBuilder userStringList = new StringBuilder(
					"Select a file, and give us file id(s) separed by ','\n");
			int i = 0;
			for (PeerFile file : files) {
				userStringList.append(" - " + (i++) + " - " + file.getName()
						+ "\n");
			}
			String userAnswer = JOptionPane.showInputDialog(userStringList
					.toString());
			if (userAnswer == null || userAnswer.isEmpty()) {
				return;
			}
			List<PeerFile> selectedFiles = new ArrayList<PeerFile>();
			for (String pos : userAnswer.split(",")) {
				PeerFile file = files.get(Integer.parseInt(pos));
				if (file == null) {
					continue;
				}
				selectedFiles.add(file);
			}

			// 2. Start dispatching selected files by peer
			ActionExecutor.getInstance().dispatchByPeer(connector,
					selectedFiles);
		} catch (InvalidResponseException e1) {
			e1.printStackTrace();
		}
	}

	public void refreshPeersList() {
		model.clear();

		for (Peer peer : PeerClientsBundle.getInstance().getConnectors()
				.values()) {
			model.addElement(peer);
		}

		peersList.revalidate();
		peersList.repaint();
	}

	public void addFileProgressToView(PeerFile file) {

		if (displayedFiles.get(file.getKey()) != null) {
			return;
		}

		JProgressBar bar = new JProgressBar();
		bar.setMaximum(file.getBufferMap().realLength());
		bar.setValue(file.getBufferMap().cardinality());
		bar.setStringPainted(true);
		bar.setString(file.getName());

		progressPanel.add(bar);

		invalidate();
		validate();
		pack();

		synchronized (displayedFiles) {
			displayedFiles.put(file.getKey(), bar);
		}
	}

	public void removeFileProgressFromView(PeerFile file) {

		JProgressBar bar;
		synchronized (displayedFiles) {
			bar = displayedFiles.remove(file.getKey());
		}
		if (null == bar) {
			return;
		}

		progressPanel.remove(bar);
		invalidate();
		validate();
	}

	public void refreshFileProgress(PeerFile file, int piecesAcquired) {

		JProgressBar bar;
		synchronized (displayedFiles) {
			bar = displayedFiles.get(file.getKey());
		}
		if (null == bar) {
			return;
		}

		bar.setValue(piecesAcquired);
		invalidate();
		validate();
		pack();
	}
}
