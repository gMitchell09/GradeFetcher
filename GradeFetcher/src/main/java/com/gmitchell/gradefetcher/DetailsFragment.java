package com.gmitchell.gradefetcher;

import android.app.ListFragment;
import android.content.Context;
import android.database.DataSetObservable;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mitchellge on 11/19/13.
 */
public class DetailsFragment extends ListFragment {

	JSONArray mCategories;
	int mIndex;

    public static DetailsFragment newInstance(int index, JSONArray assignments) {
        DetailsFragment f = new DetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
	    args.putString("assignments", assignments.toString());
	    f.setArguments(args);

	    f.mCategories = assignments;
	    f.mIndex = index;

	    Log.e("DetailsFragment", f.mCategories.toString());

        return f;
    }

	public static DetailsFragment newInstance(int index, String assignments) {
	        DetailsFragment f = new DetailsFragment();

	        // Supply index input as an argument.
	        Bundle args = new Bundle();
	        args.putInt("index", index);
		    args.putString("assignments", assignments.toString());
		    f.setArguments(args);

		try {
			f.mCategories = new JSONArray(assignments);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		f.mIndex = index;

		    Log.e("DetailsFragment", f.mCategories.toString());

	        return f;
	    }

    public int getShownIndex() {
		Bundle args = getArguments();
	    if (args != null)
            return this.mIndex;
	    else
		    return -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
//	        return null;
        }

	    ExpandableListView listView = new ExpandableListView(getActivity());

		if (this.mCategories != null) {
	        Log.e("DetailsFragment", "\n --- " + this.mCategories.toString() + " --- \n\n");
			listView.setAdapter(new ExpandableClassAssignmentAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, 0, R.layout.class_grades_assignment_layout, this.mCategories));
		}
	    else {
			Log.wtf("DetailsFragment", "----------------------\n------------------------------\n--- WTF'nF!?!?!?!-------------\n-----------------\n");
		}

        return listView;
    }

	public class ExpandableClassAssignmentAdapter extends BaseExpandableListAdapter {
		private ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String, String>>();
		private String[] mCategoryNames;
		private DataSetObservable dataSetObservable = new DataSetObservable();
		private Context context;
		private Integer groupClosedView;
		private Integer groupExpandedView;
		private Integer childView;
		private LayoutInflater inflater;

		private JSONArray categories;

		public ExpandableClassAssignmentAdapter(Context context, int groupClosedView, int groupExpandedView,
		                                        int childView, JSONArray categories) {
			this.context = context;
			this.groupClosedView = new Integer(groupClosedView);
			this.groupExpandedView = new Integer(groupExpandedView);
			this.childView = new Integer(childView);

			this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			this.categories = categories;
		}


		@Override
		public int getGroupCount() {
			return this.categories.length();
		}

		@Override
		public int getChildrenCount(int i) {
			try {
				return this.categories.getJSONObject(i).getJSONArray("assignments").length();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return 0;
		}

		@Override
		public Object getGroup(int i) {
			try {
				return this.categories.getJSONObject(i).getString("name");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public Object getChild(int i, int i2) {
			try {
				return this.categories.getJSONObject(i).getJSONArray("assignments").getJSONObject(i2);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getGroupId(int i) {
			return i;
		}

		@Override
		public long getChildId(int i, int i2) {
			return i * i2;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View child = inflater.inflate(this.groupClosedView, null);

			TextView className = (TextView) child.findViewById(android.R.id.text1);
			className.setText(this.getGroup(groupPosition).toString());

			return child;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//			if (convertView == null) {
//				convertView = new View(this.context);
//			}
//
			View baseView = inflater.inflate(this.childView, null);

			TextView className = (TextView)baseView.findViewById(R.id.assignName);
			TextView classGrade = (TextView)baseView.findViewById(R.id.assignGrade);

			JSONObject jsonObject = (JSONObject)this.getChild(groupPosition, childPosition);

			try {
				className.setText(jsonObject.getString("name"));
			} catch (JSONException e) {
				e.printStackTrace();
				className.setText("Fuck This");
			}
			try {
				if (jsonObject.getDouble("maxPoints") != 0)
					classGrade.setText(new DecimalFormat("###.##%").format(jsonObject.getDouble("score")/jsonObject.getDouble("maxPoints")));
				else
					classGrade.setText(String.valueOf(jsonObject.getDouble("score")) + "/" + String.valueOf(jsonObject.getDouble("maxPoints")));
			} catch (JSONException e) {
				e.printStackTrace();
				classGrade.setText("Fuck this too");
			}

			if (!this.isChildSelectable(groupPosition, childPosition)) {
				className.setTextColor(Color.LTGRAY);
				classGrade.setTextColor(Color.LTGRAY);
			}
			else {
				final double numer, denom;
				double tmpN, tmpD;
				try {
					tmpN = jsonObject.getDouble("score");
					tmpD = jsonObject.getDouble("maxPoints");
					/////////////
				} catch (JSONException e) {
					tmpD = 1;
					tmpN = 0;
				}
				numer = tmpN;
				denom = tmpD;

				classGrade.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (((TextView)view).getText().toString().contains("/")) {
							if (denom != 0)
								((TextView)view).setText(new DecimalFormat("###.##%").format(numer/denom));
						} else {
							((TextView)view).setText(String.valueOf(numer) + "/" + String.valueOf(denom));
						}
					}
				});
			}

			return baseView;
		}

		@Override
		public boolean isChildSelectable(int i, int i2) {
			JSONObject json = (JSONObject)this.getChild(i, i2);

			try {
				if(json.getInt("Graded") != 0)
					return true;
				else
					return false;
			} catch (JSONException e) {
				return false;
			}
		}
	}
}
