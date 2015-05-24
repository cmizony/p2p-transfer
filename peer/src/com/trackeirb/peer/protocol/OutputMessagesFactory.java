package com.trackeirb.peer.protocol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import com.trackeirb.peer.domain.FilePiece;
import com.trackeirb.peer.domain.PeerFile;

public class OutputMessagesFactory {

	// Singleton pattern
	private static OutputMessagesFactory _INSTANCE;

	private OutputMessagesFactory() {

	}

	public static OutputMessagesFactory getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new OutputMessagesFactory();
		}
		return _INSTANCE;
	}

	// ////////////////////////////////////////////////
	// /// CLIENT MODE : MESSAGE SENT TO THE TRACKER
	// ////////////////////////////////////////////////

	public String getAnnouncementMessage(int port, List<PeerFile> seededFiles,
			List<PeerFile> leechedFiles) {
		// announce > ok
		StringBuilder announce = new StringBuilder();
		announce.append("announce listen ");
		announce.append(port + " seed [");
		int i = 0;
		if (seededFiles != null) {
			for (PeerFile file : seededFiles) {
				announce.append(file.getName() + " ");
				announce.append(file.getSize() + " ");
				announce.append(file.getPieceSize() + " ");
				announce.append(file.getKey());
				if (i++ < seededFiles.size() - 1) {
					announce.append(" ");
				}
			}
		}
		announce.append("] leech [");
		i = 0;
		if (leechedFiles != null) {
			for (PeerFile file : leechedFiles) {
				announce.append(file.getKey());
				if (i++ < seededFiles.size() - 1) {
					announce.append(" ");
				}
			}
		}
		announce.append("]");
		return announce.toString();
	}

	public String getFileListMessage(String... criterions) {
		// look > list
		StringBuilder message = new StringBuilder();
		message.append("look [");
		int i = 0;
		for (String criterion : criterions) {
			message.append(criterion);
			if (i++ < criterions.length - 1) {
				message.append(" ");
			}
		}
		message.append("]");
		return message.toString();
	}

	public String getPeerListMessage(String fileKey) {
		// getfile > peers
		return "getfile " + fileKey;
	}

	// ////////////////////////////////////////////////
	// /// CLIENT MODE : MESSAGE SENT TO ANOTHER PEER
	// ////////////////////////////////////////////////

	public String getFileBufferMapMessage(String fileKey) {
		// > interested (< have)
		return "interested " + fileKey;
	}

	public String getPiecesMessage(String fileKey, Integer... pieces) {
		// > getpieces (< data)
		StringBuilder message = new StringBuilder();
		message.append("getpieces " + fileKey + " [");
		int i = 0;
		for (int piece : pieces) {
			message.append(piece);
			if (i++ < pieces.length - 1) {
				message.append(" ");
			}
		}
		message.append("]");
		return message.toString();
	}

	public String getUpdateMessage(List<PeerFile> seededFiles,
			List<PeerFile> leechedFiles) {

		StringBuilder update = new StringBuilder();

		update.append("update seed [");
		int i = 0;
		if (seededFiles != null) {
			for (PeerFile file : seededFiles) {
				update.append(file.getKey());
				if (i++ < seededFiles.size() - 1) {
					update.append(" ");
				}
			}
		}
		update.append("] leech [");
		i = 0;
		if (leechedFiles != null) {
			for (PeerFile file : leechedFiles) {
				update.append(file.getKey());
				if (i++ < seededFiles.size() - 1) {
					update.append(" ");
				}
			}
		}
		update.append("]");
		return update.toString();
	}

	// ////////////////////////////////////////////////
	// /// SERVER MODE : MESSAGE SENT TO ANOTHER PEER
	// ////////////////////////////////////////////////

	public String returnHaveListMessage(PeerFile f) {
		// (> interested ) < have
		return "have " + f.getKey() + " " + f.getBufferMap().getStringForm();
	}

	public String returnDataMessage(PeerFile f, List<Integer> pieces) {
		// (> getpieces) < data
		if (f == null || f.getPhysicalFile() == null) {
			return null;
		}
		StringBuilder message = new StringBuilder();
		message.append("data " + f.getKey() + " [");
		try {
			InputStream reader = new FileInputStream(f.getPhysicalFile());
			int i = 0;
			for (int piece : pieces) {
				// Append the pieceID and the piece data into the message (into
				// binary format)
				FilePiece p = f.getPieces().get(piece);
				// Creates a positive BigInteger (1)
				BigInteger binary = new BigInteger(1, p.getData());
				String stringForm = binary.toString(2);
				message.append(piece + ":" + stringForm);

				// Append spaces except when it is the last element
				if (i++ < pieces.size() - 1) {
					message.append(" ");
				}
			}
			// Close the file stream
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		message.append("]");
		return message.toString();
	}
}
