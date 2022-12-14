/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.utils;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
/**
 *
 * @author DSGregorio
 */
public class RegistrarAccion {

    public RegistrarAccion() {
    }
    
    public void registrarAccion(File miArchivo, String mensaje) {
        //Declaración de variables
        try {
            FileWriter fw = new FileWriter(miArchivo, true);

            fw.write(new Date().toString()+ " - " +mensaje + "\r\n");
            fw.close();
        } catch (Exception ex) {
            System.out.println("Error al esctibir en el fichero "+miArchivo.getAbsolutePath()+" - " + ex.getMessage());
        }
    }
}
