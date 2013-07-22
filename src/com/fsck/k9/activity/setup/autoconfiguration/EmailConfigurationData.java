/*
 * Author: dzan ( inspired by donated code from Michael Kröz )
 *
 * This class stores all the configuration data we received for a specific emailprovider.
 * Basically this is a datastructure representing the content of the ispdb XML file.
 */

package com.fsck.k9.activity.setup.autoconfiguration;

import android.os.Parcel;
import android.os.Parcelable;
import com.fsck.k9.mail.AuthenticationType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ServerType;

import java.util.ArrayList;
import java.util.List;

public class EmailConfigurationData implements Parcelable {

    // version info of the data
    public String version;
    public String clientConfigUpdate;

    // General information
    public String id;
    public List<String> domains;
    public String displayName;
    public String displayShortName;
    public String email;

    // Possible servers for this ISP
    public List<ParcelableServerSettings> incomingServer;
    public List<ParcelableServerSettings> outgoingServer;

    // Configuration help/information
    public String identity;
    public List<InputField> inputFields;
    public List<DocumentationBlock> documentation;

    // Keep track of which servers were tried
    private int activeIncoming = 0;
    private int activeOutgoing = 0;

    /*
        Constructors
     */
    public EmailConfigurationData() {
        // initialise the fields
        incomingServer = new ArrayList<ParcelableServerSettings>();
        outgoingServer = new ArrayList<ParcelableServerSettings>();
        domains = new ArrayList<String>();
        documentation = new ArrayList<DocumentationBlock>();
        inputFields = new ArrayList<InputField>();
    }

    public EmailConfigurationData(Parcel parcel) {
        version = parcel.readString();
        clientConfigUpdate = parcel.readString();

        id = parcel.readString();
        domains = parcel.readArrayList(String.class.getClassLoader());
        displayName = parcel.readString();
        displayShortName = parcel.readString();
        email = parcel.readString();

        incomingServer = parcel.readArrayList(ParcelableServerSettings.class.getClassLoader());
        outgoingServer = parcel.readArrayList(ParcelableServerSettings.class.getClassLoader());

        identity = parcel.readString();
        inputFields = parcel.readArrayList(InputField.class.getClassLoader());
        documentation = parcel.readArrayList(DocumentationBlock.class.getClassLoader());

        activeIncoming = parcel.readInt();
        activeOutgoing = parcel.readInt();
    }

    public ParcelableServerSettings getActiveIncoming() {
        if (incomingServer.isEmpty()) {
            incomingServer.add(new ParcelableServerSettings(ServerType.UNSET));
        }
        return incomingServer.get(activeIncoming);
    }

    public ParcelableServerSettings getActiveOutgoing() {
        if (outgoingServer.isEmpty()) {
            outgoingServer.add(new ParcelableServerSettings(ServerType.UNSET));
        }
        return outgoingServer.get(activeOutgoing);
    }

    public void setActiveIncoming(int activeIncoming) {
        this.activeIncoming = activeIncoming;
    }

    public void setActiveOutgoing(int activeOutgoing) {
        this.activeOutgoing = activeOutgoing;
    }
    /*******************************************************************************
     Parcelable
     *******************************************************************************/

    public static final Parcelable.Creator<EmailConfigurationData> CREATOR
            = new Parcelable.Creator<EmailConfigurationData>() {

        // using a custom constructor to implement this
        @Override
        public EmailConfigurationData createFromParcel(Parcel parcel) {
            return new EmailConfigurationData(parcel);
        }

        @Override
        public EmailConfigurationData[] newArray(int i) {
            return new EmailConfigurationData[i];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(version);
        parcel.writeString(clientConfigUpdate);

        parcel.writeString(id);
        parcel.writeList(domains);
        parcel.writeString(displayName);
        parcel.writeString(displayShortName);
        parcel.writeString(email);

        parcel.writeList(incomingServer);
        parcel.writeList(outgoingServer);

        parcel.writeString(identity);
        parcel.writeList(inputFields);
        parcel.writeList(documentation);

        parcel.writeInt(activeIncoming);
        parcel.writeInt(activeOutgoing);
    }

    /*******************************************************************************
        Define types for some of the data
     *******************************************************************************/

    public static enum RestrictionType { clientIPAddress };
    public static enum InformationType { ENABLE, DOCUMENTATION }

    /*******************************************************************************
        Server types hierarchy
    *******************************************************************************/
    /*
        Abstract superclass
     */
    public static class ParcelableServerSettings extends ServerSettings implements Parcelable {

        public ParcelableServerSettings(ServerType type) {
            super(type);
            if (type != null)
                this.type = type;
            else this.type = ServerType.UNSET;
        }

        public ParcelableServerSettings(Parcel parcel, ServerType type) {
            this(type);
            host = parcel.readString();
            port = parcel.readInt();
            connectionSecurity = ConnectionSecurity.valueOf(parcel.readString());
            username = parcel.readString();
            password = parcel.readString();
            authenticationType = AuthenticationType.valueOf(parcel.readString());
            extra = parcel.readBundle();
        }

        @Override
        public int describeContents() {
            return this.hashCode();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            if (connectionSecurity == null) connectionSecurity = ConnectionSecurity.NONE;
            if (type == null) type = ServerType.UNSET;
            if (authenticationType == null) authenticationType = AuthenticationType.NONE;

            parcel.writeString(host);
            parcel.writeInt(port);
            parcel.writeString(connectionSecurity.name());
            parcel.writeString(username);
            parcel.writeString(password);
            parcel.writeString(authenticationType.name());
            parcel.writeBundle(extra);
        }
    }

    /*******************************************************************************
        Misc datacontainers
     *******************************************************************************/
    public static class InputField implements Parcelable {

        public static final Parcelable.Creator<InputField> CREATOR
        = new Parcelable.Creator<InputField>() {
            @Override
            public InputField createFromParcel(Parcel parcel) {
                return new InputField(parcel);
            }
            @Override
            public InputField[] newArray(int i) {
                return new InputField[i];
            }
        };

        public String key;
        public String label;
        public String text;

        public InputField(Parcel parcel) {
            if (parcel != null) {
                key = parcel.readString();
                label = parcel.readString();
                text = parcel.readString();
            }
        }

        public InputField(String key, String label, String text) {
            this.key = key;
            this.label = label;
            this.text = text;
        }

        @Override
        public int describeContents() {
            return hashCode();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(key);
            parcel.writeString(label);
            parcel.writeString(text);
        }
    }

    /*
        Used for instructions embedded in the ispdb xml files.
        Contains an url and a list of <language, text> pairs
     */
    public static class DocumentationBlock implements Parcelable {

        // fields
        public String url;
        public InformationType type;

        // first one is the language, second the text
        public ArrayList<String> languages = new ArrayList<String>();
        public ArrayList<String> descriptions = new ArrayList<String>();

        public DocumentationBlock(Parcel parcel) {
            if (parcel != null) {
                url = parcel.readString();
                languages = parcel.createStringArrayList();
                descriptions = parcel.createStringArrayList();
                type = InformationType.valueOf(parcel.readString());
            }
        }

        public DocumentationBlock(String url, InformationType type) {
            this.url = url;
            this.type = type;
        }

        @Override
        public int describeContents() {
            return hashCode();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(url);
            parcel.writeStringList(languages);
            parcel.writeStringList(descriptions);
            parcel.writeString(type.name());
        }

        public boolean isEmpty() {
            if (!!url.isEmpty() || languages.size() > 0)
                return true;
            else return false;
        }

        public static final Parcelable.Creator<DocumentationBlock> CREATOR
                = new Parcelable.Creator<DocumentationBlock>() {
            @Override
            public DocumentationBlock createFromParcel(Parcel parcel) {
                return new DocumentationBlock(parcel);
            }
            @Override
            public DocumentationBlock[] newArray(int i) {
                return new DocumentationBlock[i];
            }
        };
    }
}
