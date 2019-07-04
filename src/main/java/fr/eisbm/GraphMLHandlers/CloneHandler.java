package fr.eisbm.GraphMLHandlers;

import java.util.List;

import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;

import fr.eisbm.GRAPHML2SBGNML.ConverterDefines;
import fr.eisbm.GRAPHML2SBGNML.Utils;

import org.sbgn.bindings.Map;

public class CloneHandler {

	public final static String CloneIsSet = "CloneIsSet";

	public void setClonedGlyphs(Map map) {
		for (Glyph g : map.getGlyph()) {
			if (!(Utils.isProcessType(g) || (Utils.isOperatorType(g)) || (g.getClazz().equals(ConverterDefines.SBGN_SOURCE_AND_SINK)))) {
				if (!(g.getClazz().equals(ConverterDefines.SBGN_COMPLEX)
						|| g.getClazz().equals(ConverterDefines.SBGN_COMPLEX_MULTIMER))) {
					setCloneSimpleGlyphs(g, map.getGlyph());
				} else {
					findCloneComplexGlyph(g, map.getGlyph());
				}
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
					setClone(g1, g2);
				}
			}
		}
	}

	private void setCloneSimpleGlyphs(Glyph g1, List<Glyph> listOfGlyphs) {

		for (Glyph g2 : listOfGlyphs) {
			boolean bClone = findClonedSimilarGlyphs(g1, g2);
			if (bClone) {
				setClone(g1, g2);
			}
		}
	}

	private void setClone(Glyph g1, Glyph g2) {
		Clone clone = null;
		boolean bClone = false;

		if (g1.getClone() != null) {
			clone = g1.getClone();
			bClone = true;

		} else if (g2.getClone() != null) {
			clone = g2.getClone();
			bClone = true;
		}
		if (bClone) {
			g1.setClone(clone);
			g2.setClone(clone);
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
			if (g.getClazz().equals(ConverterDefines.SBGN_STATE_VARIABLE)) {
				if (!g2.getGlyph().contains(g)) {
					bSameStateVariable = false;
				}
			}
		}

		for (Glyph g : g2.getGlyph()) {
			if (g.getClazz().equals(ConverterDefines.SBGN_STATE_VARIABLE)) {
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
			if (g.getClazz().equals(ConverterDefines.SBGN_UNIT_OF_INFORMATION)) {
				if (!g2.getGlyph().contains(g)) {
					bSameUOI = false;
				}
			}
		}

		for (Glyph g : g2.getGlyph()) {
			if (g.getClazz().equals(ConverterDefines.SBGN_UNIT_OF_INFORMATION)) {
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

}
