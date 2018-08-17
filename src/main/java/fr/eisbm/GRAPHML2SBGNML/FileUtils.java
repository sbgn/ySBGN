package fr.eisbm.GRAPHML2SBGNML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;

public class FileUtils {	
	public static final String IN_SBGN_FILE = "example_files/test.sbgn";
	public static final String IN_YED_FILE = "example_files/test.graphml";
	
	public static final int DEFAULT_FONT_SIZE = 10;
	public static final int MAX_PORT_NO = 2;
	public static final double PORT2GLYPH_DISTANCE = 10.8;
	
	public static Sbgn readFromFile(String szFileName) throws JAXBException {
		Sbgn result = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(szFileName));
			BOMskip(reader);
			String content = reader.lines().collect(Collectors.joining());

			// set given sbgn to always be 0.2 to avoid compatibility problems
			content = content.replaceFirst("http://sbgn\\.org/libsbgn/0\\.3", "http://sbgn.org/libsbgn/0.2");

			JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
			Unmarshaller unmarshaller = context.createUnmarshaller();
			result = (Sbgn) unmarshaller.unmarshal(new StringReader(content));
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Skip BOM char. BOM is present in output of Newt. See
	 * https://stackoverflow.com/a/18275066 courtesy to Ludovic Roy
	 * 
	 * @param reader
	 * @throws IOException
	 */
	public static void BOMskip(Reader reader) throws IOException {
		reader.mark(1);
		char[] possibleBOM = new char[1];
		reader.read(possibleBOM);

		if (possibleBOM[0] != '\ufeff') {
			reader.reset();
		}
	}
	
	public static Glyph findGlyph(String szId, Glyph g) {
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
	
	public static float getPointDistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

}
