import sys, os, argparse, glob
import numpy as np
from collections import defaultdict
from matplotlib import pyplot as plt

""" Plots the option ranking tables that are written to complete_output as
written by the print in writeOptionRanking in Agent.java """

def get_option_rankings(directory):
	#defaultdict of defaultdicts of lists
	values = defaultdict(lambda: defaultdict(list))

	# directory is assumed to have files complete_output_<number> in them 
	for filepath in glob.iglob(directory + "/complete_output_*"):
		filename = os.path.basename(filepath)
		with open(filepath) as f:
			parsing = False
			i = -1 
			for line in f.readlines():
				if line.startswith("Final option ranking: "):
					line = line.split("Final option ranking: ")[1]
					# start parsing
					parsing = True
					i += 1
				if parsing:
					l = line.split(' -> ')
					if(len(l) != 2):
						parsing = False
						continue
					# prepend with zero-values if needed
					if values[l[0]][filename] == [] and i != 0:
						for j in range(i):
							values[l[0]][filename].append(0.)
					# add and strip \n
					values[l[0]][filename].append(float(l[1][:-1]))

	return values

def make_plot(values):
	new_values = {}
	# Set new_values to a dictionary like:
	# {option: [[run1, run2 ...], [run1, ...], ... ], ...}
	for option, dic in values.iteritems():
		new_values[option] = []
		for ranking_list in dic.values():
			new_values[option].append(ranking_list)
		new_values[option] = np.array(new_values[option])
	print new_values
	# Per option, add a line:
	fig = plt.figure(figsize=(5, 5))
	ax = fig.add_subplot(111)
	colour_index = 0
	for option, values in new_values.iteritems():
		print "Values:", values
		print "Average:", np.average(values, axis=0)
		averages = np.average(values, axis=0)
		stds = np.std(values, axis=0)
		xs = range(0, len(averages))
		bounds = ((y[0] - y[1], y[0] + y[1]) for y in zip(averages, stds))
		ymax, ymin = zip(*bounds)
		ax.plot(xs, averages, label=option, linewidth=1.0)
		col = ax.get_lines()[-1].get_color()
		ax.fill_between(xs, ymax, ymin, alpha=.3, edgecolor="w",
							 color=col)
	plt.legend()
	plt.show()





if __name__ == "__main__":
	values = get_option_rankings('../output')
	make_plot(values)
