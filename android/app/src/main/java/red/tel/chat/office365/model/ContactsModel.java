package red.tel.chat.office365.model;

import com.google.gson.annotations.SerializedName;
import com.microsoft.graph.extensions.EmailAddress;

import java.util.List;


public class ContactsModel {
    @SerializedName("@odata.nextLink")
    private String nextLink;
    @SerializedName("value")
    private List<DataContacts> dataContacts;

    public String getNextLink() {
        return nextLink;
    }

    public List<DataContacts> getDataContacts() {
        return dataContacts;
    }

    public int getNexPage() {
        return getNextLink() != null ? Integer.parseInt(getNextLink().substring(getNextLink().indexOf("=")+1)) : 0;
    }

    public static class DataContacts {
        @SerializedName("id")
        private String id;
        @SerializedName("createdDateTime")
        private String createdDateTime;
        @SerializedName("lastModifiedDateTime")
        private String lastModifiedDateTime;
        @SerializedName("fileAs")
        private String fileAs;
        @SerializedName("displayName")
        private String displayName;
        @SerializedName("givenName")
        private String givenName;
        @SerializedName("emailAddresses")
        private List<EmailAddress> emailAddresses;
        @SerializedName("nickName")
        private String nickName;

        public String getId() {
            return id;
        }

        public String getCreatedDateTime() {
            return createdDateTime;
        }

        public String getLastModifiedDateTime() {
            return lastModifiedDateTime;
        }

        public String getFileAs() {
            return fileAs;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getGivenName() {
            return givenName;
        }

        public List<EmailAddress> getEmailAddresses() {
            return emailAddresses;
        }

        public String getNickName() {
            return nickName;
        }
    }
}
