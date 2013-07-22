package com.fsck.k9.activity.setup.autoconfiguration;

import android.os.AsyncTask;
import android.util.Log;
import com.fsck.k9.K9;
import com.fsck.k9.helper.Utility;
import com.fsck.k9.mail.ServerSettings;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class IspDbTask extends AsyncTask<String, String, EmailConfigurationData> {

    // Mozilla url's
    private String dbBaseUrl = "https://autoconfig.thunderbird.net/v1.1/";//"http://live.mozillamessaging.com/autoconfig/v1.1/";
    private String dnsMXLookupUrl = "https://live.mozillamessaging.com/dns/mx/";

    /*
        see https://developer.mozilla.org/en/Thunderbird/Autoconfiguration

        note: order they are listed is the order they'll be checked and the same order Thunderbird uses,
        there is no https, see https://bugzilla.mozilla.org/show_bug.cgi?id=534722#c21
    */
    private ArrayList<String> urlTemplates = new ArrayList<String>(Arrays.asList(
            "http://autoconfig.%domain%/mail/config-v1.1.xml?emailaddress=%address%",
            "http://%domain%/.well-known/autoconfig/mail/config-v1.1.xml",
            dbBaseUrl + "%domain%"));

    private HttpClient mHttpClient =  new DefaultHttpClient();

    /*
            Algorithm to fetch and parse configuration data from
            Mozilla's isp database. Also performs a DNS lookup to handle MX redirects.
         */
    @Override
    protected EmailConfigurationData doInBackground(String... params) {
        EmailConfigurationData result = null;

        /*
            Determine domains to check
         */
        String domain = splitEmail(params[0])[1];
        ArrayList<String> domains = new ArrayList<String>();
        domains.add(domain);

        // add dns detected fallbacks
        try{
            domains.addAll(getMXDomains(domain));
            debug("Domains to try: " + Arrays.toString(domains.toArray()));
        } catch (IOException ex) {
            debug("Getting MX domains failed. Moving on without extra domains.\n"
                    + ex.getLocalizedMessage());
        }

        // progress
        publishProgress("DNS done");

        // cancelled?
        if (isCancelled()) {
            return null;
        }

        // iterate over each domain
        int domainIndex = 0;
        while (result == null && domainIndex < domains.size()) {

            // iterate over each url template
            int templateIndex = 0;
            while (result == null && templateIndex < urlTemplates.size()) {
                /*
                    Try to find a XML file
                 */
                String tmpUrl = urlTemplates.get(templateIndex).replaceAll("%domain%",domains.get(domainIndex));
                tmpUrl = tmpUrl.replaceAll("%address%",params[0]);
                debug("Trying " + tmpUrl);

                /*
                    Fetch & parse the XML
                 */
                InputStream xmlData = null;
                HttpURLConnection conn = null;
                try {
                    // fetch
                    conn = (HttpURLConnection) new URL(tmpUrl).openConnection();
                    xmlData = conn.getInputStream();

                    // failed fetch?
                    if (xmlData == null) {
                        ++templateIndex;
                        continue;
                    }

                    // cancelled?
                    if (isCancelled()) {
                        break;
                    }

                    // progress
                    publishProgress("Got data");

                    /*
                        Parse the XML data
                    */
                    try {
                        //TODO some quick scans to prevent setting up the whole parser for fancy html 404's and such.

                        // set up parser
                        ConfigurationXMLHandler parser = new ConfigurationXMLHandler();
                        XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                        xr.setContentHandler(parser);

                        // parse
                        xr.parse(new InputSource(xmlData));
                        result = parser.getAutoconfigInfo();

                        debug("Successfully parsed the configuration data.");

                        // progress
                        publishProgress("Parse done");
                    } catch (Exception e) {
                        debug("Failed to parse XML configuration data: " + e.getLocalizedMessage());
                        // progress
                        publishProgress("Parse failed");
                    } finally {
                        if (xmlData != null) {
                            xmlData.close();
                        }
                    }
                } catch (Exception e) {
                    debug("Failed to get XML configuration data: " + e.getLocalizedMessage());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

                // try next template
                ++templateIndex;

                // cancelled?
                if (isCancelled()) {
                    break;
                }
            }

            // cancelled?
            if (isCancelled()) {
                break;
            }

            // try next domain
            ++domainIndex;
        }

        // convert username field
        ServerSettings settings;
        for (int i=0; i < result.incomingServer.size(); ++i) {
            settings = result.incomingServer.get(i);
            settings.username = ConfigurationXMLHandler.convertUsername(settings.username, params[0]);
        }

        for (int i=0; i < result.outgoingServer.size(); ++i) {
            settings = result.outgoingServer.get(i);
            settings.username = ConfigurationXMLHandler.convertUsername(settings.username, params[0]);
        }

        return result;
    }

    private Set<String> getMXDomains(String domain) throws IOException {
        HttpGet method = new HttpGet(dnsMXLookupUrl + domain);

        // do request
        HttpResponse response = mHttpClient.execute(method);
        String data = EntityUtils.toString(response.getEntity());

        // filter and get tld's
        Set<String> result =  new HashSet<String>();
        for (String mxDom : Arrays.asList(data.split("[\\r\\n]+"))) {
            String tld = Utility.extractTopLevelDomain(mxDom);
            if (tld != null) {
                result.add(tld);
            }
        }

        return result;
    }

    private String[] splitEmail(String email) {
        String[] retParts = new String[2];
        String[] emailParts = email.split("@");
        retParts[0] = (emailParts.length > 0) ? emailParts[0] : "";
        retParts[1] = (emailParts.length > 1) ? emailParts[1] : "";
        return retParts;
    }

    private void debug(String msg) {
        if (K9.DEBUG_SETUP) {
            Log.d(K9.LOG_TAG, msg);
        }
    }
}
