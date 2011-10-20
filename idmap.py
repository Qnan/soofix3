import sys


lens = {}
idmap = {}
with open(sys.argv[2], 'rt') as f:
	i = 0
	for line in f.readlines():
		idmap[i] = int(line)
		i = i + 1	

with open(sys.argv[1], 'rt') as f:
	for line in f.readlines():
		if line[0] == '#':
			print line.strip()
		else:
			print ' '.join([str(idmap[int(id)]) for id in line.split()])
