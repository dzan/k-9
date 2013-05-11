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

import com.fsck.k9.activity.setup.autoconfiguration.EmailConfigurationData.*;
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
    public static Map<String, SocketType> socketTypeMap = new HashMap<String, SocketType>();
    // TODO public static Map<String, RestrictionType> restrictionTypeMap = new HashMap<String, RestrictionType>();

    static {
        // all known authentication types
        authenticationTypeMap.put("plain", AuthenticationType.plain);
        authenticationTypeMap.put("password-cleartext", AuthenticationType.plain);
        authenticationTypeMap.put("secure", AuthenticationType.secure);
        authenticationTypeMap.put("password-encrypted", AuthenticationType.secure);
        authenticationTypeMap.put("NTLM", AuthenticationType.NTLM);
        authenticationTypeMap.put("GSSAPI", AuthenticationType.GSSAPI);
        authenticationTypeMap.put("client-IP-address", AuthenticationType.clientIPaddress);
        authenticationTypeMap.put("TLS-client-cert", AuthenticationType.TLSclientcert);
        authenticationTypeMap.put("none", AuthenticationType.none);

        // known socket types
        socketTypeMap.put("plain", SocketType.plain);
        socketTypeMap.put("SSL", SocketType.SSL);
        socketTypeMap.put("STARTTLS", SocketType.STARTTLS);

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
    private Server mServerInProgress;
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
            if (mServerInProgress != null)
                throw new SAXParseException("Nested server-tags. This is not allowed!", mLocator);
            String type = attributes.getValue(Attribute.TYPE.getXMLStringVersion());
            if (type != null) {
                mServerInProgress = ServerType.toType(type).getServerObject(null);
                if (Tag.toTag(localName) == Tag.INCOMINGSERVER)
                    mEmailConfigurationData.incomingServer.add((IncomingServer)mServerInProgress);
                else if (Tag.toTag(localName) == Tag.OUTGOINGSERVER)
                    mEmailConfigurationData.outgoingServer.add((OutgoingServer)mServerInProgress);
            } else {
                // this should never happen, this file is not formed correctly
                throw new SAXParseException("Incoming|Outgoing-Server tag has no type attribute!", mLocator);
            }
            break;
        }

        // does not have plain text inside
        case CHECKINTERVAL: {
            try {
                ((IncomingServerPOP3)mServerInProgress).checkInterval =
                    Integer.parseInt(attributes.getValue(Attribute.MINUTES.getXMLStringVersion()));
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
                mServerInProgress.hostname = value;
                break;
            case PORT:
                mServerInProgress.port = Integer.parseInt(value);
                break;
            case SOCKETTYPE:
                mServerInProgress.socketType = socketTypeMap.get(value);
                break;
            case USERNAME:
                mServerInProgress.username = value;
                break;
            case AUTHENTICATION:
                mServerInProgress.authentication = authenticationTypeMap.get(value);
                break;

            /*
                Pop3
                    ( casts are safe, checked in startElement method )
             */
            case LEAVEMESSAGESONSERVER:
                ((IncomingServerPOP3)mServerInProgress).leaveMessagesOnServer =
                        Boolean.parseBoolean(value);
                break;
            case DOWNLOADONBIFF:
                ((IncomingServerPOP3)mServerInProgress).downloadOnBiff =
                        Boolean.parseBoolean(value);
                break;
            case DAYSTOLEAVEMESSAGESONSERVER:
                ((IncomingServerPOP3)mServerInProgress).daysToLeaveMessagesOnServer =
                        Integer.parseInt(value);
                break;

            /*
                Outgoing Server extra options
             */
            case ADDTHISSERVER:
                ((OutgoingServerSMTP)mServerInProgress).addThisServer =
                        Boolean.parseBoolean(value);
                break;
            case USEGLOBALPREFERREDSERVER:
                ((OutgoingServerSMTP)mServerInProgress).useGlobalPreferredServer =
                        Boolean.parseBoolean(value);
                break;
            case RESTRICTION:
                ((OutgoingServerSMTP)mServerInProgress).restriction =
                        RestrictionType.clientIPAddress;
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
