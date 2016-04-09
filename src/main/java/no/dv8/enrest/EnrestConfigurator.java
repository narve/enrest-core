package no.dv8.enrest;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Slf4j
public class EnrestConfigurator {



    public static <T> T parseJSON(Class<T> clz, String bodyAsString) {

        try {
            bodyAsString = URLDecoder.decode(bodyAsString, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        log.info( "Parsing: {}", bodyAsString );
        return new Gson().fromJson(bodyAsString, clz);
    }

    public static String getBodyAsString(ServletRequest req) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = br.readLine()) != null) sb.append(s).append("\r\n");
            return sb.toString().substring( "body=".length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
