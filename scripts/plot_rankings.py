import sys, os, argparse, glob
import numpy as np
from collections import defaultdict
from matplotlib import pyplot as plt

""" Plots the option ranking tables that are written to file_prefix as
written by the print in writeOptionRanking in Agent.java """

def get_option_rankings(directory, file_prefix):
	""" takes all option rankings from files named file_prefix_* in the
	output-directory directory. 


	Keyword arguments:
	directory -- the directory with complete_output_ files
	"""

	# defaultdict of defaultdicts of lists
	values = defaultdict(lambda: defaultdict(list))
	variances = defaultdict(lambda: defaultdict(list))

	# directory is assumed to have files complete_output_<number> in them 
	for filepath in glob.iglob(directory + "/" + file_prefix + "*"):
		filename = os.path.basename(filepath)
		with open(filepath) as f:
			parsing = False
			i = -1 
			for line in f.readlines():
				if line.startswith("Final option ranking: "):
					# line = line.split("Final option ranking: ")[1]
					# start parsing
					parsing = True
					d = values
					i += 1
				elif line.startswith("Final option variance: "):
					# line = line.split("Final option variance: ")[1]
					# start parsing
					parsing = True
					d = variances
				elif parsing:
					l = line.split(' -> ')
					if len(l) != 2:
						if l != ['\n']:
							print "Line %s not accepted" % l
						parsing = False
						continue
					# prepend with zero-values if needed
					if d[l[0]][filename] == [] and i != 0:
						for j in range(i):
							d[l[0]][filename].append(0.)
					# add and strip \n
					d[l[0]][filename].append(float(l[1][:-1]))
	print variances
	return values, variances

def make_plot(values, contains, legend_location, variance=None, plot_variance=False):
	""" Plots the values in values


	Keyword arguments:
	values -- the values that should be plot as returned by get_option_rankings
	contains -- (list) the option should contain these strings in order to make it into
	the graph (useful for, for example, only plotting action_options)
	variance -- A dictionary containing the variances of values
	plot_variance -- if this is true, the values' variance is plotted
	""" 
	new_values = {}
	new_variances = {}
	# Set new_values to a dictionary like:
	# {option: [[run1, run2 ...], [run1, ...], ... ], ...}
	for option, dic in values.iteritems():
		# check if contains is a substring of option
		for c in contains:
			if option.find(c) != -1:
				new_values[option] = []
				for ranking_list in dic.values():
					new_values[option].append(ranking_list)
				new_values[option] = np.array(new_values[option])
	for option, dic in variances.iteritems():
		# check if contains is a substring of option
		for c in contains:
			if option.find(c) != -1:
				new_variances[option] = []
				for ranking_list in dic.values():
					new_variances[option].append(ranking_list)
				new_variances[option] = np.array(new_variances[option])

	# Per option, add a line for mean and std. to the plot:
	fig = plt.figure(figsize=(5, 5))
	ax = fig.add_subplot(111)
	if len(new_values.keys()) == 0:
		print "No values found"
		return
	for option, values in new_values.iteritems():
		print "adding option", option
		print [np.size(v) for v in values]
		print [np.size(v) for v in new_variances[option]]
		averages = np.average(values, axis=0)
		# stds = np.std(values, axis=0)
		stds = np.average(new_variances[option], axis=0)
		xs = range(0, len(averages))
		bounds = ((y[0] - y[1], y[0] + y[1]) for y in zip(averages, stds))
		ymax, ymin = zip(*bounds)
		ax.plot(xs, averages, label=option, linewidth=2.0)
		col = ax.get_lines()[-1].get_color()
		if plot_variance:
			ax.fill_between(xs, ymax, ymin, alpha=.3, edgecolor="w",
							 color=col)
	# plt.ylim([-1, 1])
	plt.legend(loc=legend_location)
	plt.show()





if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Barplot total wins')
	parser.add_argument('-o', '--output', 
			help='directory with output files', default="output/")
	parser.add_argument('-c', '--contains', 
			help='option name contains', default=[''], nargs="+")
	parser.add_argument('-l', '--legend-location', 
			help="""location of the legend in the plot. can be one of the
			following: 'best' 'upper right' 'upper left' 'lower left'
			'lower right' 'right' 'center left' 'center right' 'lower center'
			'upper center' 'center'""", default="best")
	parser.add_argument('-s', '--plot-variance', 
			help="""if this is set, variance is plotted into the graph""",
			dest="plot_variance", action="store_true")
	parser.add_argument('-f', '--file-prefix', 
			help="""score file prefix""",
			dest="file_prefix", default="complet_output_")
	parser.set_defaults(plot_variance=False)
	args = parser.parse_args()
	values, variances = get_option_rankings(args.output, args.file_prefix)
	make_plot(values, args.contains, args.legend_location, plot_variance=args.plot_variance,
			variance=variances)
