import sys, os, argparse
from collections import defaultdict
import numpy as np

def get_mean(directory):
	""" Calculates the mean of all the space separated rows in file f """
	values = {}

	# Assumes output/<controller>/<score-files> and nothing else. Score files
	# end with _score
	games = defaultdict(list)
	maxes = defaultdict(lambda: defaultdict(lambda: float(-999999999.)))
	mins = defaultdict(lambda: defaultdict(lambda: float(999999999.)))
	controllers = []
	for subdir, dirs, files in os.walk(directory):
		if files == []:
			continue
		controller = os.path.basename(subdir)
		values[controller] = {}
		for fi in files:
			if fi.endswith('_score'):
				# Filename is o_<game>_score. get gamename
				game = fi.split('_')[1].split('-')
				game_name = game[0]
				if len(game) > 1:
					game_level = game[1]
				else:
					game_level = ''
				if not(values[controller].has_key(game_name)):
					values[controller][game_name] = {}
				# Read values from file
				scores = \
					np.genfromtxt(os.path.join(subdir, fi)).tolist()
				# save values
				values[controller][game_name][game_level] = scores
				# get max and min for normalizing
				sorted_scores = sorted(x[1] for x in scores);
				if maxes[game_name][game_level] < sorted_scores[-1]:
					maxes[game_name][game_level] = sorted_scores[-1]
				if mins[game_name][game_level] > sorted_scores[0]:
					mins[game_name][game_level] = sorted_scores[0]
	normalize_score(values, maxes, mins)
	return values

def calculate_game_stats(values):
	stats = {}
	for controller, dic in values.iteritems():
		stats[controller] = {}
		for game in dic:
			stats[controller][game] = []
			for level, scores in values[controller][game].iteritems():
				if scores == []:
					continue
				if isinstance(scores[0], list):
					stats[controller][game].extend(scores)
				else:
					stats[controller][game].append(scores)
			# Here, normalize scores:
	return stats

def normalize_score(values, maxes, mins):
	for controller, dic in values.iteritems():
		for game in dic:
			for level in values[controller][game]:
				ma = maxes[game][level]
				mi = mins[game][level] if ma != mins[game][level] else 0
				values[controller][game][level] = \
					np.subtract(values[controller][game][level],
					np.array([0., float(mi), 0.]))
				if ma - mi != 0:
					values[controller][game][level] = \
						np.divide(values[controller][game][level],
						np.array([1., float(ma - mi), 1.]))

def print_stats(stats):
	""" Prints stats. Stats is built like this:
		stats[controller][game] = [scores]
	This function prints:
		mean, std, total
	"""
	for controller, dic in stats.iteritems():
		print "controller:", controller
		for level, v in dic.iteritems():
			print 'level:', level
			print '\tmean:  ', np.mean(v, 0)
			print '\tstd:   ', np.std(v, 0)
			print '\ttotal: ', np.sum(v, 0)



if __name__ == "__main__":
	values = get_mean('output')
	stats = calculate_game_stats(values)
	print_stats(stats)

