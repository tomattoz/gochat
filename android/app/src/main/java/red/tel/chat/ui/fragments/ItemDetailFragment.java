package red.tel.chat.ui.fragments;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import okio.ByteString;
import red.tel.chat.Backend;
import red.tel.chat.EventBus;
import red.tel.chat.Model;
import red.tel.chat.R;
import red.tel.chat.generated_protobuf.Text;
import red.tel.chat.ui.SoftKeyboard;
import red.tel.chat.ui.activitys.ItemDetailActivity;
import red.tel.chat.ui.activitys.ItemListActivity;
import red.tel.chat.ui.adapter.MessageListAdapter;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements MessageListAdapter.CallBackMessage {

    private static final String TAG = "ItemDetailFragment";

    // The fragment argument representing the item ID that this fragment represents.
    public static final String ARG_ITEM_ID = "item_id";

    private String whom;
    private List<Text> textList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MessageListAdapter listAdapter;
    private SoftKeyboard softKeyboard;

    // Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
    // screen orientation changes).
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment arguments. In a real-world scenario,
            // use a Loader to load content from a content provider.
            whom = getArguments().getString(ARG_ITEM_ID);

            Activity activity = this.getActivity();
            Toolbar appBarLayout = (Toolbar) activity.findViewById(R.id.detail_toolbar);
            if (appBarLayout != null) {
                appBarLayout.setTitle(whom);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);
        getActivity().setTitle(whom);

        EditText messageEdit = rootView.findViewById(R.id.messageEdit);
        ImageButton messageSend = rootView.findViewById(R.id.chatSendButton);
        LinearLayout content = rootView.findViewById(R.id.container);
        recyclerView = rootView.findViewById(R.id.chat_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        listAdapter = new MessageListAdapter(this);
        recyclerView.setAdapter(listAdapter);
        messageSend.setOnClickListener(v -> {
            String message = messageEdit.getText().toString();
            Model.shared().addText(message, Model.shared().getUsername(), whom);
            Backend.shared().sendText(message, whom);
            Text text = new Text.Builder().body(ByteString.encodeUtf8(message)).from(Model.shared().getUsername()).to(whom).build();
            textList.add(text);
            int position = textList.size() - 1;
            listAdapter.notifyItemInserted(position);
            recyclerView.scrollToPosition(position);
            messageEdit.setText("");
        });
        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(content, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {

            @Override
            public void onSoftKeyboardHide() {
                // Code here
            }

            @Override
            public void onSoftKeyboardShow() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    scrollListMessage();
                }, 500);
            }
        });

        updateTexts();
        scrollListMessage();
        EventBus.listenFor(getContext(), EventBus.Event.TEXT, () -> {
            updateTexts();
            scrollListMessage();
        });
        return rootView;
    }

    private void scrollListMessage() {
        if (textList.size() > 0) {
            int position = textList.size() - 1;
            recyclerView.scrollToPosition(position);
        }
    }

    private void updateTexts() {
        for (Text text : Model.shared().getTexts()) {
            if (text != null && (text.to.equals(whom) || text.from.equals(whom))) {
                textList.remove(text);
                textList.add(text);
            }
        }
    }

    @Override
    public String getWhom() {
        return whom;
    }

    @Override
    public List<Text> getMessage() {
        return textList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (softKeyboard != null) {
            softKeyboard.unRegisterSoftKeyboardCallback();
        }
    }
}
