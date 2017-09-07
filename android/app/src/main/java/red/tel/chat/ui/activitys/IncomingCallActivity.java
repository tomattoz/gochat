package red.tel.chat.ui.activitys;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import red.tel.chat.R;
import red.tel.chat.camera.CameraView;
import red.tel.chat.network.NetworkCall;
import red.tel.chat.ui.fragments.ItemDetailFragment;

public class IncomingCallActivity extends BaseActivity implements View.OnClickListener, NetworkCall.NetworkCallProposalReceiverProtocol {
    private TextView from;
    private Button btnAccept;
    private Button btnDecline;
    private CameraView cameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        from = findViewById(R.id.from);
        cameraView = findViewById(R.id.camera);
        btnAccept = findViewById(R.id.accept);
        btnDecline = findViewById(R.id.decline);
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String whom = bundle.getString(ItemDetailFragment.ARG_ITEM_ID);
            from.setText(whom != null ? whom : "");
        }

        NetworkCall.getInstance().callAudioAsync(from.getText().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accept:
                break;
            case R.id.decline:
                break;
            default:
                break;
        }
    }

    @Override
    public void callInfo(NetworkCall.NetworkCallProposalInfo info) {

    }
}
