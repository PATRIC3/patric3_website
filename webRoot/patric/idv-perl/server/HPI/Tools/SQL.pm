package HPI::Tools::SQL;

use base 'Exporter';

our @EXPORT = ('sqlesc');

use Data::Table;



###########################################################
# static methods

sub sqlesc {
	my $raw = shift;
	my $esc  = 0;
	my $safe = $raw;
	my $indx = index($safe, "'");
	if ($indx > -1) {
		$safe =~ s/'/''/;
		$esc = 1;
	}
	$indx = index($safe, "\\");
	if ($indx > -1) {
		$safe =~ s/\\/\\\\/;
		$esc = 1;
	}
	$indx = index($safe, "\/");
	if ($indx > -1) {
		$safe =~ s/\//\/\//;
		$esc = 1;
	}
#	$indx = index($safe, "[");
#	if ($indx > -1) {
#		$safe =~ tr/[/[[/;
#		$esc = 1;
#	}
	$esc ? return "E'$safe'" : return "'$safe'";
}



###########################################################
# instance methods

sub new {
	my $class = shift;
	my $self = {};
	bless($self, $class);
	return $self;
}

sub print_sql {
	my ($self, $data, $schema, $sqltable, $filename) = @_;

	my $id_list = join ", ", @{$data->header};
	my $first = 1;
	my $full_schema = "";
	foreach my $id (keys %$schema) {
		$full_schema .= ", " unless ($first);
		$full_schema .= $id . " " . $schema->{$id};
		$first = 0;
	}

	open (OUT, ">$filename") or die "Unable to open $filename for writing: $!\n";
	print OUT "DROP TABLE IF EXISTS $sqltable;\n";
	print OUT "CREATE TABLE $sqltable ($full_schema);\n";
	print OUT "COPY $sqltable ($id_list) FROM stdin;\n";
	for (my $i=0; $i<$data->nofRow; $i++) {
		my $row = $data->rowRef($i);
		for (my $j=0; $j<scalar(@$row); $j++) {
			my $val = $row->[$j];
			print OUT "\t" unless ($j == 0);
			print OUT "$val";
		}
		print OUT "\n";
	}
	print OUT "\\.\n\n";
	close OUT;
}


sub print_sql_inserts {
	my ($self, $table, $sqltable, $filename, $writetxt) = @_;
	
	if (defined $writetxt && $writetxt) {
		my $dfile = $filename . ".txt";
		open(OUT, "> $dfile");
		print OUT $table->tsv;
		close OUT;
	}
	
	my $qfile = $filename . ".sql";
	
	my @colnames = $table->header;
	my $cols = "";
	foreach my $colname (@colnames) {
		$cols .= "," unless ($cols eq "");
		$cols .= "$colname";
	}
	open(OUT2, "> $qfile");
	for (my $i=0; $i<$table->nofRow; $i++) {
		my $vals = "";
		foreach my $colname (@colnames) {
			$vals .= "," unless ($vals eq "");
			my $val = sqlesc($self, $table->elm($i, $colname));
			$vals .= "$val";
		}
		print OUT2 "INSERT INTO $sqltable ($cols) VALUES ($vals);\n";
	}
	close OUT2;
}


sub print_sql_updates {
	my ($self, $table, $sqltable, $filename, $writetxt) = @_;
	
	if (defined $writetxt && $writetxt) {
		my $dfile = $filename . ".txt";
		open(OUT, "> $dfile");
		print OUT $table->tsv;
		close OUT;
	}
	
	my $qfile = $filename . ".sql";
	
	my @colnames = $table->header;
	open(OUT2, "> $qfile");
	for (my $i=0; $i<$table->nofRow; $i++) {
		# colname='colval',colname='colval'
		my $update_list = "";
		# colname='colval'
		my $filter = "";
		my $count = 0;
		foreach my $colname (@colnames) {
			my $val = sqlesc($self, $table->elm($i, $colname));
			if ($count == 0) {
				# use the first col as the WHERE filter
				$filter = "$colname = $val";
			} else {
				$update_list .= "," unless ($update_list eq "");
				$update_list .= "$colname = $val";
			}
			$count++;
		}
		print OUT2 "UPDATE $sqltable SET $update_list WHERE $filter;\n";
	}
	close OUT2;
}



1;

