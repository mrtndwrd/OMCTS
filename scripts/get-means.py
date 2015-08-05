import sys, os, argparse
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
	print calculate_game_stats(values)

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

	for controller, dic in stats.iteritems():
		print "controller:", controller
		for level, v in dic.iteritems():
			print 'level:', level
			print '\tmean:  ', np.mean(v, 0)
			print '\tstd:   ', np.std(v, 0)
			print '\ttotal: ', np.sum(v, 0)


if __name__ == "__main__":
	get_mean('output')

