package com.example.opcodeapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.opcodeapp.view.EventDetailsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LotterySystemIntentTest {

    @Test
    public void testLotteryCriteriaDialog() {
        FragmentScenario.launchInContainer(EventDetailsFragment.class);

        // lottery information dialog
        onView(withId(R.id.btn_lottery_info)).perform(click());

        // verify the the text matches the showLotteryCriteriaDialog() method
        onView(withText("Lottery Selection Guidelines")).check(matches(isDisplayed()));
        onView(withText("Got it")).perform(click());
    }
}