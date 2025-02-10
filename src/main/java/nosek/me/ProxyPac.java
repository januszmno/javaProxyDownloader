package nosek.me;

import javax.script.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import static nosek.me.ProxyTypePac.DIRECT;

public class ProxyPac {

    //convert URLs to URI with toURL

    private URL proxyURL;
    private Invocable invocablePac;
    private boolean init = false;
    public final String PROXY_PAC_FUNCTION = "FindProxyForURL";
    public final String PAC_FILE_NAME = "ascii_pac_utils_jdk5.js";
    public final String PROXY_DELIM = ";";
    public final String PROXY_TUPLE_DELIM = " ";
    public final int DEFAULT_PORT = 80;

    public final int POS_PROTOCOL = 0;
    public final int POS_HOSTNAME = 1;
    public final String RAW_TEST_URL = "https://127.0.0.1";

    private URL TEST_URL;

    {
        try {

            TEST_URL = new URI(RAW_TEST_URL).toURL();
        } catch (URISyntaxException uris){
            throw new RuntimeException("Bad syntax initiating test URL: ["+RAW_TEST_URL+"]",uris);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Test URL is malformed: ["+RAW_TEST_URL+"]",mue);
        }
    }

    ProxyPac (){}

    public String findProxyForURL(final URL requestUrl){
        return findProxyForURL(requestUrl, requestUrl.getHost());
    }

    public String findProxyForURL(final URL requestUrl, final String requestHost){
     if (!init){
         throw new IllegalStateException("ProxyPac not initialized");
     }
        return findProxyForURLHelper( requestUrl,requestHost);
    }


    private String findProxyForURLHelper(final URL requestUrl, final String requestHost){

        try {
            final Object rawProxy = invocablePac.invokeFunction(PROXY_PAC_FUNCTION, requestUrl.toString(), requestHost);

            return rawProxy.toString();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    private Proxy[] proxyParser(final String rawProxyString, final URL requestUrl){


        final String[] rawProxyArrayfromJS = rawProxyString.split(PROXY_DELIM);
        final ArrayList<Proxy> proxies = new ArrayList<>();

        String[] proxyTuple;
        for(String rawProxyArrayTuple:rawProxyArrayfromJS){
            proxyTuple = rawProxyArrayTuple.trim().split(PROXY_TUPLE_DELIM);


            if (1 == proxyTuple.length){

                if (DIRECT.toString().equals(proxyTuple[POS_PROTOCOL])){

                    proxies.add(Proxy.NO_PROXY);
                }

            }
            else if (2 == proxyTuple.length){

                URI proxyURI = null;
                String rawURI = null;
                try {
                    rawURI = ProxyTypePac.HTTP.getProtocol()+"://"+proxyTuple[POS_HOSTNAME];
                    proxyURI = new URI(rawURI);
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Unable to parse request URL ["+rawURI+"]",e);
                }
                int proxyPort;
                try{
                    proxyPort = proxyURI.getPort();
                    if (proxyPort<1){
                        proxyPort = DEFAULT_PORT;
                    }
                }catch (IllegalArgumentException iae){
                    proxyPort = DEFAULT_PORT;
                }

                final SocketAddress proxyAddress = new InetSocketAddress(proxyURI.getHost(), proxyPort);



                switch(ProxyTypePac.getName(proxyTuple[POS_PROTOCOL])){
                    case ProxyTypePac.PROXY:
                        if(!requestUrl.getProtocol().contains(ProxyTypePac.SOCKS.getProtocol())){
                            proxies.add(new Proxy(Proxy.Type.HTTP,proxyAddress));
                        }
                        else{
                            proxies.add(new Proxy(Proxy.Type.SOCKS,proxyAddress));
                        }
                        break;
                    case ProxyTypePac.SOCKS:
                    case ProxyTypePac.SOCKS4:
                    case ProxyTypePac.SOCKS5:
                        proxies.add(new Proxy(Proxy.Type.SOCKS,proxyAddress));
                        break;
                    case ProxyTypePac.HTTP:
                    case ProxyTypePac.HTTPS:
                        proxies.add(new Proxy(Proxy.Type.HTTP,proxyAddress));
                        break;
                }

            }
            else{
                throw new RuntimeException("Invalid proxy type: "+rawProxyArrayTuple);
            }


        }

        return proxies.toArray(new Proxy[proxies.size()]);
    }


    public void setProxyPacURL(final URL proxyPacURL){

        final ClassLoader classLoader = getClass().getClassLoader();
        final URL pacUtilsURL =  classLoader.getResource(PAC_FILE_NAME);

        if (null == pacUtilsURL){
            throw new RuntimeException("Cannot find Proxy PAC file: ["+PAC_FILE_NAME+"]");
        }


        BufferedReader pacUtilsInput = null;
        try {
            pacUtilsInput = new BufferedReader(
                    new InputStreamReader(pacUtilsURL.openStream()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to open PAC Utils file: ["+pacUtilsURL+"]",e);
        }

        final StringBuilder utilsScript = new StringBuilder();
        int utilsScriptCounter = 0;
        try {
            String inputLine;
            while ((inputLine = pacUtilsInput.readLine()) != null) {
                utilsScript.append(inputLine);
                utilsScriptCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read line ["+utilsScriptCounter+"] from the PAC Utils file: ["+pacUtilsURL+"]",e);
        } finally {
            try {
                pacUtilsInput.close();
            } catch (IOException e) {
                throw new RuntimeException("Cannot close file handle on the PAC Utils file: ["+pacUtilsURL+"]",e);
            }
        }

        if (utilsScript.isEmpty()){
            throw new IllegalArgumentException("PAC Utils file is empty at ["+pacUtilsURL+"]");
        }






        if (null == proxyPacURL){
            throw new IllegalArgumentException("Proxy Pac URL is null");
        }
        this.proxyURL = proxyPacURL;

        BufferedReader proxyPACInput = null;
        try {
            proxyPACInput = new BufferedReader(
                    new InputStreamReader(proxyURL.openStream()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to open ["+proxyURL+"]",e);
        }

        final StringBuilder pacscript = new StringBuilder();
        int pacLineCounter = 0;
        try {
            String inputLine;
            while ((inputLine = proxyPACInput.readLine()) != null) {
                pacscript.append(inputLine);
                pacscript.append(System.getProperty("line.separator"));
                pacLineCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read line ["+pacLineCounter+"] from Proxy PAC Configuration file: ["+proxyURL+"]",e);
        } finally {
            try {
                proxyPACInput.close();
            } catch (IOException e) {
                throw new RuntimeException("Cannot close file handle on the Proxy PAC Configuration file: ["+proxyURL+"]",e);
            }
        }

        if (pacscript.isEmpty()){
            throw new IllegalArgumentException("Proxy PAC Configuration file is empty at ["+proxyURL+"]");
        }








        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("javascript");
        //FindProxyForURL(url, host)

        try {
            engine.eval(utilsScript.toString());

        } catch (ScriptException e) {
            throw new RuntimeException("Unable to evaluate PAC Util Script loaded from ["+pacUtilsURL+"]",e);
        }

        try {
            engine.eval(pacscript.toString());
        } catch (ScriptException e) {
            throw new RuntimeException("Unable to evaluate Proxy Pac Configuration file loaded from ["+proxyURL+"]",e);
        }

        invocablePac = (Invocable) engine;

        try {
            findProxyForURLHelper(TEST_URL, TEST_URL.getHost());
        }catch (Exception e){
            throw new RuntimeException("PAC Script failed self-test, cannot call ["+PROXY_PAC_FUNCTION+"] as the file appears to be corrupt.",e);
        }
        init = true;
    }


}
