from mininet.topo import Topo
from mininet.link import TCLink

class MyTopo( Topo ):
    "Topology for Week 4 Sign-off. "

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        host1 = self.addHost( 'h1' )
        host2 = self.addHost( 'h2' )
	host3 = self.addHost( 'h3' )
	host4 = self.addHost( 'h4' )
        switch1 = self.addSwitch( 's1' )
	switch2 = self.addSwitch( 's2' )

        # Add links
        self.addLink( host1, switch1, bw=10, delay='10ms' )
        self.addLink( host2, switch1, bw=10, delay='10ms' )
	self.addLink( host3, switch2, bw=10, delay='10ms' )
	self.addLink( host4, switch2, bw=10, delay='10ms' )
	self.addLink( switch1, switch2, bw=10, delay='10ms' )

topos = { 'mytopo': ( lambda: MyTopo() ) }



