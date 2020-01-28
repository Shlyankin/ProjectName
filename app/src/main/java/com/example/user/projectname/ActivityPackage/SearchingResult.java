package com.example.user.projectname.ActivityPackage;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.user.projectname.AdapterPackage.Chat;
import com.example.user.projectname.AdapterPackage.FirebaseSearchingChatRecyclerAdapter;
import com.example.user.projectname.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SearchingResult extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference refChats;
    String searchingValue = "";

    TextView notResult;

    RecyclerView.LayoutManager mLayoutManager;
    FirebaseSearchingChatRecyclerAdapter mAdapter;
    RecyclerView mRecyclerView;
    ArrayList<Chat> chats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_result);

        notResult = (TextView)findViewById(R.id.notResult);

        Intent intent = getIntent();
        searchingValue = intent.getStringExtra("searchingValue");

        database = FirebaseDatabase.getInstance();
        refChats = database.getReference();
        refChats = refChats.child("chats");

        mRecyclerView = (RecyclerView)findViewById(R.id.searchingRecylcerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.canScrollVertically(View.SCROLL_AXIS_VERTICAL);
        mRecyclerView.hasFixedSize();
        chats = new ArrayList<>();
        mAdapter = new FirebaseSearchingChatRecyclerAdapter(chats, refChats);
        mRecyclerView.setAdapter(mAdapter);


        refChats.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.child("chatName").getValue() != null && searchingValue.contains(dataSnapshot.child("chatName").getValue().toString())) {
                    chats.add(new Chat(dataSnapshot.child("chatName").getValue().toString()));
                    mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
                    notResult.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("chatName").getValue() != null && searchingValue.contains(dataSnapshot.child("chatName").getValue().toString())) {
                    chats.remove(new Chat(dataSnapshot.child("chatName").getValue().toString()));
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
