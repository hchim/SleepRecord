package im.hch.sleeprecord.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.Date;

import im.hch.sleeprecord.Constants;
import im.hch.sleeprecord.utils.DateUtils;
import lombok.Data;

/**
 * Created by huiche on 6/7/17.
 */
@Data
public class SleepRecord implements Parcelable {
    private String recordId;
    private Date recordSleepTime;  //the origional fall asleep time of the record
    private Date recordWakeupTime; //the origional wakeup time of the record

    public SleepRecord(String id, Date recordSleepTime, Date recordWakeupTime) {
        this.recordId = id;
        this.recordSleepTime = recordSleepTime;
        this.recordWakeupTime = recordWakeupTime;
    }

    public static final Parcelable.Creator<SleepRecord> CREATOR = new Parcelable.Creator<SleepRecord>() {
        public SleepRecord createFromParcel(Parcel in) {
            return new SleepRecord(in.readString(), new Date(in.readLong()), new Date(in.readLong()));
        }

        public SleepRecord[] newArray(int size) {
            return new SleepRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(recordId);
        dest.writeLong(recordSleepTime.getTime());
        dest.writeLong(recordWakeupTime.getTime());
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("recordId", recordId);
            object.put("sleepTime", DateUtils.dateToStr(recordSleepTime, Constants.DATE_FORMAT));
            object.put("wakeupTime", DateUtils.dateToStr(recordWakeupTime, Constants.DATE_FORMAT));
        } catch (Exception e) {
        }

        return object;
    }

    public static SleepRecord create(JSONObject obj) {
        if (obj == null) {
            return null;
        }

        try {
            SleepRecord sleepRecord = new SleepRecord(
                    obj.getString("_id"),
                    DateUtils.strToDate(obj.getString("recSleepTime"), Constants.DATE_FORMAT),
                    DateUtils.strToDate(obj.getString("recWakeupTime"), Constants.DATE_FORMAT)
            );
            return sleepRecord;
        } catch (Exception e) {
        }

        return null;
    }
}
