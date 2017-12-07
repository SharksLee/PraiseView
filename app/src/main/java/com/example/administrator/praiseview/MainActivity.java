package com.example.administrator.praiseview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.init(this);
        setContentView(R.layout.activity_main);
        final PraiseView praiseView = (PraiseView) findViewById(R.id.thumbUpView);
        final EditText editText = (EditText) findViewById(R.id.et_count);
        Button button = (Button) findViewById(R.id.bt_confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    return;
                }
                int count = Integer.parseInt(editText.getText().toString());
                praiseView.bindData(new PraiseView.IPraiseListener() {
                    @Override
                    public void like(boolean isPraise, int praiseCount) {
                        Toast.makeText(MainActivity.this, isPraise ? "点赞数加1" : "点赞数减1", Toast.LENGTH_SHORT).show();
                    }
                }, false,count);
            }
        });

    }

}
