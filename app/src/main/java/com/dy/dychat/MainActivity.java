package com.dy.dychat;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    int SIGN_IN_REQUEST_CODE = 1;

    private FirebaseListAdapter<ChatMessage> adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            //Start sign in/sign up activity

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        }
        else{
            //Welcome User Toast
            Toast.makeText(this, "Welcome" + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();

            displayChatMessages();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the  database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance().
                                        getCurrentUser().
                                        getDisplayName()));

                //Clear the input
                input.setText("");
            }
        });
    }


    private void displayChatMessages(){

        ListView messageList = (ListView) findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class ,R.layout.message ,
                FirebaseDatabase.getInstance().getReference() ) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
            //Get reference to the view of message.xml
                TextView messageText = (TextView)findViewById(R.id.message_text);
                TextView messageUser = (TextView)findViewById(R.id.message_user);
                TextView messageTime = (TextView)findViewById(R.id.message_time);

                //set text on layout
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                //Formatting Date before showing
                messageTime.setText("Undefined");
            }
        };

        messageList.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Successfully signed in. Welcome!", Toast.LENGTH_LONG).show();
                displayChatMessages();
            }
            else{
                Toast.makeText(this, "We couldn't sign you in. Try again later", Toast.LENGTH_LONG).show();

                //Close App
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.menu_sign_out){

            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "YOuhave been Signed Out", Toast.LENGTH_LONG).show();

                        //Close activity
                         finish();
                        }
                    });
        }

        return true;
    }



}
