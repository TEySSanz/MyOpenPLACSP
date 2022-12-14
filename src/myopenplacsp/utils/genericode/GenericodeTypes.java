/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.utils.genericode;

/**
 *
 * @author DSGregorio
 */
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import myopenplacsp.view.ParserController;

public enum GenericodeTypes {
    ESTADO("/resources/gc/SyndicationContractFolderStatusCode-2.04.gc"),
    TIPO_CONTRATO("/resources/gc/ContractCode-2.08.gc"),
    TIPO_PROCEDIMIENTO("/resources/gc/SyndicationTenderingProcessCode-2.07.gc"),
    SISTEMA_CONTRATACION("/resources/gc/ContractingSystemTypeCode-2.08.gc"),
    TRAMITACION("/resources/gc/DiligenceTypeCode-1.04.gc"),
    PRESENTACION_OFERTA("/resources/gc/TenderDeliveryCode-1.04.gc"),
    RESULTADO("/resources/gc/TenderResultCode-2.02.gc"),
    TIPO_ADMINISTRACION("/resources/gc/ContractingAuthorityCode-1.04.gc"),
    CODIGO_FINANCIACION("/resources/gc/FundingProgramCode-2.08.gc");
    /*ESTADO("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/SyndicationContractFolderStatusCode-2.04.gc"),
        TIPO_CONTRATO("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/ContractCode-2.08.gc"),
	TIPO_PROCEDIMIENTO("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/SyndicationTenderingProcessCode-2.07.gc"),
	SISTEMA_CONTRATACION("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/ContractingSystemTypeCode-2.08.gc"),
	TRAMITACION("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/DiligenceTypeCode-1.04.gc"),
	PRESENTACION_OFERTA("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/TenderDeliveryCode-1.04.gc"),
	RESULTADO("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/TenderResultCode-2.02.gc"),
	TIPO_ADMINISTRACION("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/ContractingAuthorityCode-1.04.gc"),
	CODIGO_FINANCIACION("C:/Users/DSGregorio/Documents/NetBeansProjects/MyOpenPLACSP/src/myopenplacsp/resources/gc/FundingProgramCode-2.08.gc");    */

    private final Logger logger = LogManager.getLogger(ParserController.class.getName());

    private HashMap<String, String> codes = null;

    GenericodeTypes(String nombreGenericode) {

        try {
            File file = new File(GenericodeTypes.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath());
            //userDirectory tendrá la dirección desde la que se está ejecutando:
            //  el jar C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\dist
            //  la compilación  C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\build
            String userDirectory = file.getParent();

            /*System.out.println(userDirectory);*/
            logger.debug("Se lee la lista de códigos " + userDirectory + nombreGenericode);
            codes = GenericodeManager.generateMap(new FileInputStream(userDirectory + nombreGenericode));
        } catch (Exception e) {
            logger.error("Se produjo un error al cargar la lista de códigos " + nombreGenericode + "\n> " + e.getMessage());
        }
    }

    public String getValue(String key) {
        if (codes.containsKey(key)) {
            return codes.get(key);
        } else {
            return key;
        }

    }

}
