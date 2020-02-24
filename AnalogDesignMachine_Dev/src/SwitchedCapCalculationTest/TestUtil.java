package SwitchedCapCalculationTest;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import SwitchedCapCalculation.*;
import SwitchedCapComponents.ControlledVoltageSource;
import SwitchedCapComponents.SingleEndedAmplifier;
import SwitchedCapComponents.Switch;

public class TestUtil {
	
	public static boolean approxEqual(double valueA, double valueB, double error) {
		if(error<0)
			error=-error;
		if(valueA+error>valueB && valueA-error<valueB)
			return true;
		return false;
	}
	
	/*print methods*/
	public static void printMatrix(double [][] mat) {
		for(double [] row: mat) {
			System.out.println(Arrays.toString(row));
		}
	}
	
	public static void printMatrix(byte [][] mat) {
		for(byte [] row: mat) {
			System.out.println(Arrays.toString(row));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void printNodesInCircuit(SwitchedCapCircuit circuit) {
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
	public static void printCapacitorsInCircuit(SwitchedCapCircuit circuit) {
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
	public static void printVoltDependInCircuit(SwitchedCapCircuit circuit) {
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
	
	public static ConstNodePotential addConstNodePotential(String nodeName, double potential, SwitchedCapCircuit circuit) {
		ConstNodePotential c=new ConstNodePotential(nodeName, potential);
		circuit.addComponent(c);
		return c;
	}
	
	public static Capacitor addCapacitor(String name, String posNode, String negNode, SwitchedCapCircuit circuit) {
		Capacitor c=new Capacitor(name, posNode, negNode);
		circuit.addComponent(c);
		return c;
	}
	
	public static ControlledVoltageSource addControlledVoltageSource(String name, 
			String posNode, String negNode, SwitchedCapCircuit circuit) {
		ControlledVoltageSource c=new ControlledVoltageSource(name, posNode, negNode);
		circuit.addComponent(c);
		return c;
	}
	
	public static Switch addSwitch(String name, String posNode, String negNode, SwitchedCapCircuit circuit) {
		Switch c=new Switch(name, posNode, negNode);
		circuit.addComponent(c);
		return c;
	}
	
	public static SingleEndedAmplifier addSingleEndedAmplifier(String name, String posOutNode, String negOutNode, 
			String posInNode, String negInNode, SwitchedCapCircuit circuit) {
		SingleEndedAmplifier c=new SingleEndedAmplifier(name, posOutNode, negOutNode, posInNode, negInNode);
		circuit.addComponent(c);
		return c;
	}
}
