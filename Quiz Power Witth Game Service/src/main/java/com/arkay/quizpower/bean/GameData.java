package com.arkay.quizpower.bean;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

import com.arkay.quizpower.MenuHomeScreenActivity;
/**
 * Game Data that will store on Google cloud.
 * You can add more data if we want that need to store on cloud. 
 * @author Arkay
 *
 */
public class GameData {
	
	 // serialization format version
    private static final String SERIAL_VERSION = "1.1";
    
	private int totalScore;
	private int levelCompleted=1;
	private int countHowManyTimePlay;
	private int countHowManyQuestionCompleted;
	private int countHowManyRightAnswareQuestion;
	
	 /** Constructs a SaveGame object from serialized data. */
    public GameData(byte[] data) {
        if (data == null) return; // default progress
        loadFromString(new String(data));
    }
    
    
	public GameData() {
		// TODO Auto-generated constructor stub
	}

	 /** Save this SaveGame object to a SharedPreferences. */
    public void save(SharedPreferences sp, String key) {
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(key, toString());
        spe.commit();
    }
    
	private void loadFromString(String json) {
		 if (json == null || json.trim().equals("")) return;
		 
		 try {
	            JSONObject obj = new JSONObject(json);
	            String format = obj.getString("version");
	            if (!format.equals(SERIAL_VERSION)) {
	                throw new RuntimeException("Unexpected loot format " + format);
	            }
	            JSONObject gameData = obj.getJSONObject("score");
	            this.setTotalScore(gameData.getInt(MenuHomeScreenActivity.TOTAL_SCORE));
	            this.setLevelCompleted(gameData.getInt(MenuHomeScreenActivity.LEVEL_COMPLETED));
	            this.setCountHowManyTimePlay(gameData.getInt(MenuHomeScreenActivity.HOW_MANY_TIMES_PLAY_QUIZ));
	            this.setCountHowManyQuestionCompleted(gameData.getInt(MenuHomeScreenActivity.COUNT_QUESTION_COMPLETED));
	            this.setCountHowManyRightAnswareQuestion(gameData.getInt(MenuHomeScreenActivity.COUNT_RIGHT_ANSWARE_QUESTIONS));
	           
	        }
	        catch (JSONException ex) {
	            ex.printStackTrace();
	            //throw new RuntimeException("Save data has a syntax error: " + json, ex);
	        }
	        catch (NumberFormatException ex) {
	            ex.printStackTrace();
	           // throw new RuntimeException("Save data has an invalid number in it: " + json, ex);
	        }
		
	}

	/** Constructs a SaveGame object by reading from a SharedPreferences. */
    public GameData(SharedPreferences sp, String key) {
    	loadFromString(sp.getString(key, ""));
    }

    
	 /** Serializes this SaveGame to an array of bytes. */
    public byte[] toBytes() {
        return toString().getBytes();
    }
    
	@Override
	public String toString() {
		  try {
	            JSONObject gameData = new JSONObject();
	            gameData.put(MenuHomeScreenActivity.TOTAL_SCORE, getTotalScore());
	            gameData.put(MenuHomeScreenActivity.LEVEL_COMPLETED, getLevelCompleted());
	            gameData.put(MenuHomeScreenActivity.HOW_MANY_TIMES_PLAY_QUIZ, getCountHowManyTimePlay());
	            gameData.put(MenuHomeScreenActivity.COUNT_QUESTION_COMPLETED, getCountHowManyQuestionCompleted());
	            gameData.put(MenuHomeScreenActivity.COUNT_RIGHT_ANSWARE_QUESTIONS, getCountHowManyRightAnswareQuestion());
	            
	            JSONObject obj = new JSONObject();
	            obj.put("version", SERIAL_VERSION);
	            obj.put("score", gameData);
	            return obj.toString();
	        }
	        catch (JSONException ex) {
	            ex.printStackTrace();
	            throw new RuntimeException("Error converting save data to JSON.", ex);
	        }
	}

	/*public void saveDataLocal(SharedPreferences settings){
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(MenuHomeScreenActivity.LEVEL_COMPLETED,getLevelCompleted());
		editor.putInt(MenuHomeScreenActivity.TOTAL_SCORE, getTotalScore());
		editor.putInt(MenuHomeScreenActivity.HOW_MANY_TIMES_PLAY_QUIZ,getCountHowManyTimePlay());
		editor.putInt(MenuHomeScreenActivity.COUNT_QUESTION_COMPLETED,getCountHowManyQuestionCompleted());
		editor.putInt(MenuHomeScreenActivity.COUNT_RIGHT_ANSWARE_QUESTIONS,getCountHowManyRightAnswareQuestion());
		editor.commit();
	}*/
	
	/**
     * Computes the union of this SaveGame with the given SaveGame. The union will have any
     * levels present in either operand. If the same level is present in both operands,
     * then the number of stars will be the greatest of the two.
     *
     * @param other The other operand with which to compute the union.
     * @return The result of the union.
     */
    public GameData unionWith(GameData other) {
    	GameData result = clone();
    	int existingPoint = result.getCountHowManyQuestionCompleted();
        int newPoint = other.getCountHowManyQuestionCompleted();
        if (newPoint > existingPoint) {
        	result.setCountHowManyQuestionCompleted(newPoint);
        }
        
        existingPoint = result.getCountHowManyRightAnswareQuestion();
        newPoint = other.getCountHowManyRightAnswareQuestion();
        if (newPoint > existingPoint) {
        	result.setCountHowManyRightAnswareQuestion(newPoint);
        }

        
        existingPoint = result.getCountHowManyTimePlay();
        newPoint = other.getCountHowManyTimePlay();
        if (newPoint > existingPoint) {
        	result.setCountHowManyTimePlay(newPoint);
        }
        
        existingPoint = result.getLevelCompleted();
        newPoint = other.getLevelCompleted();
        if (newPoint > existingPoint) {
        	result.setLevelCompleted(newPoint);
        }

        existingPoint = result.getTotalScore();
        newPoint = other.getTotalScore();
        if (newPoint > existingPoint) {
        	result.setTotalScore(newPoint);
        }
        return result;
    }
    
    /** Returns a clone of this SaveGame object. */
    public GameData clone() {
    	GameData result = new GameData();
    	result.setCountHowManyQuestionCompleted(countHowManyQuestionCompleted);
    	result.setCountHowManyRightAnswareQuestion(countHowManyRightAnswareQuestion);
    	result.setCountHowManyTimePlay(countHowManyTimePlay);
    	result.setLevelCompleted(levelCompleted);
    	result.setTotalScore(totalScore);
    	return result;
    }
	public int getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	public int getLevelCompleted() {
		return levelCompleted;
	}
	public void setLevelCompleted(int levelCompleted) {
		this.levelCompleted = levelCompleted;
	}
	public int getCountHowManyTimePlay() {
		return countHowManyTimePlay;
	}
	public void setCountHowManyTimePlay(int countHowManyTimePlay) {
		this.countHowManyTimePlay = countHowManyTimePlay;
	}
	public int getCountHowManyQuestionCompleted() {
		return countHowManyQuestionCompleted;
	}
	public void setCountHowManyQuestionCompleted(int countHowManyQuestionCompleted) {
		this.countHowManyQuestionCompleted = countHowManyQuestionCompleted;
	}
	public int getCountHowManyRightAnswareQuestion() {
		return countHowManyRightAnswareQuestion;
	}
	public void setCountHowManyRightAnswareQuestion(
			int countHowManyRightAnswareQuestion) {
		this.countHowManyRightAnswareQuestion = countHowManyRightAnswareQuestion;
	}
	
	
	
	
}
