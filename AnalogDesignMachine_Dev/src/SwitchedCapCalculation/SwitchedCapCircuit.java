package SwitchedCapCalculation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.linear.*;

import SwitchedCapComponents.ControlledVoltageSource;
import SwitchedCapComponents.SingleEndedAmplifier;
import SwitchedCapComponents.Switch;


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
	/**returns index of node in list*/
	protected int addNode(String nodeName) {
		if(circuitLocked) {
			throw new RuntimeException("circuit is locked");
		}
		int nodeIndex=nodeNameList.lastIndexOf(nodeName);
		if(nodeIndex<0) {
			nodeNameList.add(nodeName);
			nodeIndex=nodeNameList.size()-1;
		}
		return nodeIndex;
	}
	
	/**unique name is not mandatory*/
	protected Capacitor addCapacitor(Capacitor cap) {
		if(circuitLocked) {
			throw new RuntimeException("circuit is locked");
		}
		capacitorList.add(cap);
		return cap;
	}
	
	protected ConstNodePotential addConstNodePotential(ConstNodePotential constNode) {
		if(circuitLocked) {
			throw new RuntimeException("circuit is locked");
		}
		//check, that new const potential
		for(int i=0; i<constNodePotentialList.size(); i++) {
			if(constNodePotentialList.get(i).getNodeName().equals(constNode.getNodeName())) {
				throw new RuntimeException("try to redefined const potential for "+constNode.getNodeName());
			}
		}
		constNodePotentialList.add(constNode);
		return constNode;
	}
	
	/**unique name is not mandatory*/
	protected VoltageDependency addVoltageDependency(VoltageDependency voltageDependency) {
		if(circuitLocked) {
			throw new RuntimeException("circuit is locked");
		}
		voltageDependencyList.add(voltageDependency);
		return voltageDependency;
	}
	
	
	
	public void addComponent(SwitchedCapComponent component) {
		component.addToCircuit(this);
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
	
	public void updateFreeCoefficientForStates(VoltageDependency src) {
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
