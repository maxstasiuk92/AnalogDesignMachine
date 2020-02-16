package SwitchedCapCalculation;

import java.util.Arrays;

public class SingleEndedAmplifier extends VoltageDependency {
	private int positiveInputNodeIndex, negativeInputNodeIndex;
	private double openLoopGain, inputOffsetVoltage;
	
	protected SingleEndedAmplifier(String name, int positiveOutputNodeIndex, int negativeOutputNodeIndex,
			int positiveInputNodeIndex, int negativeInputNodeIndex, SwitchedCapCircuit circuit)
	{
		if(null==circuit)
			throw new RuntimeException("circuit is null");
		this.name=name;
		this.circuit=circuit;
		this.positiveConductiveNodeIndex=positiveOutputNodeIndex;
		this.negativeConductiveNodeIndex=negativeOutputNodeIndex;
		this.positiveInputNodeIndex=positiveInputNodeIndex;
		this.negativeInputNodeIndex=negativeInputNodeIndex;
		this.openLoopGain=1;
		this.inputOffsetVoltage=0;
	}

	public double getOpenLoopGain() {return openLoopGain;}
	public double getInputOffsetVoltage() {return inputOffsetVoltage;}
	
	public SingleEndedAmplifier setOpenLoopGain(double openLoopGain) {
		if(circuit.isCircuitLocked())
			throw new RuntimeException("circuit is locked");
		if(openLoopGain<=0)
			throw new RuntimeException("openLoop is negative or zero");
		this.openLoopGain=openLoopGain;
		return this;
	}
	
	public SingleEndedAmplifier setInputOffsetVoltage(double inputOffsetVoltage) {
		if(circuit.isCircuitLocked())
			throw new RuntimeException("circuit is locked");
		this.inputOffsetVoltage=inputOffsetVoltage;
		return this;
	}

/*VoltageDependency interface*/
	protected boolean getConductiveState() {return true;}
	protected boolean isActiveComponent() {return true;}
	
	protected double [] getCoefficients(int cols) {
		double [] nodeMat=new double[cols];
		nodeMat[positiveConductiveNodeIndex]=-1;
		nodeMat[negativeConductiveNodeIndex]=1;
		nodeMat[positiveInputNodeIndex]=openLoopGain;
		nodeMat[negativeInputNodeIndex]=-openLoopGain;
		return nodeMat;
	}
	
	protected double getFreeCoefficient() {
		return -inputOffsetVoltage*openLoopGain;
	}
}
