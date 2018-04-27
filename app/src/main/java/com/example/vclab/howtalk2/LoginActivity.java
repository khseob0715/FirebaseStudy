package com.example.vclab.howtalk2;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private Button signup;

    private FirebaseRemoteConfig mFirebaseRemoteConfig; // 원격으로 테마를 적용 받기 위해서는 필요함.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        getWindow().setStatusBarColor(Color.parseColor(splash_background)); // 위에 상단 바

        // 버튼 원격 테마 적용
        login = (Button)findViewById(R.id.loginActivity_button_login);
        signup = (Button)findViewById(R.id.loginActivity_button_signUP);

        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }
}
