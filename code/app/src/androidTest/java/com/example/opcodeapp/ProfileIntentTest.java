package com.example.opcodeapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileIntentTest {

    @Before
    public void setup() {
        FragmentScenario.launchInContainer(ProfileFragment.class);
    }

    @Test
    public void testUpdateFlow() {
        onView(withId(R.id.profile_name_input)).perform(typeText("Michali"));
        onView(withId(R.id.profile_update_button)).perform(click());
        onView(withId(R.id.profile_name_input)).check(matches(isDisplayed()));
    }
}