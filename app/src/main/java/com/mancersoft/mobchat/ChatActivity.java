package com.mancersoft.mobchat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private List<UserMessage> mMessageList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ListView chatList = findViewById(R.id.listViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);

        load(ChannelListActivity.currentOpenChannel.getUrl());
        List<String> messages = new ArrayList<>(mMessageList.size());
        for (UserMessage msg : mMessageList) {
            messages.add(msg.getSender().getUserId() + ": " + msg.getMessage());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatList.setAdapter(adapter);
        SendBird.addChannelHandler(UUID.randomUUID().toString(), new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (ChannelListActivity.currentOpenChannel.getUrl().equals(baseChannel.getUrl()) && baseMessage instanceof UserMessage) {
                    UserMessage userMessage = (UserMessage)baseMessage;
                    mMessageList.add(userMessage);
                    adapter.add(userMessage.getSender().getUserId() + ": " + userMessage.getMessage());
                }
            }
        });
    }

    @Override
    protected void onStop() {
        save();
        super.onStop();
    }

    public void buttonSendOnClick(View v) {
        ChannelListActivity.currentOpenChannel.sendUserMessage(editTextMessage.getText().toString(), null, "776", new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(getBaseContext(), getString(R.string.send_message_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }

                mMessageList.add(userMessage);
                editTextMessage.getText().clear();
                adapter.add(userMessage.getSender().getUserId() + ": " + userMessage.getMessage());
            }
        });
    }

    private void save() {
        try {
            StringBuilder sb = new StringBuilder();
            if (ChannelListActivity.currentOpenChannel != null) {
                // Convert current channel instance into a string.
                sb.append(Base64.encodeToString(ChannelListActivity.currentOpenChannel.serialize(), Base64.NO_WRAP));

                // Converts up to 100 messages within the channel into a string.
                BaseMessage message = null;
                for (int i = 0; i < Math.min(mMessageList.size(), 100); i++) {
                    message = mMessageList.get(i);
                    sb.append("\n");
                    sb.append(Base64.encodeToString(message.serialize(), Base64.NO_WRAP));
                }

                String data = sb.toString();
                String md5 = generateMD5(data);

                // Create a file within the app's cache directory.
                File appDir = new File(this.getCacheDir(), SendBird.getApplicationId());
                appDir.mkdirs();

                // Create a data file and a hash file within the directory.
                File dataFile = new File(appDir, generateMD5(SendBird.getCurrentUser().getUserId() + ChannelListActivity.currentOpenChannel.getUrl()) + ".data");
                File hashFile = new File(appDir, generateMD5(SendBird.getCurrentUser().getUserId() + ChannelListActivity.currentOpenChannel.getUrl()) + ".hash");

                try {
                    String content = loadFromFile(hashFile);
                    // If data has not been changed, do not save.
                    if(md5.equals(content)) {
                        return;
                    }
                } catch(IOException e) {
                    // File not found. Save the data.
                }

                saveToFile(dataFile, data);
                saveToFile(hashFile, md5);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void load(String channelUrl) {
        try {
            File appDir = new File(this.getCacheDir(), SendBird.getApplicationId());
            appDir.mkdirs();

            File dataFile = new File(appDir, generateMD5(SendBird.getCurrentUser().getUserId() + channelUrl) + ".data");

            String content = loadFromFile(dataFile);
            String [] dataArray = content.split("\n");

            // Load the channel instance.
            ChannelListActivity.currentOpenChannel = (OpenChannel) GroupChannel.buildFromSerializedData(Base64.decode(dataArray[0], Base64.NO_WRAP));

            // Add the loaded messages to the currently displayed message list.
            for(int i = 1; i < dataArray.length; i++) {
                mMessageList.add((UserMessage)UserMessage.buildFromSerializedData(Base64.decode(dataArray[i], Base64.NO_WRAP)));
            }
        } catch(Exception e) {
            // Nothing to load.
        }
    }

    private static String generateMD5(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(data.getBytes());
        byte messageDigest[] = digest.digest();

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

        return hexString.toString();
    }

    private static void saveToFile(File file, String data) throws IOException {
        File tempFile = File.createTempFile("sendbird", "temp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(data.getBytes());
        fos.close();

        if(!tempFile.renameTo(file)) {
            throw new IOException("Error to rename file to " + file.getAbsolutePath());
        }
    }

    private static String loadFromFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }
}
