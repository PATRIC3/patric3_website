package HPI::Graph::UndirectedGraph;

use base 'HPI::Graph::Graph';

sub new {
	my $class = shift;
	my $self = HPI::Graph::Graph->new(@_);
	bless($self, $class);
	return $self;
}


1;

	
