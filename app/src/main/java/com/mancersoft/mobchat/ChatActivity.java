package com.mancersoft.mobchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ListView chatList = findViewById(R.id.listViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        chatList.setAdapter(adapter);
        SendBird.addChannelHandler(UUID.randomUUID().toString(), new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (ChannelListActivity.currentOpenChannel.getUrl().equals(baseChannel.getUrl()) && baseMessage instanceof UserMessage) {
                    UserMessage userMessage = (UserMessage)baseMessage;
                    adapter.add(userMessage.getSender().getUserId() + ": " + userMessage.getMessage());
                }
            }
        });
    }

    public void buttonSendOnClick(View v) {
        ChannelListActivity.currentOpenChannel.sendUserMessage(editTextMessage.getText().toString(), null, "776", new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(getBaseContext(), getString(R.string.send_message_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }

                editTextMessage.getText().clear();
                adapter.add(userMessage.getSender().getUserId() + ": " + userMessage.getMessage());
            }
        });
    }
}
