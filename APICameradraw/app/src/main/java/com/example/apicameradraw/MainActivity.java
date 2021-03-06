package com.example.apicameradraw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
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
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Button btnCapture;
    private TextureView textureView;
    private ImageView imageView;
    private CameraDevice cameraDevice;
    private String cameraId;
    private Size imageDimensions;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private boolean flashSupported;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    CameraDevice.StateCallback stateCallback;
    File file;
    final int MY_PERMISSIONS_REQUEST_CAMERA=102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = (TextureView)findViewById(R.id.textureView);
        imageView = (ImageView)findViewById(R.id.imageView);

        assert textureView != null;
        assert imageView != null;

        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                createCameraPreview();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                MainActivity.this.cameraDevice.close();
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                MainActivity.this.cameraDevice.close();
                MainActivity.this.cameraDevice = null;        // Change to global
            }
        };

        textureView.setSurfaceTextureListener(textureListener);
        btnCapture = (Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    private Bitmap addTextToBitmap(Bitmap bm)
    {
        String gText = "hello";

        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap gray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

        return gray;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void takePicture()
    {
        if(cameraDevice == null)
            return;

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try
        {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;

            if(cameraCharacteristics == null)
                jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            // Capture image with custom size
            int width = 640;
            int height = 480;

            if(jpegSizes != null && jpegSizes.length > 0)
            {
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

                    file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
                    Image image = null;

                    try
                    {
                        image = imageReader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        // startActivity(new Intent(MainActivity.this, ImageDisplay.class).putExtra("FILE", file.getPath()));
                    }

                    finally {
                        if(image != null)
                            image.close();
                    }
                }

                public void save(byte[] bytes)
                {
                    OutputStream outputStream = null;

                    try
                    {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(outputStream == null) {
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
                    Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    createCameraPreview();
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
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {

        SurfaceTexture texture = textureView.getSurfaceTexture();
        assert texture != null;

        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
        Surface surface = new Surface(texture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if(cameraDevice == null)
                        return;

                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera()
    {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try
        {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Permission to Camera is not granted, request for permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},  MY_PERMISSIONS_REQUEST_CAMERA);
                return;
            }

            cameraManager.openCamera(cameraId, stateCallback, null);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.e("Surfacetexturedestroyed", "called");
            if(cameraDevice != null)
            {
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
            paint.setTextSize(35*m.density);

            Log.i("sss", String.format("width : %d, height:%d", bitmap.getWidth(), bitmap.getHeight()));

        //  Bitmap gray2 =  drawTextToBitmap(this, 50, "hello");
            Bitmap gray = addTextToBitmap(bitmap);

            //Bitmap text_added_bitmap = ();
            Canvas canvas = new Canvas(gray);
            canvas.drawText("Above will add write the text on the bottom left corner of the image", 150, gray.getHeight() - 10, paint);
            imageView.setImageBitmap(gray);

        }
    };

    public Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new intialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text size in pixels
        paint.setTextSize((int) (70 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int m = (bitmap.getWidth() - bounds.width()) / 2;
        int l = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(gText, 1000, l, paint);

        return bitmap;
    }

    public static Bitmap drawStringOnBitmap(Bitmap src, String string, Point location, int color, int alpha, int size, boolean underline, int width , int height) {

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
         string="hello";
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(string, location.x, location.y, paint);

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        }
        catch (InterruptedException e) {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 102:     //MY_PERMISSIONS_REQUEST_CAMERA=102   Use Camera

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay!
                    openCamera();
                }
                else
                {
                    // permission denied, boo!
                    Toast.makeText(this, "Permission to use device Camera denied! Cannot proceed ahead!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            default: Toast.makeText(this, "Failed to handle permissions response!", Toast.LENGTH_SHORT).show();
        }
    }

}