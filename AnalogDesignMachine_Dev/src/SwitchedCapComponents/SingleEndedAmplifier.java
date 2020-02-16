package SwitchedCapComponents;

import SwitchedCapCalculation.SwitchedCapCircuit;
import SwitchedCapCalculation.SwitchedCapComponent;
import SwitchedCapCalculation.VoltageDependency;

public class SingleEndedAmplifier implements SwitchedCapComponent {
	private VoltageDependency amp;
	String positiveInputNodeName, negativeInputNodeName;
	private double openLoopGain, inputOffsetVoltage;
	
	
	public SingleEndedAmplifier(String name, String positiveOutputNodeName, String negativeOutputNodeName,
			String positiveInputNodeName, String negativeInputNodeName)
	{
		if(positiveInputNodeName == null || negativeOutputNodeName==null) {
			throw new RuntimeException("node names should not be null");
		}
		amp=new VoltageDependency(name, positiveOutputNodeName, negativeOutputNodeName, true);
		//to get positiveInput-negativeInput=positiveOutput-negativeOutput
		amp.setNodeCoefficient(positiveInputNodeName, -1); 
		amp.setNodeCoefficient(negativeInputNodeName, 1); 
		amp.setConductiveState(true);
		this.positiveInputNodeName=positiveInputNodeName;
		this.negativeInputNodeName=negativeInputNodeName;
		openLoopGain=1;
		inputOffsetVoltage=0;
	}

	public double getOpenLoopGain() {return openLoopGain;}
	public double getInputOffsetVoltage() {return inputOffsetVoltage;}
	
	public SingleEndedAmplifier setOpenLoopGain(double openLoopGain) {
		if(openLoopGain<=0) {
			throw new RuntimeException("openLoop should be > 0");
		}
		this.openLoopGain=openLoopGain;
		//to get positiveInput-negativeInput+inputOffsetVoltage=positiveOutput-negativeOutput
		amp.setNodeCoefficient(positiveInputNodeName, -this.openLoopGain);
		amp.setNodeCoefficient(negativeInputNodeName, this.openLoopGain);
		amp.setFreeCoefficient(this.openLoopGain*this.inputOffsetVoltage);
		return this;
	}
	
	public SingleEndedAmplifier setInputOffsetVoltage(double inputOffsetVoltage) {
		this.inputOffsetVoltage=inputOffsetVoltage;
		//to get positiveInput-negativeInput+inputOffsetVoltage=positiveOutput-negativeOutput
		amp.setFreeCoefficient(-this.openLoopGain*this.inputOffsetVoltage);
		return this;
	}

	public void addToCircuit(SwitchedCapCircuit circuit) {
		amp.addToCircuit(circuit);
	}
}
