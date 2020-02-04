package SwitchedCapCalculationTest;
import java.lang.reflect.*;
import java.util.ArrayList;

import SwitchedCapCalculation.ConstNodePotential;
import SwitchedCapCalculation.SwitchedCapCircuit;
import SwitchedCapCalculation.SwitchedCapCircuitStateCreator;


public class TestStateCreation {
	@SuppressWarnings("unchecked")
	public static boolean testGetInstancesThatCreateShorts() {
		boolean correctResult=true;
		boolean resultsRetrieved=true;
		TestModels testModels=new TestModels();
		SwitchedCapCircuit circuit;
		ArrayList<String> scInstList=null, redundInstList=new ArrayList<String>();
		Field fieldRedundantVoltDepFlag, fieldVoltageDependencyList;
		boolean [] redundantVoltDepFlag;
		ArrayList<Object> voltageDependencyList;
				
		TestModels.ShortCircuits model=testModels.getShortCircuits(new SwitchedCapCircuit());
		circuit=model.getSwitchedCapCircuit();
		try {
			Field fieldStateCreator;
			fieldStateCreator=circuit.getClass().getDeclaredField("stateCreator");
			fieldStateCreator.setAccessible(true);
			//init mat for volt. dep.
			Method method;
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("createMatForVoltDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("initMatForVoltDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			//create lin. comb
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("fillVoltDepAndConstPotLinComb");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			//get shorts			
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("getInstancesThatCreateShorts");
			method.setAccessible(true);
			scInstList=(ArrayList<String>)method.invoke(fieldStateCreator.get(circuit));
			//define redundant
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("markRedundantVoltageDependecies");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			//get redundantVoltDepFlag
			fieldRedundantVoltDepFlag=fieldStateCreator.get(circuit).getClass().getDeclaredField("redundantVoltDepFlag");
			fieldRedundantVoltDepFlag.setAccessible(true);
			redundantVoltDepFlag=(boolean [])fieldRedundantVoltDepFlag.get(fieldStateCreator.get(circuit));
			//get voltageDependencyList
			fieldVoltageDependencyList=circuit.getClass().getDeclaredField("voltageDependencyList");
			fieldVoltageDependencyList.setAccessible(true);
			voltageDependencyList=(ArrayList<Object>)fieldVoltageDependencyList.get(circuit);
			//get names of redundant inst
			for(int i=0; i<redundantVoltDepFlag.length; i++) {
				if(redundantVoltDepFlag[i]) {
					redundInstList.add((String)voltageDependencyList.get(i).getClass().getDeclaredMethod("GetName")
					.invoke(voltageDependencyList.get(i)));
				}
			}
		}
		catch(Exception e) {
			System.out.println("Reflection error: TestGetShortCircuits()");
			e.printStackTrace();
			resultsRetrieved=false;
		}
		
		if(resultsRetrieved) {
			correctResult &= model.allShortsDetected(scInstList);
			correctResult &= model.allRedundantDetected(redundInstList);
		}
		
		if(!correctResult) {
			System.out.println("Fail: getInstancesThatCreateShorts()");
		}
		return correctResult;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean testSetPotentialsToFloatingNodes() {
		boolean correctResult=true;
		boolean resultsRetrieved=true;
		SwitchedCapCircuit circuit=null;
		Field fieldStateCreator, fieldExtraConstNodePotentialList;
		Method method;
		
		TestModels testModels=new TestModels();
		ArrayList<ConstNodePotential> extraConstNode=null;
		
		TestModels.FloatingNodes model=testModels.getFloatingNodes(new SwitchedCapCircuit());
		circuit=model.getSwitchedCapCircuit();
		try {
			fieldStateCreator=circuit.getClass().getDeclaredField("stateCreator");
			fieldStateCreator.setAccessible(true);
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("createMatForVoltDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("initMatForVoltDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("setPotentialsToFloatingNodes");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
					
			fieldExtraConstNodePotentialList=fieldStateCreator.get(circuit).getClass().getDeclaredField("extraConstNodePotentialList");
			fieldExtraConstNodePotentialList.setAccessible(true);
			extraConstNode=(ArrayList<ConstNodePotential>)fieldExtraConstNodePotentialList.get(fieldStateCreator.get(circuit));	
		}
		catch(Exception e) {
			System.out.println("Reflection error: TestGetExtraConstNodePotentialList()");
			e.printStackTrace();
			resultsRetrieved=false;
		}
		
		if(resultsRetrieved) {
			correctResult=model.allFloatingDetected(extraConstNode);
		}
		
		if(!correctResult) {
			System.out.println("Fail: TestGetExtraConstNodePotentialList()");
		}
		
		return correctResult;
	}
	
	public static boolean testReflectCondInstInCapConnMat() {
		boolean correctResult=true;
		boolean resultsRetrieved=true;
		SwitchedCapCircuit circuit=null;
		Field fieldStateCreator;
		Method method;
		Field fieldCapConnMat, fieldValidCapConnMatRow;
		
		TestModels testModels=new TestModels();
			
		byte [][] capConnMat=null;
		boolean [] validCapConnMatRow=null;
		
		TestModels.ChargeCondInstBetweenCaps model=testModels.getChargeCondInstBetweenCaps(new SwitchedCapCircuit());
		circuit=model.getSwitchedCapCircuit();
		try {
			fieldStateCreator=circuit.getClass().getDeclaredField("stateCreator");
			fieldStateCreator.setAccessible(true);
			//all for volt. dep.
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("createMatForVoltDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("initMatForVoltDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("fillVoltDepAndConstPotLinComb");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("markRedundantVoltageDependecies");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			//getInstancesThatCreateShorts is not needed
			//remove floating nodes
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("setPotentialsToFloatingNodes");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			
			//all for charge dep.
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("initMatForChargeDep");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			method=fieldStateCreator.get(circuit).getClass().getDeclaredMethod("reflectCondInstInCapConnMat");
			method.setAccessible(true);
			method.invoke(fieldStateCreator.get(circuit));
			
			//get matrices
			fieldCapConnMat=fieldStateCreator.get(circuit).getClass().getDeclaredField("capConnMat");
			fieldCapConnMat.setAccessible(true);
			fieldValidCapConnMatRow=fieldStateCreator.get(circuit).getClass().getDeclaredField("validCapConnMatRow");
			fieldValidCapConnMatRow.setAccessible(true);
			
			capConnMat=(byte[][])fieldCapConnMat.get(fieldStateCreator.get(circuit));
			validCapConnMatRow=(boolean[])fieldValidCapConnMatRow.get(fieldStateCreator.get(circuit));
		}
		catch(Exception e) {
			System.out.println("Reflection error: TestreflectCondInstInCapConnMat()");
			e.printStackTrace();
			resultsRetrieved=false;
		}
		
		if(resultsRetrieved) {
			correctResult=model.correctCapConnMat(capConnMat, validCapConnMatRow);
		}
				
		if(!correctResult) {
			System.out.println("Fail: TestreflectCondInstInCapConnMat()");
		}
		
		return correctResult;
	}
	
	/**check, that states are created without exceptions*/
	public static boolean testCreateState() {
		boolean correctResult=true;
		TestModels models=new TestModels();
		TestModels.CapToVsrc capToVsrc=models.getCapToVsrc(new SwitchedCapCircuit());
		TestModels.CapBetweenVsrc capBetweenVsrc=models.getCapBetweenVsrc(new SwitchedCapCircuit());
		TestModels.CapBetweenVsrcAndFloating capBetweenVsrcAndFloating=models.getCapBetweenVsrcAndFloating(new SwitchedCapCircuit());
		TestModels.FloatingNodes floatingNodes= models.getFloatingNodes(new SwitchedCapCircuit());
		TestModels.ChargeCondInstBetweenCaps chargeCondInstBetweenCaps=models.getChargeCondInstBetweenCaps(new SwitchedCapCircuit());
		
		ArrayList<AbstractTestModel> testModelList=new ArrayList<>();
		testModelList.add(capToVsrc);
		testModelList.add(capBetweenVsrc);
		testModelList.add(capBetweenVsrcAndFloating);
		testModelList.add(floatingNodes);
		testModelList.add(chargeCondInstBetweenCaps);
		
		for(int i=0; i<testModelList.size() && correctResult; i++) {
			AbstractTestModel testModel=testModelList.get(i);
			int numOfStates=testModel.getStateNumber();
			for(int j=0; j<numOfStates; j++) {
				testModel.setState(j);
				SwitchedCapCircuit circuit=testModel.getSwitchedCapCircuit();
				try {
					Field fieldStateCreator=circuit.getClass().getDeclaredField("stateCreator");
					fieldStateCreator.setAccessible(true);
					SwitchedCapCircuitStateCreator stateCreator=(SwitchedCapCircuitStateCreator)fieldStateCreator.get(circuit);
					Method methodCreateState=stateCreator.getClass().getDeclaredMethod("createState");
					methodCreateState.setAccessible(true);
					methodCreateState.invoke(stateCreator);
				}catch(Exception e) {
					System.out.println("Fail: TestCreateState(); model: "+testModel.getClass().getName().toString()+"; state: "+j);
					correctResult=false;
					//e.printStackTrace();
				}
			}
		}
		return correctResult;
	}
	
}
