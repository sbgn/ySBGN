package fr.eisbm.GRAPHML2SBGNML;

public class GraphMLResource {
	String m_ID;
	String m_Class;
	float m_fHeight;
	float m_fWidth;
	float m_XCoord;
	float m_YCoord;
	String m_szText;
	
	public GraphMLResource(String _id, String _class, float _fHeight, float _fWidth, float _xCoord, float _yCoord, String _text) {
		super();
		this.m_ID = _id;
		this.m_Class = _class;
		this.m_fHeight = _fHeight;
		this.m_fWidth = _fWidth;
		this.m_XCoord = _xCoord;
		this.m_YCoord = _yCoord;
		this.m_szText = _text;
	}

	public GraphMLResource() {
		super();
		this.m_ID = "";
		this.m_Class = FileUtils.COM_YWORKS_SBGN_STATE_VARIABLE;
		this.m_fHeight = 0;
		this.m_fWidth = 0;
		this.m_XCoord = 0;
		this.m_YCoord = 0;
		this.m_szText = "";
	}

	public String getId() {
		return m_ID;
	}

	public String getResourceClass() {
		return m_Class;
	}

	public float getHeight() {
		return m_fHeight;
	}

	public float getWidth() {
		return m_fWidth;
	}

	public float getXCoord() {
		return m_XCoord;
	}

	public float getYCoord() {
		return m_YCoord;
	}
	
	public String getText() {
		return this.m_szText;
		
	}

	public void setId(String _id) {
		this.m_ID = _id;
	}

	public void setClass(String _class) {
		this.m_Class = _class;
	}

	public void setHeight(float _fHeight) {
		this.m_fHeight = _fHeight;
	}

	public void setWidth(float _fWidth) {
		this.m_fWidth = _fWidth;
	}

	public void setXCoord(float _XCoord) {
		this.m_XCoord = _XCoord;
	}

	public void setYCoord(float _YCoord) {
		this.m_YCoord = _YCoord;
	}

	public void setText(String value) {
		this.m_szText = value;
		
	}
	
	

}
