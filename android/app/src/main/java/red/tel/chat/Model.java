package red.tel.chat;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import okio.ByteString;
import red.tel.chat.EventBus.Event;
import red.tel.chat.generated_protobuf.Wire;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.generated_protobuf.Contact;
import red.tel.chat.generated_protobuf.Text;

import static red.tel.chat.generated_protobuf.Wire.Which.PRESENCE;

public class Model {

    private static final String TAG = "Model";
    private static final String TEXTS = "texts";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String AUTHOR = "authorization";
    private static final String TYPE_LOGIN = "type_login";
    private static final String TOKEN_PUSH_NOTIFICATION = "token_push";
    private Map<String, Contact> roster = new HashMap<>();
    private List<red.tel.chat.generated_protobuf.Text> texts = new ArrayList<>();
    private static Model instance;

    public static Model shared() {
        if (instance == null) {
            instance = new Model();
            EventBus.listenFor(ChatApp.getContext(), Event.AUTHENTICATED, () -> Backend.shared().sendLoad(TEXTS));
        }
        return instance;
    }

    public List<String> getContacts() {
        return roster.values().stream().map(contact -> contact.id).collect(Collectors.toList());
    }

    public Boolean isOnline(String name) {
        Contact contact = roster.get(name);
        return contact != null && contact.online != null && contact.online;
    }

    public List<Text> getTexts() {
        return texts;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ChatApp.getContext());
    }

    public String getUsername() {
        return getSharedPreferences().getString(USERNAME, null);
    }

    String getPassword() {
        return getSharedPreferences().getString(PASSWORD, null);
    }

    public String getAccessToken() {
        return getSharedPreferences().getString(AUTHOR, null);
    }

    public void setUsername(String username) {
        getSharedPreferences().edit().putString(USERNAME, username).apply();
    }

    public void setPassword(String username) {
        getSharedPreferences().edit().putString(PASSWORD, username).apply();
    }

    public void setAccessToken(String author) {
        getSharedPreferences().edit().putString(AUTHOR, author).apply();
    }

    public void setTypeLogin(int type) {
        getSharedPreferences().edit().putInt(TYPE_LOGIN, type).apply();
    }

    public int getTypeLogin() {
        return getSharedPreferences().getInt(TYPE_LOGIN, 0);
    }

    public void setTokenPushNotification(String token) {
        getSharedPreferences().edit().putString(TOKEN_PUSH_NOTIFICATION, token).apply();
    }

    public String getTokenPushNotification() {
        return getSharedPreferences().getString(TOKEN_PUSH_NOTIFICATION, null);
    }

    void incomingFromServer(Wire wire) {
        switch (wire.which) {
            case CONTACTS:
                roster = wire.contacts.stream().collect(Collectors.toMap(c -> c.id, c -> c));
                EventBus.announce(Event.CONTACTS);
                break;
            case PRESENCE:
                roster = wire.contacts.stream().collect(Collectors.toMap(c -> c.id, c -> c));
                RxBus.getInstance().sendEvent(PRESENCE);
                break;
            default:
                Log.e(TAG, "Did not handle incoming " + wire.which);
        }
    }

    void incomingFromPeer(Voip voip, String peerId) {
        switch (voip.which) {
            case TEXT:
                addText(voip.payload.utf8(), peerId, getUsername());
                Log.d(TAG, "text " + voip.payload.utf8() + ", texts.size = " + texts.size());
                EventBus.announce(Event.TEXT);
                break;
            default:
                Log.e(TAG, "Did not handle incoming " + voip.which);
        }
    }

    public void addText(String body, String from, String to) {
        Text text = new Text.Builder().body(ByteString.encodeUtf8(body)).from(from).to(to).build();
        texts.add(text);
        storeTexts();
    }

    private void storeTexts() {
        byte[] data = new Voip.Builder().textStorage(texts).build().encode();
        Backend.shared().sendStore(TEXTS, data);
    }

    void onReceiveStore(String key, byte[] value) throws Exception {
        if (key.equals(TEXTS)) {
            Voip parsed = Voip.ADAPTER.decode(value);
            texts = new ArrayList<>(parsed.textStorage);
            EventBus.announce(Event.TEXT);
        } else {
            Log.e(TAG, "unsupported key " + key);
        }
    }

    public void setContacts(List<String> names) {
        /*for ( int i = 0; i < names.size()-1; i++ ) {
            for ( int j = i + 1; j < names.size(); j++ ) {
                if ( names.get(i).equals(names.get(j))) {
                    names.remove(i);
                    i--;
                    break;
                }
            }
        }*/

        roster = names.stream().collect(Collectors.toMap(id -> id, id ->
                roster.containsKey(id) ?
                        roster.get(id) :
                        new Contact.Builder().id(id).build(), (contact, contact2) -> contact));
        Backend.shared().sendContacts(new ArrayList<>(roster.values()));
    }

    public static String parseJsonUser(String username, String accessToken, String tokenNotification) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", username);
        jsonObject.addProperty("authenToken", accessToken);
        jsonObject.addProperty("deviceToken", tokenNotification);
        return gson.toJson(jsonObject);
    }
}
