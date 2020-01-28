package com.example.user.projectname.AdapterPackage;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.projectname.R;

import java.util.List;

public class FirebaseCommentRecylcerAdapter extends RecyclerView.Adapter<FirebaseCommentRecylcerAdapter.CommentsViewHolder>{

    List<Comment> comments;

    public FirebaseCommentRecylcerAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        CommentsViewHolder viewHolder = new CommentsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        final Comment comment = this.comments.get(position);

        holder.usernameComment.setText(comment.getAuthor());
        holder.textComment.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView usernameComment, textComment;


        public CommentsViewHolder(View itemView) {
            super(itemView);
            usernameComment = (TextView) itemView.findViewById(R.id.usernameComment);
            textComment = (TextView) itemView.findViewById(R.id.textComment);
        }
    }
}
