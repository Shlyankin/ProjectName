package com.example.user.projectname.ActivityPackage;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.projectname.AdapterPackage.Chat;
import com.example.user.projectname.AdapterPackage.CustomNote;
import com.example.user.projectname.AdapterPackage.FirebaseChatRecyclerAdapter;
import com.example.user.projectname.AdapterPackage.FirebaseNewsRecyclerAdapter;
import com.example.user.projectname.AdapterPackage.FirebaseSubscribeNewsRecyclerAdapter;
import com.example.user.projectname.AdapterPackage.News;
import com.example.user.projectname.AdapterPackage.User;
import com.example.user.projectname.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hirondelle.date4j.DateTime;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static DatabaseReference refUser;
    private FirebaseDatabase database;
    private DatabaseReference refNews;
    private DatabaseReference refChats;
    private FirebaseAuth mAuth;

    private FloatingActionButton addFloatingBtn, addChatFloatingBtn;
    private TextView name;
    private Spinner category;
    private TabHost tabHost;
    private EditText searchEditText;
    private String[] categoryText = {"Все новости", "Стажировки", "Олимпиады", "Конкурсы", "Мероприятия", "Вакансии", "" };

    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseNewsRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<News> news;

    private FirebaseChatRecyclerAdapter chatAdapter;
    private RecyclerView chatRecyclerView;
    private static ArrayList<Chat> chats;

    private static User user;


    private RecyclerView mSubscribeRecyclerView;
    private ArrayList<String> idSubsribeNews = new ArrayList<>();
    private ArrayList<News> subscribeNews;
    private FirebaseSubscribeNewsRecyclerAdapter mSubscribeAdapter;

    CaldroidFragment caldroidFragment;
    private ArrayList<CustomNote> noteArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Ошибка при входе в аккаунт\nПроверьте подключение к интернету", Toast.LENGTH_LONG).show();
            onBackPressed();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = (EditText)findViewById(R.id.searchEditText);
        tabHost = (TabHost) findViewById(R.id.tabHost);
        addFloatingBtn = (FloatingActionButton)findViewById(R.id.addFloatingBtn);
        addChatFloatingBtn = (FloatingActionButton)findViewById(R.id.addChatFloatingButton);
        name = (TextView)findViewById(R.id.NameUser);

        caldroidFragment = new CaldroidFragment();

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.tab1CalendarLayout, caldroidFragment);
        t.commit();

        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                if(caldroidFragment.getBackgroundForDateTimeMap().get(DateTime.isParseable(date.toString()))
                         == null) {
                    onCreateCreateNoteDialog(date).show();
                } else {
                    onCreateCreateNoteDialog(date).show();
                }

            }
        });

        createTabhost();

        database = FirebaseDatabase.getInstance();

        String id = getId(mAuth.getCurrentUser().getEmail().toString());
        refUser = database.getReference();
        refUser = refUser.child("users");
        refUser = refUser.child(id);
        refUser.child("note").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noteArrayList.removeAll(noteArrayList);
                for(DataSnapshot e : dataSnapshot.getChildren()) {
                    noteArrayList.add(new CustomNote(e.child("date").getValue().toString(), e.child("text").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Canceled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        refUser.child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = new User();
                user.admin = Integer.parseInt(dataSnapshot.child("admin").getValue().toString());
                user.name = dataSnapshot.child("name").getValue().toString();
                name.setText(user.getName());
                if(user.getAdmin() == 1) {
                    addFloatingBtn.setVisibility(View.VISIBLE);
                    addFloatingBtn.setClickable(true);
                    addChatFloatingBtn.setVisibility(View.VISIBLE);
                    addChatFloatingBtn.setClickable(true);
                } else {
                    addFloatingBtn.setVisibility(View.INVISIBLE);
                    addFloatingBtn.setClickable(false);
                    addChatFloatingBtn.setVisibility(View.INVISIBLE);
                    addChatFloatingBtn.setClickable(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Canceled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        refUser.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats.removeAll(chats);
                for(DataSnapshot e : dataSnapshot.getChildren()) {
                    chats.add(new Chat(e.getValue().toString()));
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Canceled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        refNews = database.getReference();
        refNews = refNews.child("news");
        refNews.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null)
                    addElementArray(dataSnapshot, news);
                else
                {
                    Toast.makeText(MainActivity.this, "No connection to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                    deleteElementArray(dataSnapshot, news);
                else
                {
                    Toast.makeText(MainActivity.this, "No connection to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        refChats = database.getReference();
        refChats = refChats.child("chats");

        createNewsRecyclerView();
        createChatRecyclerView();
        createSeubscribeNewsRecyclerView();


        //обновление списка при выборе категории
        category = (Spinner)findViewById(R.id.category);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                categoryText[6] = categoryText[i];
                mAdapter.updateAndSortAdapter(categoryText[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }



    private void addElementArray(DataSnapshot dataSnapshot, List news) {
        News examplerNews = new News(dataSnapshot.child("id").getValue().toString(),
                 dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("date").getValue().toString(),
                 dataSnapshot.child("category").getValue().toString(),
                 dataSnapshot.child("about").getValue().toString(),
                 dataSnapshot.child("author").getValue().toString(),
                 dataSnapshot.child("storagePath").getValue().toString(),
                 dataSnapshot.child("uri").getValue().toString(),
                 false);
        news.add(examplerNews);
        if(!checkStringDateAndCorrect(examplerNews.getDate()).isEmpty()) {
            try {
                caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.caldroid_holo_blue_light))
                        , new Date(examplerNews.getDate()));
                caldroidFragment.refreshView();
            } catch (IllegalArgumentException e) {
            }
        }
        if(!subscribeNews.contains(examplerNews))
            for(String e : idSubsribeNews) {
                if(e.equals(examplerNews.getId())) {
                    examplerNews.setSubscribe(true);
                    subscribeNews.add(examplerNews);
                    mSubscribeAdapter.notifyItemChanged(mSubscribeAdapter.getItemCount() - 1);
                }
            }
        mAdapter.addElement(examplerNews, categoryText[6]);
        mAdapter.notifyDataSetChanged();
    }

    public static String checkStringDateAndCorrect(String dateString) {
        if(dateString.isEmpty())
            return "";
        StringBuilder result = new StringBuilder();
        int spaceIndex = dateString.indexOf(' ');
        for(int i = spaceIndex+ 1; i < dateString.length(); i++) {
            if(dateString.charAt(i) <= '9' && dateString.charAt(i) >= '0' && (i - spaceIndex) <= 10)
                result.append(dateString.charAt(i));
            else if(dateString.charAt(i) == '/' && (i == (5 + spaceIndex) || i == (8 + spaceIndex)))
                result.append(dateString.charAt(i));
            else
                return "";
        }
        return result.toString();
    }

    private void deleteElementArray(DataSnapshot dataSnapshot, ArrayList<News> news) {
        String id = dataSnapshot.child("id").getValue().toString();
        for(int i = 0; i < news.size(); i++) {
            if(id.equals(news.get(i).getId())) {
                if(!checkStringDateAndCorrect(news.get(i).getDate()).isEmpty()) {
                    try {
                        caldroidFragment.clearBackgroundDrawableForDate(new Date(news.get(i).getDate()));
                        caldroidFragment.refreshView();
                    } catch (IllegalArgumentException e) {
                    }
                }
                mAdapter.deleteElement(i, categoryText[6]);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void createNewsRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerList);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.canScrollVertically(View.SCROLL_AXIS_VERTICAL);
        mRecyclerView.hasFixedSize();
        news = new ArrayList<>();
        mAdapter = new FirebaseNewsRecyclerAdapter(news);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void createChatRecyclerView() {
        chatRecyclerView = (RecyclerView)findViewById(R.id.chatRecyclerview);
        mLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        chatRecyclerView.setItemAnimator(itemAnimator);
        chatRecyclerView.canScrollVertically(View.SCROLL_AXIS_VERTICAL);
        chatRecyclerView.hasFixedSize();
        chats = new ArrayList<>();
        chatAdapter = new FirebaseChatRecyclerAdapter(chats, refChats);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void createSeubscribeNewsRecyclerView() {
        mSubscribeRecyclerView = (RecyclerView)findViewById(R.id.subscribeNewsRecyclerAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mSubscribeRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mSubscribeRecyclerView.setItemAnimator(itemAnimator);
        mSubscribeRecyclerView.canScrollVertically(View.SCROLL_AXIS_VERTICAL);
        mSubscribeRecyclerView.hasFixedSize();
        subscribeNews = new ArrayList<>();
        mSubscribeAdapter = new FirebaseSubscribeNewsRecyclerAdapter(subscribeNews);
        mSubscribeRecyclerView.setAdapter(mSubscribeAdapter);
    }

    private void createTabhost() {
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag0");
        tabSpec.setContent(R.id.tab0ChatsLayout);
        tabSpec.setIndicator("Беседы");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.tab1CalendarLayout);
        tabSpec.setIndicator("Календарь");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.tab2NewsLayout);
        tabSpec.setIndicator("Новости");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setContent(R.id.tab3CabinetLayout);
        tabSpec.setIndicator("Кабинет");
        tabHost.addTab(tabSpec);

        tabHost.canScrollHorizontally(View.SCROLL_AXIS_HORIZONTAL);
        tabHost.setHorizontalScrollBarEnabled(true);

        tabHost.setCurrentTab(2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addFloatingBtn:
                Intent intent = new Intent(MainActivity.this, AfterAddBtnClickedScreen.class);
                intent.putExtra("id", mAdapter.getAllItemCount());
                intent.putExtra("author", name.getText().toString());
                startActivity(intent);
                break;
            case R.id.addChatFloatingButton:
                onCreateCreateChatDialog().show();
                break;
            case R.id.searchBtn:
                if(!searchEditText.getText().toString().isEmpty()) {
                    Intent intentToSearchingResult = new Intent(MainActivity.this, SearchingResult.class);
                    intentToSearchingResult.putExtra("searchingValue", searchEditText.getText().toString());
                    startActivity(intentToSearchingResult);
                }
                break;
            case R.id.changePasswordButton:
                onCreateChangePasswordDialog().show();
                break;
        }
    }

    public boolean checkNewsOnDate(Date date) {
        String currentDate = new SimpleDateFormat("yyyy/MM/dd").format(date);
        for(News e : news) {
            if(checkStringDateAndCorrect(e.getDate()).equals(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public Dialog onCreateCreateNoteDialog(final Date date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogLayout = inflater.inflate(R.layout.create_note_dialog, (ViewGroup)findViewById(R.id.linearLayoutNoteDialog));

        TextView textOfNote = (TextView)dialogLayout.findViewById(R.id.textOfNoteView);
        for(CustomNote e : noteArrayList) {
            if(e.getDate().equals(date.toString())) {
                textOfNote.setText(e.getText());
                break;
            }
        }
        builder.setView(dialogLayout)
                .setNeutralButton("Удалить запись", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refUser.child("note").child(date.toString()).removeValue();
                        if(checkNewsOnDate(date)) {
                                caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.caldroid_holo_blue_light)), date);
                        } else {
                            caldroidFragment.clearBackgroundDrawableForDate(date);
                        }
                        caldroidFragment.refreshView();
                    }
                })
                .setPositiveButton("Создать запись", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText noteET = (EditText)dialogLayout.findViewById(R.id.noteView);
                        if(!noteET.getText().toString().isEmpty()) {
                            HashMap<String, Object> noteHM = new HashMap();
                            CustomNote note = new CustomNote(date.toString(), noteET.getText().toString());
                            noteHM.put(date.toString(), note.toMap());
                            refUser.child("note").updateChildren(noteHM);
                            if (checkNewsOnDate(date)) {
                                caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources()
                                        .getColor(R.color.caldroid_sky_blue)), date);
                            } else {
                                caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources()
                                        .getColor(R.color.caldroid_light_red)), date);
                            }
                            caldroidFragment.refreshView();
                        } else {
                            Toast.makeText(MainActivity.this, "Вы ничего не написали", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null);
        return builder.create();
    }

    public Dialog onCreateChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogLayout = inflater.inflate(R.layout.change_password_dialog, (ViewGroup)findViewById(R.id.linearLayoutDialog));
        builder.setView(dialogLayout)
                // Add action buttons
                .setPositiveButton("Сменить пароль", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText oldPassword = (EditText)dialogLayout.findViewById(R.id.oldPasswordPassDialog);
                        EditText password = (EditText)dialogLayout.findViewById(R.id.passwordPassDialog);
                        EditText repPassword = (EditText)dialogLayout.findViewById(R.id.repPasswordPassDialog);
                        String oldPasswordString = oldPassword.getText().toString();
                        final String passwordString = password.getText().toString();
                        String repPasswordString = repPassword.getText().toString();
                        if(!passwordString.isEmpty()) {
                            if (repPasswordString.equals(passwordString)) {
                                final FirebaseUser userTemp = mAuth.getCurrentUser();

                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(userTemp.getEmail(), oldPasswordString);

                                userTemp.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    userTemp.updatePassword(passwordString).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(MainActivity.this, "Пароль изменен", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(MainActivity.this, "Ошибка. Пароль не изменен", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Ошибка: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(MainActivity.this, "Пароли не совпадают\nПопробуйте еще раз", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Отмена", null);
        return builder.create();
    }

    public Dialog onCreateCreateChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogLayout = inflater.inflate(R.layout.create_chat_dialog, (ViewGroup)findViewById(R.id.linearLayoutDialog));
        builder.setView(dialogLayout)
                .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText nameChat = (EditText)dialogLayout.findViewById(R.id.nameChatDialog);
                        EditText passwordChat = (EditText)dialogLayout.findViewById(R.id.passwordChatDialog);
                        final String name = nameChat.getText().toString();
                        final String password = passwordChat.getText().toString();
                        refChats.child(getId(name)).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                //вот значение поля "datee"
                                if(mutableData.getValue() == null) {
                                    if(!name.isEmpty()) {
                                        Chat chat = new Chat(name, password);
                                        Map chatMap = chat.toMap();
                                        mutableData.setValue(chatMap);
                                        return Transaction.success(mutableData);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Поле название беседы пусто", Toast.LENGTH_SHORT).show();
                                        return Transaction.abort();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Название занято", Toast.LENGTH_SHORT).show();
                                    return Transaction.abort();
                                }
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if (databaseError == null) {
                                    Map<String, Object> chat = new HashMap<>();
                                    chat.put(MainActivity.getId(name), name);
                                    MainActivity.getRefUser().child("chats").updateChildren(chat);
                                } else {
                                    Toast.makeText(MainActivity.this, "Ошибка. Возможно название уже занято или в название присутствуют недопустимые символы", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Отмена", null);
        return builder.create();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из вашего аккаунта?")
                .setMessage("Вы действительно хотите выйти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    protected void onDestroy() {
        mAuth.signOut();
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        //tabHost.setCurrentTab(2);
        refUser.child("subscribes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idSubsribeNews.removeAll(idSubsribeNews);
                subscribeNews.removeAll(subscribeNews);
                for(News i : news) {
                    i.setSubscribe(false);
                }
                for(DataSnapshot e : dataSnapshot.getChildren()) {
                    idSubsribeNews.add(e.getValue().toString());
                    loop1:for(News i : news) {
                        if(i.getId().equals(idSubsribeNews.get(idSubsribeNews.size() - 1))) {
                            subscribeNews.add(i);
                            i.setSubscribe(true);
                            break loop1;
                        }
                    }
                }
                mSubscribeAdapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Canceled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @NonNull
    public static String getId (String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character .isLetterOrDigit(s.charAt(i)))
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    public static ArrayList<Chat> getChats() {
        return chats;
    }

    public static DatabaseReference getRefUser() { return refUser; }
    public static User getCurrentUser() { return user; }
}
