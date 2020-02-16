package SwitchedCapCalculationTest;

import java.util.ArrayList;

import SwitchedCapCalculation.SwitchedCapCircuit;
import SwitchedCapCalculation.SwitchedCapCircuitCreator;

public class TestStateCalculation {
	
	public static boolean testCalculate() {
		boolean correctResult=true;
		TestModels models=new TestModels();
		TestModels.CapToVsrc capToVsrc=models.getCapToVsrc(SwitchedCapCircuitCreator.createSwitchedCapCircuit());
		TestModels.CapBetweenVsrc capBetweenVsrc=models.getCapBetweenVsrc(SwitchedCapCircuitCreator.createSwitchedCapCircuit());
		TestModels.CapBetweenVsrcAndFloating capBetweenVsrcAndFloating=models.getCapBetweenVsrcAndFloating(SwitchedCapCircuitCreator.createSwitchedCapCircuit());
		TestModels.SingEndInt singEndInt=models.getSingEndInt(SwitchedCapCircuitCreator.createSwitchedCapCircuit());
		TestModels.DiffSwAmp diffSwAmp=models.getDiffSwAmp(SwitchedCapCircuitCreator.createSwitchedCapCircuit());
			
		ArrayList<AbstractTestModel> testModelList=new ArrayList<>();
		testModelList.add(capToVsrc);
		testModelList.add(capBetweenVsrc);
		testModelList.add(capBetweenVsrcAndFloating);
		testModelList.add(singEndInt);
		testModelList.add(diffSwAmp);
		
		for(int i=0; i<testModelList.size(); i++) {
			AbstractTestModel testModel=testModelList.get(i);
			if(!testModel.checkCalculation()) {
				System.out.println("Fail TestCalculate, model "+testModel.getClass().getName());
				correctResult=false;
			}
		}
		return correctResult;
	}
	
}
