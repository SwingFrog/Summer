package com.swingfrog.summer.web.view;

public class BlankView extends TextView {

	public static BlankView of() {
		return new BlankView();
	}

	public BlankView() {
		super("");
	}
	
	@Override
	public String toString() {
		return "BlankView";
	}
}
