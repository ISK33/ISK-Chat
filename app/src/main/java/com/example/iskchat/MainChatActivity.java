
package com.example.iskchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainChatActivity extends AppCompatActivity {

    // TODO: Add member variables here:
    private String mDisplayName="anyone";
    private ListView mChatListView;
    private EditText mInputText;
    private TextView user_tool;
    private ImageButton mSendButton;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private ChatListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        // TODO: Set up the display name and get the Firebase reference
       setupDisplayName();
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        // Link the Views in the layout to the Java code
        mInputText = (EditText) findViewById(R.id.messageInput);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        mChatListView = (ListView) findViewById(R.id.chat_list_view);
        user_tool=(TextView) findViewById(R.id.tool_user) ;

        // TODO: Send the message when the "enter" button is pressed
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                sendMessage();
                return true;
            }
        });

        // TODO: Add an OnClickListener to the sendButton to send a message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    // TODO: Retrieve the display name from the Shared Preferences
    private void setupDisplayName(){
        FirebaseUser firebaseUser =mAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null) {
            String userid=firebaseUser.getUid();
             reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
             reference.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     User user=dataSnapshot.getValue(User.class);
                     mDisplayName=user.getUsername();
                     user_tool.setText(mDisplayName);


                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });
            //  SharedPreferences prefs = getSharedPreferences(LoginActivity.CHAT_PREFS, MODE_PRIVATE);
         //   mDisplayName = firebaseUser.getDisplayName();
            // mDisplayName = prefs.getString(LoginActivity.DISPLAY_NAME_KEY, null);
        }
        if (mDisplayName == null) mDisplayName = "AnnyOne";
    }



    private void sendMessage() {

       // Log.d("FlashChat", "I sent something");
        // TODO: Grab the text the user typed in and push the message to Firebase
        String input = mInputText.getText().toString();
        if (!input.equals("")) {
           InstantMessage chat = new InstantMessage(input, mDisplayName);
            mDatabaseReference.child("New").push().setValue(chat);
            mInputText.setText("");
        }

    }



    // TODO: Override the onStart() lifecycle method. Setup the adapter here.
    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new ChatListAdapter(this,mDatabaseReference,mDisplayName);
        mChatListView.setAdapter(mAdapter);
    }


    @Override
    public void onStop() {
        super.onStop();

        // TODO: Remove the Firebase event listener on the adapter.
       mAdapter.cleanup();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
        }
        return true;
    }
}
