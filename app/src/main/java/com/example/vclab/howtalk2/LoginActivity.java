package com.example.vclab.howtalk2;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;

    private FirebaseRemoteConfig firebaseRemoteConfig; // 원격으로 테마를 적용 받기 위해서는 필요함.
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStaceListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();  // auth를 받아오는 이벤트!!
        firebaseAuth.signOut();

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color));
        getWindow().setStatusBarColor(Color.parseColor(splash_background)); // 위에 상단 바

        // 버튼 원격 테마 적용
        login = (Button)findViewById(R.id.loginActivity_button_login);
        signup = (Button)findViewById(R.id.loginActivity_button_signUP);

        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));


        id = (EditText)findViewById(R.id.loginActivity_edittext_id);
        password = (EditText)findViewById(R.id.loginActivity_edittext_password);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEvent();
            }
        });

        // 로그인 인터페이스 리스너
        authStaceListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // 로그인이 되었거나, 로그아웃이 되어 상태가 변경되었을떄
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    // 로그인
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // context를 못 넘겨주는 오류를 확인해야된다.
                    startActivity(intent);
                    finish();
                }else{
                    // 로그아웃
                }
            }
        };
    }

    void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() { // 로그인이 완료되었다고만 알려주는 것.
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            // 로그인이 실패했을때
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStaceListener);
        // 시작했을때 리스너를 실행.
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStaceListener);
        // 멈추었을때 리스너를 실행 .
    }
}
