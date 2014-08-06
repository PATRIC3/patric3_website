package HPI::Tools;

use strict;
use base 'Exporter';
our @EXPORT = qw(trim 
								 parsetype 
								 format_file_as_table 
								 print_table 
								 print_matrix 
								 read_ma_config 
								 format_type 
								 percent_encode 
								 table_from_string 
								 trim_table_fields);

use lib "../CPAN";
use HTML::Entities;
use Data::Table;



###########################################################
# static methods

sub trim {
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

sub trim_table_fields {
	my $table = shift;
	my @hdr = $table->header;
	for (my $i=0; $i<$table->nofRow; $i++) {
		foreach my $col (@hdr) {
			$table->setElm($i, $col, trim($table->elm($i, $col)));
		}
	}
	return $table;
}

sub table_from_string {
	my ($data, $cdelim, $rdelim, $hrow) = @_;
	my $table;
	$hrow = 0 unless ($hrow);
	my @rows = split /$rdelim/, $data;
	my $i = -1;
	foreach my $row (@rows) {
		my @cols = split /$cdelim/, $row;
		$i++;
		if ($i == 0) {
			if ($hrow) {
				my @lc = map { lc $_ } @cols;
				$table = new Data::Table([], \@lc, 1);
				next;
			} else {
				my @hdr = ();
				for (my $i=1; $i<scalar(@cols)+1; $i++) {
					push @hdr, "$i";
				}
				$table = new Data::Table([], \@hdr, 1);
			}
		}
		$table->addRow(\@cols);
	}
	return $table;
}

sub percent_encode {
	my $str = shift;
# !	*	'	(	)	;	:	@	&	=	+	$	,	/	?	#	[	]
# %21	%2A	%27	%28	%29	%3B	%3A	%40	%26	%3D	%2B	%24	%2C	%2F	%3F	%23	%5B	%5D
	my %enc = ( ' ' => '%20', 
							'!' => '%21', 
							'*' => '%2A', 
							"'" => '%27', 
							'(' => '%28', 
							')' => '%29', 
							';' => '%3B', 
							':' => '%3A', 
							'@' => '%40', 
							'&' => '%26', 
							'=' => '%3D', 
							'+' => '%2B', 
							'$' => '%24', 
							',' => '%2C', 
							'/' => '%2F', 
							'?' => '%3F', 
							'#' => '%23', 
							'[' => '%5B', 
							']' => '%5D' 
						);
	my @chars = split //, $str;
	for (my $i=0; $i < scalar(@chars); $i++) {
		$chars[$i] = $enc{$chars[$i]} if (defined $enc{$chars[$i]});
	}
	return join("", @chars);
}

sub parsetype {
	my $type = shift;
	my $b = {"default" => "boolean", "bool" => 1, "boolean" => 1, "b" => 1};
	my $i = {"default" => "int", "int" => 1, "integer" => 1, "i" => 1};
	my $l = {"default" => "long", "lng" => 1, "long" => 1, "l" => 1, };
	my $d = {"default" => "double", "dbl" => 1, "double" => 1, "d" => 1};
	my $f = {"default" => "float", "fl" => 1, "float" => 1, "f" => 1};
	my $s = {"default" => "string", "str" => 1, "string" => 1, "s" => 1};
	if (defined $type) {
		$type = lc $type;
		return $b->{"default"} if (exists $b->{$type});
		return $i->{"default"} if (exists $i->{$type});
		return $l->{"default"} if (exists $l->{$type});
		return $d->{"default"} if (exists $d->{$type});
		return $f->{"default"} if (exists $f->{$type});
		return $s->{"default"};
	} else {
		my $accepted = {"boolean" => ["boolean", "bool", "b"], 
										"int"     => ["integer", "int", "i"], 
										"long"    => ["long", "lng", "l"], 
										"double"  => ["double", "dbl", "d"], 
										"float"   => ["float", "fl", "fl"], 
										"string"  => ["string", "str", "s"] };
	}
}


sub format_file_as_table {
	my ($infile, $delim, $outfile, $width) = @_;
	
	open (IN, $infile) or die "Unable to open $infile for formatting: $!";
	
	my $tmpfile = $infile . "-t";
	if (defined $outfile && length $outfile > 0) {
		open (OUT, "> $outfile") or die "Unable to open $outfile for writing: $!";
	} else {
		open (OUT, "> $tmpfile") or die "Unable to open a tmp file for writing: $!";
	}
	
	my $linenum = 0;
	my $colreq = $width;
	while (<IN>) {
		chomp;
		my $line = $_;
		# skip blank lines
		next if ($line =~ /^\s*$/);
		$linenum++;
		my $colcount = $line =~ tr/\t//;
		# use the first line of the file to determine correct number of columns
		unless (defined $colreq) {
			#print "$colcount columns\n";
			$colreq = $colcount;
		}
		if ($colcount > $colreq) {
			my @cells = split /\t/, $line;
			$line = join "\t", @cells[0..$colreq-1];
		} else {
			for (my $i=$colcount; $i<$colreq-1; $i++) {
				#print "add tab to line $i\n";
				$line .= "\t";
			}
		}
		print OUT $line, "\n";
	}
	close IN;
	close OUT;
	
	unless (defined $outfile && length $outfile > 0) {
		rename($tmpfile, $infile);
	}
}


sub print_table {
	my ($table, $filename, $writehtml) = @_;
	
	unless (defined $filename) {
		print $table->tsv;
		return;
	}
	
	my $dfile = $filename . ".txt";
	$table->tsv( 1, {file=>$dfile} );
	
	if (defined $writehtml && $writehtml) {
		my $hfile = $filename . ".html";
		open(OUT2, "> $hfile");
		print OUT2 $table->html;
		close OUT2;
	}
}


sub print_matrix {
	my ($matrix, $delim) = @_;
	$delim = " " unless (defined $delim);
	foreach my $row (@$matrix) {
		foreach my $val (@$row) {
			print "${val}${delim}";
		}
		print "\n";
	}
}


sub read_ma_config {
	my $config_f = shift;
	my $opts = {};
	$opts->{"samples"} = {};
	my $sample = undef;
	open (IN, $config_f) or die "Unable to open $config_f for read: $!";
	while (<IN>) {
		chomp;
		my $line = $_;
		# skip blank lines
		next if ($line =~ /^\s*$/);
		# skip comments
		next if ($line =~ /^\s*#.*$/);
		my ($k, $v) = split /=/, $line, 2;
		next unless (defined $k && defined $v);
		$k = trim($k);
		$v = trim($v);
		if ($k eq "sample") {
			$sample = $v;
			$opts->{"samples"}->{$v} = {};
		} elsif (defined $sample && $k eq "name") {
			$opts->{"samples"}->{$sample}->{"name"} = $v;
		} elsif (defined $sample && $k eq "control") {
			$opts->{"samples"}->{$sample}->{"controls"} = {} unless defined($opts->{"samples"}->{$sample}->{"controls"});
			$opts->{"samples"}->{$sample}->{"controls"}->{$v} = 1;
		} elsif (defined $sample && $k eq "variable") {
			$opts->{"samples"}->{$sample}->{"variables"} = {} unless defined($opts->{"samples"}->{$sample}->{"variables"});
			$opts->{"samples"}->{$sample}->{"variables"}->{$v} = 1;
		} else {
			$sample = undef;
			$opts->{$k} = $v;
		}
	}
	close IN;
	return $opts;
}

sub format_type {
	my ($type) = shift;
	my $b = {"default" => "boolean", "bool" => 1, "boolean" => 1, "b" => 1};
	my $i = {"default" => "int", "int" => 1, "integer" => 1, "i" => 1};
	my $l = {"default" => "long", "lng" => 1, "long" => 1, "l" => 1};
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

sub oracle_esc {
	my $input = @_;
	
}

=pod
table_to_json($table, $printable)
accepts a HPI-style table and converts it into a json data structure
table columns are converted into fields, with each row a data array:

json = {fields: [{name: col_header. type: type}, ...],
				data:   [[col1, col2, ... coln], ...] }

Note: row 0 of an HPI-style table holds the data type for each column. 
parse this using the HPI::Tools->format_type method.

if $printable exists and is true, this method returns the json as a properly-formatted string. 
otherwise, it returns the perl hash ref that contains the json data.
sub table_to_json {
	my ($table, $printable) = @_;
	
	# init the json obj
	# fields is an array of hashes. data is an array of arrays
	my %json = ("fields" => [], "data" => []);
	# parse the fields from the table header
	my @hdr = $table->header;
	foreach my $field (@hdr) {
		push @{$json{"fields"}}, {"name" => $field, "type" => undef};
	}
	
	# parse the table data
	# store a tmp arr with data types for use in assembling a printable string
	my @types = ();
	for (my $i=0; $i<$table->nofRow; $i++) {
		my $vals = ();
		for (my $j=0; $j<$table->nofCol; $j++) {
			my $col = $hdr[$j];
			my $val = $table->elm($i, $col);
			# first row is the data type for this column
			# this is set in the fields hash
			if ($i == 0) {
				my $type = format_type($val);
				$type = "bool" if ($type eq "boolean");
				$json{"fields"}->[$j]->{"type"} = $type;
				push @types, $type;
			} else {
				//$val = "" if (!defined $val || $val eq "\\N" || $val eq "null");
				push @$vals, $val;
			}
		}
		push @{$json{"data"}}, $vals unless ($i == 0);
	}
	# return the object unless caller wants a printable string
	return \%json unless ($printable);
	
	my (@fields, @data) = ();
	foreach my $field (@{$json{"fields"}}) {
		my $name = $field->{"name"};
		my $type = $field->{"type"};
		($type eq "string") ? push @fields, "{ \"name\": \"$name\" }" 
												: push @fields, "{ \"name\": \"$name\", \"type\": \"$type\" }";
	}
	foreach my $row (@{$json{"data"}}) {
		my @cols = ();
		for (my $i=0; $i<scalar(@$row); $i++) {
			my $type = $types[$i];
			my $val = $row->[$i];
			if ($type eq "string" || $type eq "bool") {
				$val = encode_entities($val, "&");
				push @cols, "\"$val\"";
			} else {
				push @cols, "$val";
			}
		}
		push @data, "[" . join(", ", @cols) . "]";
	}
	
	my $count = scalar @data;
	my $printStr = "{\"fields\": [";
	$printStr .= join ", ", @fields;
	$printStr .= "], \"results\": $count, \"data\": [";
	$printStr .= join ", ", @data;
	$printStr .= "]}";
	return $printStr;
}
=cut

1;

