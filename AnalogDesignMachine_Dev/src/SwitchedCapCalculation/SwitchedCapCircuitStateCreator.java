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
	private boolean [] redundantVoltDepFlag;
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
		redundantVoltDepFlag=new boolean [voltageDependencyList.size()];
		voltDepAndConstPotMat=new byte [constNodePotentialList.size()+voltageDependencyList.size()][nodeNameList.size()];
		voltDepAndConstPotLinComb=new boolean [constNodePotentialList.size()+voltageDependencyList.size()][constNodePotentialList.size()+voltageDependencyList.size()];
	}/*createMatForVoltDep*/
	
	/*init matrices for voltage dependency equations*/
	private void initMatForVoltDep() {
		int rows;
		Arrays.fill(redundantVoltDepFlag, false);
		
		rows=voltDepAndConstPotMat.length;
		for(int i=0; i<rows; i++)
			Arrays.fill(voltDepAndConstPotMat[i], (byte)0);
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
						for(int modCol=scanCol; modCol<nodeNum; modCol++) {
							voltDepAndConstPotMat[modRow][modCol]-=voltDepAndConstPotMat[modRow][scanCol]/voltDepAndConstPotMat[curRow][scanCol]*voltDepAndConstPotMat[curRow][modCol];
							//normalize to get +/-1
							if(voltDepAndConstPotMat[modRow][modCol]!=0) {
								if(voltDepAndConstPotMat[modRow][modCol]<0)
									voltDepAndConstPotMat[modRow][modCol]=-1;
								else
									voltDepAndConstPotMat[modRow][modCol]=1;
							}
						}
						for(int modCol=voltDepOffs; modCol<srcNum; modCol++)
							voltDepAndConstPotLinComb[modRow][modCol] |= voltDepAndConstPotLinComb[curRow][modCol];
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
			if(voltDepAndConstPotLinComb[i+voltDepOffs][i+voltDepOffs]==false) {
				boolean activeInLoop=voltageDependencyList.get(i).isActiveComponent();
				for(int j=0; j<voltageDependencyList.size() && !activeInLoop; j++) {
					if(voltDepAndConstPotLinComb[i+voltDepOffs][j+voltDepOffs])
						activeInLoop |= voltageDependencyList.get(j).isActiveComponent();
				}
				if(activeInLoop) {
					shortInstList.add(voltageDependencyList.get(i).GetName());
				}
			}
		}
		return shortInstList;
	}/*getInstancesThatCreateShorts*/
	
	/* 
	 * mark sources/switches, which may be presented by lin. comb. of other sources
	 * sources/switches, which create shorts are already reported
	 */
	private void markRedundantVoltageDependecies() {
		for(int i=0, j=constNodePotentialList.size(); i<voltageDependencyList.size(); i++, j++) {
			redundantVoltDepFlag[i]=!voltDepAndConstPotLinComb[j][j];
		}
	}
	
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
		int [] indexSeq=new int [nodeNameList.size()];
		int indexSeqSize=1, indexSeqPtr=0;
		floatingNodeFlag[newConstPotentialNodeIndex] = false;
		indexSeq[0]=newConstPotentialNodeIndex;

		while(indexSeqSize!=0) {
			newConstPotentialNodeIndex=indexSeq[indexSeqPtr];

			// propagate via capacitors
			for (int i = 0; i < capacitorList.size(); i++) {
				Capacitor cap = capacitorList.get(i);
				if (cap.getCapacitance() != 0.0) {
					int posNodeInd = cap.getPositiveNodeIndex();
					int negNodeInd = cap.getNegativeNodeIndex();
					if (posNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[negNodeInd]==true) {
						floatingNodeFlag[negNodeInd]=false;
						indexSeq[indexSeqSize]=negNodeInd;
						indexSeqSize++;
					} else {
						if (negNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[posNodeInd]==true) {
							floatingNodeFlag[posNodeInd]=false;
							indexSeq[indexSeqSize]=posNodeInd;
							indexSeqSize++;
						}
					}
				}
			}
			// propagate via VoltageDependency
			for (int i = 0; i < voltageDependencyList.size(); i++) {
				VoltageDependency src = voltageDependencyList.get(i);
				if (!redundantVoltDepFlag[i] && src.getConductiveState()) {
					int posNodeInd = src.getPositiveConductiveNodeIndex();
					int negNodeInd = src.getNegativeConductiveNodeIndex();
					if (posNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[negNodeInd]==true) {
						floatingNodeFlag[negNodeInd]=false;
						indexSeq[indexSeqSize]=negNodeInd;
						indexSeqSize++;
					} else {
						if (negNodeInd == newConstPotentialNodeIndex && floatingNodeFlag[posNodeInd]==true) {
							floatingNodeFlag[posNodeInd]=false;
							indexSeq[indexSeqSize]=posNodeInd;
							indexSeqSize++;
						}
					}
				}
			}
			indexSeqSize--;
			indexSeqPtr++;
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
		for(int i=0; i<nodeNameList.size(); i++) {
			Arrays.fill(capConnMat[i], (byte)0);
			validCapConnMatRow[i]=false;
		}
		for(int i=0; i<capacitorList.size(); i++) {
			Capacitor cap=capacitorList.get(i);
			if(cap.getCapacitance()!=0.0) {
				capConnMat[cap.getPositiveNodeIndex()][i]=1;
				validCapConnMatRow[cap.getPositiveNodeIndex()]=true;
				capConnMat[cap.getNegativeNodeIndex()][i]=-1;
				validCapConnMatRow[cap.getNegativeNodeIndex()]=true;
			}
		}
		for(int i=0; i<constNodePotentialList.size(); i++) {
			validCapConnMatRow[constNodePotentialList.get(i).getNodeIndex()]=false;
		}
		for(int i=0; i<extraConstNodePotentialList.size(); i++) {
			validCapConnMatRow[extraConstNodePotentialList.get(i).getNodeIndex()]=false;
		}
		
		for(int i=0; i<nodeNameList.size(); i++)
			Arrays.fill(condInstConnMat[i], (byte)0);
		for(int i=0; i<voltageDependencyList.size(); i++) {
			VoltageDependency src=voltageDependencyList.get(i);
			if(!redundantVoltDepFlag[i] && src.getConductiveState()) {
				condInstConnMat[src.getPositiveConductiveNodeIndex()][i]=1;
				condInstConnMat[src.getNegativeConductiveNodeIndex()][i]=-1;
			}
		}
	}/*initMatForChargeDep*/
	
	//TODO: from here
	/*
	 * 
	 */
	private void reflectCondInstInCapConnMat() {
		//create copy of condInstConnMat
		int rows=nodeNameList.size();
		int cols=voltageDependencyList.size();
		
		//modify capConnMat
		for(int scanCol=0; scanCol<cols; scanCol++) {
			//search first non-zero
			int curRow=0;
			while(curRow<rows && !(validCapConnMatRow[curRow] && condInstConnMat[curRow][scanCol]!=0.0)) curRow++;
			//search second non-zero
			int modRow=curRow+1;
			while(modRow<rows && !(validCapConnMatRow[modRow] && condInstConnMat[modRow][scanCol]!=0.0)) modRow++;
			if(curRow<rows) {
				if(modRow<rows) {
					//add rows
					for(int n=0; n<cols; n++)
						condInstConnMat[curRow][n]+=condInstConnMat[modRow][n];
					for(int n=0; n<capacitorList.size(); n++)
						capConnMat[curRow][n]+=capConnMat[modRow][n];
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
	 * methods to create state
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
				state.setEquType(SwitchedCapCircuitState.equationType.chargeTransferEquation, i);
				state.setEquInstInd(i, i);
				for(int j=0; j<capNum; j++) {
					if(capConnMat[i][j]!=0) {
						Capacitor cap=capacitorList.get(j);
						double capVal=cap.getCapacitance();
						int posNodeInd=cap.getPositiveNodeIndex();
						int negNodeInd=cap.getNegativeNodeIndex();
						if(i==posNodeInd) {
							state.addToCoefficient(capVal, i, posNodeInd);
							state.addToCoefficient(-capVal, i, negNodeInd);
						}
						else {
							state.addToCoefficient(-capVal, i, posNodeInd);
							state.addToCoefficient(capVal, i, negNodeInd);
						}
					}
				}
			}
		}
	}/*fillCoeffInStateForChargeTransf*/
	
	private void fillCoeffInStateForVoltDep(SwitchedCapCircuitState state) {
		int volDepNum=voltageDependencyList.size();
		int nodeNum=nodeNameList.size();
		int rowSt=0;
		
		for(int i=0; i<volDepNum; i++) {
			if(!redundantVoltDepFlag[i]) {
				//search free row in state coeff. mat.
				while(rowSt<nodeNum && state.getEquType(rowSt)!=SwitchedCapCircuitState.equationType.invalidEquation) rowSt++;
				if(rowSt<nodeNum) {
					state.setEquType(SwitchedCapCircuitState.equationType.voltageDependencyEquation, rowSt);
					state.setEquInstInd(i, rowSt);
					double [] voltDepEqu=voltageDependencyList.get(i).getCoefficients(nodeNum);
					state.setCoefficientRow(voltDepEqu, rowSt);
				}
				else {
					throw new RuntimeException("row for voltage dependency equation NOT found");
				}
			}
		}
	}/*fillCoeffInStateForVoltDep*/
	
	private void fillCoeffInStateForConstNode(SwitchedCapCircuitState state, boolean extraConstNodePotential) {
		int nodeNum=nodeNameList.size();
		List<ConstNodePotential> constNodeList=constNodePotentialList;
		SwitchedCapCircuitState.equationType equTypeVal=SwitchedCapCircuitState.equationType.constPotentialEquation;
		if(extraConstNodePotential) {
			equTypeVal=SwitchedCapCircuitState.equationType.fixFloatingNodeEquation;
			constNodeList=extraConstNodePotentialList;
		}
		int constNodeNum=constNodeList.size();
		int rowSt=0;
		for(int i=0; i<constNodeNum; i++) {
			while(rowSt<nodeNum && state.getEquType(rowSt)!=SwitchedCapCircuitState.equationType.invalidEquation) rowSt++;
			if(rowSt<nodeNum) {
				state.setEquType(equTypeVal, rowSt);
				state.setEquInstInd(i, rowSt);
				state.setCoefficient(1.0, rowSt, constNodeList.get(i).getNodeIndex());
				state.setFreeCoefficient(constNodeList.get(i).getNodePotential(), rowSt);
			}
			else {
				throw new RuntimeException("row for const node equation NOT found");
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
		initMatForVoltDep();
		//check short circuits, remove redundant voltage dependencies
		fillVoltDepAndConstPotLinComb();
		ArrayList<String> instCreateShortList=getInstancesThatCreateShorts();
		if(instCreateShortList.size()>0) {
			throw new RuntimeException("instances "+instCreateShortList.toString()+" create short circuits");
		}
		markRedundantVoltageDependecies();
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
		fillCoeffInStateForConstNode(state, false);
		fillCoeffInStateForConstNode(state, true);
		state.setExtraConstNodePotentialList(extraConstNodePotentialList);
		state.lockState();
		return state;
	}
}
