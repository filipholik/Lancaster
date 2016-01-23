package net.floodlightcontroller.practical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;

import java.net.InetAddress; //F: For IP addressing


import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Complex Topology, Firewall for Week 7 Sign-Off
public class Practical implements IFloodlightModule, IOFMessageListener {

	protected static Logger log = LoggerFactory.getLogger(Practical.class);
    protected IFloodlightProviderService floodlightProvider;
	protected HashMap<IOFSwitch, HashMap> switchToMapping;
	protected boolean FWRulesInserted;
    	//protected HashMap<Long,Short> macToPort;

	@Override
	public String getName() {
		return "practical";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
    	Collection<Class<? extends IFloodlightService>> l =
        	new ArrayList<Class<? extends IFloodlightService>>();
    	l.add(IFloodlightProviderService.class);
    	return l;
	}
	
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
    	floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
    	log = LoggerFactory.getLogger(Practical.class);
	//Hash map initialization
	switchToMapping = new HashMap<IOFSwitch, HashMap>();	
	FWRulesInserted = false;
    	
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
    	floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	/* Handle a packet message - called every time a packet is received*/
	/* TODO  My implementation */
   	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		  //Get Packet-In message
        	OFPacketIn pi = (OFPacketIn) msg;

		//F: Load details from packet	
		OFMatch match = new OFMatch();
		match.loadFromPacket(pi.getPacketData(), pi.getInPort());

		//F: retrieve the source MAC
		Long sourceMac = Ethernet.toLong(match.getDataLayerSource());

		//F: Print MAC in readable format	
        	String readableSourceMac = HexString.toHexString(sourceMac);
		log.debug("Packet with source MAC: {} came in on port: {}", readableSourceMac, pi.getInPort());
 
		//F: Store MAC to port mapping
		//Verify switch mapping exists
		if(!switchToMapping.containsKey(sw)) {
			//Switch does not exist yet, create new hash map
			HashMap<Long,Short> macToPort = new HashMap<Long,Short>();		
			switchToMapping.put(sw, macToPort);
		}
		

		//F: Get destination port in readable format
		Long destinationMac = Ethernet.toLong(match.getDataLayerDestination());
		String readableDestinationMac = HexString.toHexString(destinationMac);
		
		
		HashMap<Long,Short> macToPort = switchToMapping.get(sw);
		//TODO enable only on switch2!!!		
		
			if(macToPort != null){
				//F: Put mapping into hashmap	
				macToPort.put(sourceMac, (short) pi.getInPort()); 
				switchToMapping.put(sw, macToPort);	

				if(!FWRulesInserted && sw.getId() == 2)
					enableFirewall(sw, cntx);			
			
				//F: Verify destination MAC known		
				if(macToPort.containsKey(destinationMac)) {
					log.debug("Destination MAC address known: {}", readableDestinationMac);
					//F: Retrieve port number
					short destinationPort = (short) macToPort.get(destinationMac);
					log.debug("Destination MAC address known: {} forwards to port: {}", 						readableDestinationMac, destinationPort);
			
					//F: More efficient function
					installFlowMod(sw, pi, match, (short) destinationPort, 60, 120, cntx);
					writePacketToPort(sw, pi, (short) destinationPort, cntx);	
				}else{
					//F: Hub behavior 
					//F: More efficient function
					log.debug("Destination MAC address unknown: flooding");
					writePacketToPort(sw, pi, OFPort.OFPP_FLOOD.getValue(), cntx);		
				}
			}
		

		//Allow Floodlight to continue processing the packet
        return Command.CONTINUE;
    }

    /*public boolean firewallAllowed(OFMatch match){
	Long sourceMac = Ethernet.toLong(match.getDataLayerSource());
	Long destinationMac = Ethernet.toLong(match.getDataLayerDestination());
	//int networkDestination = (int) match.getNetworkDestination();
	OFMatch fw = new OFMatch();	
	//String ip = OFMatch.ipToString(networkDestination);
	//InetAddress.getByName("10.0.0.2");
	//String ip = InetAddress.ToString(networkDestination);
	
	//Drop traffic with source 00:00:00:00:00:01
	if(HexString.toHexString(sourceMac) == "00:00:00:00:00:01") return false; 
	log.debug("Firewall allows...");

	//Drop traffic with destination IP address 10.0.0.2
	fw.fromString("ip_dst=10.0.0.2");
	if(match.getNetworkDestination() == fw.getNetworkDestination()) return false; 	

	//Drop traffic with source 00:00:00:00:00:03 and destination 00:00:00:00:00:06
	if(HexString.toHexString(sourceMac) == "00:00:00:00:00:03" && HexString.toHexString(destinationMac) == "00:00:00:00:00:06") return false; 

	//Drop traffic from TCP port 80
	fw.fromString("tp_src=80");
	if(match.getTransportSource() == fw.getTransportSource()) return false; 	
	
	log.debug("Firewall allows...");
    	return true;
    }*/
    
    /* Write a packet out to a specific port */
    public void writePacketToPort (IOFSwitch sw, OFPacketIn pi, short outPort,  FloodlightContext cntx) {
        OFPacketOut packetOut = (OFPacketOut) floodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT);
        packetOut.setBufferId(pi.getBufferId());
        packetOut.setInPort(pi.getInPort());
        OFActionOutput action = new OFActionOutput().setPort(outPort);
        packetOut.setActions(Collections.singletonList((OFAction)action));
        packetOut.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
        if (pi.getBufferId() == 0xffffffff) {
            byte[] packetData = pi.getPacketData();
            packetOut.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH + packetOut.getActionsLength() + packetData.length));
            packetOut.setPacketData(packetData);
        } else {
            packetOut.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH + packetOut.getActionsLength()));
        }
        try {
            sw.write(packetOut, cntx);
        } catch (IOException e) {
            log.error("Failure writing packet to port", e);
        }
    }

	private void enableFirewall(IOFSwitch sw, FloodlightContext cntx){
		log.debug("Enabling firewall...");		

		//Drop traffic with source 00:00:00:00:00:01
		OFMatch match = new OFMatch();
		match.setDataLayerSource("00:00:00:00:00:01");
		installFWFlowMod(sw, match, cntx);
		//installFlowMod(sw, null, match, (short) OFPort.OFPP_FLOOD.getValue(), 1800, 3600, cntx);

		//Drop traffic with destination IP address 10.0.0.2
		match = new OFMatch();
		//match.setNetworkDestination();
		//FWRulesInserted = true;

		//Drop traffic with source 00:00:00:00:00:03 and destination 00:00:00:00:00:06
		match = new OFMatch();
		match.setDataLayerSource("00:00:00:00:00:03");
		match.setDataLayerDestination("00:00:00:00:00:06");
		installFWFlowMod(sw, match, cntx);

		//Drop traffic from TCP port 80
		match = new OFMatch();
		//match.setNetworkProtocol();
		//match.setTransportSource((short)80);
		//installFWFlowMod(sw, match, cntx);		
	}
    
    /* Install a flow-mod with given parameters */
     private void installFlowMod(IOFSwitch sw, OFPacketIn pi, OFMatch match, short outPort, int idleTimeout, int hardTimeout, FloodlightContext cntx) {
        OFFlowMod flowMod = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
        flowMod.setBufferId(-1);
        flowMod.setOutPort(outPort);
        flowMod.setMatch(match);
        flowMod.setCommand(OFFlowMod.OFPFC_ADD);
        flowMod.setIdleTimeout((short) idleTimeout);
        flowMod.setHardTimeout((short) hardTimeout);
        flowMod.setPriority((short) 10);
        flowMod.setActions(Arrays.asList((OFAction) new OFActionOutput(outPort, (short) 0xffff)));
        flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH));
        try {
            sw.write(flowMod, cntx);		
        } catch (IOException e) {
            log.error("Failure writing Flow-Mod", e);
        }
    }

	/* Install a Firewall flow-mod */
     private void installFWFlowMod(IOFSwitch sw, OFMatch match, FloodlightContext cntx) {
        OFFlowMod flowMod = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
        flowMod.setBufferId(-1);
        flowMod.setOutPort((short)OFPort.OFPP_NONE.getValue());
        flowMod.setMatch(match);
        flowMod.setCommand(OFFlowMod.OFPFC_ADD);
        flowMod.setIdleTimeout((short) 1800);
        flowMod.setHardTimeout((short) 3600);
        flowMod.setPriority((short) 200);
        flowMod.setActions(Arrays.asList((OFAction) new OFActionOutput(OFPort.OFPP_NONE.getValue(), (short) 0xffff)));
        flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH));
        try {
            sw.write(flowMod, cntx);
		log.debug("Firewall flow inserted...");
        } catch (IOException e) {
            log.error("Failure writing FW Flow-Mod", e);
        }
    }

	@Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // We don't provide any services, return null
        return null;
    }
}
