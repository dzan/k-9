/*
 * Author: dzan ( inspired by donated code from Michael Kröz )
 *
 * This class stores all the configuration data we received for a specific emailprovider.
 * Basically this is a datastructure representing the content of the ispdb XML file.
 */

package com.fsck.k9.activity.setup.autoconfiguration;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;
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

    // Possible servers for this ISP
    public List<IncomingServer> incomingServer;
    public List<OutgoingServer> outgoingServer;

    // Configuration help/information
    public String identity;
    public List<InputField> inputFields;
    public List<DocumentationBlock> documentation;


    /*
        Constructors
     */
    public EmailConfigurationData() {
        // initialise the fields
        incomingServer = new ArrayList<IncomingServer>();
        outgoingServer = new ArrayList<OutgoingServer>();
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

        incomingServer = parcel.readArrayList(IncomingServer.class.getClassLoader());
        outgoingServer = parcel.readArrayList(OutgoingServer.class.getClassLoader());

        identity = parcel.readString();
        inputFields = parcel.readArrayList(InputField.class.getClassLoader());
        documentation = parcel.readArrayList(DocumentationBlock.class.getClassLoader());

    }

    public boolean hasExtraInfo() {
        return(documentation.size() > 0);
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

        parcel.writeList(incomingServer);
        parcel.writeList(outgoingServer);

        parcel.writeString(identity);
        parcel.writeList(inputFields);
        parcel.writeList(documentation);
    }

    /*******************************************************************************
        Define types for some of the data
     *******************************************************************************/
    /*
        K-9 only supports plain or CRAM-MD5
        throw exception for all others
    */
    private static final int PLAIN = 0;
    private static final int CRAMMD5 = 1;
    private static final int UNSUPPORTED = 2;
    private static final int NONE = 3;

    public static enum AuthenticationType {
        plain(PLAIN), secure(CRAMMD5), NTLM(UNSUPPORTED), GSSAPI(UNSUPPORTED),
        clientIPaddress(NONE), TLSclientcert(UNSUPPORTED), none(NONE), UNSET(UNSUPPORTED);

        private int type;
        AuthenticationType(int type) {
            this.type = type;
        }

        public String getAuthString() throws UnsupportedEncodingException {
            switch (type) {
            case PLAIN:
                return "PLAIN";
            case CRAMMD5:
                return "CRAM_MD5";
            case NONE:
                return "";
            case UNSUPPORTED: // fall-through
            default:
                throw new UnsupportedEncodingException();
            }
        }
    };

    public static enum SocketType {
        plain(""), SSL("ssl"), STARTTLS("tls"), UNSET("");
        private String schemeName;

        SocketType(String schemeName) {
            this.schemeName = schemeName;
        }

        public String getSchemeName() {
            return schemeName;
        }
    };

    public static enum RestrictionType { clientIPAddress };
    public static enum InformationType { ENABLE, DOCUMENTATION }

    public static enum ServerType {
        IMAP(0, "imap"), POP3(1, "pop3"), SMTP(2, "smtp"), UNSET(3, ""), NO_VALUE(4, ""), WRONG_TAG(5, "");

        private int type;
        private String schemeName;

        ServerType(int type, String schemeName) {
            this.type = type;
            this.schemeName = schemeName;
        }

        public Server getServerObject(Parcel parcel) {
            // ugly but cleanest solution to mix the parcelable and inheritance in the server classes ( user of super() )
            switch (type) {
            case 0:
                if (parcel != null) return new IncomingServerIMAP(parcel);
                else return new IncomingServerIMAP();
            case 1:
                if (parcel != null)return new IncomingServerPOP3(parcel);
                else return new IncomingServerPOP3();
            case 2:
                if (parcel != null) return new OutgoingServerSMTP(parcel);
                else return new OutgoingServerSMTP();
            default:
                return null;
            }
        }

        public static ServerType toType(String str) {
            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return WRONG_TAG;
            } catch (NullPointerException ex) {
                return NO_VALUE;
            }
        }

        public String getSchemeName() {
            return schemeName;
        }
    }


    /*******************************************************************************
        Server types hierarchy
    *******************************************************************************/
    public static abstract class Server implements Parcelable {
        public ServerType type;
        public String hostname;
        public int port;
        public SocketType socketType;
        public String username;
        public AuthenticationType authentication;

        public Server(ServerType type) {
            if (type != null)
                this.type = type;
            else this.type = ServerType.UNSET;
        }

        public Server(Parcel parcel, ServerType type) {
            this(type);
            hostname = parcel.readString();
            port = parcel.readInt();
            socketType = SocketType.valueOf(parcel.readString());
            username = parcel.readString();
            authentication = AuthenticationType.valueOf(parcel.readString());
        }

        @Override
        public int describeContents() {
            return this.hashCode();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            if (socketType == null) socketType = SocketType.UNSET;
            if (type == null) type = ServerType.UNSET;
            if (authentication == null) authentication = AuthenticationType.UNSET;

            parcel.writeString(hostname);
            parcel.writeInt(port);
            parcel.writeString(socketType.name());
            parcel.writeString(username);
            parcel.writeString(authentication.name());
        }
    }

    public static abstract class IncomingServer extends Server {
        public IncomingServer(ServerType type) {
            super(type);
        }
        public IncomingServer(Parcel parcel, ServerType type) {
            super(parcel, type);
        }
    }

    public static abstract class OutgoingServer extends Server {
        public RestrictionType restriction;
        public OutgoingServer(ServerType type) {
            super(type);
        }
        public OutgoingServer(Parcel parcel, ServerType type) {
            super(parcel, type);
        }
    }

    public static class IncomingServerPOP3 extends IncomingServer {
        public static final Parcelable.Creator<IncomingServerPOP3> CREATOR
        = new Parcelable.Creator<IncomingServerPOP3>() {
            @Override
            public IncomingServerPOP3 createFromParcel(Parcel parcel) {
                return new IncomingServerPOP3(parcel);
            }
            @Override
            public IncomingServerPOP3[] newArray(int i) {
                return new IncomingServerPOP3[i];
            }
        };

        // hardcode the type
        public ServerType type = ServerType.POP3;

        // pop3 options
        public boolean leaveMessagesOnServer;
        public boolean downloadOnBiff;
        public int daysToLeaveMessagesOnServer;
        public int checkInterval;

        // constructors
        public IncomingServerPOP3() {
            super(ServerType.POP3);
        }

        public IncomingServerPOP3(Parcel parcel) {
            super(parcel, ServerType.POP3);

            // load in extras
            leaveMessagesOnServer = (parcel.readInt() == 1) ? true : false;
            downloadOnBiff = (parcel.readInt() == 1) ? true : false;
            daysToLeaveMessagesOnServer = parcel.readInt();
            checkInterval = parcel.readInt();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);

            // extra fields in this class
            parcel.writeInt(leaveMessagesOnServer ? 1 : 0);
            parcel.writeInt(downloadOnBiff ? 1 : 0);
            parcel.writeInt(daysToLeaveMessagesOnServer);
            parcel.writeInt(checkInterval);
        }
    }

    public static class IncomingServerIMAP extends IncomingServer {
        public static final Parcelable.Creator<IncomingServerIMAP> CREATOR
        = new Parcelable.Creator<IncomingServerIMAP>() {
            @Override
            public IncomingServerIMAP createFromParcel(Parcel parcel) {
                return new IncomingServerIMAP(parcel);
            }
            @Override
            public IncomingServerIMAP[] newArray(int i) {
                return new IncomingServerIMAP[i];
            }
        };

        public ServerType type = ServerType.IMAP;

        // constructor
        public IncomingServerIMAP() {
            super(ServerType.IMAP);
        }
        public IncomingServerIMAP(Parcel parcel) {
            super(parcel, ServerType.IMAP);
        }
    }

    public static class OutgoingServerSMTP extends OutgoingServer {
        public static final Parcelable.Creator<OutgoingServerSMTP> CREATOR
        = new Parcelable.Creator<OutgoingServerSMTP>() {
            @Override
            public OutgoingServerSMTP createFromParcel(Parcel parcel) {
                return new OutgoingServerSMTP(parcel);
            }
            @Override
            public OutgoingServerSMTP[] newArray(int i) {
                return new OutgoingServerSMTP[i];
            }
        };

        // hardcode the type
        public ServerType type = ServerType.SMTP;

        // SMTP options
        public boolean addThisServer;
        public boolean useGlobalPreferredServer;

        // constructor
        public OutgoingServerSMTP() {
            super(ServerType.SMTP);
        }
        public OutgoingServerSMTP(Parcel parcel) {
            super(parcel, ServerType.SMTP);

            // load in extras
            addThisServer = (parcel.readInt() == 1) ? true : false;
            useGlobalPreferredServer = (parcel.readInt() == 1) ? true : false;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);

            // extra fields in this class
            parcel.writeInt(addThisServer ? 1 : 0);
            parcel.writeInt(useGlobalPreferredServer ? 1 : 0);
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
