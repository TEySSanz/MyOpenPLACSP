/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.utils;

/**
 *
 * @author DSGregorio
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final String FILE_PROPERTIES = "/resources/open-placsp.properties";
    //private static final String FILE_PROPERTIES = "C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/open-placsp.properties";

    private static Properties defaultProps = new Properties();

    static {
        try {
            //InputStream in = Config.class.getResourceAsStream(FILE_PROPERTIES);    
            File file = new File(Config.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath());
            //userDirectory tendrá la dirección desde la que se está ejecutando:
            //  el jar C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\dist
            //  la compilación  C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\build
            String userDirectory = file.getParent();
            
            //System.out.println(userDirectory);
            
            InputStream in = new FileInputStream(userDirectory+FILE_PROPERTIES);
            defaultProps.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return defaultProps.getProperty(key);
    }

}
