package utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.xml.sax.SAXException;

public class SBGNComplianceConversion {
	public static void makeSBGNCompliant (String szInSBGNFileName, String szOutSBGNv02FileName) {
		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn;
		try {
			File f = new File(szInSBGNFileName);

			// Now read from "f" and put the result in "sbgn"
			sbgn = SbgnUtil.readFromFile(f);

			// map is a container for the glyphs and arcs
			Map map = sbgn.getMap().get(0);

			Map map1 = new org.sbgn.bindings.Map();
			map1.setLanguage(map.getLanguage());
			map1.setId(map.getId());

			// we can get a list of glyphs (nodes) in this map with getGlyph()
			for (Glyph g : map.getGlyph()) {
				String newGlyphId = "glyph_" + g.getId();
				newGlyphId = newGlyphId.replaceAll("::", "_");
				g.setId(newGlyphId);

				if (g.getGlyph().size() > 0) {

					correctInternalGlyphsIds(g.getId(), g.getGlyph());
				}

				if (g.getPort().size() > 0) {
					for (Port p : g.getPort()) {
						String newPortId = g.getId() + "_port_" + p.getId();
						newPortId = newPortId.replaceAll("::", "_");
						p.setId(newPortId);
					}
				}

				map1.getGlyph().add(g);
			}

			for (Arc a : map.getArc()) {
				String arcId = "arc_" + a.getId();
				arcId = arcId.replaceAll("::", "_");
				a.setId(arcId);

				for (int i = 0; i < a.getGlyph().size(); i++) {

					if (a.getGlyph().get(i).getClazz().equals(ConverterDefines.SBGN_CARDINALITY)
							&& a.getGlyph().get(i).getLabel().getText().equals("0")) {
						a.getGlyph().remove(i);
					}
				}

				if (a.getGlyph().size() > 0) {
					correctInternalGlyphsIds(a.getId(), a.getGlyph());
				}
				map1.getArc().add(a);
			}

			// write everything to disk
			File outputFile = new File(szOutSBGNv02FileName);
			Sbgn sbgn1 = new Sbgn();

			sbgn1.getMap().add(map1);
			SbgnUtil.writeToFile(sbgn1, outputFile);

			SBGNValidation.validateSBGN(szOutSBGNv02FileName);

		} catch (JAXBException | SAXException | IOException e2) {
			e2.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("simulation finished");
	}

	private static void correctInternalGlyphsIds(String parentGlyphId, List<Glyph> glyphList) {
		for (Glyph _glyph : glyphList) {
			String newVal = parentGlyphId + "_" + _glyph.getId();
			newVal = newVal.replaceAll("::", "_");
			_glyph.setId(newVal);
			if (_glyph.getGlyph().size() > 0) {
				correctInternalGlyphsIds(_glyph.getId(), _glyph.getGlyph());
			}
		}
	}
}
