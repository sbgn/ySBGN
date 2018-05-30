package fr.eisbm.GRAPHML2SBGNML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.xml.sax.SAXException;

public class transformToSBGN02 {
	private static java.util.Map visited = new HashMap<String, Boolean>();

	public static void transformToSBGNv02(String szInSBGNFileName, String szOutSBGNv02FileName) {
		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn;
		try {
			sbgn = FileUtils.readFromFile(szInSBGNFileName);

			// map is a container for the glyphs and arcs
			Map map = (org.sbgn.bindings.Map) sbgn.getMap();

			Map map1 = new org.sbgn.bindings.Map();
			map1.setLanguage(map.getLanguage());

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

					if (a.getGlyph().get(i).getClazz().equals(FileUtils.SBGN_CARDINALITY)
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
			sbgn1.setMap(map1);
			SbgnUtil.writeToFile(sbgn1, outputFile);
			System.out.println(
					"SBGN file validation: " + (SbgnUtil.isValid(outputFile) ? "validates" : "does not validate"));
		} catch (JAXBException | SAXException | IOException e2) {
			e2.printStackTrace();
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
