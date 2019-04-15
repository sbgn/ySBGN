package fr.eisbm.GraphMLHandlers;

public class ResourceCoordinates {
	float m_fXCoord;
	float m_fYCoord;
	String m_szText = null;
	public ResourceCoordinates(float fXCoord, float fYCoord, String szText) {
		super();
		this.m_fXCoord = fXCoord;
		m_fYCoord = fYCoord;
		m_szText = szText;
	}
	public float getXCoord() {
		return m_fXCoord;
	}
	public float getYCoord() {
		return m_fYCoord;
	}
	public void updateXCoord(float fXCoord) {
		this.m_fXCoord += fXCoord;
	}
	public void updateYCoord(float fYCoord) {
		m_fYCoord += fYCoord;
	}
	public String getText() {
		
		return m_szText;
	}

}
