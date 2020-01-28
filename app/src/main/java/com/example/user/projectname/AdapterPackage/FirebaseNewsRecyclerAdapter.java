package com.example.user.projectname.AdapterPackage;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.user.projectname.ActivityPackage.AboutNews;
import com.example.user.projectname.ActivityPackage.MainActivity;
import com.example.user.projectname.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirebaseNewsRecyclerAdapter extends RecyclerView.Adapter<NewsViewHolder>  {

    List<News> allNews;
    List<News> news;
    public FirebaseNewsRecyclerAdapter(List<News> allNews) {
        this.allNews = allNews;
        this.news = allNews;
    }

    /*public void updateLastNews() {
        news = allNews;
        this.notifyItemChanged(this.getItemCount() - 1);
    }*/

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        NewsViewHolder viewHolder = new NewsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder holder, final int position) {
        final News examplerNews = this.news.get(position);

        holder.heading.setText(examplerNews.getName());
        holder.dateTV.setText(examplerNews.getDate());

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), AboutNews.class);
                        intent.putExtra("id", news.get(position).getId());
                        view.getContext().startActivity(intent);
                    }
                }
        );

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                User user = MainActivity.getCurrentUser();
                if(user != null && user.getAdmin() == 1) {
                    new AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Удалить новость?")
                            .setMessage("Вы действительно хотите удалить новость?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //удалять новость с сервера
                                    String uri = news.get(position).getUri();
                                    if (uri.equals("https://firebasestorage.googleapis.com/v0/b/projectname-a7d30.appspot.com/o/image%2Flogo_color.png?alt=media&token=9e07ca14-cea8-4a4b-b420-41e0b82d5419")) {
                                        deleteNews(position);
                                    } else {
                                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(uri);
                                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // File deleted successfully
                                                deleteNews(position);
                                                Log.d("tag", "onSuccess: deleted file");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Toast.makeText(holder.itemView.getContext(), "ошибка при удалении " + exception.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            }).create().show();
                }
                return true;
            }
        });

        holder.subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //написать то, что будет происходить по нажатию на кнопку подписки на новость
                //менять картинку по нажатию и добавлять в firebase(в клиентскую ветку) подписку
                if (!news.get(position).getSubscribe()) {
                    holder.subscribeBtn.setImageResource(R.drawable.btn_star_big_on);
                    Map<String, Object> idNews = new HashMap<>();
                    idNews.put(news.get(position).getId(), news.get(position).getId());
                    MainActivity.getRefUser().child("subscribes").updateChildren(idNews);
                    //notifyDataSetChanged();
                } else {
                    holder.subscribeBtn.setImageResource(R.drawable.btn_star_big_off_selected);
                    MainActivity.getRefUser().child("subscribes").child(news.get(position).getId()).removeValue();
                    news.get(position).setSubscribe(false);
                    //notifyDataSetChanged();
                }
            }
        });
        if (news.get(position).getSubscribe()) {
            holder.subscribeBtn.setImageResource(R.drawable.btn_star_big_on);
        } else {
            holder.subscribeBtn.setImageResource(R.drawable.btn_star_big_off_selected);
        }
    }

    private void deleteNews(int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refNews = database.getReference().child("news").
                child(news.get(position).getId());
        refNews.removeValue();
    }

    public void deleteElement(int position, String category) {
        News deletedNews = allNews.get(position);
        if(deletedNews.getCategory().equals(category) || category.equals("Все новости")) {
            for (int i = 0; i < news.size(); i++) {
                if(deletedNews.getId().equals(news.get(i).getId())) {
                    news.remove(i);
                    allNews.remove(position);
                    this.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public void addElement(News examplerNews, String category) {
        if(examplerNews.getCategory().equals(category) || category.equals("Все новости")) {
            news.add(examplerNews);
            this.notifyItemChanged(this.getItemCount() - 1);
        }
    }

    public void updateAndSortAdapter(String category) {
        news = new ArrayList<>();
        for(int i = 0; i < allNews.size() ; i++) {
            if(allNews.get(i).getCategory().equals(category) || category.equals("Все новости"))
                news.add(allNews.get(i));
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public int getAllItemCount() {
        return allNews.size();
    }
}