package me.backgo.azure.util;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {
    /**
     * Getting Browser
     * @param request
     * @return String
     * @throws Exception
     */
    public static String getBrowser(HttpServletRequest request) throws Exception {
        String header = request.getHeader("User-Agent");

        if (header.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (header.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (header.indexOf("Opera") > -1) {
            return "Opera";
        }

        return "Firefox";
    }
    
    /**
     * Getting Disposition
     * @param filename
     * @param browser
     * @return String
     * @throws Exception
     */
    public static String getDisposition(String filename, String browser) throws Exception {
        String dispositionPrefix = "attachment;filename=";
        String encodedFilename = null;
        
        if (browser.equals("MSIE")) {
            encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } else if (browser.equals("Firefox")) {
            encodedFilename =  URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } else if (browser.equals("Opera")) {
            encodedFilename =  new String(filename.getBytes("UTF-8"), "8859_1");
        } else if (browser.equals("Chrome")) {
            StringBuffer sb = new StringBuffer();
            
            for (int i = 0; i < filename.length(); i++) {
                char c = filename.charAt(i);
                if (c > '~') {
                    sb.append(URLEncoder.encode("" + c, "UTF-8"));
                } else {
                    sb.append(c);
                }
            }
            
            encodedFilename = sb.toString();
        } else {
            throw new RuntimeException("Not supported browser");
        }
        
        return dispositionPrefix + encodedFilename;
    }
}