package pd;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arc.Next;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.Sbgn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.eisbm.GRAPHML2SBGNML.ConverterDefines;
import fr.eisbm.GRAPHML2SBGNML.ModelAttributes;
import fr.eisbm.GRAPHML2SBGNML.Utils;
import fr.eisbm.GraphMLHandlers.ArcHandler;
import fr.eisbm.GraphMLHandlers.CloneHandler;
import fr.eisbm.GraphMLHandlers.GlyphHandler;
import fr.eisbm.GraphMLHandlers.SBGNMLStyle;
import fr.eisbm.GraphMLHandlers.StyleHandler;


public class GraphML2PD {

	Sbgn sbgn = new Sbgn();
	Map map = new Map();
	java.util.Map<String, String> colorMap = new HashMap<String, String>();

	public static void main(String[] args) {
		convert(Utils.IN_YED_FILE);
		System.out.println("simulation finished");
	}

	public static void convert(String szInputFileName) {
		GraphML2PD pdConverter = new GraphML2PD();
		String szOutSBGNFile = szInputFileName.replace(".graphml", "").concat(".sbgn");
		boolean bConversion = pdConverter.parseGraphMLFile(szInputFileName, szOutSBGNFile);
		System.out.println(szInputFileName + "\t " + bConversion);
	}
	
	public boolean parseGraphMLFile(String szInGraphMLFileName, String szOutSBGNFile) {
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

			GlyphHandler glyphHandler = new GlyphHandler();
			ArcHandler arcHandler = new ArcHandler();
			StyleHandler styleHandler = new StyleHandler();

			// read information on the ids of tags for of annotation, color etc
			ModelAttributes modelAttr = new ModelAttributes();
			NodeList nKeyList = doc.getElementsByTagName(ConverterDefines.KEY_TAG);
			modelAttr.populateModelAttributes(nKeyList);

			// handle complexes and compartments first
			// complexes and compartments are mapped by yEd groups
			NodeList nCompartComplexList = doc.getElementsByTagName(ConverterDefines.NODE_TAG);

			for (int temp = 0; temp < nCompartComplexList.getLength(); temp++) {
				Node nNode = nCompartComplexList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					// compartment - mapped by yEd groups with the <y:GroupNode> tag
					NodeList _nlGroupList = eElement.getElementsByTagName(ConverterDefines.Y_GROUP_NODE);
					if (_nlGroupList.getLength() > 0)
						glyphHandler.parseCompartments(eElement, map, styleHandler);
					else {
						// checking the node type
						NodeList _nlConfigList = eElement.getElementsByTagName(ConverterDefines.Y_GENERIC_GROUP_NODE);
						glyphHandler.parseComplexes(doc, modelAttr, eElement, _nlConfigList, map, styleHandler);
					}
				}
			}

			// nodes:
			NodeList nList = doc.getElementsByTagName(ConverterDefines.NODE_TAG);
			glyphHandler.parseNodes(doc, modelAttr, nList, map, styleHandler);

			// for the process glyphs, ports will be created by default
			glyphHandler.createPorts(map.getGlyph());

			// edges/arcs:
			NodeList nEdgeList = doc.getElementsByTagName(ConverterDefines.EDGE_TAG);
			arcHandler.processArcs(nEdgeList, map, styleHandler);
			arcHandler.setArcsToPorts(map);

			// resources:
			NodeList nResourceList = doc.getElementsByTagName(ConverterDefines.Y_RESOURCE);
			glyphHandler.processResources(nResourceList, map);

			// add information on doc extension such as annotation, color etc
			addExtension(doc, styleHandler);

			// move the network elements to start from top/left, i.e. coordinates (0,0)
			moveNetworkToTopLeft();

			// correct the orientation of the processes and connected arcs
			arcHandler.correctPortOrientationAndConnectedArcs(map.getGlyph(), map.getArc());

			// handle clone marker
			CloneHandler cloneHandler = new CloneHandler();
			cloneHandler.setClonedGlyphs(map);

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

	private void addExtension(Document doc, StyleHandler sh) {
		// add extension data
		Extension ext = new Extension();
		// add render information tag
		Element eltRenderInfo = doc.createElementNS("http://www.sbml.org/sbml/level3/version1/render/version1",
				"renderInformation");
		eltRenderInfo.setAttribute(ConverterDefines.ID_ATTR, "renderInformation");
		eltRenderInfo.setAttribute("backgroundColor", "#ffffff");
		eltRenderInfo.setAttribute("programName", "graphml2sbgn");
		eltRenderInfo.setAttribute("programVersion", "0.1");

		// add list of colors
		Element eltListOfColor = doc.createElement(ConverterDefines.LIST_OF_COLOR_DEFINITIONS_TAG);
		eltRenderInfo.appendChild(eltListOfColor);

		int i = 0;
		for (String _color : sh.colorSet) {
			i++;
			colorMap.put(_color, ConverterDefines.COLOR_PREFIX + i);
		}

		for (Entry<String, String> e : colorMap.entrySet()) {
			Element eltColorId = doc.createElement(ConverterDefines.COLOR_DEFINITION_TAG);
			eltColorId.setAttribute(ConverterDefines.ID_ATTR, e.getValue());
			eltColorId.setAttribute(ConverterDefines.VALUE_ATTR, e.getKey());
			eltListOfColor.appendChild(eltColorId);
		}

		// add list of styles
		Element eltListOfStyles = doc.createElement(ConverterDefines.LIST_OF_STYLES_TAG);
		eltRenderInfo.appendChild(eltListOfStyles);
		for (Entry<String, SBGNMLStyle> e : sh.styleMap.entrySet()) {
			Element eltStyleId = doc.createElement(ConverterDefines.STYLE_TAG);
			eltStyleId.setAttribute(ConverterDefines.ID_ATTR, e.getKey());
			eltStyleId.setAttribute(ConverterDefines.ID_LIST_ATTR, e.getValue().getElementSet());

			// add graphics of the style
			Element graphics = doc.createElement(ConverterDefines.GRAPHICS_TAG);
			graphics.setAttribute(ConverterDefines.FILL_ATTR, colorMap.get(e.getValue().getFillColor()));
			graphics.setAttribute(ConverterDefines.FONTSIZE_ATTR, Float.toString(e.getValue().getFontSize()));
			graphics.setAttribute(ConverterDefines.STROKE_ATTR, colorMap.get(e.getValue().getStrokeColor()));
			graphics.setAttribute(ConverterDefines.STROKE_WIDTH_ATTR, Float.toString(e.getValue().getStrokeWidth()));
			eltStyleId.appendChild(graphics);

			eltListOfStyles.appendChild(eltStyleId);
		}

		ext.getAny().add(eltRenderInfo);

		boolean bDefaultColorStyleConfig = false;
		// The default configuration consists in the existence of two styles (one for
		// glyphs, another for arcs) and two colors (black and white for stroke and fill
		// colors respectively).
		// Information will be added only if the style/color configuration is not the
		// default one; otherwise, it will generate a difference between the before and
		// after conversion files.
		if ((sh.styleMap.size() == 2) && (sh.colorSet.size() == 2)) {
			if ((sh.colorSet.contains("#FFFFFF") || (sh.colorSet.contains("#ffffff")))
					&& (sh.colorSet.contains("#000000"))) {
				bDefaultColorStyleConfig = true;
			}
		}

		if (!bDefaultColorStyleConfig) {
			map.setExtension(ext);
		}
	}

	// moving elements section:
	private void moveNetworkToTopLeft() {
		float x_min = Float.MAX_VALUE;
		float y_min = Float.MAX_VALUE;

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
}
