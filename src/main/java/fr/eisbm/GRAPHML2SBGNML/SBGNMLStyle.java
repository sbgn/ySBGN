package fr.eisbm.GRAPHML2SBGNML;

import java.util.HashSet;
import java.util.Set;

public class SBGNMLStyle {
	String m_szId;
	String m_szFillColor;
	String m_szStrokeColor;
	float m_fStrokeWidth;
	float m_fFontSize;
	Set<String> m_lElementIdSet;

	public SBGNMLStyle(String szId, String szFillColor, String szStrokeColor, float fStrokeWidth, float fFontSize) {
		super();
		this.m_szId = szId;
		this.m_szFillColor = szFillColor;
		this.m_szStrokeColor = szStrokeColor;
		this.m_fFontSize = fFontSize;
		this.m_fStrokeWidth = fStrokeWidth;
		m_lElementIdSet = new HashSet<String>();
	}

	public SBGNMLStyle(String szId, String szStrokeColor, float fStrokeWidth) {
		super();
		this.m_szId = szId;
		this.m_szFillColor = "#000000";
		this.m_szStrokeColor = szStrokeColor;
		this.m_fFontSize = FileUtils.DEFAULT_FONT_SIZE;
		this.m_fStrokeWidth = fStrokeWidth;
		m_lElementIdSet = new HashSet<String>();
	}

	public String getFillColor() {
		return m_szFillColor;
	}

	public String getStrokeColor() {
		return m_szStrokeColor;
	}

	public float getStrokeWidth() {
		return m_fStrokeWidth;
	}

	public float getFontSize() {
		return m_fFontSize;
	}

	public void setFillColor(String szFillColor) {
		this.m_szFillColor = szFillColor;
	}

	public void setStrokeColor(String szStrokeColor) {
		this.m_szStrokeColor = szStrokeColor;
	}

	public void setStrokeWidth(float fStrokeWidth) {
		this.m_fStrokeWidth = fStrokeWidth;
	}

	public void setFontSize(float fFontSize) {
		this.m_fFontSize = fFontSize;
	}

	public String getId() {
		return m_szId;
	}

	public void setId(String szId) {
		this.m_szId = szId;
	}

	public void addElementIdToSet(String szElementId) {
		m_lElementIdSet.add(szElementId);
	}

	public String getElementSet() {
		String szGlyphSetStr = "";

		for (String item : m_lElementIdSet) {
			szGlyphSetStr = szGlyphSetStr.concat(item + " ");
		}
		// remove the last white space
		szGlyphSetStr = szGlyphSetStr.substring(0, szGlyphSetStr.length() - 1);
		return szGlyphSetStr;
	}
}
