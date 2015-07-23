package com.arkay.quizpower.playquizbeans;

import java.util.ArrayList;

/**
 * This class is for question that is question and it's option with it't true answar.
 * @author Arkay Apps
 *
 */
public class PlayQuizQuestion {
	private String question;
	private ArrayList<String> options = new ArrayList<String>();
	private String trueAns;
	
	public PlayQuizQuestion(String question) {
		super();
		this.question = question;
	}


	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public boolean addOption(String option){
		return this.options.add(option);
	}
	

	public ArrayList<String> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<String> options) {
		this.options = options;
	}

	
	public String getTrueAns() {
		return trueAns;
	}

	public void setTrueAns(String trueAns) {
		this.trueAns = trueAns;
	}

	@Override
	public String toString() {
		return "Question: "+ question +" OptionS: "+options;
	}
	
	
	
		
}
