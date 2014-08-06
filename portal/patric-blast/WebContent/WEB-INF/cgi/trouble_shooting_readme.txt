------------------------------------------------------------------------

    Quick Guide to Fix Known Problems 

------------------------------------------------------------------------


1. Check file permissions for executable files.

# ls -al *.REAL
# chmod +x *.REAL


2. Check you have "TmpGifs" directory with writable permission for Web user

# mkdir TmpGifs
# chomd 777 TmpGifs


3. Check you have a symbolic link to db directory

# ln -s /opt/jboss/patric-blast-db db


4. Check blast.rc file has a correct configuration

/*
blastn refseq_cds refseq_rna brc_cds brc_rna patric_cds patric_rna genome_seq
blastp refseq_protein brc_protein patric_protein
blastx refseq_protein brc_protein patric_protein
tblastn refseq_cds refseq_rna brc_cds brc_rna patric_cds patric_rna genome_seq
tblastx refseq_cds refseq_rna brc_cds brc_rna patric_cds patric_rna genome_seq
*/ 