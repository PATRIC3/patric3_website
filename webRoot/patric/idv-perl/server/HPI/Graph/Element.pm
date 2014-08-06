package HPI::Graph::Element;
use strict;


sub new {
	my ($class, $id) = @_;
	my $self = { ID				 => $id, 
							 CHILDREN  => {}, 
							 TAGS      => {}, 
							 PROPS     => {}, 
							 PROPTYPES => {} 
						 };
	bless($self, $class);
	return $self;
}


sub getid {
	my $self = shift;
	return $self->{ID};
}

sub setid {
	my ($self, $id) = @_;
	$self->{ID} = $id;
}


sub contains {
	my $self = shift;
	return scalar(keys %{$self->{CHILDREN}});
}


sub haschild {
	my ($self, $name) = @_;
	return exists $self->{CHILDREN}->{$name};
}

sub addchild {
	my ($self, $name, $child) = @_;
	$self->{CHILDREN}->{$name} = $child;
}

sub getchild {
	my ($self, $name) = @_;
	exists($self->{CHILDREN}->{$name}) ? 
				return $self->{CHILDREN}->{$name} : 
				return undef;
}

sub delchild {
	my ($self, $name) = @_;
	delete $self->{CHILDREN}->{$name} if (exists($self->{CHILDREN}->{$name}));
}

sub listchildren {
	my $self = shift;
	return keys %{$self->{CHILDREN}};
}

sub clearchildren {
	my $self = shift;
	$self->{CHILDREN} = {};
}


sub hastag {
	my ($self, $tag) = @_;
	return exists $self->{TAGS}->{$tag};
}

sub tag {
	my ($self, $tag) = @_;
	$self->{TAGS}->{$tag} = 1;
}

sub untag {
	my ($self, $tag) = @_;
	delete $self->{TAGS}->{$tag};
}

sub listtags {
	my $self = shift;
	my @tags = keys %{$self->{TAGS}};
	return \@tags;
}

sub cleartags {
	my $self = shift;
	$self->{TAGS} = {};
}



sub hasprop {
	my ($self, $prop, $val) = @_;
	if (defined $val) {
		return 0 unless (exists $self->{PROPS}->{$prop});
		($self->get_type($prop) eq "string") ? 
						return ($self->{PROPS}->{$prop} eq $val) :
						return ($self->{PROPS}->{$prop} == $val);
	} else {
		return exists $self->{PROPS}->{$prop};
	}
}

sub setprop {
	my ($self, $prop, $val, $type) = @_;
	my $typestr = _format_type($self, $type);
	return 0 unless _validate($self, $prop, $typestr);
	$self->{PROPS}->{$prop}     = $val;
	$self->{PROPTYPES}->{$prop} = $typestr;
	return 1;
}

sub getprop {
	my ($self, $prop) = @_;
	exists($self->{PROPS}->{$prop}) ? 
				return $self->{PROPS}->{$prop} : 
				return undef;
}

sub listprops {
	my $self = shift;
	return [] unless (keys %{$self->{PROPS}});
	my @props = keys %{$self->{PROPS}};
	return \@props;
}

sub delprop {
	my ($self, $prop) = @_;
	return unless (defined $self->{PROPS}->{$prop});
	delete $self->{PROPS}->{$prop};
	delete $self->{PROPTYPES}->{$prop};
}

sub clearprops {
	my $self = shift;
	$self->{PROPS} = {};
	$self->{PROPTYPES} = {};
}


sub get_type {
	my ($self, $prop) = @_;
	exists($self->{PROPTYPES}->{$prop}) ? 
				return $self->{PROPTYPES}->{$prop} : 
				return undef;
}



############################################################
# private methods

sub _validate {
	my ($self, $prop, $proptype) = @_;
	return 1 unless (exists $self->{PROPS}->{$prop});
	return (_format_type($self, $self->{PROPTYPES}->{$prop}) eq _format_type($self, $proptype));
}


sub _format_type {
	my ($self, $type) = @_;
	my $b = {"default" => "boolean", "bool" => 1, "boolean" => 1, "b" => 1};
	my $i = {"default" => "int", "int" => 1, "integer" => 1, "i" => 1};
	my $l = {"default" => "long", "lng" => 1, "long" => 1, "l" => 1, };
	my $d = {"default" => "double", "dbl" => 1, "double" => 1, "d" => 1};
	my $f = {"default" => "float", "fl" => 1, "float" => 1, "f" => 1};
	my $s = {"default" => "string", "str" => 1, "string" => 1, "s" => 1};
	return $b->{"default"} if (exists $b->{$type});
	return $i->{"default"} if (exists $i->{$type});
	return $l->{"default"} if (exists $l->{$type});
	return $d->{"default"} if (exists $d->{$type});
	return $f->{"default"} if (exists $f->{$type});
	return $s->{"default"};
}



1;
