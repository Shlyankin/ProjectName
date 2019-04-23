package com.example.user.projectname.AdapterPackage;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.projectname.ActivityPackage.MainActivity;
import com.example.user.projectname.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSearchingChatRecyclerAdapter extends RecyclerView.Adapter<FirebaseSearchingChatRecyclerAdapter.SearchingChatViewHolder> {

    List <Chat> chats;
    DatabaseReference refChats;

    public FirebaseSearchingChatRecyclerAdapter(List<Chat> chats, DatabaseReference refChats) {
        this.chats = chats;
        this.refChats = refChats;
    }

    @Override
    public SearchingChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searched_chat, parent, false);
        SearchingChatViewHolder viewHolder = new SearchingChatViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SearchingChatViewHolder holder, final int position) {
        final Chat chat = this.chats.get(position);

        holder.nameChat.setText(chat.getChatName());

        holder.addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String chatName = chats.get(position).getChatName();
                ArrayList<Chat> addedChats = MainActivity.getChats();
                boolean dontRepeat = true;
                for(Chat e : addedChats) {
                    if(e.getChatName().equals(chatName))
                        dontRepeat = false;
                }
                if(dontRepeat) {
                    refChats.child(MainActivity.getId(chatName)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (holder.passwordChat.getText().toString().equals(dataSnapshot.child("password").getValue())) {
                                Map<String, Object> chat = new HashMap<>();
                                chat.put(MainActivity.getId(chatName), chatName);
                                MainActivity.getRefUser().child("chats").updateChildren(chat);
                                refChats.child(MainActivity.getId(chatName)).child("countUsers").runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        //вот значение поля "datee"
                                        Integer countUsers = mutableData.getValue(Integer.class);
                                        if (countUsers == null) {
                                            return Transaction.success(mutableData);
                                        }
                                        countUsers = ++countUsers;
                                        mutableData.setValue(countUsers);
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                        if (databaseError == null) {
                                            //всё ОК
                                        } else {
                                            Toast.makeText(holder.itemView.getContext(), "Ошибка: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                Toast.makeText(holder.itemView.getContext(), "Чат добавлен", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else
                    Toast.makeText(holder.itemView.getContext(), "Чат уже добавлен", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class SearchingChatViewHolder extends RecyclerView.ViewHolder {

        TextView nameChat;
        EditText passwordChat;
        ImageButton addChatButton;

        public SearchingChatViewHolder(View itemView) {
            super(itemView);
            nameChat = (TextView) itemView.findViewById(R.id.nameChatView);
            passwordChat = (EditText) itemView.findViewById(R.id.passwordChatView);
            addChatButton = (ImageButton) itemView.findViewById(R.id.addChatButton);
        }
    }
}
