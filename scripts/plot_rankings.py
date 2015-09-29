import sys, os, argparse, glob
import numpy as np
from collections import defaultdict
from matplotlib import pyplot as plt

""" Plots the option ranking tables that are written to complete_output as
written by the print in writeOptionRanking in Agent.java """

def get_option_rankings(directory):
	""" takes all option rankings from files named complete_output_* in the
	output-directory directory. 


	Keyword arguments:
	directory -- the directory with complete_output_ files
	"""

	# defaultdict of defaultdicts of lists
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
						print filepath, i
						continue
					# prepend with zero-values if needed
					if values[l[0]][filename] == [] and i != 0:
						for j in range(i):
							values[l[0]][filename].append(0.)
					# add and strip \n
					values[l[0]][filename].append(float(l[1][:-1]))
	return values

def make_plot(values, contains, legend_location):
	""" Plots the values in values


	Keyword arguments:
	values -- the values that should be plot as returned by get_option_rankings
	contains -- the option should contain this string in order to make it into
	the graph (useful for, for example, only plotting action_options)
	""" 
	new_values = {}
	# Set new_values to a dictionary like:
	# {option: [[run1, run2 ...], [run1, ...], ... ], ...}
	for option, dic in values.iteritems():
		# check if contains is a substring of option
		if option.find(contains) != -1:
			new_values[option] = []
			for ranking_list in dic.values():
				new_values[option].append(ranking_list)
			new_values[option] = np.array(new_values[option])

	# Per option, add a line for mean and std. to the plot:
	fig = plt.figure(figsize=(5, 5))
	ax = fig.add_subplot(111)
	for option, values in new_values.iteritems():
		averages = np.average(values, axis=0)
		stds = np.std(values, axis=0)
		xs = range(0, len(averages))
		bounds = ((y[0] - y[1], y[0] + y[1]) for y in zip(averages, stds))
		ymax, ymin = zip(*bounds)
		ax.plot(xs, averages, label=option, linewidth=2.0)
		col = ax.get_lines()[-1].get_color()
		ax.fill_between(xs, ymax, ymin, alpha=.3, edgecolor="w",
							 color=col)
	plt.legend(loc=legend_location)
	plt.show()





if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Barplot total wins')
	parser.add_argument('-o', '--output', 
			help='directory with complete_output_* files', default="output/")
	parser.add_argument('-c', '--contains', 
			help='option name contains', default="")
	parser.add_argument('-l', '--legend-location', 
			help="""location of the legend in the plot. can be one of the
			following: 'best' 'upper right' 'upper left' 'lower left'
			'lower right' 'right' 'center left' 'center right' 'lower center'
			'upper center' 'center'""", default="best")
	args = parser.parse_args()
	values = get_option_rankings(args.output)
	make_plot(values, args.contains, args.legend_location)