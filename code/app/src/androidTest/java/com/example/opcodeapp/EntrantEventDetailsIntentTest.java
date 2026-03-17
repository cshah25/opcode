package com.example.opcodeapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantEventDetailsIntentTest {

    @Test
    public void testStaticUIElements() {
        FragmentScenario.launchInContainer(EntrantEventDetailsFragment.class);

        onView(withId(R.id.btn_lottery_info)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_join_waitlist)).check(matches(isDisplayed()));
    }

    @Test
    public void testLotteryDialogIntent() {
        FragmentScenario.launchInContainer(EntrantEventDetailsFragment.class);

        // Click the lottery info button
        onView(withId(R.id.btn_lottery_info)).perform(click());

        // Verify the dialog text appears
        onView(withText("Lottery Selection Guidelines")).check(matches(isDisplayed()));
    }
}