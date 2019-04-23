package com.example.user.projectname.AdapterPackage;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.projectname.ActivityPackage.AboutNews;
import com.example.user.projectname.ActivityPackage.MainActivity;
import com.example.user.projectname.R;

import java.util.List;

public class FirebaseSubscribeNewsRecyclerAdapter extends RecyclerView.Adapter<NewsViewHolder> {

    List<News> subscribeNews;

    public FirebaseSubscribeNewsRecyclerAdapter(List<News> news) {
        this.subscribeNews = news;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        NewsViewHolder viewHolder = new NewsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder holder, final int position) {
        final News examplerNews = this.subscribeNews.get(position);
        holder.subscribeBtn.setImageResource(R.drawable.btn_star_big_on);
        holder.heading.setText(examplerNews.getName());
        holder.dateTV.setText(examplerNews.getDate());

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), AboutNews.class);
                        intent.putExtra("id", subscribeNews.get(position).getId());
                        view.getContext().startActivity(intent);
                    }
                }
        );

        holder.subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //написать то, что будет происходить по нажатию на кнопку подписки на новость
                //менять картинку по нажатию и удалять в firebase(в клиентской ветке) подписку
                //удалять из recyclerview нажатую новость(удаление в news
                // + FirebaseSubscribeNewsRecyclerAdapter.this.notifyItemChanged(position);
                holder.subscribeBtn.setImageResource(R.drawable.btn_star_big_off_selected);
                MainActivity.getRefUser().child("subscribes").child(subscribeNews.get(position).getId()).removeValue();
            }
        });
    }

    public void addElement(News examplerNews) { //добавляет новость в recyclerview
        subscribeNews.add(examplerNews);
        this.notifyItemChanged(this.getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return subscribeNews.size();
    }
}
