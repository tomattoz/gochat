package red.tel.chat;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocketState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okio.ByteString;
import red.tel.chat.generated_protobuf.Contact;
import red.tel.chat.generated_protobuf.Login;
import red.tel.chat.generated_protobuf.Store;
import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.generated_protobuf.Wire;
import red.tel.chat.office365.Constants;

import static red.tel.chat.generated_protobuf.Wire.Which.CONTACTS;
import static red.tel.chat.generated_protobuf.Wire.Which.HANDSHAKE;
import static red.tel.chat.generated_protobuf.Wire.Which.LOGIN;
import static red.tel.chat.generated_protobuf.Wire.Which.PAYLOAD;
import static red.tel.chat.generated_protobuf.Wire.Which.PUBLIC_KEY;
import static red.tel.chat.generated_protobuf.Wire.Which.PUBLIC_KEY_RESPONSE;
import static red.tel.chat.notification.NotificationUtils.ANDROID_CHANNEL_ID;

// shuttles data between Network and Model
public class WireBackend extends IntentService {

    private static final String TAG = "WireBackend";
    private static WireBackend instance;

    public WireBackend() {
        super(TAG);
    }

    private Network network;
    private String sessionId;
    private Crypto crypto;
    private Map<String, ArrayList<Hold>> queue = new HashMap<>();

    public static WireBackend shared() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        network = Network.getInstance();
        network.onInitConnectServer();
        Log.d(TAG, "re login: onCreate..............");
        EventBus.listenFor(this, EventBus.Event.CONNECTED, () -> {
            int typeLogin = Model.shared().getTypeLogin();
            String username = Model.shared().getUsername();
            String authenToken = Model.shared().getAccessToken();
            String deviceToken = Model.shared().getTokenPushNotification();
            if (username != null && deviceToken != null) {
                WireBackend.this.login(typeLogin, username, authenToken, deviceToken);
                Log.d(TAG, "re login: ");
            }
        });
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //fixed bugs run the service in the background state is not allowed because an IllegalStateException is thrown on Android O
        if (intent != null && Objects.equals(intent.getAction(), Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setOngoing(true).build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }
    }

    // receive from Network
    void onReceiveFromServer(byte[] binary) {
        try {
            Wire wire = Wire.ADAPTER.decode(binary);
            Log.d(TAG, "incoming " + wire.which + " from server");

            if (sessionId == null && wire.sessionId != null) {
                authenticated(wire.sessionId);
            }

            switch (wire.which) {
                case CONTACTS:
                case PRESENCE:
                    Model.shared().incomingFromServer(wire);
                    break;
                case STORE:
                    onReceiveStore(wire);
                    break;
                case HANDSHAKE:
                case PAYLOAD:
                    crypto.onReceivePayload(wire.payload.toByteArray(), wire.from);
                    break;
                case PUBLIC_KEY:
                case PUBLIC_KEY_RESPONSE:
                    onPublicKey(wire);
                    break;
                case LOGIN_RESPONSE:
                    RxBus.getInstance().sendEvent(EventBus.Event.LOGIN_RESPONSE);
                    break;
                case LOGIN:
                    Log.d(TAG, "login : ");
                    break;
                default:
                    break;
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getLocalizedMessage());
        }
    }

    // tell the server to store data
    void sendStore(String key, byte[] value) {
        try {
            ByteString encrypted = ByteString.of(crypto.keyDerivationEncrypt(value));
            ByteString keyBytes = ByteString.encodeUtf8(key);
            Store store = new Store.Builder().key(keyBytes).build();
            Wire.Builder wireBuilder = new Wire.Builder().store(store).which(Wire.Which.STORE).payload(encrypted);
            buildAndSend(wireBuilder);
        } catch (Exception exception) {
            Log.e(TAG, exception.getLocalizedMessage());
        }
    }

    // request the server to send back stored data
    void sendLoad(String key) {
        ByteString payload = ByteString.encodeUtf8(key);
        Wire.Builder wireBuilder = new Wire.Builder().which(Wire.Which.LOAD).payload(payload);
        buildAndSend(wireBuilder);
    }

    // the server sent back stored data, due to a LOAD request
    private void onReceiveStore(Wire wire) throws Exception {
        byte[] value = crypto.keyDerivationDecrypt(wire.payload.toByteArray());
        Model.shared().onReceiveStore(wire.store.key.utf8(), value);
    }

    private void onPublicKey(Wire wire) throws Exception {
        crypto.setPublicKey(
                wire.payload.toByteArray(),
                wire.from,
                wire.which == Wire.Which.PUBLIC_KEY_RESPONSE);
    }

    private void authenticated(String sessionId) {
        try {
            crypto = new Crypto(Model.shared().getUsername(), Model.shared().getPassword());
        } catch (Exception exception) {
            Log.e(TAG, exception.getLocalizedMessage());
            return;
        }
        this.sessionId = sessionId;
        EventBus.announce(EventBus.Event.AUTHENTICATED);
        Log.d(TAG, "authenticated: ");
    }

    private class Hold {
        byte[] data;
        String peerId;

        Hold(byte[] data, String peerId) {
            this.data = data;
            this.peerId = peerId;
        }
    }

    private void enqueue(byte[] data, String peerId) {
        if (!queue.containsKey(peerId)) {
            queue.put(peerId, new ArrayList<>());
        }
        Hold hold = new Hold(data, peerId);
        queue.get(peerId).add(hold);
    }

    private void send(byte[] data, String peerId) {
        if (crypto.isSessionEstablishedFor(peerId)) {
            encryptAndSend(data, peerId);
        } else {
            enqueue(data, peerId);
        }
    }

    private void encryptAndSend(byte[] data, String peerId) {
        try {
            ByteString encrypted = ByteString.of(crypto.encrypt(data, peerId));
            Wire.Builder payloadBuilder = new Wire.Builder().payload(encrypted).which(PAYLOAD).to(peerId);
            buildAndSend(payloadBuilder);
            Log.d(TAG, "encryptAndSend: ");
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    private void buildAndSend(Wire.Builder wireBuilder) {
        wireBuilder.sessionId = sessionId;
        send(wireBuilder.build());
    }

    private void send(Wire wire) {
        if (network.getWebSocket() != null && network.getWebSocket().getState() == WebSocketState.OPEN) {
            network.send(wire.encode());
        } else {
            EventBus.announce(EventBus.Event.DISCONNECTED);
        }
    }

    // send to Network
    public void login(int type, String username, String authenToken, String deviceToken) {
        Login login = new Login
                .Builder()
                .type(type)
                .userName(username)
                .authenToken(authenToken)
                .deviceToken(deviceToken)
                .build();
        Wire.Builder wire = new Wire.Builder().which(LOGIN).login(login);
        instance.buildAndSend(wire);
    }

    public void sendContacts(List<Contact> contacts) {
        Wire.Builder wire = new Wire.Builder().which(CONTACTS).contacts(contacts);
        instance.buildAndSend(wire);
    }

    //send message
    public void sendText(String message, String peerId) {
        okio.ByteString text = okio.ByteString.encodeUtf8(message);
        byte[] data = new Voip.Builder().which(Voip.Which.TEXT).payload(text).build().encode();
        send(data, peerId);
    }

    void sendPublicKey(byte[] key, String recipient, Boolean isResponse) {
        Wire.Which which = isResponse ? PUBLIC_KEY_RESPONSE : PUBLIC_KEY;
        sendData(which, key, recipient);
    }

    private void sendData(Wire.Which which, byte[] data, String recipient) {
        okio.ByteString byteString = ByteString.of(data);
        Wire.Builder wire = new Wire.Builder().which(which).payload(byteString).to(recipient);
        instance.buildAndSend(wire);
    }

    void sendHandshake(byte[] key, String recipient) {
        sendData(HANDSHAKE, key, recipient);
    }

    /**
     *
     * @param peerId
     */
    void handshook(String peerId) {
        ArrayList<Hold> list = queue.get(peerId);
        if (list == null) {
            return;
        }
        for (Hold hold : list) {
            encryptAndSend(hold.data, hold.peerId);
            Log.d(TAG, "handshook: " + hold.peerId);
        }
        queue.clear();//clear the hold list after sending
    }


}
