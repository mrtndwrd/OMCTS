import sys, os, argparse
import get_means
import numpy as np
from collections import defaultdict
from matplotlib import pyplot as plt

WIDTH=0.8
COLORS = ['blue', 'green', 'magenta', 'cyan']

def plot_means(directory='output', style='bar', show_score=False):
	""" Create boxplot for the runs in directory

	Keyword arguments: 
	directory -- An output directory containing runs with _o_*_score files
		Default: 'output'
	style -- the style of the plot, can be 'bar' or 'box', default: bar
	"""

	means = get_means.get_mean(directory)
	stats = get_means.calculate_game_stats(means)

	if style == 'bar':
		barplot_stats(stats, show_score)
	elif style == 'game':
		barplot_games(stats, show_score)

def barplot_stats(stats, show_score):
	""" Barplots the totals of stats """

	# variables 
	totals = defaultdict(list)
	controllers = stats.keys()
	name = 'score' if show_score else 'wins'
	# Loop through stats
	for i, (controller, dic) in enumerate(stats.iteritems()):
		print "controller:", controller
		for level, v in dic.iteritems():
			for win, score, time in v:
				if show_score:
					totals[controller].append(score)
				else:
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
	plt.title('Total %s of all runs, all levels' % name)
	plt.ylabel(name)
	plt.subplots_adjust(bottom=.4)
	plt.show()

def barplot_games(stats, show_score):
	# Create figure and get some axis
	fig = plt.figure()
	ax = fig.add_subplot(111)
	# This will contain all bars from the ax.bar command
	rects = []
	# Names of the controllers, for the legend:
	legend = []
	# Bar width
	width=.15
	name = 'score' if show_score else 'wins'
	for i, (controller, game_dic) in enumerate(stats.iteritems()):
		legend.append(controller)
		# Get the indices of all bars (hope N is always the same here...)
		N = len(game_dic)
		ind = np.arange(N)
		print "controller:", controller
		games = []
		totals = []
		stds = []
		for game, v in game_dic.iteritems():
			# Append game name to list of games
			games.append(game)
			total = 0
			number_of_games = len(v)
			avg = np.mean(v, 0)
			std = np.std(v, 0)
			index = 2 if show_score else 0
			totals.append(avg[index])
			stds.append(std[index])
		rects.append(ax.bar(ind + (i * width), totals, width, color=COLORS[i],
			yerr=stds, error_kw=dict(elinewith=2, ecolor='black')))

	ax.set_xlim(-width,len(ind)+width)
	#ax.set_ylim(0,45)
	ax.set_ylabel('Mean %s totals over %d games' % (name, number_of_games))
	ax.set_title('Total game %s by controller name' % name)
	xTickMarks = games 
	ax.set_xticks(ind+width)
	xtickNames = ax.set_xticklabels(xTickMarks)
	plt.setp(xtickNames, rotation=45, fontsize=10)

	## add a legend
	ax.legend( rects, legend )

	plt.show()

if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Barplot total wins')
	parser.add_argument('output', metavar='output',
						help='output directory')
	parser.add_argument('-t', '--type', 
		help="""type of plot that should be made. "bar" for all games at once, "game" for plot per game""", 
		default='bar')
	parser.add_argument('--score', '-s', action='store_true',
		help="If this argument is given, scores are plotted")
	args = parser.parse_args()
	plot_means(args.output, args.type, show_score=args.score)


