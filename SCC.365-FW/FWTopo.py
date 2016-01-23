from mininet.topo import Topo
from mininet.link import TCLink

class MyTopo( Topo ):
    "FW Topology (Stage 1) for Week 7 Sign-off. "

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        host1 = self.addHost( 'h1' )
        host2 = self.addHost( 'h2' )
	host3 = self.addHost( 'h3' )
	host4 = self.addHost( 'h4' )
	host5 = self.addHost( 'h5' )
	host6 = self.addHost( 'h6' )
        switch1 = self.addSwitch( 's1' )

        # Add links
        self.addLink( host1, switch1, bw=100, delay='5ms' )
        self.addLink( host2, switch1, bw=100, delay='5ms' )
	self.addLink( host3, switch1, bw=100, delay='5ms' )
	self.addLink( host4, switch1, bw=5, delay='10ms' )
	self.addLink( host5, switch1, bw=5, delay='10ms' )
	self.addLink( host6, switch1, bw=5, delay='10ms' )

topos = { 'mytopo': ( lambda: MyTopo() ) }



