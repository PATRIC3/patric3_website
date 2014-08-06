package HPI::IDV;

use HPI::Tools;

use HPI::Graph::Graph;
use HPI::Graph::Node;
use HPI::Graph::Edge;

use lib "../CPAN";
use Data::Table;


sub new {
	my ($class, $seed) = @_;
	my $self = { DEBUG         => 0, 
							 SEED_TAXON 	 => $seed, 
							 DISEASE_ROOTS => { "C01.252" => 
							 											{	"mesh_id" 		=> "D001424", 
							 												"name" 				=> "Bacterial Infections", 
							 												"description" => "Infections by bacteria, general or unspecified." }, 
							 										"C01.252.400" => 
							 											{	"mesh_id" 		=> "D016905", 
							 												"name" 				=> "Gram-Negative Bacterial Infections", 
							 												"description" => "Infections caused by bacteria that show up as pink (negative) when treated by the gram-staining method." }, 
							 										"C01.252.410" => 
							 											{	"mesh_id" 		=> "D016908", 
							 												"name" 				=> "Gram-Positive Bacterial Infections", 
							 												"description" => "Infections caused by bacteria that retain the crystal violet stain (positive) when treated by the gram-staining method." } 
							 										}, 
								TAXRANK			 => { "superkingdom" => 0, 
																	"kingdom" 		 => 1, 
																	"subkingdom" 	 => 2, 
																	"superphylum"  => 3, 
																	"phylum" 			 => 4, 
																	"subphylum" 	 => 5, 
																	"superclass" 	 => 6, 
																	"class" 			 => 7, 
																	"subclass" 		 => 8, 
																	"superorder" 	 => 9, 
																	"order" 			 => 10, 
																	"suborder" 		 => 11, 
																	"superfamily"  => 12, 
																	"family" 			 => 13, 
																	"subfamily" 	 => 14, 
																	"supergenus" 	 => 15, 
																	"genus" 			 => 16, 
																	"subgenus" 		 => 17, 
																	"superspecies" => 18, 
																	"species" 		 => 19, 
																	"subspecies" 	 => 20, 
																	"superstrain"  => 21, 
																	"strain" 			 => 22, 
																	"substrain" 	 => 23, 
																	"isolate" 		 => 24, 
																	"biovar" 			 => 25, 
																	"no rank" 		 => 10000 }
							 };
	bless ($self, $class);
	return $self;
}

sub debug {
	my ($self, $state) = @_;
	$self->{DEBUG} = $state;
}

sub getrankval {
	my ($self, $rank) = @_;
	(defined $self->{"TAXRANK"}->{$rank}) ? return $self->{"TAXRANK"}->{$rank} : return 1000000;
}

sub getrank_byval {
	my ($self, $rankval) = @_;
	my %vals = reverse %{$self->{"TAXRANK"}};
	(defined $vals{$rankval}) ? return $vals{$rankval} : return "no rank";
}


=cut
accepts a graph, and a table of pathogens. adds the pathogens to the 
graph, then looks up their lineage in the taxonomy tree. if an ancestor of the 
pathogen exists in the graph, adds a pathogen-ancestor edge.

accepts an optional list 'ignore' for any pathogen ids that should NOT be 
extended to species bins.

returns the updated graph.
=cut
sub graph_pathogens {
	my ($self, $graph, $ptable, $ignore) = @_;
	$ignore = {} unless (defined $ignore);
	
	# first add all of the pathogen nodes to the graph
	my $prows = {};
	print "  adding pathogen data to graph...\n" if ($self->{DEBUG});
	for (my $i=0; $i<$ptable->nofRow; $i++) {
		my $pathid   = $ptable->elm($i, "taxon_id");
		$prows->{$pathid} = $i;
		my $pname    = $ptable->elm($i, "organism_name");
		my $label    = $pname;
		my $xrefs    = "tax-id:$pathid";
		my $rank     = $ptable->elm($i, "organism_rank");
		my $rankval  = $self->getrankval($rank);
		if ($rankval > 18) {
			my @arr = split /\s/, $pname;
			my $lead = substr(shift(@arr), 0, 1) . ". ";
			$label = $lead . join(" ", @arr);
		}
		unless ($graph->has_node($pathid)) {
			$pnode = new HPI::Graph::Node($pathid);
			$pnode->setprop("name", $pname, "s");
			$pnode->setprop("label", $label, "s");
			$pnode->setprop("rank", $rankval, "i");
			$pnode->setprop("role", "pathogen", "s");
			$pnode->setprop("groups", "", "s");
			$pnode->setprop("xrefs", $xrefs, "s");
			$pnode->setprop("infectious diseases", 0, "i");
			$pnode->setprop("virulence genes", 0, "i");
			$pnode->setprop("host genes", 0, "i");
			$pnode->setprop("host-pathogen PPIs", 0, "i");
			$graph->add_node($pathid, $pnode);
		}
		
		# also need a 'species bin' for taxa at the genus or family level
		# diseases will link to the 'species bin' node, NOT the higher-order node
		unless ($rankval >= 18 || defined($ignore->{$pathid})) {
			$pathid = $pathid . "-bin";
			$pname = "$pname sp.";
			unless ($graph->has_node($pathid)) {
				$pnode = new HPI::Graph::Node($pathid);
				$pnode->setprop("name", $pname, "s");
				$pnode->setprop("label", $pname, "s");
				$pnode->setprop("rank", $rankval, "i");
				$pnode->setprop("role", "collection:pathogen", "s");
				$pnode->setprop("groups", "", "s");
				$pnode->setprop("xrefs", $xrefs, "s");
				$pnode->setprop("infectious diseases", 0, "i");
				$pnode->setprop("virulence genes", 0, "i");
				$pnode->setprop("host genes", 0, "i");
				$pnode->setprop("host-pathogen PPIs", 0, "i");
				$graph->add_node($pathid, $pnode);
			}
		}
	}
	if ($self->{DEBUG}) {
		print "  done.\n";
		printf "  graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	}
	
	# now look up the taxonomy tree for each pathogen and add any ancestor edges 
	print "  adding taxonomy hierarchy data to graph...\n" if ($self->{DEBUG});
	for (my $i=0; $i<$ptable->nofRow; $i++) {
		my $pathid   = $ptable->elm($i, "taxon_id");
		my $parentid = $ptable->elm($i, "parent_id");
		# if the parent is not in our graph, set the parent to be the global root
		$parentid = $self->{SEED_TAXON} unless (defined $prows->{$parentid});
		# if this node is the global root, we can skip it
		if ($pathid == $self->{SEED_TAXON}) {
			$graph->get_node($pathid)->setprop( "groups", 
																					"tax:root", 
																					"s" );
		} else {
			unless ($graph->has_edge($parentid, $pathid)) {
				my $edge = new HPI::Graph::Edge($parentid, $pathid);
				$edge->setprop("srole", "pathogen", "s");
				$edge->setprop("trole", "pathogen", "s");
				$edge->setprop("role", "hierarchy:taxonomy", "s");
				$edge->setprop("evidence", "PATRIC;assigned;tax-id:$parentid", "s");
				$edge->setprop("xrefs", "", "s");
				$edge->setprop("label", "Hierarchy (Taxonomic)", "s");
				$graph->add_edge( $parentid, 
													$pathid, 
													$graph->get_node($parentid), 
													$graph->get_node($pathid), 
													$edge );
			}
			my $parent_name = $ptable->elm($prows->{$parentid}, "organism_name");
			$graph->get_node($pathid)->setprop( "groups", 
																					"tax-name:$parent_name", 
																					"s" );
		}
		# if this pathogen is above the species level, it will have a 'species bin' 
		# node as well. we add an edge from the species bin back to the pathogen. 
		my $bin_pathid = $pathid . "-bin";
		if ($graph->has_node($bin_pathid)) {
			# add the pathogen as the bin node's parent
			my $pathname = $ptable->elm($i, "organism_name");
			$graph->get_node($bin_pathid)->setprop( "groups", 
																							"tax-name:$pathname", 
																							"s" );
			# add the lineage edge: pathogen->bin
			if ($graph->has_node($pathid) 	  && 
					$graph->has_node($bin_pathid) 	&& 
					!$graph->has_edge($pathid, $bin_pathid)) {
				my $edge = new HPI::Graph::Edge($pathid, $bin_pathid);
				$edge->setprop("srole", "pathogen", "s");
				$edge->setprop("trole", "collection:pathogen", "s");
				$edge->setprop("role", "hierarchy:taxonomy", "s");
				$edge->setprop("evidence", "PATRIC;identity;tax-id:$pathid", "s");
				$edge->setprop("xrefs", "", "s");
				$edge->setprop("label", "Hierarchy", "s");
				$graph->add_edge( $pathid, 
													$bin_pathid, 
													$graph->get_node($pathid), 
													$graph->get_node($bin_pathid), 
													$edge );
			}
		}
	}
	if ($self->{DEBUG}) {
		print "  done.\n";
		printf "  graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	}
	
	return $graph;
}


=cut
accepts a graph, and a table of associated diseases. adds the diseases to the 
graph as nodes, and edges connecting them to their associated pathogens. also 
adds disease-ancestor edges for any whose parent diseases are also in the graph.

returns the updated graph.
=cut
sub graph_diseases {
	my ($self, $graph, $dtable) = @_;
	
	my $drows = {};	# table rows keyed by mesh tree nodes
	
	print "  adding disease data and disease-pathogen edges to graph...\n" if ($self->{DEBUG});
	for (my $i=0; $i<$dtable->nofRow; $i++) {
		# verify this row actually has a disease
		my $did = trim($dtable->elm($i, "disease_id"));
		next if (!$did || $did eq "");

		# verify that the associated pathogen is in the graph. 
		# if not, we skip this disease.
		my $pathid = trim($dtable->elm($i, "taxon_id"));
		next unless ($graph->has_node($pathid));
		
		my $dname    = trim($dtable->elm($i, "disease_name"));
		my $treenode = trim($dtable->elm($i, "tree_node"));
		my $rankval  = scalar(my @steps = split /\./, $treenode);
		
		# make or get the disease node
		my $dnode;
		if ($graph->has_node($did)) {
			$dnode = $graph->get_node($did);
		} else {
			$dnode = new HPI::Graph::Node($did);
			$dnode->setprop("name", $dname, "s");
			$dnode->setprop("label", $dname, "s");
			$dnode->setprop("role", "disease", "s");
			$dnode->setprop("rank", $rankval, "i");
			$dnode->setprop("groups", "mesh-tree:$treenode", "s");
			$dnode->setprop("xrefs", "mesh-id:$did", "s");
			$dnode->setprop("host genes", 0, "i");
			$dnode->setprop("ctd genes", 0, "i");
			$dnode->setprop("gad genes", 0, "i");
			$graph->add_node($did, $dnode);
		}
		
		# get the pathogen node
		# if a 'species bin' node exists, want to link to the bin node instead.
		$pathid = $pathid . "-bin" if ($graph->has_node($pathid . "-bin"));
		my $pnode = $graph->get_node($pathid);

		# add the disease-pathogen edge to the graph
		unless ($graph->has_edge($pathid, $did)) {
			# add to count of diseases on pathogen node
			my $count = $pnode->getprop("infectious diseases");
			$pnode->setprop("infectious diseases", $count+1, "i");
			my $edge = new HPI::Graph::Edge($pathid, $did);
			$edge->setprop("srole", "pathogen", "s");
			$edge->setprop("trole", "disease", "s");
			$edge->setprop("role", "association:pathogen-disease", "s");
			$edge->setprop("evidence", "PATRIC;assigned;", "s");
			$edge->setprop("xrefs", "", "s");
			$edge->setprop("label", "Association (PATRIC)", "s");
			$graph->add_edge($pathid, $did, $pnode, $dnode, $edge);
			
			# store the row that contains this disease
			$drows->{$treenode} = $i;
		}
	}
	if ($self->{DEBUG}) {
		print "  done.\n";
		printf "  graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	}
	
	print "  adding disease hierarchy data to graph...\n" if ($self->{DEBUG});
	for (my $i=0; $i<$dtable->nofRow; $i++) {
		my $did = trim($dtable->elm($i, "disease_id"));
		
		next if (!$did || $did eq "");
		
		# look for parent of this disease in the disease table, by walking back up 
		# the mesh tree node.
		my $pid;
		my $parent_treenode;
		my $treenode = trim($dtable->elm($i, "tree_node"));
		my @nodes = split /\./, $treenode;
		while (scalar @nodes > 3) {
			pop @nodes;
			$parent_treenode = join ".", @nodes;
			last if (defined $drows->{$parent_treenode});
		}
		# if no parent exists in the graph, we still need a parent to ensure the 
		# resulting subtree is rooted. every disease must have a parent.
		#print "disease $did has parent tree node $parent_treenode.\n" if ($self->{DEBUG});
		if (defined $drows->{$parent_treenode}) {
			# get the parent disease id. since it is in drows, we've already added it 
			# to the graph.
			my $drow = $drows->{$parent_treenode};
			$pid = trim($dtable->elm($drow, "disease_id"));
			#print "found $parent_treenode ($pid) in the disease table!\n" if ($self->{DEBUG});
		} else {
			# join this disease to one of the global roots.
			print "  $parent_treenode is NOT in the disease table!\n" if ($self->{DEBUG});
			$parent_treenode = "C01.252" unless (defined $self->{DISEASE_ROOTS}->{$parent_treenode});
			print "  parent tree node is now $parent_treenode.\n" if ($self->{DEBUG});
			my $parent_obj = $self->{DISEASE_ROOTS}->{$parent_treenode};
			$pid = $parent_obj->{"mesh_id"};
			# add the global root as a node, if necessary
			unless ($graph->has_node($pid)) {
				print "  adding new graph node for $pid ($parent_treenode).\n" if ($self->{DEBUG});
				my $rankval  = scalar(my @steps = split /\./, $parent_treenode);
				my $gpnode = new HPI::Graph::Node($pid);
				$gpnode->setprop("name", $parent_obj->{"name"}, "s");
				$gpnode->setprop("label", $parent_obj->{"name"}, "s");
				$gpnode->setprop("role", "disease", "s");
				$gpnode->setprop("rank", $rankval, "i");
				$gpnode->setprop("groups", "mesh-tree:$parent_treenode", "s");
				$gpnode->setprop("xrefs", "mesh-id:$pid|mesh-term:" . $parent_obj->{"name"}, "s");
				$gpnode->setprop("host genes", 0, "i");
				$gpnode->setprop("ctd genes", 0, "i");
				$gpnode->setprop("gad genes", 0, "i");
				$graph->add_node($pid, $gpnode);
			}
		}
		
		# add a hierarchical edge to the graph, between this disease and its parent 
		if ($graph->has_node($pid) && 
				$graph->has_node($did) && 
				!$graph->has_edge($pid, $did)) {
			my $dnode = $graph->get_node($did);
			my $pnode = $graph->get_node($pid);
			# make the new lineage edge
			my $edge = new HPI::Graph::Edge($pid, $did);
			$edge->setprop("srole", "disease", "s");
			$edge->setprop("trole", "disease", "s");
			$edge->setprop("role", "hierarchy:disease", "s");
			$edge->setprop("evidence", "MeSH;assigned;mesh-term:" . $pnode->getprop("name"), "s");
			$edge->setprop("xrefs", "ext:mesh", "s");
			$edge->setprop("label", "Hierarchy (MeSH)", "s");
			$graph->add_edge($pid, $did, $pnode, $dnode, $edge);
		}
	}
	print "  done.\n" if $self->{DEBUG};
	
	print "  looking for duplicate edges to root disease nodes...\n" if ($self->{DEBUG});
	# loop thru the inedges for each disease node
	TARGET:
	foreach my $tid (@{$graph->get_nids("role" => "disease")->{"ingroup"}}) {
		# get the inedges as a hash of edge objects keyed by source id
		my $edges = $graph->get_inedges($tid);
		# count disease sources; only continue if more than one
		my $count = 0;
		foreach my $sid (keys %$edges) {
			$count++ if ($graph->get_node($sid)->getprop("role") eq "disease");
		}
		next TARGET unless ($count > 1);
		print "  found multiple disease inedges to $tid\n" if ($self->{DEBUG});
		SOURCE:
		foreach my $sid (keys %$edges) {
			next SOURCE unless ($graph->get_node($sid)->getprop("role") eq "disease");
			if ($sid eq "D001424" || $sid eq "D016905" || $sid eq "D016908") {
				$graph->del_edge($sid, $tid);
				print "  !! DELETING edge $sid to $tid.\n" if ($self->{DEBUG});
				next TARGET;
			}
		}
	}

	if ($self->{DEBUG}) {
		print "  done.\n";
		printf "  graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	}
	
	return $graph;
}


=cut
accepts a graph, a table of host genes, and a limit.

gene_table: 
	gene_symbol 
	gene_name 
	disease_id 
	mesh_disease_name 
	evidence 
	pmids

if the number of associated genes for a disease exceeds the limit, adds a 
Host Gene Collection node associated with that disease. otherwise, adds all of 
the genes as individual nodes with edges connecting them to their associated 
disease(s).

returns the updated graph.
=cut
sub graph_dahgs {
	my ($self, $graph, $gtable, $limit) = @_;
	print "  adding disease-associated genes to graph...\n" if ($self->{DEBUG});
	
	# convert input table into a pair hashes
	# $bydis->{did} = { gsym => [rows], ... }
	my $bydis = {};
	# $bygene->{gsym} = { did => [rows], ... }
	my $bygene = {};
	
	for (my $i=0; $i<$gtable->nofRow; $i++) {
		my $did = $gtable->elm($i, "disease_id");
		my $gsym = $gtable->elm($i, "gene_symbol");
		
		# add the row to the hash of genes by disease
		$bydis->{$did} = {} unless (defined $bydis->{$did});
		$bydis->{$did}->{$gsym} = [] unless (defined $bydis->{$did}->{$gsym});
		push @{$bydis->{$did}->{$gsym}}, $i;

		# add the row to the hash of diseases by gene
		$bygene->{$gsym} = {} unless (defined $bygene->{$gsym});
		$bygene->{$gsym}->{$did} = [] unless (defined $bygene->{$gsym}->{$did});
		push @{$bygene->{$gsym}->{$did}}, $i;
	}
	
	# count the number of unique genes per disease (unique = not shared among 
	# multiple diseases). this will determine if we 'collect' the gene nodes
	my $gcount_bydis = {};
	foreach my $did (keys %$bydis) {
		$gcount_bydis->{$did} = 0;
		foreach my $gsym (keys %{$bydis->{$did}}) {
			$gcount_bydis->{$did}++ unless (scalar keys %{$bygene->{$gsym}} > 1);
		}
	}
	
	# loop through the gene list for each disease
	foreach my $did (keys %$bydis) {
		#print "  looking at $did.\n" if ($self->{DEBUG});
		next unless ($graph->has_node($did));
		
		# get the disease node
		my $dnode = $graph->get_node($did);
		my $dname = $dnode->getprop("name");
		
		# update the total count of genes on this disease node
		$dnode->setprop("host genes", $dnode->getprop("host genes") + scalar (keys %{$bydis->{$did}}), "i");
		
		if (defined $limit && $gcount_bydis->{$did} > $limit) {
			# add 'shared' genes as separate nodes; 'collect' all others
			# initialize or retrieve the collection node
			my $collid = $did . "-gbin";
			my $collnode;
			if ($graph->has_node($collid)) {
				$collnode = $graph->get_node($collid);
			} else {
				if ($self->{DEBUG}) {
					print "  adding new gene collection for $dname.\n";
				}
				$collnode = new HPI::Graph::Node($collid);
				$collnode->setprop("name", "", "s");
				$collnode->setprop("label", "", "s");
				$collnode->setprop("role", "collection:host gene", "s");
				$collnode->setprop("rank", 0, "i");
				$collnode->setprop("groups", "tax-name:Homo sapiens", "s");
				$collnode->setprop("xrefs", "tax-id:9606", "s");
				$collnode->setprop("host genes", 0, "i");
				$collnode->setprop("ctd genes", 0, "i");
				$collnode->setprop("gad genes", 0, "i");
				$graph->add_node($collid, $collnode);
			}
			# update the count on the collection node
			my $gcount = $collnode->getprop("genes") + $gcount_bydis->{$did};
			$collnode->setprop("name", $gcount . " Host Genes", "s");
			$collnode->setprop("label", $gcount . " Host Genes", "s");
			$collnode->setprop("rank", $gcount, "i");
			$collnode->setprop("xrefs", $collnode->getprop("xrefs") . "|mesh-term:$dname", "s");
			$collnode->setprop("host genes", $gcount, "i");
			# add the collection-disease edge, unless it already exists
			my $colledge;
			if ($graph->has_edge($collid, $did)) {
				$colledge = $graph->get_edge($collid, $did);
			} else {
				$colledge = new HPI::Graph::Edge($collid, $did);
				$colledge->setprop("srole", "collection:host gene", "s");
				$colledge->setprop("trole", "disease", "s");
				$colledge->setprop("role", "association:host gene-disease", "s");
				$colledge->setprop("evidence", "", "s");
				$colledge->setprop("xrefs", "", "s");
				$colledge->setprop("label", "Association", "s");
				$colledge->weight(0);
				$graph->add_edge($collid, $did, $collnode, $dnode, $colledge);
			}
			
			# add edge from collection to organism node, if one exists in the graph
			if ($graph->has_node("9606") && 
					($graph->get_node("9606")->getprop("role") eq "host" || 
					 $graph->get_node("9606")->getprop("role") eq "collection:host")) {
				my $onode = $graph->get_node("9606");
				my $oedge;
				if ($graph->has_edge("9606", $collid)) {
					$oedge = $graph->get_edge("9606", $collid);
				} else {
					$oedge = new HPI::Graph::Edge("9606", $collid);
					$oedge->setprop("srole", "host", "s");
					$oedge->setprop("trole", "collection:host gene", "s");
					$oedge->setprop("role", "association:host-host gene", "s");
					$oedge->setprop("evidence", "", "s");
					$oedge->setprop("xrefs", "", "s");
					$oedge->setprop("label", "Association", "s");
					$graph->add_edge("9606", $collid, $onode, $collnode, $oedge);
				}
				# add the genes to the count on the organism
				$onode->setprop("host genes", $onode->getprop("host genes") + $collnode->getprop("host genes"), "i");
			}
			# extract shared genes and add as individual nodes
			# at the same time, compile evidences and xrefs for the collection
			# 	xrefs will be either GAD or CTD or both
			# 	evidences will be pubmed ids combined with evidence (if exists)
			my %collection_xrs = ();
			my %collection_evs = ();
			foreach my $gsym (keys %{$bydis->{$did}}) {
				if (scalar keys %{$bygene->{$gsym}} > 1) {
					# this gene is shared by multiple diseases
					# add the gene as a node if it doesn't already exist
					unless ($graph->has_node($gsym)) {
						if ($self->{DEBUG}) {
							print "  adding new node for $gsym ($dname).\n";
						}
						my $gnode = new HPI::Graph::Node($gsym);
						my $gname = $gtable->elm($bygene->{$gsym}->{$did}->[0], "gene_name");
						$gname = $gsym if (!(defined $gname) || $gname eq "");
						$gnode->setprop("name", $gname, "s");
						$gnode->setprop("label", $gsym, "s");
						$gnode->setprop("role", "host gene", "s");
						$gnode->setprop("rank", 1, "i");
						$gnode->setprop("groups", "tax-name:Homo sapiens", "s");
						$gnode->setprop("xrefs", "tax-id:9606|mesh-term:$dname", "s");
						$gnode->setprop("host genes", 1, "i");
						$graph->add_node($gsym, $gnode);
					}
					# add the multiple gene-disease edges, unless they exist
					foreach my $disease_id (keys %{$bygene->{$gsym}}) {
						my $gdedge;
						if ($graph->has_edge($gsym, $disease_id)) {
							$gdedge = $graph->get_edge($gsym, $disease_id);
						} else {
							$gdedge = new HPI::Graph::Edge($gsym, $disease_id);
							$gdedge->setprop("srole", "host gene", "s");
							$gdedge->setprop("trole", "disease", "s");
							$gdedge->setprop("role", "association:host gene-disease", "s");
							$gdedge->setprop("evidence", "", "s");
							$gdedge->setprop("xrefs", "", "s");
							$gdedge->weight(0);
							$gdedge->setprop("label", "Association", "s");
							$graph->add_edge(	$gsym, 
																$disease_id, 
																$graph->get_node($gsym), 
																$graph->get_node($disease_id), 
																$gdedge );
						}
						# update the edge with xrefs and evidences
						my %xrs = map { $_ => 1 } split /\|/, $gdedge->getprop("xrefs");
						my %evs = map { $_ => 1 } split /\|/, $gdedge->getprop("evidence");
						my $isctd = 0;
						my $isgad = 0;
						foreach my $row (@{$bygene->{$gsym}->{$disease_id}}) {
							my @pmids = split /\|/, $gtable->elm($row, "pmids");
							my $ev = $gtable->elm($row, "evidence");
							if ("gad" eq lc($ev)) {
								$isgad = 1;
								if (scalar @pmids < 1) {
									$evs{"Genetic;Imported from GAD;"} = 1;
								} else {
									foreach my $pmid (@pmids) {
										$evs{"Genetic;Imported from GAD;pubmed-id:" . $pmid} = 1;
									}
								}
							} else {
								$isctd = 1;
								if (scalar @pmids < 1) {
									$evs{"Chemical;CTD $ev;"} = 1;
								} else {
									foreach my $pmid (@pmids) {
										$evs{"Chemical;CTD $ev;pubmed-id:" . $pmid} = 1;
									}
								}
							}
						}
						$gdedge->setprop("xrefs", join("|", keys %xrs), "s");
						$gdedge->setprop("evidence", join("|", keys %evs), "s");
						if (scalar keys %evs > 0) {
							my %gdl = ();
							foreach my $k (keys %evs) {
								my @v = split(/;/, $k);
								$gdl{$v[0]} = 1;
							}
							my $gdassoc = join ", ", keys %gdl;
							$gdedge->setprop("label", "Association (" . $gdassoc . ")" , "s");
						}
						$gdedge->weight(scalar keys %evs);
					}
				} else {
					# this gene is unique to the given disease
					# use it to update the collection xrefs and evidences and gene counts
					my $isctd = 0;
					my $isgad = 0;
					foreach my $row (@{$bydis->{$did}->{$gsym}}) {
						my @pmids = split /\|/, $gtable->elm($row, "pmids");
						my $ev = $gtable->elm($row, "evidence");
						if ("gad" eq lc($ev)) {
							$collection_evs{"Genetic;Imported from GAD;"} = 1;
							$isgad = 1;
						} else {
							$collection_evs{"Chemical;CTD $ev;"} = 1;
							$isctd = 1;
						}
					}
					$collnode->setprop("ctd genes", $collnode->getprop("ctd genes") + $isctd, "i");
					$collnode->setprop("gad genes", $collnode->getprop("gad genes") + $isgad, "i");
					$colledge->setprop("evidence", join("|", keys %collection_evs), "s");
					$colledge->setprop("xrefs", join("|", keys %collection_xrs), "s");
					$colledge->setprop("evidence", join("|", keys %collection_evs), "s");
					if (scalar keys %collection_evs > 0) {
						my %cl = ();
						foreach my $k (keys %collection_evs) {
							my @v = split(/;/, $k);
							$cl{$v[0]} = 1;
						}
						my $cassoc = join ", ", keys %cl;
						$colledge->setprop("label", "Association (" . $cassoc . ")" , "s");
					}
					$colledge->weight(scalar keys %collection_evs);
				}
			}
		} else {
			# this gene is unique to this disease
			foreach my $gsym (keys %{$bydis->{$did}}) {
				# add the gene as a node if it doesn't already exist
				unless ($graph->has_node($gsym)) {
					if ($self->{DEBUG}) {
						print "  adding new node for $gsym ($dname).\n";
					}
					my $gnode = new HPI::Graph::Node($gsym);
					my $gname = $gtable->elm($bydis->{$did}->{$gsym}->[0], "gene_name");
					$gname = $gsym if (!(defined $gname) || $gname eq "");
					$gnode->setprop("name", $gname, "s");
					$gnode->setprop("label", $gsym, "s");
					$gnode->setprop("role", "host gene", "s");
					$gnode->setprop("rank", 1, "i");
					$gnode->setprop("groups", "tax-name:Homo sapiens", "s");
					$gnode->setprop("xrefs", "tax-id:9606|mesh-term:$dname", "s");
					$gnode->setprop("host genes", 1, "i");
					$graph->add_node($gsym, $gnode);
				}
				# add the multiple gene-disease edges, unless they exist
				foreach my $disease_id (keys %{$bygene->{$gsym}}) {
					my $gdedge;
					if ($graph->has_edge($gsym, $disease_id)) {
						$gdedge = $graph->get_edge($gsym, $disease_id);
					} else {
						$gdedge = new HPI::Graph::Edge($gsym, $disease_id);
						$gdedge->setprop("srole", "host gene", "s");
						$gdedge->setprop("trole", "disease", "s");
						$gdedge->setprop("role", "association:host gene-disease", "s");
						$gdedge->setprop("evidence", "", "s");
						$gdedge->setprop("xrefs", "", "s");
						$gdedge->weight(0);
						$gdedge->setprop("label", "Association", "s");
						$graph->add_edge(	$gsym, 
															$disease_id, 
															$graph->get_node($gsym), 
															$graph->get_node($disease_id), 
															$gdedge );
					}
					# update the edge with xrefs and evidences
					my %xrs = map { $_ => 1 } split /\|/, $gdedge->getprop("xrefs");
					my %evs = map { $_ => 1 } split /\|/, $gdedge->getprop("evidence");
					foreach my $row (@{$bygene->{$gsym}->{$disease_id}}) {
						my @pmids = split /\|/, $gtable->elm($row, "pmids");
						my $ev = $gtable->elm($row, "evidence");
						if ("gad" eq lc($ev)) {
							if (scalar @pmids < 1) {
								$evs{"Genetic;Imported from GAD;"} = 1;
							} else {
								foreach my $pmid (@pmids) {
									$evs{"Genetic;Imported from GAD;pubmed-id:" . $pmid} = 1;
								}
							}
						} else {
							if (scalar @pmids < 1) {
								$evs{"Chemical;CTD $ev;"} = 1;
							} else {
								foreach my $pmid (@pmids) {
									$evs{"Chemical;CTD $ev;pubmed-id:" . $pmid} = 1;
								}
							}
						}
					}
					$gdedge->setprop("xrefs", join("|", keys %xrs), "s");
					$gdedge->setprop("evidence", join("|", keys %evs), "s");
					if (scalar keys %evs > 0) {
						my %gdl = ();
						foreach my $k (keys %evs) {
							my @v = split(/;/, $k);
							$gdl{$v[0]} = 1;
						}
						my $gdassoc = join ", ", keys %gdl;
						$gdedge->setprop("label", "Association (" . $gdassoc . ")" , "s");
					}
					$gdedge->weight(scalar keys %evs);
				}
			}
		}


		# add total host gene count for this disease to each associated pathogen node
		my $edgehash = $graph->get_edges($did);
		my @pids = ();
		my @gids = ();
		my @gcids = ();
		foreach my $id (keys %{$edgehash->{$did}}) {
			push @pids, $id if ($graph->get_node($id)->getprop("role") eq "pathogen");
			push @gids, $id if ($graph->get_node($id)->getprop("role") eq "host gene");
			push @gcids, $id if ($graph->get_node($id)->getprop("role") eq "collection:host gene");
		}
		my $total = $dnode->getprop("host genes");
		foreach my $pid (@pids) {
			my $pnode = $graph->get_node($pid);
			$pnode = $graph->get_node($pid . "-bin") if ($graph->has_node($pid . "-bin") && 
																									 $pnode->getprop("rank") < 18);
			my $count = $pnode->getprop("host genes");
			$pnode->setprop("host genes", $count+$total, "i");
		}
		
		foreach my $gid (@gids) {
			my $e = $graph->get_edge($gid, $did);
			my %evidence = split /\|/, $e->getprop("evidence");
			my $isctd = 0;
			my $isgad = 0;
			foreach my $k (keys %evidence) {
				my @v = split(/;/, $k);
				if ($v[0] eq "Genetic") {
					$isgad = 1;
				} elsif ($v[0] eq "Chemical") {
					$isctd = 1;
				}
			}
			$dnode->setprop("ctd genes", $dnode->getprop("ctd genes") + $isctd, "i");
			$dnode->setprop("gad genes", $dnode->getprop("gad genes") + $isgad, "i");
		}
		
		foreach my $gcid (@gcids) {
			$dnode->setprop("ctd genes", $dnode->getprop("ctd genes") + $graph->get_node($gcid)->getprop("ctd genes"), "i");
			$dnode->setprop("gad genes", $dnode->getprop("gad genes") + $graph->get_node($gcid)->getprop("gad genes"), "i");
		}

	}
	if ($self->{DEBUG}) {
		print "  done.\n";
		printf "  graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	}
	
	return $graph;
}


=cut
accepts a graph, a table of virulence factors, and a limit.

vf_table: 
	ncbi_tax_id
	genome_name
	accession
	na_feature_id
	algorithm
	source_id
	product
	vfg_id
	vf_id
	gene_name
	rank
	parent_id
	pvalue

if the number of associated VFs for a pathogen exceeds the limit, adds a 
Virulence Gene Collection node associated with that pathogen. otherwise, adds 
all of the VFs as individual nodes with edges connecting them to their associated 
pathogen.

returns the updated graph.
=cut
sub graph_vfs {
	my ($self, $graph, $vftable, $limit) = @_;
	print "  adding pathogen virulence factors to graph...\n" if ($self->{DEBUG});
	
	# convert input table into a pair hashes
	# $bypath->{pid} = { gsym => [rows], ... }
	my $bypath = {};
	# $bygene->{gsym} = { pid => [rows], ... }
	my $bygene = {};
	
	for (my $i=0; $i<$vftable->nofRow; $i++) {
		my $pid  = $vftable->elm($i, "ncbi_tax_id");
		my $gid  = $vftable->elm($i, "vfg_id");
		
		# skip if this pathogen is not in the graph
		next unless $graph->has_node($pid);
		#next unless (lc $vftable->elm($i, "algorithm") eq "id mapping");

		# add the row to the hash of genes by disease
		$bypath->{$pid} = {} unless (defined $bypath->{$pid});
		$bypath->{$pid}->{$gid} = [] unless (defined $bypath->{$pid}->{$gid});
		push @{$bypath->{$pid}->{$gid}}, $i;

		# add the row to the hash of diseases by gene
		$bygene->{$gid} = {} unless (defined $bygene->{$gid});
		$bygene->{$gid}->{$pid} = [] unless (defined $bygene->{$gid}->{$pid});
		push @{$bygene->{$gid}->{$pid}}, $i;
	}
	
	# count the number of unique genes per pathogen (unique = not shared among 
	# multiple pathogens). this will determine if we 'collect' the gene nodes
	my $gcount_bypath = {};
	foreach my $pid (keys %$bypath) {
		$gcount_bypath->{$pid} = 0;
		foreach my $gid (keys %{$bypath->{$pid}}) {
			$gcount_bypath->{$pid}++ unless (scalar keys %{$bygene->{$gid}} > 1);
		}
	}
	
	foreach my $pid (keys %$bypath) {
		# skip if this pathogen is not in the graph
		next unless $graph->has_node($pid);
		
		print "  looking at VFs for pathogen $pid.\n" if ($self->{DEBUG});
		
		# get the pathogen node
		my $pnode = $graph->get_node($pid);
		my $orgname = $pnode->getprop("name");
		my $pidg = $pid;
		if ($graph->has_node($pid . "-bin") && $pnode->getprop("rank") < 18) {
			$pnode = $graph->get_node($pid . "-bin");
			$pidg = $pid . "-bin";
		}
		
		# update the total count of VFs on this pathogen node
		$pnode->setprop("virulence genes", $pnode->getprop("virulence genes") + 
																				 scalar (keys %{$bypath->{$pid}}), "i");

		if (defined $limit && scalar(keys %{$bypath->{$pid}}) > $limit) {
			# add 'shared' VFs as separate nodes; 'collect' all others
			# initialize or retrieve the collection node
			my $collid = $pidg . "-vfbin";
			my $collnode;
			my $vfincoll = 0;
			if ($graph->has_node($collid)) {
				$collnode = $graph->get_node($collid);
			} else {
				if ($self->{DEBUG}) {
					print "  adding new VF collection for $orgname.\n";
				}
				$collnode = new HPI::Graph::Node($collid);
				$collnode->setprop("name", "", "s");
				$collnode->setprop("label", "", "s");
				$collnode->setprop("role", "collection:virulence gene", "s");
				$collnode->setprop("groups", "tax-name:$orgname", "s");
				$collnode->setprop("rank", 0, "i");
				$collnode->setprop("xrefs", "tax-id:$pid", "s");
				$collnode->setprop("virulence genes", 0, "i");
				$graph->add_node($collid, $collnode);
			}
			# update the count and label for the collection node
			my $count = $collnode->getprop("virulence genes");
			$count += scalar(keys %{$bypath->{$pid}});
			$collnode->setprop("virulence genes", $count, "i");
			$collnode->setprop("label", $count . " Virulence Genes", "s");
			$collnode->setprop("name", $count . " Virulence Genes", "s");
			$collnode->setprop("rank", $count, "i");
			# add the collection-pathogen edge, unless it already exists
			my $colledge;
			if ($graph->has_edge($pidg, $collid)) {
				$colledge = $graph->get_edge($pidg, $collid);
			} else {
				$colledge = new HPI::Graph::Edge($pidg, $collid);
				$colledge->setprop("srole", $pnode->getprop("role"), "s");
				$colledge->setprop("trole", "collection:virulence gene", "s");
				$colledge->setprop("role", "association:pathogen-virulence gene", "s");
				$colledge->setprop("evidence", "", "s");
				$colledge->setprop("xrefs", "", "s");
				$colledge->setprop("label", "Association", "s");
				$colledge->weight(0);
				$graph->add_edge($pidg, $collid, $pnode, $collnode, $colledge);
			}
			
			# extract shared genes from the collection and add as individual nodes
			# at the same time, compile evidences and xrefs for the collection
			my %collection_xrs = ();
			my %collection_evs = ();
			foreach my $gid (keys %{$bypath->{$pid}}) {
				# loop through all the VFs for this pathogen
				if (scalar keys %{$bygene->{$gid}} > 1) {
					# this VF is shared by multiple pathogens
					# add the VF as a separate node if it doesn't already exist
					unless ($graph->has_node($gid)) {
						if ($self->{DEBUG}) {
							print "  adding new VF node $gid for $orgname.\n";
						}
						my $gnode = new HPI::Graph::Node($gid);
						my $gsymbol = $vftable->elm($bygene->{$gid}->{$pid}->[0], "gene_name");
						my $gname   = $vftable->elm($bygene->{$gid}->{$pid}->[0], "product");
						my $vfid    = $vftable->elm($bygene->{$gid}->{$pid}->[0], "vf_id");
						my $fid     = $vftable->elm($bygene->{$gid}->{$pid}->[0], "na_feature_id");
						$gname = $gsymbol if (!(defined $gname) || $gname eq "");
						$gnode->setprop("name", $gname, "s");
						$gnode->setprop("label", $gsymbol, "s");
						$gnode->setprop("role", "virulence gene", "s");
						$gnode->setprop("rank", 1, "i");
						$gnode->setprop("groups", "tax-name:$orgname", "s");
						$gnode->setprop("xrefs", "patric-id:$fid|tax-id:$pid|vfdb-id:$vfid|vfdb-gid:$gid", "s");
						$gnode->setprop("virulence genes", 1, "i");
						$graph->add_node($gid, $gnode);
					}
					# add edges from this extracted VF to its pathogens
					foreach my $pathid (keys %{$bygene->{$gid}}) {
						my $pathidg  = $pathid;
						my $pathnode = $graph->get_node($pathid);
						if ($graph->has_node($pathid . "-bin") && $pnode->getprop("rank") < 18) {
							$pathnode = $graph->get_node($pathid . "-bin");
							$pathidg  = $pathid . "-bin";
						}
						my $pvedge;
						if ($graph->has_edge($pathidg, $gid)) {
							$pvedge = $graph->get_edge($pathidg, $gid);
						} else {
							$pvedge = new HPI::Graph::Edge($pathidg, $gid);
							$pvedge->setprop("srole", "pathogen", "s");
							$pvedge->setprop("trole", "virulence gene", "s");
							$pvedge->setprop("role", "association:pathogen-virulence gene", "s");
							$pvedge->setprop("evidence", "", "s");
							$pvedge->setprop("xrefs", "", "s");
							$pvedge->weight(0);
							$pvedge->setprop("label", "Association", "s");
							$graph->add_edge(	$pathidg, 
																$gid, 
																$graph->get_node($pathidg), 
																$graph->get_node($gid), 
																$pvedge );
						}
						# update the edge with evidences
						my %evs = map { $_ => 1 } split /\|/, $pvedge->getprop("evidence");
						my $wt  = 0;
						foreach my $row (@{$bygene->{$gid}->{$pid}}) {
							my $featid = $vftable->elm($row, "na_feature_id");
							my $vfid   = $vftable->elm($row, "vf_id");
							my $evid   = lc $vftable->elm($row, "algorithm");
							if ($evid eq "id mapping") {
								if ($vfid eq "" || $vfid eq "\\N") {
									$evs{"VFDB;Imported from VFDB;"} = 1;
								} else {
									$evs{"VFDB;Imported from VFDB;vfdb-id:$vfid"} = 1;
								}
							} elsif ($evid eq "blastp") {
								$evs{"PATRIC;Inferred via BLASTP;vfdb-gid:$gid"} = 1;
							}
							# the edge weight is a mean of 10^pval
							$wt += 10 ** (1-$vftable->elm($row, "pvalue"));
						}
						$pvedge->setprop("evidence", join("|", keys %evs), "s");
						if (scalar keys %evs > 0) {
							my %pvl = ();
							foreach my $k (keys %evs) {
								my @v = split(/;/, $k);
								$pvl{$v[1]} = 1;
							}
							my $pvassoc = join ", ", keys %pvl;
							$pvedge->setprop("label", "Association (" . $pvassoc . ")" , "s");
						}
						$pvedge->weight($wt/(scalar @{$bygene->{$gid}->{$pid}}));
					}
				} else {
					# this VF is unique to the current pathogen
					# use it to update the collection values
					$vfincoll++;
					my $wt = 0;
					foreach my $row (@{$bypath->{$pid}->{$gid}}) {
						my $featid = $vftable->elm($row, "source_id");
						my $vfid   = $vftable->elm($row, "vf_id");
						my $evid   = lc $vftable->elm($row, "algorithm");
						if ($evid eq "id mapping") {
							$collection_evs{"VFDB;Imported from VFDB;"} = 1;
						} elsif ($evid eq "blastp") {
							$collection_evs{"PATRIC;Inferred via BLASTP;"} = 1;
						}
						$collection_xrs{"vfdb-id:$vfid"} = 1;
						$wt += 10 ** (1-$vftable->elm($row, "pvalue"));
					}
					$colledge->setprop("xrefs", join("|", keys %collection_xrs), "s");
					$colledge->setprop("evidence", join("|", keys %collection_evs), "s");
					if (scalar keys %collection_evs > 0) {
						my %cl = ();
						foreach my $k (keys %collection_evs) {
							my @v = split(/;/, $k);
							$cl{$v[1]} = 1;
						}
						my $cassoc = join ", ", keys %cl;
						$colledge->setprop("label", "Association (" . $cassoc . ")" , "s");
					}
					$colledge->weight($colledge->weight + $wt);
				}
			}
			$colledge->weight(($colledge->weight)/$vfincoll);
		} else {
			# this VF is unique to this pathogen, and there are less than $limit 
			# unique VFs
			foreach my $gid (keys %{$bypath->{$pid}}) {
				my $gnode;
				my $row = $bypath->{$pid}->{$gid}->[0];
				my $name    = $vftable->elm($row, "product");
				my $symbol  = $vftable->elm($row, "gene_name");
				my $vfid    = $vftable->elm($row, "vf_id");
				my $fid     = $vftable->elm($row, "na_feature_id");
				my $evid   = lc $vftable->elm($row, "algorithm");
				$name = $symbol if ($name eq "");
				if ($graph->has_node($gid)) {
					$gnode = $graph->get_node($gid);
				} else {
					if ($self->{DEBUG}) {
						print "  adding new VF node $gid for $orgname.\n";
					}
					$gnode = new HPI::Graph::Node($gid);
					$gnode->setprop("name", $name, "s");
					$gnode->setprop("label", $symbol, "s");
					$gnode->setprop("role", "virulence gene", "s");
					$gnode->setprop("rank", 1, "i");
					$gnode->setprop("groups", "tax-name:$orgname", "s");
					$gnode->setprop("xrefs", "patric-id:$fid|tax-id:$pid|vfdb-id:$vfid|vfdb-gid:$gid", "s");
					$gnode->setprop("virulence genes", 1, "i");
					$graph->add_node($gid, $gnode);
				}
				# add or update the vf-pathogen edge
				my $pvedge;
				if ($graph->has_edge($pidg, $gid)) {
					$pvedge = $graph->get_edge($pidg, $gid);
				} else {
					#my $count = $pnode->getprop("virulence factors");
					#$pnode->setprop("virulence factors", $count+1, "i");
					$pvedge = new HPI::Graph::Edge($pidg, $gid);
					$pvedge->setprop("srole", "pathogen", "s");
					$pvedge->setprop("trole", "virulence gene", "s");
					$pvedge->setprop("role", "association:pathogen-virulence gene", "s");
					$pvedge->setprop("evidence", "", "s");
					$pvedge->setprop("xrefs", "", "s");
					$pvedge->setprop("label", "Association", "s");
					$pvedge->weight(10 ** (1-$vftable->elm($row, "pvalue")));
					$graph->add_edge($pidg, $gid, $pnode, $gnode, $pvedge);
				}
				# update the edge with evidences
				my %evs = map { $_ => 1 } split /\|/, $pvedge->getprop("evidence");
				if ($evid eq "id mapping") {
					if ($vfid eq "" || $vfid eq "\\N") {
						$evs{"VFDB;Imported from VFDB;"} = 1;
					} else {
						$evs{"VFDB;Imported from VFDB;vfdb-id:$vfid"} = 1;
					}
				} elsif ($evid eq "blastp") {
					$evs{"PATRIC;Inferred via BLASTP;vfdb-gid:$gid"} = 1;
				}
				# the edge weight is the log-2(pval) score
				$pvedge->setprop("evidence", join("|", keys %evs), "s");
				if (scalar keys %evs > 0) {
					my %pvl = ();
					foreach my $k (keys %evs) {
						my @v = split(/;/, $k);
						$pvl{$v[1]} = 1;
					}
					my $pvassoc = join ", ", keys %pvl;
					$pvedge->setprop("label", "Association (" . $pvassoc . ")" , "s");
				}
			}
		}
	}
	if ($self->{DEBUG}) {
		print "  done.\n";
		printf "  graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	}
	return $graph;
}


sub rollup_counts {
	my ($self, $graph) = @_;
	print "  merging counts on graph nodes...\n" if ($self->{DEBUG});
	# merge the counts on each node into one node prop
	foreach my $nid (@{$graph->get_nids}) {
		#print "looking at node $nid...\n" if ($self->{DEBUG});
		my $node = $graph->get_node($nid);
		my $role = $node->getprop("role");
		#print "  role is $role\n" if ($self->{DEBUG});
		my $countstr = "";
		if ($role eq "pathogen" || $role eq "collection:pathogen") {
			$countstr .= "paids:"  . $node->getprop("infectious diseases");
			$countstr .= "|pavfs:" . $node->getprop("virulence genes");
			$countstr .= "|dahgs:" . $node->getprop("host genes");
		} elsif ($role eq "disease") {
			$countstr .= "dahgs:" . $node->getprop("host genes");
			$countstr .= "|dahgs-ctd:" . $node->getprop("ctd genes");
			$countstr .= "|dahgs-gad:" . $node->getprop("gad genes");
		} elsif ($role eq "host gene") {
			$countstr .= "dahgs:" . $node->getprop("host genes");
		} elsif ($role eq "collection:host gene") {
			$countstr .= "dahgs:" . $node->getprop("host genes");
			$countstr .= "|dahgs-ctd:" . $node->getprop("ctd genes");
			$countstr .= "|dahgs-gad:" . $node->getprop("gad genes");
		} elsif ($role eq "virulence gene" || $role eq "collection:virulence gene") {
			$countstr .= "pavfs:" . $node->getprop("virulence genes");
		}
		# delete the old props
		$node->delprop("infectious diseases");
		$node->delprop("virulence genes");
		$node->delprop("host genes");
		$node->delprop("ctd genes");
		$node->delprop("gad genes");
		$node->delprop("host-pathogen PPIs");
		$node->setprop("counts", $countstr, "s");
		#print "$countstr\n" if ($self->{DEBUG});
	}
	print "  done.\n" if ($self->{DEBUG});
	
	return $graph;
}


1;
