package com.arkay.quizpower.bean;

public class SingleAnswareLevelInfo {
	private int levelNo;
	private String levelName;
	
	
	public SingleAnswareLevelInfo(int levelNo, String levelName) {
		this.levelNo = levelNo;
		this.levelName = levelName;
	}
	
	public int getLevelNo() {
		return levelNo;
	}
	public void setLevelNo(int levelNo) {
		this.levelNo = levelNo;
	}
	public String getLevelName() {
		return levelName;
	}
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	
	
	
}
