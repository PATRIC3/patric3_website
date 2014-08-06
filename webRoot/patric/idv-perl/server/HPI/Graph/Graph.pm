package HPI::Graph::Graph;

use lib "../../CPAN";
use HTML::Entities;
use Data::Table;
use Switch;

use HPI::Tools;
use HPI::Data::AdjacencyList;
use HPI::Data::PriorityQueue;
use HPI::Data::UnionFind;

use HPI::Graph::Node;
use HPI::Graph::Edge;

use base 'HPI::Graph::Element';

sub new {
	my $class = shift;
	my %opts = @_;
	my $self = new HPI::Graph::Element("graph");
	$self->{NODE_INDICES}  				= {};
	$self->{NODES}         				= {};
	$self->{NODE_IDS}      				= [];
	$self->{NODECOUNT}     				= 0;
	$self->{ADJACENCIES}   				= new HPI::Data::AdjacencyList;
	$self->{ADJACENCIES_DIRECTED} = new HPI::Data::AdjacencyList;
	$self->{EDGES}         				= {};
	$self->{EDGECOUNT}     				= 0;
	$self->{MAXDEGREE}     				= 0;
	$self->{SHORTESTPATHS} 				= {};
	$self->{EIGENVECTOR}   				= [];
		
	bless($self, $class);
	
	foreach $k (keys %opts) {
		if ($k eq "id") {
			$self->setid($opts{$k});
		} else {
			$self->setprop($k, $opts{$k});
		}
	}
	return $self;
}

sub decompose {
	my ($self, $filter) = @_;
	# filter is a hash that defines the decomposition:
	#
	# $filter->{"nodes"} : required : prop, val pairs that define the nodes that 
	#																	will be included in the decomposition
	#
	# $filter->{"nbrs"} : optional : prop, val pairs that define the neighbors 
	#																 that will be used to join nodes in the 
	#                                decomposition. if missing, all neighbors 
	#                                will be used. (IOW, any neighbor will be 
	#																 considered a valid connector.)
	#
	my $decomp = new HPI::Graph::Graph(id=>"decomposition of " . $self->getid);

=cut
	foreach my $nid (@{$self->get_nids}) {
		printf "node %s props: ", $nid;
		my $node = $self->get_node($nid);
		foreach my $prop (@{$node->listprops}) {
			printf "$prop='%s' : ", $node->getprop($prop);
		}
		print "\n";
	}
	return $decomp;
=cut
	
	# get all nodes from the source graph that we want to try to join
	my %nidhash = ();
	foreach my $prop (keys %{$filter->{"nodes"}}) {
		my $npart = $self->get_nids($prop => $filter->{"nodes"}->{$prop});
		foreach my $nid (@{$npart->{"ingroup"}}) {
			$nidhash{$nid} = 1;
		}
	}
	my @nids = keys %nidhash;
	
	# loop through all possible node pairs in our set of interest
	for (my $i=0; $i <scalar(@nids); $i++) {
		my $nid1 = $nids[$i];
		for (my $j=$i+1; $j<scalar(@nids); $j++) {
			my $nid2 = $nids[$j];
			# get the common neighbors of the two nodes
			my @pair = ($nid1, $nid2);
			my $intersection = $self->intersection(\@pair);
			my @nbrids = keys %$intersection;
			next if (scalar(@nbrids) < 3);
			# if the two nodes intersect in the source graph, add them to the decomposition
			print "intersection of $nid1 - $nid2: " . @nbrids  . "\n";

			# get all of the original edges
			my @edges = ();
			foreach my $nbrid (@nbrids) {
				# skip if the neighbor is one of the two target nodes themselves
				next if ($self->indx_from_id($nbrid) == $self->indx_from_id($nid1) || 
								 $self->indx_from_id($nbrid) == $self->indx_from_id($nid2));
				if ($filter->{"nbrs"}) {
					my $match = 0;
					my $nbr = $self->get_node($nbrid);
					FILTER:
					foreach my $fprop (keys %{$filter->{"nbrs"}}) {
						my $fval = $filter->{"nbrs"}->{$fprop};
						if ($nbr->hasprop($fprop, $fval)) {
							$match++;
							last FILTER;
						}
					}
					next unless $match;
				}
				my $nbr_edge1 = $self->get_edge($nid1, $nbrid);
				push @edges, $nbr_edge1;

				my $nbr_edge2 = $self->get_edge($nid2, $nbrid);
				push @edges, $nbr_edge2;
			}
			# if there is no filter on the nieghbors, @edges will contain at least 
			# two edges (node1<->nbr<->node2). this is to account for a filter that 
			# invalidates all n1-nbr-n2 connections.
			next unless (scalar(@edges) > 0);
=cut
=cut

			# add the nodes to the decomposition
			my $node1;
			if ($decomp->has_node($nid1)) {
				$node1 = $decomp->get_node($nid1);
			} else {
				$node1 = $self->get_node($nid1);
				$decomp->add_node($nid1, $node1);
			}
			my $node2;
			if ($decomp->has_node($nid2)) {
				$node2 = $decomp->get_node($nid2);
			} else {
				$node2 = $self->get_node($nid2);
				$decomp->add_node($nid2, $node2);
			}
			
			# add the edge to the decomposition
			my $edge;
			if ($decomp->has_edge($nid1, $nid2)) {
				$edge = $decomp->get_edge($nid1, $nid2);
			} else {
				$edge = new HPI::Graph::Edge($nid1, $nid2);
				$decomp->add_edge($nid1, $nid2, $node1, $node2, $edge);
			}
			
			# add the original edges to the decomposition edge
			foreach my $e (@edges) {
				$decomp->get_edge($nid1, $nid2)->add_edge($e);
			}
		}
	}
	return $decomp;
}

sub fill {
	my ($self, $nodes, $edges) = @_;
	
	$self->empty;
#	print $nodes->header . "\n";
	
	# make sure the bare minimum formatting exists
	return -1 if ($nodes->colIndex("id") == -1 || 
								$edges->colIndex("source") == -1 || 
								$edges->colIndex("target") == -1);
	# strip out any duplicate table entries
	$nodes = $self->_rmdups($nodes, ["id"]);
	$edges = $self->_rmdups($edges, ["source", "target"]);
	
	# an HPI node table uses int ids and string user-supplied ids. if the uid column 
	# doesn't exist, we just assume the id column contains the unique id strings
	my $nuid = "uid";
	$nuid = "id" if ($nodes->colIndex("uid") == -1);
	
	# get the data type for each column in the edge table
	my %etypes = ();
#	print "edge header has columns:\n";
	foreach my $col ($edges->header) {
#		print " $col";
		next if ($col eq "source" || $col eq "target");
		$etypes{$col} = $edges->elm(0, $col);
	}
#	print "\n";

	# set up a hash for looking up node rows by id
	my $noderows = {};
	for (my $i=1; $i<$nodes->nofRow; $i++) {
		my $indx = $nodes->elm($i, "id");
		$noderows->{$indx} = $i;
		my $nid = $nodes->elm($i, $nuid);
		my $node = new HPI::Graph::Node($nid);
		foreach my $col ($nodes->header) {
			next if ($col eq "id" || $col eq $nuid);
			$node->setprop($col, $nodes->elm($i, $col), $nodes->elm(0, $col));
		}
		$self->add_node($nid, $node);
	}
	
	# set up node and edge objects and add to graph
	for (my $i=1; $i<$edges->nofRow; $i++) {
		my $sindx = $edges->elm($i, "source");
		my $sid = $nodes->elm($noderows->{$sindx}, $nuid);
		my $snode = $self->get_node($sid);
		
		my $tindx = $edges->elm($i, "target");
		my $tid = $nodes->elm($noderows->{$tindx}, $nuid);
		my $tnode = $self->get_node($tid);
		
		next if ($self->has_edge($sid, $tid));
				
		my $edge = new HPI::Graph::Edge($sid, $tid);
		foreach my $col ($edges->header) {
			next if ($col eq "source" || $col eq "target");
			if ($col eq "wt" || $col eq "weight") {
				$edge->weight($edges->elm($i, $col));
			} else {
				$edge->setprop($col, $edges->elm($i, $col), $edges->elm(0, $col));
			}
		}
		$self->add_edge($sid, $tid, $snode, $tnode, $edge);
	}
	
	return $self->nodecount;
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
	$self->{CHILDREN}  		= {};
	$self->{TAGS}     		= {};
	$self->{PROPS}     		= {};
	$self->{PROPTYPES} 		= {};
}

sub directed {
	my $self = shift;
	return 0;
}

sub is_tree {
	my $self = shift;
	return ((scalar @{$self->get_all_components} == 1) && ($self->has_cycle == 0));
}

sub is_forest {
	my $self = shift;
	my $forest = 1;
	foreach my $c ($self->get_all_components) {
		if ($c->has_cycle) {
			$forest = 0;
			last;
		}
	}
	return $forest;
}

sub has_cycle {
	my $self = shift;
	my $clist = $self->get_all_components;
	foreach my $c (@$clist) {
#		$c->print_pretty;
		my $t = $c->dfs;
#		$t->print_pretty;
		return 1 if ($t->edgecount < $c->edgecount);
	}
	return 0;
}



############################################################
# node and node-related methods

sub nodecount {
	my $self = shift;
	return $self->{NODECOUNT};
}

sub indx_from_id {
	my ($self, $id) = @_;
	$self->has_node($id) ? return $self->{NODE_INDICES}->{$id} : return undef;
}

sub id_from_indx {
	my ($self, $indx) = @_;
	($indx < $self->nodecount) ? 
				return $self->{NODE_IDS}->[$indx] : 
				return undef;
}

sub has_node {
	my ($self, $id) = @_;
	# look up this id in the node hash
	return exists $self->{NODE_INDICES}->{$id};
}

sub get_node {
	my ($self, $id) = @_;
	(exists $self->{NODES}->{$id}) ? 
				return $self->{NODES}->{$id} : 
				return undef;
}

sub set_node {
	my ($self, $id, $node) = @_;
	return 0 unless ($self->has_node($id));
	$self->{NODES}->{$id} = $node;
	return 1;
}

sub add_node {
	my ($self, $id, $node) = @_;
	# don't add a duplicate
	return $self->{NODE_INDICES}->{$id} if ($self->has_node($id));
	# otherwise add the node to the end of the node array
	my $len = push @{$self->{NODE_IDS}}, $id;
	# add the node, node_indx pair to the hash for fast lookups
	$self->{NODE_INDICES}->{$id} = $len-1;
	$node = new HPI::Graph::Node($id) unless defined $node;
	$self->set_node($id, $node);

	# add a new adjacency list for the node
	$self->{ADJACENCIES}->add;	
	$self->{ADJACENCIES_DIRECTED}->add;	
	# increment the node count
	$self->{NODECOUNT}++;
	# return the index of the new node
	return $len-1;
}

sub add_nodes {
	my ($self, $nodes) = @_;
	foreach my $node (@$nodes) {
		# don't add a duplicate
		next if ($self->has_node($node->id));
		# otherwise add the node id to the end of the node array
		my $len = push @{$self->{NODE_IDS}}, $node->id;
		# add the node, node_indx pair to the hash for fast lookups
		$self->{NODE_INDICES}->{$node->id} = $len-1;
		$self->set_node($node->id, $node);
		# add a new adjacency list for the node
		$self->{ADJACENCIES}->add;	
		$self->{ADJACENCIES_DIRECTED}->add;	
		# increment the node count
		$self->{NODECOUNT}++;
	}
	return $self->nodecount;
}

sub get_nids {
	my $self = shift;
	my %filter = @_;
#	print @{$self->{NODE_IDS}} . "\n";
	!%filter && return $self->{NODE_IDS};
#	print $self->{NODE_IDS} . "\n";
#	print "begin node partitioning...\n";
	my @ingroup  = ();
	my @outgroup = ();
	NODE:
	foreach my $id (@{$self->{NODE_IDS}}) {
		my $fail = 0;
		my $node = $self->get_node($id);
		# if the node has no props, it can not pass the filter
		$fail = 1 unless scalar(@{$node->listprops} > 0);
#		print "hit the prop scanner\n";
		PROP:
		foreach my $prop (keys %filter) {
			my $val = $filter{$prop};
#			print "scanning $prop and $val\n";
			# in order for a node to pass the filter, BOTH of the following must be 
			# true:
			#   1. the node has a property with this name
			#   2. the value of that property matches the given value
			# the hasprop() method of the Node class only returns true if both of the 
			# above are true
			unless ($node->hasprop($prop, $val)) {
				$fail = 1;
				last PROP;
			}
		}
		$fail ? push(@outgroup, $id) : push(@ingroup, $id);
	}
#	print "ingroup:\n@ingroup\n";
#	print "outgroup:\n@outgroup\n";
	my $r = {"ingroup" => \@ingroup, "outgroup" => \@outgroup};
#	print "end node partitioning...\n";
	return $r;
}

sub maxdeg {
	my $self = shift;
	return $self->{MAXDEGREE};
}

sub get_nids_by_degree {
	my ($self, $k, $comp) = @_;
	$comp = "eq" unless defined $comp;
	my @nodelist = ();
	foreach my $id (@{$self->{NODE_IDS}}) {
		my $indx = $self->indx_from_id($id);
		switch ($comp) {
			case "eq" { push(@nodelist, $nid) if ($self->deg($id) == $k) }
			case "ne" { push(@nodelist, $nid) if ($self->deg($id) != $k) }
			case "lt" { push(@nodelist, $nid) if ($self->deg($id) < $k) }
			case "le" { push(@nodelist, $nid) if ($self->deg($id) <= $k) }
			case "gt" { push(@nodelist, $nid) if ($self->deg($id) > $k) }
			case "ge" { push(@nodelist, $nid) if ($self->deg($id) >= $k) }
			else { push(@nodelist, $nid) if ($self->deg($id) == $k) }
		}
	}
}

sub sort_nids_by_degree {
	my ($self, $order, $limit) = @_;
	return undef unless defined $order;
	
	# put the node degrees in an unsorted hash keyed by node id
	my %unsorted = ();
	foreach my $nid (@{$self->get_nids}) {
		my $k = $self->deg($nid);
		$unsorted{$nid} = $k;
	}
	
	# sort hash by value and store sorted node ids in an array
	my @sortedids;
	if ($order eq "a" || $order eq "ascending" || $order eq "u" || $order eq "up") {
		@sortedids = sort { $unsorted{$a} <=> $unsorted{$b} } keys %unsorted;
	} else {
		@sortedids = sort { $unsorted{$b} <=> $unsorted{$a} } keys %unsorted;
	}
	
	# build the table of sorted nodes and their degrees
	# if a limit is specified, we may sometimes return more than the limit 
	# because we want to keep any ties (k1 = k2).
	my $table = new Data::Table([], ["node", "k"], 1);
	my $stop  = scalar(@sortedids);
	$stop = $limit if (defined $limit);
	
	my $count =  0;
	my $held  =  1;
	my $lastk = -1;
	for ($i=0; $i<scalar(@sortedids); $i++) {
		my $nid = $sortedids[$i];
		my $k = $unsorted{$nid};
		if ($lastk == $k) {
			$held++;
		} else {
			$count += $held;
			$held = 1;
			$lastk = $k;
		}
#		print "count $count : held $held : stop $stop\n";
		last unless ($count < $stop);
		$table->addRow([$nid, $k]);
	}
	return $table;
}

sub get_node_degree_dist {
	my $self = shift;
	my $dist = new Data::Table([], ["k", "count"], 1);
	my @counts = ();
	for (0..$self->maxdeg) {
		push @counts, 0;
	}
	my $nids = $self->get_nids;
	foreach my $nid (@$nids) {
		my $k = $self->deg($nid);
		my $newcount = $counts[$k] + 1;
		$counts[$k] = $newcount;
	}
	for (my $i=0; $i<scalar(@counts); $i++) {
		$dist->addRow([$i, $counts[$i]]);
	}
	return $dist;
}

sub deg {
	my ($self, $sid) = @_;
	return 0 unless $self->has_node($sid);
	return $self->{ADJACENCIES}->count_adjacent($self->indx_from_id($sid));
}

sub intersection {
	my ($self, $nids) = @_;
	my %intersection = ();
	# abort if any of the nodes in the list are not in the graph
	foreach $nid (@$nids) {
		return {} unless ($self->has_node($nid));
		$intersection{$nid} = $self->get_node($nid);
	}
	# build a list of all nodes that neighbor the given node set
	# this is the maximum possible intersecting set
	my @maxcover = ();
	foreach my $nid (@$nids) {
		foreach my $nbr (@{$self->get_neighbors($nid)}) {
			push @maxcover, $nbr;
		}
	}
	# loop through the neighbors in the maximum possible intersecting set
	# save any neighbor that is adjacent to all nodes in the input set 
	foreach my $nbr (@maxcover) {
		my $exclude = 0;
		foreach my $nid (@$nids) {
			unless ($self->are_neighbors($nid, $nbr)) {
				$exclude = 1;
				last;
			}
		}
		$intersection{$nbr} = $self->get_node($nbr) unless ($exclude);
	}
#	printf "nbrs of '%s' and '%s' -> ", $nids->[0], $nids->[1];
#	foreach my $nbr (keys %intersection) {
#		print "$nbr : ";
#	}
	return \%intersection;
}



############################################################
# edge and edge-related methods

sub edgecount {
	my $self = shift;
	return $self->{EDGECOUNT};
}

sub has_edge {
	my ($self, $sid, $tid) = @_;
	# can't be an edge unless both nodes exist
	return 0 unless ($self->has_node($sid) && $self->has_node($tid));
	# test for s->t link in the AM.
	# we can get away with testing adjacency one way (s->t) because of the 
	# way we build the AM for an undirected graph (when an edge is added 
	# to the graph, we add entries in the AM for both s and t).
	return $self->are_neighbors($sid, $tid);
}

sub get_edge {
	my ($self, $sid, $tid) = @_;
	# for undirected graphs, the edge object could be stored in either s-t or t-s. 
	# so we look in both places for a defined edge object.
	if (defined $self->{EDGES}->{$sid} && 
			defined $self->{EDGES}->{$sid}->{$tid}) {
		return $self->{EDGES}->{$sid}->{$tid};
	} elsif (defined $self->{EDGES}->{$tid} && 
					 defined $self->{EDGES}->{$tid}->{$sid}) {
		return $self->{EDGES}->{$tid}->{$sid};
	} else {
		return undef;
	}
}

sub get_edges {
	my ($self, $sid) = @_;
	if (defined $sid) {
		# for a single node, get just the ids of the neighboring nodes
		my $edges = {};
		$edges->{$sid} = {};
		foreach my $tid (@{$self->get_neighbors($sid)}) {
			$edges->{$sid}->{$tid} = $self->get_edge($sid, $tid);
		}
		return $edges;
	} else {
		# for the entire graph, get all node pairs that are joined by edges
		return $self->{EDGES};
	}
}

sub get_inedges {
	my ($self, $tid) = @_;
	my $edges = {};
	my $tindx = $self->indx_from_id($tid);
	foreach my $sindx (@{$self->{ADJACENCIES_DIRECTED}->get_sources($tindx)}) {
		my $sid = $self->id_from_indx($sindx);
		$edges->{$sid} = $self->get_edge($sid, $tid);
	}
	return $edges;
}

sub set_edge {
	my ($self, $sid, $tid, $edge) = @_;
	return 0 unless ($self->has_edge($sid, $tid));
	$self->{EDGES}->{$sid} = {} unless (defined $self->{EDGES}->{$sid});
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
		# if no edge object exists, return wt of 0
		return 0;
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
	
	# add t to the adjacency list for s. also add s to the adjacency list for t.
	# this allows us to look up an edge irrespective of directionality.
	# it also makes it easier to convert an undirected into directed graph.
	# (note that the AM will contain two entries for each true edge.)
	$self->{ADJACENCIES}->make_adjacent($sindx, $tindx);
	$self->{ADJACENCIES}->make_adjacent($tindx, $sindx);
	$self->{ADJACENCIES_DIRECTED}->make_adjacent($sindx, $tindx);
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
	# only do the work if an s-t edge exists
	if ($self->has_node($sid) && $self->has_node($tid) && 
			$self->has_edge($sid, $tid)) {
		my $sindx = $self->indx_from_id($sid);
		my $tindx = $self->indx_from_id($tid);
		$self->{ADJACENCIES}->del($sindx, $tindx);
		$self->{ADJACENCIES}->del($tindx, $sindx);
		$self->{ADJACENCIES_DIRECTED}->del($sindx, $tindx);
		delete $self->{EDGES}->{$sid}->{$tid} if (exists $self->{EDGES}->{$sid} && 
																							exists $self->{EDGES}->{$sid}->{$tid});
		delete $self->{EDGES}->{$tid}->{$sid} if (exists $self->{EDGES}->{$tid} && 
																							exists $self->{EDGES}->{$tid}->{$sid});
		
		# if deleting this edge changes the max degree of the graph, store the new max
		my $max = 0;
		foreach my $nid ($self->get_nids) {
			my $deg = $self->deg($nid);
			$max = $deg if ($deg > $max);
		}
		$self->{MAXDEGREE} = $max;
		
		# update edge-dependent, pre-calculated results.
		$self->{SHORTESTPATHS} = {};
		$self->{EIGENVECTOR}   = [];
		$self->{EDGECOUNT}--;
	}
}

sub get_edge_wts {
	my $self = shift;
	my @stack = ();
	my %read  = ();
	my $edgepairs = $self->get_edges;
	foreach my $sid (keys %$edgepairs) {
#		print "sid $sid\n";
		foreach my $tid (keys %{$edgepairs->{$sid}}) {
#			print "tid $tid\n";
			# get_edge (undirected) looks in both places (s-t and t-s) for the edge 
			# object. so once we find it in one, we don't need to look in the other
			next if (exists $read{$sid} && exists $read{$tid});
			my $wt = $self->get_edge($sid, $tid)->weight;
#			print "wt $wt\n";
			push @stack, {"source" => $sid, "target" => $tid, "weight" => $wt};
			$read{$sid} = 1;
			$read{$tid} = 1;
		}
	}
	return \@stack;
}

sub sort_edges_by_wt {
	my ($self, $order, $limit) = @_;
	($order = "d") unless defined $order;
	
	# put edge weights in a hash, keyed by s->t pair
	my %unsorted = ();
	my %read = ();
	
#	print "unsorted:\n";
	SOURCE:
	for (my $sindx=0; $sindx < $self->{ADJACENCIES}->size; $sindx++) {
		my $alist = $self->{ADJACENCIES}->get_all_adjacent($sindx);
		my $sid = $self->id_from_indx($sindx);
		TARGET:
		foreach my $tindx (@$alist) {
			my $tid = $self->id_from_indx($tindx);
			next TARGET if (exists $unsorted{"$tid|$sid"});  # handles undirected edges
			my $edge = $self->get_edge($sid, $tid);
			$unsorted{"$sid|$tid"} = $edge->weight;  # keying hash with a string is not desired!
#			printf "  $sid - $tid : %s\n", $edge->weight;
		}
	}
	
	# sort edge weights hash by value
	my @sortedids;
	if ($order eq "a" || $order eq "ascending" || $order eq "u" || $order eq "up") {
		@sortedids = sort { $unsorted{$a} <=> $unsorted{$b} } keys %unsorted;
	} else {
		@sortedids = sort { $unsorted{$b} <=> $unsorted{$a} } keys %unsorted;
	}

	# build the table of sorted edges and their weights
	# if a limit is specified, we may sometimes return more than the limit 
	# because we want to keep any ties (w1 = w2).
	my $table = new Data::Table([], ["source", "target", "weight"], 1);
	my $stop  = scalar(@sortedids);
	$stop = $limit if (defined $limit);

	my $count =  0;
	my $held  =  1;
	my $lastw = -1;
#	print "sorted:\n";
	for ($i=0; $i<scalar(@sortedids); $i++) {
		my ($sid, $tid) = split /\|/, $sortedids[$i];
		my $w = $unsorted{$sortedids[$i]};
		if ($lastw == $w) {
			$held++;
		} else {
			$count += $held;
			$held = 1;
			$lastw = $w;
		}
#		print "count $count : held $held : stop $stop\n";
		last unless ($count < $stop);
#		print "  $sid - $tid : $w\n";
		$table->addRow([$sid, $tid, $w]);
	}
	return $table;
}

sub get_neighbors {
	my ($self, $id) = @_;
	# can't get neighbors on a nonexistent node
	return [] unless $self->has_node($id);
	my @nbrs = ();
	# get the index of the node
	my $indx = $self->indx_from_id($id);
	# get the adjacency list for this node
	my $nbr_indxs = $self->{ADJACENCIES}->get_all_adjacent($indx);
	# look up the node id for each index in the adjacency list
	foreach my $nbr_indx (@$nbr_indxs) {
		push @nbrs, $self->id_from_indx($nbr_indx);
	}
	#print "nbrs of $id are: @nbrs\n";
	return \@nbrs;
}

sub get_neighbors_as_hash {
	my ($self, $id) = @_;
	# can't get neighbors on a nonexistent node
	return {} unless $self->has_node($id);
	my $nbrs = {};
	# get the index of the node
	my $indx = $self->indx_from_id($id);
	# get the adjacency list for this node
	my $nbr_indxs = $self->{ADJACENCIES}->get_all_adjacent($indx);
	# look up the node for each index in the adjacency list
	foreach my $nbr_indx (@$nbr_indxs) {
		$nbrs->{$self->id_from_indx($nbr_indx)} = 1;
	}
#	print "nbrs of $id are: ", keys %$nbrs\n";
	return $nbrs;
}

sub are_neighbors {
	my ($self, $sid, $tid) = @_;
	return 0 unless ($self->has_node($sid) && $self->has_node($tid));
	my $sindx = $self->indx_from_id($sid);
	my $tindx = $self->indx_from_id($tid);
	return $self->{ADJACENCIES}->are_adjacent($sindx, $tindx);
}

sub connect_all {
	my ($self, $nids) = @_;
	for (my $i=0; $i<scalar(@$nids); $i++) {
		next unless ($self->has_node($nids->[$i]));
		for (my $j=$i; $j<scalar(@$nids); $j++) {
			next unless ($self->has_node($nids->[$j]));
			$self->add_edge($nids->[$i], $nids->[$j]) 
				unless ($self->has_edge($nids->[$i], $nids->[$j]));
		}
	}
}



############################################################
# Graph analysis and related routines

sub are_connected {
	my ($self, $sid, $tid) = @_;
	my @cnode_ids = @{$self->get_component($sid)->get_nids};
#	print "$sid, $tid\n";
#	print "@cnode_ids\n";
	foreach my $cid (@cnode_ids) {
		return 1 if ($cid eq $tid);
	}
	return 0;
}

sub get_component {
	my ($self, $id) = @_;
	my $bfs = $self->bfs($id);
	return $self->subgraph($bfs->get_nids);
}

sub get_all_components {
	my $self = shift;
	my @components = ();
	my @nodeids = @{$self->get_nids};
	while (scalar(@nodeids) > 0) {
#		printf "%s nodes left in the graph\n", scalar(@nodes);
		my $c = $self->get_component($nodeids[0]);
#		printf "found a component of size %s using node %s\n", $c->nodecount, $nodeids[0];
		push @components, $c;
		# remove nodes in c from the nodes array
		my @newarr = ();
		GRAPH:
		foreach my $id (@nodeids) {
			my $found = 0;
			COMP:
			foreach my $cid (@{$c->get_nids}) {
				if ($cid eq $id) {
					$found = 1;
					last COMP;
				}
			}
			push(@newarr, $id) unless $found;
		}
		@nodeids = @newarr;
	}
	return \@components;
}

sub merge {
	my ($self, $g2) = @_;
	my $stats = {"nodes_before_merge" => $self->nodecount, 
							 "edges_before_merge" => $self->edgecount, 
							 "nodes_in_ingraph" => $g2->nodecount, 
							 "edges_in_ingraph" => $g2->edgecount, 
							 "nodes_after_merge" => $self->nodecount, 
							 "edges_after_merge" => $self->edgecount };
	
	foreach my $id (@{$g2->get_nids}) {
		my $node = $g2->get_node($id);
		$self->add_node($id, $node);
	}
	my $edges = $g2->get_edges;
	foreach my $sid (keys %$edges) {
		my $tids = $edges->{$sid};
		foreach my $tid (keys %$tids) {
			my $snode = $g2->get_node($sid);
			my $tnode = $g2->get_node($tid);
			my $edge  = $g2->get_edge($sid, $tid);
			$self->add_edge($sid, $tid, $snode, $tnode, $edge);
		}
	}
	$stats->{"nodes_after_merge"} = $self->nodecount;
	$stats->{"edges_after_merge"} = $self->edgecount;
	
	if ($stats->{"nodes_after_merge"} == $stats->{"nodes_before_merge"} && 
			$stats->{"edges_after_merge"} == $stats->{"edges_before_merge"}) {
		return stats;
	} else {
		return 0;
	}
}

sub cut {
	my ($self, $nodeset) = @_;
#	print "begin cut\n";	
	my $nodeids = {};
	foreach my $id (@$nodeset) {
		$nodeids->{$id} = 1;
	}
	my $cut = {};
	
	SRC:
	foreach my $sid (@$nodeset) {
		next unless $self->has_node($sid);
		my $adjlist = $self->{ADJACENCIES}->get_all_adjacent($self->indx_from_id($sid));
		TARG:
		foreach my $tindx (@$adjlist) {
			# look at each edge incident on the source node
			my $tid = $self->id_from_indx($tindx);
			unless (exists $nodeids->{$tid}) {
				# if the target node is not in the nodeset, add the edge to the cut
				# if it is in the nodeset, s->t is not a cut
				$cut->{$sid} = {} unless exists $cut->{$sid};
				$cut->{$sid}->{$tid} = $self->get_edge($sid, $tid);
			}
		}
	}

#	foreach my $sid (keys %$cut) {
#		foreach my $tid (keys %{$cut->{$sid}}) {
#			printf "added cut edge $sid : $tid : %s\n", $cut->{$sid}->{$tid};
#		}
#	}
#	print "end cut\n";

	# returns hash of hashes {$sid1=>{$tid1=>edge1-1,$tid2=>edge1-2,...}, $sid2=>...}
	return $cut;
}

sub subgraph {
	my ($self, $nodeset) = @_;
#	print "begin subgraph...\n";
	my $subgraph = new HPI::Graph::Graph(id=>"subgraph");
	# hash the nodeset, for faster lookup in the for loop below
	my $nodeids = {};
#	print "  nodeset:";
	foreach my $id (@$nodeset) {
		$nodeids->{$id} = 1;
#		print " $id";
	}
#	print "\n";
	# build the subgraph
	# contains all nodes in the node set, and all edges that stay within the set
	foreach my $sid (keys %$nodeids) {
		next unless ($self->has_node($sid));
#		print "$sid : $sindx\n";
		my $s = $self->get_node($sid);
		my @nbrs = @{$self->get_neighbors($sid)};
#		print "  $sid AL:";
		foreach my $tid (@nbrs) {
			next unless ($self->has_node($tid));
#			print " $tid";
			# look at each edge incident on the source node
			if (exists $nodeids->{$tid}) {
				# if the target node is also in the nodeset, add the edge to the subgraph
				my $t = $self->get_node($tid);
				my $e  = $self->get_edge($sid, $tid);
				$subgraph->add_node($sid, $s) unless ($subgraph->has_node($sid));
				$subgraph->add_node($tid, $t) unless ($subgraph->has_node($tid));
				$subgraph->add_edge($sid, $tid, $s, $t, $e);
			}
		}
#		print "\n";
	}
#	printf "  subgraph contains %s nodes and %s edges\n", $subgraph->nodecount, $subgraph->edgecount;
#	print "end subgraph.\n";
	return $subgraph;
}

sub bfs {
	my ($self, $id) = @_;
	return undef unless $self->has_node($id);
	
	my $component = new HPI::Graph::Graph(id=>"bfs on $id");
	
	# @found holds 0/1 for every node. nodes that have been 'discovered' 
	# by the component building loop are given a 1.
	my @found = ();
	for (0 .. $self->nodecount-1) {
		push @found, 0;
	}
	$found[$self->indx_from_id($id)] = 1;
	
	# $layers is an arrayref of arrayrefs of nodes.
	my $layers = [[]];
	my $lcount = 0;
	$layers->[$lcount] = [$id];

	while (scalar(@{$layers->[$lcount]}) > 0) {
#		printf "adding new layer %s\n", $lcount+1;
		$layers->[$lcount+1] = [];
		foreach my $uid (@{$layers->[$lcount]}) {
			my $u = $self->get_node($uid);
			my $nbr_indxs = $self->{ADJACENCIES}->get_all_adjacent($self->indx_from_id($uid));
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

sub dfs {
	my ($self, $v, $tree, $visited, $stack) = @_;
	$v = $self->id_from_indx(0) unless defined $v;
	$tree = new HPI::Graph::Graph(id=>"dfs on $v") unless defined $tree;
	$visited = {} unless defined $visited;
	$stack   = [] unless defined $stack;
	$visited->{$v} = 1;
	push @$stack, $v;
	foreach my $w (@{$self->get_neighbors($v)}) {
		$self->dfs($w, $tree, $visited, $stack) unless (exists $visited->{$w});
	}
	# backtrack and add the edge from v to the last w
	unless (scalar(@$stack) < 2) {
		my $tid = pop @$stack;
		my $sid = $stack->[scalar(@$stack)-1];
		$tree->add_edge($sid, $tid, 
										$self->get_node($sid), 
										$self->get_node($tid), 
										$self->get_edge($sid, $tid));
	}
	return $tree;
}

sub mst {
	my $self = shift;
	my $mst = new HPI::Graph::Graph(id=>"mst");
	# add all nodes to the mst (makes it a true minimum spanning forest)
	my @nodes = values %{$self->{NODES}};
	$mst->add_nodes(\@nodes);
	# sort edges by increasing cost (weight)
	my @edgewts = @{$self->get_edge_wts};
	my @edgestack = sort { $a->{"weight"} <=> $b->{"weight"} } @edgewts;
#	foreach my $e (@edgestack) {
#		printf "edge: %s->%s (%s)\n", $e->{"source"}, $e->{"target"}, $e->{"weight"};
#	}
	
	# use Kruskal's Algorithm to build the tree
	my $uf = new HPI::Data::UnionFind;
	$uf->init($self->get_nids);
	foreach my $ehash (@edgestack) {
		my $a = $uf->find($ehash->{"source"});
		my $b = $uf->find($ehash->{"target"});
		unless ($a eq $b) {
			# merge the components
			$uf->union($a, $b);
			# add the edge to the mst
			my $snode = $mst->get_node($ehash->{"source"});
			my $tnode = $mst->get_node($ehash->{"target"});
			my $edge = $self->get_edge($snode->id, $tnode->id);
			$mst->add_edge($snode->id, $tnode->id, $snode, $tnode, $edge);
		}
	}
	return $mst;
}

sub shortest_path {
	my ($self, $sid, $tid) = @_;
	return undef unless ($self->has_node($sid));
	if (defined $tid) {
		return undef unless($self->has_node($tid) && 
												$self->are_connected($sid, $tid));
	}

	# use Dijkstra's Algorithm to find the shortest path between two nodes, or 
	# between one node and all others in the graph

	# init the explored set S with the source node s, 
	# and the distance to s as 0.
	my $S = {$sid => 1};
	my $dv = 0;
	
	# get all the nodes in the component that contains s
	my $component = $self->get_component($sid);
	my $node_ids = $component->get_nids;
	# build V (G-S)
	my @V = ();
	foreach my $id (@$node_ids) {
		push(@V, $id) unless ($id eq $sid);
	}
	# init a priority queue to hold the shortest path distances from s to 
	# every node in V
	my $queue = new HPI::Data::PriorityQueue;
	$queue->init(\@V, inf);
#	$queue->pretty;
	
	# init a hash to track edges in shortest path from every node in V back to s
	my $pathedges = {};
	
	until ($queue->is_empty) {
		# investigate each node v in explored set S
		foreach my $vid (keys %$S) {
			# get the min distance of the path from s to any node w NOT in S that 
			# goes through v (v-w edge exists)
			foreach my $wid (@{$component->get_neighbors($vid)}) {
				next if (exists $S->{$wid});
				# update the priority queue if this (v,w) edge changes the length of 
				# the shortest s->w path
				my $key = $queue->get_key_by_name($wid);
				my $le  = $component->get_edge($vid, $wid)->weight;
				if (($dv + $le) < $key) {
					$queue->change_key($wid, $dv+$le);
					$pathedges->{$wid} = $vid;
				}
#				print "e($vid, $wid): key for $wid is $key. l(e) is $le and d(v) is $dv\n";
			}
		}
#		$queue->pretty;
		# extract the minimum distance from any node w NOT in S
		my ($min, $min_id) = @{$queue->extract_min_with_name};
#		print "extracted min $min ($min_id)\n";
		# add the node to S and update the distance to s
		$S->{$min_id} = 1;
		$dv = $min;
		last if (defined $tid && $min_id eq $tid);
	}
	
#	foreach my $s (keys %$pathedges) {
#		printf "shortest edge: %s, %s\n", $s, $pathedges->{$s};
#	}
	
	# reconstruct shortest path(s) to s, using the hash of shortest edges
	my $paths = {};
	if (defined $tid) {
		# only need to return the s->t path
		my @path = ($tid);
		my $id = $tid;
		while (1) {
			$id = $pathedges->{$id};
			unshift @path, $id;
			last if ($id eq $sid);
		}
		$paths->{$tid} = {"length" => $dv, "path" => \@path};
	} else {
		# return paths from all nodes to s
		foreach my $tid (keys %$pathedges) {
			my @path = ($tid);
			my $id = $tid;
			my $len = 0;
			while (1) {
				my $tmp = $id;
				$id = $pathedges->{$id};
				unshift @path, $id;
				$len += $component->get_edge($id, $tmp)->weight;
				last if ($id eq $sid);
			}
			$paths->{$tid} = {"length" => $len, "path" => \@path};
		}
	}
	return $paths;
}

sub shortest_paths {
	my $self = shift;
	# if the algorithm has already run, and the graph has not changed in the 
	# meantime, the data is cached internally and ready for use.
	return 1 if (defined $self->{SHORTESTPATHS}->{"path matrix"});
	
	# if we have to re-calculate the paths, it helps to remember that s->t and 
	# t->s are equivalent for undirected graphs.
	
	my $path = [];	# stores the shortest path lengths between all pairs of nodes
	my $next = [];	# stores the next node to visit along each shortest path
	my $n = $self->nodecount;
	
	# init path matrix with edge weights; inf if no edge between them
	# init next matrix with -1
	my ($id1, $id2);
	for my $i (0..$n-1) {
		$path->[$i] = [];
		$next->[$i] = [];
		$id1 = $self->id_from_indx($i);
		next unless defined $id1;
		for my $j (0..$n-1) {
			if ($i == $j) {
				$path->[$i]->[$j] = 0;
				$next->[$i]->[$j] = [$i];
				next;
			}
			$id2 = $self->id_from_indx($j);
			next unless defined $id2;
			$next->[$i]->[$j] = [-1];
			
			$self->has_edge($id1, $id2) ? 
					($path->[$i]->[$j] = $self->get_edge($id1, $id2)->weight) : 
					($path->[$i]->[$j] = inf);
			$id2 = undef;
		}
		$id1 = undef;
	}
	# fill the path and next matrices
	for my $k (0..$n-1) {
		for my $i (0..$n-1) {
			next if ($k == $i);
			for my $j (0..$n-1) {
				next if (($i == $j) || ($k == $j));
				if ($path->[$i]->[$k] + $path->[$k]->[$j] <= $path->[$i]->[$j]) {
					$next->[$i]->[$j] = [] unless ($path->[$i]->[$k] + $path->[$k]->[$j] == $path->[$i]->[$j]);
					push(@{$next->[$i]->[$j]}, $k);
					$path->[$i]->[$j] = $path->[$i]->[$k] + $path->[$k]->[$j];
				}
			}
		}
	}
	
=cut
	# debug
	print" path lengths:\n";
	foreach my $iarr (@$path) {
		foreach my $len (@$iarr) {
			print "$len ";
		}
		print "\n";
	}
	print" paths:\n";
	my $i = 0;
	foreach my $iarr (@$next) {
		my $j = 0;
		foreach my $jarr (@$iarr) {
			print "$i, $j [";
			foreach my $nindx (@$jarr) {
				print "$nindx ";
			}
			print "]\n";
			$j++;
		}
		print "\n";
		$i++;
	}
	# end debug
=cut

	# store the path and next matrices
	$self->{SHORTESTPATHS}->{"path matrix"}     = $path;
	$self->{SHORTESTPATHS}->{"neighbor matrix"} = $next;
	return 1;
}

sub reconstruct_shortest_paths {
	my ($self, $sid, $tid) = @_;
	# return undef if s and t are not in the same component
	return undef unless $self->are_connected($sid, $tid);
	# calculate the shortest paths if they haven't been stored yet
	$self->shortest_paths;
	# could be multiple shortest paths between two nodes
	# so our return value is an array of arrays, each of which is a path
	my $paths = [];
	my $sindx = $self->indx_from_id($sid);
	my $tindx = $self->indx_from_id($tid);
	
	foreach my $uindx (@{$self->{SHORTESTPATHS}->{"neighbor matrix"}->[$sindx]->[$tindx]}) {
		# an empty array if a shortest path between s and t is a direct edge
		# (coded as an indx of -1 in the neighbor matrix)
		if ($uindx == -1) {
			push @$paths, [];
			next;
		}
		my $uid = $self->id_from_indx($uindx);
		# recurse through this method to assemble the full path list
		my $lpaths = $self->reconstruct_shortest_paths($sid, $uid);
		my $rpaths = $self->reconstruct_shortest_paths($uid, $tid);
		foreach my $path (@$lpaths) {
			foreach my $rpath (@$rpaths) {
				push @$path, $uid;
				push @$path, @$rpath;
				push @$paths, $path;
			}
		}
	}
	return $paths;
}

sub get_shortest_paths {
	my ($self, $sid, $tid) = @_;
	# return undef if s and t are not in the same component
	return undef unless $self->are_connected($sid, $tid);
	# call to the recursive method that assembles the paths
	my $paths = $self->reconstruct_shortest_paths($sid, $tid);
	# append the start and end node ids to each path
	foreach my $path (@$paths) {
		unshift @$path, $sid;
		push @$path, $tid;
	}
	return $paths;
}

sub get_shortest_paths_thru {
	my ($self, $sid, $tid, $vid) = @_;
	my $paths = $self->get_shortest_paths($sid, $tid);
	my $paths_thru_v =[];
	PATH:
	foreach my $path (@$paths) {
		NODE:
		for my $nid (@$path) {
			if ($nid eq $vid) {
				push @$paths_thru_v, $path;
				next PATH;
			}
		}
	}
	return $paths_thru_v;
}

sub get_shortest_path_length {
	my ($self, $sid, $tid) = @_;
	# only need to calculate the paths if they haven't been stored yet
	$self->shortest_paths unless defined $self->{SHORTESTPATHS}->{"path matrix"};
	my $sindx = $self->indx_from_id($sid);
	my $tindx = $self->indx_from_id($tid);
	# return a path length of inf if no s-t path at all
	($self->{SHORTESTPATHS}->{"path matrix"}->[$sindx]->[$tindx] == inf) ? 
		return $self->{SHORTESTPATHS}->{"path matrix"}->[$tindx]->[$sindx] : 
		return $self->{SHORTESTPATHS}->{"path matrix"}->[$sindx]->[$tindx];
}

sub k_centrality {
	my ($self, $v) = @_;
	my $cent;
	my $n = $self->nodecount;
	$cent = 0;
	if (defined $v) {
		# calculate the degree centrality of the given node $v
		my $kv = $self->deg($v);
		$cent = $kv/($n-1);
	} else {
		# calculate the degree centrality of the entire graph
		my $kvstar = $self->maxdeg;
		my $sum = 0;
		foreach my $v ($self->get_nids) {
			my $kv = $self->k_centrality($v);
			$sum += $kvstar - $kv;
		}
		$cent = $sum/($n-2);
	}
	return $cent;
}

sub closeness {
	my ($self, $v) = @_;
	my $c = $self->get_component($v);
	my $n = $c->nodecount;
	# get the shortest paths from v to all other connected nodes
	my $paths = $c->shortest_path($v);
	my $sum = 0;
	foreach my $tid (keys %$paths) {
		$sum += $paths->{$tid}->{"length"};
	}
	return 1/($sum/($n-1));
}

sub betweenness {
	my ($self, $v) = @_;
	my $n = $self->nodecount;
	my $vindx = $self->indx_from_id($v);
	my $sum = 0;
	for my $sindx (0..$n-1) {
		next if ($sindx == $vindx);
		my $s = $self->id_from_indx($sindx);
		for my $tindx (0..$n-1) {
			next if ($sindx == $tindx);
			next if ($tindx == $vindx);
			my $t = $self->id_from_indx($tindx);
			my $numer = scalar @{$self->get_shortest_paths_thru($s, $t, $v)};
			my $denom = scalar @{$self->get_shortest_paths($s, $t)};
#			print "$s, $t through $v : $numer\n";
#			print "$s, $t total      : $denom\n";
			$sum += ($numer/$denom)/2;
		}
	}
	return $sum;
}

sub eigenvector_centrality {
	my ($self, $v) = @_;
	$self->set_eigenvector unless scalar @{$self->{EIGENVECTOR}};
	return $self->{EIGENVECTOR}->[$self->indx_from_id($v)];
}

sub set_eigenvector {
	my ($self, $threshold) = @_;
	my $threshold = 0.01 unless defined $threshold;
	my $n = $self->nodecount;
	# init the starting vector values to 1
	my @v = ();
	for my $i (0 .. $n-1) {
		$v[$i] = 1;
	}
	# track the number of iterations
	my $step = 0;
	# trigger to exit the algorithm
	my $continue = 1;
	while ($continue) {
		my @b = ();
		for my $i (0 .. $n-1) {
			for my $j (0 .. $n-1) {
				$b[$i] += $v[$j] + 1 if ($self->{ADJACENCIES}->are_adjacent($j, $i));
			}
		}
		my $norm = 0;
		for my $k (0 .. $n-1) {
			$norm += $b[$k];
		}
		last if ($norm == 0);
		$continue = 0;
		for my $p (0 .. $n-1) {
			my $newval = $b[$p]/$norm;
			# if any value is still above the threshold, the algorithm continues
			$continue = 1 if (abs($v[$p] - $newval)/$v[$p] > $threshold);
			$v[$p] = $newval;
		}
		$step++;
	}
	$self->{EIGENVECTOR} = \@v;
	return $step;
}

sub print_pretty {
	my $self = shift;
	printf "%s\n", $self->getid;
	print "  NODES\n";
	foreach my $nid (@{$self->{NODE_IDS}}) {
		print "  $nid\n";
	}
	print "  EDGES:\n";
	foreach my $sid (keys %{$self->{EDGES}}) {
		foreach my $tid (keys %{$self->{EDGES}->{$sid}}) {
			print "  $sid-$tid\n";
		}
	}
}

sub get_nodetable {
	my $self = shift;
	my $table;
	my ($header, $types) = [];
	my $hdrhash = {};
	my $init = 1;
	push @$header, "id", "uid";
	$hdrhash->{"id"} = 0;
	$hdrhash->{"uid"} = 1;
	push @$types, "int", "string";
	foreach my $sid (@{$self->{NODE_IDS}}) {
		my $node = $self->get_node($sid);
		next unless (defined $node);
		if ($init) {
			my $incr = 2;
			for my $prop (@{$node->listprops}) {
				push @$header, $prop;
				$hdrhash->{$prop} = $incr;
				push @$types, $node->get_type($prop);
				$incr++;
			}
			$table = new Data::Table([], $header, 1);
			$table->addRow($types);
#			printf "[%s]\n", join(" | ", @$header);
#			printf "[%s]\n", join(" | ", @$types);
			$init = 0;
		}
		my $vals = [$self->indx_from_id($sid), $sid];
		for my $prop (@{$node->listprops}) {
			$vals->[$hdrhash->{$prop}] = $node->getprop($prop);
		}
#		printf "[%s]\n", join(" | ", @$vals);
		$table->addRow($vals);
	}
	return $table;
}

sub get_edgetable {
	my $self = shift;
	my $table;
	my ($header, $types) = [];
	my $hdrhash = {};
	push @$header, "source", "target", "weight";
	$hdrhash->{"source"} = 0;
	$hdrhash->{"target"} = 1;
	$hdrhash->{"weight"} = 2;
	push @$types, "int", "int", "float";
	my $init = 1;
	my $edgehash = $self->get_edges;
	foreach my $sid (keys %$edgehash) {
		foreach my $tid (keys %{$edgehash->{$sid}}) {
			my $edge = $self->get_edge($sid, $tid);
			next unless (defined $edge);
			if ($init) {
				my $incr = 3;
				for my $prop (@{$edge->listprops}) {
					push @$header, $prop;
					$hdrhash->{$prop} = $incr;
					push @$types, $edge->get_type($prop);
					$incr++;
				}
				$table = new Data::Table([], $header, 1);
				$table->addRow($types);
#				printf "[%s]\n", join(" | ", @$header);
#				printf "[%s]\n", join(" | ", @$types);
				$init = 0;
			}
			my $vals = [$self->indx_from_id($sid), $self->indx_from_id($tid), $edge->weight];
			for my $prop (@{$edge->listprops}) {
				$vals->[$hdrhash->{$prop}] = $edge->getprop($prop);
			}
#			printf "[%s]\n", join(" | ", @$vals);
			$table->addRow($vals);
		}
	}
	return $table;
}

sub json {
	my ($self, $outf) = @_;
	my $ntable = $self->get_nodetable;
	my $etable = $self->get_edgetable;
	$outf ? $write = 1 : $write = 0;
	
	my $njson = table_to_json($ntable, $write);
	my $ejson = table_to_json($etable, $write);
	
	if ($write) {
		open NOUT, ">", $outf . "-nodes.txt" or die "Unable to open node table file: $!\n";
		print NOUT "Content-type: application/json\n\n";
		print NOUT "$njson\n";
		close NOUT;
	
		open EOUT, ">", $outf . "-edges.txt" or die "Unable to open edge table file: $!\n";
		print EOUT "Content-type: application/json\n\n";
		print EOUT "$ejson\n";
		close EOUT;
	} else {
		return {"nodes" => $njson, "edges" => $ejson};
	}
	
}

sub graphml {
	my ($self, $outf) = @_;
	
	# lines in the file are built as an array. if a path to an outf is passed, 
	# the file is written directly. if no outf is passed, a ref to the array 
	# of lines is returned to the caller.
	my @lines = ();
	
	my $nodecount = $self->nodecount;
	my $edgecount = $self->edgecount;
	my $direction;
	($self->directed) ? $direction = "directed" : $direction = "undirected";

	# add the graphml header
#	push @lines, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
#	push @lines, "<graphml xmlns=\"http:\/\/graphml.graphdrawing.org\/xmlns\">";
	push @lines, "<graphml>";
	push @lines, "<graph edgedefault=\"$direction\">";
	push @lines, "";	
	push @lines, "<!-- total node count: $nodecount -->";
	push @lines, "<!-- total edge count: $edgecount -->";
	push @lines, "";

	# build the nodes and node schema
	# node ids are stored in an array $self->{NODE_IDS}
	# node objects are stored in a hash $self->{NODES}, keyed by node id string (not indx).
	my $nodetypes = {};
	foreach my $sid (@{$self->{NODE_IDS}}) {
		my $node = $self->get_node($sid);
		if (defined $node) {
			for my $prop (@{$node->listprops}) {
				my $val  = $node->getprop($prop);
				my $type = $node->get_type($prop);
				($nodetypes->{$prop} = $type) unless (exists $nodetypes->{$prop});
			}
		}
	}
	
	# build the edges and edge schema
	# edge objects are stored in a 2D hash of hashes. the outer hash is keyed 
	# by source node id string; the inner hash by target node id string.
	my $edgetypes = {"weight" => "float"};
	my $edgehash = $self->get_edges;
#	printf "get_edges returns hash count: %s\n", scalar(keys %$edgehash);
	foreach my $sid (keys %$edgehash) {
		foreach my $tid (keys %{$edgehash->{$sid}}) {
			my $edge = $edgehash->{$sid}->{$tid};
#			printf "found edge %s - %s (%s)\n", $edge->source, $edge->target, $edge->weight;
			if (defined $edge) {
				for my $prop (@{$edge->listprops}) {
					my $val  = $edge->getprop($prop);
					my $type = $edge->get_type($prop);
					($edgetypes->{$prop} = $type) unless (exists $edgetypes->{$prop});
				}
			}
		}
	}

	# add the node schema
	push @lines, "<!-- node schema -->";
	push @lines, "<key attr.type=\"string\" attr.name=\"uid\" id=\"uid\" for=\"node\" \/>";
	foreach my $prop (keys %$nodetypes) {
		my $type = $nodetypes->{$prop};
		push @lines, "<key attr.type=\"$type\" attr.name=\"$prop\" id=\"$prop\" for=\"node\" \/>";
	}
	push @lines, "";
	
	# add the edge schema
	push @lines, "<!-- edge schema -->";
	foreach my $prop (keys %$edgetypes) {
		my $type = $edgetypes->{$prop};
		push @lines, "<key attr.type=\"$type\" attr.name=\"$prop\" id=\"$prop\" for=\"edge\" \/>";
	}
	push @lines, "";
	push @lines, "";

	# add the nodes
	# puts default values for nodes that are missing properties
	push @lines, "<!-- nodes -->";
	foreach my $sid (@{$self->{NODE_IDS}}) {
		my $sindx = $self->indx_from_id($sid);
		push @lines, "<node id=\"$sindx\">";
		my $node = $self->get_node($sid);
#		my $contains = 0;
#		$contains = $node->contains if (defined $node);
#		push @lines, "<data key=\"contains\">$contains<\/data>";
		push @lines, "<data key=\"uid\">$sid<\/data>";
		for my $prop (keys %$nodetypes) {
			my $val;
			if (defined $node && $node->hasprop($prop)) {
				$val = $node->getprop($prop);
			} elsif ($nodetypes->{$prop} eq "string") {
				$val = "";
			} else {
				$val = -1;
			}
			push @lines, "<data key=\"$prop\">$val<\/data>";
		}
		push @lines, "<\/node>";
	}
	push @lines, "";
	push @lines, "";
	
	
	# add the edges
	# puts default values for edges that are missing properties
	push @lines, "<!-- edges -->";
	foreach my $sid (keys %$edgehash) {
		my $sindx = $self->indx_from_id($sid);
		foreach my $tid (keys %{$edgehash->{$sid}}) {
			my $tindx = $self->indx_from_id($tid);
			push @lines, "<edge source=\"$sindx\" target=\"$tindx\">";
			my $edge = $edgehash->{$sid}->{$tid};
			next unless (defined $edge);
#			my $contains = 0;
#			$contains = $edge->contains;
#			push @lines, "<data key=\"contains\">$contains<\/data>";
			my $wt = 1;
			$wt = $edge->weight;
			push @lines, "<data key=\"weight\">$wt<\/data>";
			for my $prop (keys %$edgetypes) {
				next if ($prop eq "weight");
				my $val;
				if (defined $edge && $edge->hasprop($prop)) {
					$val = $edge->getprop($prop);
				} elsif ($edgetypes->{$prop} eq "string") {
					$val = "";
				} else {
					$val = -1;
				}
				push @lines, "<data key=\"$prop\">$val<\/data>";
			}
			push @lines, "<\/edge>";
		}
	}
	push @lines, "";
	
	
	# close the graph
	push @lines, "<\/graph>";
	push @lines, "<\/graphml>";
	
	# print to output file, if one was given
	return \@lines unless defined $outf;
	# make sure to encode nasty chars like '&'
	open (OUT, "> $outf") or die "Unable to print to $outf: $!\n";
	foreach my $line (@lines) {
		print OUT encode_entities($line, '&') . "\n";
	}
	close OUT;
}



# HOW TO BUILD A CONTEXTUAL GRAPH
# 1 get the nodeset S [n1, n2, ...] with a certain prop-val pair
# 2 cut the graph G into: 
# 		G' (S plus any edge e where: e is in E and e(s,t) where s and t are both in S)
# 		G-G'
# 		edges E' where: e is in E and e(s,t) where either s or t is in S, but not both
#	3 compress S into hypernode H and add to G'
# 4 add E' to G' st each e runs from H to n not in H (coalesce any parallel edges into a hyperedge)
# 5 repeat to coalsce other hyperelements
sub coalesce {
	my ($self, $filter, $hnid) = @_;
	
	# get the set of nodes in the hypernode, the cut, and both subgraphs
	my $nodesets = $self->get_nodes_ids($filter);
	my $cut = $self->cut($nodesets->{"ingroup"});
	my $ingraph  = $self->subgraph($nodesets->{"ingroup"});
	my $outgraph = $self->subgraph($nodesets->{"outgroup"});

#	print "begin hypernode creation\n";

	# coalesce all nodes in the ingraph into one hypernode
	my $hypernode = new HPI::Graph::Node($hnid);
	my $trigger = 1;
	foreach my $id (@{$ingraph->get_nodes_ids}) {
		# we'll need the types and values of each prop in the filter to describe the hypernode
		# these must be the same for all nodes, so just get them from the first node
		if ($trigger) {
			foreach my $prop (keys %$filter) {
				my $val = $filter->{$prop};
				my $type = $proptypes->{$prop};
				$hypernode->setprop($prop, $val, $type);
			}
			$trigger = 0;
		}
		# get the node object for the original node
		my $node = $ingraph->get_node($id);
		# add the node object to the new hypernode
		$hypernode->add_node($node);
	}
	$outgraph->add_node($hnid, $hypernode);

#	print "end hypernode creation\n";
#	print "begin hyperedge creation\n";

	# assemble the hyperedges
	# note that the keys of the cut hash are all in the hypernode
	# we reverse that for easier use, and key the hash by outgroup nodes instead
	my $rcut = {};
	foreach my $sid (keys %$cut) {
		foreach my $tid (keys %{$cut->{$sid}}) {
			$rcut->{$tid} = {} unless exists $rcut->{$tid};
			$rcut->{$tid}->{$sid} = $cut->{$sid}->{$tid};
		}
	}

	# now each inner hash of rcut represents a single hyperedge
	foreach my $tid (keys %$rcut) {
		my @sids = keys %{$rcut->{$tid}};
		next unless (scalar @sids > 0);
		my $hyperedge = new HPI::Graph::Edge($hnid, $tid);
		if (scalar @sids == 1) {
			# not truly a hyperedge, so we just transfer the props from the orginal edge
			$edge = $rcut->{$tid}->{$sids[0]};
			foreach my $prop (@{$edge->listprops}) {
				my $val  = $edge->getprop($prop);
				my $type = $edge->get_type($prop);
				$hyperedge->setprop($prop, $val, $type);
			}
		} else {
			# add the edges to the hyperedge
			foreach my $sid (@sids) {
				my $edge = $rcut->{$tid}->{$sid};
				$hyperedge->add_edge($edge);
			}
		}
		# add the edge to the graph
		my $t = $outgraph->get_node($tid);
		$outgraph->add_edge($hnid, $tid, $hypernode, $t, $hyperedge);
	}
	
#	print "end hyperedge creation\n";
	
#	my $cutsize = 0;
#	foreach my $tid (keys %$rcut) {
#		foreach my $sid (keys %{$rcut->{$tid}}) {
#			$cutsize++;
#		}
#	}
#	printf "edges in cut:      $cutsize.\n";
#	printf "edges added:       $edgesadded.\n";
#	printf "edges in outgraph: %s\n", $outgraph->edgecount;

	# return the outgraph
	return $outgraph;
}



############################################################
# private methods

# remove table rows that have duplicate ids
# only the first instance of the id is retained
sub _rmdups {
	my ($self, $table, $idcols) = @_;
	
	my @cols = $table->header;
	my $pruned = new Data::Table([], \@cols, 1);
	
	my $idhash = {};
	for (my $i=0; $i<$table->nofRow; $i++) {
		my $idstr = "";
		foreach my $col (@$idcols) {
			$idstr .= "|" unless ($idstr eq "");
			$idstr .= $table->elm($i, $col);
		}
		unless (exists $idhash->{$idstr}) {
			my @row = $table->row($i);
			$pruned->addRow(\@row);
			$idhash->{$idstr} = 1;
		}
	}
	return $pruned;
}










1;
