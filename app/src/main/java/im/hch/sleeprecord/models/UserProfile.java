package im.hch.sleeprecord.models;

import java.util.Date;

import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String email;
    private String username;
    private String accessToken;
}
