package red.tel.chat.ui.adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import red.tel.chat.EventBus;
import red.tel.chat.Model;
import red.tel.chat.R;
import red.tel.chat.generated_protobuf.Contact;
import red.tel.chat.ui.OnLoadMoreListener;

public class ItemContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ItemContactAdapter.class.getSimpleName();
    public List<Contact> values = new ArrayList<>();
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean isLoading = true;
    private int pageNext = 0;

    private OnLoadMoreListener onLoadMoreListener;


    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    public void notifyData() {
        Log.d(TAG, "notifyData: .........");
        notifyDataSetChanged();
    }

    public void loadData() {
        values = Model.shared().getContacts();
        if (values.size() == 0) {
            return;
        }
        softListContact();
    }

    public ItemContactAdapter(RecyclerView recyclerView) {
        loadData();
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore(pageNext);
                    }
                    isLoading = true;
                }
            }
        });

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_list_content, parent, false);
        return new ItemContactAdapter.ViewHolder(view);
    }

    public void onLoadDataFromServer(Context context) {
        EventBus.listenFor(context, EventBus.Event.CONTACTS, () -> {
            values = Model.shared().getContacts();
            softListContact();
            notifyData();
        });
    }

    private void softListContact() {
        if (values.size() == 0) {
            return;
        }
        Collections.sort(values, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact t1) {
                return contact.name == null || t1.name == null ? 0 : contact.name.compareTo(t1.name);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemContactAdapter.ViewHolder viewHolder = (ItemContactAdapter.ViewHolder) holder;
        String name = this.values.get(position).name;
        if (name == null || name.equals("")) {
            return;
        }
        viewHolder.contactName.setText(name);
        if (Model.shared().isOnline(name)) {
            viewHolder.contactName.setTextColor(Color.BLUE);
            viewHolder.contactName.setTypeface(null, Typeface.BOLD);
        } else {
            viewHolder.contactName.setTextColor(Color.GRAY);
            viewHolder.contactName.setTypeface(null, Typeface.NORMAL);
        }
        viewHolder.view.setOnClickListener((View v) -> onLoadMoreListener.onClickItemAdapter(name));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void setPageNext(int pageNext) {
        this.pageNext = pageNext;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView contactName;
        final ImageButton deleteButton;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            contactName = view.findViewById(R.id.contactName);
            deleteButton = view.findViewById(R.id.deleteButton);
            view.setOnLongClickListener(v -> {
                deleteButton.setVisibility(View.VISIBLE);
                return true;
            });
            deleteButton.setOnClickListener(v -> onClickDelete());
        }

        private void onClickDelete() {
            AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
            alert.setTitle(R.string.del_contact_title);
            alert.setMessage(R.string.del_contact_message);

            alert.setPositiveButton(R.string.ok, (dialog, whichButton) ->{
                onLoadMoreListener.onDeleteContact(getAdapterPosition());
                deleteButton.setVisibility(View.GONE);
            });

            alert.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());
            alert.create().show();
        }
    }
}
