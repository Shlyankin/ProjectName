package com.example.user.projectname.ActivityPackage;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.projectname.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    EditText email, pass;
    CheckBox checkAutoLogin;
    Button enterBtn, registrationBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        enterBtn = (Button)findViewById(R.id.enterBtn);
        registrationBtn = (Button)findViewById(R.id.registrationBtn);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.pass);
        checkAutoLogin = (CheckBox) findViewById(R.id.checkAutoLogin1);
        writeEmailAndPass();
        onClick(findViewById(R.id.enterBtn));
    }

    private void writeEmailAndPass() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean("autoLogin", false)) { // чекаем настройки автологина и заполняем поля
            email.setText(settings.getString("login",""));
            pass.setText(settings.getString("password", ""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        writeEmailAndPass();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registrationBtn: //действие на кнопку регистрации
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
            case R.id.enterBtn: //действие на кнопку входа
                //проверить введенные логин и пароль по серверу
                enterBtn.setClickable(false);
                registrationBtn.setClickable(false);
                if (email.getText().toString().isEmpty() || pass.getText().toString().isEmpty()) {
                    enterBtn.setClickable(true);
                    registrationBtn.setClickable(true);
                    Toast.makeText(LoginActivity.this, "Пароль или логин пусты", Toast.LENGTH_SHORT).show();
                }
                else {
                    mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                            .addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                enterBtn.setClickable(true);
                                                registrationBtn.setClickable(true);
                                                if (mAuth.getCurrentUser().isEmailVerified()) {
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    enterBtn.setClickable(true);
                                                    registrationBtn.setClickable(true);
                                                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                                    SharedPreferences.Editor edit = settings.edit();
                                                    if (checkAutoLogin.isChecked()) {
                                                        edit.remove("autoLogin");
                                                        edit.putBoolean("autoLogin", true);
                                                        edit.putString("login", email.getText().toString());
                                                        edit.putString("password", pass.getText().toString());
                                                        edit.commit();
                                                    } else {
                                                        edit.remove("autoLogin");
                                                        edit.putBoolean("autoLogin", false);
                                                        edit.remove("login");
                                                        edit.remove("password");
                                                        edit.commit();
                                                    }
                                                } else {
                                                    enterBtn.setClickable(true);
                                                    registrationBtn.setClickable(true);
                                                    Toast.makeText(LoginActivity.this, "Email не подтвержден", Toast.LENGTH_SHORT).show();
                                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                                Toast.makeText(LoginActivity.this, "Письмо для подтверждения email было отправлено", Toast.LENGTH_SHORT).show();
                                                            else {
                                                                Toast.makeText(LoginActivity.this, "Не могу отослать письмо на ваш email снова. Ваш аккаунт удален", Toast.LENGTH_SHORT).show();
                                                                mAuth.getCurrentUser().delete();
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                                                enterBtn.setClickable(true);
                                                registrationBtn.setClickable(true);
                                            }
                                        }
                                    }
                            );
                }
                break;
            case R.id.forgotPasswordBtn:
                onCreateForgotPasswordDialog().show();
        }
    }

    public Dialog onCreateForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogLayout = inflater.inflate(R.layout.forgot_password_dialog, (ViewGroup)findViewById(R.id.linearLayoutDialog));
        builder.setView(dialogLayout)
                // Add action buttons
                .setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText email = (EditText)dialogLayout.findViewById(R.id.emailDialog);
                        String emailString = email.getText().toString();
                        if(!emailString.isEmpty()) {
                            mAuth.sendPasswordResetEmail(emailString).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        Toast.makeText(LoginActivity.this, "Письмо отправлено на вашу почту", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(LoginActivity.this, "Ошибка: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("Отмена", null);
        return builder.create();
    }
}
