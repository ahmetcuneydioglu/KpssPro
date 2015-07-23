package com.arkay.quizpower.bean;

/**
 * Bean of QuizLeve whatever user play which level. according to this level we get question from question bank.
 * @author I-BALL
 *
 */
public class QuizLevel {

	private String levelName;
	private int levelNo=0;
	private int questionNO=0;
	
	
	public QuizLevel(int levelNo, String levelName) {
		super();
		this.levelName = levelName;
		this.levelNo = levelNo;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public int getLevelNo() {
		return levelNo;
	}

	public void setLevelNo(int levelNo) {
		this.levelNo = levelNo;
	}

	public int getQuestionNO() {
		return questionNO;
	}

	public void setQuestionNO(int questionNO) {
		this.questionNO = questionNO;
	}

	
	
}
