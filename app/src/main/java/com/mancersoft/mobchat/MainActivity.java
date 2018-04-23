package com.mancersoft.mobchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextLogin = findViewById(R.id.editTextLogin);
    }

    public void buttonLoginOnClick(View v) {
        SendBird.connect(editTextLogin.getText().toString(),"26eb6357a4602246c20c734e6bb8bdf2113c081b" , new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(getBaseContext(), getString(R.string.login_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getBaseContext(), ChannelListActivity.class);
                startActivity(intent);
            }
        });
    }
}
