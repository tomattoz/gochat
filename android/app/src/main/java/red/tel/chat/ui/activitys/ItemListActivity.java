package red.tel.chat.ui.activitys;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.extensions.Contact;
import com.microsoft.graph.extensions.IContactCollectionPage;
import com.microsoft.graph.extensions.IGraphServiceClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import red.tel.chat.ChatApp;
import red.tel.chat.EventBus;
import red.tel.chat.Model;
import red.tel.chat.R;
import red.tel.chat.generated_protobuf.Wire;
import red.tel.chat.office365.Constants;
import red.tel.chat.office365.model.ContactsModel;
import red.tel.chat.ui.OnLoadMoreListener;
import red.tel.chat.ui.fragments.ItemDetailFragment;
import red.tel.chat.ui.presenter.ContactsContract;
import red.tel.chat.ui.presenter.ContactsPresenter;

/**
 * An activity representing a list of Items. This activity has different presentations for handset
 * and tablet-size devices. On handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing item details. On tablets, the activity presents
 * the list of items and item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends BaseActivity implements ContactsContract.ContactsView, OnLoadMoreListener {

    private static final String TAG = "ItemListActivity";
    private boolean isTwoPane; // Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    private SimpleItemRecyclerViewAdapter recyclerViewAdapter;
    private ContactsContract.Presenter presenter;
    private IGraphServiceClient mGraphServiceClient;

    private Scheduler scheduler;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            isTwoPane = true;
        }
        // Initialize Presenter
        presenter = new ContactsPresenter();
        // Attach View to it
        presenter.attachView(this);
        if (Model.shared().getTypeLogin() == Constants.TYPE_LOGIN_MS) {
            presenter.getListContacts(0);
            mGraphServiceClient = ((ChatApp) getApplication()).getGraphServiceClient();
            mGraphServiceClient.getMe().getContacts().buildRequest().get(new ICallback<IContactCollectionPage>() {
                @Override
                public void success(IContactCollectionPage iContactCollectionPage) {
                    for (Contact contact : iContactCollectionPage.getCurrentPage()) {
                        Log.d(TAG, "success: " + contact.displayName);
                    }
                }

                @Override
                public void failure(ClientException ex) {
                    Log.e(TAG, "failure: ", ex);
                }
            });
        }

    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
        this.recyclerViewAdapter = new SimpleItemRecyclerViewAdapter(recyclerView);
        recyclerView.setAdapter(this.recyclerViewAdapter);
        this.recyclerViewAdapter.setOnLoadMoreListener(this);
        this.recyclerViewAdapter.onLoadDataFromServer(this);
    }

    @Override
    public void showListContact(ContactsModel contactsModel) {
        Log.d(TAG, "showListContact: ");

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = Observable.just(recyclerViewAdapter.values)
                .observeOn(scheduler)
                .map(t -> {
                    Log.d(TAG, "showListContact: 1");
                    List<red.tel.chat.generated_protobuf.Contact> contacts = new ArrayList<>();
                    contacts.addAll(t);
                    List<red.tel.chat.generated_protobuf.Contact> contactList = new ArrayList<>();
                    for (ContactsModel.DataContacts newCon : contactsModel.getDataContacts()) {
                        if (newCon.getEmailAddresses() == null || newCon.getEmailAddresses().get(0).address == null) {
                            continue;
                        }
                        red.tel.chat.generated_protobuf.Contact contact = new red.tel.chat.generated_protobuf.Contact.Builder()
                                .id(newCon.getEmailAddresses().get(0).address)
                                .name(newCon.getEmailAddresses().get(0).address).build();
                        for (red.tel.chat.generated_protobuf.Contact contact1 : contacts) {
                            if (contact1.id.equals(newCon.getEmailAddresses().get(0).address)) {//chu y
                                contacts.remove(contact1);
                                break;
                            }
                        }
                        contactList.add(contact);
                    }
                    contactList.addAll(contacts);
                    Collections.sort(contactList, new Comparator<red.tel.chat.generated_protobuf.Contact>() {
                        @Override
                        public int compare(red.tel.chat.generated_protobuf.Contact contact, red.tel.chat.generated_protobuf.Contact t1) {
                            return contact.name.compareTo(t1.name);
                        }
                    });
                    return contactList;
                }).flatMap(res -> {
                    Log.d(TAG, "showListContact: 2");
                    return Observable.just(res);
                }).observeOn(AndroidSchedulers.mainThread()).
                        subscribe(t -> {
                            Log.d(TAG, "showListContact: 3");
                            recyclerViewAdapter.values = t;
                            Model.shared().setContacts(recyclerViewAdapter.values);
                            recyclerViewAdapter.notifyData();

                            if (contactsModel.getNexPage() != 0) {
                                recyclerViewAdapter.setPageNext(contactsModel.getNexPage());
                                recyclerViewAdapter.setLoaded();
                            }
                        });

    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError: ", e);
    }

    //load more list contact
    @Override
    public void onLoadMore(int nexPage) {
        if (Model.shared().getTypeLogin() == Constants.TYPE_LOGIN_MS) {
            presenter.getListContacts(nexPage);
        }
    }

    class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<red.tel.chat.generated_protobuf.Contact> values = new ArrayList<>();
        private int visibleThreshold = 5;
        private int lastVisibleItem, totalItemCount;
        private boolean isLoading;
        private int pageNext = 0;

        private OnLoadMoreListener onLoadMoreListener;


        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            this.onLoadMoreListener = mOnLoadMoreListener;
        }

        public void notifyData() {
            Log.d(TAG, "notifyData: .........");
            notifyDataSetChanged();
        }

        SimpleItemRecyclerViewAdapter(RecyclerView recyclerView) {
            values = Model.shared().getContacts();
            softListContact();
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading && totalItemCount <= (lastVisibleItem + 1)) {
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
            //onLoadDataFromServer(parent);
            return new ViewHolder(view);
        }

        private void onLoadDataFromServer(Context context) {
            EventBus.listenFor(context, EventBus.Event.CONTACTS, () -> {
                values = Model.shared().getContacts();
                softListContact();
                notifyData();
            });
        }

        private void softListContact() {
            Collections.sort(values, new Comparator<red.tel.chat.generated_protobuf.Contact>() {
                @Override
                public int compare(red.tel.chat.generated_protobuf.Contact contact, red.tel.chat.generated_protobuf.Contact t1) {
                    return contact.name.compareTo(t1.name);
                }
            });
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
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
            viewHolder.view.setOnClickListener((View v) -> {
                if (isTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, name);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, name);
                    context.startActivity(intent);
                }
            });
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

                alert.setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                    recyclerViewAdapter.values.remove(contactName.getText().toString());
                    Model.shared().setContacts(recyclerViewAdapter.values);
                    recyclerViewAdapter.notifyData();
                });

                alert.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());
                alert.create().show();
            }
        }
    }

    public void onClickAdd(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.add_contact_title);
        alert.setMessage(R.string.add_contact_message);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.ok, (dialog, whichButton) -> {
            String name = input.getEditableText().toString();
            red.tel.chat.generated_protobuf.Contact contact = new red.tel.chat.generated_protobuf.Contact.Builder()
                    .id(name)
                    .name(name).build();
            recyclerViewAdapter.values.add(contact);
            Model.shared().setContacts(recyclerViewAdapter.values);
            recyclerViewAdapter.notifyData();
        });

        alert.setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel());
        alert.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    protected void onSubscribeEvent(Object object) {
        super.onSubscribeEvent(object);
        if (object == Wire.Which.PRESENCE) {
            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.notifyData();
            }
        }
    }
}
