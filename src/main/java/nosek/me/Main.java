package nosek.me;


import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

import static nosek.me.ProxyTypePac.DIRECT;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {

        ProxyPac pPAC = new ProxyPac();
        URL proxyURL = null;

        try {
            URI proxyURI = new URI("http://my.proxy.com:8080");
            proxyURL = proxyURI.toURL();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        pPAC.setProxyPacURL(URL);


        SocketAddress proxiedAddress = new InetSocketAddress("my.proxy.com", 8080);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxiedAddress);
        URL url = new URL("https://repo1.maven.org/maven2/org/apache/groovy/groovy-all/4.0.12/groovy-all-4.0.12-groovydoc.jar");
        URLConnection conn = url.openConnection(proxy);


        conn.


        pPAC.setProxyPacURL(new URL("file:///Users/jmn/Downloads/scratch2test/proxyDownloader/src/main/resources/proxy.pac"));
        System.out.println(pPAC.findProxyForURL(url));


        String rawProxyString = "PROXY proxy1.example.com; PROXY proxy2.example.com; DIRECT";







    }
}