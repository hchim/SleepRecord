package im.hch.sleeprecord.activities.noise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.sleepaiden.androidcommonutils.media.AudioService;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;

public class WhiteNoiseFragment extends BaseFragment {

    @BindView(R.id.whiteNoiseImageButton) ImageButton whiteNoiseImageBtn;
    @BindView(R.id.rainImageButton) ImageButton rainImageBtn;
    @BindView(R.id.oceanImageButton) ImageButton oceanImageBtn;
    @BindView(R.id.wombImageButton) ImageButton wombImageBtn;
    @BindView(R.id.adWidget) LinearLayout adWidgetView;
    @BindView(R.id.playButton) ImageButton playButton;
    @BindView(R.id.timerButton) ImageButton timerButton;

    @BindString(R.string.nav_white_noise) String title;
    @BindDrawable(R.drawable.ic_play_48dp) Drawable playIcon;
    @BindDrawable(R.drawable.ic_pause_48dp) Drawable pauseIcon;

    private int[] sounds = {
        R.raw.white_noise,
        R.raw.rain,
        R.raw.ocean,
        R.raw.womb_sound
    };

    public static WhiteNoiseFragment newInstance() {
        WhiteNoiseFragment fragment = new WhiteNoiseFragment();
        return fragment;
    }

    private BroadcastReceiver audioStateBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioService.ACTION_PLAY_STATUS.equals(intent.getAction())) {
                AudioService.PlayStatus status =
                        AudioService.PlayStatus.valueOf(intent.getStringExtra(AudioService.EXTRA_PLAY_STATUS));
                setPlayButtonStatus(status);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_white_noise, container, false);
        ButterKnife.bind(this, view);

        mainActivity.setTitle(title);
        setupAdView(view, adWidgetView);
        whiteNoiseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(sounds[0], AudioService.PlayMode.Loop, 720);
            }
        });

        rainImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(sounds[1], AudioService.PlayMode.Loop, 720);
            }
        });
        oceanImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(sounds[2], AudioService.PlayMode.Loop, 720);
            }
        });
        wombImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(sounds[3], AudioService.PlayMode.Loop, 720);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playButton.getDrawable() == playIcon) {
                    resumeAudio();
                } else {
                    pauseAudio();
                }
            }
        });
        registerForContextMenu(timerButton);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.showContextMenu();
            }
        });

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = mainActivity.getMenuInflater();
        inflater.inflate(R.menu.timer, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int time = 0;
        switch (item.getItemId()) {
            case R.id.timer_15m:
                time = 15;
                break;
            case R.id.timer_30m:
                time = 30;
                break;
            case R.id.timer_1h:
                time = 60;
                break;
            case R.id.timer_2h:
                time = 120;
                break;
            case R.id.timer_4h:
                time = 240;
                break;
            case R.id.timer_6h:
                time = 360;
                break;
            case R.id.timer_8h:
                time = 480;
                break;
            case R.id.timer_10h:
                time = 600;
                break;
        }
        resetPlayTime(time);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        AudioService.PlayStatus status = getPlayStatus();
        setPlayButtonStatus(status);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioService.ACTION_PLAY_STATUS);
        mainActivity.registerReceiver(audioStateBR, intentFilter);
    }

    @Override
    public void onPause() {
        mainActivity.unregisterReceiver(audioStateBR);
        super.onPause();
    }

    protected void setPlayButtonStatus(AudioService.PlayStatus status) {
        if (status == AudioService.PlayStatus.Playing) {
            playButton.setImageDrawable(pauseIcon);
        } else {
            playButton.setImageDrawable(playIcon);
        }
    }
}
