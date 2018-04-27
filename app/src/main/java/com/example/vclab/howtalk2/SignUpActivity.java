package com.example.vclab.howtalk2;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.vclab.howtalk2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class SignUpActivity extends AppCompatActivity {

    private EditText email;
    private EditText name;
    private EditText password;

    private Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = (EditText)findViewById(R.id.signupActivity_edittext_email);
        name = (EditText)findViewById(R.id.signupActivity_edittext_name);
        password = (EditText)findViewById(R.id.signupActivity_edittext_password);
        signup = (Button)findViewById(R.id.signupactivity_button_signup);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        getWindow().setStatusBarColor(Color.parseColor(splash_background)); // 위에 상단 바

        signup.setBackgroundColor(Color.parseColor(splash_background));
        // 회원가입은 데이터베이스에 접근해야되므로, Authentication을 추가해주어야 함.
        // 이는 로그인 관련 라이브러리

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null){
                    return;
                }
                // 아래의 명령으로 회원가입 정보를 넣을 수 있음.
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // uid를 받아와 데이터베이스에 저장.
                                //  String uid = task.getResult().getUser().getUid();
                                UserModel userModel = new UserModel();
                                userModel.userName = name.getText().toString();


                                String uid = task.getResult().getUser().getUid();

                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel);

                            }
                        });
            }

        });

    }
}
