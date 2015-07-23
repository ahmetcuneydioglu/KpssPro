package com.arkay.quizpower.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.arkay.quizpower.bean.Question;
import com.arkay.quizpower.bean.SingleAnswareLevelInfo;
import com.arkay.quizpower.playquizbeans.PlayQuizQuestion;

/**
 * Data access object to read data from local database. if you choose local or not question from form web.
 * then this class read question from local sqlite. 
 * @author Arkay
 *
 */
public class QuestionsDAO {
	private ArrayList<Question> singleAnswreQuestions = new ArrayList<Question>();
	
	private String packageName;
	
	private static final String DATABASE_NAME = "database.db";
	public QuestionsDAO(String packageName){
		this.packageName = packageName;
	}
	
	public List<Question> getSingleAnswareQuestion(int quizLevel){
		//quizLevel--;
		//int start = quizLevel * 20;
		//int end = start + 20;
		//String sql = "select * from questions where question_id  >= "+start+" and question_id <= "+end ;
		String sql = "select * from questions where quiz_level = "+quizLevel ;
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + packageName + "/databases/"	+ DATABASE_NAME, null, 0);
        Cursor cursor = db.rawQuery(sql, null);
        int i=1;
        if (cursor.moveToFirst()){ // data?
         	do{
        	      Question question = new Question(i, cursor.getString(cursor.getColumnIndex("question")));
        	      String rightAns = cursor.getString(cursor.getColumnIndex("right_ans"));
        	      if(rightAns.equalsIgnoreCase("A")){
        	    	  question.setAnsware(cursor.getString(cursor.getColumnIndex("option_a")));
	         		}else if(rightAns.equalsIgnoreCase("B")){
	         			question.setAnsware(cursor.getString(cursor.getColumnIndex("option_b")));
	         		}else if(rightAns.equalsIgnoreCase("C")){
	         			question.setAnsware(cursor.getString(cursor.getColumnIndex("option_c")));
	         		}else{
	         			question.setAnsware(cursor.getString(cursor.getColumnIndex("option_d")));
	         		}
        	      System.out.println(question);
        	      singleAnswreQuestions.add(question);
        	      i++;
        	   }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return singleAnswreQuestions;
	}
	public ArrayList<SingleAnswareLevelInfo> getTotalSingleAnswareQuestionLevel(){
		
		ArrayList<SingleAnswareLevelInfo>  returnLelveInfo = new ArrayList<SingleAnswareLevelInfo>();
		String sql = "select * from level_info";
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + packageName + "/databases/"	+ DATABASE_NAME, null, 0);
        Cursor cursor = db.rawQuery(sql, null);
       
        if (cursor.moveToFirst()){
        	do{
	        	SingleAnswareLevelInfo temp = new SingleAnswareLevelInfo(Integer.parseInt(cursor.getString(cursor.getColumnIndex("level_id"))), cursor.getString(cursor.getColumnIndex("level_name")));
	        	returnLelveInfo.add(temp);
        	}while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return returnLelveInfo;
		}
	public List<PlayQuizQuestion> getFourOptionQuestionRendom(int noOfQuestion){
		List<PlayQuizQuestion> playQuizquestions = new ArrayList<PlayQuizQuestion>();
		int total = noOfQuestion +5;
		String sql = "select *  FROM questions   ORDER BY RANDOM() LIMIT "+total;
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/" + packageName + "/databases/"	+ DATABASE_NAME, null, 0);
        Cursor cursor = db.rawQuery(sql, null);
        int i=1;
        if (cursor.moveToFirst()){ 
         	do{
         		PlayQuizQuestion question = new PlayQuizQuestion(cursor.getString(cursor.getColumnIndex("question")));
         		question.addOption(cursor.getString(cursor.getColumnIndex("option_a")));
         		question.addOption(cursor.getString(cursor.getColumnIndex("option_b")));
         		question.addOption(cursor.getString(cursor.getColumnIndex("option_c")));
         		question.addOption(cursor.getString(cursor.getColumnIndex("option_d")));
         		
         		String rightAns = cursor.getString(cursor.getColumnIndex("right_ans"));
         		
         		if(rightAns.equalsIgnoreCase("A")){
         			question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_a")));
         		}else if(rightAns.equalsIgnoreCase("B")){
         			question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_b")));
         		}else if(rightAns.equalsIgnoreCase("C")){
         			question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_c")));
         		}else{
         			question.setTrueAns(cursor.getString(cursor.getColumnIndex("option_d")));
         		}
         		
         		if(question.getOptions().size()==4){
         			playQuizquestions.add(question);
         		}
        	      i++;
        	   }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        Collections.shuffle(playQuizquestions);
        playQuizquestions = playQuizquestions.subList(0, noOfQuestion);
        return playQuizquestions;
	}

	
}
