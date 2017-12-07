package com.example.administrator.praiseview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.init(this);
        setContentView(R.layout.activity_main);
        PraiseView praiseView = (PraiseView) findViewById(R.id.thumbUpView);
        praiseView.bindData(new PraiseView.IPraiseListener() {
            @Override
            public void like(boolean isPraise, int praiseCount) {
                Toast.makeText(MainActivity.this, isPraise ? "点赞数加1" : "点赞数减1", Toast.LENGTH_SHORT).show();
            }
        }, false, 99);
    }

}
