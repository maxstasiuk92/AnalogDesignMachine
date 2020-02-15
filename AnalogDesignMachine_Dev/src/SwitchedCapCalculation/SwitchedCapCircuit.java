package SwitchedCapCalculation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
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
	private HashMap<String, SwitchedCapCircuitState> stateList;
	
	protected SwitchedCapCircuit() {
		circuitLocked=false;
		circuitCalculated=false;
		nodeNameList=new ArrayList<String>();
		capacitorList=new ArrayList<Capacitor>();
		voltageDependencyList=new ArrayList<VoltageDependency>();
		constNodePotentialList=new ArrayList<ConstNodePotential>();
		stateCreator=null;
		stateList=new HashMap<String, SwitchedCapCircuitState>();
	}
	
	protected void setStateCreator(SwitchedCapCircuitStateCreator stateCreator) {
		this.stateCreator=stateCreator;
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
	/*returns index of node in list*/
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
		if(circuitLocked) {
			throw new RuntimeException("circuit is locked");
		}
		ControlledVoltageSource vsrc=new ControlledVoltageSource(name, addNode(positiveNodeName), addNode(negativeNodeName), this);
		voltageDependencyList.add(vsrc);
		return vsrc;
	}
	
	/**unique name is not mandatory*/
	public SingleEndedAmplifier addSingleEndedAmplifier(String name, String positiveOutputNodeName, String negativeOutputNodeName,
			String positiveInputNodeName, String negativeInputNodeName) {
		if(circuitLocked) {
			throw new RuntimeException("circuit is locked");
		}
		SingleEndedAmplifier amp=new SingleEndedAmplifier(name, addNode(positiveOutputNodeName), addNode(negativeOutputNodeName),
				addNode(positiveInputNodeName), addNode(negativeInputNodeName), this);
		voltageDependencyList.add(amp);
		return amp;
	}
	
	public NodePotentialProbe getNodePotentialProbe(String nodeName) {
		if(!circuitLocked) {
			throw new RuntimeException("circuit is NOT locked");
		}
		int nodeIndex=nodeNameList.lastIndexOf(nodeName);
		if(nodeIndex<0) {
			throw new RuntimeException("node was not found");
		}
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
	
	protected void updateFreeCoefficientForStates(VoltageDependency src) {
		//search index of instance
		boolean updated;
		int instInd=voltageDependencyList.lastIndexOf(src);
		if(instInd<0) {
			throw new RuntimeException("VoltageDependency "+src+" was not found in circuit");
		}
		//iterate over all states
		Iterator<SwitchedCapCircuitState> stateIterator=stateList.values().iterator();
		while(stateIterator.hasNext()) {
			SwitchedCapCircuitState state=stateIterator.next();
			updated=false;
			for(int i=0; i<state.getEquNumber() && !updated; i++) {
				if(state.isVoltageDependencyEquation(i) && state.getEquInstInd(i)==instInd) {
					state.setFreeCoefficient(src.getFreeCoefficient(), i);
					updated=true;
				}
			}
		}
	}
	
	/*create SwichedCapCircuitState*/
	private void setChargesToState(SwitchedCapCircuitState state) {
		int capNum=capacitorList.size();
		//fill free coefficients
		for(int i=0; i<state.getEquNumber(); i++) {
			if(state.isChargeTransferEquation(i)) {
				double charge=0;
				for(int j=0; j<capNum; j++) {
					charge+=state.getCapConnMatEntry(i, j)*capacitorCharge.getEntry(j, 0); //avoid matrix mult. for efficiency
				}
				state.setFreeCoefficient(charge, i);
			}
		}
	}
	
	private void setVoltagesToState(SwitchedCapCircuitState state) {
		for(int i=0; i<state.getEquNumber(); i++) {
			if(state.isVoltageDependencyEquation(i)) {
				VoltageDependency src=voltageDependencyList.get(state.getEquInstInd(i));
				state.setFreeCoefficient(src.getFreeCoefficient(), i);
			}
		}
	}
	
	private void calculateCharges() {
		int capNum=capacitorList.size();
		//calculate capacitor charges
		for(int i=0; i<capNum; i++) {
			Capacitor cap=capacitorList.get(i);
			double capVolt=nodePotential.getEntry(cap.getPositiveNodeIndex(), 0)-nodePotential.getEntry(cap.getNegativeNodeIndex(), 0);
			capacitorCharge.setEntry(i, 0, capVolt*cap.getCapacitance());
		}
	}
	
	/**calculate potentials and charges*/
	public void calculate() {
		if(!circuitLocked) {
			throw new RuntimeException("circuit is not locked");
		}
		if(stateCreator==null) {
			throw new RuntimeException("stateCreator is not set");
		}
		SwitchedCapCircuitState state=stateCreator.createState();
		setChargesToState(state);
		setVoltagesToState(state);
		//calculate node potentials
		nodePotential=state.calculateRoots();
		//calculate capacitor charges
		calculateCharges();
		//circuit calculated
		circuitCalculated=true;
	}
	
	public void calculateState(String stateName) {
		if(!circuitLocked) {
			throw new RuntimeException("circuit is not locked");
		}
		if(stateCreator==null) {
			throw new RuntimeException("stateCreator is not set");
		}
		SwitchedCapCircuitState state;
		state=stateList.get(stateName);
		if(state == null) {
			throw new RuntimeException("state with name "+stateName+" does NOT exist");
		}
		setChargesToState(state);
		//calculate node potentials
		nodePotential=state.calculateRoots();
		//calculate capacitor charges
		calculateCharges();
		//circuit calculated
		circuitCalculated=true;
	}
	
	public void saveState(String stateName) {
		if(!circuitLocked) {
			throw new RuntimeException("circuit is not locked");
		}
		if(stateCreator==null) {
			throw new RuntimeException("stateCreator is not set");
		}
		SwitchedCapCircuitState state;
		state=stateList.get(stateName);
		if(state != null) {
			throw new RuntimeException("state with name "+stateName+" already exists");
		}
		state=stateCreator.createState();
		stateList.put(stateName, state);
	}
}
