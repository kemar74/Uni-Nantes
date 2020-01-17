grapheSize = 5
adj = [(0, 1, 3), (0, 3, 5), (1, 2, 5), (1, 3, 1), (2, 4, 1), (3, 1, 1), (3, 2, 5), (3, 4, 5), (4, 2, 3), (4, 0, 3)]

time = 0
def parcoursProfondeur(start, adjacence, marked, action=None):
	global time
	if(marked == []):
		marked = [False]*(grapheSize+1)
	marked[start] = True
	time += 1
	timeStart = time
	for e in adjacence:
		if(e[0] == start and not marked[e[1]]):
			parcoursProfondeur(e[1], adjacence, marked, action)
	time += 1
	if action:
		action(start, timeStart, time)
	return marked

def parcoursLargeur(start, adjacence, marked, action=None):
	global time
	if(marked == []):
		marked = [False]*(grapheSize+1)
	marked[start] = True
	liste = [start]
	timeStart = time
	while liste != []:
		n = liste[0]
		del liste[0]

		for e in adjacence:
			if(e[0] == n and not marked[e[1]]):
				time += 1
				action(e[1], time, time + 1)
				marked[e[1]] = True
				liste.append(e[1])
	if action:
		action(start, timeStart, time)
	return marked

def connexite(start, adjacence):
	groupe = parcoursProfondeur(start, adjacence, [])
	return [i for i, n in enumerate(groupe) if n == True]

def fortementConnexe(start, adjacence):
	groupe1 = parcoursProfondeur(start, adjacence, [])
	groupe2 = parcoursProfondeur(start, reverseCouples(adjacence), [])

	return [i for i, n in enumerate(groupe2) if n == True and groupe1[i] == True]

def reverseCouples(liste, index1=0, index2=1):
	newList = []
	for x in liste:
		if(index1 > index2):
			tmp = index1
			index1 = index2
			index2 = tmp
		rest = x[0:index1] + x[(index1+1):index2] + x[(index2 + 1):]
		newList.append((x[index2], x[index1]) + rest)

	return newList

def hasCircuit(adjacence):
	for n in range(grapheSize):
		if(len(fortementConnexe(n, adjacence)) > 1):
			return True
	return False

def addDate(date, i, start, end):
	date[i] = (start, end)
	return date

def getEdge(s, t, graph):
	for e in graph:
		if(e[0] == s and e[1] == t):
			return e[2]

def relax(graph, s, t, dists, pred):
	if(dists[s] + getEdge(s, t, graph) < dists[t]):
		pred[t] = s
		dists[t] = dists[s] + getEdge(s, t, graph)
	return (pred, dists)

def init():
	preds = [None]*grapheSize
	dists = [100000]*grapheSize
	return (preds, dists)

def getSuccessor(graph, s):
	x = []
	for e in graph:
		if(e[0] == s):
			x.append(e[1])
	return x

def displayChemin(preds, t):
	chemin = [t]
	while preds[t] != None:
		chemin.append(preds[t])
		t = preds[t]
	chemin.reverse()
	return chemin

def djikstra(graph, s, t):
	(preds, dists) = init()
	dists[s] = 0
	Q = {0, 1, 2, 3, 4}
	while Q != set():
		q = -1
		for x in Q:
			if q == -1 or dists[x] <= dists[q]:
				q = x
		Q.remove(q)
		for succ in getSuccessor(graph, q):
			(preds, dists) = relax(graph, q, succ, dists, preds)
	return displayChemin(preds, t)

def bellman(graph, s, t):
	(preds, dists) = init()
	dists[s] = 0
	Q = {0, 1, 2, 3, 4}
	for i in range(grapheSize):
		for e in graph:
			relax(graph, e[0], e[1], dists, preds)
	for e in graph:
		if(dists[e[0]] + getEdge(e[0], e[1], graph) < dists[e[1]]):
			print("Cycle negatif")
			return False
	return displayChemin(preds, t)


print(bellman(adj, 0, 4))

# dates1 = [None]*(grapheSize+1)
# parcoursProfondeur(1, adj, [], lambda n, start, end : addDate(dates1, n, start, end))
# print(dates1)

# time = 0
# parcoursLargeur(1, adj, [], lambda n, start, end : addDate(dates1, n, start, end))
# print(dates1)
# print(fortementConnexe(1, adj))
# print(hasCircuit(adj))