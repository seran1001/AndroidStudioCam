package com.example.twobuttons;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity{
    private Button button_webview, button_camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_webview=findViewById(R.id.webview_button);
        button_camera=findViewById(R.id.camera_button);

        button_webview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openMainActivityCamera();
            }
        });

        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openMainActivityWeb();
            }
        });
    }
    public void openMainActivityWeb(){
        Intent intent = new Intent(this,MainActivityWeb.class);
        startActivity(intent);
    }

    public void openMainActivityCamera(){
        Intent intent = new Intent(this,MainActivityCamera.class);
        startActivity(intent);
    }


}