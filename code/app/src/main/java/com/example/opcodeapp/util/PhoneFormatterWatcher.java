package com.example.opcodeapp.util;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

/**
 * A {@link TextWatcher} implementation that formats phone numbers
 * in real time as the user types.
 *
 * <p>This class uses Google's libphonenumber
 * {@link com.google.i18n.phonenumbers.AsYouTypeFormatter}
 * to automatically apply region-specific phone number formatting
 * such as parentheses, spaces, and hyphens.
 *
 * <p>Example for Canada / US:
 * <pre>
 * 7805551234 -> (780) 555-1234
 * </pre>
 *
 * <p>The formatter clears and rebuilds the formatted value after
 * each text change to ensure consistent formatting.
 */
public class PhoneFormatterWatcher implements TextWatcher {
    private final AsYouTypeFormatter formatter;
    private boolean selfChange = false;

    /**
     * Creates a phone formatter watcher for the specified region.
     *
     * @param regionCode the ISO 3166-1 two-letter region code
     *                   (for example "CA" or "US")
     */
    public PhoneFormatterWatcher(String regionCode) {
        this.formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionCode);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    /**
     * Re-formats the phone number after each text change.
     *
     * <p>All non-digit characters are removed, then each digit is
     * re-applied through the {@code AsYouTypeFormatter} to produce
     * the correctly formatted phone number.
     *
     * <p>A self-change guard is used to prevent recursive calls when
     * the text is programmatically updated.
     *
     * @param s the editable text being modified
     */
    @Override
    public void afterTextChanged(Editable s) {
        // Prevent recursive formatting calls
        if (selfChange) return;

        selfChange = true;

        // Reset formatter before rebuilding formatted output
        formatter.clear();

        // Strip all formatting and keep only digits
        String digits = s.toString().replaceAll("\\D", "");
        String formatted = "";

        // Rebuild formatted phone number digit by digit
        for (char c : digits.toCharArray()) {
            formatted = formatter.inputDigit(c);
        }

        s.replace(0, s.length(), formatted);

        selfChange = false;
    }
}
