package com.example.androiddataparsing;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {
    TextView itemname;
    ImageView pictureurl;

    //itemname에 출력할 텍스트
    String name;
    //ImageView에 출력할 이미지 url
    String image;

    class ThreadEx extends Thread {
        public void run() {
            //다운로드 받은 문자열을 저장할 변수
            String result = null;
            try {
                URL url = new URL("http://192.168.0.200:8080/oracleserver/detail?itemid=" + 1);
                HttpURLConnection con =
                        (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(30000);

                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    sb.append(line + "\n");
                }
                result = sb.toString();
                br.close();
                con.disconnect();

            } catch (Exception e) {
                Log.e("다운로드 예외", e.getMessage());
            }

            //가져온 데이터 파싱
            try {
                JSONObject object = new JSONObject(result);
                JSONObject item = object.getJSONObject("item");
                //itemname 과 pictureurl 만 가져와서 저장
                name = item.getString("itemname");
                image = item.getString("pictureurl");

                //핸들러 호출
                Message message = new Message();
                handler.sendMessage(message);

            } catch (Exception e) {
                Log.e("파싱 예외", e.getMessage());
            }
        }
    }

    Handler handler = new Handler(
            Looper.getMainLooper()) {

        public void handleMessage(Message message) {
            itemname.setText(name);
            //이미지를 다운로드 받기 위해서 이미지 스레드를 실행
            new ImageThread().start();
        }
    };


    //이미지를 다운로드 받는 스레드
    class ImageThread extends Thread {
        public void run() {
            try {
                URL url = new URL(
                        "http://192.168.0.200:8080/oracleserver/img/"
                                + image);
                InputStream is = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                //핸들러를 호출하기
                Message message = new Message();
                message.obj = bitmap;
                imageHandler.sendMessage(message);
            } catch (Exception e) {
                Log.e("이미지 다운로드 예외", e.getMessage());
            }
        }
    }

    Handler imageHandler = new Handler(
            Looper.getMainLooper()){
        public void handleMessage(Message message){
            Bitmap bitmap = (Bitmap)message.obj;
            pictureurl.setImageBitmap(bitmap);
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        itemname = (TextView)findViewById(R.id.itemname);
        pictureurl = (ImageView)findViewById(R.id.pictureurl);

        new ThreadEx().start();
    }
}