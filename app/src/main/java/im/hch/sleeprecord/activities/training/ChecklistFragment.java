package im.hch.sleeprecord.activities.training;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import butterknife.BindArray;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;

public class ChecklistFragment extends BaseFragment {

    private ChecklistAdapter checklistAdapter;
    private LayoutInflater layoutInflater;
    private LinearLayoutManager layoutManager;
    private int checkedNumber = 0;

    @BindView(R.id.checklist) RecyclerView checklistView;
    @BindView(R.id.fab) FloatingActionButton floatingActionButton;

    @BindString(R.string.sleep_training_checklist_title) String title;
    @BindArray(R.array.sleep_training_checklist) String[] checklist;
    @BindArray(R.array.sleep_training_checklist_description) String[] checklistDesc;

    public static ChecklistFragment newInstance() {
        return new ChecklistFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.layoutInflater = inflater;

        View view = inflater.inflate(R.layout.fragment_checklist, container, false);
        ButterKnife.bind(this, view);

        mainActivity.setTitle(title);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        checklistView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mainActivity);
        checklistView.setLayoutManager(layoutManager);
        checklistAdapter = new ChecklistAdapter();
        checklistView.setAdapter(checklistAdapter);
        floatingActionButton.setVisibility(View.GONE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.loadFragment(PlanningFragment.newInstance(), null);
            }
        });
        return view;
    }

    private class ChecklistAdapter extends RecyclerView.Adapter<ChecklistViewHolder> {

        @Override
        public int getItemCount() {
            return checklist.length;
        }

        @Override
        public ChecklistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = layoutInflater.inflate(R.layout.list_item_checklist, parent, false);
            ChecklistViewHolder viewHolder = new ChecklistViewHolder(convertView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ChecklistViewHolder viewHolder, int position) {
            viewHolder.update(checklist[position], checklistDesc[position]);
        }
    }

    class ChecklistViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @BindView(R.id.checkBoxView) CheckBox checkBox;
        @BindView(R.id.titleView) TextView titleView;
        @BindView(R.id.subtitleView) TextView subTitleView;

        public ChecklistViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ButterKnife.bind(this, itemView);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkedNumber++;
                        if (checkedNumber == checklist.length) {
                            floatingActionButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        checkedNumber--;
                        floatingActionButton.setVisibility(View.GONE);
                    }
                }
            });
        }

        public void update(String title, String subTitle) {
            titleView.setText(title);
            subTitleView.setText(subTitle);
        }
    }
}
