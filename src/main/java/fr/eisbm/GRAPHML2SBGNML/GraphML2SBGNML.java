package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jgrapht.alg.util.Pair;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Next;
import org.sbgn.bindings.Arc.Start;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.SBGNBase.Notes;
import org.sbgn.bindings.Sbgn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.eisbm.GRAPHML2SBGNML.FileUtils.PortType;

public class GraphML2SBGNML {

	private static final String LIST_OF_COLOR_DEFINITIONS_TAG = "listOfColorDefinitions";
	private static final String VALUE_ATTR = "value";
	private static final String ID_LIST_ATTR = "idList";
	private static final String ID_ATTR = "id";
	private static final String GRAPHICS_TAG = "g";
	private static final String FILL_ATTR = "fill";
	private static final String STROKE_WIDTH_ATTR = "strokeWidth";
	private static final String STROKE_ATTR = "stroke";
	private static final String STYLE_TAG = "style";
	private static final String COLOR_DEFINITION_TAG = "colorDefinition";
	private static final String LIST_OF_STYLES_TAG = "listOfStyles";

	private static final String COLOR_PREFIX = "color_";
	private static final String STYLE_PREFIX = "style_";
	private static final String COLOR_ATTR = "color";
	private static final String HEIGHT_ATTR = "height";
	private static final String WIDTH_ATTR = "width";
	private static final String FONTSIZE_ATTR = "fontSize";
	private static final String X_POS_ATTR = "x";
	private static final String Y_POS_ATTR = "y";
	private static final String ICON_DATA_ATTR = "iconData";

	private static final String XMLNS_N2_NS = "xmlns:ns2";
	private static final String XMLNS_NS = "xmlns";
	private static final String XMLNS_BQBIOL_NS = "xmlns:bqbiol";
	private static final String XMLNS_BQMODEL_NS = "xmlns:bqmodel";
	private static final String XMLNS_CELL_DESIGNER_NS = "xmlns:celldesigner";
	private static final String XMLNS_DC_NS = "xmlns:dc";
	private static final String XMLNS_DC_TERMS_NS = "xmlns:dcterms";
	private static final String XMLNS_VCARD_NS = "xmlns:vCard";

	private static final String ANNOTATION_TAG = "annotation";
	private static final String KEY_TAG = "key";
	private static final String NOTES_TAG = "notes";
	private static final String CLONE_TAG = "clone";
	private static final String ORIENTATION_TAG = "orientation";
	private static final String NODE_TAG = "node";
	private static final String EDGE_TAG = "edge";
	private static final String DATA_TAG = "data";
	private static final String RDF_LI_TAG = "rdf:li";
	private static final String URL_TAG = "url";
	private static final String RDF_RESOURCE_TAG = "rdf:resource";
	private static final String RDF_BAG_TAG = "rdf:Bag";
	private static final String RDF_RDF_TAG = "rdf:RDF";
	private static final String RDF_ABOUT_TAG = "rdf:about";
	private static final String RDF_DESCRIPTION_TAG = "rdf:Description";

	private static final int MAX_PORT_NO = 2;
	private static final double PORT2GLYPH_DISTANCE = 10.8;

	Sbgn sbgn = new Sbgn();
	Map map = new Map();
	java.util.Map<Pair<String, String>, ResourceCoordinates> resourceMap = new HashMap<Pair<String, String>, ResourceCoordinates>();
	Set<String> colorSet = new HashSet<String>();
	java.util.Map<String, String> colorMap = new HashMap<String, String>();
	java.util.Map<String, SBGNMLStyle> styleMap = new HashMap<String, SBGNMLStyle>();
	java.util.Map<String, String> compoundComplexMap = new HashMap<String, String>();
	java.util.Map<String, String> compoundCompartmentMap = new HashMap<String, String>();
	Set<String> complexSet = new HashSet<String>();
	Set<String> compartmentSet = new HashSet<String>();
	java.util.Map<String, PortArcsRelationship> port_arc_map = new HashMap<String, PortArcsRelationship>();;

	public static void main(String[] args) {
		convert(FileUtils.IN_YED_FILE);
		System.out.println("simulation finished");
	}

	public static void convert(String szInputFileName) {
		GraphML2SBGNML gs = new GraphML2SBGNML();
		String szOutSBGNFile = szInputFileName.replace(".graphml", "").concat(".sbgn");
		boolean bConversion = gs.parseGraphMLFile(szInputFileName, szOutSBGNFile);
		if (bConversion) {
			String szSBGNv02FileName = szInputFileName.replace(".graphml", "-SBGNv02.sbgn");
			transformToSBGN02.transformToSBGNv02(szOutSBGNFile, szSBGNv02FileName);
		}
	}

	boolean parseGraphMLFile(String szInGraphMLFileName, String szOutSBGNFile) {
		boolean bConversion = false;
		try {
			File inputFile = new File(szInGraphMLFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			File outputFile = new File(szOutSBGNFile);

			map.setLanguage("process description");
			sbgn.setMap(map);

			String szNotesTagId = "";
			String szCloneTagId = "";
			String szBqmodelIsTagId = "";
			String szBqmodelIsDescribedByTagId = "";
			String szBqbiolIsTagId = "";
			String szBqbiolIsDescribedByTagId = "";
			String szAnnotationTagId = "";
			String szNodeURLTagId = "";
			String szOrientationTagId = "";
			NodeList nKeyList = doc.getElementsByTagName(KEY_TAG);
			for (int temp = 0; temp < nKeyList.getLength(); temp++) {
				Node nNode = nKeyList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("attr.name").toLowerCase().equals(NOTES_TAG)) {
						szNotesTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(CLONE_TAG)) {
						szCloneTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(ANNOTATION_TAG)) {
						szAnnotationTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(FileUtils.BQMODEL_IS)) {
						szBqmodelIsTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").equals(FileUtils.BQMODEL_IS_DESCRIBED_BY)) {
						szBqmodelIsDescribedByTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").equals(FileUtils.BQBIOL_IS)) {
						szBqbiolIsTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").equals(FileUtils.BQBIOL_IS_DESCRIBED_BY)) {
						szBqbiolIsDescribedByTagId = eElement.getAttribute(ID_ATTR);
					} else if (eElement.getAttribute("attr.name").toLowerCase().equals(ORIENTATION_TAG)) {
						szOrientationTagId = eElement.getAttribute(ID_ATTR);
					} else if ((eElement.getAttribute("attr.name").toLowerCase().equals(URL_TAG))
							&& (eElement.getAttribute("for").toLowerCase().equals(NODE_TAG))) {
						szNodeURLTagId = eElement.getAttribute(ID_ATTR);
					}
				}
			}

			// complexes and compartments are mapped by yEd groups
			NodeList nComplexList = doc.getElementsByTagName(NODE_TAG);

			for (int temp = 0; temp < nComplexList.getLength(); temp++) {
				Node nNode = nComplexList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					// compartment - mapped by yEd groups with the <y:GroupNode> tag
					NodeList _nlGroupList = eElement.getElementsByTagName(FileUtils.Y_GROUP_NODE);
					if (_nlGroupList.getLength() > 0) {
						String szCompartmentId = eElement.getAttribute(ID_ATTR);
						Glyph _compartmentGlyph = new Glyph();
						_compartmentGlyph.setId(szCompartmentId);
						_compartmentGlyph.setClazz(FileUtils.SBGN_COMPARTMENT);

						NodeList _nlNodeLabelList = eElement.getElementsByTagName(FileUtils.Y_NODE_LABEL);
						String szTextContent = _nlNodeLabelList.item(0).getTextContent().trim();

						if (!szTextContent.equals("")) { // setting the label of the complex e.g. cytosolic proteasome..
							Label _label = new Label();
							_label.setText(szTextContent);
							_compartmentGlyph.setLabel(_label);
						}

						// setting the bbox info setBbox(_compartmentGlyph,
						eElement.getElementsByTagName(FileUtils.Y_GEOMETRY);

						// setting the bbox info
						setBbox(_compartmentGlyph, eElement.getElementsByTagName(FileUtils.Y_GEOMETRY));

						// setting style info
						setStyle(eElement, szCompartmentId);

						NodeList nCompoundList = eElement.getElementsByTagName(NODE_TAG);
						for (int tempCompound = 0; tempCompound < nCompoundList.getLength(); tempCompound++) {
							Node nCompoundNode = nCompoundList.item(tempCompound);
							if (nCompoundNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eCompoundElement = (Element) nCompoundNode;
								String szCompoundId = eCompoundElement.getAttribute(ID_ATTR);
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

					else {
						// checking the node type
						NodeList _nlConfigList = eElement.getElementsByTagName(FileUtils.Y_GENERIC_GROUP_NODE);
						if (_nlConfigList.getLength() > 0) {
							if (((Element) _nlConfigList.item(0)).hasAttribute("configuration")) {
								String szYEDNodeType = ((Element) _nlConfigList.item(0)).getAttribute("configuration");
								if (szYEDNodeType.equals(FileUtils.COM_YWORKS_SBGN_COMPLEX)) {
									String szComplexId = eElement.getAttribute(ID_ATTR);
									Glyph _complexGlyph = null;

									if (!compoundComplexMap.containsKey(szComplexId)) {
										_complexGlyph = new Glyph();
										_complexGlyph.setId(szComplexId);
										String szGlyphClass = parseYedNodeType(szYEDNodeType, false);
										_complexGlyph.setClazz(szGlyphClass);

										NodeList _nlNodeLabelList = eElement
												.getElementsByTagName(FileUtils.Y_NODE_LABEL);
										String szTextContent = _nlNodeLabelList.item(0).getTextContent().trim();

										if (!szTextContent.equals("")) {
											// setting the label of the complex e.g. cytosolic proteasome..
											Label _label = new Label();
											_label.setText(szTextContent);
											_complexGlyph.setLabel(_label);
										}

										// setting the bbox info
										setBbox(_complexGlyph, eElement.getElementsByTagName(FileUtils.Y_GEOMETRY));

										// setting style info
										setStyle(eElement, szComplexId);

										complexSet.add(szComplexId);

										// if the complex is part of a compartment, the reference to the compartment is
										// set
										if (compoundCompartmentMap.containsKey(szComplexId)) {
											String szCompartmentId = compoundCompartmentMap.get(szComplexId);
											setCompartmentRefToGlyph(szCompartmentId, _complexGlyph, map.getGlyph());
										}

										// add the glyph to the map
										map.getGlyph().add(_complexGlyph);
									} else {

										for (Glyph _glyph : map.getGlyph()) {
											_complexGlyph = findGlyph(szComplexId, _glyph);
											if (null != _complexGlyph) {
												break;
											}
										}
									}

									NodeList nCompoundList = eElement.getElementsByTagName(NODE_TAG);
									for (int tempCompound = 0; tempCompound < nCompoundList
											.getLength(); tempCompound++) {
										Node nCompoundNode = nCompoundList.item(tempCompound);

										if (nCompoundNode.getNodeType() == Node.ELEMENT_NODE) {
											Element eCompoundElement = (Element) nCompoundNode;
											String szCompoundId = eCompoundElement.getAttribute(ID_ATTR);

											Element nParentElement = (Element) (nCompoundNode.getParentNode());
											String szParentID = nParentElement.getAttribute(ID_ATTR).substring(0,
													nParentElement.getAttribute(ID_ATTR).length() - 1);
											if (_complexGlyph.getId().equals(szParentID)) {

												Glyph _glyph = parseGlyphInfo(doc, szNotesTagId, szCloneTagId,
														szBqmodelIsTagId, szBqmodelIsDescribedByTagId, szBqbiolIsTagId,
														szBqbiolIsDescribedByTagId, szAnnotationTagId, szNodeURLTagId,
														szOrientationTagId, eCompoundElement, szCompoundId);

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
			}

			// nodes:
			NodeList nList = doc.getElementsByTagName(NODE_TAG);
			processNodes(doc, szNotesTagId, szCloneTagId, szBqmodelIsTagId, szBqmodelIsDescribedByTagId,
					szBqbiolIsTagId, szBqbiolIsDescribedByTagId, szAnnotationTagId, szNodeURLTagId, szOrientationTagId,
					nList);

			// for the process glyphs, ports will be created by default
			createPorts(map.getGlyph());

			// edges/arcs:
			NodeList nEdgeList = doc.getElementsByTagName(EDGE_TAG);
			processArcs(nEdgeList);
			assignArcsToPorts();

			// resources:
			NodeList nResourceList = doc.getElementsByTagName(FileUtils.Y_RESOURCE);
			processResources(nResourceList);

			addExtension(doc);

			moveNetworkToTopLeft();
			correctPortOrientationAndConnectedArcs();
			setClonedGlyphs();

			// write everything to disk
			SbgnUtil.writeToFile(sbgn, outputFile);

			System.out.println(
					"SBGN file validation: " + (SbgnUtil.isValid(outputFile) ? "validates" : "does not validate"));
			bConversion = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bConversion;
	}

	private void setClonedGlyphs() {
		for (Glyph g : map.getGlyph()) {
			if (!(g.getClazz().equals(FileUtils.SBGN_COMPLEX)
					|| g.getClazz().equals(FileUtils.SBGN_COMPLEX_MULTIMER))) {
				setCloneSimpleGlyphs(g, map.getGlyph());
			} else {
				findCloneComplexGlyph(g, map.getGlyph());
			}
		}
	}

	private void findCloneComplexGlyph(Glyph g1, List<Glyph> listOfGlyphs) {
		for (Glyph g2 : listOfGlyphs) {
			if (!g1.getId().equals(g2.getId()) && (g1.getClazz().equals(g2.getClazz()))) {
				boolean bClone = findClonedSimilarGlyphs(g1, g2);

				if (bClone) {
					if (g1.getGlyph().size() != g2.getGlyph().size()) {
						bClone = false;
					}
				}

				if (bClone) {
					for (Glyph g11 : g1.getGlyph()) {
						// this flag assures that all previous elements have clones; otherwise, if there
						// is an element from the first map that has not clone in the second map, no
						// further checking is necessarly as the complexes are not clones
						if (bClone) {
							for (Glyph g22 : g2.getGlyph()) {
								bClone = findClonedSimilarGlyphs(g11, g22);
								// when finding the clone of the current element, the for loop can move to the
								// next element
								if (bClone) {
									break;
								}
							}
						}
					}
				}

				if (bClone) {
					Clone clone = new Clone();

					if (g1.getClone() != null) {
						clone = g1.getClone();
					} else if (g2.getClone() != null) {
						clone = g2.getClone();
					}
					g1.setClone(clone);
					g2.setClone(clone);
				}
			}
		}

	}

	private void setCloneSimpleGlyphs(Glyph g1, List<Glyph> listOfGlyphs) {

		for (Glyph g2 : listOfGlyphs) {
			boolean bClone = findClonedSimilarGlyphs(g1, g2);
			if (bClone) {
				Clone clone = new Clone();

				if (g1.getClone() != null) {
					clone = g1.getClone();
				} else if (g2.getClone() != null) {
					clone = g2.getClone();
				}
				g1.setClone(clone);
				g2.setClone(clone);
			}
		}
	}

	private boolean findClonedSimilarGlyphs(Glyph g1, Glyph g2) {
		boolean bClone = false;

		if (!g1.getId().equals(g2.getId()) && (g1.getClazz().equals(g2.getClazz()))) {

			bClone = haveSameCompartment(g1, g2);
			if (bClone) {
				bClone = haveSameTextLabel(g1, g2);
			}

			if (bClone) {
				bClone = haveSameUnitsOfInfo(g1, g2);
			}

			if (bClone) {
				bClone = haveSameStateVariables(g1, g2);
			}
		}
		return bClone;
	}

	private boolean haveSameStateVariables(Glyph g1, Glyph g2) {
		boolean bSameStateVariable = true;

		for (Glyph g : g1.getGlyph()) {
			if (g.getClazz().equals(FileUtils.SBGN_STATE_VARIABLE)) {
				if (!g2.getGlyph().contains(g)) {
					bSameStateVariable = false;
				}
			}
		}

		for (Glyph g : g2.getGlyph()) {
			if (g.getClazz().equals(FileUtils.SBGN_STATE_VARIABLE)) {
				if (!g1.getGlyph().contains(g)) {
					bSameStateVariable = false;
				}
			}
		}
		return bSameStateVariable;
	}

	private boolean haveSameUnitsOfInfo(Glyph g1, Glyph g2) {
		boolean bSameUOI = true;

		for (Glyph g : g1.getGlyph()) {
			if (g.getClazz().equals(FileUtils.SBGN_UNIT_OF_INFORMATION)) {
				if (!g2.getGlyph().contains(g)) {
					bSameUOI = false;
				}
			}
		}

		for (Glyph g : g2.getGlyph()) {
			if (g.getClazz().equals(FileUtils.SBGN_UNIT_OF_INFORMATION)) {
				if (!g1.getGlyph().contains(g)) {
					bSameUOI = false;
				}
			}
		}
		return bSameUOI;
	}

	private boolean haveSameCompartment(Glyph g1, Glyph g2) {
		boolean bSameCompartment = false;

		if (g1.getCompartmentRef() != null) {
			if (g2.getCompartmentRef() != null) {
				if (g1.getCompartmentRef().equals(g2.getCompartmentRef())) {
					bSameCompartment = true;
				}
			}
		} else {
			// if both glyphs have no compartment references
			if (g2.getCompartmentRef() == null) {
				bSameCompartment = true;
			}
		}
		return bSameCompartment;

	}

	private boolean haveSameTextLabel(Glyph g1, Glyph g2) {
		boolean bSameTextLabel = false;

		if (g1.getLabel() != null) {
			if (g2.getLabel() != null) {
				if (g1.getLabel().getText().equals(g2.getLabel().getText())) {
					bSameTextLabel = true;
				}
			}
		} else {
			if (g2.getLabel() == null) {
				bSameTextLabel = true;
			}
		}
		return bSameTextLabel;
	}

	private void moveNetworkToTopLeft() {
		float x_min = 0;
		float y_min = 0;

		for (Glyph glyph : map.getGlyph()) {
			if (glyph.getBbox().getX() < x_min) {
				x_min = glyph.getBbox().getX();
			}
			if (glyph.getBbox().getY() < y_min) {
				y_min = glyph.getBbox().getY();
			}

			if (glyph.getPort().size() > 0) {
				for (Port port : glyph.getPort()) {
					if (port.getX() < x_min) {
						x_min = port.getX();
					}

					if (port.getY() < y_min) {
						y_min = port.getY();
					}
				}
			}
		}

		for (Arc arc : map.getArc()) {
			if (arc.getStart().getX() < x_min) {
				x_min = arc.getStart().getX();
			}
			if (arc.getEnd().getX() < x_min) {
				x_min = arc.getEnd().getX();
			}
			if (arc.getStart().getY() < y_min) {
				y_min = arc.getStart().getY();
			}
			if (arc.getEnd().getY() < y_min) {
				y_min = arc.getEnd().getY();
			}
		}

		if (x_min < 0) {
			moveGlyphs(map.getGlyph(), Math.abs(x_min), 0);
			moveArcs(map.getArc(), Math.abs(x_min), 0);
		}

		if (y_min < 0) {
			moveGlyphs(map.getGlyph(), 0, Math.abs(y_min));
			moveArcs(map.getArc(), 0, Math.abs(y_min));
		}
	}

	private void moveGlyphs(List<Glyph> glyphList, float x_val, float y_val) {
		for (Glyph glyph : glyphList) {
			float newX = glyph.getBbox().getX() + x_val;
			float newY = glyph.getBbox().getY() + y_val;
			glyph.getBbox().setX(newX);
			glyph.getBbox().setY(newY);

			if (glyph.getPort().size() > 0) {
				for (Port port : glyph.getPort()) {
					newX = port.getX() + x_val;
					newY = port.getY() + y_val;
					port.setX(newX);
					port.setY(newY);
				}
			}

			if (glyph.getGlyph().size() > 0) {
				moveGlyphs(glyph.getGlyph(), x_val, y_val);
			}
		}
	}

	private void moveArcs(List<Arc> arcList, float x_val, float y_val) {
		for (Arc arc : arcList) {
			float newX = arc.getStart().getX() + x_val;
			float newY = arc.getStart().getY() + y_val;
			arc.getStart().setX(newX);
			arc.getStart().setY(newY);

			newX = arc.getEnd().getX() + x_val;
			newY = arc.getEnd().getY() + y_val;
			arc.getEnd().setX(newX);
			arc.getEnd().setY(newY);

			if (arc.getNext().size() > 0) {
				for (Next n : arc.getNext()) {
					newX = n.getX() + x_val;
					newY = n.getY() + y_val;
					n.setX(newX);
					n.setY(newY);
				}
			}
		}
	}

	private void processNodes(Document doc, String szNotesTagId, String szCloneTagId, String szBqmodelIsTagId,
			String szBqmodelIsDescribedByTagId, String szBqbiolIsTagId, String szBqbiolIsDescribedByTagId,
			String szAnnotationTagId, String szNodeURLTagId, String szOrientationTagId, NodeList nList) {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				// get id of the node/glyph
				String szGlyphId = eElement.getAttribute(ID_ATTR);

				if ((!compoundComplexMap.containsKey(szGlyphId)) && (!complexSet.contains(szGlyphId))
						&& (!compartmentSet.contains(szGlyphId))) {
					Glyph _glyph = parseGlyphInfo(doc, szNotesTagId, szCloneTagId, szBqmodelIsTagId,
							szBqmodelIsDescribedByTagId, szBqbiolIsTagId, szBqbiolIsDescribedByTagId, szAnnotationTagId,
							szNodeURLTagId, szOrientationTagId, eElement, szGlyphId);

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

	private void processResources(NodeList nResourceList) {
		for (int temp = 0; temp < nResourceList.getLength(); temp++) {
			Node nResource = nResourceList.item(temp);

			if (nResource.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nResource;
				Glyph _glyph = new Glyph();

				// get id of the resources
				String szResourceId = eElement.getAttribute(ID_ATTR);
				_glyph.setId(szResourceId);

				NodeList _nlGenericNodeList = eElement.getElementsByTagName(FileUtils.Y_GENERIC_NODE);
				NodeList _nlShapeNodeList = eElement.getElementsByTagName(FileUtils.Y_SHAPE_NODE);

				if (_nlGenericNodeList.getLength() > 0) {
					String szYEDNodeType = ((Element) _nlGenericNodeList.item(0)).getAttribute("configuration");
					String szGlyphClass = parseYedNodeType(szYEDNodeType, false);
					_glyph.setClazz(szGlyphClass);
				} else if (_nlShapeNodeList.getLength() > 0) {
					_glyph.setClazz(FileUtils.SBGN_UNIT_OF_INFORMATION);
				}

				NodeList _nlNodeLabelList = eElement.getElementsByTagName(FileUtils.Y_NODE_LABEL);
				if (_nlNodeLabelList.getLength() > 0) {
					String szNodeText = _nlNodeLabelList.item(0).getTextContent().trim();

					if (!szNodeText.equals("")) {

						if (_glyph.getClazz().equals(FileUtils.SBGN_STATE_VARIABLE)) {
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
						} else if (_glyph.getClazz().equals(FileUtils.SBGN_UNIT_OF_INFORMATION)) {
							Label _label = new Label();
							_label.setText(szNodeText);
							_glyph.setLabel(_label);
						}
					}
				}

				// add the state variable to the corresponding glyph
				for (Entry<Pair<String, String>, ResourceCoordinates> _resource : resourceMap.entrySet()) {
					if (_resource.getKey().first.equals(szResourceId)) {
						NodeList nlGeometry = eElement.getElementsByTagName(FileUtils.Y_GEOMETRY);
						String szHeight = ((Element) (nlGeometry.item(0))).getAttribute(HEIGHT_ATTR);
						String szWidth = ((Element) (nlGeometry.item(0))).getAttribute(WIDTH_ATTR);

						Bbox bbox = new Bbox();
						bbox.setH(Float.parseFloat(szHeight));
						bbox.setW(Float.parseFloat(szWidth));
						if (_resource.getValue() != null) {
							bbox.setX(_resource.getValue().getXCoord());
							bbox.setY(_resource.getValue().getYCoord());
						}

						_glyph.setBbox(bbox);

						String szParentGlyphId = _resource.getKey().second;
						addGlyphToList(szParentGlyphId, _glyph, map.getGlyph());
					}
				}
			}
		}
	}

	private void processArcs(NodeList nEdgeList) {
		float fPort2PointDistance = 0;
		for (Glyph process : map.getGlyph()) {
			if (isProcessType(process)) {
				fPort2PointDistance = 2 * process.getBbox().getH();
				break;
			}
		}
		for (int temp = 0; temp < nEdgeList.getLength(); temp++) {
			Node nEdge = nEdgeList.item(temp);
			Arc arc = new Arc();

			if (nEdge.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nEdge;
				String szArrowDirection = processNodeList(eElement.getElementsByTagName(FileUtils.Y_ARROWS));
				boolean bEdgeToBeCorrected = setArcClazz(arc, szArrowDirection);

				// get id of the edge/arc
				String szArcAttributes = getElementAttributes(eElement).trim();

				String delims = "[\t]";
				String szArcId = "", szArcSource = "", szArcTarget = "";
				szArcAttributes = szArcAttributes.replaceAll("\"", "");
				String[] tokens = szArcAttributes.split(delims);
				float fStartX = 0, fStartY = 0, fStartH = 0, fStartW = 0;
				float fTargetX = 0, fTargetY = 0, fTargetH = 0, fTargetW = 0;

				for (int i = 0; i < tokens.length; i++) {
					if (tokens[i].contains("id=")) {
						szArcId = tokens[i].replaceAll("id=", "");
						arc.setId(szArcId);
					} else if (tokens[i].contains("source=")) {
						szArcSource = tokens[i].replaceAll("source=", "");

						Glyph g = null;
						for (Glyph _glyph : map.getGlyph()) {
							g = findGlyph(szArcSource, _glyph);
							if (null != g) {
								break;
							}
						}

						if (g != null) {
							if (bEdgeToBeCorrected) {
								arc.setTarget(g);
							} else {
								arc.setSource(g);
							}

							if (null != g.getBbox()) {
								fStartX = g.getBbox().getX();
								fStartY = g.getBbox().getY();
								fStartH = g.getBbox().getH();
								fStartW = g.getBbox().getW();
							}
						}

					} else if (tokens[i].contains("target=")) {
						szArcTarget = tokens[i].replaceAll("target=", "");

						Glyph g = null;
						for (Glyph _glyph : map.getGlyph()) {
							g = findGlyph(szArcTarget, _glyph);
							if (null != g) {
								break;
							}
						}

						if (g != null) {
							if (bEdgeToBeCorrected) {
								arc.setSource(g);
							} else {
								arc.setTarget(g);
							}

							if (null != g.getBbox()) {
								fTargetX = g.getBbox().getX();
								fTargetY = g.getBbox().getY();
								fTargetH = g.getBbox().getH();
								fTargetW = g.getBbox().getW();
							}
						}
					}

				}

				// if the process is a consumption, it is easy to draw from process to entity
				// and this can not be detected before
				if (arc.getClazz().equals(FileUtils.SBGN_CONSUMPTION)) {

					Glyph target = (Glyph) arc.getTarget();
					if (!isProcessType(target)) {
						target = (Glyph) arc.getSource();
						arc.setSource(arc.getTarget());
						arc.setTarget(target);
					}
				}

				// given the fact that the consumption line could be used also for the
				// representation of the logic or equivalence arcs, this has to be checked and
				// decoded accordingly
				if (arc.getClazz().equals(FileUtils.SBGN_CONSUMPTION)) {
					if ((arc.getSource() != null) && (arc.getTarget() != null)) {

						if (((Glyph) arc.getSource()).getClazz().toUpperCase().equals("OR")
								|| ((Glyph) arc.getTarget()).getClazz().toUpperCase().equals("OR")
								|| (((Glyph) arc.getSource()).getClazz().toUpperCase().equals("AND")
										|| ((Glyph) arc.getTarget()).getClazz().toUpperCase().equals("AND"))
								|| (((Glyph) arc.getSource()).getClazz().toUpperCase().equals("NOT")
										|| ((Glyph) arc.getTarget()).getClazz().toUpperCase().equals("NOT"))) {
							arc.setClazz(FileUtils.SBGN_LOGIC_ARC);
						} else if ((((Glyph) arc.getSource()).getClazz().equals(FileUtils.SBGN_SUBMAP))
								|| ((Glyph) arc.getSource()).getClazz().equals(FileUtils.SBGN_TAG)
								|| ((Glyph) arc.getTarget()).getClazz().equals(FileUtils.SBGN_SUBMAP)
								|| ((Glyph) arc.getTarget()).getClazz().equals(FileUtils.SBGN_TAG))
							arc.setClazz(FileUtils.SBGN_EQUIVALENCE_ARC);
					} else if (arc.getSource() == null) {
						System.out.println("source " + arc.getId() + " " + bEdgeToBeCorrected);
					} else if (arc.getTarget() == null) {
						System.out.println("target " + arc.getId());
					}
				}

				String szPathCoordinates = processNodeList(eElement.getElementsByTagName(FileUtils.Y_PATH));
				String delimsCoord = "[\t]";
				szPathCoordinates = szPathCoordinates.replaceAll("\"", "");
				String[] tokensCoordinates = szPathCoordinates.split(delimsCoord);
				if (tokensCoordinates.length == 4) {
					Start _start = new Start();
					String sx = tokensCoordinates[0].replaceAll("sx=", "");
					_start.setX(Float.parseFloat(sx) + fStartX + fStartW / 2);
					// _start.setX(Float.parseFloat(szSX));

					String sy = tokensCoordinates[1].replaceAll("sy=", "");
					_start.setY(Float.parseFloat(sy) + fStartY + fStartH / 2);
					// _start.setY(Float.parseFloat(szSY));

					arc.setStart(_start);

					End _end = new End();
					String tx = tokensCoordinates[2].replaceAll("tx=", "");
					_end.setX(Float.parseFloat(tx) + fTargetX + fTargetW / 2);
					// _end.setX(Float.parseFloat(szTX));
					String ty = tokensCoordinates[3].replaceAll("ty=", "");
					_end.setY(Float.parseFloat(ty) + fTargetY + fTargetH / 2);
					// _end.setY(Float.parseFloat(szTY));
					arc.setEnd(_end);
				}

				// for processes, ports must be set for consumption and production arcs
				// for logic operators, ports must be set for incoming arcs and for outgoing
				// regulatory arcs (catalysis, stimulation, inhibition, modulation, necessary
				// stimulation)
				setPortToArc(arc);

				String szPointInfo = processNodeList(eElement.getElementsByTagName(FileUtils.Y_POINT));
				setBendPoints(arc, szPointInfo, fPort2PointDistance);

				NodeList nlLineStyle = eElement.getElementsByTagName(FileUtils.Y_LINE_STYLE);
				// getting the border color info
				String szStrokeColorId = ((Element) (nlLineStyle.item(0))).getAttribute(COLOR_ATTR);
				colorSet.add(szStrokeColorId);

				// getting the stroke width info
				float fStrokeWidth = Float.parseFloat(((Element) (nlLineStyle.item(0))).getAttribute(WIDTH_ATTR));

				String szStyleId = STYLE_PREFIX + fStrokeWidth + szStrokeColorId.replaceFirst("#", "");

				if (!styleMap.containsKey(szStyleId)) {
					styleMap.put(szStyleId, new SBGNMLStyle(szStyleId, szStrokeColorId, fStrokeWidth));
				}
				styleMap.get(szStyleId).addElementIdToSet(eElement.getAttribute(ID_ATTR));

				NodeList nlCardinalityList = eElement.getElementsByTagName(FileUtils.Y_EDGE_LABEL);
				if (nlCardinalityList.getLength() > 0) {
					String szCardinality = nlCardinalityList.item(0).getTextContent().trim();
					if (!szCardinality.equals("")) {

						Glyph cardGlyph = new Glyph();
						cardGlyph.setClazz(FileUtils.SBGN_CARDINALITY);
						cardGlyph.setId(FileUtils.SBGN_CARDINALITY + "_" + szCardinality);
						Label _label = new Label();
						_label.setText(szCardinality);
						cardGlyph.setLabel(_label);
						Bbox cardBbox = new Bbox();
						cardBbox.setH(0);
						cardBbox.setW(0);
						cardBbox.setX(0);
						cardBbox.setY(0);
						cardGlyph.setBbox(cardBbox);
						arc.getGlyph().add(cardGlyph);
					}
				}
			}

			// add the arc to the map
			map.getArc().add(arc);
		}
	}

	private void setBendPoints(Arc arc, String szPointInfo, float fPort2PointDistance) {

		if (!szPointInfo.isEmpty()) {
			String delims = "[\t]";
			float fXCoord = 0, fYCoord = 0;
			szPointInfo = szPointInfo.replaceAll("\"", "");
			String[] tokensBendPoint = szPointInfo.split(delims);

			for (int i = 0; i < tokensBendPoint.length - 1; i += 2) {
				if (tokensBendPoint[i].contains("x=")) {
					fXCoord = Float.parseFloat(tokensBendPoint[i].replaceAll("x=", ""));
				}
				if (tokensBendPoint[i + 1].contains("y=")) {
					fYCoord = Float.parseFloat(tokensBendPoint[i + 1].replaceAll("y=", ""));
				}

				boolean bFoundPort = false;
				if (arc.getSource() instanceof Port) {
					Port p = (Port) arc.getSource();
					if (getPointDistance(p.getX(), p.getY(), fXCoord, fYCoord) <= fPort2PointDistance) {
						bFoundPort = true;
					}
				} else if (arc.getTarget() instanceof Port) {
					Port p = (Port) arc.getTarget();
					if (getPointDistance(p.getX(), p.getY(), fXCoord, fYCoord) <= fPort2PointDistance) {
						bFoundPort = true;
					}
				}
				if (!bFoundPort) {

					boolean bBendPointFound = false;
					for (Next n : arc.getNext()) {
						if ((n.getX() == fXCoord) && (n.getY() == fYCoord)) {
							bBendPointFound = true;
							break;
						}
					}

					if (!bBendPointFound) {
						Next next = new Next();
						next.setX(fXCoord);
						next.setY(fYCoord);
						arc.getNext().add(next);
					}
				}
			}
		}
	}

	private boolean setArcClazz(Arc arc, String szArrowDirection) {
		String szArcType = FileUtils.SBGN_CONSUMPTION;
		boolean bEdgeToBeCorrected = false;

		if (szArrowDirection.contains("white_delta_bar")) {
			szArcType = FileUtils.SBGN_NECESSARY_STIMULATION;
			if (szArrowDirection.contains("source=\"white_delta_bar\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("white_diamond")) {
			szArcType = FileUtils.SBGN_MODULATION;
			if (szArrowDirection.contains("source=\"white_diamond\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("t_shape")) {
			szArcType = FileUtils.SBGN_INHIBITION;
			if (szArrowDirection.contains("source=\"t_shape\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("white_delta")) {
			szArcType = FileUtils.SBGN_STIMULATION;
			if (szArrowDirection.contains("source=\"white_circle\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("delta")) {
			szArcType = FileUtils.SBGN_PRODUCTION;
			if (szArrowDirection.contains("source=\"delta\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("white_circle")) {
			szArcType = FileUtils.SBGN_CATALYSIS;
			if (szArrowDirection.contains("source=\"white_circle\"")) {
				bEdgeToBeCorrected = true;
			}
		}

		arc.setClazz(szArcType);
		return bEdgeToBeCorrected;
	}

	private Glyph findGlyph(String szId, Glyph g) {
		if (g.getId().equals(szId)) {
			return g;
		}
		List<Glyph> innerGlyphs = g.getGlyph();
		Glyph res = null;
		for (int i = 0; res == null && i < innerGlyphs.size(); i++) {
			res = findGlyph(szId, innerGlyphs.get(i));
		}
		return res;
	}

	private boolean isProcessType(Glyph source) {
		boolean bIsProcess = false;
		if (source.getClazz().equals(FileUtils.SBGN_PROCESS)
				|| (source.getClazz().equals(FileUtils.SBGN_UNCERTAIN_PROCESS))
				|| (source.getClazz().equals(FileUtils.SBGN_OMITTED_PROCESS))) {
			bIsProcess = true;
		}
		return bIsProcess;
	}

	private boolean isOperatorType(Glyph source) {
		boolean bIsOperator = false;
		if (source.getClazz().equals(FileUtils.SBGN_AND) || (source.getClazz().equals(FileUtils.SBGN_OR))
				|| (source.getClazz().equals(FileUtils.SBGN_NOT))) {
			bIsOperator = true;
		}
		return bIsOperator;
	}

	private void setCompartmentRefToGlyph(String szParentCompartmentId, Glyph _glyph, List<Glyph> _listOfGlyphs) {
		for (Glyph _compartment : _listOfGlyphs) {
			if (_compartment.getId().equals(szParentCompartmentId)) {
				_glyph.setCompartmentRef(_compartment);
			} else {
				setCompartmentRefToGlyph(szParentCompartmentId, _glyph, _compartment.getGlyph());
			}
		}

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

	private void setStyle(Element eElement, String szId) {
		String szFillColorId = ((Element) (eElement.getElementsByTagName(FileUtils.Y_FILL).item(0)))
				.getAttribute(COLOR_ATTR);
		colorSet.add(szFillColorId);

		NodeList nlBorderStyle = eElement.getElementsByTagName(FileUtils.Y_BORDER_STYLE);
		// getting the border color info
		String szStrokeColorId = ((Element) (nlBorderStyle.item(0))).getAttribute(COLOR_ATTR);
		colorSet.add(szStrokeColorId);

		// getting the stroke width color info
		float fStrokeWidth = Float.parseFloat(((Element) (nlBorderStyle.item(0))).getAttribute(WIDTH_ATTR));

		// getting the stroke width color info
		String szStrokeColorWidth = ((Element) (eElement.getElementsByTagName(FileUtils.Y_NODE_LABEL).item(0)))
				.getAttribute(FONTSIZE_ATTR);
		float fFontSize = FileUtils.DEFAULT_FONT_SIZE;
		if (!szStrokeColorWidth.equals("")) {
			fFontSize = Float.parseFloat(szStrokeColorWidth);
		}

		String szStyleId = STYLE_PREFIX + fStrokeWidth + szFillColorId.replaceFirst("#", "") + fFontSize
				+ szStrokeColorId.replaceFirst("#", "");
		if (!styleMap.containsKey(szStyleId)) {
			styleMap.put(szStyleId,
					new SBGNMLStyle(szStyleId, szFillColorId, szStrokeColorId, fStrokeWidth, fFontSize));
		}
		styleMap.get(szStyleId).addElementIdToSet(szId);
	}

	private Glyph parseGlyphInfo(Document doc, String szNotesTagId, String szCloneTagId, String szBqmodelIsTagId,
			String szBqmodelIsDescribedByTagId, String szBqbiolIsTagId, String szBqbiolIsDescribedByTagId,
			String szAnnotationTagId, String szNodeURLTagId, String szOrientationTagId, Element eElement,
			String szGlyphId) {
		Glyph _glyph = new Glyph();
		_glyph.setId(szGlyphId);

		boolean bIsMultimer = false;
		// the multimer is represented by a macromolecule having the property
		// <y:Property class="java.lang.Integer" name="com.yworks.sbgn.style.mcount"
		// value="2"/> in GraphML
		NodeList _nlNodePropertiesList = (eElement.getElementsByTagName(FileUtils.Y_PROPERTY));
		for (int i = 0; i < _nlNodePropertiesList.getLength(); i++) {
			Element _elem = (Element) _nlNodePropertiesList.item(i);
			if (_elem.getAttribute("name").equals(FileUtils.COM_YWORKS_SBGN_STYLE_MCOUNT)) {
				bIsMultimer = true;
				break;
			}
		}

		// setting the glyph class
		NodeList _nlConfigList = eElement.getElementsByTagName(FileUtils.Y_GENERIC_GROUP_NODE);

		if (0 == _nlConfigList.getLength()) {
			_nlConfigList = eElement.getElementsByTagName(FileUtils.Y_GENERIC_NODE);
		}

		if (_nlConfigList.getLength() > 0) {
			String szYEDNodeType = ((Element) _nlConfigList.item(0)).getAttribute("configuration");
			String szGlyphClass = parseYedNodeType(szYEDNodeType, bIsMultimer);
			NodeList _nlNodeLabelList = eElement.getElementsByTagName(FileUtils.Y_NODE_LABEL);

			String szTextContent = _nlNodeLabelList.item(0).getTextContent().trim();

			if (szGlyphClass.equals(FileUtils.SBGN_PROCESS) && szTextContent.equals("?")) {
				szGlyphClass = FileUtils.SBGN_UNCERTAIN_PROCESS;
			} else if (szGlyphClass.equals(FileUtils.SBGN_PROCESS) && szTextContent.equals("\\\\")) {
				szGlyphClass = FileUtils.SBGN_OMITTED_PROCESS;
			}

			if (!szGlyphClass.equals("operator")) {

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
			setBbox(_glyph, eElement.getElementsByTagName(FileUtils.Y_GEOMETRY));

			// the glyph has resouces (i.e. state variables, multimer states etc.)
			if (_nlNodeLabelList.getLength() > 1) {
				for (int i = 1; i < _nlNodeLabelList.getLength(); i++) {
					Element _element = (Element) _nlNodeLabelList.item(i);

					if (_element.hasAttribute(ICON_DATA_ATTR)) {
						Pair pair = new Pair(_element.getAttribute(ICON_DATA_ATTR), szGlyphId);
						if (!resourceMap.containsKey(pair)) {
							resourceMap.put(pair,
									new ResourceCoordinates(_glyph.getBbox().getX(), _glyph.getBbox().getY()));
							if (_element.hasAttribute("x")) {
								resourceMap.get(pair).updateXCoord(Float.parseFloat(_element.getAttribute("x")));
							}
							if (_element.hasAttribute("y")) {
								resourceMap.get(pair).updateYCoord(Float.parseFloat(_element.getAttribute("y")));
							}
						}
					}

					// the glyph is a multimer (the shape from the yEd SBGN palette was used for
					// drawing) and the child glyph is an unit of information
					if ((_element.getTextContent().trim().contains("N:"))
							|| (_element.getTextContent().trim().contains("RNA"))
							|| (_element.getTextContent().trim().toUpperCase().contains("RECEPTOR"))) {
						Glyph _uofGlyph = new Glyph();
						_uofGlyph.setClazz(FileUtils.SBGN_UNIT_OF_INFORMATION);
						_uofGlyph.setId(_glyph.getId() + "_uof_" + i);
						Label _label = new Label();
						_label.setText(_element.getTextContent().trim());
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
			}

			// setting style info
			setStyle(eElement, szGlyphId);

			// parse data information on notes, annotation, orientation, clone etc.
			NodeList nlDataList = eElement.getElementsByTagName(DATA_TAG);

			Element eltAnnotation = doc.createElement(ANNOTATION_TAG);
			// TODO: to read the namespace from the file
			Element rdfRDF = doc.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", RDF_RDF_TAG);
			eltAnnotation.appendChild(rdfRDF);
			Element rdfDescription = doc.createElement(RDF_DESCRIPTION_TAG);
			rdfRDF.appendChild(rdfDescription);
			rdfDescription.setAttribute(RDF_ABOUT_TAG, "#" + _glyph.getId());

			for (int temp2 = 0; temp2 < nlDataList.getLength(); temp2++) {
				Element _element = ((Element) (nlDataList.item(temp2)));

				// parse notes information

				/*
				 * if (_element.getAttribute(KEY_TAG).equals(szNotesTagId)) {
				 * _glyph.setNotes(getSBGNNotes(_element)); }
				 */

				// setting the orientation value for the SBGN process
				if (_element.getAttribute(KEY_TAG).equals(szOrientationTagId)) {
					if (isProcessType(_glyph)) {
						_glyph.setOrientation(_element.getTextContent());
					}
				}

				// parse annotation information
				else if (_element.getAttribute(KEY_TAG).equals(szAnnotationTagId)) {
					String szText = _element.getTextContent();
					if (!szText.equals("")) {
						szText = szText.replaceAll("\"", "");
						String delims = " ";
						String[] tokens = szText.split(delims);
						for (int i = 0; i < tokens.length; i++) {
							String value = tokens[i].substring(tokens[i].indexOf("=") + 1);
							if (tokens[i].contains(XMLNS_N2_NS + "=")) {
								eltAnnotation.setAttribute(XMLNS_N2_NS, value);
							} else if (tokens[i].contains(XMLNS_NS + "=")) {
								eltAnnotation.setAttribute(XMLNS_NS, value);
							}
						}
					}
				}

				// parse namespace information
				else if (_element.getAttribute(KEY_TAG).equals(szNodeURLTagId)) {
					String szText = _element.getTextContent();
					if (!szText.equals("")) {
						szText = szText.replaceAll("\"", "");
						String delims = " ";
						String[] tokens = szText.split(delims);
						for (int i = 0; i < tokens.length; i++) {
							String value = tokens[i].substring(tokens[i].indexOf("=") + 1);
							if (!value.equals("")) {

								if (tokens[i].contains(XMLNS_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_NS, value);
								} else if (tokens[i].contains(XMLNS_BQBIOL_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_BQBIOL_NS, value);
								} else if (tokens[i].contains(XMLNS_BQMODEL_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_BQMODEL_NS, value);
								} else if (tokens[i].contains(XMLNS_CELL_DESIGNER_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_CELL_DESIGNER_NS, value);
								} else if (tokens[i].contains(XMLNS_DC_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_DC_NS, value);
								} else if (tokens[i].contains(XMLNS_DC_TERMS_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_DC_TERMS_NS, value);
								} else if (tokens[i].contains(XMLNS_VCARD_NS + "=")) {
									rdfRDF.setAttribute(XMLNS_VCARD_NS, value);
								}
							}
						}
					}
				}

				// parse clone information
				else if (_element.getAttribute(KEY_TAG).equals(szCloneTagId)) {
					if (!_element.getTextContent().equals("")) {
						Label _label = new Label();
						String text = "";
						// This is in concordance with the SBGN to GraphML translation as sometimes,
						// label text within in the clone is not specified, but it is usually equal to
						// empty space. Thus, the condition if from above would be false and the clone
						// would not be set. The FlieUtils.CloneIsSet was introduced in order to show
						// that the glyph has a clone that has to be set, even if the label text is an
						// empty string. At the decoding step, this additional information must be
						// removed.
						if (!_element.getTextContent().equals(FileUtils.CloneIsSet)) {
							text = _element.getTextContent();
						}
						_label.setText(text);
						Clone _clone = new Clone();
						_clone.setLabel(_label);
						_glyph.setClone(_clone);
					}
				}

				// parse bqmodel:is information
				else if (_element.getAttribute(KEY_TAG).equals(szBqmodelIsTagId)) {
					String szText = _element.getTextContent();
					if (!szText.equals("")) {
						String delimsLine = "[\n]";
						String[] tokens = szText.split(delimsLine);
						for (int i = 0; i < tokens.length; i++) {
							Element elBqtModelIs = doc.createElement(FileUtils.BQMODEL_IS);
							// add rdf:Bag
							Element eltRDFBag = doc.createElement(RDF_BAG_TAG);
							elBqtModelIs.appendChild(eltRDFBag);
							Element eltRDFLi = doc.createElement(RDF_LI_TAG);
							eltRDFLi.setAttribute(RDF_RESOURCE_TAG, tokens[i]);
							eltRDFBag.appendChild(eltRDFLi);
							rdfDescription.appendChild(elBqtModelIs);
						}
					}
				}

				// parse bqmodel:isDescribedBy information
				else if (_element.getAttribute(KEY_TAG).equals(szBqmodelIsDescribedByTagId)) {
					String szText = _element.getTextContent().trim();

					if (!szText.equals("")) {
						String delimsLine = "[\n]";
						String[] tokens = szText.split(delimsLine);
						for (int i = 0; i < tokens.length; i++) {
							Element elBqtModelIsDescribedBy = doc.createElement(FileUtils.BQMODEL_IS_DESCRIBED_BY);
							// add rdf:Bag
							Element eltRDFBag = doc.createElement(RDF_BAG_TAG);
							elBqtModelIsDescribedBy.appendChild(eltRDFBag);
							Element eltRDFLi = doc.createElement(RDF_LI_TAG);
							eltRDFLi.setAttribute(RDF_RESOURCE_TAG, tokens[i]);
							eltRDFBag.appendChild(eltRDFLi);
							rdfDescription.appendChild(elBqtModelIsDescribedBy);
						}
					}
				}

				// parse bqbiol:is information
				else if (_element.getAttribute(KEY_TAG).equals(szBqbiolIsTagId)) {
					String szText = _element.getTextContent();
					if (!szText.equals("")) {
						Element eltBqbiolIs = doc.createElement(FileUtils.BQBIOL_IS);
						// add rdf:Bag
						Element eltRDFBag = doc.createElement(RDF_BAG_TAG);
						eltBqbiolIs.appendChild(eltRDFBag);

						String delimsLine = "[\n]";
						String[] tokens = szText.split(delimsLine);
						for (int i = 0; i < tokens.length; i++) {
							Element eltRDFLi = doc.createElement(RDF_LI_TAG);
							eltRDFLi.setAttribute(RDF_RESOURCE_TAG, tokens[i]);
							eltRDFBag.appendChild(eltRDFLi);
						}
						rdfDescription.appendChild(eltBqbiolIs);
					}
				}

				// parse bqbiol:isDescribedBy information
				else if (_element.getAttribute(KEY_TAG).equals(szBqbiolIsDescribedByTagId)) {
					String szText = _element.getTextContent().trim();

					if (!szText.equals("")) {
						String delimsLine = "[\n]";
						String[] tokens = szText.split(delimsLine);
						for (int i = 0; i < tokens.length; i++) {
							Element elBqbiolIsDescribedBy = doc.createElement(FileUtils.BQBIOL_IS_DESCRIBED_BY);
							// add rdf:Bag
							Element eltRDFBag = doc.createElement(RDF_BAG_TAG);
							elBqbiolIsDescribedBy.appendChild(eltRDFBag);
							Element eltRDFLi = doc.createElement(RDF_LI_TAG);
							eltRDFLi.setAttribute(RDF_RESOURCE_TAG, tokens[i]);
							eltRDFBag.appendChild(eltRDFLi);
							rdfDescription.appendChild(elBqbiolIsDescribedBy);
						}
					}
				}
			}

			Extension _extension = new Extension();
			_extension.getAny().add(eltAnnotation);
			_glyph.setExtension(_extension);
		}
		return _glyph;
	}

	private void setBbox(Glyph _glyph, NodeList nlGeometry) {
		Bbox bbox = new Bbox();

		String szHeight = ((Element) (nlGeometry.item(0))).getAttribute(HEIGHT_ATTR);
		String szWidth = ((Element) (nlGeometry.item(0))).getAttribute(WIDTH_ATTR);
		String szXPos = ((Element) (nlGeometry.item(0))).getAttribute(X_POS_ATTR);
		String szYPos = ((Element) (nlGeometry.item(0))).getAttribute(Y_POS_ATTR);

		bbox.setH(Float.parseFloat(szHeight));
		bbox.setW(Float.parseFloat(szWidth));
		bbox.setX(Float.parseFloat(szXPos));
		bbox.setY(Float.parseFloat(szYPos));
		_glyph.setBbox(bbox);
	}

	public Notes getSBGNNotes(Element notes) {
		Notes newNotes = new Notes();
		if (notes != null) {
			newNotes.getAny().add(notes);
			return newNotes;
		}
		return null;
	}

	private void addExtension(Document doc) {
		// add extension data
		Extension ext = new Extension();
		// add render information tag
		Element eltRenderInfo = doc.createElementNS("http://www.sbml.org/sbml/level3/version1/render/version1",
				"renderInformation");
		eltRenderInfo.setAttribute(ID_ATTR, "renderInformation");
		eltRenderInfo.setAttribute("backgroundColor", "#ffffff");
		eltRenderInfo.setAttribute("programName", "graphml2sbgn");
		eltRenderInfo.setAttribute("programVersion", "0.1");

		// add list of colors
		Element eltListOfColor = doc.createElement(LIST_OF_COLOR_DEFINITIONS_TAG);
		eltRenderInfo.appendChild(eltListOfColor);

		int i = 0;
		for (String _color : colorSet) {
			i++;
			colorMap.put(_color, COLOR_PREFIX + i);
		}

		for (Entry<String, String> e : colorMap.entrySet()) {
			Element eltColorId = doc.createElement(COLOR_DEFINITION_TAG);
			eltColorId.setAttribute(ID_ATTR, e.getValue());
			eltColorId.setAttribute(VALUE_ATTR, e.getKey());
			eltListOfColor.appendChild(eltColorId);
		}

		// add list of styles
		Element eltListOfStyles = doc.createElement(LIST_OF_STYLES_TAG);
		eltRenderInfo.appendChild(eltListOfStyles);
		for (Entry<String, SBGNMLStyle> e : styleMap.entrySet()) {
			Element eltStyleId = doc.createElement(STYLE_TAG);
			eltStyleId.setAttribute(ID_ATTR, e.getKey());
			eltStyleId.setAttribute(ID_LIST_ATTR, e.getValue().getElementSet());

			// add graphics of the style
			Element graphics = doc.createElement(GRAPHICS_TAG);
			graphics.setAttribute(FILL_ATTR, colorMap.get(e.getValue().getFillColor()));
			graphics.setAttribute(FONTSIZE_ATTR, Float.toString(e.getValue().getFontSize()));
			graphics.setAttribute(STROKE_ATTR, colorMap.get(e.getValue().getStrokeColor()));
			graphics.setAttribute(STROKE_WIDTH_ATTR, Float.toString(e.getValue().getStrokeWidth()));
			eltStyleId.appendChild(graphics);

			eltListOfStyles.appendChild(eltStyleId);
		}

		ext.getAny().add(eltRenderInfo);

		map.setExtension(ext);
	}

	private String parseYedNodeType(String szType, boolean bIsMultimer) {
		String szGlyphClass = "";
		if (szType.contains(FileUtils.COM_YWORKS_SBGN_SIMPLE_CHEMICAL)) {
			if (bIsMultimer) {
				szGlyphClass = FileUtils.SBGN_SIMPLE_CHEMICAL_MULTIMER;
			} else {
				szGlyphClass = FileUtils.SBGN_SIMPLE_CHEMICAL;
			}
		} else if (szType.contains(FileUtils.COM_YWORKS_SBGN_PROCESS)) {
			szGlyphClass = FileUtils.SBGN_PROCESS;
		}
		// Unspecified entity
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_UNSPECIFIED_ENTITY)) {
			szGlyphClass = FileUtils.SBGN_UNSPECIFIED_ENTITY;
		}
		// Perturbing agent
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_PERTURBING_AGENT)) {
			szGlyphClass = FileUtils.SBGN_PERTURBING_AGENT;
		}
		// Phenotype
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_PHENOTYPE)) {
			szGlyphClass = FileUtils.SBGN_PHENOTYPE;
		}
		// nucleic acid feature
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_NUCLEIC_ACID_FEATURE)) {
			if (bIsMultimer) {
				szGlyphClass = FileUtils.SBGN_NUCLEIC_ACID_FEATURE_MULTIMER;
			} else {
				szGlyphClass = FileUtils.SBGN_NUCLEIC_ACID_FEATURE;
			}
		}
		// submap
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_SUBMAP)) {
			szGlyphClass = FileUtils.SBGN_SUBMAP;
		}
		// Macromolecule
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_MACROMOLECULE)) {
			// the multimer is represented by a macromolecule having the property
			// <y:Property class="java.lang.Integer" name="com.yworks.sbgn.style.mcount"
			// value="2"/> in GraphML
			if (bIsMultimer) {
				szGlyphClass = FileUtils.SBGN_MACROMOLECULE_MULTIMER;
			}
			// macromolecule
			else {
				szGlyphClass = FileUtils.SBGN_MACROMOLECULE;
			}
		}
		// Complex
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_COMPLEX)) {
			if (bIsMultimer) {
				szGlyphClass = FileUtils.SBGN_COMPLEX_MULTIMER;
			} else {
				szGlyphClass = FileUtils.SBGN_COMPLEX;
			}
		}
		// Tag
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_TAG)) {
			szGlyphClass = FileUtils.SBGN_TAG;
		}
		// State variable
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_STATE_VARIABLE)) {
			szGlyphClass = FileUtils.SBGN_STATE_VARIABLE;
		}
		// State variable
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_UNIT_OF_INFORMATION)) {
			szGlyphClass = FileUtils.SBGN_UNIT_OF_INFORMATION;
		}
		// Operator
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_OPERATOR)) {
			szGlyphClass = "operator";
		}
		// source and sink
		else if (szType.contains(FileUtils.COM_YWORKS_SBGN_SOURCE_AND_SINK)) {
			szGlyphClass = FileUtils.SBGN_SOURCE_AND_SINK;
		}

		return szGlyphClass;
	}

	private String processNodeList(NodeList nodeList) {
		String szContent = "";
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			Node nNode = nodeList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				szContent = szContent.concat(getElementAttributes(eElement));
			}
		}
		return szContent;
	}

	private String getElementAttributes(Element eElement) {
		String szAttributeValues = "";
		for (int i = 0; i < eElement.getAttributes().getLength(); i++) {
			szAttributeValues = szAttributeValues.concat(eElement.getAttributes().item(i) + "\t");
		}
		return szAttributeValues;
	}

	private void createPorts(List<Glyph> list) {
		for (Glyph glyph : list) {
			if (isOperatorType(glyph) || isProcessType(glyph)) {
				if (glyph.getPort() != null) {
					if (glyph.getPort().size() != MAX_PORT_NO) {
						for (int i = 0; i < MAX_PORT_NO; i++) {
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
		port.setId(glyph.getId() + "." + i);

		float x = 0;
		float y = 0;

		// the glyph (process/ operator) is oriented vertically, so the ports will be
		// located top/down
		if (glyph.getOrientation().equals("vertical")) {
			x = glyph.getBbox().getX();

			if (0 == i) {
				y = glyph.getBbox().getY() - (float) PORT2GLYPH_DISTANCE;
			} else {
				y = glyph.getBbox().getY() + glyph.getBbox().getH() + (float) PORT2GLYPH_DISTANCE;
			}
		}
		// the glyph (process/ operator) is oriented horizontally, so the ports will be
		// located left-right
		else {
			if (0 == i) {
				x = glyph.getBbox().getX() - (float) PORT2GLYPH_DISTANCE;
			} else {
				x = glyph.getBbox().getX() + glyph.getBbox().getW() + (float) PORT2GLYPH_DISTANCE;
			}
			y = glyph.getBbox().getY();
		}

		port.setX(x);
		port.setY(y);
		glyph.getPort().add(port);
	}

	private void setPortToArc(Arc arc) {

		// if the port has not been set yet, i.e. the arc source/ target is an instance
		// of the Glyph class and not of the Port class yet
		Port currentPort = null;
		Port alternativePort = null;
		Glyph glyph = null;

		if (arc.getClazz().equals(FileUtils.SBGN_PRODUCTION)) {
			if (arc.getSource() instanceof Glyph) {
				glyph = (Glyph) arc.getSource();
				Port port1 = glyph.getPort().get(0);
				Port port2 = glyph.getPort().get(1);

				float point_x = glyph.getBbox().getX();
				float point_y = glyph.getBbox().getY();

				if (arc.getNext().size() > 0) {
					Next bendPoint = getClosestBendPoint(glyph, arc);
					point_x = bendPoint.getX();
					point_y = bendPoint.getY();
				} else {
					point_x = arc.getEnd().getX();
					point_y = arc.getEnd().getY();
				}

				float dist1 = getPointDistance(point_x, point_y, port1.getX(), port1.getY());
				float dist2 = getPointDistance(point_x, point_y, port2.getX(), port2.getY());

				if (dist1 <= dist2) {
					currentPort = port1;
					alternativePort = port2;
				} else {
					currentPort = port2;
					alternativePort = port1;
				}
			}
		} else if (arc.getClazz().equals(FileUtils.SBGN_CONSUMPTION)) {
			if (arc.getTarget() instanceof Glyph) {

				glyph = (Glyph) arc.getTarget();

				if (glyph.getPort() != null) {
					if (glyph.getPort().size() == MAX_PORT_NO) {
						Port port1 = glyph.getPort().get(0);
						Port port2 = glyph.getPort().get(1);

						float point_x = glyph.getBbox().getX();
						float point_y = glyph.getBbox().getY();

						if (arc.getNext().size() > 0) {
							Next bendPoint = getClosestBendPoint(glyph, arc);
							point_x = bendPoint.getX();
							point_y = bendPoint.getY();
						} else {
							point_x = arc.getStart().getX();
							point_y = arc.getStart().getY();
						}

						float dist1 = getPointDistance(point_x, point_y, port1.getX(), port1.getY());
						float dist2 = getPointDistance(point_x, point_y, port2.getX(), port2.getY());

						if (dist1 <= dist2) {
							currentPort = port1;
							alternativePort = port2;
						} else {
							currentPort = port2;
							alternativePort = port1;
						}
					}
				}
			}
		}

		else if (arc.getClazz().equals(FileUtils.SBGN_LOGIC_ARC)) {
			if (arc.getTarget() instanceof Glyph) {
				glyph = (Glyph) arc.getTarget();
				if (isOperatorType(glyph)) {
					Port port1 = glyph.getPort().get(0);
					Port port2 = glyph.getPort().get(1);

					float point_x = glyph.getBbox().getX();
					float point_y = glyph.getBbox().getY();

					if (arc.getNext().size() > 0) {
						Next bendPoint = getClosestBendPoint(glyph, arc);
						point_x = bendPoint.getX();
						point_y = bendPoint.getY();
					} else {
						point_x = arc.getStart().getX();
						point_y = arc.getStart().getY();
					}

					float dist1 = getPointDistance(point_x, point_y, port1.getX(), port1.getY());
					float dist2 = getPointDistance(point_x, point_y, port2.getX(), port2.getY());

					if (dist1 <= dist2) {
						currentPort = port1;
						alternativePort = port2;
					} else {
						currentPort = port2;
						alternativePort = port1;
					}
				}
			}
		} else if (arc.getClazz().equals(FileUtils.SBGN_CATALYSIS) || arc.getClazz().equals(FileUtils.SBGN_STIMULATION)
				|| arc.getClazz().equals(FileUtils.SBGN_INHIBITION) || arc.getClazz().equals(FileUtils.SBGN_MODULATION)
				|| arc.getClazz().equals(FileUtils.SBGN_NECESSARY_STIMULATION)) {
			if (arc.getSource() instanceof Glyph) {
				glyph = (Glyph) arc.getSource();
				if (isOperatorType(glyph)) {
					Port port1 = glyph.getPort().get(0);
					Port port2 = glyph.getPort().get(1);

					float point_x = glyph.getBbox().getX();
					float point_y = glyph.getBbox().getY();

					if (arc.getNext().size() > 0) {
						Next bendPoint = getClosestBendPoint(glyph, arc);
						point_x = bendPoint.getX();
						point_y = bendPoint.getY();
					} else {
						point_x = arc.getStart().getX();
						point_y = arc.getStart().getY();
					}

					float dist1 = getPointDistance(point_x, point_y, port1.getX(), port1.getY());
					float dist2 = getPointDistance(point_x, point_y, port2.getX(), port2.getY());

					if (dist1 <= dist2) {
						currentPort = port1;
						alternativePort = port2;
					} else {
						currentPort = port2;
						alternativePort = port1;
					}
				}
			}
		}

		if (currentPort != null) {
			if (!port_arc_map.containsKey(currentPort.getId())) {
				port_arc_map.put(currentPort.getId(), new PortArcsRelationship(currentPort));
				port_arc_map.put(alternativePort.getId(), new PortArcsRelationship(alternativePort));
				port_arc_map.get(currentPort.getId()).addArcToSet(arc);

				if (arc.getClazz().equals(FileUtils.SBGN_PRODUCTION)) {
					if (!isReversibleProcess(glyph)) {
						port_arc_map.get(currentPort.getId()).setType(FileUtils.PortType.SourcePort);
						port_arc_map.get(alternativePort.getId()).setType(FileUtils.PortType.TargetPort);
					} else {
						port_arc_map.get(currentPort.getId()).setType(FileUtils.PortType.SourcePort);
						port_arc_map.get(alternativePort.getId()).setType(FileUtils.PortType.SourcePort);
					}
				} else if (arc.getClazz().equals(FileUtils.SBGN_CONSUMPTION)) {
					port_arc_map.get(currentPort.getId()).setType(FileUtils.PortType.TargetPort);
					port_arc_map.get(alternativePort.getId()).setType(FileUtils.PortType.SourcePort);
				} else if (arc.getClazz().equals(FileUtils.SBGN_LOGIC_ARC)) {
					port_arc_map.get(currentPort.getId()).setType(FileUtils.PortType.TargetPort);
					port_arc_map.get(alternativePort.getId()).setType(FileUtils.PortType.SourcePort);
				} else if (arc.getClazz().equals(FileUtils.SBGN_CATALYSIS)
						|| arc.getClazz().equals(FileUtils.SBGN_STIMULATION)
						|| arc.getClazz().equals(FileUtils.SBGN_INHIBITION)
						|| arc.getClazz().equals(FileUtils.SBGN_MODULATION)
						|| arc.getClazz().equals(FileUtils.SBGN_NECESSARY_STIMULATION)) {
					port_arc_map.get(currentPort.getId()).setType(FileUtils.PortType.SourcePort);
					port_arc_map.get(alternativePort.getId()).setType(FileUtils.PortType.TargetPort);
				}
			}
			// the port exists already in the map, and it has already some arcs assigned to
			// it. It must check if the existent arcs have the same clazz; if there are arcs
			// of different clazz, they must be assigned to the alternative port
			else {

				if (arc.getClazz().equals(FileUtils.SBGN_PRODUCTION)) {
					if (port_arc_map.get(currentPort.getId()).getPortType() == PortType.SourcePort) {
						port_arc_map.get(currentPort.getId()).addArcToSet(arc);
					} else {
						port_arc_map.get(alternativePort.getId()).addArcToSet(arc);
					}
				} else if (arc.getClazz().equals(FileUtils.SBGN_CONSUMPTION)) {
					if (port_arc_map.get(currentPort.getId()).getPortType() == PortType.TargetPort) {
						port_arc_map.get(currentPort.getId()).addArcToSet(arc);
					} else {
						port_arc_map.get(alternativePort.getId()).addArcToSet(arc);
					}
				} else if (arc.getClazz().equals(FileUtils.SBGN_LOGIC_ARC)) {

					if (port_arc_map.get(currentPort.getId()).getPortType() == PortType.TargetPort) {
						port_arc_map.get(currentPort.getId()).addArcToSet(arc);
					} else {
						port_arc_map.get(alternativePort.getId()).addArcToSet(arc);
					}
				} else if (arc.getClazz().equals(FileUtils.SBGN_CATALYSIS)
						|| arc.getClazz().equals(FileUtils.SBGN_STIMULATION)
						|| arc.getClazz().equals(FileUtils.SBGN_INHIBITION)
						|| arc.getClazz().equals(FileUtils.SBGN_MODULATION)
						|| arc.getClazz().equals(FileUtils.SBGN_NECESSARY_STIMULATION)) {

					if (port_arc_map.get(currentPort.getId()).getPortType() == PortType.SourcePort) {
						port_arc_map.get(currentPort.getId()).addArcToSet(arc);
					} else {
						port_arc_map.get(alternativePort.getId()).addArcToSet(arc);
					}
				}
			}
		}
	}

	private void assignArcsToPorts() {

		for (Entry<String, PortArcsRelationship> entry : port_arc_map.entrySet()) {
			if (entry.getValue().getPortType() == PortType.SourcePort) {
				for (Arc a : entry.getValue().getConnectedArcs()) {
					a.setSource(entry.getValue().getPort());
				}
			} else if (entry.getValue().getPortType() == PortType.TargetPort) {
				for (Arc a : entry.getValue().getConnectedArcs()) {
					a.setTarget(entry.getValue().getPort());
				}
			}
		}
	}

	private float getPointDistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	private void correctPortOrientationAndConnectedArcs() {

		for (Glyph glyph : map.getGlyph()) {
			int horizontal = 0;
			int vertical = 0;

			if (isProcessType(glyph)) {
				if (!isReversibleProcess(glyph)) {
					for (Arc arc : map.getArc()) {
						if (arc.getClazz().equals(FileUtils.SBGN_CONSUMPTION)
								|| arc.getClazz().equals(FileUtils.SBGN_PRODUCTION)) {
							boolean bArcAttachedToProcess = isArcConnectedToGlyph(arc, glyph);

							if (bArcAttachedToProcess) {
								float point_x = glyph.getBbox().getX();
								float point_y = glyph.getBbox().getY();

								if (arc.getNext().size() > 0) {
									Next bendPoint = getClosestBendPoint(glyph, arc);
									point_x = bendPoint.getX();
									point_y = bendPoint.getY();
								} else {
									if (arc.getTarget() instanceof Port) {
										point_x = arc.getStart().getX();
										point_y = arc.getStart().getY();

									} else if (arc.getSource() instanceof Port) {
										point_x = arc.getEnd().getX();
										point_y = arc.getEnd().getY();
									}

								}
								float y_shape = (float) (glyph.getBbox().getY() - glyph.getBbox().getH() * 0.5);
								float x_shape = (float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);

								if (Math.abs(point_y - y_shape) < Math.abs(point_x - x_shape)) {
									horizontal++;
								} else {
									vertical++;
								}
							}

						}
					}
					rearrangePorts(glyph, horizontal, vertical);
				}
			} else if (isOperatorType(glyph)) {
				for (Arc arc : map.getArc()) {
					if (arc.getClazz().equals(FileUtils.SBGN_LOGIC_ARC)
							|| arc.getClazz().equals(FileUtils.SBGN_CATALYSIS)
							|| arc.getClazz().equals(FileUtils.SBGN_STIMULATION)
							|| arc.getClazz().equals(FileUtils.SBGN_INHIBITION)
							|| arc.getClazz().equals(FileUtils.SBGN_MODULATION)
							|| arc.getClazz().equals(FileUtils.SBGN_NECESSARY_STIMULATION)) {
						boolean bArcAttachedToProcess = isArcConnectedToGlyph(arc, glyph);

						if (bArcAttachedToProcess) {
							float point_x = glyph.getBbox().getX();
							float point_y = glyph.getBbox().getY();

							if (arc.getNext().size() > 0) {
								Next bendPoint = getClosestBendPoint(glyph, arc);
								point_x = bendPoint.getX();
								point_y = bendPoint.getY();
							} else {
								if (arc.getTarget() instanceof Port) {
									point_x = arc.getStart().getX();
									point_y = arc.getStart().getY();
								} else if (arc.getSource() instanceof Port) {
									point_x = arc.getEnd().getX();
									point_y = arc.getEnd().getY();
								}
							}

							float y_shape = (float) (glyph.getBbox().getY() - glyph.getBbox().getH() * 0.5);
							float x_shape = (float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);

							if (Math.abs(point_y - y_shape) < Math.abs(point_x - x_shape)) {
								horizontal++;
							} else {
								vertical++;
							}
						}

					}
				}

				rearrangePorts(glyph, horizontal, vertical);
			}
		}
	}

	private void rearrangePorts(Glyph glyph, int horizontal, int vertical) {
		if (horizontal >= vertical) {
			glyph.getPort().get(0).setX(glyph.getBbox().getX() - (float) PORT2GLYPH_DISTANCE);
			glyph.getPort().get(1).setX(glyph.getBbox().getX() + glyph.getBbox().getW() + (float) PORT2GLYPH_DISTANCE);

			glyph.getPort().get(0).setY(glyph.getBbox().getY());
			glyph.getPort().get(1).setY(glyph.getBbox().getY());
		} else {
			glyph.getPort().get(0).setX((float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5));
			glyph.getPort().get(1).setX((float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5));

			glyph.getPort().get(0).setY(glyph.getBbox().getY() + (float) PORT2GLYPH_DISTANCE);
			glyph.getPort().get(1).setY(glyph.getBbox().getY() + glyph.getBbox().getH() - (float) PORT2GLYPH_DISTANCE);
		}
	}

	private Next getClosestBendPoint(Glyph glyph, Arc arc) {
		Next closestBendPoint = arc.getNext().get(0);
		float min_dist = getPointDistance(closestBendPoint.getX(), closestBendPoint.getY(), glyph.getBbox().getX(),
				glyph.getBbox().getY());

		for (Next next : arc.getNext()) {
			float dist = getPointDistance(next.getX(), next.getY(), glyph.getBbox().getX(), glyph.getBbox().getY());
			if (dist < min_dist) {
				min_dist = dist;
				closestBendPoint = next;
			}
		}

		return closestBendPoint;
	}

	private boolean isArcConnectedToGlyph(Arc arc, Glyph glyph) {
		boolean bArcAttachedToProcess = false;
		Port port = null;

		if (arc.getSource() instanceof Port) {
			port = (Port) arc.getSource();
		} else if (arc.getTarget() instanceof Port) {
			port = (Port) arc.getTarget();
		}

		if (null != port) {
			if (port.equals(glyph.getPort().get(0)) || (port.equals(glyph.getPort().get(1)))) {
				bArcAttachedToProcess = true;
			}
		}
		return bArcAttachedToProcess;
	}

	private boolean isReversibleProcess(Glyph process) {
		boolean bReversible = true;

		for (Arc arc : map.getArc()) {
			if (isArcConnectedToGlyph(arc, process)) {
				// there is at least one arc connected to the current process glyph that is not
				// a production arc; thus, the process is not reversible
				if (!arc.getClazz().equals(FileUtils.SBGN_PRODUCTION)) {
					bReversible = false;
					break;
				}
			}
		}

		return bReversible;
	}
}
