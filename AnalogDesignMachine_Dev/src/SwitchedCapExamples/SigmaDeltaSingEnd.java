package SwitchedCapExamples;
import SwitchedCapCalculation.*;
import SwitchedCapComponents.ControlledVoltageSource;
import SwitchedCapComponents.Switch;

public class SigmaDeltaSingEnd {
	private Stage stage1, stage2;
	
	private class Stage{
		private SwitchedCapCircuit circuit;
		private ControlledVoltageSource signal, reference;
		private Switch smpSigSw1, smpSigSw2;
		private Switch smpRefSw1, smpRefSw2;
		private Switch intSigSw1, intSigSw2;
		private Switch intRefSw1, intRefSw2;
		private Switch rstSw;
		private NodePotentialProbe outPrb;
		private double Vsign;
		
		public Stage(double Vref) {
			Vsign=0;
			//circuit=new SwitchedCapCircuit();
			circuit.addConstNodePotential("gnd", 0.0);
			
			signal=circuit.addControlledVoltageSource("Vsign", "in", "gnd").setVoltage(0);
			smpSigSw1=circuit.addSwitch("", "in", "inS");
			circuit.addCapacitor("Csig", "inS", "intS").setCapacitance(1.0);
			smpSigSw2=circuit.addSwitch("", "intS", "gnd");
			intSigSw1=circuit.addSwitch("", "inS", "gnd");
			intSigSw2=circuit.addSwitch("", "intS", "intA");
			
			reference=circuit.addControlledVoltageSource("Vref", "ref", "gnd").setVoltage(Vref);
			smpRefSw1=circuit.addSwitch("", "ref", "inR");
			circuit.addCapacitor("Csig", "inR", "intR").setCapacitance(1.0);
			smpRefSw2=circuit.addSwitch("", "intR", "gnd");
			intRefSw1=circuit.addSwitch("", "inR", "gnd");
			intRefSw2=circuit.addSwitch("", "intR", "intA");
			
			circuit.addSingleEndedAmplifier("A", "out", "gnd", "intA", "gnd").setOpenLoopGain(1e6);
			circuit.addCapacitor("Cint", "out", "intA").setCapacitance(1.0);
			rstSw=circuit.addSwitch("", "out", "intA");
			circuit.lockCircuit();
			outPrb=circuit.getNodePotentialProbe("out");
		}
		
		public void SamplingPhase() {
			smpSigSw1.setConductiveState(true);
			smpSigSw2.setConductiveState(true);
			intSigSw1.setConductiveState(false);
			intSigSw2.setConductiveState(false);
			
			smpRefSw1.setConductiveState(true);
			smpRefSw2.setConductiveState(true);
			intRefSw1.setConductiveState(false);
			intRefSw2.setConductiveState(false);
		}
		
		public void IntegrationPhase() {
			smpSigSw1.setConductiveState(false);
			smpSigSw2.setConductiveState(false);
			intSigSw1.setConductiveState(true);
			intSigSw2.setConductiveState(true);
			
			smpRefSw1.setConductiveState(false);
			smpRefSw2.setConductiveState(false);
			intRefSw1.setConductiveState(true);
			intRefSw2.setConductiveState(true);
		}
		
		public void ResetPhase() {
			SamplingPhase();
			rstSw.setConductiveState(true);
		}
		
		public void setSignal(double val) {
			Vsign=val;
		}
		
		public double getOutput() {
			return outPrb.getNodePotential();
		}
	}
	
	public SigmaDeltaSingEnd() {
		stage1=new Stage(1.0);
		stage2=new Stage(1.0);
		stage1.ResetPhase();
		stage2.ResetPhase();
	}

}

