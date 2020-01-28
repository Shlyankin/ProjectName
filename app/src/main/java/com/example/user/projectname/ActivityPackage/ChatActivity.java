package com.example.user.projectname.ActivityPackage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.projectname.AdapterPackage.Comment;
import com.example.user.projectname.AdapterPackage.FirebaseCommentRecylcerAdapter;
import com.example.user.projectname.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseDatabase database;
    DatabaseReference refChat;

    ArrayList<Comment> comments;
    RecyclerView.LayoutManager commentLayoutManager;
    FirebaseCommentRecylcerAdapter commentAdapter;
    RecyclerView commentRecyclerView;

    ScrollView scrollView;
    TextView countUsersView;
    EditText commentET;

    String nameChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        scrollView = (ScrollView)findViewById(R.id.scrollChat);
        commentET = (EditText)findViewById(R.id.commentEditText);
        countUsersView = (TextView) findViewById(R.id.countUsersView);

        nameChat = MainActivity.getId(getIntent().getStringExtra("name"));

        commentRecyclerView = (RecyclerView)findViewById(R.id.chatRecyclerView);
        commentLayoutManager = new LinearLayoutManager(this);
        commentRecyclerView.setLayoutManager(commentLayoutManager);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        commentRecyclerView.setItemAnimator(itemAnimator);
        comments = new ArrayList<>();
        commentAdapter = new FirebaseCommentRecylcerAdapter(comments);
        commentRecyclerView.setAdapter(commentAdapter);

        database = FirebaseDatabase.getInstance();
        refChat = database.getReference();
        refChat = refChat.child("chats");
        refChat = refChat.child(nameChat);
        refChat.child("countUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Toast.makeText(ChatActivity.this, "Чат удален", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    MainActivity.getRefUser().child("chats").child(MainActivity.getId(nameChat)).removeValue();
                }
                else {
                    countUsersView.setText(countUsersView.getText().toString() + dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refChat.child("chat").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                comments.add(new Comment(dataSnapshot.child("author").getValue().toString(),
                        dataSnapshot.child("comment").getValue().toString()));
                commentAdapter.notifyItemChanged(commentAdapter.getItemCount() - 1);
                commentRecyclerView.getLayoutManager().scrollToPosition(commentAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendComment:
                if(!commentET.getText().toString().isEmpty()) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                    Comment comment = new Comment(settings.getString("username", "username"),
                            commentET.getText().toString());
                    HashMap<String, Object> commentInMap = new HashMap<>();
                    commentInMap.put(refChat.push().getKey(), comment.toMap());
                    refChat.child("chat").updateChildren(commentInMap);
                    Toast.makeText(this, "Сообщение отправлено", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Возникла ошибка при отправке сообщения", Toast.LENGTH_SHORT).show();
                commentET.setText("");
                break;
            case R.id.goBack:
                onBackPressed();
                break;
        }
    }
}
