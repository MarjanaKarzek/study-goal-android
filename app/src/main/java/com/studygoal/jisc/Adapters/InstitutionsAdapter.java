package com.studygoal.jisc.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

/**
 * Institutions Adapter
 * <p>
 * Handles institution items.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class InstitutionsAdapter extends RecyclerView.Adapter<InstitutionsAdapter.ViewHolder> {

    private static final String TAG = InstitutionsAdapter.class.getSimpleName();

    private List<Institution> institutions;
    private LayoutInflater inflater;
    private Context context;

    public InstitutionsAdapter(Context context) {
        this.context = context;
        institutions = new ArrayList<>();
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public InstitutionsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = inflater.from(viewGroup.getContext()).inflate(R.layout.list_item_institution, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InstitutionsAdapter.ViewHolder viewHolder, int i) {
        final Institution item = institutions.get(i);

        String tempName = item.name;
        if (tempName.toLowerCase().contains("Gloucestershire".toLowerCase())) {
            viewHolder.name.setText("University of Gloucestershire");
        } else if (tempName.toLowerCase().contains("Oxford Brookes".toLowerCase())) {
            viewHolder.name.setText("Oxford Brookes University");
        } else if (tempName.toLowerCase().contains("South Wales".toLowerCase())) {
            viewHolder.name.setText("University of South Wales | Prifysgol De Cymru");
        } else if (tempName.toLowerCase().contains("Strathclyde".toLowerCase())) {
            viewHolder.name.setText("University of Strathclyde");
        } else {
            viewHolder.name.setText(item.name);
        }
    }

    @Override
    public int getItemCount() {
        return institutions.size();
    }

    /**
     * Gets the item at the specified position.
     *
     * @param position position of the item
     * @return item
     */
    public Institution getItem(int position) {
        return institutions.get(position);
    }

    /**
     * Changes the current list of items to the given list.
     *
     * @param items list to be exchanged
     */
    public void updateItems(List<Institution> items) {
        if (items != null && items.size() > 0) {
            institutions.clear();
            institutions.addAll(items);
            notifyDataSetChanged();
        }
    }

    /**
     * View holder for the recycler view of institutions.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Institution currentItem;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            name.setTypeface(DataManager.getInstance().myriadpro_regular);
            name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            //view.setTag(currentItem);
        }
    }
}
