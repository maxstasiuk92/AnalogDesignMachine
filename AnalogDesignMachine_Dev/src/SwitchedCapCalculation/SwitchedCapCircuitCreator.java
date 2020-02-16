package SwitchedCapCalculation;

public class SwitchedCapCircuitCreator {
	
	public static SwitchedCapCircuit createSwitchedCapCircuit() {
		SwitchedCapCircuit circuit=new SwitchedCapCircuit();
		circuit.setStateCreator(new SwitchedCapCircuitStateCreator(circuit));
		return circuit;
	}
}
