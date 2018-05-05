package com.example.vclab.howtalk2.chat;

import android.icu.text.SimpleDateFormat;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMessageActivity extends AppCompatActivity {

    Map<String, UserModel> users = new HashMap<>(); // User의 정보를 담는 그릇.

    String destiantionRoom;
    String uid;
    EditText editText;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private RecyclerView recyclerView;

    List<ChatModel.Comment> comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        destiantionRoom = getIntent().getStringExtra("destinationRoom");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = (EditText)findViewById(R.id.groupMessageActivity_editText);

        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {    // 여러 데이터가 배열로 넘어온다.
                // 유저에 대한 정보를 받아오기
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    // 받아온 키 값을. Hash Map에 저장하고, Value 값을 받아와서 저장 하는 것.
                    users.put(item.getKey(), item.getValue(UserModel.class));
                }

                init();
                recyclerView = (RecyclerView)findViewById(R.id.groupMessageActivity_recyclerview);
                // 어댑터와 recyclerview를 연결
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    void init(){
        Button button = (Button)findViewById(R.id.groupMessageActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destiantionRoom)
                        .child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        editText.setText("");
                    }
                });
            }
        });
    }
    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter() {
            getMessageList();
        }

        // message list를 전달받는 것.
        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destiantionRoom).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();  // 서버에서는 모든 데이터를 보내기 때문에 이를 쓰지 않으면 이전에 보낸 값이 계속 쌓인다.
                    // message를 읽어오면 message를 담는 부분이 comments가 된다.
                    Map<String, Object> readUsersMap = new HashMap<>();

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);

                        comment_modify.readUsers.put(uid,true);  // 읽었다는 태그를 달아줌.
                        readUsersMap.put(key, comment_modify);
                        comments.add(comment_origin);
                    }

                    if(comments.get(comments.size()-1).readUsers.containsKey(uid)){
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destiantionRoom).child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged(); // 데이터 갱신. // 메시지 갱신
                                recyclerView.scrollToPosition(comments.size() - 1); // 맨 마지막 이동.
                            }
                        });
                    }else{
                        notifyDataSetChanged(); // 데이터 갱신. // 메시지 갱신

                        recyclerView.scrollToPosition(comments.size() - 1); // 맨 마지막 이동.
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
           // view를 그려주는것.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new GroupMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            GroupMessageViewHolder messageViewHolder = ((GroupMessageViewHolder)holder);

            if(comments.get(position).uid.equals(uid)){ // 내가 쓴 말풍선과 상대방이 쓴 말풍선을 나눈 것.
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);  // 내꺼 프로필은 감추기
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
           //     setReadCount(position, messageViewHolder.textView_readCounter_left);

            }else{ // 이건 상대방이 보낸 메시지
                Glide.with(holder.itemView.getContext())
                        .load(users.get(comments.get(position).uid).profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView);
                messageViewHolder.textView_name.setText(users.get(comments.get(position).uid).userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);

               // setReadCount(position, messageViewHolder.textView_readCounter_right);
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
            // comments의 개수만큼 리스트를 만드는 것임.
        }

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {

            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;


            public GroupMessageViewHolder(View view) {
                super(view);

                textView_message = (TextView)view.findViewById(R.id.messageitem_textview);
                textView_name = (TextView)view.findViewById(R.id.message_textview_name);
                imageView = (ImageView)view.findViewById(R.id.messageActivity_imageView_profile);
                linearLayout_destination = (LinearLayout)view.findViewById(R.id.messageActivity_linearlayout_destination);
                linearLayout_main = (LinearLayout)view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView)view.findViewById(R.id.messageItem_textview_timestamp);
                textView_readCounter_left = (TextView)view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = (TextView)view.findViewById(R.id.messageItem_textview_readCounter_right);
            }
        }
    }

}
