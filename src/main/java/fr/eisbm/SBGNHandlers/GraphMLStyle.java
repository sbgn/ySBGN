package fr.eisbm.SBGNHandlers;

import utils.Utils;

public class GraphMLStyle {
	static final String DEFAULT_FILL_COLOR = "#FFFFFF";
	static final String DEFAULT_STROKE_COLOR = "#000000";
	static final String DEFAULT_STROKE_WIDTH = "1.0";

	public GraphMLStyle() {
		m_Id ="";
		m_FillColor = DEFAULT_FILL_COLOR;
		m_FontSize = Utils.DEFAULT_FONT_SIZE;
		m_StrokeColor = "#000000";
		m_StrokeWidth = "1.0";
	}

	public GraphMLStyle(String szStyleId, String szFillColor, int szFontSize, String szStrokeColor, String szStrokeWidth) {
		m_Id = szStyleId;
		m_FillColor = szFillColor;
		m_FontSize = szFontSize;
		m_StrokeColor = szStrokeColor;
		m_StrokeWidth = szStrokeWidth;
	}
	
	String m_Id;
	String m_FillColor;
	int m_FontSize;
	String m_StrokeColor;
	String m_StrokeWidth;

	public void setId(String attribute) {
		m_Id = attribute;
	}
	
	public void setFillColor(String attribute) {
		m_FillColor = attribute;
	}

	public void setFontSize(int attribute) {
		m_FontSize = attribute;
	}

	public void setStrokeColor(String attribute) {
		m_StrokeColor = attribute;
	}

	public void setStrokeWidth(String attribute) {
		m_StrokeWidth = attribute;
	}

	@Override
	public String toString() {
		return "Style [m_FillColor=" + m_FillColor + ", m_FontSize=" + m_FontSize + ", m_StrokeColor=" + m_StrokeColor
				+ ", m_StrokeWidth=" + m_StrokeWidth + "]";
	}

	public String getFillColor() {
		return m_FillColor;
	}

	public int getFontSize() {
		return m_FontSize;
	}

	public String getStrokeColor() {
		return m_StrokeColor;
	}

	public String getStrokeWidth() {
		return m_StrokeWidth;
	}
	
	public String getId() {
		return m_Id;
	}
}
