/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.downloadatom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
//import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
//import java.security.KeyManagementException;
import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
//import javax.net.ssl.X509TrustManager;

/**
 *
 * @author DSGregorio
 */
public class DownloadAtom {

    private String rutaKeystore;
    private String passwordKeystore;
    private String rutaAtom;
    private String urlAtom;

    public DownloadAtom(String rutaKeystore, String passwordKeystore, String rutaAtom, String urlAtom) {
        this.rutaKeystore = rutaKeystore;
        this.passwordKeystore = passwordKeystore;
        this.rutaAtom = rutaAtom;
        this.urlAtom = urlAtom;
    }

    public String getRutaKeystore() {
        return rutaKeystore;
    }

    public void setRutaKeystore(String rutaKeystore) {
        this.rutaKeystore = rutaKeystore;
    }

    public String getPasswordKeystore() {
        return passwordKeystore;
    }

    public void setPasswordKeystore(String passwordKeystore) {
        this.passwordKeystore = passwordKeystore;
    }

    public String getRutaAtom() {
        return rutaAtom;
    }

    public void setRutaAtom(String rutaAtom) {
        this.rutaAtom = rutaAtom;
    }

    public String getUrlAtom() {
        return urlAtom;
    }

    public void setUrlAtom(String urlAtom) {
        this.urlAtom = urlAtom;
    }

    public void download() throws Exception {

        //try {
        
        // Creamos el file writer a null y creamos un nuevo fichero
        //FileWriter fichero = null;
        //fichero = new FileWriter(rutaAtom);
        OutputStreamWriter fichero =
             new OutputStreamWriter(new FileOutputStream(rutaAtom), StandardCharsets.UTF_8);
        
        // Carga del fichero que tiene los certificados de los servidores en
        // los que confiamos.
        InputStream fileCertificadosConfianza = new FileInputStream(new File(rutaKeystore));
        KeyStore ksCertificadosConfianza = KeyStore.getInstance(KeyStore.getDefaultType());
        ksCertificadosConfianza.load(fileCertificadosConfianza, passwordKeystore.toCharArray());
        fileCertificadosConfianza.close();
        // Creamos la fabrica de gestores de confianza basada
        // en un KeyStore y/o fuentes especificas del proveedor
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ksCertificadosConfianza);
        // Creamos un contexto SSL con nuestro manager de certificados en los
        // que confiamos.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        SSLSocketFactory sslSocketFactory = context.getSocketFactory();
        // Abrimos la conexión y le pasamos nuestra URL
        URL url = new URL(urlAtom);
        URLConnection conexion = url.openConnection();
        ((HttpsURLConnection) conexion).setSSLSocketFactory(sslSocketFactory);
        // Ya podemos conectar y leer
        conexion.connect();
        InputStream is = conexion.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        char[] buffer = new char[1000];
        int leido;
        // Escribimos línea a liea todo lo sacado de la lectura en el fichero creado anteriormente
        while ((leido = br.read(buffer)) > 0) {
            fichero.write(new String(buffer, 0, leido));
        }

        if (null != fichero) {
            fichero.close();
        }
        
        //Todos los mensajes de error diferenciados para que de el pertinente en cada caso
        /*} catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }*/
    }
}
