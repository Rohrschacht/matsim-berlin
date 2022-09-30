import pandas as pd
from pyproj import Transformer
import matplotlib.pyplot as plt


filename = "carfree2/berlin-v5.5-1pct.output_trips.csv.gz"
proj_transformer = Transformer.from_crs("epsg:31468", "epsg:25833")
berlin_png = plt.imread("berlin.png")

parked_vehicles = {
	"bicycle": {"x": [], "y": [], "coords": {}},
	"car": {"x": [], "y": [], "coords": {}}
}
data = pd.read_csv(filename, compression="gzip", delimiter=";", low_memory=False)
changed_mode = data[data.isin(["mode-change"]).any(axis=1)]
coords = {}


iterator = changed_mode.iterrows()
for (i, start), (j, end) in zip(iterator, iterator):
	x, y = proj_transformer.transform(start["end_y"], start["end_x"])
	coord = f"{x},{y}"
	if start["main_mode"] == "car" and end["main_mode"] != "car" \
		or (start["main_mode"] != "car" and end["main_mode"] == "car"):

		if coord in parked_vehicles["car"]["coords"]:
			parked_vehicles["car"]["coords"][coord] += 1
		else:
			parked_vehicles["car"]["coords"][coord] = 1
			parked_vehicles["car"]["x"].append(x)
			parked_vehicles["car"]["y"].append(y)

	if start["main_mode"] == "bicycle" and end["main_mode"] != "bicycle" \
		or (start["main_mode"] != "bicycle" and end["main_mode"] == "bicycle"):

		if coord in parked_vehicles["bicycle"]["coords"]:
			parked_vehicles["bicycle"]["coords"][coord] += 1
		else:
			parked_vehicles["bicycle"]["coords"][coord] = 1
			parked_vehicles["bicycle"]["x"].append(x)
			parked_vehicles["bicycle"]["y"].append(y)

print("mode switch car:", len(parked_vehicles["car"]["x"]))
print("mode switch bike:", len(parked_vehicles["bicycle"]["x"]))

print("car:")
for i in range(len(parked_vehicles["car"]["x"])):
	coord = str.format("{},{}", parked_vehicles["car"]["x"][i], parked_vehicles["car"]["y"][i])
	capacity = parked_vehicles["car"]["coords"][coord]
	print(coord, capacity)

print("bicycle:")
for i in range(len(parked_vehicles["bicycle"]["x"])):
	coord = str.format("{},{}", parked_vehicles["bicycle"]["x"][i], parked_vehicles["bicycle"]["y"][i])
	capacity = parked_vehicles["bicycle"]["coords"][coord]
	print(coord, capacity)
