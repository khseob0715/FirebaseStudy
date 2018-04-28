package com.example.vclab.howtalk2.chat;

import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.vclab.howtalk2.R;
import com.example.vclab.howtalk2.model.ChatModel;
import com.example.vclab.howtalk2.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();            // 채팅을 요구하는 아이디
        destinationUid = getIntent().getStringExtra("destinationUid");      // 채팅을 당하는 아이디

        button = (Button)findViewById(R.id.messageActivity_button);
        editText = (EditText)findViewById(R.id.messageActivity_editText);
        recyclerView = (RecyclerView)findViewById(R.id.messageActivity_recyclerView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 대화방이 만들어지도록
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);

                if(chatRoomUid == null){
                    button.setEnabled(false);  // 방 생성이 완료 되었음을 기다리기 위해서 사용함. 자잘한 버그 해결 용
                    // push는 primary key와 비슷한 기능으로 채팅방의 이름을 임의로 설정해줄 것임
                    // push를 쓰지 않는다면 채팅방의 이름이 없음.
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();  // call-back을 이용하여 방의 중복 생성을 방지하는 것.

                        }
                    });
                }else{
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP;
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            editText.setText("");
                        }
                    });
                }


            }
        });
        checkChatRoom();
    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class); // chat room에 id가 있는지 없는지를 받아온 것.
                    if(chatModel.users.containsKey(destinationUid)){
                        chatRoomUid = item.getKey(); // 이건 방 아이디
                        button.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // 중복 체크
    }
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<ChatModel.Comment> comments;
        UserModel userModel;
        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            //유저 정보를 가져오는 것.
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        // message list를 전달받는 것.
        void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();  // 서버에서는 모든 데이터를 보내기 때문에 이를 쓰지 않으면 이전에 보낸 값이 계속 쌓인다.

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    notifyDataSetChanged(); // 데이터 갱신. // 메시지 갱신

                    recyclerView.scrollToPosition(comments.size() - 1); // 맨 마지막 이동.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // xml 레이아웃 추가
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {// 데이터 바인딩
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            if(comments.get(position).uid.equals(uid)){ // 내가 쓴 말풍선과 상대방이 쓴 말풍선을 나눈 것.
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);  // 내꺼 프로필은 감추기
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
            }else{ // 이건 상대방이 보낸 메시지
                Glide.with(holder.itemView.getContext())
                        .load(userModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView);
                messageViewHolder.textView_name.setText(userModel.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
            }
            long unixTime = (long)comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(android.icu.util.TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;


            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView)view.findViewById(R.id.messageitem_textview);
                textView_name = (TextView)view.findViewById(R.id.message_textview_name);
                imageView = (ImageView)view.findViewById(R.id.messageActivity_imageView_profile);
                linearLayout_destination = (LinearLayout)view.findViewById(R.id.messageActivity_linearlayout_destination);
                linearLayout_main = (LinearLayout)view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView)view.findViewById(R.id.messageItem_textview_timestamp);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}