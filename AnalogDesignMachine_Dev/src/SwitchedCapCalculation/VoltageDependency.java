package SwitchedCapCalculation;

import java.util.ArrayList;
import java.util.Arrays;

/*model: coefficients*nodes=freeCoefficient*/
public class VoltageDependency implements SwitchedCapComponent{
	private SwitchedCapCircuit circuit;
	private String name;
	private Node positiveConductiveNode, negativeConductiveNode;
	private ArrayList<Node> nonConductiveNodeList;
	private boolean conductiveState, activeComponent;
	private double [] coefficients;
	private double freeCoefficient;
	
	/*class to group properties of node*/
	private class Node {
		public String name;
		public int index;
		public double coefficient;
	}
	
	public VoltageDependency(String name,
			String positiveConductiveNodeName, String negativeConductiveNodeName,
			boolean activeComponent) {
		if(null==name || positiveConductiveNodeName==null || negativeConductiveNodeName==null) {
			throw new NullPointerException("parameters should not be null");
		}
		this.name=name;
		this.positiveConductiveNode=new Node();
		this.positiveConductiveNode.name=positiveConductiveNodeName;
		this.positiveConductiveNode.coefficient=1;
		this.negativeConductiveNode=new Node();
		this.negativeConductiveNode.name=negativeConductiveNodeName;
		this.negativeConductiveNode.coefficient=-1;
		this.nonConductiveNodeList=new ArrayList<Node>();
		this.activeComponent=activeComponent;
		this.conductiveState=false;
		this.coefficients=null;
		this.freeCoefficient=0;
		this.circuit=null;
	}
	
	public SwitchedCapCircuit getCircuit() {return circuit;}
	public String getName() {return name;}
	
	public VoltageDependency setNodeCoefficient(String nodeName, double coefficient) {
		if(circuit != null && circuit.isCircuitLocked()) {
			throw new RuntimeException("circuit is locked");
		}
		if(nodeName == null) {
			throw new RuntimeException("nodeName should not be null");
		}
		if(coefficient==0.0) {
			throw new RuntimeException("coefficient should not be 0.0");
		}
		if(nodeName.equals(positiveConductiveNode.name)) {
			positiveConductiveNode.coefficient=coefficient;
		} else if(nodeName.equals(negativeConductiveNode.name)) {
			negativeConductiveNode.coefficient=coefficient;
		} else {
			//search node among non-conductive
			boolean nodePresent=false;
			for(int i=0; i<nonConductiveNodeList.size() && !nodePresent; i++) {
				Node node=nonConductiveNodeList.get(i);
				if(nodeName.equals(node.name)) {
					node.coefficient=coefficient;
					nodePresent=true;
				}
			}
			//add node, which is not present among non-conductive
			if(!nodePresent) {
				Node node=new Node();
				node.name=nodeName;
				node.coefficient=coefficient;
				nonConductiveNodeList.add(node);
			}
		}
		return this;
	}
	
	public void setFreeCoefficient(double freeCoefficient) {this.freeCoefficient=freeCoefficient;}
	
	public boolean isActiveComponent() {return activeComponent;}
	public boolean getConductiveState() {return conductiveState;}
	public void setConductiveState(boolean conductiveState) {this.conductiveState=conductiveState;}
	
	/*interaction inside package. No check, that component added to circuit*/
	protected double [] getCoefficients(int cols) {
		//create coefficients if needed
		if(coefficients==null) {
			coefficients=new double[cols]; //default values are 0.0
			coefficients[positiveConductiveNode.index]=positiveConductiveNode.coefficient;
			coefficients[negativeConductiveNode.index]=negativeConductiveNode.coefficient;
			for(int i=0; i<nonConductiveNodeList.size(); i++) {
				Node node=nonConductiveNodeList.get(i);
				coefficients[node.index]=node.coefficient;
			}
		} else if(cols!=coefficients.length) {
			throw new RuntimeException("number of columns is different, than before");
		}
		return Arrays.copyOf(coefficients, cols);
	}
	
	protected double getFreeCoefficient() {
		return freeCoefficient;
	}
	
	protected int getPositiveConductiveNodeIndex() {
		return positiveConductiveNode.index;
	}
	protected int getNegativeConductiveNodeIndex() {
		return negativeConductiveNode.index;
	}
	
	/**add component to circuit */
	public void addToCircuit(SwitchedCapCircuit circuit) {
		if(this.circuit != null) {
			throw new RuntimeException("voltage dependenucy "+name+" is already added to some circuit");
		}
		/*circuit lock is checked inside SwitchedCapCircuit*/
		this.circuit=circuit;
		positiveConductiveNode.index=this.circuit.addNode(positiveConductiveNode.name);
		negativeConductiveNode.index=this.circuit.addNode(negativeConductiveNode.name);
		for(int i=0; i<nonConductiveNodeList.size(); i++) {
			Node node=nonConductiveNodeList.get(i);
			node.index=this.circuit.addNode(node.name);
		}
		this.circuit.addVoltageDependency(this);
	}
	
}
