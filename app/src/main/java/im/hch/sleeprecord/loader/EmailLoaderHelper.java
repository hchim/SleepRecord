package im.hch.sleeprecord.loader;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class EmailLoaderHelper {

    private Context mContext;

    public EmailLoaderHelper(Context context) {
        this.mContext = context;
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;

        // Retrieve data rows for the device user's 'profile' contact.
        Uri URI = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI
                , ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        // selection based on mime type
        String SELECTION_MIME_TYPE = ContactsContract.Contacts.Data.MIMETYPE + " = ?";
        String SORT_IS_PRIMARY_DESC = ContactsContract.Contacts.Data.IS_PRIMARY + " DESC";
        String[] ARGS_EMAIL_TYPE = {ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
    }

    public Loader<Cursor> createLoader() {
        return new CursorLoader(mContext,
                ProfileQuery.URI,
                ProfileQuery.PROJECTION,
                ProfileQuery.SELECTION_MIME_TYPE,
                ProfileQuery.ARGS_EMAIL_TYPE,
                ProfileQuery.SORT_IS_PRIMARY_DESC);
    }

    /**
     * Iterate the cursor and get all the emails.
     * @param cursor
     * @return
     */
    public List<String> getEmails(Cursor cursor) {
        List<String> emails = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        return emails;
    }
}
