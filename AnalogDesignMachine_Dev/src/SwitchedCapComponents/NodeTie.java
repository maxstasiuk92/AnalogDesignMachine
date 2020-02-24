package SwitchedCapComponents;

import SwitchedCapCalculation.*;

public class NodeTie implements SwitchedCapComponent {
	private VoltageDependency tie;
	
	public NodeTie(String name, String positiveNodeName, String negativeNodeName) {
		tie=new VoltageDependency(name, positiveNodeName, negativeNodeName, false);
		tie.setConductiveState(true);
	}
	
	public void addToCircuit(SwitchedCapCircuit circuit) {
		tie.addToCircuit(circuit);
	}

}
