/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.model;

/**
 *
 * @author DSGregorio
 */
import ext.place.codice.common.caclib.ContractFolderStatusType;

public interface DatosCodice {
	public abstract Object valorCodice(ContractFolderStatusType contractFolder);
	
	public EnumFormatos getFormato();
}