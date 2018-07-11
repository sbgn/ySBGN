package fr.eisbm.GRAPHML2SBGNML;

import java.util.HashSet;
import java.util.Set;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Port;

import fr.eisbm.GRAPHML2SBGNML.FileUtils.PortType;

public class PortArcsRelationship {

	private Port m_Port;
	private Set<Arc> m_ConnectedArcs;
	private PortType m_portType;

	public PortArcsRelationship(Port currentPort) {
		super();
		this.m_ConnectedArcs = new HashSet<Arc>();;
		this.m_Port = currentPort;
	}

	public Port getPort() {
		return m_Port;
	}

	public Set<Arc> getConnectedArcs() {
		return m_ConnectedArcs;
	}

	public void setPort(Port currentPort) {
		this.m_Port = currentPort;
	}

	public void setConnectedArcs(Set<Arc> setConnectedArcs) {
		this.m_ConnectedArcs = setConnectedArcs;
	}
	
	public void addArcToSet(Arc _arc) {
		this.m_ConnectedArcs.add(_arc);
	}

	public void setType(PortType portType) {
		m_portType = portType;
		
	}

	public PortType getPortType() {
		return m_portType;
	}

}
