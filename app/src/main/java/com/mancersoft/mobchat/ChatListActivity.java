package com.mancersoft.mobchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChatListActivity extends AppCompatActivity {

    String[] names = { "Stas", "Max", "Sasha", "Anya", "Danya", "Vlad", "Masha", "???" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ListView chatList = findViewById(R.id.listViewChatList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        chatList.setAdapter(adapter);
    }
}
