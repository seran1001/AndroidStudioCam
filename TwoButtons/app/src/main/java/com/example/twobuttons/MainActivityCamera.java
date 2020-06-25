package com.example.twobuttons;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivityCamera extends AppCompatActivity {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public static final String webUrl = "http://10.0.2.2/javascript_test.html";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    SurfaceView cameraView, transparentView;
    SurfaceHolder holder, holderTransparent;
    private Button btnCapture;
    private TextureView textureView;
    private SurfaceTexture surface;
    private ImageView imageView;
    private CameraDevice cameraDevice;
    private String cameraId;
    private Size imageDimensions;
    private Size mPreviewSize;
    private String mCameraId;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private boolean flashSupported;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private float RectLeft, RectTop, RectRight, RectBottom;
    CameraDevice.StateCallback stateCallback;
    File file;
    final int MY_PERMISSIONS_REQUEST_CAMERA = 102;

    public void onGetNameClick(View view) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_camera);
        textureView = (TextureView) findViewById(R.id.textureView);
        imageView = (ImageView) findViewById(R.id.imageView);

        assert textureView != null;
        assert imageView != null;

        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                try {
                    createCameraPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                MainActivityCamera.this.cameraDevice.close();
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                MainActivityCamera.this.cameraDevice.close();
                MainActivityCamera.this.cameraDevice = null;        // Change to global
            }
        };

        textureView.setSurfaceTextureListener(textureListener);
        btnCapture = (Button) findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    public void loadWebPage(View view) {

        WebViewFragment webViewFragment = new WebViewFragment();

    }


    private Bitmap addTextToBitmap(Bitmap bm) {
        String gText = "hello";

        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap gray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

        return gray;


    }


    public void setSurface(SurfaceTexture _surface) {
        surface = _surface;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void takePicture() {
        if (cameraDevice == null)
            return;

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;

            if (cameraCharacteristics == null)
                jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            // Capture image with custom size
            int width = 640;
            int height = 480;



            /**/
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            final List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(imageReader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Bitmap bm = textureView.getBitmap();
                    file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
                    Image image = null;

                    try {
                        image = imageReader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        Bitmap cameraBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);

                        if (cameraBitmap == null) {
                            Log.d("oof", "ImageSaver cameraBitmap is null");
                            return;
                        } else {
                            bm = cameraBitmap;
                        }
                        // startActivity(new Intent(MainActivity.this, ImageDisplay.class).putExtra("FILE", file.getPath()));
                    } finally {
                        if (image != null)
                            image.close();
                    }

                    Canvas camImgCanvas = new Canvas(bm);


                }

                public void save(byte[] bytes) {
                    OutputStream outputStream = null;

                    try {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (outputStream == null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };

            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);

            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivityCamera.this, "Saved", Toast.LENGTH_SHORT).show();
                    try {
                        createCameraPreview();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureCallback, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() throws CameraAccessException {

        SurfaceTexture texture = textureView.getSurfaceTexture();

        assert texture != null;
        textureView.animate();

        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
        Surface surface = new Surface(texture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;

                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivityCamera.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(MainActivityCamera.this, "Error!", Toast.LENGTH_SHORT).show();

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        onDraw();
        /**
         Canvas canvas = textureView.lockCanvas();
         Paint myPaint = new Paint();
         myPaint.setColor(Color.WHITE);
         myPaint.setStrokeWidth(10);
         canvas.drawRect(100, 100, 300, 300, myPaint);
         textureView.unlockCanvasAndPost(canvas);
         **/
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ContextCompat.checkSelfPermission(MainActivityCamera.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Permission to Camera is not granted, request for permission
                ActivityCompat.requestPermissions(MainActivityCamera.this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
                return;
            }

            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected final void onDraw() {
        //   Canvas canvas = textureView.lockCanvas();
        Paint myPaint = new Paint();
        myPaint.setColor(Color.WHITE);
        myPaint.setStrokeWidth(10);
        //  canvas.drawRect(100, 100, 300, 300, myPaint);
        Bitmap bm = textureView.getBitmap();
        //  drawTextToBitmap(getApplicationContext(),1,"Hello");
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(android.R.color.black)); // Text Color
        DisplayMetrics m = getResources().getDisplayMetrics();
        paint.setTextSize(315 * m.density);
        canvas.drawText("Hi", 6, bm.getHeight() - 10, paint);
        textureView.unlockCanvasAndPost(canvas);
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            openCamera();
            Bitmap bm = textureView.getBitmap();
            //  drawTextToBitmap(getApplicationContext(),1,"Hello");
            Canvas canvas = new Canvas(bm);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(android.R.color.black)); // Text Color
            DisplayMetrics m = getResources().getDisplayMetrics();
            paint.setTextSize(315 * m.density);
            canvas.drawText("Hi", 6, bm.getHeight() - 10, paint);

            addTextToBitmap(bm);
            //  Toast.makeText(getApplicationContext(), "TextureView is available", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.e("Surfacetexturedestroyed", "called");

            //    Toast.makeText(getApplicationContext(), "TextureView is available", Toast.LENGTH_SHORT).show();

            if (cameraDevice != null) {
                Log.e("Camera not null", "make null");
                cameraDevice.close();
                cameraDevice = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            Bitmap bitmap = textureView.getBitmap();
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(android.R.color.black)); // Text Color
            DisplayMetrics m = getResources().getDisplayMetrics();
            paint.setTextSize(315 * m.density);
            // SurfaceTexture texture = textureView.getSurfaceTexture();
            //      Log.i("sss", String.format("width : %d, height:%d", bitmap.getWidth(), bitmap.getHeight()));

            String text = "TextureView is available";
            // Toast.makeText(getApplicationContext(), "TextureView is available", Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.CYAN);
            v.setBackgroundColor(Color.TRANSPARENT);

            //toast.show();

            Bitmap gray = addTextToBitmap(bitmap);
            //Bitmap text_added_bitmap = ();

            Canvas canvas = new Canvas(gray);
            canvas.drawText("Hi", 6, gray.getHeight() - 10, paint);
            // canvas.drawText("Above will add write the text ", 1, gray.getHeight() - 3, paint);
            // *** textureView.setImageBitmap(gray);
            // getApplicationContext();

            //   textureView.setSurfaceTextureListener(textureListener);
            //   textureView = new TextureView(textureView.getContext());

            //(R.id.textureView)
            //can get listeners and mainthread changable UI
            imageView.setImageBitmap(gray);
            textureView.getContext();
        }
    };

    private void editTextureView(int width, int height) {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        Bitmap bitmap = textureView.getBitmap();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(android.R.color.black)); // Text Color
        DisplayMetrics m = getResources().getDisplayMetrics();
        paint.setTextSize(315 * m.density);
        // SurfaceTexture texture = textureView.getSurfaceTexture();
        Log.i("sss", String.format("width : %d, height:%d", bitmap.getWidth(), bitmap.getHeight()));

        String text = "TextureView is available";
        // Toast.makeText(getApplicationContext(), "TextureView is available", Toast.LENGTH_SHORT).show();
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.CYAN);
        v.setBackgroundColor(Color.TRANSPARENT);

        toast.show();

        Bitmap gray = addTextToBitmap(bitmap);
        //Bitmap text_added_bitmap = ();

        Canvas canvas = new Canvas(gray);
        canvas.drawText("Hi", 6, gray.getHeight() - 10, paint);
        // canvas.drawText("Above will add write the text ", 1, gray.getHeight() - 3, paint);
        // *** textureView.setImageBitmap(gray);
        // getApplicationContext();

        //   textureView.setSurfaceTextureListener(textureListener);
        //   textureView = new TextureView(textureView.getContext());

        //(R.id.textureView)
        //can get listeners and mainthread changable UI
        imageView.setImageBitmap(gray);
        textureView.getContext();

        if (mPreviewSize == null || textureView == null) {
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = textureRectF.centerX();
    }

    public static Bitmap drawStringOnBitmap(Bitmap src, String string, Point location, int color, int size, boolean underline, int width, int height) {

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        string = "hello";
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(color);

        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(string, location.x, location.y, paint);

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // surface1.startDrawThread();
        startBackgroundThread();

        if (textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        // surface1.stopDrawThread();
        stopBackgroundThread();

        super.onPause();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Handler for handling the user event after requesting permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 102:     //MY_PERMISSIONS_REQUEST_CAMERA=102   Use Camera

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    openCamera();
                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "Permission to use device Camera denied! Cannot proceed ahead!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            default:
                Toast.makeText(this, "Failed to handle permissions response!", Toast.LENGTH_SHORT).show();
        }
    }

}
