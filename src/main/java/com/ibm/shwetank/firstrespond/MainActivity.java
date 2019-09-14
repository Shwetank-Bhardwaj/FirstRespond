package com.ibm.shwetank.firstrespond;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationButton;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationCategory;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationOptions;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> tagsAvailable = new ArrayList<>();
    MFPPush push;
    Button mockData;
    Button rescued;

    //Handles the notification when it arrives
    MFPPushNotificationListener notificationListener = new MFPPushNotificationListener() {

        @Override
        public void onReceive (final MFPSimplePushNotification message){
            // Handle Push Notification
            //non UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject j = new JSONObject(message.getPayload());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeSDK();
        mockData = findViewById(R.id.filldata);
        rescued = findViewById(R.id.rescued);


        mockData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TextView name = findViewById(R.id.name);
                TextView address = findViewById(R.id.address);
                TextView phone = findViewById(R.id.phone);
                TextView rs = findViewById(R.id.rs);
                name.setText("Chinmay Karanjakar");
                address.setText("1701 E 8th St #140, Tempe, AZ, 85281");
                phone.setVisibility(View.VISIBLE);
                phone.setText("4808696921");
                rs.setText("10");
                rescued.setText("Rescue");
                rescued.setBackgroundColor(getResources().getColor(R.color.red));
                rescued.setVisibility(View.VISIBLE);
            }
        });
        rescued.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rescued.setText("Rescued");
                rescued.setBackgroundColor(getResources().getColor(R.color.green));
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(push != null) {
            push.listen(notificationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (push != null) {
            push.hold();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(push != null){
            push.registerDevice(new MFPPushResponseListener<String>() {

                @Override
                public void onSuccess(final String response) {
                    //handle successful device registration here
                    getTags();

                }

                @Override
                public void onFailure(MFPPushException ex) {
                    //handle failure in device registration here
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void getTags() {
        push.getTags(new MFPPushResponseListener<List<String>>(){

            @Override
            public void onSuccess(List<String> tags){
                tagsAvailable = tags;
                subscribeToTags();
            }

            @Override
            public void onFailure(MFPPushException ex){
                System.out.println("Error getting available tags.. " + ex.getMessage());
            }
        });
    }

    private void subscribeToTags() {
        push.subscribe("responders", new MFPPushResponseListener<String>() {

            @Override
            public void onSuccess(String arg) {
                System.out.println("Succesfully Subscribed to: "+ arg);
            }

            @Override
            public void onFailure(MFPPushException ex) {
                System.out.println("Error subscribing to Tag1.." + ex.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(push != null){
            push.unregister(new MFPPushResponseListener<String>() {

                @Override
                public void onSuccess(String s) {
                    // Handle success
                }

                @Override
                public void onFailure(MFPPushException e) {
                    // Handle Failure
                }
            });
        }
    }

    private void initializeSDK() {
        MFPPushNotificationOptions options = new MFPPushNotificationOptions();
        MFPPushNotificationButton viewButton = new MFPPushNotificationButton.Builder("View")
                .setIcon("extension_circle_icon")
                .setLabel("view")
                .build();
        List<MFPPushNotificationButton> buttonGroup =  new ArrayList<MFPPushNotificationButton>();
        ArrayList list = new ArrayList();
        list.add(viewButton);
        MFPPushNotificationCategory category = new MFPPushNotificationCategory.Builder("First_Button_Group").setButtons(buttonGroup).build();
        List<MFPPushNotificationCategory> categoryList =  new ArrayList<MFPPushNotificationCategory>();
        categoryList.add(category);
        options.setInteractiveNotificationCategories(categoryList);

        // Initialize the SDK
        BMSClient.getInstance().initialize(this, BMSClient.REGION_US_SOUTH);
        //Initialize client Push SDK
        push = MFPPush.getInstance();
        push.initialize(getApplicationContext(), "a6c30504-d212-4bff-8d20-ba51ee26d51c", "1d39e2bf-adc5-4f63-84a4-0196eab5b2f2", options);
    }


}
