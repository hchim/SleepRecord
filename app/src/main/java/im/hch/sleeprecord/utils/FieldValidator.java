package im.hch.sleeprecord.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidator {
    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

    public static boolean isEmailValid(String email) {
        if (email == null || TextUtils.isEmpty(email.trim())) {
            return false;
        }

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * 1. Length >= 6 and <= 20
     * 2. Contain at least a number
     * 3. Contain at least a lowercase character
     * 4. Contain at least a uppercase character
     * 5. Contain at least a character in [@, #, $, %]
     * @param password
     * @return
     */
    public static boolean isPasswordValidStrict(String password) {
        if (password == null || TextUtils.isEmpty(password.trim())) {
            return false;
        }

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        if (password == null || TextUtils.isEmpty(password.trim())) {
            return false;
        }
        return password.length() >= 6;
    }
}
