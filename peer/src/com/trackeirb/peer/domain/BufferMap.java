package com.trackeirb.peer.domain;

import java.util.BitSet;

public class BufferMap extends BitSet {

	private static final long serialVersionUID = 1L;
	private int nbits;

	public BufferMap() {
		super();
	}

	public BufferMap(int nbits) {
		super(nbits);
		this.nbits = nbits;
	}

	/**
	 * Returns String form
	 * 
	 * @return string form buffer map
	 */

	public String getStringForm() {
		StringBuilder stringForm = new StringBuilder();
		for (int i = 0; i < length(); i++) {
			boolean value = get(i);
			if (value) {
				stringForm.append(1);
			} else {
				stringForm.append(0);
			}
		}
		return stringForm.toString();
	}

	/**
	 * Imports BufferMap from String
	 * 
	 * @param str
	 * @return
	 */

	public BufferMap stringToBuffermap(String str) {
		// Reset the bit mask
		clear();

		// Analyze characters and set bits
		int i = 0;
		for (char c : str.toCharArray()) {
			switch (c) {
			case '0':
				set(i++, false);
				break;
			case '1':
				set(i++, true);
				break;
			}
		}
		return this;
	}

	/**
	 * It's a kind of HACK but it returns the size defined into the
	 * constructor... and not a magical value dropped from nowhere !
	 * 
	 * @return
	 */

	public int realLength() {
		return nbits;
	}
}
