package com.example.opcodeapp.util;

import androidx.annotation.NonNull;

import com.example.opcodeapp.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Static helper methods for searching and filtering events in the event list.
 */
public final class EventFilterUtil {
    private EventFilterUtil() {
    }

    /**
     * Filters the provided events using the current keyword and checkbox state.
     *
     * @param events        the events to filter
     * @param keyword       the user-entered search keyword
     * @param availableOnly whether to keep only events with open registration
     * @param capacityOnly  whether to keep only events with remaining waitlist capacity
     * @param isAdmin       whether the viewer is an admin
     * @return the filtered events in their original order
     */
    @NonNull
    public static List<Event> filterEvents(
            @NonNull List<Event> events,
            String keyword,
            boolean availableOnly,
            boolean capacityOnly,
            boolean isAdmin
    ) {
        List<Event> filteredEvents = new ArrayList<>();
        String normalizedKeyword = normalizeKeyword(keyword);

        for (Event event : events) {
            if (!matchesKeyword(event, normalizedKeyword))
                continue;

            if (availableOnly && !isRegistrationOpen(event))
                continue;

            if (capacityOnly && !hasCapacity(event))
                continue;

            if (!event.isPublic() && !isAdmin)
                continue;

            filteredEvents.add(event);
        }

        return filteredEvents;
    }

    /**
     * Checks whether an event matches the provided keyword in its name, description, or location.
     *
     * @param event   the event being checked
     * @param keyword the keyword to search for
     * @return {@code true} when the event matches the keyword, otherwise {@code false}
     */
    public static boolean matchesKeyword(@NonNull Event event, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return true;
        }

        return containsKeyword(event.getName(), normalizedKeyword)
                || containsKeyword(event.getDescription(), normalizedKeyword)
                || containsKeyword(event.getLocation(), normalizedKeyword);
    }

    /**
     * Performs a case-insensitive substring check.
     *
     * @param value   the source text
     * @param keyword the keyword to search for
     * @return {@code true} when {@code value} contains the keyword, otherwise {@code false}
     */
    public static boolean containsKeyword(String value, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return true;
        }

        return value != null && value.toLowerCase(Locale.getDefault()).contains(normalizedKeyword);
    }

    /**
     * Checks whether registration is currently open for an event.
     *
     * @param event the event being checked
     * @return {@code true} when the current time falls within the registration window
     */
    public static boolean isRegistrationOpen(@NonNull Event event, @NonNull LocalDateTime time) {
        LocalDateTime registrationStart = event.getRegistrationStart();
        LocalDateTime registrationEnd = event.getRegistrationEnd();

        return registrationStart != null
                && registrationEnd != null
                && !time.isBefore(registrationStart)
                && !time.isAfter(registrationEnd);
    }


    /**
     * Checks whether registration is currently open for an event.
     *
     * @param event the event being checked
     * @return {@code true} when the current time falls within the registration window
     */
    public static boolean isRegistrationOpen(@NonNull Event event) {
        return isRegistrationOpen(event, LocalDateTime.now());
    }

    /**
     * Checks whether an event can still accept more applicants.
     *
     * @param event the event being checked
     * @return {@code true} when the event still has waitlist capacity
     */
    public static boolean hasCapacity(@NonNull Event event) {
        if (event.getWaitlistLimit() < 0)
            return true;

        return event.getWaitlistCount() < event.getWaitlistLimit();
    }

    /**
     * Normalizes a search keyword for case-insensitive comparisons.
     *
     * @param keyword the keyword to normalize
     * @return the trimmed, lowercase keyword, or an empty string when none is provided
     */
    @NonNull
    public static String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());
    }
}
