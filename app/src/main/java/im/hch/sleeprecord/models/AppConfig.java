package im.hch.sleeprecord.models;

import org.json.JSONObject;

import lombok.Data;

@Data
public class AppConfig {
    private String splashImageUrl;

    public AppConfig() {}

    public AppConfig(JSONObject jsonObject) {

    }
}
