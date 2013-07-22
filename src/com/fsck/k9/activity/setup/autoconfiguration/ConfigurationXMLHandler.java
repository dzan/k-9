/*
 * Copyright 2013 Sander Bogaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Parses XML files containing information to configure email accounts.
 * These files are writtin according to the Mozilla defined XML format.
 *
 * This class is not made to verify the correctness of XML input! It assumes correct input! And does some small
 * sanity checks.
 *
 * Information urls:
 *  http://viewvc.svn.mozilla.org/vc/mozillamessaging.com/sites/ispdb.mozillamessaging.com/trunk/ispdb/tests/relaxng_schema.1.1.xml?revision=74797&view=markup
 *  This is used by mozilla to verify submissions.
 *
 *  https://wiki.mozilla.org/Thunderbird:Autoconfiguration:ConfigFileFormat
 *  https://developer.mozilla.org/en/Thunderbird/Autoconfiguration/FileFormat/HowTo
 *  ( https://developer.mozilla.org/en/Thunderbird/Autoconfiguration )
 *
 * We do not ( and should not according to the scheme ) support standalone instruction tags.
 */

/*
    NOTE: For now I ignore the clientDescription & clientDescriptionUpdate
    since I have no clue what they are for and doubt they will be useful for k9.

    NOTE: <inputfield> isn't in the relexng scheme, it is in the examples
 */

package com.fsck.k9.activity.setup.autoconfiguration;

// Sax stuff

import android.util.Log;
import com.fsck.k9.activity.setup.autoconfiguration.EmailConfigurationData.*;
import com.fsck.k9.mail.AuthenticationType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ServerType;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

// Types
// Other

public class ConfigurationXMLHandler extends DefaultHandler {

    /*********************************************************************************
                           Data we need to parse
     *********************************************************************************/
    private enum Tag {
        // main structure tags
        CLIENTCONFIG, CLIENTCONFIGUPDATE, EMAILPROVIDER, INCOMINGSERVER, OUTGOINGSERVER,

        // email provider extra information
        DOMAIN, DISPLAYNAME, DISPLAYSHORTNAME, ENABLE, INSTRUCTION, IDENTITY, INPUTFIELD, DOCUMENTATION, DESCR,

        // server settings
        HOSTNAME, PORT, SOCKETTYPE, USERNAME, AUTHENTICATION,

        // pop3 options
        POP3, LEAVEMESSAGESONSERVER, DOWNLOADONBIFF, DAYSTOLEAVEMESSAGESONSERVER, CHECKINTERVAL,

        // outgoing server settings
        RESTRICTION, ADDTHISSERVER, USEGLOBALPREFERREDSERVER,

        // meta
        NO_VALUE, WRONG_TAG,
        NONE, INPUT_FIELD, INFORMATION_STRING;

        public static Tag toTag(String str) {
            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return WRONG_TAG;
            } catch (NullPointerException ex) {
                return NO_VALUE;
            }
        }
    }

    public static String convertUsername(String xmlValue, String address) {
        String[] emailParts = address.split("@");
        String result = null;

        if (xmlValue.equals("%EMAILADDRESS%")) {
            result = address;
        } else if (xmlValue.equals("%EMAILLOCALPART%")) {
            result = emailParts[0];
        } else if (xmlValue.equals("%EMAILDOMAIN%")) {
            result = emailParts[1];
        } else if (xmlValue.equals("%REALNAME%")) {
            throw new UnsupportedOperationException(
                    "Mozilla autoconfiguration xml parser does not support %REALNAME% yet.");
        } else {
            throw new UnsupportedOperationException(
                    "Mozilla autoconfiguration xml parser does not support input fields for usernames yet.");
        }

        return (result == null) ? "" : result;
    }
    private enum Attribute {
        // main structure attributes
        ID, TYPE,

        // email provider extra information
        VISITURL, INSTRUCTION, DESCR, LANG, URL, KEY, LABEL,

        // server settings
        POP3, IMAP, SMTP,

        // pop3 options
        MINUTES,

        // meta
        NO_VALUE, WRONG_TAG;

        public static Attribute toAttribute(String str) {
            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return WRONG_TAG;
            } catch (NullPointerException ex) {
                return NO_VALUE;
            }
        }

        public String getXMLStringVersion() {
            return toString().toLowerCase();
        }
    }

    /*
        Mappings
     */
    public static Map<String, AuthenticationType> authenticationTypeMap = new HashMap<String, AuthenticationType>();
    public static Map<String, ConnectionSecurity> socketTypeMap = new HashMap<String, ConnectionSecurity>();
    public static Map<String, ServerType> serverTypeMap = new HashMap<String, ServerType>();

    // TODO public static Map<String, RestrictionType> restrictionTypeMap = new HashMap<String, RestrictionType>();

    static {
        // all known authentication types
        authenticationTypeMap.put("plain", AuthenticationType.PLAIN);
        authenticationTypeMap.put("password-cleartext", AuthenticationType.PLAIN);
        authenticationTypeMap.put("secure", AuthenticationType.CRAM_MD5);
        authenticationTypeMap.put("password-encrypted", AuthenticationType.CRAM_MD5);
        authenticationTypeMap.put("NTLM", AuthenticationType.NTLM);
        authenticationTypeMap.put("GSSAPI", AuthenticationType.GSSAPI);
        authenticationTypeMap.put("client-IP-address", AuthenticationType.CLIENT_IP);
        authenticationTypeMap.put("TLS-client-cert", AuthenticationType.TLS_CLIENT_CERT);
        authenticationTypeMap.put("none", AuthenticationType.NONE);

        // known socket types
        socketTypeMap.put("plain", ConnectionSecurity.NONE);
        socketTypeMap.put("SSL", ConnectionSecurity.SSL_TLS_REQUIRED);
        socketTypeMap.put("STARTTLS", ConnectionSecurity.STARTTLS_REQUIRED);

        // mapping xml strings to server types
        serverTypeMap.put("imap", ServerType.IMAP);
        serverTypeMap.put("pop3", ServerType.POP3);
        serverTypeMap.put("smtp", ServerType.SMTP);

        // restriction types
        //restrictionTypeMap.put("clientIPAddress", RestrictionType.clientIPAddress);
    }

    private Tag mParseContext = Tag.NONE;

    /*********************************************************************************
                          Parsing routine
     ********************************************************************************/
    // Main object we are filling
    private EmailConfigurationData mEmailConfigurationData;

    // Helper objects during parsing
    private ParcelableServerSettings mServerInProgress;
    private InputField mInputFieldInProgress;
    private DocumentationBlock mInformationBlockInProgress;

    // Other stuff
    private Locator mLocator;

    // Getter for the data after it's parsed
    public EmailConfigurationData getAutoconfigInfo() {
        return mEmailConfigurationData;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.mLocator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        mEmailConfigurationData = new EmailConfigurationData();
    }

    @Override
    public void endDocument() throws SAXException {
        /*
            Adding some checks here too to see if we have useable data
            TODO: add more
         */
        if (!(mEmailConfigurationData.outgoingServer.size() > 0 || mEmailConfigurationData.incomingServer.size() > 0))
            throw new SAXException("Unusable server data, no incoming or outgoing servers found.");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (Tag.toTag(localName)) {
        case INCOMINGSERVER:
        case OUTGOINGSERVER:
            // we can use this to check for illegal nesting of server tags in the startElement method
            mServerInProgress = null;
            break;
        case INPUTFIELD:
            mInputFieldInProgress = null;
            break;
        case ENABLE:
        case DOCUMENTATION:
            mInformationBlockInProgress = null;
            break;
        default:
            return;
        }
    }

    /*
        Place where all the magic happens.
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        Tag tag = Tag.toTag(localName);
        switch (tag) {

        /*
            Email Provider basic information
        */
        case CLIENTCONFIG:
            break; // ignore this for now
        case EMAILPROVIDER:
            mEmailConfigurationData.id = attributes.getValue(Attribute.ID.getXMLStringVersion());
            break;

        /*
            Start server description
            does not have plain text inside
        */
        case INCOMINGSERVER: case OUTGOINGSERVER: {
            if (mServerInProgress != null) {
                throw new SAXParseException("Nested server-tags. This is not allowed!", mLocator);
            }

            String typeString = attributes.getValue(Attribute.TYPE.getXMLStringVersion());
            ServerType type = serverTypeMap.get(typeString);

            if (type == null) {
                throw new SAXParseException("Unknown servertype found: " + typeString, mLocator);
            }

            mServerInProgress = new ParcelableServerSettings(type);
            switch (type) {
                case IMAP:  // fall through
                case POP3:
                    mEmailConfigurationData.incomingServer.add(mServerInProgress);
                    break;
                case SMTP:
                    mEmailConfigurationData.outgoingServer.add(mServerInProgress);
                    break;
                default:
                    // should never happen
            }
            break;
        }

        // does not have plain text inside
        case CHECKINTERVAL: {
            try {
                if (mServerInProgress.type != ServerType.POP3) {
                    throw new SAXParseException("'Check interval' should only occur with POP3 servers.", mLocator);
                }

                int interval = Integer.parseInt(attributes.getValue(Attribute.MINUTES.getXMLStringVersion()));
                mServerInProgress.extra.putInt(ServerSettings.POP3_CHECK_INTERVAL, interval);
            } catch (NumberFormatException ex) {
                throw new SAXParseException("Value of the minutes attribute was not an integer!", mLocator);
            }
            break;
        }

        case INPUTFIELD: {
            if (mInputFieldInProgress != null) {
                throw new SAXParseException("Nested inputField-tags. This is not allowed!", mLocator);
            }
            mInputFieldInProgress = new InputField(
                attributes.getValue(Attribute.KEY.getXMLStringVersion()),
                attributes.getValue(Attribute.LABEL.getXMLStringVersion()),
                null);

            mEmailConfigurationData.inputFields.add(mInputFieldInProgress);
            mParseContext = Tag.INPUTFIELD;
            break;
        }

        // does not have plain text inside
        case ENABLE: {
            if (mInformationBlockInProgress != null) {
                throw new SAXParseException("Illegal nesting of enable-tags, documentation-tags or both.", mLocator);
            }

            mInformationBlockInProgress = new DocumentationBlock(
                attributes.getValue(Attribute.VISITURL.getXMLStringVersion()),
                InformationType.ENABLE);

            mEmailConfigurationData.documentation.add(mInformationBlockInProgress);
            break;
        }

        // does not have plain text inside
        case DOCUMENTATION: {
            if (mInformationBlockInProgress != null) {
                throw new SAXParseException("Illegal nesting of enable-tags, documentation-tags or both.", mLocator);
            }

            mInformationBlockInProgress = new DocumentationBlock(
                    attributes.getValue(Attribute.URL.getXMLStringVersion()),
                    InformationType.DOCUMENTATION);

            mEmailConfigurationData.documentation.add(mInformationBlockInProgress);
        }

        // these have a identical structure
        case INSTRUCTION: case DESCR: {
            mInformationBlockInProgress.languages.add(attributes.getValue(Attribute.LANG.getXMLStringVersion()));
            mParseContext = Tag.INFORMATION_STRING;
            break;
        }

        // does not have plain text inside
        case CLIENTCONFIGUPDATE:
            mEmailConfigurationData.clientConfigUpdate = attributes.getValue(Attribute.URL.getXMLStringVersion());
            break;

        /*
            w00t
        */
        case NO_VALUE:
        case WRONG_TAG:
            throw new SAXParseException("Illegal or unknown tag found.", mLocator);

        default:
            mParseContext = tag;
        }
    }

    private void checkServerType(ServerType type, ServerType requiredType, Tag tag) throws SAXParseException {
        if (type != requiredType) {
            throw new SAXParseException("'" + tag + "' should only occur with " +
                    requiredType + " servers, current server is " + type, mLocator);
        }
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        String value = new String(ch, start, length);

        switch (mParseContext) {
            /*
                General email provider info
             */
            case DOMAIN:
                mEmailConfigurationData.domains.add(value);
                break;
            case DISPLAYNAME:
                mEmailConfigurationData.displayName = value;
                break;
            case DISPLAYSHORTNAME:
                mEmailConfigurationData.displayShortName = value;
                break;

            /*
                Incoming Server
             */
            case HOSTNAME:
                mServerInProgress.host = value;
                break;
            case PORT:
                mServerInProgress.port = Integer.parseInt(value);
                break;
            case SOCKETTYPE:
                mServerInProgress.connectionSecurity = socketTypeMap.get(value);
                break;
            case USERNAME:
                mServerInProgress.username = value;
                break;
            case AUTHENTICATION:
                mServerInProgress.authenticationType = authenticationTypeMap.get(value);
                break;

            /*
                Pop3
             */
            case LEAVEMESSAGESONSERVER:
                checkServerType(mServerInProgress.type, ServerType.POP3, mParseContext);
                mServerInProgress.extra.putBoolean(ServerSettings.POP3_LEAVE_MESSAGES_ON_SERVER, Boolean.parseBoolean(value));
                break;
            case DOWNLOADONBIFF:
                checkServerType(mServerInProgress.type, ServerType.POP3, mParseContext);
                mServerInProgress.extra.putBoolean(ServerSettings.POP3_DOWNLOAD_ON_BIFF, Boolean.parseBoolean(value));
                break;
            case DAYSTOLEAVEMESSAGESONSERVER:
                checkServerType(mServerInProgress.type, ServerType.POP3, mParseContext);
                mServerInProgress.extra.putInt(ServerSettings.POP3_DAYS_TO_LEAVE_MESSAGES_ON_SERVER, Integer.parseInt(value));
                break;

            /*
                Outgoing Server extra options
             */
            case ADDTHISSERVER:
                checkServerType(mServerInProgress.type, ServerType.SMTP, mParseContext);
                mServerInProgress.extra.putBoolean(ServerSettings.SMTP_ADD_THIS_SERVER, Boolean.parseBoolean(value));
                break;
            case USEGLOBALPREFERREDSERVER:
                checkServerType(mServerInProgress.type, ServerType.SMTP, mParseContext);
                mServerInProgress.extra.putBoolean(ServerSettings.SMTP_USE_GLOBAL_PREFERRED_SERVER, Boolean.parseBoolean(value));
                break;
            case RESTRICTION:
                checkServerType(mServerInProgress.type, ServerType.SMTP, mParseContext);
                mServerInProgress.extra.putString(ServerSettings.SMTP_RESTRICTION, RestrictionType.clientIPAddress.toString());
                break;

            /*
                Documentation fields etc
             */
            case INPUTFIELD:
                mInputFieldInProgress.text = value;
                break;
            case INFORMATION_STRING:
                mInformationBlockInProgress.descriptions.add(value);
                break;

            default:
        }

        mParseContext = Tag.NONE;
    }
}
