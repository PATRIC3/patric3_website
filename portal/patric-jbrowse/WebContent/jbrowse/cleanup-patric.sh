for P in JBrowse-patric/INSTALL \
		JBrowse-patric/LICENSE \
		JBrowse-patric/Makefile.PL \
		JBrowse-patric/README.md \
		JBrowse-patric/bin \
		JBrowse-patric/build-patric.sh \
		JBrowse-patric/compat_121.html \
		JBrowse-patric/docs \
		JBrowse-patric/index.html \
		JBrowse-patric/release-notes.txt \
		JBrowse-patric/sample_data \
		JBrowse-patric/setup.sh \
	; do \
		echo "rm -rf $P"; \
		rm -rf $P; \
	done
find ./JBrowse-patric -name '.DS_Store' -exec rm -rf {} \;
