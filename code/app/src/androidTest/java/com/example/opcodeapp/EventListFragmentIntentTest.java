package com.example.opcodeapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anything;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.opcodeapp.view.EventsListFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventListFragmentIntentTest {

    @Test
    public void testClickEventFromList() {
        FragmentScenario.launchInContainer(EventsListFragment.class);

        // wait for Firebase
        try { Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // Click an item in the AdapterView (ListView)
        onData(anything())
                .inAdapterView(withId(R.id.event_list_view))
                .atPosition(0)
                .perform(click());
    }
}