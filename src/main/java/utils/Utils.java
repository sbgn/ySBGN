package utils;

import java.util.List;

import org.sbgn.bindings.Glyph;

public class Utils {
	public static final String IN_SBGN_FILE = "af/F001-DendriticCell.sbgn";
	public static final String IN_YED_FILE = "downloads/F003-mevalonate.graphml";

	public static final int DEFAULT_FONT_SIZE = 10;
	public static final int MAX_PORT_NO = 2;
	public static final int FIRST_PORT = 0;
	public static final int SECOND_PORT = 1;
	
	public static float getPointDistance(float x1, float y1, float x2, float y2) {
		float dist = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return dist;
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
		if (source.getClazz() != null) {
			if (source.getClazz().equals(ConverterDefines.SBGN_AND)
					|| (source.getClazz().equals(ConverterDefines.SBGN_OR))
					|| (source.getClazz().equals(ConverterDefines.SBGN_NOT))) {
				bIsOperator = true;
			}
		}
		return bIsOperator;
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
}
