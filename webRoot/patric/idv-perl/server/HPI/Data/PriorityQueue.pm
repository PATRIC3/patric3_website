package HPI::Data::PriorityQueue;

use POSIX;


sub new {
	my ($class) = @_;
	my $self  = {HEAP     => [], 
							 NAMES    => [], 
							 NAME_HASH => {}};
	bless($self, $class);
	return $self;
}



############################################################
# private methods

sub _hup {
	my ($self, $i) = @_;
	if ($i > 0) {
		my $j = $self->_pindx($i);
		if ($self->{HEAP}->[$i] < $self->{HEAP}->[$j]) {
			$self->_swap($i, $j);
			# recurse
			$self->_hup($j);
		}
	}
}

sub _hdown {
	my ($self, $i) = @_;
#	printf "moving %s ($i) down!\n", $self->{HEAP}->[$i];
	my $n = scalar(@{$self->{HEAP}})-1;
	my $j = 0;
	if ((2*$i)+1 > $n) {
#		"return without action.\n";
		return;
	}
	elsif ((2*$i)+1 == $n) {
		$j = (2*$i)+1;
	} elsif ((2*$i)+1 < $n) {
		my $cindxs = $self->_cindxs($i);
		if ($self->{HEAP}->[$cindxs->[1]] < $self->{HEAP}->[$cindxs->[0]]) {
			$j = $cindxs->[1];
		} else {
			$j = $cindxs->[0];
		}
	}
	if ($self->{HEAP}->[$j] < $self->{HEAP}->[$i]) {
		$self->_swap($i, $j);
		# recurse
		$self->_hdown($j);
	}
#	print "hdown with $i says:\n";
#	$self->pretty;
}

sub _swap {
	my ($self, $i, $j) = @_;
#	printf "before swap: %s, %s\n", $self->{HEAP}->[$i], $self->{HEAP}->[$j];
	# swap the keys
	my $swapkey = $self->{HEAP}->[$j];
	$self->{HEAP}->[$j] = $self->{HEAP}->[$i];
	$self->{HEAP}->[$i] = $swapkey;
#	printf "after swap : %s, %s\n", $self->{HEAP}->[$i], $self->{HEAP}->[$j];

	# swap the names
	$self->{NAME_HASH}->{$self->{NAMES}->[$i]} = $j;
	$self->{NAME_HASH}->{$self->{NAMES}->[$j]} = $i;
	my $swapname = $self->{NAMES}->[$j];
	$self->{NAMES}->[$j] = $self->{NAMES}->[$i];
	$self->{NAMES}->[$i] = $swapname;
}


sub _pindx {
	my ($self, $i) = @_;
	my $indx = ceil($i/2)-1;
	($indx < 0) ? return 0 : return $indx;
}

sub _cindxs {
	my ($self, $i) = @_;
	my $n = scalar(@{$self->{HEAP}}) - 1;
	return [] unless ($i >= 0);
	my $children = [(2*$i)+1, (2*$i)+2];
#	printf "from cindxs sub ($n): %s, %s\n", $children->[0], $children->[1];
	$children->[0] = undef if ($children->[0] > $n);
	$children->[1] = undef if ($children->[1] > $n);
	my @results = ();
	push @results, $children->[0] if (defined $children->[0]);
	push @results, $children->[1] if (defined $children->[1]);
	return \@results;
}



############################################################
# public methods

sub init {
	my ($self, $names, $default) = @_;
	# add all names and set all keys in the heap to $default.
	$default = inf unless (defined $default);
#	my @t = @$names;
#	print "init gets: @t\n";
	foreach my $name (@$names) {
		my $i = push(@{$self->{HEAP}}, $default) - 1;
		push @{$self->{NAMES}}, $name;
		$self->{NAME_HASH}->{$name} = $i;
	}
}


sub is_empty {
	my $self = shift;
	my $len = scalar(@{$self->{HEAP}});
	($len > 0) ? return 0 : return 1;
}

sub position {
	my ($self, $name) = @_;
	return $self->{NAME_HASH}->{$name};
}

sub insert {
	my ($self, $val, $name) = @_;
	my $i = push(@{$self->{HEAP}}, $val) - 1;
	push @{$self->{NAMES}}, $name;
	$self->{NAME_HASH}->{$name} = $i;
	$self->_hup($i);
}

sub del_by_name {
	my ($self, $name) = @_;
	$self->del($self->position($name));
}

sub del {
	my ($self, $i) = @_;
	# if i is out of range of the heap, don't do anything
	return unless ($i > -1 && $i < scalar(@{$self->{HEAP}}));
	
	# remove the key from the very last slot in the heap (n)
	my $key = pop @{$self->{HEAP}};
	my $name = pop @{$self->{NAMES}};
	delete $self->{NAME_HASH}->{$self->{NAMES}->[$i]};
	
	# if there was only one element in the original heap, it is now empty.
	# no need to 'fix' via hup or hdown
	return if ($self->is_empty);
	
	# otherwise, put the key from n into slot i (similarly for the name)
	$self->{HEAP}->[$i]  = $key;
	$self->{NAME_HASH}->{$name} = $i;
	$self->{NAMES}->[$i] = $name;
	
	# if new key at i is smaller than it's parent key, move it up via hup
	# this is not possible if i is 0 (has no parent) so skip that case
	if ($i > 0) {
		my $pkey = $self->{HEAP}->[$self->_pindx($i)];
		if ($key < $pkey) {
			$self->_hup($i);
			return;
		}
	}
	
#	print "ready to test $key ($i) for hdown...\n";
	# if new key at i is bigger than either of it's children, move it down via hdown
	my $cindxs = $self->_cindxs($i);
#	printf "children indices: %s, %s\n", $cindxs->[0], $cindxs->[1];
	if (defined $cindxs->[0] && ($key > $self->{HEAP}->[$cindxs->[0]]) || 
			defined $cindxs->[1] && ($key > $self->{HEAP}->[$cindxs->[1]])) {
#		print "moving $key ($i) down!\n";
		$self->_hdown($i);
		return;
	}
}

sub find_min {
	my $self = shift;
	return $self->{HEAP}->[0];
}

sub find_min_name {
	my $self = shift;
	return $self->{NAME_HASH}->{$self->{HEAP}->[0]};
}

sub extract_min {
	my $self = shift;
	my $min = $self->{HEAP}->[0];
	$self->del(0);
	return $min;
}

sub extract_min_with_name {
	my $self = shift;
	my $min = $self->{HEAP}->[0];
	my $name = $self->{NAMES}->[0];
	$self->del(0);
	return [$min, $name];
}

sub change_key {
	my ($self, $name, $key) = @_;
	my $i = $self->position($name);
	$self->{HEAP}->[$i] = $key;
	# if new key at i is smaller than it's parent key, move it up via hup
	# this is not possible if i is 0 (has no parent) so skip that case
	if ($i > 0) {
		my $pkey = $self->{HEAP}->[$self->_pindx($i)];
		if ($key < $pkey) {
			$self->_hup($i);
			return;
		}
	}
	# if new key at i is bigger than either of it's children, move it down via hdown
	my $cindxs = $self->_cindxs($i);
	if (defined $cindxs->[0] && ($key > $self->{HEAP}->[$cindxs->[0]]) || 
			defined $cindxs->[1] && ($key > $self->{HEAP}->[$cindxs->[1]])) {
		$self->_hdown($i);
		return;
	}
}

sub get_key_by_name {
	my ($self, $name) = @_;
	my $pos = $self->position($name);
	return $self->{HEAP}->[$pos];
}


sub pretty {
	my $self = shift;
	my @h = @{$self->{HEAP}};
	my @p = @{$self->{NAMES}};
	print "heap : @h\n";
	print "names: @p\n";
#	print "name hash:\n";
#	foreach my $name (keys %{$self->{NAME_HASH}}) {
#		printf "%s = %s\n", $name, $self->{NAME_HASH}->{$name};
#	}
}


1;

