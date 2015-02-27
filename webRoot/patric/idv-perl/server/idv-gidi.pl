#! /usr/bin/perl

use strict;

my ($target, $seedid, $pdin, $hgin, $vfin);
$target  = $ARGV[0];
$seedid  = $ARGV[1];
$pdin 	 = $ARGV[2] if (defined $ARGV[2]);
$hgin 	 = $ARGV[3] if (defined $ARGV[3]);
$vfin 	 = $ARGV[4] if (defined $ARGV[4]);

#for Dev Env
	use lib "/opt/jboss/jboss-epp-4.3/jboss-as/server/jboss-patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl/server";
	use lib "/opt/jboss/jboss-epp-4.3/jboss-as/server/jboss-patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl/server/CPAN";

#for Prod Env
	use lib "/opt/jboss-patric/jboss_patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl/server/CPAN";
	use lib "/opt/jboss-patric/jboss_patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl/server/";

#for cluster Evn
	use lib "/usr/share/jboss_deploy/jboss-as/server/patric/patric_website/webRoot/patric/idv-perl/server/CPAN";
	use lib "/usr/share/jboss_deploy/jboss-as/server/patric/patric_website/webRoot/patric/idv-perl/server/";

use HPI::Tools;
use HPI::Graph::Graph;
use HPI::Graph::Node;
use HPI::Graph::Edge;
use HPI::IDV;
use Data::Table;

my $debug = 0;
my $hglim = 10;	# the max number of individual host genes per disease
my $vflim = 10;	# the max number of individual virulence factors per pathogen

my $idv = new HPI::IDV($seedid);
$idv->debug($debug);

my $seedname = "";
my $seedrank = "";
my $rankval  = 0;

my $pathogen_table;
my $disease_table;
my $gene_table;
my $vf_table;

if (defined $pdin && defined $hgin && defined $vfin) {
	# my $pathdis_table = table_from_string($pdin, "\t", "\n", 1);
	# $gene_table = table_from_string($hgin, "\t", "\n", 1);
	# $vf_table = table_from_string($vfin, "\t", "\n", 1);

	$disease_table = Data::Table::fromFile($pdin);
	$gene_table 	 = Data::Table::fromFile($hgin);
	$vf_table 		 = Data::Table::fromFile($vfin);
 
=cut
	# retrieve a Data::Table of all hpmis. 
	# maybe restrict to just those that overlap with the VFs??
	#
	#		pathogen_interactor					(generally) uniprot id of the pathogen protein
	#		pathogen_taxon
	#		pathogen_interactor_type		always protein for now
	#		host_interactor							(generally) uniprot id of the host protein
	#		host_taxon
	#		host_interactor_type				always protein for now
	#		method_name									detection method
	#		method_source
	#		method_source_id
	#		type_name										type of interaction
	#		type_source
	#		type_source_id
	#		reference_source						pubmed|imex|doi|None
	#		reference_source_id
	#
	print "getting host-pathogens PPIs...\n" if ($debug);
	my $hpmi_table = $idvf->get_hpmis($species);
	# add any new pathogens to the master hash of d- and v-associated pathogens
	for (my $i=0; $i<$hpmi_table->nofRow; $i++) {
		my $id = $hpmi_table->elm($i, "pathogen_taxon");
		next unless defined $sprouts->{$id};
		$pathmap->{$id} = $sprouts->{$id};
		my $pp = $hpmi_table->elm($i, "pathogen_interactor");
		my $hp = $hpmi_table->elm($i, "host_interactor");
	}
	print "done.\n" if ($debug);
=cut
}


=cut
construct the base graph of pathogen nodes and edges, then add the disease 
nodes and pathogen-disease edges.
=cut

=cut
pathogen_disease_table: 
	taxon_id 
	organism_name 
	organism_rank 
	parent_id 
	disease_name 
	disease_id 
	tree_node 
	parent_tree_node 
	description 
=cut
	$disease_table->rename("mesh_tree_node", "tree_node");
	$disease_table->rename("mesh_disease_id", "disease_id");
	$disease_table->rename("mesh_disease_name", "disease_name");

	print "cleaning up input tables...\n" if ($debug);
	$disease_table = trim_table_fields($disease_table);

=cut
host gene table:
	gene_symbol
	gene_name
	disease_id
	evidence
	pmids
=cut
	$gene_table->rename("pubmed", "pmids");
	$gene_table = trim_table_fields($gene_table);

=cut
virulence factor table:
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
=cut
	
	$vf_table = trim_table_fields($vf_table);
	print "done.\n" if ($debug);


	print "extracting pathogen data from tables...\n" if ($debug);
	my %taxa = ();
	my $pt_hdr = [qw(taxon_id organism_name organism_rank parent_id)];
	$pathogen_table = new Data::Table([], $pt_hdr, 1);
	for (my $i=0; $i<$disease_table->nofRow; $i++) {
		if ($seedid == $disease_table->elm($i, "taxon_id")) {
			$seedname = $disease_table->elm($i, "organism_name");
			$seedrank = $disease_table->elm($i, "organism_rank");
			$rankval = $idv->getrankval($seedrank);
		}
		unless (defined $taxa{$disease_table->elm($i, "taxon_id")}) {
			my @row = ( $disease_table->elm($i, "taxon_id"), 
									$disease_table->elm($i, "organism_name"), 
									$disease_table->elm($i, "organism_rank"), 
									$disease_table->elm($i, "parent_id") );
			$pathogen_table->addRow(\@row);
			$taxa{$disease_table->elm($i, "taxon_id")} = 1;
		}
	}
=cut
kludge to add evidence to VFs. they are all ID Mapping for this version.
=cut
	my @colv = ();
	my @colp = ();
	for (my $i=0; $i<$vf_table->nofRow; $i++) {
		push @colv, "ID Mapping";
		push @colp, 1;
	}
	$vf_table->addCol(\@colv, "algorithm");
	$vf_table->addCol(\@colp, "pvalue");
	for (my $i=0; $i<$vf_table->nofRow; $i++) {
		#next unless (lc $vf_table->elm($i, "algorithm") eq "id mapping");
		unless (defined $taxa{$vf_table->elm($i, "ncbi_tax_id")}) {
			my @row = ( $vf_table->elm($i, "ncbi_tax_id"), 
									$vf_table->elm($i, "genome_name"), 
									$vf_table->elm($i, "rank"), 
									$vf_table->elm($i, "parent_id") );
			$pathogen_table->addRow(\@row);
			$taxa{$vf_table->elm($i, "ncbi_tax_id")} = 1;
		}
	}
	print "done.\n" if ($debug);
	

# construct the base graph of pathogen nodes and edges.
print "constructing pathogen graph...\n" if ($debug);
my $graph = new HPI::Graph::Graph(id=>"idv-$seedid");
my $ignore = undef;
$ignore = $seedid if ($rankval < $idv->getrankval("family"));
$graph = $idv->graph_pathogens($graph, $pathogen_table, {$ignore => 1});
if ($debug) {
	print "done.\n";
	printf "graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	#$graph->graphml("$seedid.gidi.path.txt");
}


# add the disease nodes
print "adding disease nodes to the graph...\n" if ($debug);
$graph = $idv->graph_diseases($graph, $disease_table);
if ($debug) {
	print "done.\n";
	printf "graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	#$graph->graphml("$seedid.gidi.dis.txt");
}


# add the host genes to the graph
# use hglim to determine whether to add each gene node individually, or compile 
# a Host Gene Collection
print "adding host genes to the graph...\n" if ($debug);
$graph = $idv->graph_dahgs($graph, $gene_table, $hglim);
if ($debug) {
	print "done.\n";
	printf "graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	#$graph->graphml("$seedid.gidi.genes.txt");
}


# add the pathogen virulence factors to the graph
# use vflim to determine whether to add each VF node individually, or compile 
# a Pathogen Virulence Factor Collection
print "adding virulence factors to the graph...\n" if ($debug);
$graph = $idv->graph_vfs($graph, $vf_table, $vflim);
if ($debug) {
	print "done.\n";
	printf "graph has %s nodes and %s edges.\n", $graph->nodecount, $graph->edgecount;
	#$graph->graphml("$seedid.gidi.vfs.txt");
}


# add the hp protein interactions to the graph
=cut
print "adding host-pathogen protein interaction information to the graph...\n";
$graph = $idv->graph_hpmis($graph, $hpmi_table);
print "done.\n";
=cut

# add an id to each edge
print "adding ids to the graph edges...\n" if ($debug);
my $count = 0;
foreach my $sid (keys %{$graph->get_edges}) {
	print "$count\n  sid: $sid\n" if ($debug);
	my $edges = $graph->get_edges($sid)->{$sid};
	foreach my $tid (keys %$edges) {
		print "  tid: $tid\n" if ($debug);
		my $e = $graph->get_edge($sid, $tid);
		$e->setprop("id", "edge-${count}", "s");
		print "  eid: " . $e->getprop("id") . "\n" if ($debug);
		$count++;
	}
}
print "done.\n" if ($debug);

# all nodes must have the same data fields, so we need to rollup the different 
# counts into a single field.
print "rollup the counts...\n" if ($debug);
$graph = $idv->rollup_counts($graph);
print "done.\n" if ($debug);

# save the graph
if ($debug) {
	print "saving the graph to $seedid.gidi.txt...\n";
	#print_table($graph->get_nodetable, "$seedid.gidi-nodes", 0);
	#print_table($graph->get_edgetable, "$seedid.gidi-edges", 0);
	$graph->graphml("$seedid.gidi.txt");
	print "done.\n";
} else {
	print join "\n", @{$graph->graphml};
}

print "goodbye.\n" if ($debug);
exit;


