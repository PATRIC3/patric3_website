
karyotype={{folder}}/data/karyotype.txt

chromosomes_order_by_karyotype = yes
chromosomes_units              = 1000
chromosomes_display_default    = yes

<<include {{folder}}/conf/ideogram.conf>>
<<include {{folder}}/conf/ticks.conf>>
<<include {{folder}}/conf/plots.conf>>

<image>
<<include {{folder}}/conf/image.conf>>
</image>

# includes etc/colors.conf
#          etc/fonts.conf
#          etc/patterns.conf
<<include etc/colors_fonts_patterns.conf>>

# system and debug settings
<<include etc/housekeeping.conf>>

anti_aliasing* = no
