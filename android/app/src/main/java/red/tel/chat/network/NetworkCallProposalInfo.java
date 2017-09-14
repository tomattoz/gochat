package red.tel.chat.network;


import android.os.Parcel;
import android.os.Parcelable;
//info call proposa
public class NetworkCallProposalInfo implements Parcelable {
    public static final Creator<NetworkCallProposalInfo> CREATOR = new Creator<NetworkCallProposalInfo>() {
        @Override
        public NetworkCallProposalInfo createFromParcel(Parcel source) {
            return new NetworkCallProposalInfo(source);
        }

        @Override
        public NetworkCallProposalInfo[] newArray(int size) {
            return new NetworkCallProposalInfo[size];
        }
    };
    public String id;
    public String from;
    public String to;
    public boolean audio;
    public boolean video;

    public NetworkCallProposalInfo(String id, String from, String to, boolean audio, boolean video) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.audio = audio;
        this.video = video;
    }

    protected NetworkCallProposalInfo(Parcel in) {
        this.id = in.readString();
        this.from = in.readString();
        this.to = in.readString();
        this.audio = in.readByte() != 0;
        this.video = in.readByte() != 0;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isAudio() {
        return audio;
    }

    public boolean isVideo() {
        return video;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeByte(this.audio ? (byte) 1 : (byte) 0);
        dest.writeByte(this.video ? (byte) 1 : (byte) 0);
    }
}
