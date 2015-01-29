import sys, os, argparse
import matplotlib.pyplot as plt
import matplotlib as mpl
import numpy as np


class HyperVolumePlotter:
    def __init__(self, output_file='plot.pdf',
        y_min=None, y_max=None, wins=False, score=False, time=False):
        self.output_file = output_file
        self.fig = plt.figure(figsize=(5, 5))
        self.ax = self.fig.add_subplot(111)
        self.colour_index = 0
        self.y_min = y_min
        self.y_max = y_max
        self.wins = wins
        self.score = score
        self.time = time
        self.all_data = {'wins': [], 'score': [], 'time': []}

    def add_file(self, filename):
        """ Opens filename and adds a plot to the current plot. There has to be
        three columns in the file: wins score time, divided by spaces. All are
        assumed to be convertable to float"""
        # Open file
        with open(filename) as f:
            data = f.readlines()
            # compare to a random entry all_data. Length should be the same
            for tag in ['wins', 'score', 'time']:
                # We can assume that all_data[tag] has the same length for every
                # entry
                if self.all_data[tag] and len(data) != len(self.all_data[tag][0]):
                    for i in range(len(self.all_data[tag])):
                        # Trim one of the data lengths:
                        self.trim_either(self.all_data[tag][i], data)
            # Split all rules in the file by space
            data_split = []
            for d in data:
                data_split.append(d.split())
            # Add data to all time data's
            self.all_data['wins'].append([float(d[0]) for d in data_split])
            self.all_data['score'].append([float(d[1]) for d in data_split])
            self.all_data['time'].append([float(d[2]) for d in data_split])

    def trim_either(self, d1, d2):
        """ Removes the last entries from either of the lists in order to get
        two lists of the same length """
        if len(d1) > len(d2):
            del d1[len(d2):]
        elif len(d2) > len(d1):
            del d2[len(d1):]
        print "New length d1: %d d2: %d" % (len(d1), len(d2))


    def make_plot(self):
        # Plot everything
        if self.wins:
            self.add_plot("wins")
        if self.score:
            self.add_plot("score")
        if self.time:
            self.add_plot("time")


    def add_plot(self, name):
        """ Adds average and variance of 'name' to plot """
        self.all_data['average_' + name] = np.average(self.all_data[name], axis=0)
        self.all_data['variance_' + name] = np.std(self.all_data[name], axis=0)
        xs = range(0, len(self.all_data['average_' + name]))
        bounds = ((y[0] - y[1], y[0] + y[1]) for y in
                zip(self.all_data['average_' + name],
                    self.all_data['variance_' + name]))
        ymax, ymin = zip(*bounds)
        self.ax.plot(xs, self.all_data['average_' + name], label=name, linewidth=1.0)
        col = self.ax.get_lines()[-1].get_color()
        self.ax.fill_between(xs, ymax, ymin, alpha=.3, edgecolor="w",
                             color=col)




    def save(self):
        self.set_variables()
        setFigLinesBW(self.fig)
        print "saving plot to %s" % (self.output_file)
        self.fig.savefig(self.output_file)

    def plot(self):
        self.set_variables()
        plt.show()

    def set_axis_range(self):
        x1, x2, y1, y2 = plt.axis()
        # Voor 200 agents:
        if self.y_min != None:
            print "setting y_min to " , self.y_min
            y1 = self.y_min
        if self.y_max != None:
            y2 = self.y_max
            print "setting y_max to " , self.y_max
        plt.axis((x1, x2, y1, y2))


    def set_variables(self):
        plt.ylabel("")
        plt.xlabel('# Games Played')
        # Title is not needed in LaTeX articles
        # plt.title('Hypervolume')
        plt.legend(loc="lower right")


def setAxLinesBW(ax):
    """
    Take each Line2D in the axes, ax, and convert the line style to be
    suitable for black and white viewing.
    """
    MARKERSIZE = 3
    COLORMAP = {
        'b': {'marker': None, 'dash': (None, None)},
        'g': {'marker': None, 'dash': [5, 5]},
        'r': {'marker': None, 'dash': [5, 3, 1, 3]},
        'k': {'marker': None, 'dash': [1, 3]},
        'm': {'marker': None, 'dash': [5, 2]},
        'y': {'marker': None, 'dash': [1, 1]},
        'c': {'marker': None, 'dash': [10, 10]}
        }

    for line in ax.get_lines() + ax.get_legend().get_lines():
        origColor = line.get_color()
        # line.set_color('black')
        line.set_dashes(COLORMAP[origColor]['dash'])
        line.set_marker(COLORMAP[origColor]['marker'])
        line.set_markersize(MARKERSIZE)


def setFigLinesBW(fig):
    """
    Take each axes in the figure, and for each line in the axes, make the
    line viewable in black and white.
    """
    for ax in fig.get_axes():
        setAxLinesBW(ax)


if __name__ == "__main__":
    mpl.rcParams['axes.color_cycle'] = ['k', 'b', 'g', 'r']
    mpl.rcParams['figure.figsize'] = (6, 4)
    parser = argparse.ArgumentParser(description='Plot wins, scores and times')
    parser.add_argument('file', metavar='file', nargs='+',
                        help='files containing wins, scores, time (in that order)'
                             'files')
    parser.add_argument('--output', '-o', default="plot.pdf",
                        help="name of output file")
    parser.add_argument('--wins', '-w', action='store_true',
                        help="If this argument is given, wins are plotted")
    parser.add_argument('--score', '-s', action='store_true',
                        help="If this argument is given, scores are plotted")
    parser.add_argument('--time', '-t', action='store_true',
                        help="If this argument is given, time is plotted")
    parser.set_defaults(verbose=True)
    args = parser.parse_args()
    hvp = HyperVolumePlotter(args.output, wins=args.wins, score=args.score,
            time=args.time)
    # Loop through all directories in argv
    for file in args.file:
        print "adding file %s" % (file)
        hvp.add_file(file)
    hvp.make_plot()
    hvp.set_axis_range()
    hvp.ax.yaxis.major.formatter.set_powerlimits((-3, 4))
    hvp.save()
    print "done"
