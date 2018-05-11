package cameraview.troy.www.cameraview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.troy.cameralib.AutoActivity;
import com.troy.cameralib.EasyCamera;
import com.troy.cameralib.util.DisplayUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "摄像机的参数";
    private Button btnCapture;
    private ImageView ivImage;
    private int screenWidth;
    private float ratio = 0.1f; //取景框高宽比

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImage = (ImageView) findViewById(R.id.iv_image);
        btnCapture = (Button) findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String SAMPLE_CROPPED_IMAGE_NAME = "cropImage_" + System.currentTimeMillis() + ".png";
//                Uri destination = Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME));
//                EasyCamera.create(destination)
//                        .withViewRatio(ratio)
//                        .SetTmp(Integer.parseInt(TextUtils.isEmpty(edittext.getText()) ? "100" : edittext.getText().toString()))
//                        .withMarginCameraEdge((int) DisplayUtil.dp2px(MainActivity.this, 150), (int) DisplayUtil.dp2px(MainActivity.this, 100))
//                        .start(MainActivity.this);

               startActivity(new Intent(MainActivity.this, AutoActivity.class));
            }
        });

        screenWidth = (int) DisplayUtil.getScreenWidth(this);
        ivImage.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int) (screenWidth * ratio)));
        ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == EasyCamera.REQUEST_CAPTURE) {
                Uri resultUri = EasyCamera.getOutput(data);
                int width = EasyCamera.getImageWidth(data);
                int height = EasyCamera.getImageHeight(data);

                String outputUrl = EasyCamera.getOutputUrl(data);
                Log.i(TAG, "outputUrl:" + outputUrl);

                Uri parse = Uri.parse(outputUrl);
                Log.i(TAG, "parse:" + parse);
                ivImage.setImageURI(parse);
//                ivImage.setDrawingCacheEnabled(true);
//                Bitmap drawingCache = ivImage.getDrawingCache();
//                ivImage.setDrawingCacheEnabled(false);

                Log.i(TAG, "imageWidth:" + width);
                Log.i(TAG, "imageHeight:" + height);
            }
        }
    }
}
