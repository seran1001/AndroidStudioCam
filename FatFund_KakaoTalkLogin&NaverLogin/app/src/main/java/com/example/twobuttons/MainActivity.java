package com.example.twobuttons;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity{

    private Button button_webview, button_camera, button_login, button_ffw;
    String strNickname, strProfile;
    private static String OAUTH_CLIENT_ID = "00000000000000000";
    private static String OAUTH_CLIENT_SECRET = "0000000";
    private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인";
    private OAuthLoginButton btOAuthLoginButton;
    private OAuthLogin mOAuthLoginModule;
    Context mContext = MainActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getHashKey(mContext);
        button_webview=findViewById(R.id.webview_button);
        button_camera=findViewById(R.id.camera_button);
        button_login=findViewById(R.id.login_button);
        button_ffw=findViewById(R.id.ff_button);
        Button btnLogout = findViewById(R.id.kakaologout_button);
        btnLogout.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "정상적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Intent intent = new Intent(MainActivity.this, MainActivityLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        });



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

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openMainActivityLogin();
            }
        });

        button_ffw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openMainActivityffw();
            }
        });


        TextView tvNickname = findViewById(R.id.tvNickname);
        TextView tvProfile = findViewById(R.id.tvProfile);

        Intent intent = getIntent();
        strNickname = intent.getStringExtra("name");
        strProfile = intent.getStringExtra("profile");

        tvNickname.setText(strNickname);
        tvProfile.setText(strProfile);


    }
    public void openMainActivityWeb(){
        Intent intent = new Intent(this,MainActivityWeb.class);
        startActivity(intent);
    }

    public void openMainActivityCamera(){
        Intent intent = new Intent(this,MainActivityCamera.class);
        startActivity(intent);
    }

    public void openMainActivityLogin(){
        Intent intent = new Intent(this,MainActivityLogin.class);
        startActivity(intent);
    }
    public void openMainActivityffw(){
        Intent intent = new Intent(this,MainActivityFfw.class);
        startActivity(intent);
    }
    @Nullable

    public static String getHashKey(Context context) {

        final String TAG = "KeyHash";

        String keyHash = null;

        try {

            PackageInfo info =

                    context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);



            for (Signature signature : info.signatures) {

                MessageDigest md;

                md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());

                keyHash = new String(Base64.encode(md.digest(), 0));

                Log.d("findhash", keyHash);

            }

        } catch (Exception e) {

            Log.e("name not found", e.toString());

        }



        if (keyHash != null) {

            return keyHash;

        } else {

            return null;

        }

    }


}