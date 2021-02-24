package com.example.basenotesaver;

import android.provider.BaseColumns;

public final class NoteKeeperDataBaseContract {


    //Make non-recreatable
    private NoteKeeperDataBaseContract(){
    }

    public static final class CourseInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        //format for creating an index
        //CREATE (UNIQUE) INDEX <name your index> ON <table name> (column name or comma seperated list of column names);
        //CREATE INDEX course_info_index1 ON course_info (course_title)

        public static final String INDEX1 = TABLE_NAME + "_index1";
        public static final String SQL_CREATE_INDEX1 = "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME
                + "(" + COLUMN_COURSE_TITLE + ")";

        // Helper function returns the qualified column name to avoid ambiguity
        public static final String getQualifiedColumn(String columnName){
            return TABLE_NAME + "." + columnName;
        }

        // CREATE TABLE course_info (course_id, course_title)
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL, " +
                COLUMN_COURSE_TITLE + " TEXT NOT NULL)";
    }

    public static final class NoteInfoEntry implements BaseColumns{
        public static final String TABLE_NAME ="note_info";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final  String COLUMN_COURSE_ID = "course_id";

        public static final String INDEX1 = TABLE_NAME + "_index1";
        public static final String SQL_CREATE_INDEX1 = "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME
                + "(" + COLUMN_NOTE_TITLE + ")";

        // Helper function returns the qualified column name to avoid ambiguity
        public static final String getQualifiedColumn(String columnName){
            return TABLE_NAME + "." + columnName;
        }
        // CREATE TABLE note_info (note_id, note_title, course_id)
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NOTE_TITLE + " TEXT NOT NULL, " +
                COLUMN_NOTE_TEXT + " TEXT, " +
                COLUMN_COURSE_ID + " TEXT NOT NULL)";
    }
}
