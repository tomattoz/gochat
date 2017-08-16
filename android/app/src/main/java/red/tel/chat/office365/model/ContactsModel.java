package red.tel.chat.office365.model;

import com.google.gson.annotations.SerializedName;

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

    private static class DataContacts {
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
    }
}
