package com.example.basenotesaver;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {
    private NoteRecyclerAdapter mNoteRecyclerAdapter;

    //private ArrayAdapter<NoteInfo> mAdapterNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class) );
            }
        });

        initializeDisplayContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mAdapterNotes.notifyDataSetChanged();
        mNoteRecyclerAdapter.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {
//        final ListView listNotes = findViewById(R.id.list_notes);
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        mAdapterNotes = new ArrayAdapter<NoteInfo>(this, android.R.layout.simple_list_item_1, notes);
//
//        listNotes.setAdapter(mAdapterNotes);
//
//        //handler for user selection
//        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            //A nested anonymous class
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //called each time an item is clicked from our list view
//                Intent intent=new Intent(NoteListActivity.this, NoteActivity.class);
//                //get NoteInfo that corresponds to our selection
//                //NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
//                //pass selected parcel which contains our note into NoteActivity
//                intent.putExtra(NoteActivity.NOTE_POSITION, position);
//                startActivity(intent);
//            }
//        });
        final RecyclerView recyclerViewNotes = findViewById(R.id.list_notes);
        final LinearLayoutManager notesLayoutManager = new LinearLayoutManager(this);
        recyclerViewNotes.setLayoutManager(notesLayoutManager);

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);
        recyclerViewNotes.setAdapter(mNoteRecyclerAdapter);
    }

}
