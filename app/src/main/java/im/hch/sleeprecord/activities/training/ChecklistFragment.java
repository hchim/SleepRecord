package im.hch.sleeprecord.activities.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
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
    private MenuItem menuItem;
    private int checkedNumber = 0;

    @BindView(R.id.checklist) ListView checklistView;

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

        checklistAdapter = new ChecklistAdapter();
        checklistView.setAdapter(checklistAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sleep_training_checklist, menu);
        menuItem = mainActivity.getToolbar().getMenu().getItem(0);
        menuItem.setEnabled(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_training_next) {
            mainActivity.loadFragment(PlanningFragment.newInstance(), null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ChecklistAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return checklist.length;
        }

        @Override
        public Object getItem(int position) {
            return checklist[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChecklistViewHolder viewHolder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_checklist, parent, false);
                viewHolder = new ChecklistViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChecklistViewHolder) convertView.getTag();
            }

            viewHolder.update(checklist[position], checklistDesc[position]);
            return convertView;
        }
    }

    class ChecklistViewHolder {
        View itemView;
        @BindView(R.id.checkBoxView) CheckBox checkBox;
        @BindView(R.id.titleView) TextView titleView;
        @BindView(R.id.subtitleView) TextView subTitleView;

        ChecklistViewHolder(View itemView) {
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkedNumber++;
                        if (checkedNumber == checklist.length) {
                            menuItem.setEnabled(true);
                        }
                    } else {
                        checkedNumber--;
                        menuItem.setEnabled(false);
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
