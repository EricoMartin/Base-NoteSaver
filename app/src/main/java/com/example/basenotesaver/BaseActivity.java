package com.example.basenotesaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basenotesaver.NoteKeeperDataBaseContract.NoteInfoEntry;
import com.example.basenotesaver.NoteKeeperProviderContract.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class BaseActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController mNavController;
//    private RecyclerView mRecyclerViewItems;
    private GridLayoutManager mCoursesGridLayoutManager;
    private LinearLayoutManager mNotesLayoutManager;
    private NoteKeeperOpenHelper mDbOpenHelper;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BaseActivity.this, NoteActivity.class) );
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.header_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.messages_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.email_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.sync_preferences, false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes, R.id.nav_courses)
                .setDrawerLayout(drawer)
                .build();
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, mNavController);

        setNavDestination();

        initializeDisplayContent();
    }

    private void setNavDestination() {
        mNavController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.nav_notes){
                    displayNotes();
                }else if(destination.getId() == R.id.nav_courses){
                    displayCourses();
                    //handleSelection("courses");
                }
            }
        });
    }

    private void handleSelection(String text) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }

    private void initializeDisplayContent() {
        DataManager.loadFromDataBase(mDbOpenHelper);
        final RecyclerView mRecyclerViewItems = (RecyclerView) findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCoursesGridLayoutManager =  new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));



        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        displayNotes();
    }

    private void displayCourses() {
        final RecyclerView mRecyclerViewItems = findViewById(R.id.list_items);
        mRecyclerViewItems.setLayoutManager(mCoursesGridLayoutManager);
        mRecyclerViewItems.setAdapter(mCourseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setCheckable(true);
    }

    private void displayNotes() {
        final RecyclerView mRecyclerViewItems = findViewById(R.id.list_items);
        mRecyclerViewItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerViewItems.setAdapter(mNoteRecyclerAdapter);


//        SQLiteDatabase mDB = mDbOpenHelper.getReadableDatabase();
        selectNavigationMenuItem(R.id.nav_notes);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

            //mAdapterNotes.notifyDataSetChanged();
            //mNoteRecyclerAdapter.notifyDataSetChanged();
        //loadNotes();

        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);

            updateNavHeader();
    }

    private void loadNotes() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        final String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID
        };
        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," +  NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                null, null, null, null, noteOrderBy);
        mNoteRecyclerAdapter.changeCursor(noteCursor);
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);

        TextView textName = view.findViewById(R.id.text_user_name);
        TextView textEmail = view.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String username =  pref.getString("user_name", "");
        String user_email = pref.getString("user_email", "");

        textName.setText(username);
        textEmail.setText(user_email);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
            return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    //This method uses ContentProvider Contract class to encapsulate the JOIN and provide a better data access solution like a simple table
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES){
            final String[] noteColumns = {
                            Notes._ID,
                            Notes.COLUMN_NOTE_TITLE,
                            Notes.COLUMN_COURSE_TITLE
                    };
            final String noteOrderBy =
                            Notes.COLUMN_COURSE_TITLE + "," +
                                    Notes.COLUMN_NOTE_TITLE
                    ;
            loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI,
                    noteColumns, null, null, noteOrderBy);
        }
        return loader;
    }

//    This method uses the CursorLoader to load up note title and text from the DB NoteInfoEntry class
//    and a JOIN to the DB CourseInfoEntry class to load up the course title
//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        CursorLoader loader = null;
//        if(id == LOADER_NOTES){
//            loader = new CursorLoader(this){
//                @Override
//                public Cursor loadInBackground() {
//                    SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
//                    final String[] noteColumns = {
//                            NoteInfoEntry.getQualifiedColumn(NoteInfoEntry._ID),
//                            NoteInfoEntry.COLUMN_NOTE_TITLE,
//                            CourseInfoEntry.COLUMN_COURSE_TITLE
//                    };
//                    final String noteOrderBy =
//                            CourseInfoEntry.COLUMN_COURSE_TITLE + "," +  NoteInfoEntry.COLUMN_NOTE_TITLE
//                    ;
//                    //Create a JOIN to return course_id column from both note_info and course_info tables
//                    // note_info JOIN course_info ON note_info.course_id = course_info.course_id;
//                  String queryString =  NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON " + NoteInfoEntry.getQualifiedColumn( NoteInfoEntry.COLUMN_COURSE_ID) + " = " + CourseInfoEntry.getQualifiedColumn(CourseInfoEntry.COLUMN_COURSE_ID);
//                    return db.query(queryString, noteColumns, null, null, null, null, noteOrderBy);
//                }
//            };
//        }
//        return loader;
//    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES){
            mNoteRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }
}
