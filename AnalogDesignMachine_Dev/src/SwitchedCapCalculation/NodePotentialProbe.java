package SwitchedCapCalculation;

public class NodePotentialProbe {
	private SwitchedCapCircuit circuit;
	private int nodeIndex;
	
	protected NodePotentialProbe(int nodeIndex, SwitchedCapCircuit circuit) {
		this.circuit=circuit;
		this.nodeIndex=nodeIndex;
	}
	
	public String getNodeName() {
		return circuit.getNodeName(nodeIndex);
	}
	
	public double getNodePotential() {
		return circuit.getNodePotential(nodeIndex);
	}
}
