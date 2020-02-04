package SwitchedCapCalculation;
import java.util.Arrays;

import SwitchedCapCalculation.SwitchedCapCircuit;

public class Switch extends VoltageDependency {
	private boolean conductiveState;
	
	protected Switch(String name, int positiveNodeIndex, int negativeNodeIndex, SwitchedCapCircuit circuit) throws NullPointerException{
		if(null==name)
			throw new NullPointerException("name is null");
		if(null==circuit)
			throw new NullPointerException("circuit is null");
		this.name=name;
		this.circuit=circuit;
		this.positiveConductiveNodeIndex=positiveNodeIndex;
		this.negativeConductiveNodeIndex=negativeNodeIndex;
		this.name=new String(name);
		this.conductiveState=false;
	}
	
	public String getPositiveNodeName() {return circuit.getNodeName(positiveConductiveNodeIndex);}
	public String getNegativeNodeName() {return circuit.getNodeName(negativeConductiveNodeIndex);}
	
	public void setConductiveState(boolean conductiveState) {this.conductiveState=conductiveState;}

/*VoltageDependency interface*/
	protected boolean getConductiveState() {return conductiveState;}
	protected boolean isActiveComponent() {return false;}
	
	protected double [] getCoefficients(int cols) {
		double [] nodeMat=new double[cols];
		nodeMat[positiveConductiveNodeIndex]=1.0;
		nodeMat[negativeConductiveNodeIndex]=-1.0;
		return nodeMat;
	}
	protected double getFreeCoefficient() {
		return 0;
	}
		
}
