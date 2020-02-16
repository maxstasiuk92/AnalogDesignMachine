package SwitchedCapComponents;

import SwitchedCapCalculation.*;

public class Switch implements SwitchedCapComponent {
	private VoltageDependency sw;
	
	public Switch(String name, String positiveNodeName, String negativeNodeName) {
		sw=new VoltageDependency(name, positiveNodeName, negativeNodeName, false);
	}
	
	public void setConductiveState(boolean conductiveState) {sw.setConductiveState(conductiveState);}

	public void addToCircuit(SwitchedCapCircuit circuit) {
		sw.addToCircuit(circuit);
	}
}
