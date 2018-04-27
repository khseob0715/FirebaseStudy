package com.example.vclab.howtalk2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout splashactivity_linearlayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // FullScreen 적용  // 상태바가 없어짐.


        splashactivity_linearlayout = (LinearLayout) findViewById(R.id.splashactivity_linearlayout);


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG) // 빌드를 여러번 하기 위함
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config);  // 적용될 매개변수들을 가지고 있는 값들


        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetch(0)  // 시간
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {

                        }
                        displayMessage();
                    }
                });
    }
    void displayMessage(){
        // 매개변수 웹에서 받아오는 변수.
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");

        splashactivity_linearlayout.setBackgroundColor(Color.parseColor(splash_background));

        if(caps){  // caps가 true 일 경우 서버 점검 등의 이유로 앱의 실행을 중지 .
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.create().show();

        }else{
            // 기존에는 Handler를 이용해 2초 정도 지난 뒤에 전환되게 했다면,
            // 이제는 서버와 연결되어 있는 상태이므로 서버와 연결이 완료되어 값을 읽어 왔을 경우 splash 이미지가 넘어가는 형태이다.

            startActivity(new Intent(this, LoginActivity.class));
        }
    }

}
