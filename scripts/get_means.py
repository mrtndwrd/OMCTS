import sys, os, argparse
from collections import defaultdict
import numpy as np

#EXCLUDED_GAMES = ['sokoban', 'brainman', 'chipschallenge', 'modality',
#	'realportals', 'painter', 'realsokoban', 'thecitadel', 'zenpuzzle',
#	'catapults', 'labyrinth', 'escape', ]
EXCLUDED_GAMES = ['sokoban', 'brainman', 'chipschallenge', 'modality',
	'realportals', 'painter', 'realsokoban', 'thecitadel', 'zenpuzzle',
	'catapults', 'labyrinth', 'escape', 'infection', 'butterflies',
	'missilecommand', 'whackamole', 'aliens', 'plants', 'camelrace',
	'survivezombies', 'pacman', 'firestorms', 'boulderdash',
	'overload', 'roguelike', 'boulderchase', 'zelda', 'chase', 'digudug', ]

EXCLUDED_LEVELS = []

def get_mean(directory):
	""" Calculates the mean of all the space separated rows in file f """
	values = defaultdict(dict)

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
		#values[controller] = {}
		for fi in files:
			if fi.endswith('_score'):
				# Filename is o_<game>_score. get gamename
				game = fi.split('_')[1].split('-')
				game_name = game[0]
				if game_name in EXCLUDED_GAMES:
					continue
				if len(game) > 1:
					game_level = game[1]
				else:
					game_level = ''
				if game_level in EXCLUDED_LEVELS:
					continue
				# Read values from file
				scores = \
					np.genfromtxt(os.path.join(subdir, fi)).tolist()
				# If there are several scores, take the first and the last (this
				# is for OLMCTS)
				if type(scores[0]) == list:
					# Jua kali special case for Q-learning
					if(controller == 'Q-LEARNING'):
						index = 3
						current_controllers = \
							[("%s%d" % (controller, 1), scores[0]),
								("%s%d" % (controller, index+1), scores[index])]
					else:
						current_controllers = \
							[("%s%d" % (controller, 1), scores[0]),
								("%s%d" % (controller, len(scores)), scores[-1])]
				else:
					current_controllers = [(controller, scores)]
				for c, s in current_controllers:
					if not(values[c].has_key(game_name)):
						values[c][game_name] = defaultdict(list)
					# save values
					values[c][game_name][game_level].append(s)
					# Sort by score if there are more than one score in the file
					if type(s[0]) == list:
						sorted_scores = sorted(x[1] for x in s);
					else:
						sorted_scores = [s[1]]
					# get max and min for normalizing
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

