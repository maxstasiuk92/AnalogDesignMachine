package SwitchedCapComponents;

import SwitchedCapCalculation.SwitchedCapCircuit;
import SwitchedCapCalculation.SwitchedCapComponent;
import SwitchedCapCalculation.VoltageDependency;

public class DifferentialVoltageSource implements SwitchedCapComponent{
	private VoltageDependency srcP, srcN, srcCM;
	private double differentialVoltage, commonModeVoltage;
	
	public DifferentialVoltageSource(String name, String positiveNodeName, String negativeNodeName,
			String commonModeNodeName, String groundName) {
		srcP=new VoltageDependency(name, positiveNodeName, groundName, true);
		srcP.setConductiveState(true);
		srcN=new VoltageDependency(name, negativeNodeName, groundName, true);
		srcN.setConductiveState(true);
		srcCM=new VoltageDependency(name, commonModeNodeName, groundName, true);
		srcCM.setConductiveState(true);
		setVoltages(0, 0);
	}
		
	public DifferentialVoltageSource setDifferentialVoltage(double diffVolt) {
		setVoltages(diffVolt, commonModeVoltage);
		return this;
	}
	
	public DifferentialVoltageSource setCommonModeVoltage(double commModeVolt) {
		setVoltages(differentialVoltage, commModeVolt);
		return this;
	}
	
	public DifferentialVoltageSource setVoltages(double diffVolt, double commModeVolt) {
		differentialVoltage=diffVolt;
		commonModeVoltage=commModeVolt;
		srcP.setFreeCoefficient(differentialVoltage/2+commonModeVoltage);
		srcN.setFreeCoefficient(-differentialVoltage/2+commonModeVoltage);
		srcCM.setFreeCoefficient(commonModeVoltage);
		return this;
	}
	
	public DifferentialVoltageSource updateVoltagesInAllStates(double diffVolt, double commModeVolt) {
		setVoltages(diffVolt, commModeVolt);
		VoltageDependency [] srcArr= {srcN, srcP, srcCM};
		for(VoltageDependency src: srcArr) {
			SwitchedCapCircuit circuit=src.getCircuit();
			if(circuit==null) {
				throw new RuntimeException("DifferentialVoltageSource "+src.getName()+" is not added to any circuit");
			}
			circuit.updateFreeCoefficientForStates(src);
		}
		return this;
	}
	
	public double getDifferentialVoltage() {return differentialVoltage;}
	public double getCommonModeVoltage() {return commonModeVoltage;}
	
	public void addToCircuit(SwitchedCapCircuit circuit) {
		srcP.addToCircuit(circuit);
		srcN.addToCircuit(circuit);
		srcCM.addToCircuit(circuit);		
	}

}
