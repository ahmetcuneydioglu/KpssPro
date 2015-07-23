package com.arkay.quizpower;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.arkay.quizpower.bean.SingleAnswareLevelInfo;
import com.arkay.quizpower.dao.QuestionsDAO;

/**
 * Display Level on Single Answare activity.
 * @author Arkay
 *
 */
public class LevelActivity extends Activity {

	ArrayList<SingleAnswareLevelInfo> questions;
	private ListView listView;
	private AllListAdaptor adapter;
	private Context context;
	
	private ProgressDialog progress;
	String questionJson = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_level);
		this.context = this;

		questions = new ArrayList<SingleAnswareLevelInfo>();
		QuestionsDAO questionDao = new QuestionsDAO(getPackageName());
		Resources ress = getResources();
		boolean isQuestionFromWeb = ress.getBoolean(R.bool.isQuestionFormWeb); 
		if(isQuestionFromWeb){
			progress = new ProgressDialog(this);
	        progress.setTitle("Please Wait!!");
	        progress.setMessage("Data Loading..");
	        progress.setCancelable(false);
	        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        progress.show();
			LoadQuestions task = new LoadQuestions();
			task.execute(new String[] { ress.getString(R.string.get_level_info) });
		}else{
			questions = questionDao.getTotalSingleAnswareQuestionLevel();
			setAllValue();
		}
		
	}
	
	
	public void setAllValue(){
		
		listView = (ListView) findViewById(R.id.list);
		listView.setCacheColorHint(Color.TRANSPARENT);

		adapter = new AllListAdaptor(this, questions);
		listView.setAdapter(adapter);

		registerForContextMenu(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent playQuiz = new Intent(context, SingleAnsQuizActivity.class);
				//position++;
				playQuiz.putExtra("level_no", position);
				startActivity(playQuiz);

			}

		});
	}

	private class LoadQuestions extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						questionJson += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					
					JSONArray users = new JSONArray(questionJson);
					for (int i = 0; i < users.length(); i++) {
						JSONObject c = users.getJSONObject(i);
						//totalLevel = Integer.parseInt(c.getString("nooflevel"));
						SingleAnswareLevelInfo tempQuestion = new SingleAnswareLevelInfo(Integer.parseInt(c.getString("lelveno")),c.getString("levelname"));
						questions.add(tempQuestion);
					}
					
				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data" + e.toString());
				}
			}
			return questionJson;
		}

		@Override
		protected void onPostExecute(String result) {
			if(questions.size()==0){
				QuestionsDAO questionDao = new QuestionsDAO(getPackageName());
				questions = questionDao.getTotalSingleAnswareQuestionLevel();
			}
			
			progress.cancel();
			setAllValue();
		}
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}
	
}
