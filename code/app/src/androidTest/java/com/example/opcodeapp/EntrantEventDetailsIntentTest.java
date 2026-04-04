package com.example.opcodeapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.opcodeapp.view.EventCreatorFragment;
import com.example.opcodeapp.view.EventDetailsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantEventDetailsIntentTest {


    /**
     * Checks if the lottery info button and join/leave waitlist button are displayed.
     */
    @Test
    public void testStaticUIElements() {
        FragmentScenario.launchInContainer(EventDetailsFragment.class);

        onView(withId(R.id.btn_lottery_info)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_join_leave_waitlist)).check(matches(isDisplayed()));
    }


    /**
     * Checks if the lottery info dialog is displayed when the lottery info button is clicked.
     */
    @Test
    public void testLotteryDialogIntent() {
        FragmentScenario.launchInContainer(EventDetailsFragment.class);

        // Click the lottery info button
        onView(withId(R.id.btn_lottery_info)).perform(click());

        // Verify the dialog text appears
        onView(withText("Lottery Selection Guidelines")).check(matches(isDisplayed()));
    }

}