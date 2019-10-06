package com.example.iskchat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iskchat.Adapter.Chat;
import com.example.iskchat.Adapter.User;
import com.example.iskchat.Notification.MyFirebaseMessagingService;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

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
    private ImageButton mSendButton,input;
    private Intent intent;

    private DatabaseReference reference;
    private FirebaseUser fuser;

MyFirebaseMessagingService notifcation;

    ChatAdapter chatAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;
    ValueEventListener seenListner;
    boolean reseiverState;
    String localTime,current_date,checker,myUrl;
    private StorageTask uploadTask;
    private Uri fileUri;
    StorageReference storageReference;
    String userid;
    public  static  String firstUser=" ";






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
        username=(TextView) findViewById(R.id.username) ;
        profile_image=findViewById(R.id.profile_image);
        state=(TextView)findViewById(R.id.state);
        input=(ImageButton)findViewById(R.id.input);

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent=getIntent();
         userid=intent.getStringExtra("userid");
        String receiverName=intent.getStringExtra("username");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("uploads");



//Send Message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mInputText.getText().toString();
                if(!msg.equals("")){
                    ///Date
                    SimpleDateFormat  currentTime= new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat currentDate =new SimpleDateFormat("dd MMM, yyyy HH:mm",Locale.ENGLISH);
                     current_date=currentDate.format(calendar.getTime());
                     localTime=currentTime.format(calendar.getTime());


                    sendMessage(fuser.getUid(),userid,msg,current_date);
                    sendNotification(userid,MainAdapter.myName);
                }
                else {
                    Toast.makeText(MainChatActivity.this,"You can't send empty message",Toast.LENGTH_SHORT).show();
                }
                mInputText.setText("");
            }
        });
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new  CharSequence[]
                        {
                                "Images",
                                "PDF"

                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainChatActivity.this);
                builder.setTitle("Select Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                    }
                });
                builder.show();


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
                    String state_date = user.getStatus();

                    String  day= state_date.split(" ")[0];
                    String  month= state_date.split(" ")[1];
                    String  year= state_date.split(" ")[2];
                    String  date= day+" "+month+" "+year;
                    String userDate=MainAdapter.current_date;

                    String thisDay = userDate.split(" ")[0];
                    String  thisMonth= userDate.split(" ")[1];
                    String  thisYear= userDate.split(" ")[2];
                    String  thisDate= thisDay+" "+thisMonth+" "+thisYear;

                    int yesterday=Integer.parseInt(day)-1;

                    String time=state_date.split(" ")[3];
                        if (date.equals(thisDate))
                        {
                            state.setText("last seen at "+time);

                        }
                        else if (Integer.parseInt(thisDay)==(Integer.parseInt(day)+1))
                            state.setText("last seen yesterday at "+time);

                        else
                        state.setText("last seen at "+date);
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
    private String getFileExteninion (Uri uri){
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage(final String sender, final String reciver, final String msgTime){
        final ProgressDialog pd =new ProgressDialog(getBaseContext());
        pd.setMessage("Uploading");

        if (fileUri != null){
            pd.show();
            final StorageReference fileRefrence = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExteninion(fileUri));

            uploadTask = fileRefrence.putFile(fileUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRefrence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("sender",sender);
                        hashMap.put("reciver",reciver);
                        hashMap.put("message",mUri);
                        hashMap.put("seen",false);
                        hashMap.put("time",msgTime);


                        reference.child("Chats").push().setValue(hashMap);
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
                        final     DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                                .child(userid)
                                .child(fuser.getUid());
                        chatRef1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    chatRef1.child("id").setValue(fuser.getUid());


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        pd.dismiss();
                    }
                    else {
                        Toast.makeText(getBaseContext(),"faild",Toast.LENGTH_SHORT).show();
                        pd.dismiss();}

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {Toast.makeText(getBaseContext(),"No image selected",Toast.LENGTH_SHORT).show();}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==438 && resultCode == RESULT_OK && data !=null &&data.getData() !=null){
            fileUri = data.getData();
            if (! checker.equals("image")){

            }else if (checker.equals("image")){
                //uploadImage(fuser.getUid(),userid,localTime);

            }
else Toast.makeText(this,"Nothing Selected",Toast.LENGTH_SHORT).show();
        }
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
        final     DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(userid)
                .child(fuser.getUid());
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(fuser.getUid());


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        firstUser=reciver;
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
    }
}