import pandas as pd
from pyproj import Transformer
import matplotlib.pyplot as plt

filename = "carfree/berlin-v5.5-1pct.output_trips.csv.gz"
proj_transformer = Transformer.from_crs("epsg:31468", "epsg:4326")
berlin_png = plt.imread("berlin.png")

parked_vehicles = {
	"bicycle": {"x": [], "y": []},
	"car": {"x": [], "y": []}
}
data = pd.read_csv(filename, compression="gzip", delimiter=";", low_memory=False)
changed_mode = data[data.isin(["mode-change"]).any(axis=1)]

iterator = changed_mode.iterrows()
for (i, start), (j, end) in zip(iterator, iterator):

	x, y = proj_transformer.transform(start["end_y"], start["end_x"])
	print(start["end_x"], start["end_y"], x, y)
	if start["main_mode"] == "car" and end["main_mode"] != "car" \
		or (start["main_mode"] != "car" and end["main_mode"] == "car"):
		parked_vehicles["car"]["x"].append(x)
		parked_vehicles["car"]["y"].append(y)
	if start["main_mode"] == "bicycle" and end["main_mode"] != "bicycle" \
		or (start["main_mode"] != "bicycle" and end["main_mode"] == "bicycle"):
		parked_vehicles["bicycle"]["x"].append(x)
		parked_vehicles["bicycle"]["y"].append(y)

print("mode switch car:", len(parked_vehicles["car"]["x"]))
print("mode switch bike:", len(parked_vehicles["bicycle"]["x"]))

fig, ax = plt.subplots(figsize=(20, 20))

BBox = (min(parked_vehicles["car"]["x"]), max(parked_vehicles["car"]["x"]),
		min(parked_vehicles["car"]["y"]), max(parked_vehicles["car"]["y"]))

ax.scatter(parked_vehicles["car"]["x"], parked_vehicles["car"]["y"], zorder=1, alpha=0.2,
		   c='r', s=30)
ax.set_title('Plotting where people left their car')
ax.set_xlim(BBox[0], BBox[1])
ax.set_ylim(BBox[2], BBox[3])
ax.imshow(berlin_png, zorder=0, extent=BBox, aspect='auto')
plt.savefig("parking_cars.png")
