package SwitchedCapCalculationTest;

import java.util.ArrayList;
import java.util.HashMap;

import SwitchedCapCalculation.NodePotentialProbe;
import SwitchedCapCalculation.SwitchedCapCircuit;

public abstract class AbstractTestModel {
	protected SwitchedCapCircuit circuit;
	protected ArrayList<NodePotentialProbe> nodeProbes;
	protected HashMap<String, Double> correctPotentials;
	protected double nodePotentialAccuracy=1e-3;
	
	public SwitchedCapCircuit getSwitchedCapCircuit() {return circuit;}
	public abstract int getStateNumber();
	public abstract void setState(int state);
	
	public boolean checkState(int state) {
		NodePotentialProbe prb;
		boolean correctResult=true;
		double calcValue, correctValue;
		if(nodeProbes.size()!=correctPotentials.size())
			throw new RuntimeException("nodeProbes.size()!=correctPotentials.size()");
		for(int i=0; i<nodeProbes.size(); i++) {
			prb=nodeProbes.get(i);
			calcValue=prb.getNodePotential();
			correctValue=correctPotentials.get(prb.getNodeName());
			if(!TestUtil.approxEqual(calcValue, correctValue, nodePotentialAccuracy)) {
				System.out.println(prb.getNodeName()+": calc="+calcValue+"; correct="+correctValue+"; state="+state);
				correctResult=false;
			}
		}
		return correctResult;
	}
	
	public boolean checkCalculation() {
		boolean correctResult=true;
		for(int i=0; i<getStateNumber(); i++) {
			setState(i);
			circuit.calculate();
			correctResult &= checkState(i);
		}
		return correctResult;
	}
}
