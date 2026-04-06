package com.example.opcodeapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.opcodeapp.model.Event;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventFilterUtilTest {
    @Test
    public void matchesKeyword_checksNameDescriptionAndLocationCaseInsensitively() {
        Event event = createEvent(
                "event-1",
                "Spring Gala",
                "Tickets include dinner",
                "Edmonton Hall",
                LocalDateTime.of(2026, 4, 5, 9, 0),
                LocalDateTime.of(2026, 4, 10, 9, 0),
                10,
                9
        );

        assertTrue(EventFilterUtil.matchesKeyword(event, "gala"));
        assertTrue(EventFilterUtil.matchesKeyword(event, "DINNER"));
        assertTrue(EventFilterUtil.matchesKeyword(event, "edmonton"));
        assertFalse(EventFilterUtil.matchesKeyword(event, "calgary"));
    }

    @Test
    public void filterEvents_appliesKeywordAvailabilityAndCapacityTogether() {
        LocalDateTime now = LocalDateTime.now();

        Event matchingOpenEvent = createEvent(
                "open-event",
                "Chess Night",
                "Casual meetup",
                "Student Lounge",
                now.minusHours(1),
                now.plusHours(2),
                5,
                0
        );
        Event fullEvent = createEvent(
                "full-event",
                "Chess Finals",
                "Competitive bracket",
                "Main Gym",
                now.minusHours(1),
                now.plusHours(2),
                2,
                2
        );
        Event closedEvent = createEvent(
                "closed-event",
                "Chess History Talk",
                "Guest speaker",
                "Auditorium",
                now.minusDays(2),
                now.minusDays(1),
                5,
                3
        );

        List<Event> filteredEvents = EventFilterUtil.filterEvents(
                Arrays.asList(matchingOpenEvent, fullEvent, closedEvent),
                "chess",
                true,
                true,
                false
        );

        assertEquals(1, filteredEvents.size());
        assertEquals("open-event", filteredEvents.get(0).getId());
    }

    @Test
    public void isRegistrationOpen_includesStartAndEndBoundaries() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 6, 12, 0);
        ;
        LocalDateTime start = reference.minusHours(1);
        LocalDateTime end = reference.plusHours(1);
        Event event = createEvent("event-2",
                "Workshop",
                "Desc",
                "Lab",
                start,
                end,
                10,
                5
        );

        // Exact boundary
        assertTrue(EventFilterUtil.isRegistrationOpen(event, start));
        assertTrue(EventFilterUtil.isRegistrationOpen(event, end));

        // Within boundary
        assertTrue(EventFilterUtil.isRegistrationOpen(event, reference));

        // Outside boundary
        assertFalse(EventFilterUtil.isRegistrationOpen(event, start.minusSeconds(1)));
        assertFalse(EventFilterUtil.isRegistrationOpen(event, end.plusSeconds(1)));
    }

    @Test
    public void hasCapacity_allowsUnlimitedAndRejectsFullEvents() {
        Event unlimitedEvent = createEvent(
                "unlimited",
                "Open House",
                "Desc",
                "Venue",
                LocalDateTime.of(2026, 4, 1, 9, 0),
                LocalDateTime.of(2026, 4, 10, 9, 0),
                -1,
                10
        );
        Event limitedEvent = createEvent(
                "limited",
                "Meetup",
                "Desc",
                "Venue",
                LocalDateTime.of(2026, 4, 1, 9, 0),
                LocalDateTime.of(2026, 4, 10, 9, 0),
                3,
                3
        );

        assertTrue(EventFilterUtil.hasCapacity(unlimitedEvent));
        assertFalse(EventFilterUtil.hasCapacity(limitedEvent));
    }

    private Event createEvent(
            String id,
            String name,
            String description,
            String location,
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd,
            int waitlistLimit,
            int waitlistCount
    ) {
        return new Event(
                id,
                name,
                location,
                description,
                registrationEnd.plusDays(1),
                registrationEnd.plusDays(1).plusHours(2),
                registrationStart,
                registrationEnd,
                "organizer-1",
                0f,
                waitlistLimit,
                waitlistCount,
                true,
                ""
        );
    }
}
