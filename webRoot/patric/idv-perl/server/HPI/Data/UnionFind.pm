package HPI::Data::UnionFind;

use POSIX;


sub new {
	my ($class) = @_;
	my $self  = {ELEMENTS => {}, 
							 SETS     => {}};
	bless($self, $class);
	return $self;
}



############################################################
# private methods




############################################################
# public methods

sub init {
	my ($self, $elements) = @_;
	foreach my $s (@$elements) {
		$self->{ELEMENTS}->{$s} = $s;
		$self->{SETS}->{$s} = [$s];
	}
}

sub add {
	my ($self, $s) = @_;
	$self->{ELEMENTS}->{$s} = $s;
	$self->{SETS}->{$s} = [$s];
}

sub union {
	my ($self, $a, $b) = @_;
	return unless (defined $a && defined $b);
	if (scalar @{$self->{SETS}->{$a}} > scalar @{$self->{SETS}->{$a}}) {
		# merge set b into set a; new set named a
		foreach my $s (@{$self->{SETS}->{$b}}) {
			$self->{ELEMENTS}->{$s} = $a;
		}
		push @{$self->{SETS}->{$a}}, @{$self->{SETS}->{$b}};
		delete $self->{SETS}->{$b};
	}	else {
		# merge set a into set b; new set named b
		foreach my $s (@{$self->{SETS}->{$a}}) {
			$self->{ELEMENTS}->{$s} = $b;
		}
		push @{$self->{SETS}->{$b}}, @{$self->{SETS}->{$a}};
		delete $self->{SETS}->{$a};
	}
}


sub find {
	my ($self, $u) = @_;
	return unless (defined $u);
	return $self->{ELEMENTS}->{$u};
}


sub pretty {
	my $self = shift;
	print "Components:\n";
	foreach my $id (keys %{$self->{SETS}}) {
		my @set = @{$self->{SETS}->{$id}};
		print " $id = @set\n";
	}
}


1;

