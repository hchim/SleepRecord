package im.hch.sleeprecord.models;

import org.json.JSONObject;

import lombok.Data;

@Data
public class ApplicationConfig {
    private String splashImageUrl;

    public ApplicationConfig() {}

    public ApplicationConfig(JSONObject jsonObject) {

    }
}
