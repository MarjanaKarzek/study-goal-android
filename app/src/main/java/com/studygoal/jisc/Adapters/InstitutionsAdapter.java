package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.Institution;
import com.studygoal.jisc.R;

import java.util.ArrayList;
import java.util.List;

public class InstitutionsAdapter extends BaseAdapter {
    private static final String TAG = InstitutionsAdapter.class.getSimpleName();

    private List<Institution> institutions;
    private LayoutInflater inflater;
    private Context context;

    public InstitutionsAdapter(Context context) {
        this.context = context;
        institutions = new ArrayList<>();
        inflater = LayoutInflater.from(this.context);
    }

    public void updateItems(List<Institution> items) {
        if (items != null && items.size() > 0) {
            institutions.clear();
            institutions.addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return institutions.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.institution_item, viewGroup, false);
        }

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setTypeface(DataManager.getInstance().myriadpro_regular);

        // set manually to change university name
        // original code
        // name.setText(institutions.get(i).name);
        //it's hard coded part. so after web service updated.  it need to be removed.
        //hard code start
        String tempName = institutions.get(i).name;

        if (tempName.toLowerCase().contains("Gloucestershire".toLowerCase())) {
            name.setText("University of Gloucestershire");
        } else if (tempName.toLowerCase().contains("Oxford Brookes".toLowerCase())) {
            name.setText("Oxford Brookes University");
        } else if (tempName.toLowerCase().contains("South Wales".toLowerCase())) {
            name.setText("University of South Wales | Prifysgol De Cymru");
        } else if (tempName.toLowerCase().contains("Strathclyde".toLowerCase())) {
            name.setText("University of Strathclyde");
        } else {
            name.setText(institutions.get(i).name);
        }

        //hard code end

        name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        view.setTag(institutions.get(i));
        return view;
    }
}
