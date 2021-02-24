package com.example.basenotesaver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.basenotesaver.NoteKeeperDataBaseContract.CourseInfoEntry;
import com.example.basenotesaver.NoteKeeperDataBaseContract.NoteInfoEntry;

import java.net.URI;

import static com.example.basenotesaver.NoteKeeperProviderContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int INT = 0;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID ="com.example.basenotesaver.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private EditText mTextNoteText;
    private EditText mTextNoteTitle;
    private Spinner mSpinnerCourses;
    private int mNoteId;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        //Boiler-plate viewModelProvider code
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        //get viewModel
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.mIsNewlyCreated && savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated = false;


        mSpinnerCourses = findViewById(R.id.spinner_courses);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
//        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE}, new int[] {android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

//        loadCourseData();

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);
        
        readDisplayStateValue();
        saveOriginalNoteValue();
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote)
            //loadNoteData();
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null,  NoteActivity.this);
        Log.d(TAG, "onCreate");

    }

    private void loadCourseData() {
        SQLiteDatabase db  = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
          CourseInfoEntry.COLUMN_COURSE_TITLE,
          CourseInfoEntry.COLUMN_COURSE_ID,
          CourseInfoEntry._ID
        };

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

//        String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
//        + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";
        String selection = NoteInfoEntry._ID + " = ?";

//                String[] selectionArgs = {courseId, titleStart + "%"};
        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection,
                selectionArgs, null, null, null);

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToNext();
        displayNote();
    }

    private void saveOriginalNoteValue() {
        if(mIsNewNote){
            return;
        }
//        mViewModel.mOriginalNoteCourseId = String.valueOf(mNote.getId());
//        mViewModel.mOriginalNoteTitle = mNote.getTitle();
//        mViewModel.mOriginalNoteText = mNote.getText();
    }

    private void displayNote() {
        //get list of courses from DataManager
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);

        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        int courseRowIndex = 0;

         boolean more = cursor.moveToFirst();
         while(more){
             String cursorCourseId = cursor.getString(courseIdPos);
             if(courseId.equals(cursorCourseId))
                 break;
                 courseRowIndex++;
                 more = cursor.moveToNext();

         }
         return courseRowIndex;
    }

    private void readDisplayStateValue() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }
            Log.i(TAG, "mNoteId:" + mNoteId);
//            mNote = DataManager.getInstance().getNotes().get(mNoteId);

    }

    private void createNewNote() {
//        DataManager dm = DataManager.getInstance();
//        mNoteId = dm.createNewNote();
        //mNote = dm.getNotes().get(mNotePosition);
        final ContentValues values = new ContentValues();

        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mNoteUri = getContentResolver().insert(Notes.CONTENT_URI, values);
//                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
//                mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
//                return mNoteId;
                return mNoteUri;
            }
        };
        task.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }else if(id == R.id.action_cancel){
            mIsCancelling = true;
            finish();
        }else if(id == R.id.action_next){
            moveNext();
        } else if (id == R.id.action_set_reminder){
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

 @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    private void showReminderNotification() {
        String noteText = mTextNoteText.getText().toString();
        int notificationId = 0;
        String channelId = "MY_CHANNEL_ID";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Review Note")
                .setContentText(noteText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setTicker("Review Note")
                .setContentIntent(
                        PendingIntent.getActivity(
                                this,
                                0,
                                new Intent(this,
                                        NoteActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true);

            Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(path);

        NotificationManager noticeManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
            NotificationChannel channel = new NotificationChannel(channelId, "channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            noticeManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        noticeManager.notify(notificationId, builder.build());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item =  menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValue();
        displayNote();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if we are cancelling
        if(mIsCancelling){
            Log.i(TAG, "Cancelling note at position: " + mNoteId);
            //if we have created a new note
            if(mIsNewNote) {
                //remove note from backing store
                //DataManager.getInstance().removeNote(mNoteId);
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValue();
            }
        }else {
            // write to backing store to save our note
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " =?";
        final String[] selectionArgs = { Integer.toString(mNoteId)};
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    private void storePreviousNoteValue() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null)
            mViewModel.saveState(outState);
    }

    private void saveNote() {
        //set value of course in the note to what currently selected course is in spinner
        //mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();

        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;

    }

    public void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                return db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
            }
        };
        task.execute();
    }
    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();

        String subject = mTextNoteTitle.getText().toString();
        String text = "Wow , I have learnt the following in the Pluralsight.com course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;

        if( id ==  LOADER_NOTES)
           loader = createLoaderNotes();
        else  if(id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;

        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };
        return  new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);
//        return new CursorLoader(this){
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db  = mDbOpenHelper.getReadableDatabase();
//                String[] courseColumns = {
//                        CourseInfoEntry.COLUMN_COURSE_TITLE,
//                        CourseInfoEntry.COLUMN_COURSE_ID,
//                        CourseInfoEntry._ID
//                };
//
//                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
//                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
//
//            }
//        };
    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
                String[] noteColumns = {
                        Notes.COLUMN_COURSE_ID,
                        Notes.COLUMN_NOTE_TITLE,
                        Notes.COLUMN_NOTE_TEXT
                };
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri,noteColumns, null, null, null);
    }

//    private CursorLoader createLoaderNotes() {
//        mNotesQueryFinished = false;
//        return new CursorLoader(this){
//          @Override
//          public  Cursor loadInBackground(){
//              SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
//
//              String courseId = "android_intents";
//              String titleStart = "dynamic";
//              String selection = NoteInfoEntry._ID + " = ?";
//
//              String[] selectionArgs = {Integer.toString(mNoteId)};
//
//              String[] noteColumns = {
//                      NoteInfoEntry.COLUMN_COURSE_ID,
//                      NoteInfoEntry.COLUMN_NOTE_TITLE,
//                      NoteInfoEntry.COLUMN_NOTE_TEXT
//              };
//              return db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection,
//                      selectionArgs, null, null, null);
//          }
//        };
//    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES){
            loadFinishedNotes(data);

        }else if(loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueryIsFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();

        mNotesQueryFinished = true;
        displayNoteWhenQueryIsFinished();
    }

    private void displayNoteWhenQueryIsFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished){
            displayNote();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            if(mNoteCursor != null){
                mNoteCursor.close();
            }else if(loader.getId() == LOADER_COURSES){
                mAdapterCourses.changeCursor(null);
            }
        }

    }
}
