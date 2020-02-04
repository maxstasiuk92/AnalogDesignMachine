package SwitchedCapCalculation;

public class ConstNodePotential {
	private SwitchedCapCircuit circuit;
	private int nodeIndex;
	private double nodePotential;
	
	protected ConstNodePotential(int nodeIndex, double nodePotential, SwitchedCapCircuit circuit) {
		this.circuit=circuit;
		this.nodeIndex=nodeIndex;
		this.nodePotential=nodePotential;
	}

	public String getNodeName() {return new String(circuit.getNodeName(nodeIndex));}
	public double getNodePotential() {return nodePotential;}
	
	/*interaction inside package*/
	int getNodeIndex() {return nodeIndex;}
}
