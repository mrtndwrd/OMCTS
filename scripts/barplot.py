import sys, os, argparse
import get_means
import numpy as np
import itertools
from collections import defaultdict
from matplotlib import pyplot as plt

WIDTH=0.8
COLORS = ['black', 'blue', 'red', 'yellow','magenta', 'cyan',  'black']

def plot_means(directory='output', style='bar', show_score=False, filename=None):
	""" Create barplot for the runs in directory

	Keyword arguments: 
	directory -- An output directory containing runs with _o_*_score files
		Default: 'output'
	style -- the style of the plot, can be 'bar' or 'box', default: bar
	"""

	means = get_means.get_mean(directory)
	stats = get_means.calculate_game_stats(means)

	if style == 'bar':
		barplot_stats(stats, show_score, filename)
	elif style == 'game':
		barplot_games(stats, show_score, filename, order_by_controller="MCTS")

def barplot_stats(stats, show_score, filename=None):
	""" Barplots the totals of stats """
	# variables 
	totals = defaultdict(list)
	controllers = stats.keys()
	name = 'score' if show_score else 'wins'
	# Loop through stats
	for i, (controller, dic) in enumerate(stats.iteritems()):
		print "controller:", controller
		for game, v in dic.iteritems():
			v = itertools.chain(*v)
			for win, score, time in v:
				if show_score:
					totals[controller].append(score)
				else:
					totals[controller].append(win)
		plt.bar(i, np.sum(totals[controller]), label=controller, align='center', 
				width=WIDTH)
	print "Totals:"
	for controller in totals.keys():
		print "%s: %f" % (controller, np.sum(totals[controller]))
	ax=plt.gca()
	# Set ticks to each tick + width
	ax.set_xticks(np.arange(len(controllers)))
	# Set labels to the ticks
	ax.set_xticklabels(controllers)
	for label in ax.get_xticklabels():
		label.set_rotation('vertical')
	plt.title('Total %s of all runs, all games' % name)
	plt.ylabel(name)
	plt.subplots_adjust(bottom=.4)
	plt.show()

def barplot_games(stats, show_score, filename=None, order_by_controller=None,
		order_by_list=None, order_by_column=0):
	""" Plots a bar plot of all individual games, with bar colors per controller
	in stats. 
		Params:
			- show_score: If this is true, the score is shown, else the wins are
			  shown
			- filename: if given, the plot is saved to this file
			- order_by_controller: Should contain a controller name to order by that
			  controller's score or wins.
			- order_by_list: Should contain a list of game names, these games
			  are plotted in order
			- order_by_column: This contains on which column the ordering is
			  done. 0 = win, 1 = score, 2 = time. Default = 0
	"""
	# Custom ordering:
	if order_by_controller != None:
		print "Ordering by controller", order_by_controller
		if not(stats.has_key(order_by_controller)):
			print "Controller", order_by_controller, "does not exist"
			exit()
		ordered_labels = order_labels(stats[order_by_controller],
				order_by_column)
	elif order_by_list != None:
		print "Ordering by list", order_by_list
		ordered_labels = order_by_list
	# By default order by the first controller's wins
	else:
		ordered_labels = order_labels(stats[stats.get_keys()[0]],
				order_by_column)

	# Create figure and get some axis
	fig = plt.figure(figsize=(14, 3.3))
	# Add some space to the bottom
	plt.subplots_adjust(bottom=.3)
	ax = fig.add_subplot(111)
	# This will contain all bars from the ax.bar command
	rects = []
	# Names of the controllers, for the legend:
	legend = []
	# Bar width
	width=.8/float(len(stats.keys()))
	name = 'Mean score totals' if show_score else 'Win ratio'
	for i, (controller, game_dic) in enumerate(stats.iteritems()):
		legend.append(controller)
		# Get the indices of all bars (hope N is always the same here...)
		N = len(game_dic)
		ind = np.arange(N)
		print "controller:", controller
		totals = []
		stds = []
		for game in ordered_labels:
			v = list(itertools.chain(*game_dic[game]))
			total = 0
			number_of_games = len(v)
			avg = np.mean(v, 0)
			std = np.std(v, 0)
			index = 1 if show_score else 0
			totals.append(avg[index])
			stds.append(std[index])
		rects.append(ax.bar(ind + (i * width), totals, width, color=COLORS[i]))
			# If we want to plot the std:
			#yerr=stds, error_kw=dict(elinewith=2, ecolor='black')))

	ax.set_xlim(-width,len(ind)+width)
	#ax.set_ylim(0,45)
	ax.set_ylabel('%s over %d games' % (name, number_of_games))
	ax.set_title('Total game %s by controller name' % name)
	xTickMarks = ordered_labels
	ax.set_xticks(ind+width)
	xtickNames = ax.set_xticklabels(xTickMarks)
	plt.setp(xtickNames, rotation=45, fontsize=9, horizontalalignment='right')

	# Shrink current axis's height by 10% on the bottom
	box = ax.get_position()
	ax.set_position([box.x0, box.y0, box.width, 
		box.height * 0.8])

	# Put a legend below current axis
	ax.legend(rects, legend, loc='upper center', bbox_to_anchor=(0.5, 1.5),
		fancybox=True, ncol=len(legend))


	## add a legend
	#ax.legend( rects, legend )

	if filename == None:
		plt.show()
	else:
		fig.savefig(filename)
		print "Barplot saved to", filename

def order_labels(controller_stats, index):
	""" Orders the labels in controller_stats by the index in the scores """
	values = {}
	for game, levels in controller_stats.iteritems():
		# Flatten level list once to all scores (and wins and times)
		scores = list(itertools.chain(*levels))
		score = sum(scores, 0)
		values[game] = score
	print "sorting values by", index, "th column"
	values = sorted(values, cmp=lambda x, y: cmp(values[x][index],
		values[y][index]), reverse=True)
	return values


if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Barplot total wins')
	parser.add_argument('output', metavar='output',
						help='output directory')
	parser.add_argument('-t', '--type', 
		help="""type of plot that should be made. "bar" for all games at once, "game" for plot per game""", 
		default='bar')
	parser.add_argument('--score', '-s', action='store_true',
		help="If this argument is given, scores are plotted")
	parser.add_argument('--file', '-f', metavar='file',
		help="If this argument is given plots are written to file f")
	args = parser.parse_args()
	plot_means(args.output, args.type, show_score=args.score, filename=args.file)
