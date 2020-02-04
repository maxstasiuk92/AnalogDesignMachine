package SwitchedCapCalculationTest;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import SwitchedCapCalculation.*;

import SwitchedCapCalculation.SwitchedCapCircuit;

public class TestUtil {
	
	static boolean approxEqual(double valueA, double valueB, double error) {
		if(error<0)
			error=-error;
		if(valueA+error>valueB && valueA-error<valueB)
			return true;
		return false;
	}
	
	/*print methods*/
	static void printMatrix(double [][] mat) {
		for(double [] row: mat) {
			System.out.println(Arrays.toString(row));
		}
	}
	
	static void printMatrix(byte [][] mat) {
		for(byte [] row: mat) {
			System.out.println(Arrays.toString(row));
		}
	}
	
	@SuppressWarnings("unchecked")
	static void printNodesInCircuit(SwitchedCapCircuit circuit) {
		ArrayList<String> nodeNameList;
		try {
			Field nodeNameListField=circuit.getClass().getDeclaredField("nodeNameList");
			nodeNameListField.setAccessible(true);
			nodeNameList=(ArrayList<String>)nodeNameListField.get(circuit);
			System.out.println(nodeNameList.toString());
		}
		catch(Exception e) {
			System.out.println("Reflection error: static void PrintNodesInCircuit(SwitchedCapCircuit circuit)");
		}
	}
	
	@SuppressWarnings("unchecked")
	static void printCapacitorsInCircuit(SwitchedCapCircuit circuit) {
		ArrayList<Capacitor> capacitorList;
		String report=new String();
		try {
			Field capacitorListField=circuit.getClass().getDeclaredField("capacitorList");
			capacitorListField.setAccessible(true);
			capacitorList=(ArrayList<Capacitor>)capacitorListField.get(circuit);
			for(int i=0; i<capacitorList.size(); i++) {
				report+=capacitorList.get(i).getName()+" ";
			}
			System.out.println(report);
		}
		catch(Exception e) {
			System.out.println("Reflection error: static void PrintCapacitorsInCircuit(SwitchedCapCircuit circuit)");
		}
	}
	
	@SuppressWarnings("unchecked")
	static void printVoltDependInCircuit(SwitchedCapCircuit circuit) {
		ArrayList<Object> voltDepList;
		
		String report=new String();
		try {
			Field voltDepListField=circuit.getClass().getDeclaredField("voltageDependencyList");
			voltDepListField.setAccessible(true);

			voltDepList=(ArrayList<Object>)voltDepListField.get(circuit);
			for(int i=0; i<voltDepList.size(); i++) {
				Object src=voltDepList.get(i);
				report+=src.getClass().getDeclaredMethod("GetName").invoke(src)+" ";
			}
			System.out.println(report);
		}
		catch(Exception e) {
			System.out.println("Reflection error: static void PrintCapacitorsInCircuit(SwitchedCapCircuit circuit)");
		}
	}
}
