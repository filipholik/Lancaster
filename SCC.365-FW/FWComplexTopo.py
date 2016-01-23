from mininet.topo import Topo
from mininet.link import TCLink

class MyTopo( Topo ):
    "FW Complex Topology  (Stage 3) for Week 7 Sign-off. "

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
	switch2 = self.addSwitch( 's2' )
	switch3 = self.addSwitch( 's3' )

        # Add links
        self.addLink( host1, switch1, bw=100, delay='5ms' )
        self.addLink( host2, switch1, bw=100, delay='5ms' )
	self.addLink( host3, switch1, bw=100, delay='5ms' )
	self.addLink( host4, switch1, bw=5, delay='10ms' )
	self.addLink( host5, switch1, bw=5, delay='10ms' )
	self.addLink( host6, switch1, bw=5, delay='10ms' )
	self.addLink( switch1, switch2, delay='40ms' )
	self.addLink( switch2, switch3, delay='40ms' )

topos = { 'mytopo': ( lambda: MyTopo() ) }



