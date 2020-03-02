package SwitchedCapComponents;

import SwitchedCapCalculation.*;

/*model: 
 * positiveOutputNodeName-groundNodeName=openLoopGainDM*(positiveInputNodeName-negativeInputNodeName+inputOffsetDM)/2
 * 		-openLoopGainCM*[(positiveOutputNodeName+negativeOutputNodeName)/2-outCMrefNodeName+inputOffsetCM] --->
 * 
 * -groundNodeName
 * -openLoopGainDM/2*positiveInputNodeName
 * +openLoopGainDM/2*negativeInputNodeName
 * +(openLoopGainCM/2+1)*positiveOutputNodeName
 * +openLoopGainCM/2*negativeOutputNodeName
 * -openLoopGainCM*outCMrefNodeName
 * 				=
 * +openLoopGainDM/2*inputOffsetDM
 * -openLoopGainCM*inputOffsetCM
 * ------------------------------------------------------------------------------------------------------
 * negativeOutputNodeName-groundNodeName=-openLoopGainDM*(positiveInputNodeName-negativeInputNodeName+inputOffsetDM)/2
 * 				-openLoopGainCM*[(positiveOutputNodeName+negativeOutputNodeName)/2-outCMrefNodeName+inputOffsetCM] --->
 * 
 * 
 * -groundNodeName
 * +openLoopGainDM/2*positiveInputNodeName
 * -openLoopGainDM/2*negativeInputNodeName
 * +openLoopGainCM/2*positiveOutputNodeName
 * +(openLoopGainCM/2+1)*negativeOutputNodeName
 * -openLoopGainCM*outCMrefNodeName
 * 				=
 * -openLoopGainDM/2*inputOffsetDM
 * -openLoopGainCM*inputOffsetCM

 * */
public class DifferentialAmplifier implements SwitchedCapComponent {
	private VoltageDependency ampPos, ampNeg;
	private String positiveOutputNodeName, negativeOutputNodeName, groundNodeName;
	private String positiveInputNodeName, negativeInputNodeName;
	private String outCMrefNodeName;
	private double inputOffsetDM, inputOffsetCM;
	private double openLoopGainDM, openLoopGainCM;
	
	public DifferentialAmplifier(String name, 
			String positiveOutputNodeName, String negativeOutputNodeName, String groundNodeName,
			String positiveInputNodeName, String negativeInputNodeName, String outCMrefNodeName) {
		if(positiveInputNodeName == null || negativeOutputNodeName==null || outCMrefNodeName==null) {
			throw new RuntimeException("node names should not be null");
		}
		ampPos=new VoltageDependency(name, positiveOutputNodeName, groundNodeName, true);
		ampPos.setConductiveState(true);
		ampNeg=new VoltageDependency(name, negativeOutputNodeName, groundNodeName, true);
		ampNeg.setConductiveState(true);
		this.positiveOutputNodeName=positiveOutputNodeName;
		this.negativeOutputNodeName=negativeOutputNodeName;
		this.groundNodeName=groundNodeName;
		this.positiveInputNodeName=positiveInputNodeName;
		this.negativeInputNodeName=negativeInputNodeName;
		this.outCMrefNodeName=outCMrefNodeName;
		this.inputOffsetDM=0;
		this.inputOffsetCM=0;
		this.openLoopGainDM=1;
		this.openLoopGainCM=1;
	}
	
	public DifferentialAmplifier setInputOffsetDM(double offset) {
		inputOffsetDM=offset;
		setFreeCoefficients();
		return this;
	}
	
	public DifferentialAmplifier setInputOffsetCM(double offset) {
		inputOffsetCM=offset;
		setFreeCoefficients();
		return this;
	}
	
	public DifferentialAmplifier setOpenLoopGainDM(double openLoopGain) {
		if(openLoopGain <= 0) {
			throw new RuntimeException("openLoopGain should be > 0");
		}
		openLoopGainDM=openLoopGain;
		setCoefficientsForAmpPos();
		setCoefficientsForAmpNeg();
		setFreeCoefficients();
		return this;
	}
	
	public DifferentialAmplifier setOpenLoopGainCM(double openLoopGain) {
		if(openLoopGain <= 0) {
			throw new RuntimeException("openLoopGain should be > 0");
		}
		openLoopGainCM=openLoopGain;
		setCoefficientsForAmpPos();
		setCoefficientsForAmpNeg();
		setFreeCoefficients();
		return this;
	}
	
	/*calculation of coefficients*/
	private void setCoefficientsForAmpPos() {
		/*ampPos.setNodeCoefficient(groundNodeName, -1.0);
		ampPos.setNodeCoefficient(positiveInputNodeName, -openLoopGainDM/2.0);
		ampPos.setNodeCoefficient(negativeInputNodeName, openLoopGainDM/2.0);
		ampPos.setNodeCoefficient(positiveOutputNodeName, openLoopGainCM/2.0*+1.0);
		ampPos.setNodeCoefficient(negativeOutputNodeName, openLoopGainCM/2.0);
		ampPos.setNodeCoefficient(outCMrefNodeName, -openLoopGainCM);*/
		ampPos.setNodeCoefficient(groundNodeName, 1.0);
		ampPos.setNodeCoefficient(positiveInputNodeName, -openLoopGainDM/2.0);
		ampPos.setNodeCoefficient(negativeInputNodeName, openLoopGainDM/2.0);
		ampPos.setNodeCoefficient(positiveOutputNodeName, 1.0);
		//ampPos.setNodeCoefficient(negativeOutputNodeName, openLoopGainCM/2.0);
		ampPos.setNodeCoefficient(outCMrefNodeName, -1.0);
	}
	
	private void setCoefficientsForAmpNeg() {
		/*ampNeg.setNodeCoefficient(groundNodeName, -1.0);
		ampNeg.setNodeCoefficient(positiveInputNodeName, openLoopGainDM/2.0);
		ampNeg.setNodeCoefficient(negativeInputNodeName, -openLoopGainDM/2.0);
		ampNeg.setNodeCoefficient(positiveOutputNodeName, openLoopGainCM/2.0);
		ampNeg.setNodeCoefficient(negativeOutputNodeName, openLoopGainCM/2.0+1);
		ampNeg.setNodeCoefficient(outCMrefNodeName, -openLoopGainCM);*/
		ampNeg.setNodeCoefficient(groundNodeName, 1.0);
		ampNeg.setNodeCoefficient(positiveInputNodeName, openLoopGainDM/2.0);
		ampNeg.setNodeCoefficient(negativeInputNodeName, -openLoopGainDM/2.0);
		//ampNeg.setNodeCoefficient(positiveOutputNodeName, openLoopGainCM/2.0);
		ampNeg.setNodeCoefficient(negativeOutputNodeName, 1.0);
		ampNeg.setNodeCoefficient(outCMrefNodeName, -1.0);
		
	}
	
	private void setFreeCoefficients() {
		ampPos.setFreeCoefficient(openLoopGainDM/2.0*inputOffsetDM-openLoopGainCM*inputOffsetCM);
		ampNeg.setFreeCoefficient(-openLoopGainDM/2*inputOffsetDM -openLoopGainCM*inputOffsetCM);
	}
	
	public void addToCircuit(SwitchedCapCircuit circuit) {
		ampPos.addToCircuit(circuit);
		ampNeg.addToCircuit(circuit);
	}
		
}
