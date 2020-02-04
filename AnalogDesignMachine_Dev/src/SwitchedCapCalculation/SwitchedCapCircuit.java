package SwitchedCapCalculation;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.*;


public class SwitchedCapCircuit {
	private boolean circuitLocked; //all parameters are locked, calculations are allowed
	private boolean circuitCalculated; //node potentials were calculated at least once
	private List<String> nodeNameList;
	private List<Capacitor> capacitorList;
	private List<VoltageDependency> voltageDependencyList;
	private List<ConstNodePotential> constNodePotentialList;
	
	private RealMatrix nodePotential, capacitorCharge;
	private SwitchedCapCircuitStateCreator stateCreator;
	
	public SwitchedCapCircuit() {
		circuitLocked=false;
		circuitCalculated=false;
		nodeNameList=new ArrayList<String>();
		capacitorList=new ArrayList<Capacitor>();
		voltageDependencyList=new ArrayList<VoltageDependency>();
		constNodePotentialList=new ArrayList<ConstNodePotential>();
		stateCreator=new SwitchedCapCircuitStateCreator(this);
	}
	
	protected List<String> getReadOnlyNodeNameList() {return Collections.unmodifiableList(nodeNameList);}
	protected List<Capacitor> getReadOnlyCapacitorList() {return Collections.unmodifiableList(capacitorList);}
	protected List<VoltageDependency> getReadOnlyVoltageDependencyList() {
		return Collections.unmodifiableList(voltageDependencyList);
	} 
	protected List<ConstNodePotential> getReadOnlyConstNodePotentialList(){
		return Collections.unmodifiableList(constNodePotentialList);
	}

/*adding objects to circuit*/
	/**@return index of node in list*/
	private int addNode(String nodeName) {
		int nodeIndex=nodeNameList.lastIndexOf(nodeName);
		if(nodeIndex<0) {
			nodeNameList.add(nodeName);
			nodeIndex=nodeNameList.size()-1;
		}
		return nodeIndex;
	}
	
	/**unique name is not mandatory*/
	public Capacitor addCapacitor(String name, String positiveNodeName, String negativeNodeName) {
		if(circuitLocked)
			throw new RuntimeException("circuit is locked");
		Capacitor cap=new Capacitor(name, addNode(positiveNodeName), addNode(negativeNodeName), this);
		capacitorList.add(cap);
		return cap;
	}
	
	/**unique name is not mandatory*/
	public Switch addSwitch(String name, String positiveNodeName, String negativeNodeName) {
		if(circuitLocked)
			throw new RuntimeException("circuit is locked");
		/*if(positiveNodeName==negativeNodeName)
			throw new RuntimeException("positiveNodeName and negativeNodeName are equal");*/
		Switch sw=new Switch(name, addNode(positiveNodeName), addNode(negativeNodeName), this);
		voltageDependencyList.add(sw);
		return sw;
	}
	
	public ConstNodePotential addConstNodePotential(String nodeName, double potential) {
		if(circuitLocked)
			throw new RuntimeException("circuit is locked");
		//check, that new const potential
		int i=0;
		while(i<constNodePotentialList.size()) {
			if(constNodePotentialList.get(i).getNodeName().equals(nodeName))
				throw new RuntimeException("try to redefined const potential for "+nodeName);
			i++;
		}
		ConstNodePotential pot=new ConstNodePotential(addNode(nodeName), potential, this);
		constNodePotentialList.add(pot);
		return pot;
	}
	
	/**unique name is not mandatory*/
	public ControlledVoltageSource addControlledVoltageSource(String name, String positiveNodeName, 
			String negativeNodeName) {
		if(circuitLocked)
			throw new RuntimeException("circuit is locked");
		/*if(positiveNodeName==negativeNodeName)
			throw new RuntimeException("positiveNodeName and negativeNodeName are equal");*/
		ControlledVoltageSource vsrc=new ControlledVoltageSource(name, addNode(positiveNodeName), addNode(negativeNodeName), this);
		voltageDependencyList.add(vsrc);
		return vsrc;
	}
	
	/**unique name is not mandatory*/
	public SingleEndedAmplifier addSingleEndedAmplifier(String name, String positiveOutputNodeName, String negativeOutputNodeName,
			String positiveInputNodeName, String negativeInputNodeName) {
		if(circuitLocked)
			throw new RuntimeException("circuit is locked");
		/*if(positiveOutputNodeName==negativeOutputNodeName)
			throw new RuntimeException("positiveOutputNodeName and negativeOutputNodeName are equal");*/
		SingleEndedAmplifier amp=new SingleEndedAmplifier(name, addNode(positiveOutputNodeName), addNode(negativeOutputNodeName),
				addNode(positiveInputNodeName), addNode(negativeInputNodeName), this);
		voltageDependencyList.add(amp);
		return amp;
	}
	
	/*public DifferentialAmplifier AddDifferentialAmplifier(String name, String positiveInputNodeName, String negativeInputNodeName,
			String positiveOutputNodeName, String negativeOutputNodeName, String commonModeReferenceNodeName) {
		
	}*/
	
	public NodePotentialProbe getNodePotentialProbe(String nodeName) {
		if(!circuitLocked) 
			throw new RuntimeException("circuit is NOT locked");
		int nodeIndex=nodeNameList.lastIndexOf(nodeName);
		if(nodeIndex<0) 
			throw new RuntimeException("node was not found");
		return new NodePotentialProbe(nodeIndex, this);
	}
	
	/**locks circuit and prepare matrices*/
	public void lockCircuit() {
		if(circuitLocked)
			throw new RuntimeException("circuit is ALLREADY locked");
		circuitLocked=true;
		capacitorCharge=MatrixUtils.createRealMatrix(capacitorList.size(), 1);
	}
	
	public boolean isCircuitLocked() {
		return circuitLocked;
	}
	
/*return data for classes in the package */
	/*all protected*/
	public double getNodePotential(int nodeIndex) {
		if(!circuitCalculated)
			throw new RuntimeException("circuit was not calculated");
		if(nodeIndex>=nodePotential.getRowDimension())
			throw new RuntimeException("nodeIndex out of range");
		return nodePotential.getEntry(nodeIndex, 0);
	}
	
	protected String getNodeName(int nodeIndex) {
		if(nodeIndex>=nodeNameList.size())
			throw new RuntimeException("nodeIndex out of range");
		return new String(nodeNameList.get(nodeIndex));	
	}
	
	/*create SwichedCapCircuitState*/
	private void calcPotentialAndCharge(SwitchedCapCircuitState state) {
		int nodeNum=nodeNameList.size();
		int capNum=capacitorList.size();
		//fill free coefficients
		for(int i=0; i<nodeNum; i++) {
			if(state.getEquType(i)==SwitchedCapCircuitState.equationType.chargeTransferEquation) {
				double charge=0;
				for(int j=0; j<capNum; j++)
					charge+=state.getCapConnMatEntry(i, j)*capacitorCharge.getEntry(j, 0);
				state.setFreeCoefficient(charge, i);
			}
			else {
				if(state.getEquType(i)==SwitchedCapCircuitState.equationType.voltageDependencyEquation) {
					VoltageDependency src=voltageDependencyList.get(state.getEquInstInd(i));
					state.setFreeCoefficient(src.getFreeCoefficient(), i);
				}
			}
		}
		//calculate node potentials
		nodePotential=state.calculateRoots();
		//calculate capacitor charges
		for(int i=0; i<capNum; i++) {
			Capacitor cap=capacitorList.get(i);
			double capVolt=nodePotential.getEntry(cap.getPositiveNodeIndex(), 0)-nodePotential.getEntry(cap.getNegativeNodeIndex(), 0);
			capacitorCharge.setEntry(i, 0, capVolt*cap.getCapacitance());
		}
		//circuit calculated
		circuitCalculated=true;
	}
	
	/**calculate potentials and charges*/
	public void calculate() {
		if(!circuitLocked)
			throw new RuntimeException("circuit is not locked");
		SwitchedCapCircuitState state=stateCreator.createState();
		calcPotentialAndCharge(state);
	}
	
	/**/
	public void calculateState(String stateName) {
		
	}
}
