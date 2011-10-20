import sys


lens = {}
with open(sys.argv[1], 'rt') as f:
	for line in f.readlines():
		split = line.split()
		linelen = len(split)
		if not linelen in lens:
			lens[linelen] = 1
		else:
			lens[linelen] = lens[linelen] + 1 

for ll in reversed(sorted(lens.keys())):
	print ll, lens[ll]
