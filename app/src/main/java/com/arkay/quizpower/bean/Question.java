package com.arkay.quizpower.bean;

/**
 * This is class is use for Single answare question and it's have just question and single answare
 * @author Arkay
 *
 */
public class Question {
	private int questionNo;
	private String question;
	private String answare;
	
	public Question(int questionNo, String question) {
		super();
		this.questionNo = questionNo;
		this.question = question;
	}
	
	public int getQuestionNo() {
		return questionNo;
	}
	public void setQuestionNo(int questionNo) {
		this.questionNo = questionNo;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnsware() {
		return answare;
	}
	public void setAnsware(String answare) {
		this.answare = answare;
	}

	@Override
	public String toString() {
		return question +" "+answare;
	}
	
	
}
