package SwitchedCapCalculation;
import SwitchedCapCalculation.SwitchedCapCircuit;

public class Capacitor implements SwitchedCapComponent {
	private SwitchedCapCircuit circuit;
	private String name;
	private String positiveNodeName, negativeNodeName;
	private int positiveNodeIndex, negativeNodeIndex;
	private double capacitance;
	
	public Capacitor(String name, String positiveNodeName, String negativeNodeName) {
		if(null==name || positiveNodeName==null || negativeNodeName==null) {
			throw new NullPointerException("parameters should not be null");
		}
		this.name=name;
		this.positiveNodeName=positiveNodeName;
		this.negativeNodeName=negativeNodeName;
		this.circuit=null;
		this.capacitance=0;
	}
	
	public String getName() {return name;}
	public String getPositiveNodeName() {return positiveNodeName;}
	public String getNegativeNodeName() {return negativeNodeName;}
	public double getCapacitance() {return capacitance;}
	
	public Capacitor setCapacitance(double capacitance) {
		if(capacitance<0) {
			throw new RuntimeException("capacitance is negative");
		}
		if(circuit != null && circuit.isCircuitLocked()) {
			throw new RuntimeException("circuit is locked");
		}
		this.capacitance=capacitance;
		return this;
	}
	
	/*interaction inside package. No check, that component added to circuit*/
	protected int getPositiveNodeIndex() {return positiveNodeIndex;}
	protected int getNegativeNodeIndex() {return negativeNodeIndex;}
	
	/**add component to circuit */
	public void addToCircuit(SwitchedCapCircuit circuit) {
		if(this.circuit != null) {
			throw new RuntimeException("capacitor "+name+" is already added to some circuit");
		}
		/*circuit lock is checked inside SwitchedCapCircuit*/
		this.circuit=circuit;
		positiveNodeIndex=this.circuit.addNode(positiveNodeName);
		negativeNodeIndex=this.circuit.addNode(negativeNodeName);
		this.circuit.addCapacitor(this);
	}
	
}
