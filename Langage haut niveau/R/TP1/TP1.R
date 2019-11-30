library(rmarkdown)

pgcd <- function(a,b=NULL){
  if(isTRUE(!is.na(b) & !is.null(b) & length(b))) {
    a <- append(a, b)
  }
  if(length(a) > 2) {
    a <- c(pgcd(a[1], a[2]), a[3:length(a)])
    return(pgcd(a))
  }
  
  b <- a[2]
  a <- a[1]
  if (is.na(b) | abs(b)<1e-7) {
    pgs <- a
  }
  else {
    r <- a%%b
    pgs <- pgcd(b,r)
  }
  return(pgs)
}
ppcm <- function(a,b= NULL) {
  if(isTRUE(!is.na(b) & !is.null(b) & length(b))) {
    a <- append(a, b)
  }
  if(length(a) > 2) {
    a <- c(ppcm(a[1], a[2]), a[3:length(a)])
    return(ppcm(a))
  }
  
  b <- a[2]
  a <- a[1]
  a*b/(pgcd(a,b))
}
myIdentical <- function(a, b) {
  return(length(which(a == b)) == length(a))
}

rotationCran <- function(tableau, cran=1) {
  cran <- cran %% length(tableau)
  if(cran > 0) {
    return(c(tableau[(cran+1):length(tableau)], head(tableau, n=cran)))
  } else {
    cran = abs(cran)
    return(c(tail(tableau, n=cran), tableau[1:(length(tableau)-cran)]))
  }
}

trouverMatriceDePermutation <- function(vecteur) {
  vecteur <- rank(vecteur) # Cree les indices de chaque élément, au cas où...
  matrice <- matrix(rep(0, times=length(vecteur)**2), nrow=length(vecteur))
  for(i in 1:length(vecteur)) {
    matrice[i, vecteur[i]] <- 1
  }
  return(matrice)
}
trouverVecteurDePermutation <- function(matrice) {
  vecteur <- rep(0, n=ncol(matrice))
  for(i in 1:ncol(matrice)) {
    vecteur[i] <- min(which(matrice[i,] == 1))
  }
  return(vecteur)
}
permuter <- function(vecteur, matriceDePermutation) {
  vecteur <- as.matrix(vecteur)
  if(nrow(vecteur) > 1)
    vecteur <- t(vecteur)
  return(as.vector(vecteur %*% matriceDePermutation))
}
permuterLong <- function(vecteur, matriceDePermutation) {
  newVector <- vecteur
  for(i in 1:length(newVector)) {
    myPermLine <- matriceDePermutation[i,]
    newVector[i] <- vecteur[min(which(myPermLine == 1)) ]
  }
  return(newVector)
}
permuter_vecteur <- function(vecteur, vecteurDePermutation) {
  newVector <- vecteur
  for(i in 1:length(newVector)) {
    newVector[i] <- vecteur[vecteurDePermutation[i] ]
  }
  return(newVector)
}
composerPermutation <- function(permut1, permut2) {
  if(is.vector(permut1)) {
    if(is.vector(permut2)) {
      return(permuter_vecteur(permut1, permut2))
    }
    else if(is.matrix(permut2)) {
      return(permuter(permut1, permut2))
    }
  }
  else if(is.matrix(permut1)) {
    if(is.vector(permut2)){
      newMatrix <- matrix(rep(0, n=ncol(permut1)*nrow(permut1)), nrow=nrow(permut1))
      for(i in 1:nrow(permut1)) {
        newMatrix[i,] <- permuter_vecteur(permut1[i,], permut2)
      }
      return(newMatrix)
    }
    else if(is.matrix(permut2)) {
      return(permut1 %*% permut2)
    }
    return(composerPermutation(trouverVecteurDePermutation(permut1), permut2))
  }
}
inverserPermutation <- function(vecteur, matriceDePermutation) {
  return(permuter(vecteur, t(matriceDePermutation)))
}
inverserPermutation_vecteur <- function(vecteur, vecteurDePermutation) {
  vecteurInverse <- rep(0, n=length(vecteurDePermutation))
  for(i in 1:length(vecteurDePermutation)) {
    vecteurInverse[vecteurDePermutation[i]] <- i
  }
  return(permuter_vecteur(vecteur, vecteurInverse))
}
calculOrdrePermutationLong_matrice <- function(matrice) {
  vecteur <- 1:nrow(matrice)
  originalVecteur <- vecteur
  isSame <- F
  order <- 0
  while(!isSame) {
    vecteur <- permuter(vecteur, matrice)
    order <- order +1
    isSame <- myIdentical(vecteur, originalVecteur)
  }
  return(order)
}
calculOrdrePermutationLong_vecteur <- function(vecteurDePermutation) {
  vecteur <- 1:length(vecteurDePermutation)
  originalVecteur <- vecteur
  isSame <- F
  order <- 0
  while(!isSame) {
    vecteur <- permuter_vecteur(vecteur, vecteurDePermutation)
    order <- order +1
    isSame <- myIdentical(vecteur, originalVecteur)
  }
  return(order)
}
calculOrdrePermutationLong <- function(matrice) {
  if(is.vector(matrice)) {
    return(calculOrdrePermutationLong_vecteur(matrice))
  }
  else {
    return(calculOrdrePermutationLong_matrice(matrice))
  }
}

calculOrdrePermutation <- function(matrice) {
  vect1 <- 1:nrow(matrice)
  vect2 <- permuter(vect1, matrice)
  orders <- rep(1, length(vect1))
  for(x in vect1) {
    originalX <- x
    x <- vect2[x]
    while(x != originalX) {
      x <- vect2[x]
      orders[originalX] <- orders[originalX] +1
    }
  }
  return(ppcm(orders))
}
calculOrdrePermutation_vecteur <- function(vecteurDePermutation) {
  vect1 <- 1:length(vecteurDePermutation)
  vect2 <- permuter_vecteur(vect1, vecteurDePermutation)
  orders <- rep(1, length(vect1))
  for(x in vect1) {
    originalX <- x
    x <- vect2[x]
    while(x != originalX) {
      x <- vect2[x]
      orders[originalX] <- orders[originalX] +1
    }
  }
  return(ppcm(orders))
}

# sampleSize <- 100
# extrait <- sample(1:sampleSize, sampleSize)
# testMatrix = extrait
# matriceDePermutation <- trouverMatriceDePermutation(testMatrix)
# testMatrix <- sort(testMatrix)
# ptm <- proc.time()
# for(i in 1:1000) {
#   calculOrdrePermutation(matriceDePermutation)
# }
# proc.time() - ptm
# for(i in 1:calculOrdrePermutation(matriceDePermutation)) {
#   testMatrix <- permuter(testMatrix, matriceDePermutation)
# }
# matriceDePermutation
# testMatrix <- permuter(t(testMatrix), matriceDePermutation)
# 
# numberIterations <- 100
# 
# # Tests sur vectToMat
# ptm <- proc.time()
# for(i in 1:numberIterations) {
#   matriceDePermutation <- trouverMatriceDePermutation(extrait)
# }
# endingTimeTrouverMatrice <- proc.time() - ptm
# 
# # Tests sur Permutation
# ptm <- proc.time()
# for(i in 1:numberIterations) {
#   permuter(1:sampleSize, matriceDePermutation)
# }
# endingTimePermutations <- proc.time() - ptm
# 
# 
# barplot(c(endingTimeTrouverMatrice, endingTimePermutations))

generation <- function(n, p) {
  permut <- 1:n
  
  if(p == 0) return(permut)
  for(i in 1:p) {
    permut <- composerPermutation(permut, sample(1:n))
  }
  return(permut)
}
createProcessTest <- function(func, variables, numberOfTests, outputProgress=T, stopIfTooBig=10) {
  ptm <- proc.time()
  times <- c()
  for(i in 1:numberOfTests) {
    
    times <- append(times, system.time(x <- tryCatch(func(variables), error=function(e){return(Inf)}))[3])
    if(isTRUE(stopIfTooBig > 0 & times[length(times)] > stopIfTooBig)) {
      times <- replace(times, nbOperations, Inf)
      return(times)
    }
    if(outputProgress & i %% 5 == 0) {
      print(paste((i/numberOfTests) * 100, "%"))
    }
  }
  return(times)
}

doFullTest <- function(func, useMatrix, interval, maxN, outputProgress=T, stopIfTooBig=5, functionForComparingTimes=mean) {
  temps <- c()
  outputProgress <- T
  for(sampleSize in seq(from=1, to=maxN, by=interval)) {
    if(outputProgress) {
      print(paste("N = ", (sampleSize-1), " : ", round((sampleSize-1)/maxN * 100,1), "%"))
    }
    permut <- generation(sampleSize, 5)
    
    if(useMatrix) {
      permut <- trouverMatriceDePermutation(permut)
    }
    
    temps <- append(temps, functionForComparingTimes(createProcessTest(func, permut, 3, outputProgress = F, stopIfTooBig = stopIfTooBig*2)))
    
    if(isTRUE(stopIfTooBig > 0 & temps[length(temps)] > stopIfTooBig)) {
      if(outputProgress)
        print("== temps dépassé ==")
      temps <- replace(temps, maxN/interval, Inf)
      return(temps)
    }
  }
  return(temps)
}

testsDeFonctions <- function(maxN, intervalBetweenN, stopIfTooBig = 2) {
  xValues <- seq(from=1, to=maxN, by=intervalBetweenN)
  plot(x=xValues, y=doFullTest(calculOrdrePermutationLong, T, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="green", ylim=c(0, stopIfTooBig), main="Temps d'execution d'une fonction selon la taille des donnees", ylab="Temps (s)", xlab="Taille de la permutation", sub="Calcul d'ordre")
  lines(x=xValues, y=doFullTest(calculOrdrePermutation_vecteur, F, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="blue")
  lines(x=xValues, y=doFullTest(calculOrdrePermutation, T, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="red")
  lines(x=xValues, y=doFullTest(calculOrdrePermutationLong, F, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="black")
  
  legend("topleft", inset=0.05, legend=c("Matrice avec boucles", "Vecteur avec boucles", "Vecteur sans boucles", "Matrice sans boucle"), col=c("green", "black", "blue", "red"), title="Types de fonctions", lty=1)
}

testsDeFonctionsRapides <- function(maxN, intervalBetweenN, stopIfTooBig = 3) {
  xValues <- seq(from=1, to=maxN, by=intervalBetweenN)
  plot(x=xValues, y=doFullTest(calculOrdrePermutation_vecteur, F, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="blue", ylim=c(0, stopIfTooBig), main="Comparatif matrice / vecteur de permutation", sub="Calcul d'ordre avec des fonctions \"rapides\"", xlab="Taille de la permutation", ylab="Temps (s)")
  lines(x=xValues, y=doFullTest(calculOrdrePermutation, T, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="red", ylim=c(0, stopIfTooBig))
  legend("topleft", inset=0.05, legend=c("Vecteur", "Matrice"), col=c("blue", "red"), title="Type de fonction", lty=1)
}

testsDeFonctionsLongues <- function(maxN, intervalBetweenN, stopIfTooBig = 3) {
  xValues <- seq(from=1, to=maxN, by=intervalBetweenN)
  plot(x=xValues, y=doFullTest(calculOrdrePermutationLong_vecteur, F, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="black", ylim=c(0, stopIfTooBig), main="Comparatif matrice / vecteur de permutation", sub="Calcul d'ordre avec des fonctions \"longues\"", xlab="Taille de la permutation", ylab="Temps (s)")
  lines(x=xValues, y=doFullTest(calculOrdrePermutationLong_matrice, T, intervalBetweenN, maxN, stopIfTooBig = stopIfTooBig, functionForComparingTimes = mean), type="l", col="green", ylim=c(0, stopIfTooBig))
  legend("topleft", inset=0.05, legend=c("Vecteur", "Matrice"), col=c("black", "green"), title="Type de fonction", lty=1)
}
