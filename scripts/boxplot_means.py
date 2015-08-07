from collections import defaultdict
import get_means
import numpy as np
from matplotlib import pyplot as plt

WIDTH=0.8

def plot_means(directory='output', style='bar'):
	""" Create boxplot for the runs in directory

	Keyword arguments: 
	directory -- An output directory containing runs with _o_*_score files
		Default: 'output'
	style -- the style of the plot, can be 'bar' or 'box', default: bar
	"""

	means = get_means.get_mean(directory)
	stats = get_means.calculate_game_stats(means)

	if style == 'bar':
		barplot_stats(stats)

def barplot_stats(stats):
	""" Barplots the totals of stats """

	# variables 
	totals = defaultdict(list)
	controllers = stats.keys()

	# Loop through stats
	for i, (controller, dic) in enumerate(stats.iteritems()):
		print "controller:", controller
		for level, v in dic.iteritems():
			for win, score, time in v:
				totals[controller].append(win)
		plt.bar(i, np.sum(totals[controller]), label=controller, align='center', 
				width=WIDTH)
	ax=plt.gca()
	# Set ticks to each tick + width
	ax.set_xticks(np.arange(len(controllers)))
	# Set labels to the ticks
	ax.set_xticklabels(controllers)
	for label in ax.get_xticklabels():
		label.set_rotation('vertical')
	plt.title('Total scores of all runs, all levels')
	plt.ylabel('Score')
	plt.subplots_adjust(bottom=.4)
	plt.show()

if __name__ == "__main__":
	plot_means('output', 'bar')

