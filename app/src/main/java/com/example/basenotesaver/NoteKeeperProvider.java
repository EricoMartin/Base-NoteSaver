package com.example.basenotesaver;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.basenotesaver.NoteKeeperDataBaseContract.CourseInfoEntry;

import static com.example.basenotesaver.NoteKeeperProviderContract.AUTHORITY;

public class NoteKeeperProvider extends ContentProvider {

     private NoteKeeperOpenHelper mDbOpenHelper;
     private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    public static final int NOTES_ROW = 3;

    static {
         sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Courses.PATH, COURSES);
         sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Notes.PATH, NOTES);
         sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
     String mimeType = null;
     int uriMatch = sUriMatcher.match(uri);
     switch (uriMatch){
         case COURSES:
             //vnd.android,cursor.dir/vnd.com.example.basenotesaver.provider.courses
             mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ "vnd." + AUTHORITY + "." + NoteKeeperProviderContract.Courses.PATH;
     break;
         case NOTES:
             mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ "vnd." + AUTHORITY + "." + NoteKeeperProviderContract.Notes.PATH;
     break;
         case NOTES_EXPANDED:
             mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ "vnd." + AUTHORITY + "." + NoteKeeperProviderContract.Notes.PATH_EXPANDED;
             break;
         case NOTES_ROW:
             mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/"+ "vnd." + AUTHORITY + "." + NoteKeeperProviderContract.Notes.PATH;
     }
     return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
//                mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                rowId = db.insert(NoteKeeperDataBaseContract.NoteInfoEntry.TABLE_NAME, null, values);
                //content://com.example.basenotesaver.provider/notes/1
               rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI, rowId);
               break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                try {
                    throw new Exception("This Table is Read-only!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {

        mDbOpenHelper = new NoteKeeperOpenHelper((getContext()));
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteKeeperDataBaseContract.NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);

                String rowSelection = NoteKeeperDataBaseContract.NoteInfoEntry._ID + " =?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteKeeperDataBaseContract.NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null, null, null);
                break;
        }
         return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] columns = new String[projection.length];
        for(int idx = 0; idx < projection.length; idx++)
        {
            columns[idx] = projection[idx].equals(BaseColumns._ID) || projection[idx].equals(NoteKeeperProviderContract.CoursesIdColumns.COLUMN_COURSE_ID) ?
                    NoteKeeperDataBaseContract.NoteInfoEntry.getQualifiedColumn(projection[idx]) : projection[idx];
        }
        String tablesWithJoin = NoteKeeperDataBaseContract.NoteInfoEntry.TABLE_NAME +
                " JOIN " + CourseInfoEntry.TABLE_NAME + " ON " +
                NoteKeeperDataBaseContract.NoteInfoEntry.getQualifiedColumn(NoteKeeperDataBaseContract.NoteInfoEntry.COLUMN_COURSE_ID) + " = " + CourseInfoEntry.getQualifiedColumn(CourseInfoEntry.COLUMN_COURSE_ID);
    return db.query(tablesWithJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}