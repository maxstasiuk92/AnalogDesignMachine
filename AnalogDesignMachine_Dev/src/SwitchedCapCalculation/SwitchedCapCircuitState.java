package SwitchedCapCalculation;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

class SwitchedCapCircuitState {
	private static enum equationType {invalidEquation, chargeTransferEquation, voltageDependencyEquation, constPotentialEquation, fixFloatingNodeEquation};
	protected equationType [] equType;
	protected int [] equInstInd;
	protected RealMatrix coefficients;
	private RealMatrix freeCoefficients;
	/*also may allow to change non-zero capacitance to non-zero capacitance without recreating state*/
	protected RealMatrix capConnMat;
	
	protected ArrayList<ConstNodePotential> extraConstNodePotentialList;
	public boolean stateLocked;
	public DecompositionSolver solver;
	
	public SwitchedCapCircuitState(int nodeNum, int capNum) {
		stateLocked=false;
		equType=new equationType[nodeNum];
		for(int i=0; i<nodeNum; i++)
			equType[i]=equationType.invalidEquation;
		equInstInd=new int[nodeNum];
		coefficients=MatrixUtils.createRealMatrix(nodeNum, nodeNum);
		freeCoefficients=MatrixUtils.createRealMatrix(nodeNum, 1);
		capConnMat=MatrixUtils.createRealMatrix(nodeNum, capNum);
		extraConstNodePotentialList=null;
	}
	
	public int getEquNumber() {return equType.length;}
	public int getEquInstInd(int r) {return equInstInd[r];}
	public double getCoefficient(int r, int c) {return coefficients.getEntry(r, c);}
	public double getFreeCoefficient(int r) {return freeCoefficients.getEntry(r, 0);}
	public double getCapConnMatEntry(int r, int c) {return capConnMat.getEntry(r, c);}
	
	public boolean isInvalidEquation(int r) {return equType[r]==equationType.invalidEquation;}
	public boolean isChargeTransferEquation(int r) {return equType[r]==equationType.chargeTransferEquation;}
	public boolean isVoltageDependencyEquation(int r) {return equType[r]==equationType.voltageDependencyEquation;}
	public boolean isConstPotentialEquation(int r) {return equType[r]==equationType.constPotentialEquation;}
	public boolean isFixFloatingNodeEquation(int r) {return equType[r]==equationType.fixFloatingNodeEquation;}
	
	private void throwExceptionIfLocked() {
		if(stateLocked)
			throw new RuntimeException("state is locked");
	}
	
	public void setChargeTransferEquation(int r) {
		throwExceptionIfLocked();
		equType[r]=equationType.chargeTransferEquation;
	}
	public void setVoltageDependencyEquation(int r) {
		throwExceptionIfLocked();
		equType[r]=equationType.voltageDependencyEquation;
	}
	public void setConstPotentialEquation(int r) {
		throwExceptionIfLocked();
		equType[r]=equationType.constPotentialEquation;
	}
	public void setFixFloatingNodeEquation(int r) {
		throwExceptionIfLocked();
		equType[r]=equationType.fixFloatingNodeEquation;
	}
	
	public void setEquInstInd(int indVal, int r) {
		throwExceptionIfLocked();
		equInstInd[r]=indVal;
	}
	public void setCoefficient(double val, int r, int c) {
		throwExceptionIfLocked();
		coefficients.setEntry(r, c, val);
	}
	public void setFreeCoefficient(double val, int r) {
		freeCoefficients.setEntry(r, 0, val);
	}
	public void setCapConnMatEntry(byte val, int r, int c) {
		throwExceptionIfLocked();
		capConnMat.setEntry(r, c, (double)val);
	}
	
	public void setExtraConstNodePotentialList(ArrayList<ConstNodePotential> extraConstNodes) {
		throwExceptionIfLocked();
		extraConstNodePotentialList=extraConstNodes;
	}
	
	public void addToCoefficient(double val, int r, int c) {
		throwExceptionIfLocked();
		coefficients.addToEntry(r, c, val);
	}
	
	public void setCoefficientRow(double [] val, int r) {
		throwExceptionIfLocked();
		coefficients.setRow(r, val);
	}
	
	public boolean checkEquationsComplete() {
		for(int i=0; i<equType.length; i++)
			if(equType[i]==equationType.invalidEquation)
				return false;
		return true;
	}
	
	public void lockState() {
		if(stateLocked)
			throw new RuntimeException("state is already locked");
		if(!checkEquationsComplete())
			throw new RuntimeException("not enough equations in state, can't lock");
		if(extraConstNodePotentialList==null)
			throw new RuntimeException("extraConstNodePotentialList is not set, can't lock");
		
		solver=new LUDecomposition(coefficients).getSolver();
		stateLocked=true;
	}
	
	public RealMatrix calculateRoots() {
		if(!stateLocked)
			throw new RuntimeException("state is not locked, can not provide solver");
		return solver.solve(freeCoefficients);
	}
}
