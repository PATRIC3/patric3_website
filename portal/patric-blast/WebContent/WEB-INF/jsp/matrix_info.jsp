<h3> <a name = matrix>Matrix</a> </h3>

<P>
  A key element in evaluating the quality of a pairwise sequence alignment
is the "substitution matrix", which assigns a score for aligning any possible
pair of residues.  The theory of amino acid substitution matrices is described
in [1], and applied to DNA sequence comparison in [2].  In general, different
substitution matrices are tailored to detecting similarities among sequences
that are diverged by differing degrees [1-3].  A single matrix may nevertheless
be reasonably efficient over a relatively broad range of evolutionary change
[1-3].  Experimentation has shown that the BLOSUM-62 matrix [4] is among the
best for detecting most weak protein similarities.  For particularly long
and weak alignments, the BLOSUM-45 matrix may prove superior.  A detailed
statistical theory for gapped alignments has not been developed, and the best
<a href = "#open">gap costs</a> to use with a given substitution matrix are determined empirically.
  Short alignments need to be relatively strong (i.e. have a higher percentage
of matching residues) to rise above background noise.  Such short but strong
alignments are more easily detected using a matrix with a higher "relative
entropy" [1] than that of BLOSUM-62.  In particular, short query sequences
can only produce short alignments, and therefore database searches with
short queries should use an appropriately tailored matrix.  The BLOSUM series
does not include any matrices with relative entropies suitable for the shortest
queries, so the older PAM matrices [5,6] may be used instead.  For proteins,
a provisional table of recommended substitution matrices and gap costs for
various query lengths is:</P>

<PRE>
     Query length     Substitution matrix     Gap costs
     ------------     -------------------     ---------
     &lt;35              PAM-30                  ( 9,1)
     35-50            PAM-70                  (10,1)
     50-85            BLOSUM-80               (10,1)
     &gt;85              BLOSUM-62               (11,1)

</PRE>

<a name = extended></a>
<h3> <a name = open>Gap Costs </a></h3>
<P>
  The raw score of an alignment is the sum of the scores for aligning pairs of
residues and the scores for gaps.  Gapped BLAST and PSI-BLAST use "affine gap
costs" which charge the score -a for the existence of a gap, and the score -b
for each residue in the gap.  Thus a gap of k residues receives a total score
of -(a+bk); specifically, a gap of length 1 receives the score -(a+b).</P>

<h3> <a name = lambda>Lambda Ratio </a></h3>
<P>
  To convert a raw score S into a normalized score S' expressed in bits,
one uses the formula S' = (lambda*S - ln K)/(ln 2), where lambda and K are
parameters dependent upon the scoring system (substitution matrix and gap
costs) employed [7-9].  For determining S', the more important of these
parameters is lambda.  The "lambda ratio" quoted here is the ratio of the
lambda for the given scoring system to that for one using the same substitution
scores, but with infinite gap costs [8].  This ratio indicates what proportion
of information in an ungapped alignment must be sacrificed in the hope of
improving its score through extension using gaps.  We have found empirically
that the most effective gap costs tend to be those with lambda ratios in the
range 0.8 to 0.9.</P>

<PRE>
<A HREF="http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?uid=91269329&form=6&db=m&Dopt=r">[1]</A> Altschul, S.F. (1991) "Amino acid substitution matrices from an information
    theoretic perspective." J. Mol. Biol. 219:555-565.
[2] States, D.J., Gish, W. &amp; Altschul, S.F. (1991) "Improved sensitivity of
    nucleic acid database searches using application-specific scoring matrices."
    Methods 3:66-70.
<A HREF="http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?uid=93247069&form=6&db=m&Dopt=r">[3]</A> Altschul, S.F. (1993) "A protein alignment scoring system sensitive at all
    evolutionary distances." J. Mol. Evol. 36:290-300.
<A HREF="http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?uid=93066354&form=6&db=m&Dopt=r">[4]</A> Henikoff, S. &amp; Henikoff, J.G. (1992) "Amino acid substitution matrices from
    protein blocks." Proc. Natl. Acad. Sci. USA 89:10915-10919.
[5] Dayhoff, M.O., Schwartz, R.M. &amp; Orcutt, B.C. (1978) "A model of evolutionary
    change in proteins." In "Atlas of Protein Sequence and Structure, vol. 5,
    suppl. 3," M.O. Dayhoff (ed.), pp. 345-352, Natl. Biomed. Res. Found.,
    Washington, DC.
[6] Schwartz, R.M. &amp; Dayhoff, M.O. (1978) "Matrices for detecting distant
    relationships." In "Atlas of Protein Sequence and Structure, vol. 5,
    suppl. 3," M.O. Dayhoff (ed.), pp. 353-358, Natl. Biomed. Res. Found.,
    Washington, DC.
<A HREF="http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?uid=90192788&form=6&db=m&Dopt=r">[7]</A> Karlin, S. &amp; Altschul, S.F. (1990) "Methods for assessing the statistical
    significance of molecular sequence features by using general scoring
    schemes." Proc. Natl. Acad. Sci. USA 87:2264-2268.
<A HREF="http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?uid=8743700&form=6&db=m&Dopt=r">[8]</A> Altschul, S.F. &amp; Gish, W. (1996) "Local alignment statistics." Meth.
    Enzymol. 266:460-480.**
<A HREF="http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?uid=9254694&form=6&db=m&Dopt=r">[9]</A> Altschul, S.F., Madden, T.L., Sch&auml;ffer, A.A., Zhang, J., Zhang, Z., Miller,
    W. &amp; Lipman, D.J. (1997) "Gapped BLAST and PSI-BLAST: a new generation of
    protein database search programs." Nucleic Acids Res. 25:3389-3402.
</PRE>