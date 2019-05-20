package fr.eisbm.GraphMLHandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.util.Pair;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.SBGNBase.Notes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.eisbm.GRAPHML2SBGNML.ConverterDefines;
import fr.eisbm.GRAPHML2SBGNML.ModelAttributes;
import fr.eisbm.GRAPHML2SBGNML.Utils;

public class GlyphHandler {

	java.util.Map<String, String> compoundComplexMap = new HashMap<String, String>();
	java.util.Map<String, String> compoundCompartmentMap = new HashMap<String, String>();
	Set<String> complexSet = new HashSet<String>();
	Set<String> compartmentSet = new HashSet<String>();
	java.util.Map<Pair<String, String>, ResourceCoordinates> resourceMap = new HashMap<Pair<String, String>, ResourceCoordinates>();

	public String parseYedNodeType(String szType, boolean bIsMultimer) {
		String szGlyphClass = "";
		if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_SIMPLE_CHEMICAL)) {
			if (bIsMultimer) {
				szGlyphClass = ConverterDefines.SBGN_SIMPLE_CHEMICAL_MULTIMER;
			} else {
				szGlyphClass = ConverterDefines.SBGN_SIMPLE_CHEMICAL;
			}
		} else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_PROCESS)) {
			szGlyphClass = ConverterDefines.SBGN_PROCESS;
		}
		// Unspecified entity
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_UNSPECIFIED_ENTITY)) {
			szGlyphClass = ConverterDefines.SBGN_UNSPECIFIED_ENTITY;
		}
		// Perturbing agent
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_PERTURBING_AGENT)) {
			szGlyphClass = ConverterDefines.SBGN_PERTURBING_AGENT;
		}
		// Phenotype
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_PHENOTYPE)) {
			szGlyphClass = ConverterDefines.SBGN_PHENOTYPE;
		}
		// nucleic acid feature
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_NUCLEIC_ACID_FEATURE)) {
			if (bIsMultimer) {
				szGlyphClass = ConverterDefines.SBGN_NUCLEIC_ACID_FEATURE_MULTIMER;
			} else {
				szGlyphClass = ConverterDefines.SBGN_NUCLEIC_ACID_FEATURE;
			}
		}
		// submap
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_SUBMAP)) {
			szGlyphClass = ConverterDefines.SBGN_SUBMAP;
		}
		// Macromolecule
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_MACROMOLECULE)) {
			// the multimer is represented by a macromolecule having the property
			// <y:Property class="java.lang.Integer" name="com.yworks.sbgn.style.mcount"
			// value="2"/> in GraphML
			if (bIsMultimer) {
				szGlyphClass = ConverterDefines.SBGN_MACROMOLECULE_MULTIMER;
			}
			// macromolecule
			else {
				szGlyphClass = ConverterDefines.SBGN_MACROMOLECULE;
			}
		}
		// Complex
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_COMPLEX)) {
			if (bIsMultimer) {
				szGlyphClass = ConverterDefines.SBGN_COMPLEX_MULTIMER;
			} else {
				szGlyphClass = ConverterDefines.SBGN_COMPLEX;
			}
		}
		// Tag
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_TAG)) {
			szGlyphClass = ConverterDefines.SBGN_TAG;
		}
		// State variable
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_STATE_VARIABLE)) {
			szGlyphClass = ConverterDefines.SBGN_STATE_VARIABLE;
		}
		// State variable
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_UNIT_OF_INFORMATION)) {
			szGlyphClass = ConverterDefines.SBGN_UNIT_OF_INFORMATION;
		}
		// Operator
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_OPERATOR)) {
			szGlyphClass = "operator";
		}
		// source and sink
		else if (szType.contains(ConverterDefines.COM_YWORKS_SBGN_SOURCE_AND_SINK)) {
			szGlyphClass = ConverterDefines.SBGN_SOURCE_AND_SINK;
		}

		return szGlyphClass;
	}

	public void parseCompartments(Element eElement, Map map, StyleHandler sh) {
		{
			String szCompartmentId = eElement.getAttribute(ConverterDefines.ID_ATTR);
			Glyph _compartmentGlyph = new Glyph();
			_compartmentGlyph.setId(szCompartmentId);
			_compartmentGlyph.setClazz(ConverterDefines.SBGN_COMPARTMENT);

			NodeList _nlNodeLabelList = eElement.getElementsByTagName(ConverterDefines.Y_NODE_LABEL);
			String szTextContent = _nlNodeLabelList.item(0).getTextContent().trim();

			if (!szTextContent.equals("")) { // setting the label of the complex e.g. cytosolic proteasome..
				Label _label = new Label();
				_label.setText(szTextContent);
				_compartmentGlyph.setLabel(_label);
			}

			// setting the bbox info
			NodeList nlGeometryList = eElement.getElementsByTagName(ConverterDefines.Y_GEOMETRY);
			if (nlGeometryList.getLength() > 0) {
				Node nGeometry = nlGeometryList.item(0);

				if (nGeometry.getNodeType() == Node.ELEMENT_NODE) {
					setBbox(_compartmentGlyph, (Element) nGeometry);
				}
			}

			// setting style info
			setStyle(eElement, szCompartmentId, sh);

			NodeList nCompoundList = eElement.getElementsByTagName(ConverterDefines.NODE_TAG);
			for (int tempCompound = 0; tempCompound < nCompoundList.getLength(); tempCompound++) {
				Node nCompoundNode = nCompoundList.item(tempCompound);
				if (nCompoundNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eCompoundElement = (Element) nCompoundNode;
					String szCompoundId = eCompoundElement.getAttribute(ConverterDefines.ID_ATTR);
					compoundCompartmentMap.put(szCompoundId, szCompartmentId);
				}
			}

			// if the compartment is an inner compartment, the reference is set to its
			// parental compartment
			if (compoundCompartmentMap.containsKey(szCompartmentId)) {
				setCompartmentRefToGlyph(compoundCompartmentMap.get(szCompartmentId), _compartmentGlyph,
						map.getGlyph());
			}

			compartmentSet.add(szCompartmentId);

			// add the glyph to the map
			map.getGlyph().add(_compartmentGlyph);
		}
	}

	public void parseComplexes(Document doc, ModelAttributes modelAttr, Element eElement, NodeList _nlConfigList,
			Map map, StyleHandler sh) {
		if (_nlConfigList.getLength() > 0) {
			if (((Element) _nlConfigList.item(0)).hasAttribute("configuration")) {
				String szYEDNodeType = ((Element) _nlConfigList.item(0)).getAttribute("configuration");
				if (szYEDNodeType.equals(ConverterDefines.COM_YWORKS_SBGN_COMPLEX)) {
					String szComplexId = eElement.getAttribute(ConverterDefines.ID_ATTR);

					Glyph _complexGlyph = null;

					NodeList nBoundNodeList = eElement.getElementsByTagName(ConverterDefines.Y_PROXY_AUTO_BOUNDS_NODE);
					if (nBoundNodeList.getLength() > 0) {
						Node nBoundNode = nBoundNodeList.item(0);

						if (nBoundNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eBoundElement = (Element) nBoundNode;

							NodeList nComplexNodeList = eBoundElement
									.getElementsByTagName(ConverterDefines.Y_GENERIC_GROUP_NODE);
							if (nComplexNodeList.getLength() > 0) {
								Node nComplexNode = nComplexNodeList.item(0);

								if (nComplexNode.getNodeType() == Node.ELEMENT_NODE) {
									Element eComplexElement = (Element) nComplexNode;

									boolean bIsMultimer = false;
									// the multimer is represented by a macromolecule having the property
									// <y:Property class="java.lang.Integer" name="com.yworks.sbgn.style.mcount"
									// value="2"/> in GraphML
									NodeList _nlNodePropertiesList = (eComplexElement
											.getElementsByTagName(ConverterDefines.Y_PROPERTY));

									for (int i = 0; i < _nlNodePropertiesList.getLength(); i++) {
										Element _elem = (Element) _nlNodePropertiesList.item(i);
										if ((_elem.getAttribute("name")
												.equals(ConverterDefines.COM_YWORKS_SBGN_STYLE_MCOUNT))
												&& (Double.parseDouble(_elem.getAttribute("value")) > 1)) {
											bIsMultimer = true;
											break;
										}
									}

									if (!compoundComplexMap.containsKey(szComplexId)) {
										_complexGlyph = new Glyph();
										_complexGlyph.setId(szComplexId);
										String szGlyphClass = parseYedNodeType(szYEDNodeType, bIsMultimer);
										_complexGlyph.setClazz(szGlyphClass);

										NodeList _nlNodeLabelList = eComplexElement
												.getElementsByTagName(ConverterDefines.Y_NODE_LABEL);

										String szTextContent = _nlNodeLabelList.item(0).getTextContent().trim();

										if (!szTextContent.equals("")) {
											// setting the label of the complex e.g. cytosolic proteasome..
											Label _label = new Label();
											_label.setText(szTextContent);
											_complexGlyph.setLabel(_label);
										}

										// setting the bbox info
										NodeList nlGeometryList = eElement
												.getElementsByTagName(ConverterDefines.Y_GEOMETRY);
										if (nlGeometryList.getLength() > 0) {
											Node nGeometry = nlGeometryList.item(0);

											if (nGeometry.getNodeType() == Node.ELEMENT_NODE) {
												setBbox(_complexGlyph, (Element) nGeometry);
											}
										}

										// the glyph has resouces (i.e. state variables, multimer states etc.)

										if (_nlNodeLabelList.getLength() > 1) {
											for (int i = 1; i < _nlNodeLabelList.getLength(); i++) {
												Element _element = (Element) _nlNodeLabelList.item(i);
												addUnitOfInformation(_complexGlyph, i,
														_element.getTextContent().trim());
											}
										}

										// setting style info
										setStyle(eBoundElement, szComplexId, sh);

										complexSet.add(szComplexId);

										// if the complex is part of a compartment, the reference to the compartment
										// is
										// set
										if (compoundCompartmentMap.containsKey(szComplexId)) {
											String szCompartmentId = compoundCompartmentMap.get(szComplexId);
											setCompartmentRefToGlyph(szCompartmentId, _complexGlyph, map.getGlyph());
										}

										// add the glyph to the map
										map.getGlyph().add(_complexGlyph);
									} else {

										for (Glyph _glyph : map.getGlyph()) {
											_complexGlyph = Utils.findGlyph(szComplexId, _glyph);
											if (null != _complexGlyph) {
												break;
											}
										}

										if (null == _complexGlyph) {
											System.out.println("parseComplex: complex id = " + szComplexId);
										}
									}

								}
							}
						}
					}
					NodeList nCompoundList = eElement.getElementsByTagName(ConverterDefines.NODE_TAG);
					for (int tempCompound = 0; tempCompound < nCompoundList.getLength(); tempCompound++) {
						Node nCompoundNode = nCompoundList.item(tempCompound);

						if (nCompoundNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eCompoundElement = (Element) nCompoundNode;
							String szCompoundId = eCompoundElement.getAttribute(ConverterDefines.ID_ATTR);

							Element nParentElement = (Element) (nCompoundNode.getParentNode());
							String szParentID = nParentElement.getAttribute(ConverterDefines.ID_ATTR).substring(0,
									nParentElement.getAttribute(ConverterDefines.ID_ATTR).length() - 1);

							if (null != _complexGlyph) {
								if (_complexGlyph.getId().equals(szParentID)) {

									Glyph _glyph = parseGlyphInfo(doc, modelAttr, eCompoundElement, szCompoundId, sh);

									_complexGlyph.getGlyph().add(_glyph);
									compoundComplexMap.put(szCompoundId, szComplexId);
								}
							}
						}
					}
				}
			}
		}
	}

	public void parseNodes(Document doc, ModelAttributes modelAttributes, NodeList nList, Map map, StyleHandler sh) {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				// get id of the node/glyph
				String szGlyphId = eElement.getAttribute(ConverterDefines.ID_ATTR);

				if ((!compoundComplexMap.containsKey(szGlyphId)) && (!complexSet.contains(szGlyphId))
						&& (!compartmentSet.contains(szGlyphId))) {
					Glyph _glyph = parseGlyphInfo(doc, modelAttributes, eElement, szGlyphId, sh);

					// if the glyph is part of a compartment, the reference to the compartment is
					// set
					if (compoundCompartmentMap.containsKey(szGlyphId)) {
						String szCompartmentId = compoundCompartmentMap.get(szGlyphId);
						setCompartmentRefToGlyph(szCompartmentId, _glyph, map.getGlyph());
					}

					// add the glyph to the map
					map.getGlyph().add(_glyph);
				}
			}
		}
	}

	public Glyph parseGlyphInfo(Document doc, ModelAttributes modelAttributes, Element eElement, String szGlyphId,
			StyleHandler sh) {
		Glyph _glyph = new Glyph();
		_glyph.setId(szGlyphId);

		boolean bIsMultimer = false;
		// the multimer is represented by a macromolecule having the property
		// <y:Property class="java.lang.Integer" name="com.yworks.sbgn.style.mcount"
		// value="2"/> in GraphML
		NodeList _nlNodePropertiesList = (eElement.getElementsByTagName(ConverterDefines.Y_PROPERTY));
		for (int i = 0; i < _nlNodePropertiesList.getLength(); i++) {
			Element _elem = (Element) _nlNodePropertiesList.item(i);
			if ((_elem.getAttribute("name").equals(ConverterDefines.COM_YWORKS_SBGN_STYLE_MCOUNT))
					&& (Double.parseDouble(_elem.getAttribute("value")) > 1)) {
				bIsMultimer = true;
				break;
			}
		}

		// setting the glyph class
		NodeList _nlConfigList = eElement.getElementsByTagName(ConverterDefines.Y_GENERIC_GROUP_NODE);

		if (0 == _nlConfigList.getLength()) {
			_nlConfigList = eElement.getElementsByTagName(ConverterDefines.Y_GENERIC_NODE);
		}

		if (_nlConfigList.getLength() > 0) {
			String szYEDNodeType = ((Element) _nlConfigList.item(0)).getAttribute("configuration");
			String szGlyphClass = parseYedNodeType(szYEDNodeType, bIsMultimer);
			NodeList _nlNodeLabelList = eElement.getElementsByTagName(ConverterDefines.Y_NODE_LABEL);

			String szTextContent = _nlNodeLabelList.item(0).getTextContent().trim();

			if (szGlyphClass.equals(ConverterDefines.SBGN_PROCESS) && szTextContent.equals("?")) {
				szGlyphClass = ConverterDefines.SBGN_UNCERTAIN_PROCESS;
			} else if (szGlyphClass.equals(ConverterDefines.SBGN_PROCESS) && szTextContent.equals("\\\\")) {
				szGlyphClass = ConverterDefines.SBGN_OMITTED_PROCESS;
			}

			if (!szGlyphClass.equals(ConverterDefines.SBGN_OPERATOR)) {

				_glyph.setClazz(szGlyphClass);

				if (!szTextContent.equals("")) {
					// setting the label of the glyph e.g. Coenzyme A..
					Label _label = new Label();
					_label.setText(szTextContent);
					_glyph.setLabel(_label);
				}
			} else {
				_glyph.setClazz(szTextContent.toLowerCase());
			}

			// setting the bbox info
			NodeList nlGeometryList = eElement.getElementsByTagName(ConverterDefines.Y_GEOMETRY);
			if (nlGeometryList.getLength() > 0) {
				Node nGeometry = nlGeometryList.item(0);

				if (nGeometry.getNodeType() == Node.ELEMENT_NODE) {
					setBbox(_glyph, (Element) nGeometry);
				}
			}

			// the glyph has resouces (i.e. state variables, multimer states etc.)
			if (_nlNodeLabelList.getLength() > 1) {
				for (int i = 1; i < _nlNodeLabelList.getLength(); i++) {
					Element _element = (Element) _nlNodeLabelList.item(i);
					String szText = _element.getTextContent().trim();

					if (_element.hasAttribute(ConverterDefines.ICON_DATA_ATTR)) {
						Pair<String, String> pair = new Pair<String, String>(
								_element.getAttribute(ConverterDefines.ICON_DATA_ATTR), szGlyphId);
						if (!resourceMap.containsKey(pair)) {
							resourceMap.put(pair,
									new ResourceCoordinates(_glyph.getBbox().getX(), _glyph.getBbox().getY(), szText));
							if (_element.hasAttribute("x")) {
								resourceMap.get(pair).updateXCoord(Float.parseFloat(_element.getAttribute("x")));
							}
							if (_element.hasAttribute("y")) {
								resourceMap.get(pair).updateYCoord(Float.parseFloat(_element.getAttribute("y")));
							}
						}
					}

					if (bIsMultimer) {
						addUnitOfInformation(_glyph, i, szText);
					}
				}
			}

			// setting style info
			setStyle(eElement, szGlyphId, sh);

			if (szGlyphClass.equals(ConverterDefines.SBGN_TAG)) {
				_glyph.setOrientation(ConverterDefines.SBGN_LEFT_TAG);

				NodeList _nlPropertyList = eElement.getElementsByTagName(ConverterDefines.Y_PROPERTY);
				if (_nlPropertyList.getLength() > 0) {
					Element _tagOrientation = (Element) _nlPropertyList.item(0);
					if (_tagOrientation.hasAttribute("name")) {
						if (_tagOrientation.getAttribute("name")
								.equals(ConverterDefines.COM_YWORKS_SBGN_TAG_ORIENTATION)) {
							_glyph.setOrientation(ConverterDefines.SBGN_RIGHT_TAG);
						}
					}
				}
			}

			// parse data information on notes, annotation, orientation, clone etc.
			NodeList nlDataList = eElement.getElementsByTagName(ConverterDefines.DATA_TAG);
			Element eltAnnotation = parseAnnotation(doc, modelAttributes, _glyph, nlDataList);

			if (null != eltAnnotation) {
				Extension _extension = new Extension();
				_extension.getAny().add(eltAnnotation);
				_glyph.setExtension(_extension);
			}
		}
		return _glyph;
	}

	private void addUnitOfInformation(Glyph _glyph, int i, String szText) {
		// the glyph is a multimer (the shape from the yEd SBGN palette was used for
		// drawing) and the child glyph is an unit of information
		if ((szText.contains("N:")) || (szText.contains("RNA")) || (szText.toUpperCase().contains("RECEPTOR"))) {
			Glyph _uofGlyph = new Glyph();
			_uofGlyph.setClazz(ConverterDefines.SBGN_UNIT_OF_INFORMATION);
			_uofGlyph.setId(_glyph.getId() + "_uoi_" + i);
			Label _label = new Label();
			_label.setText(szText);
			_uofGlyph.setLabel(_label);

			Bbox _uofBbox = new Bbox();
			_uofBbox.setH(16);
			_uofBbox.setW(25);
			_uofBbox.setX(_glyph.getBbox().getX() + 10);
			_uofBbox.setY(_glyph.getBbox().getY() - 10);
			_uofGlyph.setBbox(_uofBbox);

			_glyph.getGlyph().add(_uofGlyph);
		}
	}

	private Element parseAnnotation(Document doc, ModelAttributes modelAttributes, Glyph _glyph, NodeList nlDataList) {
		Element eltAnnotation = null;
		// TODO: to read the namespace from the file
		Element rdfRDF = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				ConverterDefines.RDF_RDF_TAG);
		Element rdfDescription = doc.createElement(ConverterDefines.RDF_DESCRIPTION_TAG);
		rdfRDF.appendChild(rdfDescription);
		rdfDescription.setAttribute(ConverterDefines.RDF_ABOUT_TAG, "#" + _glyph.getId());

		// check if there is at least one field of the annotation; otherwise, no need to
		// add an empty annotation
		boolean bHasAnnotation = false;

		for (int temp2 = 0; temp2 < nlDataList.getLength(); temp2++) {
			Element _element = ((Element) (nlDataList.item(temp2)));

			// parse notes information
			if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szNotesTagId)) {
				Notes notes = getSBGNNotes(_element);
				if (null != notes) {
					_glyph.setNotes(notes);
					bHasAnnotation = true;
				}
			}

			// setting the orientation value for the SBGN process
			if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szOrientationTagId)) {
				if (Utils.isProcessType(_glyph)) {
					if (_element.getTextContent().equals(ConverterDefines.SBGN_VERTICAL_PROCESS)) // otherwise implicit
					{
						_glyph.setOrientation(_element.getTextContent());
					}
				}
			}

			// parse annotation information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szAnnotationTagId)) {
				String szText = _element.getTextContent().trim();
				if (!szText.equals("")) {
					if (null == eltAnnotation) {
						eltAnnotation = doc.createElement(ConverterDefines.ANNOTATION_TAG);
					}
					szText = szText.replaceAll("\"", "");
					String delims = " ";
					String[] tokens = szText.split(delims);
					for (int i = 0; i < tokens.length; i++) {
						String value = tokens[i].substring(tokens[i].indexOf("=") + 1);
						if (tokens[i].contains(ConverterDefines.XMLNS_N2_NS + "=")) {
							eltAnnotation.setAttribute(ConverterDefines.XMLNS_N2_NS, value);
						} else if (tokens[i].contains(ConverterDefines.XMLNS_NS + "=")) {
							eltAnnotation.setAttribute(ConverterDefines.XMLNS_NS, value);
						}
					}
					bHasAnnotation = true;
				}
			}

			// parse namespace information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szNodeURLTagId)) {
				String szText = _element.getTextContent();
				if (!szText.equals("")) {

					szText = szText.replaceAll("\"", "");
					String delims = " ";
					String[] tokens = szText.split(delims);
					for (int i = 0; i < tokens.length; i++) {
						String value = tokens[i].substring(tokens[i].indexOf("=") + 1).trim();
						if (!value.equals("")) {

							if (tokens[i].contains(ConverterDefines.XMLNS_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_NS, value);
							} else if (tokens[i].contains(ConverterDefines.XMLNS_BQBIOL_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_BQBIOL_NS, value);
							} else if (tokens[i].contains(ConverterDefines.XMLNS_BQMODEL_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_BQMODEL_NS, value);
							} else if (tokens[i].contains(ConverterDefines.XMLNS_CELL_DESIGNER_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_CELL_DESIGNER_NS, value);
							} else if (tokens[i].contains(ConverterDefines.XMLNS_DC_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_DC_NS, value);
							} else if (tokens[i].contains(ConverterDefines.XMLNS_DC_TERMS_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_DC_TERMS_NS, value);
							} else if (tokens[i].contains(ConverterDefines.XMLNS_VCARD_NS + "=")) {
								rdfRDF.setAttribute(ConverterDefines.XMLNS_VCARD_NS, value);
							} else if (tokens[i].toUpperCase().contains(ConverterDefines.UNIPROT)
									|| tokens[i].toUpperCase().contains(ConverterDefines.CHEBI)) {
								String text = rdfRDF.getTextContent().trim();
								// if there is some text in the RDF tag description, this is separated through a
								// space by the newly added url information
								if (text != "") {
									text = text.concat(" ");
								}
								text = text.concat(value);
								rdfRDF.setTextContent(text);
							}
							bHasAnnotation = true;
						}
					}
				}
			}

			// parse clone information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szCloneTagId)) {
				if (!_element.getTextContent().trim().equals("")) {
					Clone _clone = new Clone();
					// This is in concordance with the SBGN to GraphML translation as sometimes,
					// label text within in the clone is not specified, but it is usually equal to
					// empty space. Thus, the condition if from above would be false and the clone
					// would not be set. The FileUtils.CloneIsSet was introduced in order to show
					// that the glyph has a clone that has to be set, even if the label text is an
					// empty string. At the decoding step, this additional information must be
					// removed.
					if (!_element.getTextContent().equals(CloneHandler.CloneIsSet)) {
						Label _label = new Label();
						String text = _element.getTextContent();
						_label.setText(text);
						_clone.setLabel(_label);
					}

					_glyph.setClone(_clone);
				}
			}

			// parse bqmodel:is information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szBqmodelIsTagId)) {
				String szText = _element.getTextContent();
				if (!szText.equals("")) {
					String delimsLine = "[\n]";
					String[] tokens = szText.split(delimsLine);
					for (int i = 0; i < tokens.length; i++) {
						Element elBqtModelIs = doc.createElement(ConverterDefines.BQMODEL_IS);
						// add rdf:Bag
						Element eltRDFBag = doc.createElement(ConverterDefines.RDF_BAG_TAG);
						elBqtModelIs.appendChild(eltRDFBag);
						Element eltRDFLi = doc.createElement(ConverterDefines.RDF_LI_TAG);
						eltRDFLi.setAttribute(ConverterDefines.RDF_RESOURCE_TAG, tokens[i]);
						eltRDFBag.appendChild(eltRDFLi);
						rdfDescription.appendChild(elBqtModelIs);
					}
					bHasAnnotation = true;
				}
			}

			// parse bqmodel:isDescribedBy information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG)
					.equals(modelAttributes.szBqmodelIsDescribedByTagId)) {
				String szText = _element.getTextContent().trim();

				if (!szText.equals("")) {
					String delimsLine = "[\n]";
					String[] tokens = szText.split(delimsLine);
					for (int i = 0; i < tokens.length; i++) {
						Element elBqtModelIsDescribedBy = doc.createElement(ConverterDefines.BQMODEL_IS_DESCRIBED_BY);
						// add rdf:Bag
						Element eltRDFBag = doc.createElement(ConverterDefines.RDF_BAG_TAG);
						elBqtModelIsDescribedBy.appendChild(eltRDFBag);
						Element eltRDFLi = doc.createElement(ConverterDefines.RDF_LI_TAG);
						eltRDFLi.setAttribute(ConverterDefines.RDF_RESOURCE_TAG, tokens[i]);
						eltRDFBag.appendChild(eltRDFLi);
						rdfDescription.appendChild(elBqtModelIsDescribedBy);
					}
					bHasAnnotation = true;
				}
			}

			// parse bqbiol:is information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG).equals(modelAttributes.szBqbiolIsTagId)) {
				String szText = _element.getTextContent();
				if (!szText.equals("")) {
					Element eltBqbiolIs = doc.createElement(ConverterDefines.BQBIOL_IS);
					// add rdf:Bag
					Element eltRDFBag = doc.createElement(ConverterDefines.RDF_BAG_TAG);
					eltBqbiolIs.appendChild(eltRDFBag);

					String delimsLine = "[\n]";
					String[] tokens = szText.split(delimsLine);
					for (int i = 0; i < tokens.length; i++) {
						Element eltRDFLi = doc.createElement(ConverterDefines.RDF_LI_TAG);
						eltRDFLi.setAttribute(ConverterDefines.RDF_RESOURCE_TAG, tokens[i]);
						eltRDFBag.appendChild(eltRDFLi);
					}
					rdfDescription.appendChild(eltBqbiolIs);
					bHasAnnotation = true;
				}
			}

			// parse bqbiol:isDescribedBy information
			else if (_element.getAttribute(ConverterDefines.KEY_TAG)
					.equals(modelAttributes.szBqbiolIsDescribedByTagId)) {
				String szText = _element.getTextContent().trim();

				if (!szText.equals("")) {
					String delimsLine = "[\n]";
					String[] tokens = szText.split(delimsLine);
					for (int i = 0; i < tokens.length; i++) {
						Element elBqbiolIsDescribedBy = doc.createElement(ConverterDefines.BQBIOL_IS_DESCRIBED_BY);
						// add rdf:Bag
						Element eltRDFBag = doc.createElement(ConverterDefines.RDF_BAG_TAG);
						elBqbiolIsDescribedBy.appendChild(eltRDFBag);
						Element eltRDFLi = doc.createElement(ConverterDefines.RDF_LI_TAG);
						eltRDFLi.setAttribute(ConverterDefines.RDF_RESOURCE_TAG, tokens[i]);
						eltRDFBag.appendChild(eltRDFLi);
						rdfDescription.appendChild(elBqbiolIsDescribedBy);
					}
					bHasAnnotation = true;
				}
			}
		}

		if (true == bHasAnnotation) {
			if (null == eltAnnotation) {
				eltAnnotation = doc.createElement(ConverterDefines.ANNOTATION_TAG);
			}
			eltAnnotation.appendChild(rdfRDF);
		}

		return eltAnnotation;
	}

	public Notes getSBGNNotes(Element notes) {
		if (notes != null) {
			if (notes.getTextContent() != null) {
				if (!notes.getTextContent().trim().isEmpty()) {
					Notes newNotes = new Notes();
					newNotes.getAny().add(notes);
					return newNotes;
				}
			}
		}
		return null;
	}

	public void setCompartmentRefToGlyph(String szParentCompartmentId, Glyph _glyph, List<Glyph> _listOfGlyphs) {
		for (Glyph _compartment : _listOfGlyphs) {
			if (_compartment.getId().equals(szParentCompartmentId)) {
				_glyph.setCompartmentRef(_compartment);
			} else {
				setCompartmentRefToGlyph(szParentCompartmentId, _glyph, _compartment.getGlyph());
			}
		}
	}

	public void setBbox(Glyph _glyph, Element nGeometry) {
		Bbox bbox = new Bbox();

		String szHeight = nGeometry.getAttribute(ConverterDefines.HEIGHT_ATTR);
		String szWidth = nGeometry.getAttribute(ConverterDefines.WIDTH_ATTR);
		String szXPos = nGeometry.getAttribute(ConverterDefines.X_POS_ATTR);
		String szYPos = nGeometry.getAttribute(ConverterDefines.Y_POS_ATTR);

		bbox.setH(Float.parseFloat(szHeight));
		bbox.setW(Float.parseFloat(szWidth));
		bbox.setX(Float.parseFloat(szXPos));
		bbox.setY(Float.parseFloat(szYPos));
		_glyph.setBbox(bbox);
	}

	public void processResources(NodeList nResourceList, Map map) {
		for (int temp = 0; temp < nResourceList.getLength(); temp++) {
			Node nResource = nResourceList.item(temp);

			if (nResource.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nResource;
				Glyph _glyph = new Glyph();

				// get id of the resources
				String szResourceId = eElement.getAttribute(ConverterDefines.ID_ATTR);
				_glyph.setId(szResourceId);

				NodeList _nlGenericNodeList = eElement.getElementsByTagName(ConverterDefines.Y_GENERIC_NODE);
				NodeList _nlShapeNodeList = eElement.getElementsByTagName(ConverterDefines.Y_SHAPE_NODE);

				if (_nlGenericNodeList.getLength() > 0) {
					String szYEDNodeType = ((Element) _nlGenericNodeList.item(0)).getAttribute("configuration");
					String szGlyphClass = parseYedNodeType(szYEDNodeType, false);
					_glyph.setClazz(szGlyphClass);
				} else if (_nlShapeNodeList.getLength() > 0) {
					_glyph.setClazz(ConverterDefines.SBGN_UNIT_OF_INFORMATION);
				}

				NodeList nlNodeLabelList = eElement.getElementsByTagName(ConverterDefines.Y_NODE_LABEL);
				boolean bAuxInfoText = false;

				if (nlNodeLabelList.getLength() > 0) {
					Node nNodeLabel = nlNodeLabelList.item(0);

					if (nNodeLabel.getNodeType() == Node.ELEMENT_NODE) {
						bAuxInfoText = setTextToAuxiliaryInformation(_glyph, nNodeLabel.getTextContent().trim());
					}
				}

				// add the auxiliary information (state variable, unit of information) to the
				// corresponding glyph
				for (Entry<Pair<String, String>, ResourceCoordinates> _resource : resourceMap.entrySet()) {
					if (_resource.getKey().first.equals(szResourceId)) {
						NodeList nlGeometryList = eElement.getElementsByTagName(ConverterDefines.Y_GEOMETRY);
						Node nGeometry = nlGeometryList.item(0);

						if (nGeometry.getNodeType() == Node.ELEMENT_NODE) {
							Element eGeometryElement = (Element) nGeometry;
							String szHeight = eGeometryElement.getAttribute(ConverterDefines.HEIGHT_ATTR);
							String szWidth = eGeometryElement.getAttribute(ConverterDefines.WIDTH_ATTR);

							Bbox bbox = new Bbox();
							bbox.setH(Float.parseFloat(szHeight));
							bbox.setW(Float.parseFloat(szWidth));
							if (_resource.getValue() != null) {
								bbox.setX(_resource.getValue().getXCoord());
								bbox.setY(_resource.getValue().getYCoord());
							}

							if (!bAuxInfoText) {
								if (_resource.getValue() != null) {
									if (_resource.getValue().getText() != null) {
										bAuxInfoText = setTextToAuxiliaryInformation(_glyph,
												_resource.getValue().getText().trim());
									}
								}
							}

							_glyph.setBbox(bbox);
						}

						String szParentGlyphId = _resource.getKey().second;
						addGlyphToList(szParentGlyphId, _glyph, map.getGlyph());
					}
				}
			}
		}
	}

	private boolean setTextToAuxiliaryInformation(Glyph _glyph, String szNodeText) {
		boolean bAuxInfoText = false;

		if (!szNodeText.equals("")) {

			if (_glyph.getClazz().equals(ConverterDefines.SBGN_STATE_VARIABLE)) {
				// setting the label of the glyph e.g. P, 2P..
				Glyph.State _state = new Glyph.State();
				int iDelimPos = szNodeText.indexOf("@");
				String szValue = "";
				String szVariable = "";

				if (iDelimPos < 0) {
					szValue = szNodeText;
				} else if (0 == iDelimPos) {
					szVariable = szNodeText.substring(iDelimPos + 1, szNodeText.length());
				} else if (iDelimPos > 0) {
					szValue = szNodeText.substring(0, iDelimPos);
					szVariable = szNodeText.substring(iDelimPos + 1, szNodeText.length());
				}

				_state.setValue(szValue);
				_state.setVariable(szVariable);
				_glyph.setState(_state);
			} else if (_glyph.getClazz().equals(ConverterDefines.SBGN_UNIT_OF_INFORMATION)) {
				Label _label = new Label();
				_label.setText(szNodeText);
				_glyph.setLabel(_label);
			}
			bAuxInfoText = true;
		}
		return bAuxInfoText;
	}

	private void addGlyphToList(String szParentGlyphId, Glyph _glyph, List<Glyph> _listOfGlyphs) {
		for (Glyph _parentGlyph : _listOfGlyphs) {
			if (_parentGlyph.getId().equals(szParentGlyphId)) {
				_parentGlyph.getGlyph().add(_glyph);
			} else {
				addGlyphToList(szParentGlyphId, _glyph, _parentGlyph.getGlyph());
			}
		}
	}

	public void setStyle(Element eElement, String szId, StyleHandler sh) {
		String szFillColorId = ((Element) (eElement.getElementsByTagName(ConverterDefines.Y_FILL).item(0)))
				.getAttribute(ConverterDefines.COLOR_ATTR);
		sh.colorSet.add(szFillColorId);

		NodeList nlBorderStyle = eElement.getElementsByTagName(ConverterDefines.Y_BORDER_STYLE);
		// getting the border color info
		String szStrokeColorId = ((Element) (nlBorderStyle.item(0))).getAttribute(ConverterDefines.COLOR_ATTR);
		sh.colorSet.add(szStrokeColorId);

		// getting the stroke width color info
		float fStrokeWidth = Float
				.parseFloat(((Element) (nlBorderStyle.item(0))).getAttribute(ConverterDefines.WIDTH_ATTR));

		// getting the stroke width color info
		String szStrokeColorWidth = ((Element) (eElement.getElementsByTagName(ConverterDefines.Y_NODE_LABEL).item(0)))
				.getAttribute(ConverterDefines.FONTSIZE_ATTR);
		float fFontSize = Utils.DEFAULT_FONT_SIZE;
		if (!szStrokeColorWidth.equals("")) {
			fFontSize = Float.parseFloat(szStrokeColorWidth);
		}

		String szStyleId = ConverterDefines.STYLE_PREFIX + fStrokeWidth + szFillColorId.replaceFirst("#", "")
				+ fFontSize + szStrokeColorId.replaceFirst("#", "");
		if (!sh.styleMap.containsKey(szStyleId)) {
			sh.styleMap.put(szStyleId,
					new SBGNMLStyle(szStyleId, szFillColorId, szStrokeColorId, fStrokeWidth, fFontSize));
		}
		sh.styleMap.get(szStyleId).addElementIdToSet(szId);
	}

	public void createPorts(List<Glyph> list) {
		for (Glyph glyph : list) {
			if (Utils.isOperatorType(glyph) || (Utils.isProcessType(glyph))) {
				if (glyph.getPort() != null) {
					if (glyph.getPort().size() != Utils.MAX_PORT_NO) {
						for (int i = 0; i < Utils.MAX_PORT_NO; i++) {
							createPort(glyph, i);
						}
					}
				}
			}

			// create ports also for inner processes/ operators
			createPorts(glyph.getGlyph());
		}
	}

	private void createPort(Glyph glyph, int i) {
		Port port = new Port();
		port.setId(glyph.getId() + "." + (i + 1));

		float x = 0;
		float y = 0;

		// the glyph (process/ operator) is oriented vertically, so the ports will be
		// located top/down
		if (glyph.getOrientation().equals("vertical")) {
			x = (float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);

			if (Utils.FIRST_PORT == i) {
				y = (float) (glyph.getBbox().getY() + glyph.getBbox().getH() + glyph.getBbox().getH() * 0.5);
			} else {
				y = (float) (glyph.getBbox().getY() - glyph.getBbox().getH() * 0.5);
			}
		}
		// the glyph (process/ operator) is oriented horizontally, so the ports will be
		// located left-right
		else {
			if (Utils.FIRST_PORT == i) {
				x = (float) (glyph.getBbox().getX() - glyph.getBbox().getW() * 0.5);
			} else {
				x = (float) (glyph.getBbox().getX() + glyph.getBbox().getW() + glyph.getBbox().getW() * 0.5);
			}
			y = (float) (glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
		}

		port.setX(x);
		port.setY(y);
		glyph.getPort().add(port);
	}
}
