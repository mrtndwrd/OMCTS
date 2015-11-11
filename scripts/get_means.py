import sys, os, argparse
from collections import defaultdict
import numpy as np

def get_mean(directory):
	""" Calculates the mean of all the space separated rows in file f """
	values = {}

	# Assumes output/<controller>/<score-files> and nothing else. Score files
	# end with _score
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
				game_level = game[1]
				if not(values[controller].has_key(game_name)):
					values[controller][game_name] = {}
				values[controller][game_name][game_level] = \
					np.genfromtxt(os.path.join(subdir, fi)).tolist()
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
	normalize_score(stats)
	return stats

def normalize_score(stats):
	games = defaultdict(list)
	controllers = []
	for controller, game_dic in stats.iteritems():
		controllers.append(controller)
		for game, v in game_dic.iteritems():
			games[game].extend(v)
	for game, values in games.iteritems():
		scores = sorted([x[1] for x in values])
		max = scores[-1]
		min = scores[0]
		for controller in controllers:
			stats[controller][game] = np.subtract(stats[controller][game], 
				np.array([0., float(min), 0.]))
			if max != 0:
				stats[controller][game] = np.divide(stats[controller][game], 
					np.array([1., float(max), 1.]))

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

