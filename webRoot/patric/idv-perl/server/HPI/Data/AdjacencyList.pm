package HPI::Data::AdjacencyList;


sub new {
	my $class = shift;
	my $self = {MATRIX => []};
	
	bless($self, $class);
	return $self;
}


sub add {
	my $self = shift;
	my $len = push @{$self->{MATRIX}}, [];
	return $len;
}


sub del {
	my ($self, $sindx, $tindx) = @_;
	my @before = @{$self->{MATRIX}->[$sindx]};
#	print "@before\n";
#	print "now remove $tindx\n";
	my $tpos = -1;
	for (my $i=0; $i<scalar(@{$self->{MATRIX}->[$sindx]}); $i++) {
		if ($self->{MATRIX}->[$sindx]->[$i] == $tindx) {
			$tpos = $i;
			last;
		}
	}
	unless ($tpos < 0) {
		splice @{$self->{MATRIX}->[$sindx]}, $tpos, 1;
	}
#	my @after = @{$self->{MATRIX}->[$sindx]};
#	print "@after\n";
}

sub size {
	my ($self, $sid) = @_;
	defined($sid) ? return scalar(@{$self->{MATRIX}->[$sindx]}) 
								:	return scalar(@{$self->{MATRIX}});
}

sub count_adjacent {
	my ($self, $sindx) = @_;
	return 0 unless exists $self->{MATRIX}->[$sindx];
	return scalar(@{$self->{MATRIX}->[$sindx]});
}

sub make_adjacent {
	my ($self, $sindx, $tindx) = @_;
	# if either node is missing from the matrix, do nothing
	return 0 unless (exists $self->{MATRIX}->[$sindx] && 
									 exists $self->{MATRIX}->[$tindx]);
	# add the target id to the source node's adjacency array
	my $len = push @{$self->{MATRIX}->[$sindx]}, $tindx;
	return $len;
}


sub get_all_adjacent {
	my ($self, $sindx) = @_;
	return [] unless exists $self->{MATRIX}->[$sindx];
	return $self->{MATRIX}->[$sindx];
}

sub get_sources {
	my ($self, $tindx) = @_;
	my @sindices = ();
	SOURCE:
	for (my $sindx=0; $sindx<scalar(@{$self->{MATRIX}}); $sindx++) {
		#next SOURCE if ($sindx == $tindx);
		TARGET:
		foreach my $t (@{$self->{MATRIX}->[$sindx]}) {
			if ($t == $tindx) {
				push @sindices, $sindx;
				next SOURCE;
			}
		}
	}
	return \@sindices;
}

sub are_adjacent {
	my ($self, $sindx, $tindx) = @_;
	return 0 unless exists $self->{MATRIX}->[$sindx];
	my @adjacent = @{$self->{MATRIX}->[$sindx]};
	foreach my $adj (@adjacent) {
		return 1 if ($adj == $tindx);
	}
#	print "looking at $sindx, found no adjacent $tindx:\n@adjacent\n";
	return 0;
}


sub get_empty {
	my $self = shift;
	my @empty = ();
	for (my $i=0; $i<scalar(@{$self->{MATRIX}}); $i++) {
		my $alist = $self->{MATRIX}->[$i];
		push(@empty, $i) unless (scalar @{$alist} > 0);
	}
	return \@empty;
}



1;
