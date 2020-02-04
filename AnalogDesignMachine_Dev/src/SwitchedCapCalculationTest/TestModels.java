package SwitchedCapCalculationTest;

import SwitchedCapCalculation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public class TestModels {
	public static final double nodePotentialAccuracy=1e-3;
	
	public CapToVsrc getCapToVsrc(SwitchedCapCircuit circuit) {return new CapToVsrc(circuit, nodePotentialAccuracy);}
	public CapBetweenVsrc getCapBetweenVsrc(SwitchedCapCircuit circuit) {return new CapBetweenVsrc(circuit, nodePotentialAccuracy);}
	public CapBetweenVsrcAndFloating getCapBetweenVsrcAndFloating(SwitchedCapCircuit circuit) {return new CapBetweenVsrcAndFloating(circuit, nodePotentialAccuracy);}
	public CapBetweenVsrcAndShortVsrc getCapBetweenVsrcAndShortVsrc(SwitchedCapCircuit circuit) {return new CapBetweenVsrcAndShortVsrc(circuit, nodePotentialAccuracy);}
	public ShortCircuits getShortCircuits(SwitchedCapCircuit circuit) {return new ShortCircuits(circuit, nodePotentialAccuracy);}
	public FloatingNodes getFloatingNodes(SwitchedCapCircuit circuit) {return new FloatingNodes(circuit, nodePotentialAccuracy);}
	public ChargeCondInstBetweenCaps getChargeCondInstBetweenCaps(SwitchedCapCircuit circuit) {return new ChargeCondInstBetweenCaps(circuit, nodePotentialAccuracy);}
	public SingEndInt getSingEndInt(SwitchedCapCircuit circuit) {return new SingEndInt(circuit, nodePotentialAccuracy);}
	
	//C1=1 in paralel with V1=[1, 2, 0]
	public class CapToVsrc extends AbstractTestModel {
		public final int ST_V1set1=0, ST_V1set2=1, ST_V1set0=2;
		private ControlledVoltageSource inst_V1;
		
		public CapToVsrc(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			this.nodePotentialAccuracy=nodePotentialAccuracy;
			this.circuit=circuit;
			circuit.addConstNodePotential("gnd", 0);
			circuit.addCapacitor("C1", "net1", "gnd").setCapacitance(1);
			inst_V1=circuit.addControlledVoltageSource("V1", "net1", "gnd").setVoltage(1);
			circuit.lockCircuit();
			
			nodeProbes=new ArrayList<NodePotentialProbe>(2);
			nodeProbes.add(circuit.getNodePotentialProbe("net1"));
			nodeProbes.add(circuit.getNodePotentialProbe("gnd"));
			
			correctPotentials=new HashMap<String, Double>(2);
			correctPotentials.put("net1", 0.0);
			correctPotentials.put("gnd", 0.0);
		}
		
		public int getStateNumber() {
			return 3;
		}
		
		public void setState(int state) {
			switch(state)
			{
			case ST_V1set1: 
				inst_V1.setVoltage(1);
				correctPotentials.replace("net1", (double)1);
				break;
			case ST_V1set2:
				inst_V1.setVoltage(2);
				correctPotentials.replace("net1", (double)2);
				break;
			case ST_V1set0:
				inst_V1.setVoltage(0);
				correctPotentials.replace("net1", (double)0);
				break;
			default: throw new RuntimeException("undefined state");
					
			}
		}
	} //CapToVsrc
	
	//C1=1 switches between V1=1(via SW1) and V2=2 (via SW2)
	public class CapBetweenVsrc extends AbstractTestModel {
		public final int ST_C1toV1=0, ST_C1toV2=1; 
		private Switch inst_SW1, inst_SW2;
		
		public CapBetweenVsrc(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			this.nodePotentialAccuracy=nodePotentialAccuracy;
			this.circuit=circuit;
			circuit.addConstNodePotential("gnd", 0);
			circuit.addCapacitor("C1", "net3", "gnd").setCapacitance(1);
			circuit.addControlledVoltageSource("V1", "net1", "gnd").setVoltage(1);
			circuit.addControlledVoltageSource("V2", "net2", "gnd").setVoltage(2);
			inst_SW1=circuit.addSwitch("SW1", "net1", "net3");
			inst_SW2=circuit.addSwitch("SW2", "net3", "net2");
			circuit.lockCircuit();
			
			nodeProbes=new ArrayList<NodePotentialProbe>(4);
			nodeProbes.add(circuit.getNodePotentialProbe("net1"));
			nodeProbes.add(circuit.getNodePotentialProbe("net2"));
			nodeProbes.add(circuit.getNodePotentialProbe("net3"));
			nodeProbes.add(circuit.getNodePotentialProbe("gnd"));
			
			correctPotentials=new HashMap<String, Double>(4);
			correctPotentials.put("net1", 0.0);
			correctPotentials.put("net2", 0.0);
			correctPotentials.put("net3", 0.0);
			correctPotentials.put("gnd", 0.0);
		}
		
		public int getStateNumber() {
			return 2;
		}
		
		public void setState(int state) {
			switch(state)
			{
			case ST_C1toV1:
				inst_SW1.setConductiveState(true);
				inst_SW2.setConductiveState(false);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 1.0);
				break;
			case ST_C1toV2:
				inst_SW1.setConductiveState(false);
				inst_SW2.setConductiveState(true);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 2.0);
				break;
			default: throw new RuntimeException("undefined state");
			}
		}
	} //CapBetweenVsrc
	
	//C1=1 switches between V1=1(via SW1) and V2=2 (via SW2)
	//state=ST_C1floating - both switches are open
	public class CapBetweenVsrcAndFloating extends CapBetweenVsrc {
		public final int ST_C1toV1=0, ST_C1toV2=1, ST_C1floating=2;
		public CapBetweenVsrcAndFloating(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			super(circuit, nodePotentialAccuracy);
		}
		
		public int getStateNumber() {
			return 3;
		}
		
		public void setState(int state) {
			switch(state)
			{
			case ST_C1toV1:
				super.inst_SW1.setConductiveState(true);
				super.inst_SW2.setConductiveState(false);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 1.0);
				break;
			case ST_C1toV2:
				super.inst_SW1.setConductiveState(false);
				super.inst_SW2.setConductiveState(true);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 2.0);
				break;
			case ST_C1floating:
				super.inst_SW1.setConductiveState(false);
				super.inst_SW2.setConductiveState(false);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 2.0);
				break;
			default: throw new RuntimeException("undefined state");
			}
		}
		
	} //CapBetweenVsrcAndFloating
	
	//C1=1 switches between V1=1(via SW1) and V2=2 (via SW2)
	//state=ST_V1shortV2 - both switches are open
	public class CapBetweenVsrcAndShortVsrc extends CapBetweenVsrc{
		public final int ST_C1toV1=0, ST_C1toV2=1, ST_V1shortV2=2;
		public CapBetweenVsrcAndShortVsrc(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			super(circuit, nodePotentialAccuracy);
		}
		
		public int getStateNumber() {
			return 3;
		}
		
		public void setState(int state) {
			switch(state)
			{
			case ST_C1toV1:
				super.inst_SW1.setConductiveState(true);
				super.inst_SW2.setConductiveState(false);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 1.0);
				break;
			case ST_C1toV2:
				super.inst_SW1.setConductiveState(false);
				super.inst_SW2.setConductiveState(true);
				correctPotentials.replace("net1", 1.0);
				correctPotentials.replace("net2", 2.0);
				correctPotentials.replace("net3", 2.0);
				break;
			case ST_V1shortV2:
				super.inst_SW1.setConductiveState(true);
				super.inst_SW2.setConductiveState(true);
				/*should fail*/
				break;
			default: throw new RuntimeException("undefined state");
			}
		}
		
	} //CapBetweenVsrcAndShortVsrc
	
	//many shorted and not shorted sources
	public class ShortCircuits extends AbstractTestModel{
		
		public ShortCircuits(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			this.circuit=circuit;
			this.nodePotentialAccuracy=nodePotentialAccuracy;
			
			circuit.addConstNodePotential("gnd", 0);
			//V1 shorted by SW1
			circuit.addControlledVoltageSource("V1", "net1", "gnd").setVoltage(1.0);
			circuit.addSwitch("SW1", "net1", "gnd").setConductiveState(true);
			//V2 not shorted by open SW2
			circuit.addControlledVoltageSource("V2", "net2", "gnd").setVoltage(1.0);
			circuit.addSwitch("SW2", "net2", "gnd").setConductiveState(false);
			//SW3.1 shorted with SW3.2
			circuit.addSwitch("SW3.1", "net3", "gnd").setConductiveState(true);
			circuit.addSwitch("SW3.2", "net3", "gnd").setConductiveState(true);
			//Serial conn. of V4.1, V4.2, V4.3 with parallel open SW4
			circuit.addControlledVoltageSource("V4.1", "net4.1", "gnd").setVoltage(1.0);
			circuit.addControlledVoltageSource("V4.2", "net4.2", "net4.1").setVoltage(1.0);
			circuit.addControlledVoltageSource("V4.3", "net4.3", "net4.2").setVoltage(1.0);
			circuit.addSwitch("SW4", "net4.3", "gnd").setConductiveState(false);
			//Serial conn. of V5.1, V5.2, V5.3 with parallel short SW5
			circuit.addControlledVoltageSource("V5.1", "net5.1", "gnd").setVoltage(1.0);
			circuit.addControlledVoltageSource("V5.2", "net5.2", "net5.1").setVoltage(1.0);
			circuit.addControlledVoltageSource("V5.3", "net5.3", "net5.2").setVoltage(1.0);
			circuit.addSwitch("SW5", "net5.3", "gnd").setConductiveState(true);
			//V6 connected to "gnd"
			circuit.addControlledVoltageSource("V6", "gnd", "gnd").setVoltage(1.0);
			//closed SW7 connected to "gnd"
			circuit.addSwitch("SW7", "gnd", "gnd").setConductiveState(true);
			//Serial conn. of V8.1, V8.2, V8.3 C8 with parallel short SW8
			circuit.addControlledVoltageSource("V8.1", "net8.1", "gnd").setVoltage(1.0);
			circuit.addControlledVoltageSource("V8.2", "net8.2", "net8.1").setVoltage(1.0);
			circuit.addControlledVoltageSource("V8.3", "net8.3", "net8.2").setVoltage(1.0);
			circuit.addCapacitor("C8", "net8.4", "net8.3").setCapacitance(1);
			circuit.addSwitch("SW8", "net8.4", "gnd").setConductiveState(true);
			//Single ended amp. A9 in "buffer connection"
			circuit.addSingleEndedAmplifier("A9", "net9", "gnd", "gnd", "net9").setOpenLoopGain(100);
			circuit.lockCircuit();
		}
		
		public int getStateNumber() {return 0;}
		
		//should not be used
		public void setState(int state) {
			throw new RuntimeException("undefined state");
		}
		
		public boolean allShortsDetected(ArrayList<String> shortedList) {
			boolean correctResult=true;
			int foundShortsNum;
			//V1 shorted by SW1
			foundShortsNum=0;
			if(shortedList.indexOf("V1")!=-1) foundShortsNum++;
			if(shortedList.indexOf("SW1")!=-1) foundShortsNum++;
			if(foundShortsNum!=1) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): V1 shorted by SW1");
			}
			//V2 not shorted by open SW2
			foundShortsNum=0;
			if(shortedList.indexOf("V2")!=-1) foundShortsNum++;
			if(shortedList.indexOf("SW2")!=-1) foundShortsNum++;
			if(foundShortsNum!=0) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): V2 not shorted by open SW2");
			}
			//SW3.1 shorted with SW3.2
			foundShortsNum=0;
			if(shortedList.indexOf("SW3.1")!=-1) foundShortsNum++;
			if(shortedList.indexOf("SW3.2")!=-1) foundShortsNum++;
			if(foundShortsNum!=0) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): SW3.1 shorted with SW3.2");
			}
			//Serial conn. of V4.1, V4.2, V4.3 with parallel open SW4
			foundShortsNum=0;
			if(shortedList.indexOf("V4.1")!=-1) foundShortsNum++;
			if(shortedList.indexOf("V4.2")!=-1) foundShortsNum++;
			if(shortedList.indexOf("V4.3")!=-1) foundShortsNum++;
			if(shortedList.indexOf("SW4")!=-1) foundShortsNum++;
			if(foundShortsNum!=0) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): Serial conn. of V4.1, V4.2, V4.3 with parallel open SW4");
			}
			//Serial conn. of V5.1, V5.2, V5.3 with parallel short SW5
			foundShortsNum=0;
			if(shortedList.indexOf("V5.1")!=-1) foundShortsNum++;
			if(shortedList.indexOf("V5.2")!=-1) foundShortsNum++;
			if(shortedList.indexOf("V5.3")!=-1) foundShortsNum++;
			if(shortedList.indexOf("SW5")!=-1) foundShortsNum++;
			if(foundShortsNum!=1) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): Serial conn. of V5.1, V5.2, V5.3 with parallel short SW5");
			}
			//V6 connected to "gnd"
			foundShortsNum=0;
			if(shortedList.indexOf("V6")!=-1) foundShortsNum++;
			if(foundShortsNum!=1) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): V6 connected to \"gnd\"");
			}
			//closed SW7 connected to "gnd"
			foundShortsNum=0;
			if(shortedList.indexOf("SW7")!=-1) foundShortsNum++;
			if(foundShortsNum!=0) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): closed SW7 connected to \"gnd\"");
			}
			//Serial conn. of V8.1, V8.2, V8.3 C8 with parallel short SW8
			foundShortsNum=0;
			if(shortedList.indexOf("V8.1")!=-1) foundShortsNum++;
			if(shortedList.indexOf("V8.2")!=-1) foundShortsNum++;
			if(shortedList.indexOf("V8.3")!=-1) foundShortsNum++;
			if(shortedList.indexOf("C8")!=-1) foundShortsNum++;
			if(shortedList.indexOf("SW8")!=-1) foundShortsNum++;
			if(foundShortsNum!=0) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): Serial conn. of V8.1, V8.2, V8.3 C8 with parallel short SW8");
			}
			//Single ended amp. A9 in "buffer connection"
			foundShortsNum=0;
			if(shortedList.indexOf("A9")!=-1) foundShortsNum++;
			if(foundShortsNum!=0) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): Single ended amp. A9 in \"buffer connection\"");
			}
			
			//check number of reported shorts
			if(shortedList.size()!=3) {
				correctResult = false;
				System.out.println("Failed AllShortsDetected(): check number of reported shorts");
				System.out.println("\t"+shortedList.toString());
				
			}
			return correctResult;
		}
		
		public boolean allRedundantDetected(ArrayList<String> redundInstList) {
			boolean correctResult=true;
			int n;
			n=0;
			//V1 shorted by SW1
			if(redundInstList.indexOf("V1")!=-1) n++;
			if(redundInstList.indexOf("SW1")!=-1) n++;
			if(n!=1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): V1 shorted by SW1");
			}
			//V2 not shorted by open SW2
			if(redundInstList.indexOf("SW2")==-1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): V2 not shorted by open SW2");
			}
			//SW3.1 shorted with SW3.2
			n=0;
			if(redundInstList.indexOf("SW3.1")!=-1) n++;
			if(redundInstList.indexOf("SW3.2")!=-1) n++;
			if(n!=1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): SW3.1 shorted with SW3.2");
			}
			//Serial conn. of V4.1, V4.2, V4.3 with parallel open SW4
			if(redundInstList.indexOf("SW4")==-1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): Serial conn. of V4.1, V4.2, V4.3 with parallel open SW4");
			}
			//Serial conn. of V5.1, V5.2, V5.3 with parallel short SW5
			n=0;
			if(redundInstList.indexOf("V5.1")!=-1) n++;
			if(redundInstList.indexOf("V5.2")!=-1) n++;
			if(redundInstList.indexOf("V5.3")!=-1) n++;
			if(redundInstList.indexOf("SW5")!=-1) n++;
			if(n!=1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): Serial conn. of V5.1, V5.2, V5.3 with parallel short SW5");
			}
			//V6 connected to "gnd"
			if(redundInstList.indexOf("V6")==-1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): V6 connected to \"gnd\"");
			}
			//closed SW7 connected to "gnd"
			if(redundInstList.indexOf("SW7")==-1) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): closed SW7 connected to \"gnd\"");
			}
			//Serial conn. of V8.1, V8.2, V8.3 C8 with parallel short SW8
			//nothing
			//Single ended amp. A9 in "buffer connection"
			//nothing
			//check number of reported shorts
			if(redundInstList.size()!=7) {
				correctResult = false;
				System.out.println("Failed AllRedundantDetected(): check number of reported shorts");
				System.out.println("\t"+redundInstList.toString());
				
			}
			
			return correctResult;			
		}
	}
	
	//many floating and not floating nets
	public class FloatingNodes extends AbstractTestModel {
		public FloatingNodes(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			this.circuit=circuit;
			this.nodePotentialAccuracy=nodePotentialAccuracy;
			
			circuit.addConstNodePotential("gnd", 0.0);
			//C1, V1, SW1(closed) conn. to "gnd"
			circuit.addCapacitor("C1", "net1.1", "gnd").setCapacitance(1.0);
			circuit.addControlledVoltageSource("V1", "net1.2", "gnd").setVoltage(1.0);
			circuit.addSwitch("SW1", "net1.3", "gnd").setConductiveState(true);
			//C2=0, SW2(open) conn. to "gnd"
			circuit.addCapacitor("C2", "net2.1", "gnd").setCapacitance(0.0);
			circuit.addSwitch("SW2", "net2.2", "gnd").setConductiveState(false);
			//C3 and V3 are floating
			circuit.addCapacitor("C3", "net3.2", "net3.1").setCapacitance(1.0);
			circuit.addControlledVoltageSource("V3", "net3.3", "net3.2").setVoltage(1.0);
			//C4.1, C4.2=0, V4, SW4(open) are floating
			circuit.addCapacitor("C4.1", "net4.2", "net4.1").setCapacitance(1.0);
			circuit.addControlledVoltageSource("V4", "net4.3", "net4.2").setVoltage(1.0);
			circuit.addSwitch("SW4", "net4.4", "net4.3").setConductiveState(false);
			circuit.addCapacitor("C4.2", "net4.5", "net4.2").setCapacitance(0.0);
			//C5=0 || V5 conn. to gnd
			circuit.addCapacitor("C5", "net5", "gnd").setCapacitance(0.0);
			circuit.addControlledVoltageSource("V5", "net5", "gnd");
			//const nodes net6.1 and net6.2 are conn. to net6.3 via caps.
			circuit.addConstNodePotential("net6.1", 1.0);
			circuit.addConstNodePotential("net6.2", 2.0);
			circuit.addCapacitor("C6.1", "net6.3", "net6.1").setCapacitance(1.0);
			circuit.addCapacitor("C6.2", "net6.3", "net6.2").setCapacitance(1.0);
			circuit.lockCircuit();
		}
		
		public int getStateNumber() {return 1;}
		
		public void setState(int state) {
			if(state!=0)
				throw new RuntimeException("undefined state");
		}
		
		public boolean checkState(int state) {
			throw new RuntimeException("model is not runable");
		}
				
		public boolean checkCalculation() {
			throw new RuntimeException("model is not runable");
		}
		
		public boolean isConstNodePresent(String nodeName, ArrayList<ConstNodePotential> extraConstNodeList) {
			boolean nodeIsPresent=false;
			for(int i=0; i<extraConstNodeList.size() && !nodeIsPresent; i++) {
				ConstNodePotential src=extraConstNodeList.get(i);
				if(src.getNodeName().equals(nodeName))
					nodeIsPresent=true;
			}
			return nodeIsPresent;
		}
		
		public boolean allFloatingDetected(ArrayList<ConstNodePotential> extraConstNodeList) {
			boolean correctResult=true;
			int n=0;
			
			n=0;
			n+=(isConstNodePresent("net2.1", extraConstNodeList))?(1):(0);
			n+=(isConstNodePresent("net2.2", extraConstNodeList))?(1):(0);
			if(n!=2) {
				correctResult = false;
				System.out.println("Failed AllFloatingDetected(): C2=0, SW2(open) conn. to \"gnd\"");
			}
			
			n=0;
			n+=(isConstNodePresent("net3.1", extraConstNodeList))?(1):(0);
			n+=(isConstNodePresent("net3.2", extraConstNodeList))?(1):(0);
			n+=(isConstNodePresent("net3.3", extraConstNodeList))?(1):(0);
			if(n!=1) {
				correctResult = false;
				System.out.println("Failed AllFloatingDetected(): C3 and V3 are floating");
			}
			
			n=0;
			n+=(isConstNodePresent("net4.1", extraConstNodeList))?(1):(0);
			n+=(isConstNodePresent("net4.2", extraConstNodeList))?(1):(0);
			n+=(isConstNodePresent("net4.3", extraConstNodeList))?(1):(0);
			if(n!=1 || !isConstNodePresent("net4.4", extraConstNodeList) || !isConstNodePresent("net4.5", extraConstNodeList)) {
				correctResult = false;
				System.out.println("Failed AllFloatingDetected(): C4.1, C4.2=0, V4, SW4(open) are floating");
			}
			
			if(extraConstNodeList.size()!=6) {
				correctResult = false;
				System.out.println("Failed AllFloatingDetected(): check number of reported floatings");
			}
			
			return correctResult;
		}
	}
	
	//C1, C2, C3, C4 are connected to gnd1
	//V12 - between C1,C2; SW23 - between C2,C3; 
	//A34 - out. between C3, C4, in. between gnd1, gnd2
	//C5, C6 are connected to gnd2
	//SW56 - between C5,C6
	//V60 - C6, gnd2
	public class ChargeCondInstBetweenCaps extends AbstractTestModel {
		public final byte [][] refCapConnMat= {{-1, -1, -1, -1, 0, 0},
											   {0, 0, 0, 0, -1, -1},
											   {1, 1, 1, 1, 0, 0},
											   {0, 1, 0, 0, 0, 0},
											   {0, 0, 1, 0, 0, 0},
											   {0, 0, 0, 1, 0, 0},
											   {0, 0, 0, 0, 1, 0},
											   {0, 0, 0, 0, 0, 1}};
		public final boolean [] refValidCapConnMatRow= {false, false, true, false, false, false, false, false};
		
		public ChargeCondInstBetweenCaps(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			this.circuit=circuit;
			this.nodePotentialAccuracy=nodePotentialAccuracy;
			//sequence of added components is important for conn. mat. check
			circuit.addConstNodePotential("gnd1", 0);
			circuit.addConstNodePotential("gnd2", 0);
			circuit.addCapacitor("C1", "net1", "gnd1").setCapacitance(1);
			circuit.addCapacitor("C2", "net2", "gnd1").setCapacitance(2);
			circuit.addCapacitor("C3", "net3", "gnd1").setCapacitance(3);
			circuit.addCapacitor("C4", "net4", "gnd1").setCapacitance(4);
			
			circuit.addControlledVoltageSource("V12", "net1", "net2").setVoltage(1);
			circuit.addSwitch("SW23", "net2", "net3").setConductiveState(true);
			circuit.addSingleEndedAmplifier("A34", "net3", "net4", "gnd1", "gnd2");
			//circuit.AddControlledVoltageSource("A34", "net3", "net4").SetVoltage(1);
			
			circuit.addCapacitor("C5", "net5", "gnd2").setCapacitance(5);
			circuit.addCapacitor("C6", "net6", "gnd2").setCapacitance(6);
			
			circuit.addSwitch("SW56", "net5", "net6").setConductiveState(true);
			circuit.addControlledVoltageSource("V60", "net6", "gnd2").setVoltage(1);
			circuit.lockCircuit();
		}
		
		public int getStateNumber() {return 1;}
		
		public void setState(int state) {
			if(state!=0)
				throw new RuntimeException("undefined state");
		}
		
		public boolean checkState(int state) {
			throw new RuntimeException("model is not runable");
		}
				
		public boolean checkCalculation() {
			throw new RuntimeException("model is not runable");
		}
		
		boolean correctCapConnMat(byte [][] capConnMat, boolean [] validCapConnMatRow) {
			boolean correctResult=true;
			//compare with reference
			for(int i=0; i<refCapConnMat.length && correctResult; i++) {
				if(Arrays.compare(refCapConnMat[i], capConnMat[i])!=0)
					correctResult=false;
			}
			if(Arrays.compare(refValidCapConnMatRow, validCapConnMatRow)!=0)
				correctResult=false;
			
			return correctResult;
		}
	}
	
	public class SingEndInt extends AbstractTestModel{
		Switch rstSw;
		Switch smpSw1, smpSw2;
		Switch intSw1, intSw2;
				
		public SingEndInt(SwitchedCapCircuit circuit, double nodePotentialAccuracy) {
			this.circuit=circuit;
			this.nodePotentialAccuracy=nodePotentialAccuracy;
			
			circuit.addConstNodePotential("gnd", 0.0);
			
			circuit.addSingleEndedAmplifier("A1", "out", "gnd", "int", "gnd").setOpenLoopGain(1e6);
			circuit.addCapacitor("Cint", "out", "int").setCapacitance(1);
			rstSw=circuit.addSwitch("SWrst", "out", "int");
			
			circuit.addCapacitor("Csmp", "smpE", "smpI").setCapacitance(1);
			smpSw1=circuit.addSwitch("SWsmpI", "smpI", "gnd");
			intSw1=circuit.addSwitch("SWintI", "smpI", "int");
			
			smpSw2=circuit.addSwitch("SWsmpE", "in", "smpE");
			intSw2=circuit.addSwitch("SWintE", "smpE", "gnd");
			
			circuit.addControlledVoltageSource("Vin", "in", "gnd").setVoltage(1.0);
			circuit.lockCircuit();
			nodeProbes=new ArrayList<NodePotentialProbe>(1);
			nodeProbes.add(circuit.getNodePotentialProbe("out"));
			correctPotentials=new HashMap<String, Double>(1);
			correctPotentials.put("out", 0.0);
		}
		
		public int getStateNumber() {return 6;}
		
		public void setState(int state) {
			switch(state) {
			case 0: //reset
				rstSw.setConductiveState(true);
				smpSw1.setConductiveState(true);
				smpSw2.setConductiveState(true);
				intSw1.setConductiveState(false);
				intSw2.setConductiveState(false);
				correctPotentials.replace("out", 0.0);
				break;
			case 1: //release reset
				rstSw.setConductiveState(false);
				break;
			case 2: //integrate
				smpSw1.setConductiveState(false);
				smpSw2.setConductiveState(false);
				intSw1.setConductiveState(true);
				intSw2.setConductiveState(true);
				correctPotentials.replace("out", 1.0);
				break;
			case 3: //sample
				smpSw1.setConductiveState(true);
				smpSw2.setConductiveState(true);
				intSw1.setConductiveState(false);
				intSw2.setConductiveState(false);
				break;
			case 4: //integrate
				smpSw1.setConductiveState(false);
				smpSw2.setConductiveState(false);
				intSw1.setConductiveState(true);
				intSw2.setConductiveState(true);
				correctPotentials.replace("out", 2.0);
				break;
			case 5: //reset in integrate phase
				rstSw.setConductiveState(true);
				correctPotentials.replace("out", 0.0);
				break;
			default:
				throw new RuntimeException("undefined state");
			}
		}
	}
}
