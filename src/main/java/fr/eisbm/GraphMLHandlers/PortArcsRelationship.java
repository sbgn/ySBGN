package fr.eisbm.GraphMLHandlers;

import java.util.HashSet;
import java.util.Set;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Port;

public class PortArcsRelationship {
	
	public enum PortType
	{
		SourcePort,
		TargetPort
	}

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
	
	public void addArcToSet(Arc arc) {
		this.m_ConnectedArcs.add(arc);
	}

	public void setType(PortType portType) {
		m_portType = portType;
		
	}

	public PortType getPortType() {
		return m_portType;
	}

	public void removeArcFromSet(Arc arc) {
		this.m_ConnectedArcs.remove(arc);
	}

}
