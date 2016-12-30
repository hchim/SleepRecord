package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.exceptions.AccountNotExistException;
import im.hch.sleeprecord.serviceclients.exceptions.EmailUsedException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.WrongPasswordException;

public class IdentityServiceClient extends BaseServiceClient {

    public static final String TAG = "IdentityServiceClient";

    public static final String REGISTER_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "register";
    public static final String LOGIN_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "login";

    public static final String ERROR_CODE_EMAIL_USED = "EMAIL_USED";
    public static final String ERROR_CODE_WRONG_PASSWORD = "WRONG_PASSWORD";
    public static final String ERROR_CODE_ACCOUNT_NOT_EXIST = "ACCOUNT_NOT_EXIST";

    public IdentityServiceClient() {
        super();
    }

    /**
     * Authenticate the user.
     * Returns the access token if authentication is successful.
     * @param email
     * @param password
     * @return
     */
    public UserProfile login(String email, String password)
            throws AccountNotExistException, WrongPasswordException, InternalServerException {
        JSONObject object = new JSONObject();

        try {
            object.put("email", email);
            object.put("password", password);

            JSONObject result = post(LOGIN_URL, object);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                } else if (errorCode.equals(ERROR_CODE_WRONG_PASSWORD)) {
                    throw new WrongPasswordException();
                }
                throw new InternalServerException();
            }

            UserProfile userProfile = new UserProfile();
            userProfile.setEmail(email);
            userProfile.setId(result.getString("userId"));
            userProfile.setUsername(result.getString("nickName"));
            userProfile.setHeaderIconUrl(result.getString("headerImageUrl"));
            return userProfile;
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            return null;
        }
    }

    /**
     * Register an account.
     * @param email
     * @param password
     * @param nickName
     * @return
     * @throws EmailUsedException
     * @throws InternalServerException
     */
    public UserProfile register(String email, String password, String nickName)
            throws EmailUsedException, InternalServerException {
        JSONObject object = new JSONObject();

        try {
            object.put("email", email);
            object.put("password", password);
            object.put("nickName", nickName);

            JSONObject result = post(REGISTER_URL, object);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_EMAIL_USED)) {
                    throw new EmailUsedException();
                }
                throw new InternalServerException();
            }

            UserProfile userProfile = new UserProfile();
            userProfile.setUsername(nickName);
            userProfile.setEmail(email);
            userProfile.setId(result.getString("userId"));

            return userProfile;
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            return null;
        }
    }

    public void updateUserProfile(UserProfile userProfile) {
        //TODO save user nick name
    }
}
