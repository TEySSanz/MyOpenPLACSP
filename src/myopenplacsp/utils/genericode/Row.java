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
@XmlRootElement(name="Row")
public class Row {
	
	@XmlElement(name = "Value")
	protected List<Value> values;
	
	public List<Value> getValues() {
		return values;
	}
	
	public void setValues(List<Value> values) {
		this.values = values;
	}
}
