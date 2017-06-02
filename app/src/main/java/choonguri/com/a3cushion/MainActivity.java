package choonguri.com.a3cushion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView resultView;
    private Predictor predictor;

    private static final String MODEL_FILE = "file:///android_asset/3cushion_frozen_model.pb";
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String[] LABEL = new String[]{"뒤돌리기(우라)", "제각돌리기(학구)", "원쿠션넣어치기(빵꾸)", "옆돌리기(마오시)", "빈쿠션(쓰리가락)", "대회전(레지)", "횡단샷(따블)", "비껴치기(짱꼴라)"};

    private Executor executor = Executors.newSingleThreadExecutor();
    private RelativeLayout table;
    private ImageView checkImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table = (RelativeLayout) findViewById(R.id.table);
        table.setDrawingCacheEnabled(true);
        ImageView redBall1 = (ImageView) findViewById(R.id.redBall1);
        ImageView redBall2 = (ImageView) findViewById(R.id.redBall2);
        ImageView whiteBall = (ImageView) findViewById(R.id.whiteBall);
        checkImage = (ImageView) findViewById(R.id.checkImage);

        redBall1.setOnTouchListener(new BallOnTouchListener(table));
        redBall2.setOnTouchListener(new BallOnTouchListener(table));
        whiteBall.setOnTouchListener(new BallOnTouchListener(table));

        WindowManager wManager=getWindowManager();
        Display display = wManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int screenWidth = (int) (metrics.widthPixels * 0.95);
        int screenHeight = screenWidth / 2;

        table.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight));

        resultView = (TextView) findViewById(R.id.resultView);
        initTensorFlowAndLoadModel();
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    predictor = Predictor.create(getAssets(), MODEL_FILE, INPUT_NAME, OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }


    public void onClickPrediction(View view) {
        table.invalidate();
        Bitmap captureBitmap = table.getDrawingCache();
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(captureBitmap, 64, 32, true);

        float[] result = predictor.getResult(resizeBitmap);
        for(float r : result) {
            Log.d(TAG, "::::: ["+r+"]");
        }

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });
        for (int i = 0; i < result.length; ++i) {
            pq.add(new Recognition(LABEL[i], result[i]));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; ++i) {
            Recognition rc = pq.poll();
            sb.append(rc.getTitle()+" : " + Math.round(rc.getConfidence()*100.0f) + "%\n");
        }

        resultView.setText(sb.toString());
    }

    private Bitmap getTestBitmap() {
        // get input stream
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("sample.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(inputStream);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                predictor.close();
            }
        });
    }

    private class Recognition {
        private String title;
        private Float confidence;

        public Recognition(String title, float confidence) {
            this.title = title;
            this.confidence = confidence;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public void setConfidence(Float confidence) {
            this.confidence = confidence;
        }
    }
}
