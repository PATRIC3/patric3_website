package HPI::Graph::DirectedGraph;

use lib "../../CPAN";
use HTML::Entities;
use Data::Table;
use Switch;

use HPI::Data::AdjacencyList;
use HPI::Data::UnionFind;
use HPI::Graph::UndirectedGraph;
use HPI::Graph::Node;
use HPI::Graph::Edge;

use base 'HPI::Graph::Graph';


sub new {
	my $class = shift;
	my $self = HPI::Graph::Graph->new(@_);
	bless($self, $class);
	return $self;
}


sub empty {
	my $self = shift;
	$self->{NODE_INDICES} = {};
	$self->{NODES}        = {};
	$self->{NODE_IDS}     = [];
	$self->{NODECOUNT}    = 0;
	$self->{ADJACENCIES}  = new HPI::Data::AdjacencyList;
	$self->{EDGES}        = {};
	$self->{EDGECOUNT}    = 0;
	$self->{MAXDEGREE}    = 0;
	$self->{MAXINDEGREE}  = 0;
	$self->{MAXOUTDEGREE} = 0;
	$self->{CHILDREN}  		= {};
	$self->{TAGS}     		= {};
	$self->{PROPS}     		= {};
	$self->{PROPTYPES} 		= {};
}

sub directed {
	my $self = shift;
	return 1;
}

sub is_polytree {
	my $self = shift;
	return 0 unless $self->is_dag;
	my $ug = new HPI::Graph::UndirectedGraph(id=>"temp");
	foreach my $edge ($self->get_edges) {
		$ug->add_edge($edge->source, $edge->target);
	}
	return $ug->is_tree;
}

sub is_dag {
	my $self = shift;
	return ((scalar @{$self->get_all_components} == 1) && ($self->has_cycle == 0));
}


############################################################
# node and node-related methods

sub deg {
	my ($self, $sid) = @_;
	return $self->outdeg($sid) + $self->indeg($sid);
}

sub outdeg {
	my ($self, $sid) = @_;
	return $self->{ADJACENCIES}->count_adjacent($self->indx_from_id($sid));
}

sub indeg {
	my ($self, $sid) = @_;
	# loop through all ALs and count how many contain $sid
	my $sindx = $self->indx_from_id($sid);
	my $count = 0;
	foreach my $tid ($self->get_nids) {
		my $tindx = $self->indx_from_id($tid);
		# increment count if edge goes from t into s
		$count++ if ($self->{ADJACENCIES}->are_adjacent($tindx, $sindx));
	}
	return $count;
}

sub sources {
	my $self = shift;
	my %inedges = ();
	foreach my $sid (@{$self->get_nids}) {
		my $sindx = $self->indx_from_id($sid);
		$inedges{$sindx} = 0 unless (exists $inedges{$sindx});
		foreach my $tindx (@{$self->{ADJACENCIES}->get_all_adjacent($sindx)}) {
			$inedges{$tindx} = 0 unless (exists $inedges{$tindx});
			$inedges{$tindx}++;
		}
	}
	my @sources = ();
	foreach my $nindx (keys %inedges) {
		push(@sources, $self->id_from_indx($nindx)) if ($inedges{$nindx} == 0);
	}
	return \@sources;
}

sub sinks {
	my $self = shift;
	my @sinks = ();
	# get the indices of all empty adjacency lists
	my $empty = $self->{ADJACENCIES}->get_empty;
	foreach my $nindx (@$empty) {
		push @sinks, $self->id_from_indx($nindx);
	}
	return \@sinks;
}

sub get_neighbors {
	my ($self, $id) = @_;
	return [] unless $self->has_node($id);
	my %nbrs = ();
	my $indx = $self->indx_from_id($id);
	# get all the target nodes
	my @tindxs = @{$self->{ADJACENCIES}->get_all_adjacent($sindx)};
	foreach my $tindx (@tindxs) {
		$nbrs{$self->id_from_indx($tindx)} = 1;
	}
	# get all nodes that have this node as a target
	foreach my $sid ($self->get_nids) {
		my $sindx = $self->indx_from_id($sid);
		$nbrs{$sid} = 1 if ($self->{ADJACENCIES}->are_adjacent($sindx, $indx));
	}
	return \@{keys %nbrs};
}

sub get_in_neighbors {
	my ($self, $id) = @_;
	return [] unless $self->has_node($id);
	my %nbrs = ();
	my $indx = $self->indx_from_id($id);
	# get all nodes that have this node as a target
	foreach my $sid ($self->get_nids) {
		my $sindx = $self->indx_from_id($sid);
		$nbrs{$sid} = 1 if ($self->{ADJACENCIES}->are_adjacent($sindx, $indx));
	}
	return \@{keys %nbrs};
}

sub get_out_neighbors {
	my ($self, $sid) = @_;
	return [] unless $self->has_node($sid);
	my %tids = ();
	my $sindx = $self->indx_from_id($sid);
	# get all the target nodes for $sid
	my @tindxs = @{$self->{ADJACENCIES}->get_all_adjacent($sindx)};
	foreach my $tindx (@tindxs) {
		$tids{$self->id_from_indx($tindx)} = 1;
	}
	return \@{keys %tids};
}

sub are_neighbors {
	my ($self, $sid, $tid) = @_;
	return 0 unless ($self->has_node($sid) && $self->has_node($tid));
	my $sindx = $self->indx_from_id($sid);
	my $tindx = $self->indx_from_id($tid);
	return ($self->{ADJACENCIES}->are_adjacent($sindx, $tindx) || 
					$self->{ADJACENCIES}->are_adjacent($tindx, $sindx));
}


############################################################
# edge and edge-related methods

sub get_edge {
	my ($self, $sid, $tid) = @_;
	# for directed graphs, the edge object is stored in the s-t bin only.
	if (defined $self->{EDGES}->{$sid} && 
			defined $self->{EDGES}->{$sid}->{$tid}) {
		return $self->{EDGES}->{$sid}->{$tid};
	} else {
		return undef;
	}
}

sub get_edges {
	my ($self, $sid) = @_;
	my $edges = {};
	if (defined $sid) {
		# for a single node, get just the ids of the adjacent (target) nodes
		$edges->{$sid} = {};
		foreach my $tid (@{$self->get_neighbors($sid)}) {
			$edges->{$sid}->{$tid} = $self->get_edge($sid, $tid);
		}
	} else {
		# for the entire graph, get all node pairs that are joined by edges
		foreach my $nid ($self->get_nids) {
			my @tids = @{$self->get_neighbors($nid)};
			next unless (scalar @tids > 0);
			foreach my $tid (@tids) {
				# just add the edge; s->t and t->s are distinct in directed graphs
				$edges->{$nid} = {} unless (exists $edges->{$nid});
				$edges->{$nid}->{$tid} = $self->get_edge($nid, $tid);
			}
		}
	}
	return $edges;
}

sub set_edge {
	my ($self, $sid, $tid, $edge) = @_;
	# edge must be recorded in order to add an edge object for it
	return 0 unless ($self->has_edge($sid, $tid));
	# add the edge object to the EDGES table
	$self->{EDGES}->{$sid} = {} unless exists $self->{EDGES}->{$sid};
	$self->{EDGES}->{$sid}->{$tid} = $edge;
	# changing an edge requires recalculating the shortest paths in the graph
	$self->{SHORTESTPATHS} = {};
	return 1;
}

sub set_edge_wt {
	my ($self, $sid, $tid, $wt) = @_;
	return 0 unless ($self->has_edge($sid, $tid));
	my $edge;
	# in an undirected graph, the edge object could be stored under s-t or t-s
	# so we look for it in both places
	if (defined $self->{EDGES}->{$tid} && 
			defined $self->{EDGES}->{$tid}->{$sid}) {
		# only change if the new wt != the old wt
		# prevents recalcuating any shortest paths unless absolutely necessary
		unless ($wt == $self->{EDGES}->{$tid}->{$sid}->weight) {
			$self->{EDGES}->{$tid}->{$sid}->weight($wt);
			$self->{SHORTESTPATHS} = {};
		}
	} elsif (defined $self->{EDGES}->{$sid} && 
			defined $self->{EDGES}->{$sid}->{$tid}) {
		# only change if the new wt != the old wt
		# prevents recalcuating any shortest paths unless absolutely necessary
		unless ($wt == $self->{EDGES}->{$sid}->{$tid}->weight) {
			$self->{EDGES}->{$sid}->{$tid}->weight($wt);
			$self->{SHORTESTPATHS} = {};
		}
	} else {
		# if no edge object exists, we create a new one to store the weight
		$self->{EDGES}->{$sid} = {} unless exists $self->{EDGES}->{$sid};
		my $edge = new HPI::Graph::Edge($sid, $tid);
		unless ($wt == $edge->weight) {
			# changing an edge requires recalculating the shortest paths in the graph
			$edge->weight($wt);
			$self->{SHORTESTPATHS} = {};
		}
		$self->{EDGES}->{$sid}->{$tid} = $edge;
	}
}


sub add_edge {
	my ($self, $sid, $tid, $snode, $tnode, $edge) = @_;
	# do nothing if an s-t edge already exists
	return 0 if $self->has_edge($sid, $tid);
	
	# get index of source node, by adding it to the graph if necessary
	my $sindx;
	if ($self->has_node($sid)) {
		$sindx = $self->indx_from_id($sid);
	} else {
		defined $snode ?
				$sindx = $self->add_node($sid, $snode) : 
				$sindx = $self->add_node($sid);
	}
			
	# get index of target node, by adding it to the graph if necessary
	my $tindx;
	if ($self->has_node($tid)) {
		$tindx = $self->indx_from_id($tid);
	} else {
		defined $snode ?
				$tindx = $self->add_node($tid, $tnode) : 
				$tindx = $self->add_node($tid);
	}
	
	# add t to the adjacency list for s.
	$self->{ADJACENCIES}->make_adjacent($sindx, $tindx);
	# increment the edge count
	$self->{EDGECOUNT}++;
	# since we've changed the AM, reset the eignevector if it exists.
	$self->{EIGENVECTOR} = [];
	
	# if adding this edge changes the max degree of the graph, store the new max 
	$self->{MAXDEGREE} = $self->{ADJACENCIES}->count_adjacent($sindx) if ($self->{ADJACENCIES}->count_adjacent($sindx) > $self->{MAXDEGREE});
	$self->{MAXDEGREE} = $self->{ADJACENCIES}->count_adjacent($tindx) if ($self->{ADJACENCIES}->count_adjacent($tindx) > $self->{MAXDEGREE});

	# store the edge object, either the given obj or a new one
	# the set_edge method makes sure we don't store duplicate edge objects
	$edge = new HPI::Graph::Edge($sid, $tid) unless defined $edge;
	$self->set_edge($sid, $tid, $edge);
	
	return 1;
}


sub del_edge {
	my ($self, $sid, $tid) = @_;
#		print "trying to del edge $sid -> $tid\n";
	if (exists $self->{EDGES}->{$sid} && exists $self->{EDGES}->{$sid}->{$tid}) {
		my $sindx = $self->id_from_indx($sid);
		my $tindx = $self->id_from_indx($tid);
		$self->{ADJACENCIES}->del($sindx, $tindx);
#		print "found edge $sid -> $tid to delete!\n";
	}
}


sub subgraph {
	my ($self, $nodearr) = @_;
#	print "begin subgraph\n";

	my $subgraph = new HPI::Graph::DirectedGraph;
	
	# hash the nodeset, for faster lookup in the for loop below
	my $nodeids = {};
	foreach my $id (@$nodearr) {
		$nodeids->{$id} = 1;
	}
	# build the subgraph
	# contains all nodes in the node set, and all edges that stay within the set
	foreach my $sid (@$nodeset) {
		my $sindx = $self->id_from_indx($sid);
		next if ($sindx == -1);
#		print "$sid : $sindx\n";
		my $s = $self->get_node($sid);
		# add the source node to the subgraph
		$subgraph->add_node($sid, $s);
		my @adjlist = @{$self->{ADJACENCIES}->get_all_adjacent($sindx)};
#		print "$sid AM: @adjlist\n";
		foreach my $tindx (@adjlist) {
			# look at each edge incident on the source node
			my $tid = $self->id_from_indx($tindx);
			if (exists $nodeids->{$tid}) {
				# if the target node is also in ingraph, add the edge to ingraph
				my $t = $self->get_node($tid);
				my $e  = $self->get_edge($sid, $tid);
				$subgraph->add_edge($sid, $tid, $s, $t, $e);
			}
		}
	}
#	print "end subgraph\n";
	return $subgraph;
}


sub bfs {
	my ($self, $sid) = @_;
	return undef unless $self->has_node($sid);
	
	my $component = new HPI::Graph::DirectedGraph;
	
	# @found holds 0/1 for every node. nodes that have been 'discovered' 
	# by the component building loop are given a 1.
	my @found = ();
	for (0 .. $self->nodecount-1) {
		push @found, 0;
	}
	$found[$self->id_from_indx($sid)] = 1;
	
	# $layers is an arrayref of arrayrefs of node ids.
	my $layers = [[]];
	my $lcount = 0;
	$layers->[$lcount] = [$sid];

	while (scalar(@{$layers->[$lcount]}) > 0) {
#		printf "adding new layer %s\n", $lcount+1;
		$layers->[$lcount+1] = [];
		foreach my $uid (@{$layers->[$lcount]}) {
			my $u = $self->get_node($uid);
			$component->add_node($uid, $u);
			my $nbr_indxs = $self->{ADJACENCIES}->get_all_adjacent($self->id_from_indx($uid));
			foreach my $vindx (@$nbr_indxs) {
				unless ($found[$vindx]) {
					my $vid = $self->id_from_indx($vindx);
					my $v = $self->get_node($vid);
					my $e = $self->get_edge($uid, $vid);
#					print "did not find $v\n";
					$found[$vindx] = 1;
					push @{$layers->[$lcount+1]}, $vid;
					$component->add_edge($uid, $vid, $u, $v, $e);
				}
			}
		}
		$lcount++;
#		printf "layer is now $lcount and has length %s\n", scalar(@{$layers->[$lcount]});
	}
	
	return $component;
}


sub mst {
	my $self = shift;
	my $mst = new HPI::Graph::DirectedGraph;
	
	# sort edges by increasing cost (weight)
	my @edges = @{$self->get_edge_weights};
	my @edgestack = sort { $a->{"w"} <=> $b->{"w"} } @edges;
#	foreach my $e (@edgestack) {
#		printf "edge: %s->%s (%s)\n", $e->{"s"}, $e->{"t"}, $e->{"w"};
#	}
	
	# use Kruskal's Algorithm to build the tree
	my $uf = new HPI::Data::UnionFind;
	$uf->init($self->get_node_ids);
	foreach my $e (@edgestack) {
		my $a = $uf->find($e->{"s"});
		my $b = $uf->find($e->{"t"});
		unless ($a eq $b) {
			# merge the components
			$uf->union($a, $b);
			# add the edge to the mst
			my $s = $self->get_node($e->{"s"});
			my $t = $self->get_node($e->{"s"});
			my $eo = $self->get_edge($e->{"s"}, $e->{"t"});
			$mst->add_edge($e->{"s"}, $e->{"t"}, $s, $t, $eo);
		}
	}
	return $mst;
}


sub kcluster {
	my ($self, $k) = @_;

	# get the mst
	my $mst = $self->mst;
	
	# sort edges by decreasing cost (weight)
	my @edges = @{$mst->get_edge_weights};
	my @edgestack = sort { $b->{"w"} <=> $a->{"w"} } @edges;
	
	my $step = 0;
	foreach my $e (@edgestack) {
#		print "$k : $step\n";
		last if ($step == $k-1);
		$mst->del_edge($e->{"s"}, $e->{"t"});
		$step++;
	}
	return $mst->get_all_components;
}




sub are_connected {
	my ($self, $sid, $tid) = @_;
	# only need to calculate the paths if they haven't been stored yet
	$self->shortest_paths unless defined $self->{SHORTESTPATHS}->{"path matrix"};
	my $sindx = $self->indx_from_id($sid);
	my $tindx = $self->indx_from_id($tid);
	return ($self->{SHORTESTPATHS}->{"path matrix"}->[$sindx]->[$tindx] != inf);
}

sub k_centrality {
	my ($self, $v) = @_;
	my $cent = {"in" => 0, "out" => 0};
	my $n = $self->nodecount;
	if (defined $v) {
		# get the indegree and outdegree centrality of the given node $v
		$cent->{"in"}  = ($self->indeg($v))/($n-1);
		$cent->{"out"} = ($self->outdeg($v))/($n-1);
	} else {
		# get the indegree and outdegree centrality of the entire graph
		my $kvstar = $g->maxdeg;
		my $insum  = 0;
		my $outsum = 0;
		foreach my $v ($self->get_nids) {
			my $kv = $self->k_centrality($v);
			$insum  += $kvstar - $kv->{"in"};
			$outsum += $kvstar - $kv->{"out"};
		}
		$cent->{"in"}  = $insum/($n-2);
		$cent->{"out"} = $outsum/($n-2);
	}
	return $cent;
}



1;
