include("CPUTime.jl")



mutable struct tabgraph
	nv::Int
	ne::Int
	adj::Array{Bool,2}
	pds::Array{Real,2}
	pred::Array{Int,2}
	predUpdated::Bool
	tabgraph(nbs::Int) = 
		new(nbs,0,
		zeros(Bool,(nbs,nbs)),
		Array{Real}(undef,nbs,nbs),
		Array{Int}(undef,nbs,nbs), 
		false)
end

monGraphe = tabgraph(10)

function relie!(G::tabgraph, s::Int, d::Int, p::Real)
	if(G.adj[s, d])
		G.pds[s, d] = G.pds[s, d] + p
	else 
		G.pds[s, d] = p
		G.ne += 1
		G.adj[s, d] = true
		G.pred[s, d] = s
	end
	G.predUpdated = false
	return G
end

function delier!(G::tabgraph, s::Int, d::Int)
	G.pds[s, d] = undef
	G.adj[s, d] = false
	G.ne -= 1
	G.pred[d, s] = undef
	G.predUpdated = false

	return G
end

function prettyDisplay2DArray(array)
	affichage = ""
	for i = 1:size(array, 1)
		affichage *= "\t" * string(i)
	end
	affichage *= "\n"
	for i = 1:size(array, 1)
		affichage *= string(i) * "\t"
		for j = 1:size(array, 2)
			if isassigned(array, i, j)
				if (isa(array[i, j], Bool) && array[i, j]) || (isa(array[i, j], Number) && array[i, j] != 0)
					if(isa(array[i, j], Number) && round(array[i, j], digits=3) == array[i, j])
						affichage *= string(array[i, j]) * "\t"
					else
						affichage *= string(round(array[i, j], digits=3)) * "\t"
					end
				else
					affichage *= " - \t"
				end
			else
				affichage *= " - \t"
			end
		end
		affichage *= "\n"
	end
	return affichage
end

function aff(G::tabgraph)
	affichage = prettyDisplay2DArray(G.pds) 
	affichage *= "\n"
	return affichage
end

function alea!(G::tabgraph, nbEdges::Int=-1, minValue::Real=0, maxValue::Real=10, useIntegers::Bool=true)
	if(nbEdges > -1)
		i = nbEdges
	else 
		i = G.nv^2
	end
	
	for x = 1:i
		A = rand(1:G.nv)
		B = rand(1:G.nv)

		if(useIntegers)
			pds = rand(minValue:maxValue)
		else
			pds = rand() * (maxValue - minValue) + minValue
		end

		relie!(G, A, B, pds)
	end
	return G
end

function voisins(G::tabgraph, s::Int)
	voisins = []
	for i = 1:G.nv
		if G.adj[s, i] || s == i
			voisins = [voisins; i]
		end
	end
	return voisins
end

function lire!(G::tabgraph, nomf::String)
	file = ""
	try
		file = open(nomf) do fichier 
			readlines(fichier)
		end
	catch e
		return false
	end

	for i = 1:size(file, 1)
		values = split(file[i])
		if size(values, 1) == 2 || size(values, 1) == 3
			A = parse(Int, values[1])
			B = parse(Int, values[2])
			poids = 1
			if size(values, 1) == 3
				poids = parse(Float64, values[3])
			end

			if isinteger(A) && A > 0 && A <= G.nv && isinteger(B) && B > 0 && B <= G.nv && isreal(poids)
				relie!(G, A, B, poids)
			end
		end
	end
	return G
end

function RoyFloydWarshall(G::tabgraph)

	for i = 1:G.nv
		for j = 1:G.nv
			if(!isassigned(G.pds, i, j))
				G.pds[i, j] = Inf
			end
		end
	end

	for k = 1:G.nv
		for i = 1:G.nv
			for j = 1:G.nv
				#if(i == j)
				#	relie!(G, i, j, 0)
				#else
				if(G.adj[i, k] && G.adj[k, j] && G.pds[i, j] > G.pds[i, k] + G.pds[k, j])
					if(G.adj[i, j])
						delier!(G, i, j)
					end
					relie!(G, i, j, G.pds[i, k] + G.pds[k, j])
					G.pred[i, j] = k
				end
			end
		end 
	end
	G.predUpdated = true
	return G
end

function repairGraph(G::tabgraph)
	for i = 1:G.nv 
		for j = 1:G.nv 
			if(!G.adj[i, j])
				G.pred[i, j] = -1
			end
		end
	end
	return G
end

function plusCourtChemin(G::tabgraph, from::Int, to::Int)
	chemin = [to]
	while(to != from)
		to = G.pred[from, to]
		chemin = [to chemin]
	end
	return chemin
end

function connexitéforte(G::tabgraph)::tabgraph
	for k = 1:G.nv
		for i = 1:G.nv
			for j = 1:G.nv
				G.adj[i, j] = G.adj[i, j] || (G.adj[i, k] && G.adj[k, j])
			end
		end 
	end
	return G
end

function connexitéforte_produit(G::tabgraph)::tabgraph
	X = G.adj
	for k = 1:size(G.adj, 1)
		X += X * X 
	end
	for i = 1:size(G.adj, 1)
		for j = 1:size(G.adj, 2)
			if X[i, j] != 0
				G.adj[i, j] = 1
			else
				G.adj[i, j] = 0
			end
		end
	end
	return G
end

function testFunction(func, graph::tabgraph, nbIter::Int)
	CPUtic()
	time = @elapsed begin
		for i = 1:nbIter
			func(graph)
		end
	end
	return [CPUtoq() time]
end

function comparerFermeturesTransitives(nbIterations::Int)
	println("Fermeture transitive sur un graphe avec $monGraphe.nv noeuds et $monGraphe.ne arcs : ")
	t1 = testFunction(connexitéforte, G, nbIterations)
	t2 = testFunction(connexitéforte_produit, G, nbIterations)
	println("Algo de Warshall       : CPU = " * string(t1[1]) * "s --- réel = " * string(t1[2]) * "s")
	println("Avec produit matriciel : CPU = " * string(t2[1]) * "s --- réel = " * string(t2[2]) * "s")
	println("(Temps pour $nbIterations itérations)")
end

function displayChemin(G::tabgraph, from::Int, to::Int)
	liste = plusCourtChemin(G, from, to)
	poids = 0
	affichage = ""
	for i = 1:size(liste, 2)
		affichage *= string(liste[i])
		if(i < size(liste, 2))
			poids += G.pds[liste[i], liste[i + 1]]
			affichage *= " -> "
		end
	end
	affichage *= " (Coût : $poids)"
	return affichage
end

function Input(prompt::String="")
	if(prompt != "")
		print(prompt * "\n")
	end
	print("> ")
	choix = readline()
end

function getNumericResponse(prompt::String="", maxValue::Int=-1)::Int
	choix = 0
	try
		choix = Input(prompt)
		choix = parse(Int, choix)
		if(maxValue > 0 && choix > maxValue)
			print("Merci de donner une valeur inférieure à " * string(maxValue) * "\n")
			return getNumericResponse(prompt)
		end
	catch
		print("Merci de donner une valeur numérique\n")
		return getNumericResponse(prompt)
	end
	return choix
end 

function menu()
	global monGraphe = repairGraph(monGraphe)
	println("----- GRAPHES AVEC JULIA -----")
	println("            Menu              ")
	println("1) Créer un graphe depuis un fichier")
	println("2) Créer un graphe aléatoire")
	println("3) Afficher le graphe")
	println("4) Appliquer une fermeture transitive")
	println("5) Chemin le plus court")
	println("6) Quitter")

	choix = getNumericResponse("Votre choix : ", 6)
	

	if(choix == 1)
		menuGrapheFromFile()
	elseif(choix == 2)
		menuGraphAlea()
	elseif(choix == 3)
		afficherGraph()
	elseif(choix == 4)
		menuFermetureTransitive()
	elseif(choix == 5)
		menuCheminPlusCourt()
	elseif(choix == 6)
		quitter()
	end
end

function menuGrapheFromFile()
	println("--- Création du graphe depuis un fichier ---")
	newSize = getNumericResponse("Nombre de sommets sur le graphe?")
	global monGraphe = tabgraph(newSize)
	repeat = true
	while(repeat)
		repeat = false
		filepath = Input("Chemin vers le fichier (chemin relatif : " * pwd() * " )")
		if(lire!(monGraphe, filepath) === false)
			println("Aïe! Impossible de lire le fichier '" * joinpath(pwd(), filepath) * "'...")
			repeat = true
		end
	end
	menu()
end

function menuGraphAlea()
	println("--- Création du graphe aléatoire ---")
	newSize = getNumericResponse("Nombre de sommets sur le graphe?")
	global monGraphe = tabgraph(newSize)
	nbEdges = getNumericResponse("Nombre d'arcs à génerer (-1 pour utiliser " * string(newSize) * "^2 )")
	minValue = getNumericResponse("Valeur minimale d'un arc?")
	maxValue = getNumericResponse("Valeur maximale d'un arc?")
	useIntegers = -1
	while(useIntegers == -1)
		useIntegersChr = Input("Ne garder que des valeurs entières ? (O/N)")
		firstChar = uppercase(useIntegersChr[1])
		if(occursin(firstChar, "YO1"))
			useIntegers = true
		elseif(occursin(firstChar, "N0"))
			useIntegers = false
		end
	end
	alea!(monGraphe, nbEdges, minValue, maxValue, useIntegers)
	menu()
end

function afficherGraph()
	println("--- Affichage du graphe ---")
	println("Graphe de $(monGraphe.nv) noeuds et $(monGraphe.ne) arcs")
	if(!monGraphe.predUpdated)
		println("Le tableau des prédécesseurs n'est peut-être pas optimisé, lancez un algorithme de plus court chemin pour le mettre à jour")
	end
	println("1) Voir la matrice d'adjacence")
	println("2) Voir la matrice des poids")
	println("3) Voir le tableau de prédécesseurs")
	println("4) Retour")

	choix = getNumericResponse("Votre choix : ", 4)

	if(choix == 1)
		println(prettyDisplay2DArray(monGraphe.adj))
	elseif(choix == 2)
		println(prettyDisplay2DArray(monGraphe.pds))
	elseif(choix == 3)
		println(prettyDisplay2DArray(monGraphe.pred))
	end
	menu()
end

function menuFermetureTransitive()
	println("--- Appliquer une fermeture transitive ---")
	println("1) Avec l'algorithme de Warshall classique")
	println("2) Avec des produits vectoriels")
	println("3) Comparer les deux méthodes")
	println("4) Retour")
	choix = getNumericResponse("Votre choix : ", 4)

	if(choix == 1)
		global monGraphe = connexitéforte(monGraphe)
	elseif(choix == 2)
		global monGraphe = connexitéforte_produit(monGraphe)
	elseif(choix == 3)
		nbIter = getNumericResponse("Nombre d'itérations voulues (nous conseillons " * monGraphe.nv^2/100 * ") : ")
		comparerFermeturesTransitives(nbIter)
	end
	menu()
end
function menuCheminPlusCourt()
	println("--- Recherche de chemin le plus court ---")
	if(monGraphe.predUpdated)
		println("1) Lancer l'algorithme de Roy-Floyd-Warshall [Déjà à jour]")
	else
		println("1) Lancer l'algorithme de Roy-Floyd-Warshall [Conseillé]")
	end
	println("2) Voir la matrice de prédecésseurs")
	println("3) Voir le chemin le plus court entre 2 sommets")
	println("4) Retour")
	choix = getNumericResponse("Votre choix : ", 4)

	if(choix == 1)
		println("Lancement de l'algorithme Roy-Floyd-Warshall...")
		global monGraphe = RoyFloydWarshall(monGraphe)
	elseif(choix == 2)
		println(prettyDisplay2DArray(monGraphe.pred))
	elseif(choix == 3)
		println("Lancement de l'algorithme Roy-Floyd-Warshall...")
		global monGraphe = RoyFloydWarshall(monGraphe)
		from = getNumericResponse("Sommet de départ : ", monGraphe.nv)
		to = getNumericResponse("Sommet d'arrivée : ", monGraphe.nv)
		println("Chemin le plus court : " * displayChemin(monGraphe, from, to))
	end
	menu()
end
function quitter()
	return 0
end

menu()