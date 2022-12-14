/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import myopenplacsp.downloadatom.DownloadAtom;
import myopenplacsp.downloadatom.ReadAtom;
import myopenplacsp.utils.Config;
import myopenplacsp.utils.RegistrarAccion;
import myopenplacsp.view.ParserController;

/**
 *
 * @author DSGregorio
 */
public class MyOpenPLACSP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws URISyntaxException {
        File file = new File(MyOpenPLACSP.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath());
        //userDirectory tendrá la dirección desde la que se está ejecutando:
        //  el jar C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\dist
        //  la compilación  C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\build
        String userDirectory = file.getParent();
        
        File errorLog = new File(userDirectory+"/error-MyOpenPLACSP.txt");
        crearFichero(errorLog);// Si no existe se crea el fichero error-MyOpenPLACSP.txt que irá registrando los errores que se produzcan durante la ejecución

        File messageLog = new File(userDirectory+"/message-MyOpenPLACSP.txt");
        crearFichero(messageLog);// Si no existe se crea el fichero message-MyOpenPLACSP.txt que irá registrando los errores que se produzcan durante la ejecución

        File debugLog = new File(userDirectory+"/debug-MyOpenPLACSP.txt");
        crearFichero(debugLog);// Si no existe se crea el fichero message-MyOpenPLACSP.txt que irá registrando los errores que se produzcan durante la ejecución

        RegistrarAccion reg = new RegistrarAccion();

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String hoyString = formatter.format(date);

        try {
            if (args.length != 7 || args[0].equalsIgnoreCase("-help")) {
                String txtException = "Se deben pasar los parámetros necesarios para la ejecución:\n\n"
                        + "rutaKeystore\t\t(keystore generado del certificado https ej: 'C:/carpeta/.keystore')\n"
                        + "passwordKeystore\t(password del keystore generado ej: 'myPassword')\n"
                        + "rutaAtom\t\t\t(ruta donde se quiere guardar el atom descargado ej: 'C:/carpeta/atom/')\n"
                        + "nombreAtom\t\t\t(nombre del atom descargado ej: 'licitacionesPerfilesContratanteCompleto3.atom')\n"
                        + "urlAtom\t\t\t\t(url del archivo atom a descargar ej: 'https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_643/licitacionesPerfilesContratanteCompleto3.atom')\n"
                        + "rutaExcel\t\t\t(ruta donde se quiere guardar el excel que se genera después de procesar el atom ej: 'C:/carpeta/atom/licitacionesPerfilesContratanteCompleto3.xlsx')\n"
                        + "rutaPlantilla\t\t(ruta donde se encuentra la plantilla excel a utilizar ej: 'C:/carpeta/datosAbiertos-PLACSP.xlsx')\n\n"
                        + "ej: java -jar \"C:\\carpeta\\MyOpenPLACSP.jar\" "
                        + "\"C:/carpeta/.keystore\" \"myPassword\" "
                        + "\"C:/carpeta/atom/\" \"licitacionesPerfilesContratanteCompleto3.atom\" "
                        + "\"https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_643/licitacionesPerfilesContratanteCompleto3.atom\" "
                        + "\"C:/carpeta/atom/licitacionesPerfilesContratanteCompleto3.xlsx\" \"C:/carpeta/datosAbiertos-PLACSP.xlsx\"\n";
                System.out.println(txtException);
                throw new Exception(txtException);
            }

            //Leer variables por parámetro
            String rutaKeystore = args[0];
            String passwordKeystore = args[1];
            String rutaAtom = args[2];
            String nombreAtomBase = args[3];
            String nombreAtom = args[3];
            String urlAtom = args[4];
            String rutaExcel = args[5];
            String rutaPlantillaExcel = args[6];
            
            /*
            //Pruebas CCAA
            String rutaKeystore = "C:/Users/DSGregorio/.keystore";
            String passwordKeystore = "Teys2022PLACSP";
            String rutaAtom = "C:/Users/DSGregorio/Documents/Proyectos/AV0000 Licitaciones/PlataformasAgregadasSinMenores_202211/";
            String nombreAtomBase = "PlataformasAgregadasSinMenores.atom";
            String nombreAtom = "PlataformasAgregadasSinMenores.atom";
            String urlAtom = "https://contrataciondelestado.es/sindicacion/sindicacion_1044/PlataformasAgregadasSinMenores.atom";
            String rutaExcel = "C:/Users/DSGregorio/Documents/Proyectos/AV0000 Licitaciones/PlataformasAgregadasSinMenores.xlsx";
            String rutaPlantillaExcel = "C:\\Users\\DSGregorio\\Documents\\NetBeansProjects\\MyOpenPLACSP\\dist\\resources\\base\\datosAbiertos-PLACSP.xlsx";*/
            
            /*
            //Pruebas Licitaciones estatales
            String rutaKeystore = "C:/Users/DSGregorio/.keystore";
            String passwordKeystore = "Teys2022PLACSP";
            String rutaAtom = "C:/Users/DSGregorio/Documents/Proyectos/AV0000 Licitaciones/licitacionesPerfilesContratanteCompleto3.atom";
            String nombreAtomBase = "licitacionesPerfilesContratanteCompleto3.atom";
            String nombreAtom = "licitacionesPerfilesContratanteCompleto3.atom";
            String urlAtom = "https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_643/licitacionesPerfilesContratanteCompleto3.atom";
            String rutaExcel = "C:/Users/DSGregorio/Documents/Proyectos/AV0000 Licitaciones/licitacionesPerfilesContratanteCompleto3.xlsx";
            String rutaPlantillaExcel = "C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/templates/datosAbiertos-PLACSP.xlsx";
            */

            DownloadAtom myDownloadAtom = new DownloadAtom(rutaKeystore, passwordKeystore, rutaAtom + nombreAtom, urlAtom);
            //Descargar fichero principal
            reg.registrarAccion(messageLog, "myDownloadAtom.download(): inicio " + nombreAtom);
            myDownloadAtom.download();
            reg.registrarAccion(messageLog, "myDownloadAtom.download(): fin " + nombreAtom);

            File fileIn = new File(rutaAtom + nombreAtom);

            //Descargar todos los ficheros atom del día
            while (!urlAtom.isEmpty()) {
                //Si se indica en el atom que hay otro fichero más con fecha del día de la descarga, se descarga también
                if (ReadAtom.readLinkNext(fileIn).contains(hoyString)) {
                    urlAtom = ReadAtom.readLinkNext(fileIn);
                    int iSlash = urlAtom.lastIndexOf("/");
                    int iLength = urlAtom.length();
                    nombreAtom = urlAtom.substring(iSlash + 1, iLength);
                    myDownloadAtom = new DownloadAtom(rutaKeystore, passwordKeystore, rutaAtom + nombreAtom, urlAtom);

                    reg.registrarAccion(messageLog, "myDownloadAtom.download(): inicio " + nombreAtom);
                    myDownloadAtom.download();
                    reg.registrarAccion(messageLog, "myDownloadAtom.download(): fin " + nombreAtom);

                    fileIn = new File(rutaAtom + nombreAtom);
                } else {
                    urlAtom = "";
                }
            }
            
            //Ficheros de propiedades CODICE
            File comprobarFileResources = new File(userDirectory + "/resources/open-placsp.properties");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/SyndicationContractFolderStatusCode-2.04.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/ContractCode-2.08.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/SyndicationTenderingProcessCode-2.07.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/ContractingSystemTypeCode-2.08.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/DiligenceTypeCode-1.04.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/TenderDeliveryCode-1.04.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/TenderResultCode-2.02.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/ContractingAuthorityCode-1.04.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }
            comprobarFileResources = new File(userDirectory + "/resources/gc/FundingProgramCode-2.08.gc");
            if (!comprobarFileResources.exists()) {
                throw new Exception("No se encuentra el archivo " + comprobarFileResources);
            }

            fileIn = new File(rutaAtom + nombreAtomBase);
            File fileOut = new File(rutaExcel);

            //Objeto de ParseController que se encarga de procesar los atom descargados y generar el fichero xlsx
            ParserController myParser = new ParserController();
            reg.registrarAccion(messageLog, "myParser.generarXLSX(fileIn, fileOut): inicio PlataformasAgregadasSinMenores.atom");
            myParser.generarXLSX(fileIn, fileOut, rutaPlantillaExcel);
            reg.registrarAccion(messageLog, "myParser.generarXLSX(fileIn, fileOut): fin PlataformasAgregadasSinMenores.atom");
        } catch (Exception ex) {
            reg.registrarAccion(errorLog, ex.getMessage());
        }
        
    }

    public static void crearFichero(File fileLog) {
        try {

            if (!fileLog.exists()) {
                fileLog.createNewFile();
            }
        } catch (Exception ex) {
            System.out.println("No se ha podido crear el fichero " + fileLog.getName() + " - " + ex.getMessage());
        }
    }

}
