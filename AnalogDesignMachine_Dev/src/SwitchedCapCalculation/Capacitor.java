package SwitchedCapCalculation;
import SwitchedCapCalculation.SwitchedCapCircuit;

public class Capacitor {
	private SwitchedCapCircuit circuit;
	private String name;
	private int positiveNodeIndex, negativeNodeIndex;
	private double capacitance;
	
	protected Capacitor(String name, int positiveNodeIndex, int negativeNodeIndex, SwitchedCapCircuit circuit) {
		if(null==name)
			throw new NullPointerException("name is null");
		this.name=new String(name);
		this.positiveNodeIndex=positiveNodeIndex;
		this.negativeNodeIndex=negativeNodeIndex;
		this.circuit=circuit;
		this.capacitance=0;
	}
	
	public String getName() {return new String(name);}
	public String getPositiveNodeName() {return circuit.getNodeName(positiveNodeIndex);}
	public String getNegativeNodeName() {return circuit.getNodeName(negativeNodeIndex);}
	public double getCapacitance() {return capacitance;}
	
	public Capacitor setCapacitance(double capacitance) {
		if(capacitance<0)
			throw new RuntimeException("capacitance is negative");
		if(circuit.isCircuitLocked())
			throw new RuntimeException("circuit is locked");
		this.capacitance=capacitance;
		return this;
	}
	
	/*interaction inside package*/
	int getPositiveNodeIndex() {return positiveNodeIndex;}
	int getNegativeNodeIndex() {return negativeNodeIndex;}
	
}
