package SwitchedCapComponents;

import SwitchedCapCalculation.*;

public class Multiplexor implements SwitchedCapComponent{
	private String name;
	private boolean addedToCircuit;
	private Switch [] switchArray;
	private String outputNodeName;
	private int conductiveChannel;
		
	public Multiplexor(String name, String outputNodeName, int numberOfChannels) {
		if(numberOfChannels<=0) {
			throw new RuntimeException("numberOfChannels should be positive");
		}
		//check that outputNodeName!=null is implemented inside Switch
		this.name=name;
		this.addedToCircuit=false;
		this.outputNodeName=outputNodeName;
		switchArray=new Switch[numberOfChannels];
		conductiveChannel=0;
	}
	
	public Multiplexor setChannelNodeName(String nodeName, int channel) {
		if(addedToCircuit) {
			throw new RuntimeException("multiplexor "+name+" is already added to circuit");
		}
		if(channel>=switchArray.length || channel<0) {
			throw new RuntimeException("channel out of range");
		}
		if(switchArray[channel]!=null) {
			throw new RuntimeException("channel "+channel+" was previously set");
		}
		Switch sw=new Switch(name, nodeName, outputNodeName);
		if(channel==conductiveChannel) {
			sw.setConductiveState(true);
		}
		switchArray[channel]=sw;
		return this;
	}
	
	/**if input node for channel was not set, all channels will be in non-conductive state*/
	public void setConductiveChannel(int channel) {
		if(channel>=switchArray.length || channel<0) {
			throw new RuntimeException("channel out of range");
		}
		if(switchArray[conductiveChannel]!=null) {
			switchArray[conductiveChannel].setConductiveState(false);
		}
		if(switchArray[channel]!=null) {
			switchArray[channel].setConductiveState(true);
		}
		conductiveChannel=channel;
	}
	
	public void addToCircuit(SwitchedCapCircuit circuit) {
		//check, that component is already in a circuit is implemented in Switch
		for(Switch sw: switchArray) {
			if(sw!=null) {
				sw.addToCircuit(circuit);
			}
		}
		addedToCircuit=true;
	}

}
