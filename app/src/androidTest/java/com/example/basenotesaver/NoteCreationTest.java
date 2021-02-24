package com.example.basenotesaver;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static androidx.test.espresso.Espresso.pressBack;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp(){
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityScenarioRule<NoteListActivity> mNoteListActivityTestRule =
            new ActivityScenarioRule<>(NoteListActivity.class);

    @Test
    public void createNewNote(){

        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "This is a Test Title";
        final String noteText  = "This is a Test Body for the course notes";
//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());
        onView(withId(R.id.fab)).perform(click());

        //click on the spinner to select the data
        onView((withId(R.id.spinner_courses))).perform(click());
        //now click on the data itself
        DataInteraction selectCourseFromSpinner = onData(allOf(instanceOf(CourseInfo.class),
                equalTo(course)));
        selectCourseFromSpinner.perform(click());

//      check that the course selected is actually displayed by the spinner
        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(containsString(course.getTitle()))));

        ViewInteraction typeNoteTitle = onView(withId(R.id.text_note_title));
        typeNoteTitle.perform(typeText(noteTitle)).check(matches(withText(containsString(noteTitle))));
//        onView(withId(R.id.text_note_title)).perform(typeText("This is a Test Title"));

        onView(withId(R.id.text_note_text)).perform(typeText(noteText),
                ViewActions.closeSoftKeyboard());

        onView(withId(R.id.text_note_text)).check(matches(withText(containsString(noteText))));
        pressBack();

        //Ensure our Note logic works properly by checking that the note gets saved after user presses the back button

        int noteIndex =  sDataManager.getNotes().size()-1;
        NoteInfo note = sDataManager.getNotes().get(noteIndex);
        assertEquals(course, note.getCourse());
        assertEquals(noteText, note.getText());
    }
}