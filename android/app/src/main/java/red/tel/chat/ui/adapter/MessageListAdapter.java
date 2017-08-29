package red.tel.chat.ui.adapter;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import red.tel.chat.R;
import red.tel.chat.generated_protobuf.Text;

import static android.text.Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;


public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolderMessageText> {
    private static final int MESSAGE_TYPE_OUTGOING = 1;
    private static final int MESSAGE_TYPE_INCOMING = 2;
    private List<Text> textList = new ArrayList<>();
    private CallBackMessage callBackMessage;

    public MessageListAdapter(CallBackMessage callBackMessage) {
        this.callBackMessage = callBackMessage;
        textList = this.callBackMessage.getMessage();
    }

    @Override
    public ViewHolderMessageText onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MESSAGE_TYPE_INCOMING:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conversation_item_received, parent, false);
                return new ViewHolderMessageText(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conversation_item_sent, parent, false);
                return new ViewHolderMessageText(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderMessageText holder, int position) {
        Text text = textList.get(position);
        String line = String.format(Locale.US, "%1$s:\n %2$s", "<b>" + text.from + "</b>", text.body.utf8());
        switch (getItemViewType(position)) {
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.tvMessage.setText(Html.fromHtml(line, TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
                } else {
                    holder.tvMessage.setText(Html.fromHtml(line));
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return textList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Text text = textList.get(position);
        if (!text.to.equals(this.callBackMessage.getWhom())) {
            return MESSAGE_TYPE_INCOMING;
        } else {
            return MESSAGE_TYPE_OUTGOING;
        }
    }

    static class ViewHolderMessageText extends RecyclerView.ViewHolder {
        private TextView tvMessage;

        ViewHolderMessageText(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    public interface CallBackMessage {
        String getWhom();

        List<Text> getMessage();
    }
}
