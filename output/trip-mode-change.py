#!/usr/bin/env python3

import gzip
import csv
from collections import defaultdict

#counter = {"total": 0, "car": 0, "bicycle": 0, "pt": 0, "walk": 0}
counter = defaultdict(lambda: 0)
tripc = defaultdict(lambda: 0)

filename = "carfree/berlin-v5.5-1pct.output_trips.csv.gz"

person_id = 0
main_mode = 8
start_activity_type = 11
end_activity_type = 12

with gzip.open(filename, mode='rt') as f:
	csvfile = csv.reader(f, delimiter=';')
	modes = ["",""]
	id = ""
	for line in csvfile:
		if line[start_activity_type] == "mode-change" or line[end_activity_type] == "mode-change":
			mode = line[main_mode]
			counter["total"] += 1
			counter[mode] += 1
			if line[end_activity_type] == "mode-change":
				modes[0] = mode
			else:
				if line[0] == id:
					modes[1] = mode
					tripc[(modes[0],modes[1])] += 1
				else:
					print("next trips person ids not matching", file=sys.stderr)
			id = line[0]


print(counter)
print(tripc)
