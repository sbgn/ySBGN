package fr.eisbm.GraphMLHandlers;

import java.util.List;

import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;

import utils.ConverterDefines;
import utils.Utils;

import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;

public class CloneHandler {

	public final static String CloneIsSet = "CloneIsSet";

	public void setClonedGlyphs(Map map) {
		for (Glyph g : map.getGlyph()) {
			if (!(Utils.isProcessType(g) || (Utils.isOperatorType(g))
					|| (g.getClazz().equals(ConverterDefines.SBGN_SOURCE_AND_SINK)))) {
				if (!(g.getClazz().equals(ConverterDefines.SBGN_COMPLEX)
						|| g.getClazz().equals(ConverterDefines.SBGN_COMPLEX_MULTIMER))) {
					setCloneSimpleGlyphs(g, map);
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
						// further checking is necessarily as the complexes are not clones
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

	private void setCloneSimpleGlyphs(Glyph g1, Map map) {

		for (Glyph g2 : map.getGlyph()) {
			boolean bClone = findClonedSimilarGlyphs(g1, g2);
			if (bClone) {
				setClone(g1, g2);
			}
		}
	}

	private void setClone(Glyph g1, Glyph g2) {
		Clone clone = null;

		if (g1.getClone() != null) {
			clone = g1.getClone();

		} else if (g2.getClone() != null) {
			clone = g2.getClone();
		} else if ((g1.getClone() == null) && (g2.getClone() == null)) {
			clone = new Clone();
		}

		if (clone != null) {
			g1.setClone(clone);
			g2.setClone(clone);
		}
	}

	private boolean findClonedSimilarGlyphs(Glyph g1, Glyph g2) {
		boolean bClone = false;

		if (!g1.getId().equals(g2.getId()) && (g1.getClazz().equals(g2.getClazz()))) {

			Label l1 = g1.getLabel();
			Label l2 = g2.getLabel();
			boolean bHaveSameLabel = false;

			if ((l1 != null) && (l2 != null)) {
				if ((l1.getText() != null) && (l2.getText() != null)) {
					if (l1.getText().equals(l2.getText())) {
						bHaveSameLabel = true;
					}
				}
			} else if ((l1 == null) && (l2 == null)) {
				bHaveSameLabel = true;
			}

			if (bHaveSameLabel) {
				bClone = haveSameCompartment(g1, g2);

				if (bClone) {
					bClone = haveSameTextLabel(g1, g2);
				}

				if (bClone) {

					if (hasInfo(g1, ConverterDefines.SBGN_UNIT_OF_INFORMATION)
							&& hasInfo(g2, ConverterDefines.SBGN_UNIT_OF_INFORMATION)) {
						bClone = haveSameAuxiliaryInfo(g1, g2, ConverterDefines.SBGN_UNIT_OF_INFORMATION);
					}

					else if ((hasInfo(g1, ConverterDefines.SBGN_UNIT_OF_INFORMATION)
							&& (!hasInfo(g2, ConverterDefines.SBGN_UNIT_OF_INFORMATION)))
							|| ((!hasInfo(g1, ConverterDefines.SBGN_UNIT_OF_INFORMATION))
									&& hasInfo(g2, ConverterDefines.SBGN_UNIT_OF_INFORMATION))) {
						bClone = false;
					}

				}

				if (bClone) {
					if (hasInfo(g1, ConverterDefines.SBGN_STATE_VARIABLE)
							&& hasInfo(g2, ConverterDefines.SBGN_STATE_VARIABLE)) {
						bClone = haveSameAuxiliaryInfo(g1, g2, ConverterDefines.SBGN_STATE_VARIABLE);
					}

					else if ((hasInfo(g1, ConverterDefines.SBGN_STATE_VARIABLE)
							&& (!hasInfo(g2, ConverterDefines.SBGN_STATE_VARIABLE)))
							|| ((!hasInfo(g1, ConverterDefines.SBGN_STATE_VARIABLE))
									&& hasInfo(g2, ConverterDefines.SBGN_STATE_VARIABLE))) {
						bClone = false;
					}
				}
			}
		}

		return bClone;

	}

	private boolean haveSameAuxiliaryInfo(Glyph g1, Glyph g2, String szType) {
		boolean bFound = true;

		for (Glyph g : g1.getGlyph()) {
			if (g.getClazz().equals(szType)) {
				if (!g2.getGlyph().contains(g)) {
					bFound = false;
				}
			}
		}

		for (Glyph g : g2.getGlyph()) {
			if (g.getClazz().equals(szType)) {
				if (!g1.getGlyph().contains(g)) {
					bFound = false;
				}
			}
		}
		return bFound;
	}

	private boolean hasInfo(Glyph g, String szInfoType) {
		boolean bFound = false;
		for (Glyph gi : g.getGlyph()) {
			if (gi.getClazz().equals(szInfoType)) {
				{
					bFound = true;
					break;
				}
			}
		}
		return bFound;
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
