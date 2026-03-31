package com.example.opcodeapp.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Collection;
import java.util.Map;

public class UIValidationUtil {

	/**
	 * Clears the error hint messages when the text is being updated
	 *
	 * @param layout The text input layout
	 * @param input  The text input text field
	 */
	public static void addErrorClearingWatcher(TextView input, TextInputLayout layout) {
		input.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				layout.setError(null);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * Validates that required fields contain a non-empty string else the {@link TextInputLayout}
	 * error message will be set.
	 *
	 * @param fields a {@link Map} containing the input field and its respective layout
	 * @return true if all fields have non-empty content, false otherwise
	 */
	public static boolean validateRequiredFields(Map<EditText, TextInputLayout> fields) {
		boolean valid = true;
		for (Map.Entry<EditText, TextInputLayout> entry : fields.entrySet()) {
			EditText input = entry.getKey();
			TextInputLayout layout = entry.getValue();
			if (getText(input).isEmpty()) {
				layout.setError("Required");
				valid = false;
			} else {
				layout.setError(null);
			}
		}
		return valid;
	}

	/**
	 * Clears all error hints from the text input layouts
	 */
	public static void clearErrors(Collection<TextInputLayout> errorFields) {
		errorFields.forEach(e -> e.setError(null));
	}

	/**
	 * @param field The field to capture input from
	 * @return The text contained in the field or an empty string
	 */
	public static String getText(EditText field) {
		Editable editable = field.getText();
		return (editable == null) ? "" : editable.toString().trim();
	}
}
