package red.tel.chat.ui.activitys;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import red.tel.chat.R;

public class IncomingCallActivity extends BaseActivity implements View.OnClickListener {
    public static final String WHOM = "whom";
    private TextView from;
    private Button btnAccept;
    private Button btnDecline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        from = findViewById(R.id.from);
        btnAccept = findViewById(R.id.accept);
        btnDecline = findViewById(R.id.decline);
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String whom = bundle.getString(WHOM);
            from.setText(whom != null ? whom : "");
        }
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
}
