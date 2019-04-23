package com.example.user.projectname.AdapterPackage;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.projectname.ActivityPackage.ChatActivity;
import com.example.user.projectname.ActivityPackage.MainActivity;
import com.example.user.projectname.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class FirebaseChatRecyclerAdapter extends RecyclerView.Adapter<FirebaseChatRecyclerAdapter.ChatViewHolder> {
    List<Chat> chats;
    DatabaseReference refChats;

    public FirebaseChatRecyclerAdapter(List<Chat> chats, DatabaseReference refChats) {
        this.chats = chats;
        this.refChats = refChats;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        ChatViewHolder viewHolder = new ChatViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, final int position) {
        final Chat chat = this.chats.get(position);

        holder.nameChat.setText(chat.getChatName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("name", chats.get(position).getChatName());
                view.getContext().startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                User currentUser = MainActivity.getCurrentUser();
                if(currentUser != null && currentUser.getAdmin() == 1) {
                    refChats.child(MainActivity.getId(chats.get(position).getChatName())).removeValue();
                    Toast.makeText(holder.itemView.getContext(), "Чат удален", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nameChat;
        public ChatViewHolder(View itemView) {
            super(itemView);
            nameChat = (TextView) itemView.findViewById(R.id.nameChatView);
        }
    }
}
