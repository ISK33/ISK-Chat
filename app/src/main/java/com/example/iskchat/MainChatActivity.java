package com.example.iskchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.example.iskchat.Notification.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class MainChatActivity extends AppCompatActivity {

    // TODO: Add member variables here:
    private String mDisplayName="anyone";
    private ListView mChatListView;
    private EditText mInputText;
    private TextView username,state;
    private ImageView profile_image;
    private ImageButton mSendButton;
    private Intent intent;

    private DatabaseReference reference;
    private FirebaseUser fuser;

MyFirebaseMessagingService notifcation;

    ChatAdapter chatAdapter;
    List<Chat> mchat;
    Chat chat;
    RecyclerView recyclerView;
    String msgTime;
    String userid;
    ValueEventListener seenListner;
    boolean reseiverState;
    String localTime;
    String current_date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);


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
        state=(TextView)findViewById(R.id.state);

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent=getIntent();
         userid=intent.getStringExtra("userid");
        String receiverName=intent.getStringExtra("username");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
//Send Message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mInputText.getText().toString();
                if(!msg.equals("")){
                    ///Date
                    SimpleDateFormat  currentTime= new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat currentDate =new SimpleDateFormat("MMM dd, yyyy",Locale.ENGLISH);
                     current_date=currentDate.format(calendar.getTime());
                     localTime=currentTime.format(calendar.getTime());


                    sendMessage(fuser.getUid(),userid,msg,localTime);
                    sendNotification(userid,MainAdapter.myName);
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
                if(user.getStatus().equals("online")){
                    reseiverState=true;
                    state.setText("Online");
                }else {
                    reseiverState = false;
                    String state_date = user.getStatus().toString();

                      String  month= state_date.split(" ")[0];
                    String  day= state_date.split(" ")[1];
                    String  year= state_date.split(" ")[2];
                    String  date= month+" "+day+" "+year;
                    //int yesterday=Integer.parseInt(day)-1;

                    String time=state_date.split(" ")[3];
                        if (date.equals(MainAdapter.current_date))
                        {
                            state.setText("last seen at "+time);

                        }
                        else
                        state.setText("last seen at "+state_date);
                }
                mDisplayName=user.getUsername();

                username.setText(mDisplayName);
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.man);

                }
                else
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                display(fuser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);

    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListner = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReciver().equals(fuser.getUid())&& chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender,String reciver,String message,String msgTime) {

        // Log.d("FlashChat", "I sent something");
        // TODO: Grab the text the user typed in and push the message to Firebase
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("reciver",reciver);
        hashMap.put("message",message);
        hashMap.put("seen",false);
        hashMap.put("time",msgTime);

        reference.child("Chats").push().setValue(hashMap);
        final String userid=intent.getStringExtra("userid");
        final     DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
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
        reference =FirebaseDatabase.getInstance().getReference("Chats");
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

    public void status(String sttatus){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",sttatus);

        reference.updateChildren(hashMap);

    }
    private void current(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("current",userid);
        editor.apply();
    }
    private void sendNotification(final String Receiver, final String name)
    {
       if (!reseiverState) {
           AsyncTask.execute(new Runnable() {
               @Override
               public void run() {
                   int SDK_INT = android.os.Build.VERSION.SDK_INT;
                   if (SDK_INT > 8) {
                       StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                               .permitAll().build();
                       StrictMode.setThreadPolicy(policy);
                       String send_email;

                       //This is a Simple Logic to Send Notification different Device Programmatically....
                       send_email = Receiver;


                       try {
                           String jsonResponse;

                           URL url = new URL("https://onesignal.com/api/v1/notifications");
                           HttpURLConnection con = (HttpURLConnection) url.openConnection();
                           con.setUseCaches(false);
                           con.setDoOutput(true);
                           con.setDoInput(true);

                           con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                           con.setRequestProperty("Authorization", "Basic NDY2OWE1YWUtODZhZi00MzU5LTg4Y2EtMjEyN2MxNmE0ZDM4");
                           con.setRequestMethod("POST");

                           String strJsonBody = "{"
                                   + "\"app_id\": \"157b8954-d041-4afa-9b56-3824f23dfd11\","

                                   + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                   + "\"data\": {\"foo\": \"bar\"},"
                                   + "\"contents\": {\"en\": \"New Message from  " + name + "\"}"
                                   + "}";


                           System.out.println("strJsonBody:\n" + strJsonBody);

                           byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                           con.setFixedLengthStreamingMode(sendBytes.length);


                           OutputStream outputStream = con.getOutputStream();
                           outputStream.write(sendBytes);


                           int httpResponse = con.getResponseCode();
                           System.out.println("httpResponse: " + httpResponse);

                           if (httpResponse >= HttpURLConnection.HTTP_OK
                                   && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                               Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                               jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                               scanner.close();
                           } else {
                               Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                               jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                               scanner.close();
                           }
                           System.out.println("jsonResponse:\n" + jsonResponse);

                       } catch (Throwable t) {
                           t.printStackTrace();
                       }
                   }
               }
           });
       }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListner);
        status(current_date+" "+localTime);
    }
}