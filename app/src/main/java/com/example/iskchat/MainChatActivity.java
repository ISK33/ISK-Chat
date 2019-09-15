package com.example.iskchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iskchat.Adapter.Chat;
import com.example.iskchat.Adapter.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainChatActivity extends AppCompatActivity {

    // TODO: Add member variables here:
    private String mDisplayName="anyone";
    private ListView mChatListView;
    private EditText mInputText;
    private TextView username;
    private ImageView profile_image;
    private ImageButton mSendButton;
    private Intent intent;

    private DatabaseReference reference,mDatabaseReference;
    private FirebaseUser fuser;

    ChatAdapter chatAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        // TODO: Set up the display name and get the Firebase reference

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        // Link the Views in the layout to the Java code
        mInputText = (EditText) findViewById(R.id.messageInput);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        //mChatListView = (ListView) findViewById(R.id.chat_list_view);
        username=(TextView) findViewById(R.id.username) ;
        profile_image=findViewById(R.id.profile_image);

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        intent=getIntent();
        final  String userid=intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
//Send Message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mInputText.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                }
                else {
                    Toast.makeText(MainChatActivity.this,"You can't send empty message",Toast.LENGTH_SHORT).show();
                }
                mInputText.setText("");
            }
        });

        reference=FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                User user=dataSnapshot.getValue(User.class);
                mDisplayName=user.getUsername();

                username.setText(mDisplayName);
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.man);

                }
                else
                    Glide.with(MainChatActivity.this).load(user.getImageURL()).into(profile_image);
                display(fuser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void sendMessage(String sender,String reciver,String message) {

        // Log.d("FlashChat", "I sent something");
        // TODO: Grab the text the user typed in and push the message to Firebase
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("reciver",reciver);
        hashMap.put("message",message);

        reference.child("Chat").push().setValue(hashMap);
        final String userid=intent.getStringExtra("userid");
   final     DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);
chatRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()){
            chatRef.child("id").setValue(userid);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});

    }
    public void display(final String myid, final String userid, final String imageurl){
        mchat = new ArrayList<>();
        reference =FirebaseDatabase.getInstance().getReference("Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReciver().equals(myid)&&chat.getSender().equals(userid)||
                            chat.getReciver().equals(userid)&&chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }
                    chatAdapter = new ChatAdapter(MainChatActivity.this,mchat,imageurl);
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}