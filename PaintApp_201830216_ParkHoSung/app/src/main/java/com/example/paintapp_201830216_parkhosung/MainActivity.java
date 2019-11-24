package com.example.paintapp_201830216_parkhosung;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //페인트 관련설정
    //브러쉬 모양리스트
    final static int POINT = 0,LINE = 1, CIRCLE = 2, BOX = 3;
    //브러쉬 모양
    static int curShape = LINE;
    //브러쉬 색상리스트
    int[] colorList_body = {Color.RED,Color.GREEN,Color.YELLOW,Color.BLUE,Color.BLACK};
    String[] colorList_idx = {"빨강","초록","노랑","파랑","검정"};
    //브러쉬 색상
    static int paintColor = Color.RED;
    //브러쉬 사이즈
    static int paintSize = 5;
    //커스텀 캔버스 뷰
    MyGraphicView myGraphicView;

    //버튼들
    Button btn_circle,btn_line,btn_point,btn_box,btn_save,btn_color,btn_strock,btn_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myGraphicView = findViewById(R.id.canvas);

        setTitle("그림판");
        //권한처리
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v("권한","Permission is granted"); // 권한이 이미있다!
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); //없으니 새로받는다.
        }
        //버튼연결
        btn_circle = findViewById(R.id.btn_circle);
        btn_line = findViewById(R.id.btn_line);
        btn_point = findViewById(R.id.btn_point);
        btn_box = findViewById(R.id.btn_box);
        btn_save = findViewById(R.id.btn_save);
        btn_color = findViewById(R.id.btn_color);
        btn_strock = findViewById(R.id.btn_strock);
        btn_reset = findViewById(R.id.btn_reset);
        //온클릭리스너
        //브러쉬 모양 리스트
        btn_point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curShape = POINT;
            }
        });
        btn_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curShape = LINE;
            }
        });
        btn_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curShape = CIRCLE;
            }
        });
        btn_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curShape = BOX;
            }
        });
        //저장
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImage(myGraphicView.bm);
            }
        });
        //리셋
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myGraphicView.resetPaint();
            }
        });
        //브러쉬 사이즈
        btn_strock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그 생산
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);

                ad.setTitle("브러쉬 사이즈 설정");       // 제목 설정
                ad.setMessage("브러쉬의 사이즈를 입력해주세요. (기본 : 5)");   // 내용 설정

                // EditText 삽입하기
                final EditText et = new EditText(MainActivity.this);
                et.setHint(Integer.toString(paintSize));
                ad.setView(et);

                // 확인 버튼 설정
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Text 값 받아서 로그 남기기
                        try {
                            paintSize = Integer.parseInt(et.getText().toString()); //굵기 설정
                        }
                        catch (Exception e){
                            Log.e("굵기설정",e.getMessage());
                        }
                        finally {
                            dialog.dismiss();     //닫기
                        }

                    }
                });
                // 취소 버튼 설정
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });

                // 창 띄우기
                ad.show();
            }
        });
        //브러쉬 색상
        btn_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("브러쉬 색상 선택"); //제목

                builder.setItems(colorList_idx, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int pos)
                    {
                        String[] items = colorList_idx;
                        Toast.makeText(getApplicationContext(),items[pos], Toast.LENGTH_SHORT).show();
                        paintColor = colorList_body[pos]; //색상 설정
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
    public static class MyGraphicView extends View {
        //터치 좌표
        int startX = -1,startY = -1,stopX = -1,stopY = -1;
        //그림이 그려질 비트맵
        Bitmap bm;
        //뷰 사이즈
        int width = -1;
        int height = -1;

        public MyGraphicView(Context context , AttributeSet attrs){
            super(context,attrs);
            bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);//비어있는 비트맵 생성
        }
        public void resetPaint(){
            bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //새 비어있는 비트맵 생성
            Paint fillPaint = new Paint(); // 채우기요 패인트
            Rect r = new Rect(0, 0, width, height);
            Canvas tempCanvas = new Canvas(bm);


            // 배경색 채우기
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.WHITE);
            tempCanvas.drawRect(r, fillPaint);

            this.invalidate();
        }
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) { //포커싱이 변화됬을때
            super.onWindowFocusChanged(hasWindowFocus);
            if(width == -1 || height == -1){ // 높이와 너비가 초기값 그대로라면 초기설정
                this.width = this.getWidth();
                this.height = this.getHeight();

                resetPaint();

//            this.invalidate();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) { //터치 이벤트

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN: //터시시작
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    break;
//                case MotionEvent.ACTION_MOVE: if(curShape!=POINT)break; // 이어지는 브러쉬 WIP
                case MotionEvent.ACTION_UP: //터치끝
                    stopX = (int) event.getX();
                    stopY = (int) event.getY();
                    this.invalidate(); //다시그리기
                    break;
            }
            return true;
        }

        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            //페인트 생성
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(paintSize);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(paintColor);
            //임시로 그림을 그릴 캔버스생성
            Canvas tempCanvas = new Canvas(bm);
            //브러쉬모양에따라
            switch (curShape){
                case POINT: //점
                    tempCanvas.drawPoint(stopX,stopY,paint);
                    canvas.drawBitmap(bm, 10, 10, new Paint());
                    break;
                case LINE: //선
                    tempCanvas.drawLine(startX,startY,stopX,stopY,paint);
                    break;
                case CIRCLE: //원
                    float hx = (stopX+startX)/2;
                    float hy = (stopY+startY)/2;
                    float radius = (stopX-hx)>(stopY-hy)?stopX-hx:stopY-hy;
                    tempCanvas.drawCircle(hx,hy,radius,paint);
                    break;
                case BOX: //사각형
                    tempCanvas.drawLine(startX,startY,startX,stopY,paint);
                    tempCanvas.drawLine(startX,startY,stopX,startY,paint);
                    tempCanvas.drawLine(stopX,stopY,startX,stopY,paint);
                    tempCanvas.drawLine(stopX,stopY,stopX,startY,paint);
                    break;
            }
            canvas.drawBitmap(bm, 10, 10, new Paint()); // 캔버스에 올려놓은걸로 캔버스에 그리기
        }
    }

    //저장 메소드
    private void SaveImage(Bitmap finalBitmap) {
        //경로설정 관련
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        //시간으로 파일명 생성
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fname = "Painter_"+ sdf.format(d) +".jpg"; // 파일명 "Painter-yyyy-MM-dd_HH:mm:ss.jps"

        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.d("저장","성공"+root + "/saved_images/"+fname);

        } catch (Exception e) {
            Log.d("저장","실패"+e.getMessage());
            e.printStackTrace();
        }
    }
}
