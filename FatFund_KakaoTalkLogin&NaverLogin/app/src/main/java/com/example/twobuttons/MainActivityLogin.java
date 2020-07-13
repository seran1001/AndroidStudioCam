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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.security.MessageDigest;

public class MainActivityLogin  extends AppCompatActivity {
        private SessionCallback sessionCallback;


        private Context mContext;
        private Button login;
        private Button button_naver;
        LoginButton logn; //xml에서 login button 가져옴
        LoginButton com_kakao_login;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_login);
                Log.d("logintag", "You reached loginactivity");
                sessionCallback = new SessionCallback();
                Session.getCurrentSession().addCallback(sessionCallback); //call back present session
                Session.getCurrentSession().checkAndImplicitOpen(); //auto login
                button_naver=findViewById(R.id.naver_button);


                button_naver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view){
                                openOAuthSampleActivity();
                        }
                });

                /**카카오톡 로그아웃 요청**/
                //한번 로그인이 성공하면 세션 정보가 남아있어서 로그인창이 뜨지 않고 바로 onSuccess()메서드를 호출합니다.
                //테스트 하시기 편하라고 매번 로그아웃 요청을 수행하도록 코드를 넣었습니다 ^^
/*
                UserManagement.requestLogout(new LogoutResponseCallback() {
                        @Override
                        public void onCompleteLogout() {
                                //로그아웃 성공 후 하고싶은 내용 코딩 ~
                        }
                });

                sessionCallback = new SessionCallback();
                Session.getCurrentSession().addCallback(sessionCallback);
*/


                getAppKeyHash();
                /*
                try {
                        PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                        for (Signature signature : info.signatures) {
                                MessageDigest md = MessageDigest.getInstance("SHA");
                                md.update(signature.toByteArray());
                                Log.e("MY KEY HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                        }
                } catch (PackageManager.NameNotFoundException e) {


                } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                }

                 */

        }

        public void openOAuthSampleActivity(){
                Intent intent = new Intent(this,OAuthSampleActivity.class);
                startActivity(intent);
        }


        private void getAppKeyHash() {
                try {
                        PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                        for (Signature signature : info.signatures) {
                                MessageDigest md;
                                md = MessageDigest.getInstance("SHA");
                                md.update(signature.toByteArray());
                                String something = new String(Base64.encode(md.digest(), 0));
                                Log.e("Hash key", something);
                        }
                } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Log.e("name not found", e.toString());
                }
        }


        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                }
        }

        @Override
        protected void onDestroy() {
                super.onDestroy();
                Session.getCurrentSession().removeCallback(sessionCallback);
        }

        private class SessionCallback implements ISessionCallback {
                @Override
                public void onSessionOpened() {
                        UserManagement.getInstance().me(new MeV2ResponseCallback() {
                                @Override
                                public void onFailure(ErrorResult errorResult) {
                                        int result = errorResult.getErrorCode();

                                        if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                                                Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                                finish();
                                        } else {
                                                Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                }

                                @Override
                                public void onSessionClosed(ErrorResult errorResult) {
                                        Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onSuccess(MeV2Response result) {

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.putExtra("name", result.getNickname());
                                        intent.putExtra("profile", result.getProfileImagePath());


                                        startActivity(intent);
                                        finish();




                                }
                        });
                }



                @Override
                public void onSessionOpenFailed(KakaoException e) {
                        Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
                }
        }



}