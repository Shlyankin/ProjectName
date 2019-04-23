package com.example.user.projectname.ActivityPackage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.projectname.AdapterPackage.User;
import com.example.user.projectname.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    Button signUp;
    EditText email, pass, repPass, username;
    CheckBox checkAutoLogin;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference refUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        refUser = database.getReference();
        refUser = refUser.child("users");



        signUp = (Button) findViewById(R.id.signUP);
        pass = (EditText) findViewById(R.id.pass);
        repPass = (EditText) findViewById(R.id.repPass);
        email = (EditText) findViewById(R.id.email);
        checkAutoLogin = (CheckBox) findViewById(R.id.checkAutoLogin2);
        username = (EditText) findViewById(R.id.username);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signUP:
                final String [] s = { email.getText().toString(), pass.getText().toString(), repPass.getText().toString() };
                if (s[1].equals(s[2]) && !(s[0].isEmpty() || s[1].isEmpty()) && !username.getText().toString().isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(s[0], s[1])
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        //startActivity(new Intent(SignUpActivity.this, LoginActivity.class));

                                        String id = MainActivity.getId(mAuth.getCurrentUser().getEmail().toString());
                                        User user = new User(0, username.getText().toString());
                                        Map<String, Object> userValues = user.toMap();
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("profile", userValues);
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put(id.toString(), userData);
                                        refUser.updateChildren(userMap);

                                        //сохранение настроек автологина
                                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SignUpActivity.this);
                                        SharedPreferences.Editor edit = settings.edit();
                                        edit.putString("username", user.getName());
                                        if (checkAutoLogin.isChecked()) {
                                            edit.remove("autoLogin");
                                            edit.putBoolean("autoLogin", true);
                                            edit.putString("login", s[0]);
                                            edit.putString("password", s[1]);
                                            edit.commit();
                                        } else {
                                            edit.remove("autoLogin");
                                            edit.putBoolean("autoLogin", false);
                                            edit.remove("login");
                                            edit.remove("password");
                                            edit.commit();
                                            sendLetter();
                                        }
                                        onBackPressed();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Ошибка при регистрации\n" +
                                                        task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    pass.setText("");
                    repPass.setText("");
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void sendLetter() {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(SignUpActivity.this, "Письмо для подтверждения email было отправлено", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(SignUpActivity.this, "Не могу отослать письмо на ваш email снова. Ваш аккаунт удален", Toast.LENGTH_SHORT).show();
                    mAuth.getCurrentUser().delete();
                }
            }
        });
    }
}
