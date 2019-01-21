# heatmap for lake.cu
# Required files from homework assignment:
# V0: lake_i.dat, lake_f.dat
# V1: lake_i.dat, lake_f.dat (exact file name not specified but this is what we use)
# V2: lake_i.dat, lake_f.dat (exact file name not specified but this is what we use)
# V3: n/a
# V4: lake_f_0.dat, lake_f_1.dat, lake_f_2.dat, lake_f_3.dat

# We only want to print a *.png if the applicable *.dat is present
# https://stackoverflow.com/questions/11610625/gnuplot-how-to-skip-missing-data-files

file_exists(file) = system("[ -f '".file."' ] && echo '1' || echo '0'") + 0

set terminal png

set xrange[0:1]
set yrange[0:1]

if ( file_exists('lake_i.dat') ) {
    set output 'lake_i.png'
    plot 'lake_i.dat' using 1:2:3 with image
}

if ( file_exists('lake_f.dat') ) {
    set output 'lake_f.png'
    plot 'lake_f.dat' using 1:2:3 with image
}

if ( file_exists('lake_f_0.dat') ) {
    set output 'lake_f_0.png'
    plot 'lake_f_0.dat' using 1:2:3 with image
}

if ( file_exists('lake_f_1.dat') ) {
    set output 'lake_f_1.png'
    plot 'lake_f_1.dat' using 1:2:3 with image
}

if ( file_exists('lake_f_2.dat') ) {
    set output 'lake_f_2.png'
    plot 'lake_f_2.dat' using 1:2:3 with image
}

if ( file_exists('lake_f_3.dat') ) {
    set output 'lake_f_3.png'
    plot 'lake_f_3.dat' using 1:2:3 with image
}
