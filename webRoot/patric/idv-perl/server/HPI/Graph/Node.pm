package HPI::Graph::Node;

use strict;
use base 'HPI::Graph::Element';


sub new {
	my ($class, $id) = @_;
	my $self = new HPI::Graph::Element;
	$self->{ID} = $id;
	bless($self, $class);
	return $self;
}

sub id {
	my $self = shift;
	return $self->{ID};
}


sub nodecount {
	my $self = shift;
	return scalar(keys %{$self->{ELEMENTS}});
}

sub add_node {
	my ($self, $node) = @_;
	$self->{ELEMENTS}->{$node->id} = $node;
	$self->{CONTAINS}++;
}

sub get_node {
	my ($self, $id) = @_;
	exists $self->{ELEMENTS}->{$id} ? 
						return $self->{ELEMENTS}->{$id} : 
						return undef;
}

sub del_node {
	my ($self, $id) = @_;
	delete $self->{ELEMENTS}->{$id};
	$self->{CONTAINS}--;
}

sub get_nodes {
	my $self = shift;
	return $self->{ELEMENTS};
}

sub get_node_ids {
	my $self = shift;
	my @ids = keys %{$self->{ELEMENTS}};
	return \@ids;
}

sub clear_nodes {
	my $self = shift;
	$self->{ELEMENTS} = {};
}

sub pretty {
	my $self = shift;
	print $self->id . "\n";
	foreach my $prop (@{$self->listprops}) {
		printf "  %s : %s\n", $prop, $self->getprop($prop);
	}
}

sub copy {
	my $self = shift;
	my $c = new HPI::Graph::Node($self->id);
	foreach my $prop (@{$self->listprops}) {
		my $val  = $self->getprop($prop);
		my $type = $self->get_type($prop);
		$c->setprop($prop, $val, $type);
	}
	foreach my $tag (@{$self->listtags}) {
		$c->tag($tag);
	}
	foreach my $node (values %{$self->{ELEMENTS}}) {
		$c->add_node($node->copy);
	}
	return $c;
}



1;
