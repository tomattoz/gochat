package red.tel.chat.camera;


import android.view.TextureView;

public interface RecordContact {
    interface View {

        void setSurfaceTextureListener(TextureView.SurfaceTextureListener listener);

        TextureView getTextureView();

        int getUploadPanelParentWidth();

        int getUploadPanelParentHeight();

        void getDataVideo(byte[] bytes);
    }

    interface Presenter {
        void setView(View view);

        void startPreview();

        void stopUpload();

        void startUpload();

        void clear();

        void setCameraFacing(@CameraFacing int cameraFacing);
    }
}
