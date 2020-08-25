package SwitchedCapCalculation;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SwitchedCapCircuitStateCreator {
	private SwitchedCapCircuit circuit;
	//references from SwitchedCapCircuit
	private List<String> nodeNameList;
	private List<Capacitor> capacitorList;
	private List<VoltageDependency> voltageDependencyList;
	private List<ConstNodePotential> constNodePotentialList;
	//auxiliary matrices. Are created once
	private boolean auxiliaryMatricesExist;
	//auxiliary matrices for voltage dependencies
	private byte [][] voltDepAndConstPotMat;
	private boolean [][] voltDepAndConstPotLinComb;
	//auxiliary matrices for charge dependencies
	private byte [][] capConnMat;
	private boolean [] validCapConnMatRow;
	private byte [][] condInstConnMat;
	//list for extra const. potentials. Is created very time state is created
	private ArrayList<ConstNodePotential> extraConstNodePotentialList;
	
	protected SwitchedCapCircuitStateCreator(SwitchedCapCircuit circuit) {
		this.circuit=circuit;
		this.nodeNameList=circuit.getReadOnlyNodeNameList();
		this.capacitorList=circuit.getReadOnlyCapacitorList();
		this.voltageDependencyList=circuit.getReadOnlyVoltageDependencyList();
		this.constNodePotentialList=circuit.getReadOnlyConstNodePotentialList();
		this.auxiliaryMatricesExist=false;
	}
	
	/*
	 * methods for voltage dependency equations
	 */
	
	/*create matrices for voltage dependency equation*/
	private void createMatForVoltDep() {
		voltDepAndConstPotMat=new byte [constNodePotentialList.size()+voltageDependencyList.size()][nodeNameList.size()];
		voltDepAndConstPotLinComb=new boolean [constNodePotentialList.size()+voltageDependencyList.size()][constNodePotentialList.size()+voltageDependencyList.size()];
	}/*createMatForVoltDep*/
	
	/*init matrices for voltage dependency equations*/
	private void initMatForVoltDep() {
		int rows;
		
		rows=voltDepAndConstPotMat.length;
		for(int i=0; i<rows; i++) {
			Arrays.fill(voltDepAndConstPotMat[i], (byte)0);
		}
		int voltDepOffs=constNodePotentialList.size();
		for(int i=0; i<constNodePotentialList.size(); i++) {
			ConstNodePotential cv=constNodePotentialList.get(i);
			voltDepAndConstPotMat[i][cv.getNodeIndex()]=1;
		}
		for(int i=0; i<voltageDependencyList.size(); i++) {
			VoltageDependency v=voltageDependencyList.get(i);
			if(v.getConductiveState()) {
				voltDepAndConstPotMat[i+voltDepOffs][v.getPositiveConductiveNodeIndex()]+=1;
				voltDepAndConstPotMat[i+voltDepOffs][v.getNegativeConductiveNodeIndex()]-=1;
			}
		}
		rows=voltDepAndConstPotLinComb.length;
		for(int i=0; i<rows; i++)
			Arrays.fill(voltDepAndConstPotLinComb[i], false);
	}/*initMatForVoltDep*/
	
	int getIndexOfVoltDep(String n) {
		for(int i=0; i<voltageDependencyList.size(); i++) {
			if(voltageDependencyList.get(i).getName().equals(n)) {
				return i;
			}
		}
		return -1;
	}
	
	/* fill voltage dependency and const potentials linear combinations
	 * defines which voltage dependencies may be presented by lin. comb. of other voltage dependencies -
	 * other words shorted by other voltage dependencies
	 */
	private void fillVoltDepAndConstPotLinComb() {
		int srcNum=constNodePotentialList.size()+voltageDependencyList.size();
		int nodeNum=nodeNameList.size();
		int voltDepOffs=constNodePotentialList.size(); //voltage dependencies start at
		
		//scan by columns
		for(int scanCol=0; scanCol<nodeNum; scanCol++) {
			//search first rows with non-zero element
			int curRow=0;
			//if voltDepAndConstPotLinComb[i][i]==true -> source may NOT be presented by linear comb.
			while(curRow<srcNum && (voltDepAndConstPotMat[curRow][scanCol]==0 || voltDepAndConstPotLinComb[curRow][curRow])) curRow++;
			if(curRow<srcNum) {
				voltDepAndConstPotLinComb[curRow][curRow]=true;
				//modify rest rows, if needed
				int modRow=curRow+1;
				while(modRow<srcNum) {
					if(voltDepAndConstPotMat[modRow][scanCol]!=0 && !voltDepAndConstPotLinComb[modRow][modRow]) {
						byte k=(byte)(voltDepAndConstPotMat[modRow][scanCol]/voltDepAndConstPotMat[curRow][scanCol]);
						for(int modCol=scanCol; modCol<nodeNum; modCol++) {
							voltDepAndConstPotMat[modRow][modCol]-=k*voltDepAndConstPotMat[curRow][modCol];
							//normalize to get +/-1
							if(voltDepAndConstPotMat[modRow][modCol]<0) {
								voltDepAndConstPotMat[modRow][modCol]=-1;
							} else if(voltDepAndConstPotMat[modRow][modCol]>0){
								voltDepAndConstPotMat[modRow][modCol]=1;
							}
						}
						for(int modCol=voltDepOffs; modCol<srcNum; modCol++) {
							voltDepAndConstPotLinComb[modRow][modCol] |= voltDepAndConstPotLinComb[curRow][modCol];
						}
					}
					modRow++;
				}
			}
		}
	} /*fillVoltDepAndConstPotLinComb*/
	
	/* 
	 * returns instances, that create short circuits 
	 */
	private ArrayList<String> getInstancesThatCreateShorts() {
		ArrayList<String> shortInstList=new ArrayList<String>();
		int voltDepOffs=constNodePotentialList.size();
		for(int i=0; i<voltageDependencyList.size(); i++) {
			//if voltDepAndConstPotLinComb[i][i]==false -> source may be presented by linear comb.
			if(!voltDepAndConstPotLinComb[i+voltDepOffs][i+voltDepOffs] && voltageDependencyList.get(i).getConductiveState()) {
				boolean activeInLoop=voltageDependencyList.get(i).isActiveComponent();
				for(int j=0; j<voltageDependencyList.size() && !activeInLoop; j++) {
					if(voltDepAndConstPotLinComb[i+voltDepOffs][j+voltDepOffs]) {
						activeInLoop |= voltageDependencyList.get(j).isActiveComponent();
					}
				}
				if(activeInLoop) {
					shortInstList.add(voltageDependencyList.get(i).getName());
				}
			}
		}
		return shortInstList;
	}/*getInstancesThatCreateShorts*/
	
	/*
	 * methods to remove floating nodes
	 * node is floating if it's potential may not be defined from 
	 * for voltage dependency equations nor charge transfer equations
	 */
	
	/*sets zero potential to floating node*/
	private void setPotentialsToFloatingNodes() {
		extraConstNodePotentialList=new ArrayList<ConstNodePotential>();
		boolean [] floatingNodeFlag=new boolean [nodeNameList.size()];
		Arrays.fill(floatingNodeFlag, true);
		//add all existing const node potentials
		for(int i=0; i<constNodePotentialList.size(); i++) {
			modifyFloatingNodeFlags(constNodePotentialList.get(i).getNodeIndex(), floatingNodeFlag);
		}
		//add extra const node potentials
		for(int i=0; i<nodeNameList.size(); i++) {
			if(floatingNodeFlag[i]==true) {
				extraConstNodePotentialList.add(new ConstNodePotential(i, 0, circuit));
				modifyFloatingNodeFlags(i, floatingNodeFlag);
			}
		}
	} /*SetPotentialsToFloatingNodes*/
	
	private void modifyFloatingNodeFlags(int newConstPotentialNodeIndex, boolean [] floatingNodeFlag){
		int [] sequence=new int [nodeNameList.size()];
		int nextPtr=1, currentPtr=0;
		floatingNodeFlag[newConstPotentialNodeIndex] = false;
		sequence[0]=newConstPotentialNodeIndex;
		int voltDepOffs=constNodePotentialList.size();
		while(currentPtr<nextPtr) {
			newConstPotentialNodeIndex=sequence[currentPtr];

			// propagate via capacitors
			for (int i = 0; i < capacitorList.size(); i++) {
				Capacitor cap = capacitorList.get(i);
				if (cap.getCapacitance() != 0.0) {
					int posNodeInd = cap.getPositiveNodeIndex();
					int negNodeInd = cap.getNegativeNodeIndex();
					if (posNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[negNodeInd]==true) {
						floatingNodeFlag[negNodeInd]=false;
						sequence[nextPtr]=negNodeInd;
						nextPtr++;
					} else {
						if (negNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[posNodeInd]==true) {
							floatingNodeFlag[posNodeInd]=false;
							sequence[nextPtr]=posNodeInd;
							nextPtr++;
						}
					}
				}
			}
			// propagate via VoltageDependency
			for (int i = 0; i < voltageDependencyList.size(); i++) {
				VoltageDependency src = voltageDependencyList.get(i);
				if (voltDepAndConstPotLinComb[i+voltDepOffs][i+voltDepOffs])  {
					int posNodeInd = src.getPositiveConductiveNodeIndex();
					int negNodeInd = src.getNegativeConductiveNodeIndex();
					if (posNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[negNodeInd]==true) {
						floatingNodeFlag[negNodeInd]=false;
						sequence[nextPtr]=negNodeInd;
						nextPtr++;
					} else if (negNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[posNodeInd]==true) {
						floatingNodeFlag[posNodeInd]=false;
						sequence[nextPtr]=posNodeInd;
						nextPtr++;
					}
				}
			}
			currentPtr++;
		}
		
	}/*modifyNotFloatingNodeList*/
	
	/*
	 * methods for charge dependency equations
	 */
	
	/*create matrices for charge transfer equations*/
	private void createMatForChargeDep() {
		capConnMat=new byte[nodeNameList.size()][capacitorList.size()];
		validCapConnMatRow=new boolean [nodeNameList.size()];
		condInstConnMat=new byte [nodeNameList.size()][voltageDependencyList.size()];
	}/*createMatForChargeDep*/
	
	/*init matrices for charge transfer equations*/
	private void initMatForChargeDep() {
		int voltDepOffs=constNodePotentialList.size();
		for(int i=0; i<nodeNameList.size(); i++) {
			Arrays.fill(capConnMat[i], (byte)0);
			validCapConnMatRow[i]=false; 
		}
		for(int i=0; i<capacitorList.size(); i++) {
			Capacitor cap=capacitorList.get(i);
			if(cap.getCapacitance()!=0.0) {
				capConnMat[cap.getPositiveNodeIndex()][i]+=1;
				validCapConnMatRow[cap.getPositiveNodeIndex()]=true;
				capConnMat[cap.getNegativeNodeIndex()][i]+=-1;
				validCapConnMatRow[cap.getNegativeNodeIndex()]=true;
			}
		}
		for(int i=0; i<nodeNameList.size(); i++) {
			Arrays.fill(condInstConnMat[i], (byte)0);
		}
		for(int i=0; i<voltageDependencyList.size(); i++) {
			VoltageDependency src=voltageDependencyList.get(i);
			if(voltDepAndConstPotLinComb[i+voltDepOffs][i+voltDepOffs]) {
				condInstConnMat[src.getPositiveConductiveNodeIndex()][i]=1;
				validCapConnMatRow[src.getPositiveConductiveNodeIndex()]=true;
				condInstConnMat[src.getNegativeConductiveNodeIndex()][i]=-1;
				validCapConnMatRow[src.getNegativeConductiveNodeIndex()]=true;
			}
		}
		for(int i=0; i<constNodePotentialList.size(); i++) {
			validCapConnMatRow[constNodePotentialList.get(i).getNodeIndex()]=false;
		}
		for(int i=0; i<extraConstNodePotentialList.size(); i++) {
			validCapConnMatRow[extraConstNodePotentialList.get(i).getNodeIndex()]=false;
		}
	}/*initMatForChargeDep*/
	
	private void reflectCondInstInCapConnMat() {
		//create copy of condInstConnMat
		int rows=nodeNameList.size();
		
		//modify capConnMat
		for(int scanCol=0; scanCol<voltageDependencyList.size(); scanCol++) {
			//search first non-zero
			int curRow=0;
			while(curRow<rows && !(validCapConnMatRow[curRow] && condInstConnMat[curRow][scanCol]!=0)) curRow++;
			//search second non-zero
			int modRow=curRow+1;
			while(modRow<rows && !(validCapConnMatRow[modRow] && condInstConnMat[modRow][scanCol]!=0)) modRow++;
			if(curRow<rows) {
				if(modRow<rows) {
					//add rows
					for(int n=0; n<voltageDependencyList.size(); n++) {
						condInstConnMat[curRow][n]+=condInstConnMat[modRow][n];
					}
					for(int n=0; n<capacitorList.size(); n++) {
						capConnMat[curRow][n]+=capConnMat[modRow][n];
						capConnMat[modRow][n]=0;
					}
					validCapConnMatRow[modRow]=false;
				}
				else {
					//node, which responds to row is controlled by VoltageDependency
					validCapConnMatRow[curRow]=false;
				}
			}
		}
	}/*ReflectCondInstInCapConnMat*/
	
	/*
	 * methods to fill coefficients inside state
	 */
	
	private void fillCapConMatInState(SwitchedCapCircuitState state) {
		int nodeNum=nodeNameList.size();
		int capNum=capacitorList.size();
		
		for(int i=0; i<nodeNum; i++) {
			if(validCapConnMatRow[i]) {
				for(int j=0; j<capNum; j++) {
					state.setCapConnMatEntry(capConnMat[i][j], i, j);
				}
			}
		}
	}/*fillCapConMatInState*/
	
	private void fillCoeffInStateForChargeTransf(SwitchedCapCircuitState state) {
		int nodeNum=nodeNameList.size();
		int capNum=capacitorList.size();
		for(int i=0; i<nodeNum; i++) {
			if(validCapConnMatRow[i]) {
				state.setChargeTransferEquation(i);
				state.setEquInstInd(i, i);
				int posNodeInd=i;
				for(int capInd=0; capInd<capNum; capInd++) {
					if(capConnMat[posNodeInd][capInd]!=0) {
						Capacitor cap=capacitorList.get(capInd);
						double capVal=cap.getCapacitance();
						state.addToCoefficient(-capVal, i, posNodeInd);
						//search other node of capacitor 
						int negNodeInd=0;
						while(!(negNodeInd!=posNodeInd && capConnMat[negNodeInd][capInd]!=0)) {
							negNodeInd++;
						}
						state.addToCoefficient(-capVal, i, negNodeInd);
					}
				}
				state.setCoefficient(-state.getCoefficient(i, i), i, i);
			}
		}
	}/*fillCoeffInStateForChargeTransf*/
	
	private void fillCoeffInStateForVoltDep(SwitchedCapCircuitState state) {
		int volDepNum=voltageDependencyList.size();
		int voltDepOffs=constNodePotentialList.size();
		int nodeNum=nodeNameList.size();
		int rowSt=0;
		
		for(int i=0; i<volDepNum; i++) {
			if(voltDepAndConstPotLinComb[i+voltDepOffs][i+voltDepOffs]) {
				//search free row in state coeff. mat.
				while(rowSt<nodeNum && !state.isInvalidEquation(rowSt)) rowSt++;
				if(rowSt<nodeNum) {
					state.setVoltageDependencyEquation(rowSt);
					state.setEquInstInd(i, rowSt);
					VoltageDependency src=voltageDependencyList.get(i);
					state.setCoefficientRow(src.getCoefficients(nodeNum), rowSt);
					state.setFreeCoefficient(src.getFreeCoefficient(), rowSt);
				}
				else {
					//in case of mistake in the algorithm
					throw new RuntimeException("row for voltage dependency equation NOT found");
				}
			}
		}
	}/*fillCoeffInStateForVoltDep*/
	
	private void fillCoeffInStateForConstNode(SwitchedCapCircuitState state) {
		int nodeNum=nodeNameList.size();
		int rowSt=0;
		for(int i=0; i<constNodePotentialList.size(); i++) {
			while(rowSt<nodeNum && !state.isInvalidEquation(rowSt)) rowSt++;
			if(rowSt<nodeNum) {
				state.setConstPotentialEquation(rowSt);
				state.setEquInstInd(i, rowSt);
				state.setCoefficient(1.0, rowSt, constNodePotentialList.get(i).getNodeIndex());
				state.setFreeCoefficient(constNodePotentialList.get(i).getNodePotential(), rowSt);
			}
			else {
				throw new RuntimeException("row for const node equation NOT found");
			}
		}
	}
	
	private void fillCoeffInStateForExtraConstNode(SwitchedCapCircuitState state) {
		int nodeNum=nodeNameList.size();
		int rowSt=0;
		for(int i=0; i<extraConstNodePotentialList.size(); i++) {
			while(rowSt<nodeNum && !state.isInvalidEquation(rowSt)) rowSt++;
			if(rowSt<nodeNum) {
				state.setFixFloatingNodeEquation(rowSt);
				state.setEquInstInd(i, rowSt);
				state.setCoefficient(1.0, rowSt, extraConstNodePotentialList.get(i).getNodeIndex());
				state.setFreeCoefficient(extraConstNodePotentialList.get(i).getNodePotential(), rowSt);
			}
			else {
				throw new RuntimeException("row for extra const node equation NOT found");
			}
		}
	}
	
	private void createAuxiliaryMatrices() {
		if(auxiliaryMatricesExist)
			throw new RuntimeException("auxiliar matrices are already created");
		createMatForVoltDep();
		createMatForChargeDep();
		auxiliaryMatricesExist=true;
	}
	
	/*
	 * methods visible in package
	 */

	protected SwitchedCapCircuitState createState() {
		if(auxiliaryMatricesExist==false) {
			createAuxiliaryMatrices();
		}
		//init matrices for voltage dependencies
		//initMatricesForVoltageDependency
		initMatForVoltDep();
		//check short circuits, remove redundant voltage dependencies
		fillVoltDepAndConstPotLinComb();
		ArrayList<String> instCreateShortList=getInstancesThatCreateShorts();
		if(instCreateShortList.size()>0) {
			throw new RuntimeException("instances "+instCreateShortList.toString()+" create short circuits");
		}
		//remove floating nodes
		setPotentialsToFloatingNodes();
		//init matrices for charge dependencies
		initMatForChargeDep();
		//modify matrices for equations with capacitors
		reflectCondInstInCapConnMat();
		//create state
		SwitchedCapCircuitState state=new SwitchedCapCircuitState(nodeNameList.size(), capacitorList.size());
		fillCapConMatInState(state);
		fillCoeffInStateForChargeTransf(state);
		fillCoeffInStateForVoltDep(state);
		fillCoeffInStateForConstNode(state);
		fillCoeffInStateForExtraConstNode(state);
		state.setExtraConstNodePotentialList(extraConstNodePotentialList);
		state.lockState();
		return state;
	}
}
