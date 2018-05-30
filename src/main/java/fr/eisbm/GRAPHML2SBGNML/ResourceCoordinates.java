package fr.eisbm.GRAPHML2SBGNML;

public class ResourceCoordinates {
	float m_fXCoord;
	float m_fYCoord;
	public ResourceCoordinates(float fXCoord, float fYCoord) {
		super();
		this.m_fXCoord = fXCoord;
		m_fYCoord = fYCoord;
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

}
