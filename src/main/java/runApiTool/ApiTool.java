package runApiTool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import redis.clients.jedis.Jedis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApiTool {

    public static void main(String[] args) throws URISyntaxException, IOException {

        Path path = Paths.get("C:\\Tools-Re02\\fileName.txt");
        try {

            System.out.println("Starting... ");

            String userName = args[0];
            String pass = args[1];
            String url = args[2];
            String host = args[3];
            int port = Integer.valueOf(args[4]);
            System.out.println(String.format("userName: %s", userName));
            System.out.println(String.format("pass: %s", pass));
            System.out.println(String.format("url: %s", url));

            int sumHttp = 0;
            Jedis jedis = null;


            StopWatch sw = new StopWatch();
            sw.start();

            sw.stop();
            System.out.println("Total Elapse newObj: " + sw.getTime() + " millieconds");

            Map<String, String> headers = new HashMap<>();
          //  setAuthorizationHeader(userName, pass, headers);
            URI uri = new URI(url);
            HttpConnector connector = new HttpConnector(host, port, null, null, 0, 90000, 120000, "", "");
            long startHttpCall = System.currentTimeMillis();
            RequestResponse response = connector.execute("", uri, headers, null, "", "GET", "admin@adinamakocp", "adinamakocp");
            long endHttpCall = System.currentTimeMillis();
            sumHttp += endHttpCall - startHttpCall;


            if (response.getStatusCode() != 200) {
                String msg = String.format("Complete response: %s, content: %s", response.getStatusCode(), response.getContent(),response.getException());
                System.out.println(msg);
                Files.write(path, msg.getBytes());
            } else {
                System.out.println("SUCCESS: " + response.getContent());
                Files.write(path, ("SUCCESS: " + response.getContent()).getBytes());

            }

        } catch (Exception ex) {
            System.out.println("error:" + ex);
            Files.write(path, ex.toString().getBytes());
        }
//        long endTotal = System.currentTimeMillis();
//        System.out.println("Sum http calls : " + sumHttp + " milliseconds");
//        System.out.println("Total Elapse: " + (endTotal - startTotal) + " smillieconds");
//        }
    }

    private static void setAuthorizationHeader(String userName, String pass, Map<String, String> headers) {

        String token = String.format("cscloud\\%s:%s", userName, pass);
        String encoded = new String(Base64.getEncoder().encode(token.getBytes()));
       // headers.put("Authorization", String.format("Basic %s", encoded));
      //  headers.put("userName", userName);
       // headers.put("tokenId", pass);
       // System.out.println(headers.get("Authorization"));
    }
}