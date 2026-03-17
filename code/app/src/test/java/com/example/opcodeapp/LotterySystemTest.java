package com.example.opcodeapp;

import static org.junit.Assert.*;

import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;

import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LotterySystemTest {

    private Event mockEvent(int numApplicants) {
        User organizer = new User("Org", "o@o.com", "111");
        Event event = new Event("Lottery Test", "Loc", "Desc",
                LocalDate.now(), LocalDateTime.now(),
                LocalDate.now().plusDays(1), LocalDateTime.now().plusDays(1),
                organizer);

        User[] applicants = new User[numApplicants];
        for (int i = 0; i < numApplicants; i++) {
            applicants[i] = new User("User" + i, i + "@u.ca", "000");
            applicants[i].setId("ID_" + i);
        }
        event.setApplicants(applicants);
        return event;
    }

    @Test
    public void testDrawWithinCapacity() {
        LotterySystem system = new LotterySystem();
        Event event = mockEvent(10); // 10 people waiting

        // Responsibility: Ensure selected entrants do not exceed requested amount
        List<User> winners = system.drawEntrants(event, 5);

        assertEquals(5, winners.size());
    }

    @Test
    public void testDrawExceedingWaitlist() {
        LotterySystem system = new LotterySystem();
        Event event = mockEvent(3); // Only 3 people waiting

        // Should only return 3 even if we ask for 10
        List<User> winners = system.drawEntrants(event, 10);

        assertEquals(3, winners.size());
    }

    @Test
    public void testRandomness() {
        LotterySystem system = new LotterySystem();
        Event event = mockEvent(50);

        List<User> draw1 = system.drawEntrants(event, 5);
        List<User> draw2 = system.drawEntrants(event, 5);

        // check if two 5-person draws from 50 are not identical and in the same order.
        assertNotEquals(draw1, draw2);
    }

    @Test
    public void testEmptyWaitlist() {
        LotterySystem system = new LotterySystem();
        Event event = mockEvent(0);

        List<User> winners = system.drawEntrants(event, 5);
        assertTrue(winners.isEmpty());
    }
}
