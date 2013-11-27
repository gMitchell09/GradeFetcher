package com.gmitchell.gradefetcher;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mitchellge on 11/18/13.
 */
public class ClassListFragment extends ListFragment {
	boolean mDualPane;
	int mCurCheckPosition = 0;
	JSONObject mJSONObject;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mJSONObject = new JSONObject(activity.getIntent().getStringExtra("json"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final JSONArray classList;

		try {
			classList = mJSONObject.getJSONArray("Classes");
			final ArrayList<String> list = new ArrayList<String>();
			for (int i=0; i<classList.length(); i++) {
				list.add(classList.getString(i));
			}

			setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_selectable_list_item, list));
			getListView().setBackgroundColor(Color.LTGRAY);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		View detailsFrame = getActivity().findViewById(R.id.classGrades_fragment);

		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
		}
		Log.e("ClassListFragment", (mDualPane==true)?"True":"False");

		if (mDualPane) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			//showDetails(mCurCheckPosition);
		}
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
        showDetails(position);
    }

	void showDetails(int index) {
		mCurCheckPosition = index;

		if (mDualPane) {
			getListView().setItemChecked(index, true);
			DetailsFragment details = (DetailsFragment) getFragmentManager().findFragmentById(R.id.classGrades_fragment);

			if (details == null || details.getShownIndex() != index) {
				JSONArray categoryArray = null;
				try {
					categoryArray = mJSONObject.getJSONArray(mJSONObject.getJSONArray("Classes").getString(index));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				details = DetailsFragment.newInstance(index, categoryArray);

				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
				if (true || index == 0) {
					fragmentTransaction.replace(R.id.classGrades_container, details);
					LinearLayout otherFragment = (LinearLayout)((LinearLayout) getView().getParent()).findViewById(R.id.classGrades_container);
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)(otherFragment.getLayoutParams());
					params.weight = 4.0f;
					otherFragment.setLayoutParams(params);

				} else {
					//fragmentTransaction.replace(R.id.a_item, details);
				}

				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.commit();
			}
		} else {
			JSONArray categoryArray = null;
			try {
				categoryArray = mJSONObject.getJSONArray(mJSONObject.getJSONArray("Classes").getString(index));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Intent intent = new Intent();
			intent.setClass(getActivity(), DetailsActivity.class);
			intent.putExtra("index", index);
			intent.putExtra("assignments", categoryArray.toString());

			startActivity(intent);
		}
	}
}
