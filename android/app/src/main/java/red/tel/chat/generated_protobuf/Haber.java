// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: wire.proto
package red.tel.chat.generated_protobuf;

import android.os.Parcelable;
import com.squareup.wire.AndroidMessage;
import com.squareup.wire.EnumAdapter;
import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireEnum;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class Haber extends AndroidMessage<Haber, Haber.Builder> {
  public static final ProtoAdapter<Haber> ADAPTER = new ProtoAdapter_Haber();

  public static final Parcelable.Creator<Haber> CREATOR = AndroidMessage.newCreator(ADAPTER);

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_VERSION = 0;

  public static final String DEFAULT_SESSIONID = "";

  public static final String DEFAULT_FROM = "";

  public static final String DEFAULT_TO = "";

  public static final Which DEFAULT_WHICH = Which.LOGIN;

  public static final String DEFAULT_LOGIN = "";

  public static final ByteString DEFAULT_PAYLOAD = ByteString.EMPTY;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer version;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String sessionId;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String from;

  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String to;

  @WireField(
      tag = 5,
      adapter = "red.tel.chat.generated_protobuf.Haber$Which#ADAPTER"
  )
  public final Which which;

  /**
   * One of the following will be filled in
   */
  @WireField(
      tag = 101,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String login;

  /**
   * for roster, presence, and invite
   */
  @WireField(
      tag = 102,
      adapter = "red.tel.chat.generated_protobuf.Contact#ADAPTER",
      label = WireField.Label.REPEATED
  )
  public final List<Contact> contacts;

  @WireField(
      tag = 103,
      adapter = "red.tel.chat.generated_protobuf.File#ADAPTER"
  )
  public final File file;

  @WireField(
      tag = 104,
      adapter = "red.tel.chat.generated_protobuf.Store#ADAPTER"
  )
  public final Store store;

  @WireField(
      tag = 105,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES",
      label = WireField.Label.REPEATED
  )
  public final List<ByteString> raw;

  @WireField(
      tag = 106,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES"
  )
  public final ByteString payload;

  public Haber(Integer version, String sessionId, String from, String to, Which which, String login,
      List<Contact> contacts, File file, Store store, List<ByteString> raw, ByteString payload) {
    this(version, sessionId, from, to, which, login, contacts, file, store, raw, payload, ByteString.EMPTY);
  }

  public Haber(Integer version, String sessionId, String from, String to, Which which, String login,
      List<Contact> contacts, File file, Store store, List<ByteString> raw, ByteString payload,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.version = version;
    this.sessionId = sessionId;
    this.from = from;
    this.to = to;
    this.which = which;
    this.login = login;
    this.contacts = Internal.immutableCopyOf("contacts", contacts);
    this.file = file;
    this.store = store;
    this.raw = Internal.immutableCopyOf("raw", raw);
    this.payload = payload;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.version = version;
    builder.sessionId = sessionId;
    builder.from = from;
    builder.to = to;
    builder.which = which;
    builder.login = login;
    builder.contacts = Internal.copyOf("contacts", contacts);
    builder.file = file;
    builder.store = store;
    builder.raw = Internal.copyOf("raw", raw);
    builder.payload = payload;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Haber)) return false;
    Haber o = (Haber) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(version, o.version)
        && Internal.equals(sessionId, o.sessionId)
        && Internal.equals(from, o.from)
        && Internal.equals(to, o.to)
        && Internal.equals(which, o.which)
        && Internal.equals(login, o.login)
        && contacts.equals(o.contacts)
        && Internal.equals(file, o.file)
        && Internal.equals(store, o.store)
        && raw.equals(o.raw)
        && Internal.equals(payload, o.payload);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (version != null ? version.hashCode() : 0);
      result = result * 37 + (sessionId != null ? sessionId.hashCode() : 0);
      result = result * 37 + (from != null ? from.hashCode() : 0);
      result = result * 37 + (to != null ? to.hashCode() : 0);
      result = result * 37 + (which != null ? which.hashCode() : 0);
      result = result * 37 + (login != null ? login.hashCode() : 0);
      result = result * 37 + contacts.hashCode();
      result = result * 37 + (file != null ? file.hashCode() : 0);
      result = result * 37 + (store != null ? store.hashCode() : 0);
      result = result * 37 + raw.hashCode();
      result = result * 37 + (payload != null ? payload.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (version != null) builder.append(", version=").append(version);
    if (sessionId != null) builder.append(", sessionId=").append(sessionId);
    if (from != null) builder.append(", from=").append(from);
    if (to != null) builder.append(", to=").append(to);
    if (which != null) builder.append(", which=").append(which);
    if (login != null) builder.append(", login=").append(login);
    if (!contacts.isEmpty()) builder.append(", contacts=").append(contacts);
    if (file != null) builder.append(", file=").append(file);
    if (store != null) builder.append(", store=").append(store);
    if (!raw.isEmpty()) builder.append(", raw=").append(raw);
    if (payload != null) builder.append(", payload=").append(payload);
    return builder.replace(0, 2, "Haber{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<Haber, Builder> {
    public Integer version;

    public String sessionId;

    public String from;

    public String to;

    public Which which;

    public String login;

    public List<Contact> contacts;

    public File file;

    public Store store;

    public List<ByteString> raw;

    public ByteString payload;

    public Builder() {
      contacts = Internal.newMutableList();
      raw = Internal.newMutableList();
    }

    public Builder version(Integer version) {
      this.version = version;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder from(String from) {
      this.from = from;
      return this;
    }

    public Builder to(String to) {
      this.to = to;
      return this;
    }

    public Builder which(Which which) {
      this.which = which;
      return this;
    }

    /**
     * One of the following will be filled in
     */
    public Builder login(String login) {
      this.login = login;
      return this;
    }

    /**
     * for roster, presence, and invite
     */
    public Builder contacts(List<Contact> contacts) {
      Internal.checkElementsNotNull(contacts);
      this.contacts = contacts;
      return this;
    }

    public Builder file(File file) {
      this.file = file;
      return this;
    }

    public Builder store(Store store) {
      this.store = store;
      return this;
    }

    public Builder raw(List<ByteString> raw) {
      Internal.checkElementsNotNull(raw);
      this.raw = raw;
      return this;
    }

    public Builder payload(ByteString payload) {
      this.payload = payload;
      return this;
    }

    @Override
    public Haber build() {
      return new Haber(version, sessionId, from, to, which, login, contacts, file, store, raw, payload, super.buildUnknownFields());
    }
  }

  /**
   * Identifies which field is filled in
   */
  public enum Which implements WireEnum {
    LOGIN(0),

    CONTACTS(1),

    PRESENCE(2),

    TEXT(3),

    FILE(4),

    AV(5),

    STORE(6),

    LOAD(7),

    PUBLIC_KEY(8),

    PUBLIC_KEY_RESPONSE(9),

    HANDSHAKE(10),

    PAYLOAD(11);

    public static final ProtoAdapter<Which> ADAPTER = new ProtoAdapter_Which();

    private final int value;

    Which(int value) {
      this.value = value;
    }

    /**
     * Return the constant for {@code value} or null.
     */
    public static Which fromValue(int value) {
      switch (value) {
        case 0: return LOGIN;
        case 1: return CONTACTS;
        case 2: return PRESENCE;
        case 3: return TEXT;
        case 4: return FILE;
        case 5: return AV;
        case 6: return STORE;
        case 7: return LOAD;
        case 8: return PUBLIC_KEY;
        case 9: return PUBLIC_KEY_RESPONSE;
        case 10: return HANDSHAKE;
        case 11: return PAYLOAD;
        default: return null;
      }
    }

    @Override
    public int getValue() {
      return value;
    }

    private static final class ProtoAdapter_Which extends EnumAdapter<Which> {
      ProtoAdapter_Which() {
        super(Which.class);
      }

      @Override
      protected Which fromValue(int value) {
        return Which.fromValue(value);
      }
    }
  }

  private static final class ProtoAdapter_Haber extends ProtoAdapter<Haber> {
    public ProtoAdapter_Haber() {
      super(FieldEncoding.LENGTH_DELIMITED, Haber.class);
    }

    @Override
    public int encodedSize(Haber value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.version)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.sessionId)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.from)
          + ProtoAdapter.STRING.encodedSizeWithTag(4, value.to)
          + Which.ADAPTER.encodedSizeWithTag(5, value.which)
          + ProtoAdapter.STRING.encodedSizeWithTag(101, value.login)
          + Contact.ADAPTER.asRepeated().encodedSizeWithTag(102, value.contacts)
          + File.ADAPTER.encodedSizeWithTag(103, value.file)
          + Store.ADAPTER.encodedSizeWithTag(104, value.store)
          + ProtoAdapter.BYTES.asRepeated().encodedSizeWithTag(105, value.raw)
          + ProtoAdapter.BYTES.encodedSizeWithTag(106, value.payload)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, Haber value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.version);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.sessionId);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.from);
      ProtoAdapter.STRING.encodeWithTag(writer, 4, value.to);
      Which.ADAPTER.encodeWithTag(writer, 5, value.which);
      ProtoAdapter.STRING.encodeWithTag(writer, 101, value.login);
      Contact.ADAPTER.asRepeated().encodeWithTag(writer, 102, value.contacts);
      File.ADAPTER.encodeWithTag(writer, 103, value.file);
      Store.ADAPTER.encodeWithTag(writer, 104, value.store);
      ProtoAdapter.BYTES.asRepeated().encodeWithTag(writer, 105, value.raw);
      ProtoAdapter.BYTES.encodeWithTag(writer, 106, value.payload);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public Haber decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.version(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.sessionId(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.from(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.to(ProtoAdapter.STRING.decode(reader)); break;
          case 5: {
            try {
              builder.which(Which.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 101: builder.login(ProtoAdapter.STRING.decode(reader)); break;
          case 102: builder.contacts.add(Contact.ADAPTER.decode(reader)); break;
          case 103: builder.file(File.ADAPTER.decode(reader)); break;
          case 104: builder.store(Store.ADAPTER.decode(reader)); break;
          case 105: builder.raw.add(ProtoAdapter.BYTES.decode(reader)); break;
          case 106: builder.payload(ProtoAdapter.BYTES.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public Haber redact(Haber value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.contacts, Contact.ADAPTER);
      if (builder.file != null) builder.file = File.ADAPTER.redact(builder.file);
      if (builder.store != null) builder.store = Store.ADAPTER.redact(builder.store);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
