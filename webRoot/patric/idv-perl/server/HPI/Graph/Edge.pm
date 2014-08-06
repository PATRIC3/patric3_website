package HPI::Graph::Edge;

use strict;
use base 'HPI::Graph::Element';

sub new {
	my ($class, $sid, $tid) = @_;
	my $self = new HPI::Graph::Element;
	$self->{SOURCE} = $sid;
	$self->{TARGET} = $tid;
	$self->{WEIGHT} = 1;
	bless($self, $class);
	return $self;
}

sub source {
	my $self = shift;
	return $self->{SOURCE};
}

sub target {
	my $self = shift;
	return $self->{TARGET};
}

sub weight {
	my ($self, $wt) = @_;
	(defined $wt) ? $self->{WEIGHT} = $wt : return $self->{WEIGHT};
}

sub wt {
	my ($self, $wt) = @_;
	(defined $wt) ? $self->{WEIGHT} = $wt : return $self->{WEIGHT};
}

sub edgecount {
	my $self = shift;
	return scalar(keys %{$self->{CHILDREN}});
}

sub add_edge {
	my ($self, $edge) = @_;
	$self->{CHILDREN}->{$edge->source . "|" . $edge->target} = $edge;
	$self->{CONTAINS}++;
}

sub get_edge {
	my ($self, $sid, $tid) = @_;
	exists $self->{CHILDREN}->{$sid . "|" . $tid} ? 
						return $self->{CHILDREN}->{$sid . "|" . $tid} : 
						return undef;
}

sub del_edge {
	my ($self, $sid, $tid) = @_;
	delete $self->{CHILDREN}->{$sid . "|" . $tid};
	$self->{CONTAINS}--;
}

sub get_edges {
	my $self = shift;
	return $self->{CHILDREN};
}

sub clear_edges {
	my $self = shift;
	$self->{CHILDREN} = {};
}

sub pretty {
	my $self = shift;
	print $self->source . "-" . $self->target . "\n";
	foreach my $prop (@{$self->listprops}) {
		printf "  %s : %s\n", $prop, $self->getprop($prop);
	}
}

sub copy {
	my $self = shift;
	my $c;
	($self->edgecount > 1) ? 
		$c = new HPI::Graph::Edge($self->source, $self->target) : 
		$c = new HPI::Graph::Edge($self->source, $self->target);
	$c->weight($self->{WEIGHT});
	foreach my $prop (@{$self->listprops}) {
		my $val  = $self->getprop($prop);
		my $type = $self->get_type($prop);
		$c->setprop($prop, $val, $type);
	}
	foreach my $e (values %{$self->{CHILDREN}}) {
		$c->add_edge($e->copy);
	}
	return $c;
}



1;

