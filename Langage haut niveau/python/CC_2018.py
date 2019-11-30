Gex = ( {3,7,9,5,42}, [ {5,42}, {7,9}, {7}, {7,3}, {42,5}, {5}, {3,7} ] )

def voisins(s,G) :
	if not(s in G[0]) :
		return set() # l'ensemble vide (!!! pas {} !!!)
	else :
		return { t for t in G[0] if {t,s} in G[1] }

print(voisins(7,Gex))

print( [b for a in Gex[0] for b in range(a) if a+b in Gex[0] ] )

print(voisins(6,Gex))

print( { a for a in Gex[0] if {a} in Gex[1] } )

for a in Gex[0] :
	for b in voisins(a,Gex) :
		if b in voisins(3,Gex) :
			print(str(a) + "( b = " + str(b) + ")")

print("\n\n")

Gor = ( {3,7,9,5,42}, { (5,42), (7,9), (7,7), (7,3), (42,5), (5,5), (3,7) } )
def diffuseVoisins(s,GG,f) :
	res = set() # l’ensemble vide
	for t in GG[0] :
		if (s,t) in GG[1] :
			res = res | { f(s,t) } # union ensembliste
	return res

def dist(a,b) :
	return abs(a-b)

print( diffuseVoisins(5,Gor,lambda x,y : (y,x) ) )
print(diffuseVoisins(9, Gor, lambda x,y : y))

print("\n\n")

def access(s,GG, vus = set()):
	voisins = diffuseVoisins(s, GG, lambda x,y : y)
	for voisin in voisins:
		if voisin not in vus:
			vus = vus | {voisin}
			vus = vus | access(voisin, GG, vus)
	return vus

print(access(3, Gor))

def fortCon(GG):
	for a in GG[0]:
		for b in GG[0]:
			if not b in access(a, GG):
				return False
	return True

Gfort = ( {1,2,3}, { (1,2),(2,3),(3,1) } )
GnFort = ( {4,7,9}, { (4,7),(4,9),(7,9) } )
print(fortCon(Gfort))
print(fortCon(GnFort))
