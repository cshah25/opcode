package com.example.opcodeapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventTest {


    private DBManager dbManager = new DBManager(FirebaseFirestore.getInstance());


    private Event mockEvent() {

        User.Builder b = User.builder("")
                .name("mock_organizer")
                .email("mock_organizer@ualberta.ca")
                .phoneNum("676767676");


        User organizer = b.build();

        Event.Builder b2 = Event.builder("")
                .name("Oscanrs")
                .location("California")
                .description("Film festival and presentation of awards")
                .start(LocalDateTime.of(2026, 3, 15, 0, 0))
                .end(LocalDateTime.of(2026, 3, 15, 23, 59))
                .registrationStart(LocalDateTime.of(2026, 3, 14, 16, 0))
                .registrationEnd(LocalDateTime.of(2026, 3, 14, 16, 0))
                .organizer(organizer);


        Event event = b2.build();

        return event;

    }


    //tests for addApplicant and getApplicants
    @Test
    void testAddApplicant() {

        User.Builder b = User.builder("")
                .name("mock_applicant_1")
                .email("mock_applicant_1@ualberta.ca")
                .phoneNum("686868686868");


        User applicant_user = b.build();
        Event event = mockEvent();
        event.addApplicant(applicant);
        assertTrue(event.getApplicants().contains(applicant));

    }

    //tests for setInvited and getInvited
    @Test
    void testSetInvited() {
        User applicant = new User("mock_applicant_2", "mock_applicant_2@ualberta.ca", "69696969696969");
        Event event = mockEvent();
        event.addApplicant(applicant);
        event.setInvited(event.getApplicants());
        assertTrue(event.getInvited().contains(applicant));


    }

    //tests for setAttendee and getAttendees
    @Test
    void testSetAttendee() {
        User applicant = new User("mock_applicant_3", "mock_applicant_3@ualberta.ca", "707070707070");
        Event event = mockEvent();
        event.addApplicant(applicant);
        event.setAttendee(applicant);
        assertTrue(event.getAttendees().contains(applicant));

    }

    //tests for setDeclined and getDeclined
    @Test
    void testSetDeclined() {
        User applicant = new User("mock_applicant_4", "mock_applicant_4@ualberta.ca", "717171717171");
        Event event = mockEvent();
        event.addApplicant(applicant);
        event.setDeclined(applicant);
        assertTrue(event.getDeclined().contains(applicant));

    }



}
