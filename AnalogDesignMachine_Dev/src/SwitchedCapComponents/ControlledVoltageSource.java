package SwitchedCapComponents;

import SwitchedCapCalculation.SwitchedCapCircuit;
import SwitchedCapCalculation.SwitchedCapComponent;
import SwitchedCapCalculation.VoltageDependency;

public class ControlledVoltageSource implements SwitchedCapComponent {
	private VoltageDependency src;
	
	public ControlledVoltageSource(String name, String positiveNodeName, String negativeNodeName) {
		src=new VoltageDependency(name, positiveNodeName, negativeNodeName, true);
		src.setConductiveState(true);
	}
	
	public ControlledVoltageSource setVoltage(double voltage) {
		src.setFreeCoefficient(voltage);
		return this;
	}
	
	public ControlledVoltageSource updateVoltageInAllStates(double voltage) {
		SwitchedCapCircuit circuit=src.getCircuit();
		if(circuit==null) {
			throw new RuntimeException("ControlledVoltageSource "+src.getName()+" is not added to any circuit");
		}
		circuit.updateFreeCoefficientForStates(src);
		return this;
	}
		
	/**/
	public void addToCircuit(SwitchedCapCircuit circuit) {
		src.addToCircuit(circuit);
	}
}
