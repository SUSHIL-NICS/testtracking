package com.example.nics.testtracking;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Main3Activity extends AppCompatActivity {
    TextView mTVCounter;
    Thread mThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        // Getting Reference to tv_counter of the layout activity_main
        mTVCounter = (TextView) findViewById(R.id.text);

        mTVCounter.setText("*** Countdown Starts ***");

        //Countdown starts
          countDown();
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            // Retrieving Bundle object from the Message object
            Bundle data = msg.getData();

            // Retrieving count from the Bundle object
            int count = data.getInt("count");

            if(count==0){ // Countdown finished
                mTVCounter.setText("*** DONE ***");
            }else{
                // Setting Current count in TextView tv_counter
                mTVCounter.setText(Integer.toString(count));
                Toast.makeText(Main3Activity.this,""+count,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void countDown(){
        // Creating a new Thread
        mThread = new Thread(){
            @Override
            public void run() {
                super.run();
                for(int i=10; i>=0; i--){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    Bundle data = new Bundle();
                    data.putInt("count", i);
                    message.setData(data);
                    mHandler.sendMessage(message);
                    //mHandler.postDelayed(r)
                }
            }
        };
        // Starting the thread mThread
        mThread.start();
    }
}
