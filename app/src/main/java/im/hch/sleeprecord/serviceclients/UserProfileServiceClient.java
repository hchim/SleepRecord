package im.hch.sleeprecord.serviceclients;

import im.hch.sleeprecord.models.UserProfile;

public class UserProfileServiceClient extends BaseServiceClient {

    /**
     * Authenticate the user.
     * Returns the access token if authentication is successful.
     * @param email
     * @param password
     * @return
     */
    public UserProfile login(String email, String password) {
        //TODO implement
        if (email.equals("foo@example.com") && password.equals("P@ssw0rd")) {
            UserProfile userProfile = new UserProfile();
            userProfile.setUsername("Foo");
            userProfile.setId("userid");
            userProfile.setHeaderIconUrl("headericonurl");
            userProfile.setEmail(email);
            userProfile.setAccessToken("defaultaccesstoken");

            return userProfile;
        }

        return null;
    }

    public UserProfile register(String email, String password, String nickName) {
        //TODO implement
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(nickName);
        userProfile.setId("userid");
        userProfile.setHeaderIconUrl("headericonurl");
        userProfile.setEmail(email);
        userProfile.setAccessToken("defaultaccesstoken");

        return userProfile;
    }

    public void updateUserProfile(UserProfile userProfile) {
        //TODO save user nick name
    }
}
