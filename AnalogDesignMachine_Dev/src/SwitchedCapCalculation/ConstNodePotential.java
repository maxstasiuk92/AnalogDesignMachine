package SwitchedCapCalculation;

public class ConstNodePotential implements SwitchedCapComponent {
	private SwitchedCapCircuit circuit;
	private String nodeName;
	private int nodeIndex;
	private double nodePotential;
	
	public ConstNodePotential(String nodeName, double nodePotential) {
		if(nodeName == null) {
			throw new NullPointerException("nodeName should not be null");
		}
		this.nodeName=nodeName;
		this.nodePotential=nodePotential;
		this.circuit=null;
	}
	
	/*only for package to create extra const. node*/
	protected ConstNodePotential(int nodeIndex, double nodePotential, SwitchedCapCircuit circuit) {
		this.circuit=circuit;
		this.nodeIndex=nodeIndex;
		this.nodePotential=nodePotential;
		this.nodeName=circuit.getNodeName(this.nodeIndex);
	}

	public String getNodeName() {return nodeName;}
	public double getNodePotential() {return nodePotential;}
	
	/*interaction inside package. No check, that component added to circuit*/
	int getNodeIndex() {return nodeIndex;}
	
	/**add component to circuit */
	public void addToCircuit(SwitchedCapCircuit circuit) {
		if(this.circuit != null) {
			throw new RuntimeException("const node "+nodeName+" is already added to some circuit");
		}
		/*circuit lock is checked inside SwitchedCapCircuit*/
		this.circuit=circuit;
		nodeIndex=this.circuit.addNode(nodeName);
		this.circuit.addConstNodePotential(this);
	}
}
