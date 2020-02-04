package SwitchedCapCalculation;

abstract class VoltageDependency {
	protected SwitchedCapCircuit circuit;
	protected String name;
	protected int positiveConductiveNodeIndex, negativeConductiveNodeIndex;
	public String GetName() {return new String(name);}
	protected int getPositiveConductiveNodeIndex() {return positiveConductiveNodeIndex;}
	protected int getNegativeConductiveNodeIndex() {return negativeConductiveNodeIndex;}
	//model:
	//NodePotentialMatrix=FreeCoefficient
	protected abstract boolean getConductiveState();
	protected abstract boolean isActiveComponent();
	protected abstract double [] getCoefficients(int cols);
	protected abstract double getFreeCoefficient();
	
}
