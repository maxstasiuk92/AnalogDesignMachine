package SwitchedCapExamples;
import java.util.Arrays;

import SwitchedCapCalculation.*;
import SwitchedCapComponents.*;
import SwitchedCapCalculationTest.*;

public class SigmaDeltaModulator {
	private SwitchedCapCircuit circuit;
	private Multiplexor refpExtMux, refnExtMux;
	private Multiplexor refpIntMux, refnIntMux;
	private Multiplexor sigpExtMux, signExtMux;
	private Multiplexor sigpIntMux, signIntMux;
	private Switch rstpSw, rstnSw;
	private final int channelForSample=0, channelForSum=1, channelForSub=2;
	private final String resetPhase="rstPh", samplePhase="smpPh";
	private final String sumPhase="sumPh", subPhase="subPh";
	private NodePotentialProbe outp, outn;
	private DifferentialVoltageSource reference, signal;
	private boolean inReset;
	
	public static void main(String [] args) {
		SigmaDeltaModulator sdm=new SigmaDeltaModulator();
		//get bitstream, length=10;
		byte [] bitstream=new byte [10];
		for(int i=0; i<bitstream.length; i++) {
			bitstream[i]=sdm.getBit();
		}
		//display bitsream
		System.out.println("Bitstream: "+Arrays.toString(bitstream));
	}
	
	public SigmaDeltaModulator() {
		buildCircuit();
		createPhases();
		inReset=true;
	}
	
	public void updateSignal(double value) {
		signal.updateVoltagesInAllStates(value, signal.getCommonModeVoltage());
	}
	
	public byte getBit() {
		if(inReset) {
			circuit.calculateState(resetPhase);
			inReset=false;
		}
		byte result=(outp.getNodePotential()-outn.getNodePotential()>0)?(byte)1:(byte)0;
		circuit.calculateState(samplePhase);
		//behaviour of comparator
		if(result>0) {
			circuit.calculateState(subPhase);
		} else {
			circuit.calculateState(sumPhase);
		}
		return result;
	}
	
	private void buildCircuit() {
		Capacitor cap;
		ControlledVoltageSource src;
		DifferentialAmplifier amp;
		circuit=SwitchedCapCircuitCreator.createSwitchedCapCircuit();
		//positive path for input signal
		sigpExtMux=new Multiplexor("Ip1", "#s1p", 3);
		sigpExtMux
		.setChannelNodeName("#sigp", channelForSample)
		.setChannelNodeName("#sigcm", channelForSum)
		.setChannelNodeName("#sigcm", channelForSub);
		circuit.addComponent(sigpExtMux);
		cap=new Capacitor("Cp1", "#s1p", "#s2p");
		cap.setCapacitance(1e-6);
		circuit.addComponent(cap);
		sigpIntMux=new Multiplexor("Ip2", "#s2p", 3);
		sigpIntMux
		.setChannelNodeName("#outcm", channelForSample)
		.setChannelNodeName("#intp", channelForSum)
		.setChannelNodeName("#intn", channelForSub); //will not be used
		circuit.addComponent(sigpIntMux);
		//negative path for input signal
		signExtMux=new Multiplexor("In1", "#s1n", 3);
		signExtMux
		.setChannelNodeName("#sign", channelForSample)
		.setChannelNodeName("#sigcm", channelForSum)
		.setChannelNodeName("#sigcm", channelForSub);
		circuit.addComponent(signExtMux);
		cap=new Capacitor("Cn1", "#s1n", "#s2n");
		cap.setCapacitance(1e-6);
		circuit.addComponent(cap);
		signIntMux=new Multiplexor("In2", "#s2n", 3);
		signIntMux
		.setChannelNodeName("#outcm", channelForSample)
		.setChannelNodeName("#intn", channelForSum)
		.setChannelNodeName("#intp", channelForSub); //will not be used
		circuit.addComponent(signIntMux);
		//positive path for reference
		refpExtMux=new Multiplexor("Irp1", "#r1p", 3);
		refpExtMux
		.setChannelNodeName("#refp", channelForSample)
		.setChannelNodeName("#refcm", channelForSum)
		.setChannelNodeName("#refcm", channelForSub);
		circuit.addComponent(refpExtMux);
		cap=new Capacitor("Crp1", "#r1p", "#r2p");
		cap.setCapacitance(1e-6);
		circuit.addComponent(cap);
		refpIntMux=new Multiplexor("Irp2", "#r2p", 3);
		refpIntMux
		.setChannelNodeName("#outcm", channelForSample)
		.setChannelNodeName("#intp", channelForSum)
		.setChannelNodeName("#intn", channelForSub);
		circuit.addComponent(refpIntMux);
		//negative path for reference
		refnExtMux=new Multiplexor("Irn1", "#r1n", 3);
		refnExtMux
		.setChannelNodeName("#refn", channelForSample)
		.setChannelNodeName("#refcm", channelForSum)
		.setChannelNodeName("#refcm", channelForSub);
		circuit.addComponent(refnExtMux);
		cap=new Capacitor("Crn1", "#r1n", "#r2n");
		cap.setCapacitance(1e-6);
		circuit.addComponent(cap);
		refnIntMux=new Multiplexor("Irn2", "#r2n", 3);
		refnIntMux
		.setChannelNodeName("#outcm", channelForSample)
		.setChannelNodeName("#intn", channelForSum)
		.setChannelNodeName("#intp", channelForSub);
		circuit.addComponent(refnIntMux);
		//amplifier with feedback and volt. source for common-mode
		/*src=new ControlledVoltageSource("Vocm", "#outcm", "#gnd");
		src.setVoltage(2);
		circuit.addComponent(src);*/
		amp=new DifferentialAmplifier("A1", "#outp", "#outn", "#gnd", "#intp", "#intn", "#outcm");
		amp.setOpenLoopGainDM(10000); //80dB
		amp.setOpenLoopGainCM(1000); //60dB
		circuit.addComponent(amp);
		
		cap=new Capacitor("Cp2", "#intp", "#outn");
		cap.setCapacitance(1e-6);
		rstpSw=new Switch("SWp", "#intp", "#outn");
		circuit.addComponent(cap);
		circuit.addComponent(rstpSw);
		
		cap=new Capacitor("Cn2", "#intn", "#outp");
		cap.setCapacitance(1e-6);
		rstnSw=new Switch("SWn", "#intn", "#outp");
		circuit.addComponent(cap);
		circuit.addComponent(rstnSw);
		
		//set potential of "#gnd" to 0, add reference and signal sources
		/*reference=new DifferentialVoltageSource("Vr", "#refp", "#refn", "#refcm", "#gnd");
		signal=new DifferentialVoltageSource("Vs", "#sigp", "#sign", "#sigcm", "#gnd");
		reference.setVoltages(1.0, 2.0);
		signal.setVoltages(0.5, 2.0);
		circuit.addComponent(reference);
		circuit.addComponent(signal);*/
		circuit.addComponent(new ConstNodePotential("#gnd", 0.0));
		//lock circuit
		circuit.lockCircuit();
		TestUtil.printNodesInCircuit(circuit);
		//get outputs
		outp=circuit.getNodePotentialProbe("#outp");
		outn=circuit.getNodePotentialProbe("#outn");
	}
	
	private void createPhases() {
		//reset phase
		rstpSw.setConductiveState(true);
		rstnSw.setConductiveState(true);
		refpExtMux.setConductiveChannel(channelForSample);
		refnExtMux.setConductiveChannel(channelForSample);
		refpIntMux.setConductiveChannel(channelForSample);
		refnIntMux.setConductiveChannel(channelForSample);
		sigpExtMux.setConductiveChannel(channelForSample);
		signExtMux.setConductiveChannel(channelForSample);
		sigpIntMux.setConductiveChannel(channelForSample);
		signIntMux.setConductiveChannel(channelForSample);
		circuit.saveState(resetPhase);
		//sample phase
		rstpSw.setConductiveState(false);
		rstnSw.setConductiveState(false);
		circuit.saveState(samplePhase);
		//sum reference with signal
		refpExtMux.setConductiveChannel(channelForSum);
		refnExtMux.setConductiveChannel(channelForSum);
		refpIntMux.setConductiveChannel(channelForSum);
		refnIntMux.setConductiveChannel(channelForSum);
		sigpExtMux.setConductiveChannel(channelForSum);
		signExtMux.setConductiveChannel(channelForSum);
		sigpIntMux.setConductiveChannel(channelForSum);
		signIntMux.setConductiveChannel(channelForSum);
		circuit.saveState(sumPhase);
		//subtract reference from signal
		refpExtMux.setConductiveChannel(channelForSub);
		refnExtMux.setConductiveChannel(channelForSub);
		refpIntMux.setConductiveChannel(channelForSub);
		refnIntMux.setConductiveChannel(channelForSub);
		sigpExtMux.setConductiveChannel(channelForSum); //signal always sums
		signExtMux.setConductiveChannel(channelForSum);
		sigpIntMux.setConductiveChannel(channelForSum);
		signIntMux.setConductiveChannel(channelForSum);
		circuit.saveState(subPhase);
	}

}

