import csv
import sys
  
ts_list = []
tj_list = []

for path in sys.argv[1:]:
    with open(path, newline='') as f:
        reader = csv.DictReader(f, fieldnames=["ts", "tj"])
        for row in reader:
            ts_list.append(float(row["ts"])/1000000)
            tj_list.append(float(row["tj"])/1000000)

if(len(ts_list) > 0 and len(tj_list) > 0):
    print("Average time (TS):", round(sum(ts_list)/len(ts_list), 2), "ms")
    print("Average time (TJ):", round(sum(tj_list)/len(tj_list), 2), "ms")
else:
    print("error parsing files, empty ts or tj!")