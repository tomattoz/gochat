package red.tel.chat;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import red.tel.chat.EventBus.Event;
import red.tel.chat.generated_protobuf.Haber;
import red.tel.chat.generated_protobuf.Contact;

public class Model {

    private static final String TAG = "Model";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static Map<String, Contact> roster = new HashMap<>();
    private static List<String> texts = new ArrayList<>();

    public static List<String> getContacts() {
        return roster.values().stream().map(contact -> contact.name).collect(Collectors.toList());
    }

    public static Contact getContact(String name) {
        return roster.get(name);
    }

    public static Boolean isOnline(String name) {
        Contact contact = roster.get(name);
        return contact.online != null && contact.online;
    }

    public static List<String> getTexts() {
        return texts;
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ChatApp.getContext());
    }

    public static String getUsername() {
        return getSharedPreferences().getString(USERNAME, null);
    }
    public static String getPassword() {
        return getSharedPreferences().getString(PASSWORD, null);
    }

    public static void setUsername(String username) {
        getSharedPreferences().edit().putString(USERNAME, username).apply();
    }

    // from Backend
    static void incoming(Haber haber) {
        switch (haber.which) {
            case CONTACTS:
                roster = haber.contacts.stream().collect(Collectors.toMap(c -> c.name, c -> c));
                EventBus.announce(Event.CONTACTS);
                break;
            case TEXT:
                texts.add(haber.payload.utf8());
                Log.d(TAG, "text " + haber.payload.utf8() + ", texts.size = " + texts.size());
                EventBus.announce(Event.TEXT);
                break;
            default:
                Log.e(TAG, "Did not handle incoming " + haber.which);
        }
    }

    public static void setContacts(List<String> names) {
        roster = names.stream().collect(Collectors.toMap(name -> name, name ->
                roster.containsKey(name) ?
                        roster.get(name) :
                        new Contact.Builder().name(name).build()));
        Backend.shared().sendContacts(new ArrayList<>(roster.values()));
    }
}