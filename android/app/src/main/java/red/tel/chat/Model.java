package red.tel.chat;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okio.ByteString;
import red.tel.chat.EventBus.Event;
import red.tel.chat.generated_protobuf.Contact;
import red.tel.chat.generated_protobuf.Text;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.generated_protobuf.Wire;

import static red.tel.chat.generated_protobuf.Wire.Which.CONTACTS;
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

    public List<Contact> getContacts() {
        return new ArrayList<>(roster.values());
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
            case PRESENCE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    roster = wire.contacts.stream().collect(Collectors.toMap(c -> c.id, c -> c));
                } else {
                    for (Contact contact : wire.contacts) {
                        roster.put(contact.id, contact);
                    }
                }
                if (wire.which == CONTACTS) {
                    EventBus.announce(Event.CONTACTS);
                } else if (wire.which == PRESENCE) {
                    RxBus.getInstance().sendEvent(PRESENCE);
                }
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

    public void setContacts(List<Contact> contactList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            roster = contactList.stream().collect(Collectors.toMap(contact -> contact.id, contactFunction ->
                    roster.containsKey(contactFunction.id) ?
                            roster.get(contactFunction.id) :
                            new Contact.Builder().id(contactFunction.id).name(contactFunction.name).build()
                    , (contact, contact2) -> contact));
        } else {
            for (int i = 0; i < contactList.size() - 1; i++) {
                for (int j = i + 1; j < contactList.size(); j++) {
                    if (contactList.get(i).equals(contactList.get(j))) {
                        contactList.remove(i);
                        i--;
                        break;
                    }
                }
            }
            for (Contact contact : contactList) {
                roster.put(contact.id, roster.containsKey(contact.id) ? roster.get(contact.id)
                        : new Contact.Builder().id(contact.id).name(contact.name).build());
            }
        }
        List<Contact> listContact = new ArrayList<>(roster.values());
        Collections.sort(listContact, (contact, t1) -> contact.name.compareTo(t1.name));
        Backend.shared().sendContacts(listContact);
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
