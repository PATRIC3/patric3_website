package HPI::Graph::BipartiteGraphAP;

use lib "../../CPAN";

use Data::Table;
use Switch;

use HPI::Tools;

sub new {
	my $class = shift;
	my $self = {};
	bless($self, $class);
	return $self;
}


############################################################
# private methods



############################################################
# public methods


=cut
determine if two sets (arrays of items) are identical (i.e., contain the same exact elements).
=cut
sub sets_are_identical {
	my ($self, $s1, $s2) = @_;
	return 0 unless (scalar(@$s1) == scalar(@$s2));
	my %hash = ();
	foreach my $item (@$s1) {
		$hash{$item} = 1;
	}
	my $shared = 0;
	foreach my $item (@$s2) {
		$shared++ if (exists $hash{$item});
	}
	return ($shared == scalar(@$s2));
}


=cut
joins two matrices (AND) of the same size on the basis of overlap:
	if i,j is 1 in both input matrices, i,j in the overlap is set to 1. 
	otherwise, i,j in the overlap is set to 0.
=cut
sub get_matrix_overlap {
	my ($self, $mx1, $mx2) = @_;
	my $mx_overlap = [];
	for (my $i=0; $i<scalar(@$mx1); $i++) {
		$mx_overlap->[$i] = [];
		for (my $j=0; $j<scalar(@{$mx1->[$i]}); $j++) {
			(($mx1->[$i]->[$j] == 1) && ($mx2->[$i]->[$j] == 1)) 
				? ($mx_overlap->[$i]->[$j] = 1) 
				: ($mx_overlap->[$i]->[$j] = 0);
		}
	}
	return $mx_overlap;
}





=cut
###########################################################

developing biclique communities

need generic way to build a graph out of an adjacency matrix.

1. calc N maximal bicliques in bpt for genes and diseases
  submit adjacency lists to lcm algorithm
    take largest set that includes node i
    place size (same-category nodes only) on diagonal
2. 
=cut

=cut
main method for constructing biclique communities from a given bipartite graph. a biclique 
is defined as a closed itemset (also known as a complete subgraph). here we address maximal 
bicliques only; a maximal biclique is the largest closed itemset such that no additional 
items can be added in isolation (i.e., without changing the neighborhood). 

requires the graph, determinants for upper and lower node sets, and thresholds for same. e.g.

$bptap->biclique_comm($bpt, "category", "gene", "disease", 2, 2);

either one or two thresholds are accepted. if only one threshold is given, it is used for both 
upper and lower sets. if two are given, the first is used on the upper nodeset and the second 
on the lower. this method runs all combos from 1 up to the given threshold(s).

returns a matrix of hashes:

[
[$hash_0,0 $hash_0,1 $hash_0,2 ,,,], 
[$hash_1,0 $hash_1,1 $hash_1,2 ,,,], 
...
]

each hash contains the overlap matrix and pairs of overlapping bicliques 
for the threshold values indicates by the matrix i,j position. each hash has the format:

$hash_i,j = 
{
 "overlap_matrix" => [[...] [...] ...], 
 "biclique_pairs" => [[clique1, clique2], [...]]
}

=cut
sub find_biclique_communities {
	my ($self, $bpt, $cat, $val1, $val2, $amax, $bmax) = @_;
		
	# get the upper and lower nodesets from the bipartite graph
	my $ns1 = $bpt->get_nodeset($cat, $val1);
	my $ns2 = $bpt->get_nodeset($cat, $val2);
# need to test that intersection of node sets is empty

	# set up the thresholding ranges for both nodesets
	my $tstart1 = 1;
	my $tstop1  = $amax;
	my $tstart2 = 1;
	my $tstop2  = $amax;
	$tstop2 = $bmax if (defined $bmax);
	
	# get the maximal bicliques
	my $mbc_arr = $self->calc_maximal_bicliques($bpt);
	# get the biclique overlap matrix, or bcom (indicates where the bicliques overlap)
	my $bcoms = $self->calc_bcom($ns1, $ns2, $mbc_arr);
	# threshold and combine the overlap matrices
	my $overall = [];
	for (my $t1=$tstart1; $t1<=$tstop1; $t1++) {
		$overall->[$t1-1] = [];
		my $k1 = $self->threshold_bcom($bcoms->{"overlap_matrix_upper"}, $t1);
		for (my $t2=$tstart2; $t2<=$tstop2; $t2++) {
			my $k2 = $self->threshold_bcom($bcoms->{"overlap_matrix_lower"}, $t2);
			my $k_comb = $self->get_matrix_overlap($k1, $k2);
			my @pairs = ();
			for (my $i=0; $i<scalar(@$k_comb); $i++) {
				next unless ($k_comb->[$i]->[$i] == 1);
				# get the first of the pair of overlapping nodesets
				my $bc1_indx = $mbc_arr->[$i];
				my @bc1 = ();
				# convert the indices to node ids
				foreach my $idx (@$bc1_indx) {
					push @bc1, $bpt->_get_id($idx);
				}
				for (my $j=$i+1; $j<scalar(@$k_comb); $j++) {
					next unless ($k_comb->[$i]->[$j] == 1);
					my $bc2_indx = $mbc_arr->[$j];
					my @bc2 = ();
					foreach my $idx (@$bc2_indx) {
						push @bc2, $bpt->_get_id($idx);
					}
					my $pair = [\@bc1, \@bc2];
					push @pairs, $pair;
				}
			}
			my $hash = {"overlap_matrix" => $k_comb, 
									"biclique_pairs" => \@pairs};
			$overall->[$t1-1]->[$t2-1] = $hash;
		}
	}
	
	return $overall;
}

=cut
finds the maximal bicliques in the given graph, applies LCM method of finding frequent 
closed itemsets to the adjacency matrix of the graph. from LCM: itemsets = P; the transaction 
ids (or vertex ids) = occ(P). together P and occ(P) form a maximal biclique.
returns an array of arrays. each array contains a single maximal biclique.
=cut
sub calc_maximal_bicliques {
	my ($self, $bpt) = @_;
	
	# construct the input file for LCM
	# each line in the file represents an itemset (set of nodes connected by edges)
	# the line number is the index of the node to which nodes in the itemset connect
	
	# get the adjacency lists for all nodes in the graph
	my $alists  = $bpt->get_alists;
	my $lcm_in  = "";
	for (my $indx=0; $indx<$alists->size; $indx++) {
		# get the indices of the adjacent nodes
		my $list = $alists->get_all_adjacent($indx);
		#next unless (scalar(@$list) > 0);
		# add the index to the string
		foreach my $aindx (@$list) {
			$lcm_in .= "$aindx ";
		}
		$lcm_in .= "\n";
	}
	# write the LCM input string to file
	open OUT, ">lcm-input.txt" or die "Unable to open input file: $!\n";
	print OUT "$lcm_in";
	close OUT;
	
	# run the LCM algorithm and save output to string
	my $lcm_out = `./lcm/lcm lcm-input.txt`;
	unlink("lcm-input.txt");
	
	# we will return a simple array of maximal bicliques (each mbc is an array of node indices)
	# output from LCM lists the maximal itemset on a single line, followed on the next line by 
	# the support - the lines where the itemset was found. e.g.,
	#
	# 4 7 12 5 2 9
	# (6 18 8)
	#
	# together, each pair of lines represents the node indices that comprise a single biclique
	my @bicliques = ();
	my $linenum  = 1;
	my $arraypos = 0;
	my @indxs = ();
	for my $line (split /\n/, $lcm_out) {
		if ($linenum%2 == 0) {
			$line =~ s/[\(\)]//gi;
			my @s = split /\s/, $line;
			push @indxs, @s;
			# LCM returns two identical frequent closed itemsets for each biclique.
			# we ignore dups - any itemset that is already present in our list of bicliques.
			my $unique = 1;
			foreach my $mbc (@bicliques) {
				if ($self->sets_are_identical(\@indxs, $mbc)) {
					$unique = 0;
					last;
				}
			}
			if ($unique) {
				$bicliques[$arraypos] = ();
				push @{$bicliques[$arraypos]}, @indxs;
				$arraypos++;
			}
			@indxs = ();
		} else {
			@indxs = split /\s/, $line;
		}
		$linenum++;
	}
	
	return \@bicliques;
}


=cut
given a bipartite graph as an array of bicliques, a set of upper nodes, and a set of lower 
nodes. the node sets must be mutually exclusive (i.e., intersection is empty). each biclique 
is a complete subgraph that can contain both upper and lower nodes.

partition the bicliques into upper and lower biclique overlap matrices:
	upper_counts => matrix containing the number of nodes common to each i,j biclique and 
									the i maximal biclique, restricted to the nodes in the upper node set
	lower_counts => same as upper_counts, but for the lower node set


=cut
sub calc_bcom {
	my ($self, $uns, $lns, $bicliques) = @_;
	
	my $matrices = {};
	
	# convert node sets to hash form for faster lookups
	my $uns_hash = {};
#	print "upper nodeset in class:\n";
	foreach my $nid (@$uns) {
		$uns_hash->{$nid} = 1;
#		print "$nid ";
	}
#	print "\n";
	
#	print "lower nodeset in class:\n";
	my $lns_hash = {};
	foreach my $nid (@$lns) {
		$lns_hash->{$nid} = 1;
#		print "$nid ";
	}
#	print "\n";
	
	# save item counts post-partition
	my $upper_count_mx = [];
	my $lower_count_mx = [];
	# save item lists post-partition
	my $upper_items_mx = [];
	my $lower_items_mx = [];
	
	# calculate the diagonals
	# need all of these in place before filling in the off-diagonals
	for (my $i=0; $i<scalar(@$bicliques); $i++) {
		# init row i in the matrices
		$upper_count_mx->[$i] = [];
		$lower_count_mx->[$i] = [];
		$upper_items_mx->[$i] = [];
		$lower_items_mx->[$i] = [];
		# also init diagonal position to hold itemset
		$upper_items_mx->[$i]->[$i] = [];
		$lower_items_mx->[$i]->[$i] = [];
		my $mbc = $bicliques->[$i];
		my $uct = 0;
		my $lct = 0;
		foreach my $nid (@$mbc) {
			if (exists $uns_hash->{$nid}) {
				$uct++;
				push @{$upper_items_mx->[$i]->[$i]}, $nid;
			} elsif (exists $lns_hash->{$nid}) {
				$lct++;
				push @{$lower_items_mx->[$i]->[$i]}, $nid;
			}
		}
#		print "upper max clique size: $uct\n";
#		print "lower max clique size: $lct\n";
		$upper_count_mx->[$i]->[$i] = $uct;
		$lower_count_mx->[$i]->[$i] = $lct;
	}
	
	# fill in the off-diagonals
	# symmetrical matrix, so only need to calculate above the diagonals
	for (my $i=0; $i<scalar(@$bicliques); $i++) {
		for (my $j=$i+1; $j<scalar(@$bicliques); $j++) {
			# init i,j and j,i positions to hold itemsets
			$upper_items_mx->[$i]->[$j] = [];
			$upper_items_mx->[$j]->[$i] = [];
			$lower_items_mx->[$i]->[$j] = [];
			$lower_items_mx->[$j]->[$i] = [];
			# compare upper mbc i with upper mbc j
			my $mbcj_hash = {};
			my $overlap_ct = 0;
			# force j set into a hash for faster lookup
			foreach my $nid (@{$upper_items_mx->[$j]->[$j]}) {
				$mbcj_hash->{$nid} = 1;
			}
			foreach my $nid (@{$upper_items_mx->[$i]->[$i]}) {
#				print "upper: $nid\n";
				if (exists $mbcj_hash->{$nid}) {
					$overlap_ct++;
					push @{$upper_items_mx->[$i]->[$j]}, $nid;
					push @{$upper_items_mx->[$j]->[$i]}, $nid;
				}
			}
#			print "upper overlap: $overlap_ct\n";
			$upper_count_mx->[$i]->[$j] = $overlap_ct;
			$upper_count_mx->[$j]->[$i] = $overlap_ct;
			
			# compare lower mbc i with lower mbc j
			$mbcj_hash = {};
			$overlap_ct = 0;
			# force j set into a hash for faster lookup
			foreach my $nid (@{$lower_items_mx->[$j]->[$j]}) {
				$mbcj_hash->{$nid} = 1;
			}
			foreach my $nid (@{$lower_items_mx->[$i]->[$i]}) {
#				print "lower: $nid\n";
				if (exists $mbcj_hash->{$nid}) {
					$overlap_ct++;
					push @{$lower_items_mx->[$i]->[$j]}, $nid;
					push @{$lower_items_mx->[$j]->[$i]}, $nid;
				}
			}
#			print "lower overlap: $overlap_ct\n";
			$lower_count_mx->[$i]->[$j] = $overlap_ct;
			$lower_count_mx->[$j]->[$i] = $overlap_ct;
		}
	}
	
	$matrices->{"overlap_matrix_upper"} = $upper_count_mx;
	$matrices->{"overlap_matrix_lower"} = $lower_count_mx;
	#$matrices->{"upper_items"}  = $upper_items_mx;
	#$matrices->{"lower_items"}  = $lower_items_mx;
	return $matrices;
}


=cut
biclique overlap matrix bcom must be square.
the current implementation of this method is exceedinly inefficient, making three passes through 
the original matrix, for O(n^2).
=cut
sub threshold_bcom {
	my ($self, $bcom, $threshold) = @_;

	# set up thresholded matrix, init all cells to -1
	my $n = scalar @$bcom;
	my $outmx = [];
	for (my $i=0; $i<$n; $i++) {
		$outmx->[$i] = [];
		for (my $j=0; $j<$n; $j++) {
			$outmx->[$i]->[$j] = -1;
		}
	}
	
	# threshold the diagonals
	for (my $i=0; $i<$n; $i++) {
		($bcom->[$i]->[$i] < $threshold) ? ($outmx->[$i]->[$i] = 0) : ($outmx->[$i]->[$i] = 1);
	}
	
	# threshold the off-diagonals for diagonals that did not meet the threshold
	# set row and col on these diagonals to 0
	for (my $i=0; $i<$n; $i++) {
		next unless ($outmx->[$i]->[$i] == 0);
		for (my $j=0; $j<$n; $j++) {
			$outmx->[$i]->[$j] = 0;
			$outmx->[$j]->[$i] = 0;
		}
	}

	# threshold the remaining elements (any cell in bcom that still has a -1 in the outmx)
	# set these elements to 1 if they meet threshold-1; otherwise, set to 0
	for (my $i=0; $i<$n; $i++) {
		for (my $j=0; $j<$n; $j++) {
			next unless ($outmx->[$i]->[$j] == -1);
			($bcom->[$i]->[$j] < $threshold-1) ? ($outmx->[$i]->[$j] = 0) : ($outmx->[$i]->[$j] = 1);
			($bcom->[$j]->[$i] < $threshold-1) ? ($outmx->[$j]->[$i] = 0) : ($outmx->[$j]->[$i] = 1);
		}
	}
	
	return $outmx;
}



1;
