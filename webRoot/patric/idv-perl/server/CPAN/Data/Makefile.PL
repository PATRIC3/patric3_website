use ExtUtils::MakeMaker;
# See lib/ExtUtils/MakeMaker.pm for details of how to influence
# the contents of the Makefile that is written.

# perl 5.005 is required for qr() support
# see README for details
#
require 5.005;

WriteMakefile(
    'NAME'	   => 'Data::Table',
    'VERSION_FROM' => 'Table.pm', # finds $VERSION
    'PREREQ_PM'    => {
                         bytes => 0
                      }
);
