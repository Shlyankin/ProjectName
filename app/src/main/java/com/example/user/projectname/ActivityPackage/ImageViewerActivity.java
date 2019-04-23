package com.example.user.projectname.ActivityPackage;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.user.projectname.R;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class ImageViewerActivity extends AppCompatActivity implements View.OnClickListener {

    String uri = "";

    ImageView bigImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        bigImageView = (ImageView)findViewById(R.id.bigImageView);

        uri = getIntent().getStringExtra("Uri");
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(bigImageView);

        Glide.with(ImageViewerActivity.this)
                .load(Uri.parse(uri))
                .fitCenter()
                .into(bigImageView);

        photoViewAttacher.update();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.goBackInImgViewer:
                onBackPressed();
                break;
        }
    }
}
