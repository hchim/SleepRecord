package im.hch.sleeprecord.serviceclients;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.exceptions.AccountNotExistException;
import im.hch.sleeprecord.serviceclients.exceptions.AuthFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.EmailUsedException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.WrongPasswordException;
import im.hch.sleeprecord.serviceclients.exceptions.WrongSecurityCodeException;
import im.hch.sleeprecord.serviceclients.exceptions.WrongVerifyCodeException;
import im.hch.sleeprecord.utils.DateUtils;

public class IdentityServiceClient extends BaseServiceClient {

    public static final String TAG = "IdentityServiceClient";

    public static final String REGISTER_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "register";
    public static final String LOGIN_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "login";
    public static final String USERS_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "users/";
    public static final String UPDATE_HEADER_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "users/update-header";
    public static final String UPDATE_USER_NAME = EndPoints.IDENTITY_SERVICE_ENDPOINT + "users/update-name";
    public static final String UPDATE_USER_PASSWORD = EndPoints.IDENTITY_SERVICE_ENDPOINT + "users/update-pswd";
    public static final String VERIFY_EMAIL_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "users/verify-email";
    public static final String SEND_PASSWORD_RESET_EMAIL_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "reset-email";
    public static final String PASSWORD_RESET_URL = EndPoints.IDENTITY_SERVICE_ENDPOINT + "reset-pswd";

    public static final String ERROR_CODE_EMAIL_USED = "EMAIL_USED";
    public static final String ERROR_CODE_WRONG_PASSWORD = "WRONG_PASSWORD";
    public static final String ERROR_CODE_ACCOUNT_NOT_EXIST = "ACCOUNT_NOT_EXIST";
    public static final String ERROR_CODE_WRONG_VERIFY_CODE = "WRONG_VERIFY_CODE";
    public static final String ERROR_CODE_WRONG_SECURITY_CODE = "WRONG_SECURITY_CODE";

    private Map<String, String> aaaHeaders;

    public IdentityServiceClient() {
        super();
        aaaHeaders = new HashMap<>();
    }

    public void setAccessToken(String accessToken) {
        aaaHeaders.put(BaseServiceClient.ACCESS_TOKEN, accessToken);

    }

    /**
     * Get user.
     * @return
     * @throws AccountNotExistException
     * @throws InternalServerException
     * @throws ConnectionFailureException
     */
    public UserProfile getUser()
            throws AccountNotExistException, InternalServerException,
            ConnectionFailureException, AuthFailureException {
        String url = USERS_URL;

        try {
            JSONObject result = get(url, aaaHeaders);
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                } else if (errorCode.equals(ERROR_AUTH_FAILURE) || errorCode.equals(ERROR_UNKNOWN_USER)) {
                    throw new AuthFailureException();
                }
                throw new InternalServerException();
            }

            UserProfile userProfile = new UserProfile();
            userProfile.setId(result.getString("userId"));
            userProfile.setUsername(result.getString("nickName"));
            userProfile.setHeaderIconUrl(result.getString("headerImageUrl"));
            userProfile.setEmailVerified(result.getBoolean("emailVerified"));
            userProfile.setCreateTime(DateUtils.strToDate(result.getString("createTime"), DATE_FORMAT));
            return userProfile;
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Authenticate the user.
     * Returns the access token if authentication is successful.
     * @param email
     * @param password
     * @return
     */
    public UserProfile login(String email, String password)
            throws AccountNotExistException, WrongPasswordException, InternalServerException, ConnectionFailureException {
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
            userProfile.setAccessToken(result.getString("accessToken"));
            return userProfile;
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
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
            throws EmailUsedException, InternalServerException, ConnectionFailureException {
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
            userProfile.setAccessToken(result.getString("accessToken"));

            return userProfile;
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Update the user name.
     * @param userName
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws AccountNotExistException
     */
    public void updateUserName(String userName)
            throws ConnectionFailureException, InternalServerException,
            AccountNotExistException, AuthFailureException {
        String url = UPDATE_USER_NAME;
        JSONObject object = new JSONObject();
        try {
            object.put("nickName", userName);
            JSONObject result = put(url, object, aaaHeaders);

            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                } else if (errorCode.equals(ERROR_AUTH_FAILURE) || errorCode.equals(ERROR_UNKNOWN_USER)) {
                    throw new AuthFailureException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Upload header image.
     * @param imagePath
     * @return url of the new header image.
     * @throws ConnectionFailureException
     * @throws InternalServerException
     */
    public String uploadHeaderIcon(String imagePath)
            throws ConnectionFailureException, InternalServerException, AuthFailureException {
        String url = UPDATE_HEADER_URL;
        JSONObject result = uploadImage(url, imagePath, aaaHeaders);
        try {
            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_AUTH_FAILURE) || errorCode.equals(ERROR_UNKNOWN_USER)) {
                    throw new AuthFailureException();
                }

                throw new InternalServerException();
            }

            return result.getString("headerImageUrl");
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Update user password.
     * @param oldPassword
     * @param newPassword
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws AccountNotExistException
     */
    public void updateUserPassword(String oldPassword, String newPassword)
            throws ConnectionFailureException, InternalServerException,
            AccountNotExistException, WrongPasswordException, AuthFailureException {
        String url = UPDATE_USER_PASSWORD;
        JSONObject object = new JSONObject();
        try {
            object.put("oldPassword", oldPassword);
            object.put("newPassword", newPassword);
            JSONObject result = put(url, object, aaaHeaders);

            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                } else if (errorCode.equals(ERROR_CODE_WRONG_PASSWORD)) {
                    throw new WrongPasswordException();
                } else if (errorCode.equals(ERROR_AUTH_FAILURE) || errorCode.equals(ERROR_UNKNOWN_USER)) {
                    throw new AuthFailureException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Verify email.
     * @param verifyCode
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws AccountNotExistException
     * @throws WrongVerifyCodeException
     */
    public void verifyEmail(String verifyCode)
            throws ConnectionFailureException, InternalServerException,
            AccountNotExistException, WrongVerifyCodeException, AuthFailureException {
        String url = VERIFY_EMAIL_URL;
        JSONObject object = new JSONObject();
        try {
            object.put("verifyCode", verifyCode);
            JSONObject result = post(url, object);

            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                } else if (errorCode.equals(ERROR_CODE_WRONG_VERIFY_CODE)) {
                    throw new WrongVerifyCodeException();
                } else if (errorCode.equals(ERROR_AUTH_FAILURE) || errorCode.equals(ERROR_UNKNOWN_USER)) {
                    throw new AuthFailureException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Send the reset password email.
     * @param email
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws AccountNotExistException
     */
    public void sendPasswordResetEmail(String email)
            throws ConnectionFailureException, InternalServerException, AccountNotExistException {
        JSONObject object = new JSONObject();
        try {
            object.put("email", email);
            JSONObject result = post(SEND_PASSWORD_RESET_EMAIL_URL, object);

            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }

    /**
     * Reset password.
     * @param email
     * @param securityCode
     * @param password
     * @throws ConnectionFailureException
     * @throws InternalServerException
     * @throws AccountNotExistException
     * @throws WrongSecurityCodeException
     */
    public void resetPassword(String email, String securityCode, String password)
            throws ConnectionFailureException, InternalServerException, AccountNotExistException, WrongSecurityCodeException {
        JSONObject object = new JSONObject();
        try {
            object.put("email", email);
            object.put("securityCode", securityCode);
            object.put("newPassword", password);

            JSONObject result = post(PASSWORD_RESET_URL, object);

            if (result.has(ERROR_CODE_KEY)) {
                if (result.has(ERROR_MESSAGE_KEY)) {
                    Log.e(TAG, result.getString(ERROR_MESSAGE_KEY));
                }

                String errorCode = result.getString(ERROR_CODE_KEY);
                if (errorCode.equals(ERROR_CODE_ACCOUNT_NOT_EXIST)) {
                    throw new AccountNotExistException();
                } else if (errorCode.equals(ERROR_CODE_WRONG_SECURITY_CODE)) {
                    throw new WrongSecurityCodeException();
                }
                throw new InternalServerException();
            }
        } catch (JSONException ex) {
            Log.e(TAG, "JSON format error");
            throw new InternalServerException();
        }
    }
}
