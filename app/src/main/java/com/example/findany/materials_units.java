package com.example.findany;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class materials_units extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String branch;
    LinearLayout linearLayout;
    String year;
    String subject;
    androidx.appcompat.widget.Toolbar toolbar;
    String toolbartitle;
    String email;
    String folder;
    String specialization;
    String[] units = {"UNIT 1", "UNIT 2", "UNIT 3", "UNIT 4","UNIT 5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.materials_units);

        linearLayout=findViewById(R.id.linearlayout);

        Intent intent = getIntent();
        year = intent.getStringExtra("year");
        branch = intent.getStringExtra("branch");
        specialization = intent.getStringExtra("specialization");
        subject = intent.getStringExtra("subject");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbartitle=year.toUpperCase().replaceAll("\\s", "")+"/"+branch.toUpperCase()+"/"+specialization.replaceAll("\\s","")+"/"+subject.toUpperCase();
        getSupportActionBar().setTitle(toolbartitle);


        for (String unit : units) {
            Button button = new Button(this);
            button.setText(unit);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    email="neelammsr@gmail.com";

                    Intent intent=new Intent(materials_units.this, materials_display_upload.class);
                    intent.putExtra("year", year.toUpperCase());
                    intent.putExtra("branch", branch.toUpperCase());
                    intent.putExtra("specialization", specialization.toUpperCase());
                    intent.putExtra("subject", subject.toUpperCase());
                    intent.putExtra("unit",unit.toUpperCase());
                    startActivity(intent);
                }
            });
            linearLayout.addView(button);
        }

    }

}