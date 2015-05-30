package com.trackeirb.peer.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a file that wrap content either received from
 * tracker/another peer or stored into the local storage.
 * 
 * @author Elian ORIOU
 * 
 */

public class PeerFile implements Cloneable {

	// Header data

	private String key;

	private String name;

	private int size;

	private int pieceSize;

	// Data

	private File physicalFile;

	private BufferMap bufferMap;

	private List<FilePiece> pieces;

	/**
	 * Load a file from local storage (the file is complete) => SEEDED FILE The
	 * buffer map is completely filled, because it's a complete file (then all
	 * pieces are present). If the seeder is the peer itself (a seeded file),
	 * the attribute seeder is set to NULL.
	 * 
	 * @param key
	 * @param name
	 * @param size
	 * @param pieceSize
	 * @param seeder
	 * @param f
	 */

	public PeerFile(String key, String name, int size, int pieceSize,
			Peer seeder, File f) {
		this.key = key;
		this.name = name;
		this.size = size;
		this.pieceSize = pieceSize;
		this.physicalFile = f;
		this.bufferMap = new BufferMap(((int) size / pieceSize) + 1);
		this.bufferMap.set(0, ((int) size / pieceSize) + 1);
		this.pieces = Collections.synchronizedList(new ArrayList<FilePiece>());
	}

	/**
	 * Creates a peer file based on the IO file header data
	 * 
	 * @param key
	 * @param pieceSize
	 * @param seeder
	 * @param f
	 */

	public PeerFile(String key, int pieceSize, Peer seeder, File f) {
		this(key, f.getName(), (int) f.length(), pieceSize, seeder, f);
	}

	/**
	 * Create a file when the peer receive the first data from network (the file
	 * is empty) => LEECHED FILE
	 * 
	 * @param key
	 * @param name
	 * @param size
	 * @param pieceSize
	 * @param seeder
	 */

	public PeerFile(String key, String name, int size, int pieceSize,
			Peer seeder) {
		this(key, name, size, pieceSize, seeder, null);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public void setPhysicalFile(File f) {
		this.physicalFile = f;
		this.size = (int) physicalFile.length();
		this.bufferMap = new BufferMap((int) physicalFile.length() / pieceSize);
	}

	public File getPhysicalFile() {
		return physicalFile;
	}

	public BufferMap getBufferMap() {
		return bufferMap;
	}

	public void setBufferMap(BufferMap map) {
		this.bufferMap = map;
	}

	public List<FilePiece> getPieces() {
		return pieces;
	}

	public void addPiece(FilePiece piece) {
		pieces.add(piece);
		bufferMap.set(piece.getPos());
	}

	@Override
	public String toString() {
		return "PeerFile [key=" + key + ", name=" + name + ", size=" + size
				+ ", pieceSize=" + pieceSize + ", physicalFile=" + physicalFile
				+ "]";
	}

	public PeerFile clone() throws CloneNotSupportedException {
		return (PeerFile) super.clone();
	}
}
