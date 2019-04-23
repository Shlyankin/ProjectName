package com.example.user.projectname.ActivityPackage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.user.projectname.AdapterPackage.News;
import com.example.user.projectname.BuildConfig;
import com.example.user.projectname.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AfterAddBtnClickedScreen extends AppCompatActivity implements View.OnClickListener{

    FirebaseDatabase database;
    DatabaseReference newsRef;
    EditText name, date, time, about;
    ProgressBar uploadProgressBar;
    Spinner category;

    private File mTempPhoto;
    private String mImageUri = "";
    private String uriForNews = "";
    private String mRereference;
    UploadTask uploadTask;

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_CAMERA = 102;
    private static final int REQUEST_CODE_TAKE_PHOTO = 103;

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_add_btn_clicked_screen);

        database = FirebaseDatabase.getInstance();
        newsRef = database.getReference("news");
        mStorageRef = FirebaseStorage.getInstance().getReference();


        name =(EditText)findViewById(R.id.name);
        category =(Spinner)findViewById(R.id.category);
        time = (EditText)findViewById(R.id.time);
        date =(EditText)findViewById(R.id.date);
        about =(EditText)findViewById(R.id.about);
        uploadProgressBar = (ProgressBar)findViewById(R.id.uploadProgressBar);
        mRereference = MainActivity.getId(database.getReference().push().getKey()) + ".png";
    }

    public void addNews() {
        Intent intent = getIntent();
        String id = newsRef.push().getKey();
        String author = intent.getStringExtra("author");
        if(uploadProgressBar.getProgress() == 100 || uploadProgressBar.getProgress() == 0) {
            if (uriForNews.isEmpty()) {
                uriForNews = "https://firebasestorage.googleapis.com/v0/b/projectname-a7d30.appspot.com/o/image%2Flogo_color.png?alt=media&token=9e07ca14-cea8-4a4b-b420-41e0b82d5419";
                mRereference = "logo_color.png";
            }
            mRereference = "image/" + mRereference;
            News examplerNews = new News(id, name.getText().toString(),
                    time.getText().toString() + " " + date.getText().toString(),
                    category.getSelectedItem().toString(), about.getText().toString(), author,
                    mRereference, uriForNews, false);
            Map<String, Object> userValues = examplerNews.toMap();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put(id, userValues);

            newsRef.updateChildren(userMap);
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Изображение не загружено. Подождите немного или отмените загрузку", Toast.LENGTH_SHORT).show();
        }
    }


    private void addPhoto() {

        //Проверяем разрешение на работу с камерой
        boolean isCameraPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        //Проверяем разрешение на работу с внешнем хранилещем телефона
        boolean isWritePermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        //Если разрешения != true
        if(!isCameraPermissionGranted || !isWritePermissionGranted) {

            String[] permissions;//Разрешения которые хотим запросить у пользователя

            if (!isCameraPermissionGranted && !isWritePermissionGranted) {
                permissions = new String[] {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (!isCameraPermissionGranted) {
                permissions = new String[] {android.Manifest.permission.CAMERA};
            } else {
                permissions = new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            //Запрашиваем разрешения у пользователя
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_RECEIVE_CAMERA);
        } else {
            //Если все разрешения получены
            try {
                mTempPhoto = createTempImageFile(getExternalCacheDir());
                mImageUri = "file:" + mTempPhoto.getAbsolutePath(); //?

                //Создаём лист с интентами для работы с изображениями
                List<Intent> intentList = new ArrayList<>();
                Intent chooserIntent = null;


                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(AfterAddBtnClickedScreen.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        mTempPhoto));

                intentList = addIntentsToList(this, intentList, pickIntent);
                intentList = addIntentsToList(this, intentList, takePhotoIntent);

                if (!intentList.isEmpty()) {
                    chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),"Choose your image source");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
                }

                /*После того как пользователь закончит работу с приложеним(которое работает с изображениями)
                 будет вызван метод onActivityResult
                */
                startActivityForResult(chooserIntent, REQUEST_CODE_TAKE_PHOTO);
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
            }
        }
    }

    //Получаем абсолютный путь файла из Uri
    private String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int columnIndex = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    /*
      File storageDir -  абсолютный путь к каталогу конкретного приложения на
      основном общем /внешнем устройстве хранения, где приложение может размещать
      файлы кеша, которыми он владеет.
     */
    public static File createTempImageFile(File storageDir) throws IOException {

        // Генерируем имя файла
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());//получаем время
        String imageFileName = "photo_" + timeStamp;//состовляем имя файла

        //Создаём файл
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /*
    Метод для добавления интента в лист интентов
    */
    public static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public void uploadFileInFireBaseStorage (final Uri uri){
        uploadTask = mStorageRef.child("image/" + mRereference).putFile(uri);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = Integer.parseInt(Long.toString((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()));
                if(progress != 0)
                    uploadProgressBar.setProgress(progress);
                else
                    uploadProgressBar.setProgress(1);
                Log.i("Load","Upload is " + progress + "% done");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                uriForNews = downloadUri.toString();
                Toast.makeText(AfterAddBtnClickedScreen.this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                uploadProgressBar.setProgress(100);
                Log.i("Load" , "Uri download" + downloadUri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(AfterAddBtnClickedScreen.this, "ошибка при загрузке изображения " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addImage:
                if(uploadProgressBar.getProgress() != 0) {
                    Toast.makeText(this, "Изображение уже загружено", Toast.LENGTH_SHORT).show();
                } else {
                    uploadProgressBar.incrementProgressBy(1);
                    addPhoto();
                }
                break;
            case R.id.addNews:
                addNews();
                break;
            case R.id.deleteImage:
                deleteImage();
                break;
        }
    }

    private void deleteImage() {
        if(uploadProgressBar.getProgress() == 100) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(uriForNews);
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d("tag", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(AfterAddBtnClickedScreen.this, "ошибка при удалении " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else if(uploadProgressBar.getProgress() != 0 && uploadProgressBar.getProgress() != 100) {
            if(uploadTask != null) {
                uploadTask.cancel();
            }
        }
        uploadProgressBar.setProgress(0);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode , resultCode , data);
        switch (requestCode){
            case REQUEST_CODE_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    uploadProgressBar.incrementProgressBy(1);
                    if (data != null && data.getData() != null) {
                        mImageUri = getRealPathFromURI(data.getData());
                        uploadFileInFireBaseStorage(data.getData());
                    } else if (mImageUri != null) {
                        mImageUri = Uri.fromFile(mTempPhoto).toString();
                        uploadFileInFireBaseStorage(Uri.fromFile((mTempPhoto)));
                    }
                }
                else {
                    uploadProgressBar.setProgress(0);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        deleteImage();
        super.onBackPressed();
    }
}

