package im.hch.sleeprecord.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.AddRecordActivity;
import im.hch.sleeprecord.activities.records.SleepRecordsActivity;
import im.hch.sleeprecord.activities.records.SleepRecordsAdapter;
import im.hch.sleeprecord.activities.settings.SettingsActivity;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.utils.SessionManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SleepRecordsAdapter sleepRecordsAdapter;
    private SessionManager sessionManager;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.list_view) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //TODO replace these default data with real data
        List<Pair<Date, Date>> sleepTimes = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date begin, end;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 15);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 20);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 33);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 20);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 30);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        //test data
        ArrayList<SleepRecord> records = new ArrayList<>();
        for (int i = 27; i >= 25; i--) {
            SleepRecord sleepRecord = new SleepRecord(11, i);
            sleepRecord.setSleepTimePairs(sleepTimes);
            records.add(sleepRecord);
        }

        sleepRecordsAdapter = new SleepRecordsAdapter(this, records);
        listView.setAdapter(sleepRecordsAdapter);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_sleep_record) {
            openAddRecordActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_records:
                openSleepRecordsActivity();
                break;
            case R.id.nav_sleep_training:
                //TODO add sleep training activity
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_settings:
                openSettingsActivity();
                break;
            case R.id.nav_logout:
                sessionManager.logoutUser();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void openAddRecordActivity() {
        startActivity(new Intent(this, AddRecordActivity.class));
    }

    private void openSleepRecordsActivity() {
        startActivity(new Intent(this, SleepRecordsActivity.class));
    }
}
