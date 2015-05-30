package com.trackeirb.peer.domain;

public class FilePiece implements Comparable<FilePiece> {

	private int pos;
	private byte[] data;
	private int length;

	public FilePiece(int pos, int len, byte[] data) {
		this.pos = pos;
		this.data = data;
		this.length = len;
	}

	public int getPos() {
		return pos;
	}

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] bytes) {
		this.data = bytes;
	}

	@Override
	public String toString() {
		return new String(data);
	}

	@Override
	public int compareTo(FilePiece o) {
		if (this.getPos() > o.getPos()) {
			return 1;
		}
		if (this.getPos() < o.getPos()) {
			return -1;
		}
		if (o.getPos() == this.getPos()) {
			return 0;
		}
		return -10;
	}
}
