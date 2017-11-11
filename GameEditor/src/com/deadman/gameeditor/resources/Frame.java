package com.deadman.gameeditor.resources;

class Frame implements Comparable<Frame> {
	public int number;
	public int delay;
	public int totalDelay;
	public Drawable picture;

	public Frame(int n, int d, Drawable p) {
		number = n;
		delay = d;
		picture = p;
	}

	@Override
	public int compareTo(Frame o) {
		return this.number - o.number;
	}
}