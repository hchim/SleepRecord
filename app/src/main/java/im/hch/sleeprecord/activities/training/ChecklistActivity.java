package im.hch.sleeprecord.activities.training;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.utils.ActivityUtils;

public class ChecklistActivity extends AppCompatActivity {

    private ChecklistAdapter checklistAdapter;
    private LayoutInflater mInflater;
    private MenuItem menuItem;
    private int checkedNumber = 0;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.checklist) ListView checklistView;

    @BindArray(R.array.sleep_training_checklist) String[] checklist;
    @BindArray(R.array.sleep_training_checklist_description) String[] checklistDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        ButterKnife.bind(this);

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checklistAdapter = new ChecklistAdapter();
        checklistView.setAdapter(checklistAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sleep_training_checklist, menu);
        menuItem = toolbar.getMenu().getItem(0);
        menuItem.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_training_next) {
            ActivityUtils.navigateToPlanningActivity(this);
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
                convertView = mInflater.inflate(R.layout.list_item_checklist, parent, false);
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
