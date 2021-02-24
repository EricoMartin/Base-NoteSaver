package com.example.basenotesaver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "NoteKeeper.db";
    public static final int DATABASE_VERSION = 2;

    public NoteKeeperOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Creates Empty Database Tables
        sqLiteDatabase.execSQL(NoteKeeperDataBaseContract.CourseInfoEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(NoteKeeperDataBaseContract.NoteInfoEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(NoteKeeperDataBaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
        sqLiteDatabase.execSQL(NoteKeeperDataBaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);

        //Adds Data rows to our Database Tables (course_info and note_info)
        DatabaseDataWorker worker = new DatabaseDataWorker(sqLiteDatabase);
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(oldVersion < 2){
            sqLiteDatabase.execSQL(NoteKeeperDataBaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
            sqLiteDatabase.execSQL(NoteKeeperDataBaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);
        }
    }
}
