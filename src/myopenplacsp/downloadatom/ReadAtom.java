/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.downloadatom;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.w3._2005.atom.FeedType;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;


/**
 *
 * @author DSGregorio
 */
public class ReadAtom {

    public static String readLinkNext(File ficheroRISP) throws Exception {

        Unmarshaller atomUnMarshaller;

        FeedType res = null;
        res = null;
        JAXBContext jc = JAXBContext.newInstance(
                "org.w3._2005.atom:org.dgpe.codice.common.caclib:org.dgpe.codice.common.cbclib:ext.place.codice.common.caclib:ext.place.codice.common.cbclib:org.purl.atompub.tombstones._1");
        atomUnMarshaller = jc.createUnmarshaller();
        InputStreamReader inStream = new InputStreamReader(new FileInputStream(ficheroRISP), StandardCharsets.UTF_8);
        res = ((JAXBElement<FeedType>) atomUnMarshaller.unmarshal(inStream)).getValue();

        // Se leen los elementos link si hay algún link con el atributo next se devuelve la url
        if (res.getLink() != null) {
            for (int indice = 0; indice < res.getLink().size(); indice++) {
                if (res.getLink().get(indice).getRel().equalsIgnoreCase("next")) {
                    return res.getLink().get(indice).getHref();
                }
            }
        }
                  
        return ""; //Sí llega hasta aquí no se ha encontrado link next
    }
}
