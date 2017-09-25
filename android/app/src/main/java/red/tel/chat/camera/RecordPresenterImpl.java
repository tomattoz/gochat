package red.tel.chat.camera;


import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import static red.tel.chat.Constant.CAMERA_FACING_FRONT;

public class RecordPresenterImpl implements RecordContact.Presenter {
    private static final String TAG = RecordPresenterImpl.class.getSimpleName();
    //audio
    private static final int SAMPLE_RATE = 44100;
    private static final VideoQuality VIDEO_QUALITY = new VideoQuality(
            240, 320, 15,
            SAMPLE_RATE,
            100000,// 640*480*15*0.1 460800
            16000,
            "superfast"
    );
    private RecordContact.View mView;
    private Activity mActivity;
    private CameraManager mCameraManager;
    private int mExpectIntervalTime = 0;
    @CameraFacing
    private int mCameraIndex;
    private long startTime = 0;
    private byte[] mNV21Buffer = null;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private boolean mIsInMirror = false;
    private int mDegree = 0;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.d(TAG, "onSurfaceTextureAvailable");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.d(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            //Log.d(TAG,"onSurfaceTextureUpdated");
        }
    };
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();

            mView.getDataVideo(bytes, size);
            Log.d(TAG, "onPreviewFrame: ");
            CameraManager.getInstance().addBuffer(bytes);
        }
    };
    private CameraManager.OpenCameraListener mOpenCameraListener = new CameraManager.OpenCameraListener() {
        @Override
        public void onCallbackCameraInfo(final int realwidth, final int realheight, final int degree, int cindex) {
            if (mCameraIndex == CAMERA_FACING_FRONT) {
                mIsInMirror = true;
            }
            mDegree = degree;
            int previewWidth = realwidth;
            int previewHeight = realheight;
            if (degree == 90 || degree == 270) {
                previewWidth = realheight;
                previewHeight = realwidth;
            }
            mNV21Buffer = new byte[previewWidth * previewHeight * 3 / 2];
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resetCameraViewSize(mView.getTextureView(), realwidth, realheight, degree);
                }
            });
        }
    };

    public RecordPresenterImpl(Activity mActivity) {
        this.mActivity = mActivity;
        this.mCameraManager = CameraManager.getInstance();
        mExpectIntervalTime = (int) ((float) 1000 / (float) VIDEO_QUALITY.getFrameRate());
    }

    @Override
    public void setView(RecordContact.View view) {
        this.mView = view;
        mView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    public void startPreview() {
        startTime = System.currentTimeMillis();
        if (mView == null) return;
        if (mView.getTextureView() == null) return;
        mCameraManager.openCacmeraAndPreview(mActivity, mCameraIndex, VIDEO_QUALITY,
                mView.getTextureView(), mView.getUploadPanelParentWidth(), mView.getUploadPanelParentHeight(),
                mOpenCameraListener, mPreviewCallback);
    }

    @Override
    public void stopUpload() {
        clear();
    }

    @Override
    public void startUpload() {

    }

    @Override
    public void clear() {
        if (mCameraManager != null) {
            mCameraManager.releaseCamera();
        }
    }

    @Override
    public void setCameraFacing(@CameraFacing int cameraFacing) {
        this.mCameraIndex = cameraFacing;
        mIsInMirror = false;
        clear();
    }

    public void resetCameraViewSize(View targetView, int realWidth, int realHeight, int degree) {
        int currentWidth = targetView.getMeasuredWidth();
        int currentHeight = targetView.getMeasuredHeight();
        mPreviewWidth = realWidth;
        mPreviewHeight = realHeight;
        mDisplayWidth = realWidth;
        mDisplayHeight = realHeight;
        if (degree == 90 || degree == 270) {
            mDisplayWidth = realHeight;
            mDisplayHeight = realWidth;
        }
        float scaleRatioOfWidth = (float) mDisplayWidth / (float) currentWidth;
        float scaleRatioOfHeight = (float) mDisplayHeight / (float) currentHeight;
        float expectRatio = scaleRatioOfWidth < scaleRatioOfHeight ? scaleRatioOfWidth : scaleRatioOfHeight;
        int expectWidth = (int) ((float) mDisplayWidth / expectRatio);
        int expectHeight = (int) ((float) mDisplayHeight / expectRatio);
        int parentWidth = ((View) targetView.getParent()).getMeasuredWidth();
        int parentHeight = ((View) targetView.getParent()).getMeasuredHeight();
        Log.d(TAG, "degree:" + degree);
        Log.d(TAG, "currentWidth:" + currentWidth);
        Log.d(TAG, "currentHeight:" + currentHeight);
        Log.d(TAG, "mDisplayWidth:" + mDisplayWidth);
        Log.d(TAG, "mDisplayHeight:" + mDisplayHeight);
        Log.d(TAG, "scaleRatioOfWidth:" + scaleRatioOfWidth);
        Log.d(TAG, "scaleRatioOfHeight:" + scaleRatioOfHeight);
        Log.d(TAG, "expectRatio:" + expectRatio);
        Log.d(TAG, "expectWidth:" + expectWidth);
        Log.d(TAG, "expectHeight:" + expectHeight);
        Log.d(TAG, "parentWidth:" + parentWidth);
        Log.d(TAG, "parentHeight:" + parentHeight);

        ViewGroup.LayoutParams lp = targetView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            lp.width = expectWidth;
            lp.height = expectHeight;
            int marginTop = (int) ((float) (parentHeight - expectHeight) / 2);
            int marginLeft = (int) ((float) (parentWidth - expectWidth) / 2);
            ((ViewGroup.MarginLayoutParams) lp).setMargins(marginLeft, marginTop, 0, 0);
            targetView.setLayoutParams(lp);
            Log.d(TAG, "marginTop:" + marginTop);
            Log.d(TAG, "marginLeft:" + marginLeft);
        }
    }
}
