package com.example.basenotesaver;

import static org.junit.Assert.*;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class NextThroughNotesTest {
    @Rule
    public ActivityScenarioRule<BaseActivity> mActivityScenarioRule = new ActivityScenarioRule<>(BaseActivity.class);


    @Test
    public void NextThroughNotes(){
        ViewInteraction viewInteraction = onView(withId(R.id.drawer_layout));
        viewInteraction.perform(DrawerActions.open());

        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));
        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        for(int index = 0; index < notes.size() - 1; index++) {
            NoteInfo noteInfo = notes.get(index);

            onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(noteInfo.getCourse().getTitle())));

            onView(withId(R.id.text_note_title)).check(matches(withText(noteInfo.getTitle())));

            onView(withId(R.id.text_note_text)).check(matches(withText(noteInfo.getText())));

            if(index < notes.size() - 1)
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());

            }
            onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
        Espresso.pressBack();

    }
}