package SwitchedCapCalculationTest;

public class TestLauncher {
	
	//TODO:
	//capacitor with pins, connected to the same net
	public static void main(String[] args) {
		boolean testPassed=true;
		System.out.println("Start tests");
		
		testPassed &= LaunchSelfTest();
		testPassed &= TestStateCreation.testGetInstancesThatCreateShorts();
		testPassed &= TestStateCreation.testSetPotentialsToFloatingNodes();
		//testPassed &= TestStateCreation.TestReflectCondInstInCapConnMat();
		testPassed &= TestStateCreation.testCreateState();
		testPassed &= TestStateCalculation.testCalculate();
		
		if(testPassed)
			System.out.println("Passed tests");
		else
			System.out.println("Failed tests");
		
	}
	
	public static boolean LaunchSelfTest() {
		boolean testPassed=true;
				
		if(!testPassed) {
			System.out.println("Failed selftest");
		}
		return testPassed;
	}
}


