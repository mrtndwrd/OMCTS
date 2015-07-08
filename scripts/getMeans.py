import sys, os, argparse
import numpy as np

def get_mean(f):
    """ Calculates the mean of all the space separated rows in file f """
    # totals = []
    # with open(f) as of:
    #     for line in of.readlines():
    #         totals.append(line.split())
    # totals = np.array(totals)

    totals = np.genfromtxt(f)
    print totals
    print "mean: "
    print np.mean(totals, 0)
    print "std: "
    print np.std(totals, 0)
    print "total: "
    print np.sum(totals, 0)
            

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print "give filename as first argument and nothing else"
        exit()
    get_mean(sys.argv[1])
