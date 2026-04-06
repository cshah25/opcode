package com.example.opcodeapp;

import android.os.Bundle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.view.EventDetailsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsFragmentIntentTest {

    @Test
    public void testJoinWaitlistToggle() {
        //  event to pass into the fragment
        Event mockEvent = Event.builder()
                .id("test_id")
                .name("test")
                .location("UofA")
                .organizerId("org")
                .waitlistCount(5)
                .waitlistLimit(10)
                .build();
        mockEvent.setName("Test Intent Event");
        mockEvent.setOrganizerId("mock_id");

        Bundle args = new Bundle();
        args.putParcelable("event", mockEvent);

        // Launch with arguments
        FragmentScenario.launchInContainer(EventDetailsFragment.class, args);

        onView(withId(R.id.btn_join_leave_waitlist)).check(matches(isDisplayed()));

        // click to Join/Leave
        onView(withId(R.id.btn_join_leave_waitlist)).perform(click());
    }
}