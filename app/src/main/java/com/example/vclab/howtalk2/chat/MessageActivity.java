package com.example.vclab.howtalk2.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.vclab.howtalk2.R;
import com.example.vclab.howtalk2.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        destinationUid = getIntent().getStringExtra("destinationUid");

        button = (Button)findViewById(R.id.messageActivity_button);
        editText = (EditText)findViewById(R.id.messageActivity_editText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 대화방이 만들어지도록
                ChatModel chatModel = new ChatModel();
                chatModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.destinationUid = destinationUid;

                // push는 primary key와 비슷한 기능으로 채팅방의 이름을 임의로 설정해줄 것임
                // push를 쓰지 않는다면 채팅방의 이름이 없음.
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);

            }
        });
    }
}
