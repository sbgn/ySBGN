package fr.eisbm.GraphMLHandlers;

import java.util.Map.Entry;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Next;
import org.sbgn.bindings.Arc.Start;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.eisbm.GRAPHML2SBGNML.ConverterDefines;
import fr.eisbm.GRAPHML2SBGNML.FileUtils;
import fr.eisbm.GraphMLHandlers.PortArcsRelationship.PortType;

public class ArcHandler {

	public static void processArcs(NodeList nEdgeList, Map map, java.util.Map<String, PortArcsRelationship> pam) {
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

				// get id of the edge/arc
				String szArcAttributes = getElementAttributes(eElement).trim();
				String szArrowDirection = processNodeList(eElement.getElementsByTagName(ConverterDefines.Y_ARROWS));
				setArcSourceTarget(arc, szArcAttributes, szArrowDirection, map);

				// given the fact that the consumption line could be used also for the
				// representation of the logic or equivalence arcs, this has to be checked and
				// decoded accordingly
				correctArcClazz(arc);

				// set coordinates for Start/End fields of the arc
				String szPathCoordinates = processNodeList(eElement.getElementsByTagName(ConverterDefines.Y_PATH));
				setArcStartEnd(arc, szPathCoordinates);

				// for processes, ports must be set for consumption and production arcs
				// for logic operators, ports must be set for incoming arcs and for outgoing
				// regulatory arcs (catalysis, stimulation, inhibition, modulation, necessary
				// stimulation)
				setPortToArc(arc, map, pam);

				// set bedn points of the arc
				String szPointInfo = processNodeList(eElement.getElementsByTagName(ConverterDefines.Y_POINT));
				setBendPoints(arc, szPointInfo, fPort2PointDistance);

				// set arc style (color, line width etc)
				NodeList nlLineStyle = eElement.getElementsByTagName(ConverterDefines.Y_LINE_STYLE);
				setArcStyle(eElement, nlLineStyle);

				// set cardinality of the arc
				NodeList nlCardinalityList = eElement.getElementsByTagName(ConverterDefines.Y_EDGE_LABEL);
				setArcCardinality(arc, nlCardinalityList);
			}

			// add the arc to the map
			map.getArc().add(arc);
		}
	}

	private static void setPortToArc(Arc arc, Map map, java.util.Map<String, PortArcsRelationship> pam) {
		// if the port has not been set yet, i.e. the arc source/ target is an instance
		// of the Glyph class and not of the Port class yet
		Port currentPort = null;
		Port alternativePort = null;
		Glyph glyph = null;

		if (arc.getClazz().equals(ConverterDefines.SBGN_PRODUCTION)) {
			if (arc.getSource() instanceof Glyph) {
				glyph = (Glyph) arc.getSource();

				if (!isReversibleProcess(glyph, map)) {
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

					float dist1 = FileUtils.getPointDistance(point_x, point_y, port1.getX(), port1.getY());
					float dist2 = FileUtils.getPointDistance(point_x, point_y, port2.getX(), port2.getY());

					if (dist1 <= dist2) {
						currentPort = port1;
						alternativePort = port2;
					} else {
						currentPort = port2;
						alternativePort = port1;
					}
				}
				// else: reversible processes
			}
		} else if (arc.getClazz().equals(ConverterDefines.SBGN_CONSUMPTION)) {
			if (arc.getTarget() instanceof Glyph) {

				glyph = (Glyph) arc.getTarget();

				if (glyph.getPort() != null) {
					if (glyph.getPort().size() == FileUtils.MAX_PORT_NO) {
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

						float dist1 = FileUtils.getPointDistance(point_x, point_y, port1.getX(), port1.getY());
						float dist2 = FileUtils.getPointDistance(point_x, point_y, port2.getX(), port2.getY());

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

		else if (arc.getClazz().equals(ConverterDefines.SBGN_LOGIC_ARC)) {
			if (arc.getTarget() instanceof Glyph) {
				glyph = (Glyph) arc.getTarget();
				if (ArcHandler.isOperatorType(glyph)) {
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

					float dist1 = FileUtils.getPointDistance(point_x, point_y, port1.getX(), port1.getY());
					float dist2 = FileUtils.getPointDistance(point_x, point_y, port2.getX(), port2.getY());

					if (dist1 <= dist2) {
						currentPort = port1;
						alternativePort = port2;
					} else {
						currentPort = port2;
						alternativePort = port1;
					}
				}
			}
		} else if (arc.getClazz().equals(ConverterDefines.SBGN_CATALYSIS)
				|| arc.getClazz().equals(ConverterDefines.SBGN_STIMULATION)
				|| arc.getClazz().equals(ConverterDefines.SBGN_INHIBITION)
				|| arc.getClazz().equals(ConverterDefines.SBGN_MODULATION)
				|| arc.getClazz().equals(ConverterDefines.SBGN_NECESSARY_STIMULATION)) {
			if (arc.getSource() instanceof Glyph) {
				glyph = (Glyph) arc.getSource();
				if (ArcHandler.isOperatorType(glyph)) {
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

					float dist1 = FileUtils.getPointDistance(point_x, point_y, port1.getX(), port1.getY());
					float dist2 = FileUtils.getPointDistance(point_x, point_y, port2.getX(), port2.getY());

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
			if (!pam.containsKey(currentPort.getId())) {
				pam.put(currentPort.getId(), new PortArcsRelationship(currentPort));
				pam.put(alternativePort.getId(), new PortArcsRelationship(alternativePort));
				pam.get(currentPort.getId()).addArcToSet(arc);

				if (arc.getClazz().equals(ConverterDefines.SBGN_PRODUCTION)) {
					if (!isReversibleProcess(glyph, map)) {
						pam.get(currentPort.getId()).setType(PortType.SourcePort);
						pam.get(alternativePort.getId()).setType(PortType.TargetPort);
					} else {
						pam.get(currentPort.getId()).setType(PortType.SourcePort);
						pam.get(alternativePort.getId()).setType(PortType.SourcePort);
					}
				} else if (arc.getClazz().equals(ConverterDefines.SBGN_CONSUMPTION)) {
					pam.get(currentPort.getId()).setType(PortType.TargetPort);
					pam.get(alternativePort.getId()).setType(PortType.SourcePort);
				} else if (arc.getClazz().equals(ConverterDefines.SBGN_LOGIC_ARC)) {
					pam.get(currentPort.getId()).setType(PortType.TargetPort);
					pam.get(alternativePort.getId()).setType(PortType.SourcePort);
				} else if (arc.getClazz().equals(ConverterDefines.SBGN_CATALYSIS)
						|| arc.getClazz().equals(ConverterDefines.SBGN_STIMULATION)
						|| arc.getClazz().equals(ConverterDefines.SBGN_INHIBITION)
						|| arc.getClazz().equals(ConverterDefines.SBGN_MODULATION)
						|| arc.getClazz().equals(ConverterDefines.SBGN_NECESSARY_STIMULATION)) {
					pam.get(currentPort.getId()).setType(PortType.SourcePort);
					pam.get(alternativePort.getId()).setType(PortType.TargetPort);
				}
			}
			// the port exists already in the map, and it has already some arcs assigned to
			// it. It must check if the existent arcs have the same clazz; if there are arcs
			// of different clazz, they must be assigned to the alternative port
			else {

				if (arc.getClazz().equals(ConverterDefines.SBGN_PRODUCTION)) {
					if (pam.get(currentPort.getId()).getPortType() == PortType.SourcePort) {
						pam.get(currentPort.getId()).addArcToSet(arc);
					} else {
						pam.get(alternativePort.getId()).addArcToSet(arc);
					}
				} else if (arc.getClazz().equals(ConverterDefines.SBGN_CONSUMPTION)) {
					if (pam.get(currentPort.getId()).getPortType() == PortType.TargetPort) {
						pam.get(currentPort.getId()).addArcToSet(arc);
					} else {
						pam.get(alternativePort.getId()).addArcToSet(arc);
					}
				} else if (arc.getClazz().equals(ConverterDefines.SBGN_LOGIC_ARC)) {

					if (pam.get(currentPort.getId()).getPortType() == PortType.TargetPort) {
						pam.get(currentPort.getId()).addArcToSet(arc);
					} else {
						pam.get(alternativePort.getId()).addArcToSet(arc);
					}
				} else if (arc.getClazz().equals(ConverterDefines.SBGN_CATALYSIS)
						|| arc.getClazz().equals(ConverterDefines.SBGN_STIMULATION)
						|| arc.getClazz().equals(ConverterDefines.SBGN_INHIBITION)
						|| arc.getClazz().equals(ConverterDefines.SBGN_MODULATION)
						|| arc.getClazz().equals(ConverterDefines.SBGN_NECESSARY_STIMULATION)) {

					if (pam.get(currentPort.getId()).getPortType() == PortType.SourcePort) {
						pam.get(currentPort.getId()).addArcToSet(arc);
					} else {
						pam.get(alternativePort.getId()).addArcToSet(arc);
					}
				}
			}
		}
	}

	private static boolean isReversibleProcess(Glyph process, Map map) {
		boolean bReversible = true;

		for (Arc arc : map.getArc()) {
			if (isArcConnectedToGlyph(arc, process)) {
				// there is at least one arc connected to the current process glyph that is not
				// a production arc; thus, the process is not reversible
				if (!arc.getClazz().equals(ConverterDefines.SBGN_PRODUCTION)) {
					bReversible = false;
					break;
				}
			}
		}

		return bReversible;
	}

	private static boolean isArcConnectedToGlyph(Arc arc, Glyph glyph) {

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

	private static void correctArcClazz(Arc arc) {
		if (arc.getClazz().equals(ConverterDefines.SBGN_CONSUMPTION)) {
			if ((arc.getSource() != null) && (arc.getTarget() != null)) {

				if (((Glyph) arc.getSource()).getClazz().toUpperCase().equals("OR")
						|| ((Glyph) arc.getTarget()).getClazz().toUpperCase().equals("OR")
						|| (((Glyph) arc.getSource()).getClazz().toUpperCase().equals("AND")
								|| ((Glyph) arc.getTarget()).getClazz().toUpperCase().equals("AND"))
						|| (((Glyph) arc.getSource()).getClazz().toUpperCase().equals("NOT")
								|| ((Glyph) arc.getTarget()).getClazz().toUpperCase().equals("NOT"))) {
					arc.setClazz(ConverterDefines.SBGN_LOGIC_ARC);
				} else if ((((Glyph) arc.getSource()).getClazz().equals(ConverterDefines.SBGN_SUBMAP))
						|| ((Glyph) arc.getSource()).getClazz().equals(ConverterDefines.SBGN_TAG)
						|| ((Glyph) arc.getTarget()).getClazz().equals(ConverterDefines.SBGN_SUBMAP)
						|| ((Glyph) arc.getTarget()).getClazz().equals(ConverterDefines.SBGN_TAG))
					arc.setClazz(ConverterDefines.SBGN_EQUIVALENCE_ARC);
			}
		}
	}

	private static void setArcStartEnd(Arc arc, String szPathCoordinates) {
		float fStartX = 0, fStartY = 0, fStartH = 0, fStartW = 0, fEndX = 0, fEndY = 0, fEndH = 0, fEndW = 0;
		Glyph source = (Glyph) arc.getSource();
		Glyph target = (Glyph) arc.getTarget();

		if (null != source.getBbox()) {
			fStartX = source.getBbox().getX();
			fStartY = source.getBbox().getY();
			fStartH = source.getBbox().getH();
			fStartW = source.getBbox().getW();
		}

		if (null != target.getBbox())

		{
			fEndX = target.getBbox().getX();
			fEndY = target.getBbox().getY();
			fEndH = target.getBbox().getH();
			fEndW = target.getBbox().getW();
		}

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
			_end.setX(Float.parseFloat(tx) + fEndX + fEndW / 2);
			// _end.setX(Float.parseFloat(szTX));
			String ty = tokensCoordinates[3].replaceAll("ty=", "");
			_end.setY(Float.parseFloat(ty) + fEndY + fEndH / 2);
			// _end.setY(Float.parseFloat(szTY));
			arc.setEnd(_end);
		}
	}

	public static boolean isProcessType(Glyph source) {
		boolean bIsProcess = false;
		if (source.getClazz().equals(ConverterDefines.SBGN_PROCESS)
				|| (source.getClazz().equals(ConverterDefines.SBGN_UNCERTAIN_PROCESS))
				|| (source.getClazz().equals(ConverterDefines.SBGN_OMITTED_PROCESS))) {
			bIsProcess = true;
		}
		return bIsProcess;
	}

	public static boolean isOperatorType(Glyph source) {
		boolean bIsOperator = false;
		if (source.getClazz().equals(ConverterDefines.SBGN_AND) || (source.getClazz().equals(ConverterDefines.SBGN_OR))
				|| (source.getClazz().equals(ConverterDefines.SBGN_NOT))) {
			bIsOperator = true;
		}
		return bIsOperator;
	}

	public static void setArcStyle(Element eElement, NodeList nlLineStyle) {
		// getting the border color info
		String szStrokeColorId = ((Element) (nlLineStyle.item(0))).getAttribute(ConverterDefines.COLOR_ATTR);
		StyleHandler.colorSet.add(szStrokeColorId);

		// getting the stroke width info
		float fStrokeWidth = Float
				.parseFloat(((Element) (nlLineStyle.item(0))).getAttribute(ConverterDefines.WIDTH_ATTR));

		String szStyleId = ConverterDefines.STYLE_PREFIX + fStrokeWidth + szStrokeColorId.replaceFirst("#", "");

		if (!StyleHandler.styleMap.containsKey(szStyleId)) {
			StyleHandler.styleMap.put(szStyleId, new SBGNMLStyle(szStyleId, szStrokeColorId, fStrokeWidth));
		}
		StyleHandler.styleMap.get(szStyleId).addElementIdToSet(eElement.getAttribute(ConverterDefines.ID_ATTR));
	}

	public static void setArcCardinality(Arc arc, NodeList nlCardinalityList) {
		if (nlCardinalityList.getLength() > 0) {
			String szCardinality = nlCardinalityList.item(0).getTextContent().trim();
			if (!szCardinality.equals("")) {

				Glyph cardGlyph = new Glyph();
				cardGlyph.setClazz(ConverterDefines.SBGN_CARDINALITY);
				cardGlyph.setId(ConverterDefines.SBGN_CARDINALITY + "_" + szCardinality);
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

	public static void setBendPoints(Arc arc, String szPointInfo, float fPort2PointDistance) {

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
					if (FileUtils.getPointDistance(p.getX(), p.getY(), fXCoord, fYCoord) <= fPort2PointDistance) {
						bFoundPort = true;
					}
				} else if (arc.getTarget() instanceof Port) {
					Port p = (Port) arc.getTarget();
					if (FileUtils.getPointDistance(p.getX(), p.getY(), fXCoord, fYCoord) <= fPort2PointDistance) {
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

	public static boolean setArcClazz(Arc arc, String szArrowDirection) {
		String szArcType = ConverterDefines.SBGN_CONSUMPTION;
		boolean bEdgeToBeCorrected = false;

		if (szArrowDirection.contains("white_delta_bar")) {
			szArcType = ConverterDefines.SBGN_NECESSARY_STIMULATION;
			if (szArrowDirection.contains("source=\"white_delta_bar\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("white_diamond")) {
			szArcType = ConverterDefines.SBGN_MODULATION;
			if (szArrowDirection.contains("source=\"white_diamond\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("t_shape")) {
			szArcType = ConverterDefines.SBGN_INHIBITION;
			if (szArrowDirection.contains("source=\"t_shape\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("white_delta")) {
			szArcType = ConverterDefines.SBGN_STIMULATION;
			if (szArrowDirection.contains("source=\"white_circle\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("delta")) {
			szArcType = ConverterDefines.SBGN_PRODUCTION;
			if (szArrowDirection.contains("source=\"delta\"")) {
				bEdgeToBeCorrected = true;
			}
		} else if (szArrowDirection.contains("white_circle")) {
			szArcType = ConverterDefines.SBGN_CATALYSIS;
			if (szArrowDirection.contains("source=\"white_circle\"")) {
				bEdgeToBeCorrected = true;
			}
		}

		arc.setClazz(szArcType);
		return bEdgeToBeCorrected;
	}

	public static void setArcSourceTarget(Arc arc, String szArcAttributes, String szArrowDirection, Map map) {
		boolean bEdgeToBeCorrected = setArcClazz(arc, szArrowDirection);

		String delims = "[\t]";
		String szArcId = "", szArcSource = "", szArcTarget = "";
		szArcAttributes = szArcAttributes.replaceAll("\"", "");
		String[] tokens = szArcAttributes.split(delims);

		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains("id=")) {
				szArcId = tokens[i].replaceAll("id=", "");
				arc.setId(szArcId);
			} else if (tokens[i].contains("source=")) {
				szArcSource = tokens[i].replaceAll("source=", "");

				Glyph g = null;
				for (Glyph _glyph : map.getGlyph()) {
					g = FileUtils.findGlyph(szArcSource, _glyph);
					if (null != g) {
						arc.setSource(g);
						break;
					}
				}
			} else if (tokens[i].contains("target=")) {
				szArcTarget = tokens[i].replaceAll("target=", "");

				Glyph g = null;
				for (Glyph _glyph : map.getGlyph()) {
					g = FileUtils.findGlyph(szArcTarget, _glyph);
					if (null != g) {
						arc.setTarget(g);
						break;
					}
				}
			}
		}

		Glyph source = (Glyph) arc.getSource();
		Glyph target = (Glyph) arc.getTarget();

		if (bEdgeToBeCorrected) {
			arc.setSource(target);
			arc.setTarget(source);
		}

		// if the process is a consumption, it is easy to draw from process to entity
		// and this can not be detected before
		if (arc.getClazz().equals(ConverterDefines.SBGN_CONSUMPTION)) {

			source = (Glyph) arc.getSource();
			target = (Glyph) arc.getTarget();

			if ((isProcessType(source)) && (!isProcessType(target))) {
				arc.setSource(target);
				arc.setTarget(source);
			}
		}
	}

	public static void correctPortOrientationAndConnectedArcs(Map map) {

		for (Glyph glyph : map.getGlyph()) {
			int horizontal = 0;
			int vertical = 0;

			if (ArcHandler.isProcessType(glyph)) {
				if (!isReversibleProcess(glyph, map)) {
					for (Arc arc : map.getArc()) {
						if (arc.getClazz().equals(ConverterDefines.SBGN_CONSUMPTION)
								|| arc.getClazz().equals(ConverterDefines.SBGN_PRODUCTION)) {
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
			} else if (ArcHandler.isOperatorType(glyph)) {
				for (Arc arc : map.getArc()) {
					if (arc.getClazz().equals(ConverterDefines.SBGN_LOGIC_ARC)
							|| arc.getClazz().equals(ConverterDefines.SBGN_CATALYSIS)
							|| arc.getClazz().equals(ConverterDefines.SBGN_STIMULATION)
							|| arc.getClazz().equals(ConverterDefines.SBGN_INHIBITION)
							|| arc.getClazz().equals(ConverterDefines.SBGN_MODULATION)
							|| arc.getClazz().equals(ConverterDefines.SBGN_NECESSARY_STIMULATION)) {
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

	public static void rearrangePorts(Glyph glyph, int horizontal, int vertical) {
		if (horizontal >= vertical) {
			glyph.getPort().get(0).setX(glyph.getBbox().getX() - (float) FileUtils.PORT2GLYPH_DISTANCE);
			glyph.getPort().get(1)
					.setX(glyph.getBbox().getX() + glyph.getBbox().getW() + (float) FileUtils.PORT2GLYPH_DISTANCE);

			glyph.getPort().get(0).setY(glyph.getBbox().getY());
			glyph.getPort().get(1).setY(glyph.getBbox().getY());
		} else {
			glyph.getPort().get(0).setX((float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5));
			glyph.getPort().get(1).setX((float) (glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5));

			glyph.getPort().get(0).setY(glyph.getBbox().getY() + (float) FileUtils.PORT2GLYPH_DISTANCE);
			glyph.getPort().get(1)
					.setY(glyph.getBbox().getY() + glyph.getBbox().getH() - (float) FileUtils.PORT2GLYPH_DISTANCE);
		}
	}

	public static Next getClosestBendPoint(Glyph glyph, Arc arc) {
		Next closestBendPoint = arc.getNext().get(0);
		float min_dist = FileUtils.getPointDistance(closestBendPoint.getX(), closestBendPoint.getY(),
				glyph.getBbox().getX(), glyph.getBbox().getY());

		for (Next next : arc.getNext()) {
			float dist = FileUtils.getPointDistance(next.getX(), next.getY(), glyph.getBbox().getX(),
					glyph.getBbox().getY());
			if (dist < min_dist) {
				min_dist = dist;
				closestBendPoint = next;
			}
		}

		return closestBendPoint;
	}

	public static String processNodeList(NodeList nodeList) {
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

	public static String getElementAttributes(Element eElement) {
		String szAttributeValues = "";
		for (int i = 0; i < eElement.getAttributes().getLength(); i++) {
			szAttributeValues = szAttributeValues.concat(eElement.getAttributes().item(i) + "\t");
		}
		return szAttributeValues;
	}

	public static void assignArcsToPorts(java.util.Map<String, PortArcsRelationship> pam) {

		for (Entry<String, PortArcsRelationship> entry : pam.entrySet()) {
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

}
