#!/bin/bash
grid_size=100
blocking=0
gather=0
make -f p2.makefile.test p2-test
mkdir results
for fn_index in 0 1 2 3 4 5 6 7
do
	for grid_size in 100 1000 10000
	do
		blocking=0
		gather=0
		prun ./p2 $grid_size $blocking $gather $fn_index > result.$grid_size.$blocking.$gather.$fn_index
		mkdir results/dir.$grid_size.$blocking.$gather.$fn_index
		echo "filename='fn-$grid_size'" > plotcmd
		cat p2.gnu >> plotcmd
		gnuplot plotcmd
		mv fn-$grid_size.png results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv result.$grid_size.$blocking.$gather.$fn_index results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv fn-$grid_size.dat results/dir.$grid_size.$blocking.$gather.$fn_index/ 

		blocking=0
		gather=1
		prun ./p2 $grid_size $blocking $gather $fn_index > result.$grid_size.$blocking.$gather.$fn_index
		mkdir results/dir.$grid_size.$blocking.$gather.$fn_index
		echo "filename='fn-$grid_size'" > plotcmd
		cat p2.gnu >> plotcmd
		gnuplot plotcmd
		mv fn-$grid_size.png results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv result.$grid_size.$blocking.$gather.$fn_index results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv fn-$grid_size.dat results/dir.$grid_size.$blocking.$gather.$fn_index/

		blocking=1
		gather=0
		prun ./p2 $grid_size $blocking $gather $fn_index > result.$grid_size.$blocking.$gather.$fn_index
		mkdir results/dir.$grid_size.$blocking.$gather.$fn_index
		echo "filename='fn-$grid_size'" > plotcmd
		cat p2.gnu >> plotcmd
		gnuplot plotcmd
		mv fn-$grid_size.png results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv result.$grid_size.$blocking.$gather.$fn_index results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv fn-$grid_size.dat results/dir.$grid_size.$blocking.$gather.$fn_index/

		blocking=1
		gather=1
		prun ./p2 $grid_size $blocking $gather $fn_index > result.$grid_size.$blocking.$gather.$fn_index
		mkdir results/dir.$grid_size.$blocking.$gather.$fn_index
		echo "filename='fn-$grid_size'" > plotcmd
		cat p2.gnu >> plotcmd
		gnuplot plotcmd
		mv fn-$grid_size.png results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv result.$grid_size.$blocking.$gather.$fn_index results/dir.$grid_size.$blocking.$gather.$fn_index/
		mv fn-$grid_size.dat results/dir.$grid_size.$blocking.$gather.$fn_index/
	done
done
