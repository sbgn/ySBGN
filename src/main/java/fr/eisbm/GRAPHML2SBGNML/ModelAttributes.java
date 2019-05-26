package fr.eisbm.GRAPHML2SBGNML;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utils.ConverterDefines;

public class ModelAttributes {

	public String szNotesTagId;
	public String szCloneTagId;
	public String szBqmodelIsTagId;
	public String szBqmodelIsDescribedByTagId;
	public String szBqbiolIsTagId;
	public String szBqbiolIsDescribedByTagId;
	public String szAnnotationTagId;
	public String szNodeURLTagId;
	public String szOrientationTagId;
	
	public ModelAttributes() {
		super();
		szNotesTagId ="";
		szCloneTagId ="";
		szBqmodelIsTagId ="";
		szBqmodelIsDescribedByTagId ="";
		szBqbiolIsTagId ="";
		szBqbiolIsDescribedByTagId ="";
		szAnnotationTagId ="";
		szNodeURLTagId ="";
		szOrientationTagId ="";
	}
	
	public void populateModelAttributes(NodeList nKeyList) {
		for (int temp = 0; temp < nKeyList.getLength(); temp++) {
			Node nNode = nKeyList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if (eElement.getAttribute("attr.name").toLowerCase().equals(ConverterDefines.NOTES_TAG)) {
					szNotesTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").toLowerCase().equals(ConverterDefines.CLONE_TAG)) {
					szCloneTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").toLowerCase()
						.equals(ConverterDefines.ANNOTATION_TAG)) {
					szAnnotationTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").toLowerCase().equals(ConverterDefines.BQMODEL_IS)) {
					szBqmodelIsTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").equals(ConverterDefines.BQMODEL_IS_DESCRIBED_BY)) {
					szBqmodelIsDescribedByTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").equals(ConverterDefines.BQBIOL_IS)) {
					szBqbiolIsTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").equals(ConverterDefines.BQBIOL_IS_DESCRIBED_BY)) {
					szBqbiolIsDescribedByTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if (eElement.getAttribute("attr.name").toLowerCase()
						.equals(ConverterDefines.ORIENTATION_TAG)) {
					szOrientationTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				} else if ((eElement.getAttribute("attr.name").toLowerCase().equals(ConverterDefines.URL_TAG))
						&& (eElement.getAttribute("for").toLowerCase().equals(ConverterDefines.NODE_TAG))) {
					szNodeURLTagId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				}
			}
		}
	}

}
