package com.gmitchell.gradefetcher;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mitchellge on 11/14/13.
 */
public class ClassTableActivity extends Activity {
	private JSONObject mJSONObject;


//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		return inflater.inflate(R.layout.activity_classes, container, false);
//	}

	protected void onCreate(Bundle savedInstanceState) {
		// Onward!
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_classes);

		try {
			mJSONObject = new JSONObject(getIntent().getStringExtra("json"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("JSON", mJSONObject.toString());

		//setupListView();
	}

//	private void setupListView() {
//		final ListView listView = (ListView)findViewById(R.id.listView);
//		JSONArray classList = null;
//		try {
//			classList = mJSONObject.getJSONArray("Classes");
//
//			final ArrayList<String> list = new ArrayList<String>();
//			for (int i=0; i<classList.length(); i++) {
//				list.add(classList.getString(i));
//			}
//
//			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.class_row, R.id.secondLine, list);
//
//			listView.setAdapter(adapter);
//
//			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
//					// do something fancy later
//				}
//			});
//			Log.wtf("JSON", "WE MADE IT!!!!" + listView.getItemAtPosition(0));
//			listView.invalidate();
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

	private void showClass(String className) {
		try {
			JSONArray categoryList = mJSONObject.getJSONArray(className);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
