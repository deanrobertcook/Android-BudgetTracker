package org.theronin.expensetracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PlayGroundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb__toolbar);
        toolbar.setTitle("PlayGround Activity");
        setSupportActionBar(toolbar);

//        AutoResizeEditText view = (AutoResizeEditText) findViewById(R.id.test_view);
//        view.setCurrency(new Currency("AUD", "$", "Australian Dollar"));
    }
}
