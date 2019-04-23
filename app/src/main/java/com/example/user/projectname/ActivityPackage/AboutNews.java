package com.example.user.projectname.ActivityPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.projectname.AdapterPackage.Comment;
import com.example.user.projectname.AdapterPackage.FirebaseCommentRecylcerAdapter;
import com.example.user.projectname.AdapterPackage.News;
import com.example.user.projectname.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.user.projectname.ActivityPackage.AfterAddBtnClickedScreen.createTempImageFile;

public class AboutNews extends AppCompatActivity implements View.OnClickListener, ViewTreeObserver.OnWindowFocusChangeListener {

    boolean downloadImage = false;

    FirebaseDatabase database;
    DatabaseReference newsRef;
    StorageReference mStorageRef;

    TextView mainTV, dateTV, aboutTV, categoryTV;
    EditText commentET;
    ImageView imageNewsView;
    ScrollView scroll;

    News examplerNews = new News();

    ArrayList<Comment> comments;
    RecyclerView.LayoutManager commentLayoutManager;
    FirebaseCommentRecylcerAdapter commentAdapter;
    RecyclerView commentRecyclerView;

    Intent intentToImageViewer;
    String uri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_news);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        mStorageRef = FirebaseStorage.getInstance().getReference();

        intentToImageViewer = new Intent(this, ImageViewerActivity.class);

        imageNewsView = (ImageView) findViewById(R.id.imageNewsView);
        mainTV = (TextView) findViewById(R.id.mainText);
        dateTV = (TextView) findViewById(R.id.dateTV);
        aboutTV = (TextView) findViewById(R.id.aboutTV);
        categoryTV = (TextView) findViewById(R.id.categoryTV);
        commentET = (EditText) findViewById(R.id.commentEditText);
        scroll = (ScrollView) findViewById(R.id.scrollAboutNews);

        commentRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        commentLayoutManager = new LinearLayoutManager(this);
        commentRecyclerView.setLayoutManager(commentLayoutManager);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        commentRecyclerView.setItemAnimator(itemAnimator);
        comments = new ArrayList<>();
        commentAdapter = new FirebaseCommentRecylcerAdapter(comments);
        commentRecyclerView.setAdapter(commentAdapter);

        database = FirebaseDatabase.getInstance();
        newsRef = database.getReference();
        newsRef = newsRef.child("news");
        newsRef = newsRef.child(id);
        imageNewsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!uri.equals("")) {
                    intentToImageViewer.putExtra("Uri", uri);
                    startActivity(intentToImageViewer);
                } else
                    Toast.makeText(AboutNews.this, "Подождите, изображение не загрузилось", Toast.LENGTH_SHORT).show();
            }
        });
        newsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    News tempNews = new News(dataSnapshot.child("id").getValue().toString(),
                            dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("date").getValue().toString(),
                            dataSnapshot.child("category").getValue().toString(),
                            dataSnapshot.child("about").getValue().toString(),
                            dataSnapshot.child("author").getValue().toString(),
                            dataSnapshot.child("storagePath").getValue().toString(),
                            dataSnapshot.child("uri").getValue().toString(),
                            false);
                    if (!examplerNews.equals(tempNews)) {
                        examplerNews = tempNews;
                        mainTV.setText(examplerNews.getName());
                        dateTV.setText(examplerNews.getDate());
                        categoryTV.setText(examplerNews.getCategory());
                        aboutTV.setText(examplerNews.getAbout());

                        try {
                            downloadImage = true;
                            File localFile = createTempImageFile(getExternalCacheDir());
                            final File finalLocalFile = localFile;
                            mStorageRef.child(examplerNews.getStoragePath()).getFile(localFile)
                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Glide.with(AboutNews.this)
                                                    .load(Uri.fromFile(finalLocalFile))
                                                    .centerCrop()
                                                    .into(imageNewsView);
                                            uri = Uri.fromFile(finalLocalFile).toString();
                                            downloadImage = false;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Load", "" + e);
                                }
                            });
                        } catch (IOException e) {
                            Log.e("Exception", e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AboutNews.this, "Canceled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        newsRef.child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    comments.add(new Comment(dataSnapshot.child("author").getValue().toString(),
                            dataSnapshot.child("comment").getValue().toString()));
                    commentAdapter.notifyItemChanged(commentAdapter.getItemCount() - 1);
                }
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
            case R.id.goBack:
                onBackPressed();
                break;
            case R.id.sendComment:
                if (!commentET.getText().toString().isEmpty()) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                    Comment comment = new Comment(settings.getString("username", "username"),
                            commentET.getText().toString());
                    HashMap<String, Object> commentInMap = new HashMap<>();
                    commentInMap.put(newsRef.push().getKey(), comment.toMap());
                    newsRef.child("comments").updateChildren(commentInMap);
                    Toast.makeText(this, "комментарий отправлен", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Возникла ошибка при отправке комментария", Toast.LENGTH_SHORT).show();
                commentET.setText("");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!downloadImage)
            super.onBackPressed();
        else
            Toast.makeText(this, "Идет загрузка изображения, подождите", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        scroll.scrollTo(0, 0);
    }
}
