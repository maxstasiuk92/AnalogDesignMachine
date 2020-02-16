package SwitchedCapCalculation;

public class ControlledVoltageSource extends VoltageDependency {
	private double voltage;
	
	protected ControlledVoltageSource(String name, int positiveNodeIndex, int negativeNodeIndex, SwitchedCapCircuit circuit) {
		if(null==name)
			throw new NullPointerException("name is null");
		if(null==circuit)
			throw new NullPointerException("circuit is null");
		this.name=new String(name);
		this.positiveConductiveNodeIndex=positiveNodeIndex;
		this.negativeConductiveNodeIndex=negativeNodeIndex;
		this.circuit=circuit;
		this.voltage=0;
	}
	
	public String getPositiveNodeName() {return circuit.getNodeName(positiveConductiveNodeIndex);}
	public String getNegativeNodeName() {return circuit.getNodeName(negativeConductiveNodeIndex);}
	public double getVoltage() {return voltage;}
	
	public ControlledVoltageSource setVoltage(double voltage) {
		this.voltage=voltage;
		return this;
	}
	
	public ControlledVoltageSource updateVoltageInAllStates(double voltage) {
		this.voltage=voltage;
		circuit.updateFreeCoefficientForStates(this);
		return this;
	}
	
/*VoltageDependency interface*/	
	protected boolean getConductiveState() {return true;}
	protected boolean isActiveComponent() {return true;}
	
	protected double [] getCoefficients(int cols) {
		double [] nodeMat=new double[cols];
		nodeMat[positiveConductiveNodeIndex]=1.0;
		nodeMat[negativeConductiveNodeIndex]=-1.0;
		return nodeMat;
	}
	
	protected double getFreeCoefficient() {
		return voltage;
	}
}
