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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="SimpleCodeList")
public class SimpleCodeList {
	
	@XmlElement(name="Row")
	protected List<Row> row;
	
	public List<Row> getRows() {
		return row;
	}
	
	public void setRows(List<Row> rows) {
		this.row = rows;
	}

}
