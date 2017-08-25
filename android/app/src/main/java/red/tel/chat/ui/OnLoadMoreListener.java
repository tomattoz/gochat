package red.tel.chat.ui;


public interface OnLoadMoreListener {
    void onLoadMore(int nextPage);

    void onClickItemAdapter(String name);

    void onDeleteContact(int position);
}
