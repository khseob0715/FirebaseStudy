package com.example.vclab.howtalk2.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.vclab.howtalk2.R;
import com.example.vclab.howtalk2.chat.MessageActivity;
import com.example.vclab.howtalk2.model.ChatModel;
import com.example.vclab.howtalk2.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    ChatModel chatModel = new ChatModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.selectFriendActivity_recyclerview);
        recyclerView.setAdapter(new SelectFriendPeopleFragmentRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button button = (Button)findViewById(R.id.selectFriendActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.users.put(myUid, true);

                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
            }
        });
    }

    class SelectFriendPeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<UserModel> userModels;
        public SelectFriendPeopleFragmentRecyclerViewAdapter() { // DB에 접근할 생성자
            userModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // 처음 넘어오는 데이터  // ArrayList로 넘어옴.
                    userModels.clear();
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()) {

                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if(userModel.uid.equals(myUid)){
                            continue; // 내 uid면 넘어감.
                        }
                        userModels.add(userModel);
                    }
                    notifyDataSetChanged();  // 데이터가 쌓인 후 새로고침과 비슷한 기능.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop()).
                    into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid",userModels.get(position).uid);  // 채팅 상대 uid 전달
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent,activityOptions.toBundle());
                    // 프로필 버튼 눌렀을때 액티비티 전환
                }
            });

            if(userModels.get(position).comment != null) {
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);
            }

            ((CustomViewHolder) holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    // 체크 된 상태
                    if(b){
                        chatModel.users.put(userModels.get(position).uid,true);


                    }else{ // 체크 취소 상태
                        chatModel.users.remove(userModels.get(position));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView)view.findViewById(R.id.frienditem_imageView);
                textView = (TextView)view.findViewById(R.id.frienditem_textView);
                textView_comment = (TextView)view.findViewById(R.id.frienditem_textView_commnet);
                checkBox = (CheckBox)view.findViewById(R.id.frienditem_checkbox);
            }
        }
    }
}
