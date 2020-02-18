package com.example.nics.testtracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
//import android.widget.TextView;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private Button start,end;
    private TextView textView;
    private int count;
    boolean check;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        textView=(TextView)findViewById(R.id.text);
        start=(Button)findViewById(R.id.start);
        start.setOnClickListener(this);
        end=(Button)findViewById(R.id.end);
        end.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       switch(v.getId()){
           case R.id.start:
               check=true;
               new Thread() {
                   @Override
                   public void run() {
                       while (check) {
                           try {
                               Thread.sleep(1000);
                               count++;
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                           Log.v("sushil",Thread.currentThread().getId()+"");
                           textView.setText(" " + count);
                       }
                   }
               }.start();
               break;
           case R.id.end:
               check=false;
               break;

       }
    }
}
