package SwitchedCapCalculationTest;

import java.util.ArrayList;

import SwitchedCapCalculation.SwitchedCapCircuit;

public class TestStateCalculation {
	
	public static boolean testCalculate() {
		boolean correctResult=true;
		TestModels models=new TestModels();
		TestModels.CapToVsrc capToVsrc=models.getCapToVsrc(new SwitchedCapCircuit());
		TestModels.CapBetweenVsrc capBetweenVsrc=models.getCapBetweenVsrc(new SwitchedCapCircuit());
		TestModels.CapBetweenVsrcAndFloating capBetweenVsrcAndFloating=models.getCapBetweenVsrcAndFloating(new SwitchedCapCircuit());
		TestModels.SingEndInt singEndInt=models.getSingEndInt(new SwitchedCapCircuit());
			
		ArrayList<AbstractTestModel> testModelList=new ArrayList<>();
		testModelList.add(capToVsrc);
		testModelList.add(capBetweenVsrc);
		testModelList.add(capBetweenVsrcAndFloating);
		testModelList.add(singEndInt);
		
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
