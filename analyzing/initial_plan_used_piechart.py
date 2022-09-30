import pandas as pd
from pyproj import Transformer
import matplotlib.pyplot as plt

filename = "carfree/berlin-v5.5-1pct.output_trips.csv.gz"
affected_persons = "list_affected_ppl_plans.csv"

data = pd.read_csv(filename, compression="gzip", delimiter=";", low_memory=False)
changed_mode = data[data.isin(["mode-change"]).any(axis=1)]

used_change_mode = 0

with open(affected_persons, "r") as file:
	persons = file.readlines()
for person in persons:
	if person.replace("\n", "") in changed_mode["person"].values:
		used_change_mode += 1

overall_persons = len(persons)
print(overall_persons, overall_persons - used_change_mode, used_change_mode)

fig1, ax1 = plt.subplots()
ax1.pie([overall_persons - used_change_mode, used_change_mode],
		labels=["Used initial plan", "Used changed mode plan"],
		autopct='%1.1f%%',
		shadow=True, startangle=90)

ax1.axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.
plt.xlabel(f"Total Persons: {len(persons)}")
plt.title("Usage of changed mode plans")
plt.savefig("usage_new_plans.pdf", dpi=200)
