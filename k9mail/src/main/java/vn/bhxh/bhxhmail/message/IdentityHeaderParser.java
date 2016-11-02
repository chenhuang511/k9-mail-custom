package vn.bhxh.bhxhmail.message;


import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mail.filter.Base64;


public class IdentityHeaderParser {
    /**
     * Parse an identity string.  Handles both legacy and new (!) style identities.
     *
     * @param identityString
     *         The encoded identity string that was saved in a drafts header.
     *
     * @return A map containing the value for each {@link IdentityField} in the identity string.
     */
    public static Map<IdentityField, String> parse(final String identityString) {
        Map<IdentityField, String> identity = new HashMap<IdentityField, String>();

        if (K9.DEBUG) {
            Log.d(K9.LOG_TAG, "Decoding identity: " + identityString);
        }

        if (identityString == null || identityString.length() < 1) {
            return identity;
        }

        // Check to see if this is a "next gen" identity.
        if (identityString.charAt(0) == IdentityField.IDENTITY_VERSION_1.charAt(0) && identityString.length() > 2) {
            Uri.Builder builder = new Uri.Builder();
            builder.encodedQuery(identityString.substring(1));  // Need to cut off the ! at the beginning.
            Uri uri = builder.build();
            for (IdentityField key : IdentityField.values()) {
                String value = uri.getQueryParameter(key.value());
                if (value != null) {
                    identity.put(key, value);
                }
            }

            if (K9.DEBUG) {
                Log.d(K9.LOG_TAG, "Decoded identity: " + identity.toString());
            }

            // Sanity check our Integers so that recipients of this result don't have to.
            for (IdentityField key : IdentityField.getIntegerFields()) {
                if (identity.get(key) != null) {
                    try {
                        Integer.parseInt(identity.get(key));
                    } catch (NumberFormatException e) {
                        Log.e(K9.LOG_TAG, "Invalid " + key.name() + " field in identity: " + identity.get(key));
                    }
                }
            }
        } else {
            // Legacy identity

            if (K9.DEBUG) {
                Log.d(K9.LOG_TAG, "Got a saved legacy identity: " + identityString);
            }
            StringTokenizer tokenizer = new StringTokenizer(identityString, ":", false);

            // First item is the body length. We use this to separate the composed reply from the quoted text.
            if (tokenizer.hasMoreTokens()) {
                String bodyLengthS = Base64.decode(tokenizer.nextToken());
                try {
                    identity.put(IdentityField.LENGTH, Integer.valueOf(bodyLengthS).toString());
                } catch (Exception e) {
                    Log.e(K9.LOG_TAG, "Unable to parse bodyLength '" + bodyLengthS + "'");
                }
            }
            if (tokenizer.hasMoreTokens()) {
                identity.put(IdentityField.SIGNATURE, Base64.decode(tokenizer.nextToken()));
            }
            if (tokenizer.hasMoreTokens()) {
                identity.put(IdentityField.NAME, Base64.decode(tokenizer.nextToken()));
            }
            if (tokenizer.hasMoreTokens()) {
                identity.put(IdentityField.EMAIL, Base64.decode(tokenizer.nextToken()));
            }
            if (tokenizer.hasMoreTokens()) {
                identity.put(IdentityField.QUOTED_TEXT_MODE, Base64.decode(tokenizer.nextToken()));
            }
        }

        return identity;
    }
}
