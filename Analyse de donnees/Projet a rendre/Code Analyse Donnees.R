library(ade4)
library(FactoMineR)
library(gplots)
library(crayon)
library(stringr)
library(car)
library(varhandle)
library(stats)
library(plotrix)

epsilonPower = 8 # Les nombres inferieurs a 10^(-8) seront consideres comme nuls
roundingValue = 2 # Nombre de chiffres apres la virgule a l'affichage

# Calcul de l'inter-quartile
interQuartile <- function(valeurs) {
  return(quantile(valeurs)[4] - quantile(valeurs)[2])
}
# Quelles valeurs sont extremes parmis la colonne
extremValues <- function(column, coef = 1.5) {
  return(column < quantile(column)[2] - coef*interQuartile(column) | 
           quantile(column)[4] + coef*interQuartile(column) < column)
}
# Calcule le coefficient d'asymetrie
skewness <- function(values) {
  center <- mean(values)
  first <- sum((values - center)**3)/length(values)
  second <- (sum((values - center)**2) / length(values))**(3/2)
  return(first/second)
}
# Calcule le coefficient d'applatissement
# Inspiré par la bibliothèque "moments"
kurtosis <- function(values) {
  
  if (is.matrix(values)) 
    return(apply(values, 2, kurtosis))
  else if (is.vector(values)) {
    n <- length(values)
    return(n * (sum((values - mean(values))^4))/(sum((values - mean(values))^2)^2) - 3)
  }
  else if (is.data.frame(values))  {
    return(sapply(values, kurtosis))
  }
  else {
    return(kurtosis(as.vector(values)))
  }
}

# Produit Vectoriel
produitVect <- function(X, Y) {
  return(t(X) %*% Y)
}
# Recupere l'extension du fichier donnee en parametre
getExtension <- function(file){ 
  ex <- strsplit(basename(file), split="\\.")[[1]]
  return(ex[-1])
} 

# Fonction pour recuperer le contenu du fichier sous forme de matrice
recuperer_donnees_en_matrice <- function(fichier, entete=FALSE, transpose = F) {
  if(getExtension(fichier) == "csv") { # Si le fichier est un CSV, on utilise la fonction read.csv
    donnees <- read.csv(fichier, sep=",", header=entete, stringsAsFactors = F) # On recupere les donnees en "brut"
  } else { # Sinon on utilise la fonction read.table, plus generique
    donnees <- read.table(fichier, header = entete, stringsAsFactors = F, dec=",")
  }
  # On transpose les donnees si demande
  if(transpose) donnees <- t(donnees)
  if(!entete) {
    colnames(donnees) <- 1:ncol(donnees)
    rownames(donnees) <- 1:nrow(donnees)
  }
  return(as.matrix(donnees)) # On les transforme en matrice
}
# Recupere la variance d'une colonne de valeurs
getVariance <- function(column) {
  sums <- sum((column - mean(column))**2)
  return(sums/length(column))
}
# Centrer et reduire un tableau de valeurs
centerAndScale <- function(values, center = T, scale = T) {
  if(center){
    values <- apply(values, 2, function(x){ return(x - mean(x))}) # Chaque colonne est centree avec sa moyenne
  }
  if(scale){
    values <- apply(values, 2, function(x){ return(x / sqrt(getVariance(x)))}) 
    # On divise chaque valeur avec la variance de sa colonne
  }
  return(values)
}
# Fonction pour passer les valeurs a l'intervalle [-1;1]
toPercents <- function(values) { # Normalise chaque valeur d'un vecteur
  return(values/sum(values))
}

# Tentative de recuperation du coude d'un eboulis de valeurs
# On cherche la premiere valeur qui marque un "coude", c'est a dire qui reduit l'ecart avec la valeur precedente
# On cherche un ecart entre i et i+1 "negligence fois" inferieur a l'ecart entre i-1 et i
getCoude <- function(values, negligence = 4) { 
  slope <- 0 # On commence avec un ecart minimal
  for(i in 1:(length(values) -1)){
    newSlope <- values[i]-values[i+1] # On a le nouvel ecart entre i et i+1
    if((newSlope*negligence) < slope) { # Si cet ecart est suffisament faible, 
        # on dit que la valeur a recuperer est i-1 (nombre d'axes principaux)
      return(i-1)
    }
    slope <- newSlope # Sinon on recommence
  }
  return(length(values))
}
# Fonction pour "estimer" le nombre de composantes principales de notre jeu de donnees
# On utilise 3 regles :
# - critere de Keiser (inertie superieure a I/p)
# - critere de pourcentage represente (cumul des valeurs propres doivent representer 70% de l'inertie totale)
# - regle du coude
getNbAxes <- function(data) {
  rule1 <- length(which(data$eig > 1))
  rule2 <- getCumSumForValue(toPercents(data$eig), 0.7)
  rule3 <- getCoude(data$eig)
  return(c(rule1, rule2, rule3)) # On retourne tous les resultats
}
# Fonction pour connaitre l'indice pour lequel la somme cumulee est superieure ou egale a une certaine valeur
getCumSumForValue <- function(values, wantedSum) {
  cumul <- 0
  for(i in 1:length(values)) {
    cumul <- cumul + values[i]
    if(isTRUE(cumul >= wantedSum)) {
      return(i)
    }
  }
  return(length(values))
}
# Fonction pour recuperer les valeurs propres superieures a 0
getEigenValues <- function(data) {
  eigenValues <- eigen(data$correlation)$values
  eigenValues[eigenValues < 0] <- 0
  return(eigenValues)
}

## Fonctions d'affichage
# Affichage de l'ebouli de valeurs
displayScreePlot <- function(data) {
  heights <- data$eig[data$eig > 10**(-epsilonPower)] # Les valeurs du barplot sont les 
   # valeurs propres superieures a 0
  percents <- toPercents(heights)*100
  br <- barplot(height = percents, 
                width = 2, 
                ylab = "% inertie", 
                names.arg = round(heights, roundingValue), 
                col = c(rep("red", data$numberOfAxis), # On colore en rouge les X composantes choisies
                        rep("gray", length(heights)-data$numberOfAxis)),
                main = "Eboulis de valeurs propres", 
                ylim = c(0, percents[1]+2))
  text(x = br, 
       y = percents+1, 
       labels = round(percents, roundingValue)) # On affiche le pourcentage represente par chaque composante
  
  lines(x = br, 
        y = percents) # On ajoute une courbe pour reperer le coude
  
  # On cherche a afficher une droite horizontale pour voir les valeurs propres superieures a 1
  heightFor1 <- 100 / sum(data$eig) 
  abline(h = heightFor1)
  text(x = br[length(br)-1], 
       y = heightFor1+1, labels="Valeurs > 1")
  
  # On va afficher une droite veritcale pour distinger les composantes principales qui 
    # expliquent jusu'a 70% de l'inertie
  maxValueFor70 <- getCumSumForValue(percents, 70)
  widthFor70 <- (br[maxValueFor70] + br[maxValueFor70 +1]) / 2
  abline(v = widthFor70)
  text(x = widthFor70 + 0.5, 
       y = percents[1]-2, 
       labels = "% >= 70", 
       srt = 90)
  
  # Affichage du coude avec une fleche
  coudeIndex <- getCoude(heights)
  x0 <- br[coudeIndex+1]
  y0 <- percents[coudeIndex+1]
  arrows(x0 = x0, y0 = y0, x1 = x0 + 2, y1 = y0 + 10, code=1)
  text(x = x0 + 2.1, 
       y = y0 + 11, 
       labels = "Coude")
}

# Affichage des individus sur un plan 2D
# Il est possible de choisir le modele voulu : FactoMineR ou ADE4
displayIndividus <- function(data, axe1=1, axe2=2, colors=NULL, beLike=NULL) {
  coord <- data$ind$coord
  if(ncol(coord) == 1) axe2 = axe1
  if(!is.null(data$ind$sup)) {coord.sup <- data$ind$sup$coord}
  if(!is.null(beLike))
  {
    if(grepl(toupper(beLike), "ADE", fixed=T)) {
      coord[,axe2] = -coord[,axe2]
      if(!is.null(data$ind$sup)) {
        coord.sup[,axe1] = -coord.sup[,axe1]
      }
    } else if(grepl(toupper(beLike), "FACTOMINER", fixed=T)) {
      # Rien a faire
    }
  }
  
  labelOffset <- 0.5 # Valeur arbitraire
  
  # On affiche les individus selon les coordonnees calculees precedemment
  plot(coord[,c(axe1, axe2)], 
       xlim = c(min(coord[,axe1]), max(coord[,axe1])), 
       ylim = c(min(coord[,axe2]), max(coord[,axe2])), 
       xlab = paste("Axe ", axe1, collapse=""),
       ylab = paste("Axe ", axe2, collapse=""),
       col=colors, 
       pch=16,
       asp = 1)
  
  labels = 1:data$nbInd
  if(length(rownames(data$table)) > 0) labels = rownames(data$table)
  text(x = coord[,axe1], 
       y = coord[,axe2]+labelOffset, 
       labels = labels, 
       col = colors)
  
  # S'il y a des individus supplementaires, on les affiche aussi
  if(!is.null(data$ind$sup)) {
    points(x = coord.sup[,axe1],
           y = coord.sup[,axe2],
           pch = 4)
    text(x = coord.sup[,axe1], 
         y = coord.sup[,axe2] + labelOffset, 
         labels = 1:data$ind$sup$nbSup, 
         font = 3)
  }
  displayQuadrillage()
}

# Affichage du quadrillage
displayQuadrillage <- function(interval = NULL, color="gray") {
  # On reecupere les parametre du plot
  param <- par()
  maxX <- max(abs(c(param$xaxp[1], param$xaxp[2])))
  maxY <- max(abs(c(param$yaxp[1], param$yaxp[2])))
  if(is.null(interval))
  {
    interval <- min(abs(c((param$xaxp[1] - param$xaxp[2])/param$xaxp[3], 
                          (param$yaxp[1] - param$yaxp[2])/param$yaxp[3])))
  }
  abline(h=seq(from=-maxY*2, to=maxY*2, by=interval), 
         v=seq(from=-maxX*2, to=maxX*2, by=interval), col="gray")
  abline(h=0, v=0)
  legend(x="topright", legend = paste("d =", interval, collapse=""))
}

# Affichage de la matrice de correlation sous forme d'image
# pour les grandes matrices. Rouge = correlation, jaune = pas de correlation
displayCorrelationAsHeatmap <- function(correlation) {
  image(x = 1:nrow(correlation), 
        y = 1:ncol(correlation), 
        z = correlation, #t(apply(correlation, 2, rev)), 
        col=rev(heat.colors(100)), 
        #xaxt='n', 
        #yaxt='n', 
        ann=FALSE, 
        bty='n', 
        asp=1,
        ylim = rev(c(1, ncol(correlation))),
        xlim = c(1, ncol(correlation)))
  title(main = "Correlation entre les variables", sub = "Representation en couleur")
  param <- par()
  x <- ncol(correlation) + 1
  x2 <- x + ncol(correlation)/20
  y2 <- ncol(correlation)
  y <- y2 - ncol(correlation)/5
  color.legend(xl = x,xr = x2,yt = y, yb = y2, c("1", "", "0"), 
               c("red", "orange", "yellow"), gradient="y", align = "rb")
}

# Affichage des variables dans le cercle des correlations
displayVariables <- function(data, axe1=1, axe2=2, beLike=NULL, useArrows=T, colorWeakValues = F) {
  coord <- data$var$coord
  if(ncol(coord) == 1) axe2 = axe1
  coord <- coord[,c(axe1, axe2)]
  if(!is.null(data$var$sup)) {
    
    coord.sup <- data$var$sup$coord[,c(axe1, axe2)]
  }
  if(!is.null(beLike))
  {
    if(grepl(toupper(beLike), "ADE", fixed=T)) {
      coord[,1] <- -coord[,1]
      if(!is.null(data$var$sup)) {
        coord.sup[,1] <- -coord.sup[,1]
      }
    } else if(grepl(toupper(beLike), "FACTOMINER", fixed=T)){
      # Rien a faire
    }
  }
  # On initialise le graphique
  plot(x = coord, 
       xlim=c(-1,1), 
       ylim=c(-1,1), 
       type="n",
       xlab = paste("Axe ", axe1, collapse=""),
       ylab = paste("Axe ", axe2, collapse=""),
       asp=1)
  displayQuadrillage()
  
  # Affichage du cercle de correlation
  piSeq <- seq(from=0, to=2*pi, length=100)
  circleValuesX = cos(piSeq)
  circleValuesY = sin(piSeq)
  lines(x = circleValuesX, y = circleValuesY)
  color = "blue"
  if(colorWeakValues) {
    color = apply(coord, 1, FUN=function(row){ 
        if(max(row[c(axe1, axe2)]) < colorWeakValues) {
          return("blue") 
        } else {
          return("red")
        }
      })
  }
  
  if(useArrows) {
    # On utilise des fleches pour representer les variables
    arrows(x0=rep(0, n=data$nbVar), 
           y0=rep(0, data$nbVar), 
           x1=coord[,1], 
           y1=coord[,2], 
           col=color)
  } else {
    points(x=coord[,1], 
           y=coord[,2], 
           col=color,
           pch = 4)
  }
  
  # Affichage des noms de variables
  if(!is.null(names(data$table))) {
    label <- names(data$table)
  } else if(length(rownames(data$var$coord)) > 0) {
    label <- rownames(data$var$coord)
  } else {
    label <- 1:data$nbVar
  }
  text(x = coord[,1], 
       y = coord[,2], 
       labels = label, 
       col = color,
       pos = 1)
  
  # Affichage des variables supplementaires
  if(!is.null(data$var$sup)) {
    if(useArrows){
      arrows(x0=rep(0, n=data$var$sup$nbSup), 
             y0=rep(0, data$var$sup$nbSup), 
             x1=coord.sup[,1], 
             y1=coord.sup[,2], 
             col="green",
             lty = "dotted")
    } else {
      points(x=coord.sup[,1], 
             y=coord.sup[,2], 
             col="green",
             pch = 9)
    }
    if(length(rownames(data$var$sup$coord)) > 0) {
      label.sup <- rownames(data$var$sup$coord)
    } else {
      label.sup <- 1:data$var$sup$nbSup
    }
    text(x = coord.sup[,1], 
         y = coord.sup[,2], 
         labels = label.sup, 
         col = "green",
         pos = 1)
  }
}

# Fonction SVD
# Code réutilisé de FactoMineR
SVD <- function(data) {
  X <- data$table
  row.w <- rep(1/nrow(X), nrow(X))
  col.w <- rep(1, ncol(X))
  ncp <- data$numberOfAxis
  row.w <- row.w / sum(row.w)
  # X <- t(t(X)*sqrt(col.w))*sqrt(row.w)
  if (ncol(X) < nrow(X)){
    mSVD <- svd(X,nu=ncp,nv=ncp)
    if (names(mSVD)[[1]]=="message"){
      mSVD <- svd(t(X),nu=ncp,nv=ncp)
      if (names(mSVD)[[1]]=="d"){
        aux <- mSVD$u
        mSVD$u <- mSVD$v
        mSVD$v <- aux
      } else{
        bb <- eigen(crossprod(X,X),symmetric=TRUE)
        mSVD <- vector(mode = "list", length = 3)
        mSVD$d[mSVD$d<0]=0
        mSVD$d <- sqrt(mSVD$d)
        mSVD$v <- bb$vec[,1:ncp]
        #          mSVD$u <- sweep(X%*%mSVD$v,2,mSVD$d[1:ncp],FUN="/")
        mSVD$u <- t(t(crossprod(t(X),mSVD$v))/mSVD$d[1:ncp])
      }
    }
    U <- mSVD$u
    V <- mSVD$v
    if (ncp >1){
      mult <- sign(as.vector(crossprod(rep(1,nrow(V)),as.matrix(V))))
      mult[mult==0] <- 1
      U <- t(t(U)*mult)
      V <- t(t(V)*mult)
    }
    U <- U/sqrt(row.w)
    V <- V/sqrt(col.w)
  }
  else{
    mSVD <- svd(t(X),nu=ncp,nv=ncp)
    if (names(mSVD)[[1]]=="message"){
      mSVD <- svd(X,nu=ncp,nv=ncp)
      if (names(mSVD)[[1]]=="d"){
        aux <- mSVD$u
        mSVD$u <- mSVD$v
        mSVD$v <- aux
      } else{
        bb <- eigen(crossprod(t(X),t(X)),symmetric=TRUE)
        mSVD <- vector(mode = "list", length = 3)
        mSVD$d[mSVD$d<0]=0
        mSVD$d <- sqrt(mSVD$d)
        mSVD$v <- bb$vec[,1:ncp]
        mSVD$u <- t(t(crossprod(X,mSVD$v))/mSVD$d[1:ncp])
      }
    }
    U <-  mSVD$v
    V <- mSVD$u
    mult <- sign(as.vector(crossprod(rep(1,nrow(V)),as.matrix(V))))
    mult[mult==0] <- 1
    V <- t(t(V)*mult)/sqrt(col.w)
    U <- t(t(U)*mult)/sqrt(row.w)
  }
  vs <- mSVD$d[1:min(ncol(X),nrow(X)-1)]
  num <- which(vs[1:ncp]<1e-15)
  if (length(num)==1){
    U[,num] <- U[,num,drop=FALSE]*vs[num]
    V[,num] <- V[,num,drop=FALSE]*vs[num]
  } 
  if (length(num)>1){
    U[,num] <- t(t(U[,num])*vs[num])
    V[,num] <- t(t(V[,num])*vs[num])
  }
  res <- list(vs = vs, U = U, V = V)
  return(res)
}


# Affichage du rapport d'ACP
displayFullSummary <- function(pca, colors = "auto", beLike = NULL, 
                               displayLimit = 20, numberOfClasses = 2, useOriginalTable = F, 
                               bestContributions = 30, variableType = "variables") {
  data <- pca
  # On retrouve les couleurs utilisees pour les classes
  classColors = colors
  colorToBlack <- F
  if(!is.null(classColors)) {
    colorToBlack <- T
    if(!grepl(toupper(classColors), "AUTO", fixed=T)) {
      classColors = levels(as.factor(classColors))
      colorToBlack <- F
    }
  } 
  if(colorToBlack) {
    colors = "black"
  }
  
  # Nombre de variables maximal a afficher par graphique
  maxGenesPerPlot <- 50
  usedTable = data$table
  if(useOriginalTable) usedTable <- data$originalTable
  
  ylim = c(min(usedTable), max(usedTable))
  
  # Si toutes les variables tiennent dans un graphique
  if(data$nbVar <= maxGenesPerPlot) {
    boxplot(usedTable, 
            main = paste("Representation des ", variableType), 
            ylim = ylim, 
            xlab= paste("Nom de ", variableType), 
            ylab= paste("Valeurs des ", variableType))
  }
  else { # Sinon on realise plusieurs graphiques
    for(i in 1:(ceiling(data$nbVar) / maxGenesPerPlot)) {
      start <- (i-1)*maxGenesPerPlot+1
      end <- min(i*maxGenesPerPlot, data$nbVar)
      boxplot(usedTable[,start:end], 
              main=paste("Representation des ", variableType, " ", start, " a ", 
                         end, collapse=""), 
              ylim = ylim, 
              xlab = paste("Nom de ", variableType), 
              ylab = paste("Valeurs des ", variableType))
    }
  }
  
  
  # Eboulis de valeurs propres avec lignes additionnelles
  displayScreePlot(data)
  
  # Heatmap de la matrice de correlation
  displayCorrelation(data)
  
  # affichage des variables et des individus
  if(data$numberOfAxis > 1) { # Cas classique avec plusieurs composantes principales
    for(i in 1:(data$numberOfAxis-1)) {
      for(j in (i+1):data$numberOfAxis) { # On fait chaque combinaison d'axes possibles
        # Affichage des variables
        displayVariables(data, axe1 = i, axe2 = j, beLike=beLike)
        # Affichage des individus
        displayIndividus(data, colors=colors, axe1 = i, axe2 = j, beLike=beLike)
        # Classification
        displayClasses(data, axe1 = i, axe2 = j, colors = classColors, 
                       usePreviousGraph = T, beLike=beLike, nbCenters = numberOfClasses)
      }
    }
  } else {
    # S'il n'y a qu'un axe, on gere ce cas
    displayIndividus(data, colors = colors, axe1 = 1, axe2 = 1)
  }
  
  # Affichage dans la console de l'inertie des individus et des variables
  displayInertiaIndividus(data, displayLimit = displayLimit, bestContribution = bestContributions)
  displayInertiaVariables(data, displayLimit = displayLimit, bestContribution = bestContributions)
  
}

# Fonction pour afficher le tableau de correlation
displayCorrelation <- function(data) {
  if(data$nbVar < 10) { # Pour peu de variables, on affiche le tableau de correlation en console
    print(data$correlation)
  } else { # Sinon on l'affiche graphiquement
    displayCorrelationAsHeatmap(data$correlation)
  }
}

# Affichage de l'inertie et contribution des individus sur les axes
displayInertiaIndividus <- function(data, bestContribution=NULL, 
                                    displayLimit = Inf, numberOfAxis = Inf) {
  numberOfAxis <- min(data$numberOfAxis, numberOfAxis)
  displayLimit = min(displayLimit, data$nbInd)
  
  # Affichage des coordonnees
  print("Coordonnees des individus")
  print(round(data$ind$coord[1:displayLimit, 1:numberOfAxis], roundingValue))
  
  # Affichage des contributions
  print("[CTR en %]")
  print(round(data$ind$contrib[1:displayLimit, 1:numberOfAxis] * 100, roundingValue))
  
  # Affichage de la qualite de representation
  print("[QLT en %]")
  print(round(data$ind$quality[1:displayLimit, 1:numberOfAxis] * 100, roundingValue))
  
  # Pour chaque axes, on affiche les plus grosses contributions
  for(axe in 1:numberOfAxis) {
    # Contributions positives
    bestPlus <- sort(data$ind$contrib[,axe][data$ind$coord[,axe] > 0], decreasing = T)
    bestPlus <- bestPlus[bestPlus > 1/ data$nbInd]
    print(paste("Individus contributant le plus a l'axe", axe, "(positif)", collapse=" "))
    if(!is.null(bestContribution)) bestPlus <- head(bestPlus, n = bestContribution)
    print(round(bestPlus * 100, roundingValue))
    
    # Contributions negatives
    bestMoins <- sort(data$ind$contrib[,axe][data$ind$coord[,axe] < 0], decreasing = T)
    bestMoins <- bestMoins[bestMoins > 1/ data$nbInd]
    print(paste("Individus contributant le plus a l'axe", axe, "(negatif)", collapse=" "))
    if(!is.null(bestContribution)) bestMoins <- head(bestMoins, n = bestContribution)
    print(round(bestMoins * 100, roundingValue))
  }
}

# Affichage des inerties et qualites des variables
displayInertiaVariables <- function(data, bestContribution=NULL, 
                                    displayLimit = Inf, numberOfAxis = Inf) {
  numberOfAxis <- min(data$numberOfAxis, numberOfAxis)
  displayLimit = min(displayLimit, data$nbVar)
  
  # Affichage des coordonnees
  print("Coordonnees des Variables")
  print(round(data$var$coord[1:displayLimit, 1:numberOfAxis], roundingValue))
  
  # Affichage des contributions
  print("[CTR en %]")
  print(round(data$var$contrib[1:displayLimit, 1:numberOfAxis] * 100, roundingValue))
  
  # Affichage de la qualite de la representation
  print("[QLT en %]")
  print(round(data$var$quality[1:displayLimit, 1:numberOfAxis] * 100, roundingValue))
  
  # Pour chaque axes, on affiche les plus grosses contributions
  for(axe in numberOfAxis) {
    # Contributions positives
    bestPlus <- sort(data$var$contrib[,axe][data$var$coord[,axe] > 0], decreasing = T)
    bestPlus <- bestPlus[bestPlus > 1/ data$nbVar]
    print(paste("Variables contributant le plus a l'axe", axe, "(positif)", collapse=" "))
    if(!is.null(bestContribution)) bestPlus <- head(bestPlus, n = bestContribution)
    print(round(bestPlus * 100, roundingValue))
    
    # Contributions negatives
    bestMoins <- sort(data$var$contrib[,axe][data$var$coord[,axe] < 0], decreasing = T)
    bestMoins <- bestMoins[bestMoins > 1/ data$nbVar]
    print(paste("Variables contributant le plus a l'axe", axe, "(negatif)", collapse=" "))
    if(!is.null(bestContribution)) bestMoins <- head(bestMoins, n = bestContribution)
    print(round(bestMoins * 100, roundingValue))
  }
}

# Classification non-supervisee par K-means
displayClasses <- function(data, nbCenters = 2, axe1 = 1, axe2 = 2, 
                           colors = rainbow(nbCenters), usePreviousGraph = F, beLike=NULL) {
  coord = data$ind$coord
  if(!is.null(beLike)) {
    # Pour un affichage façon "FactoMineR", on inverse l'axe 1
    if(grepl(toupper(beLike), "ADE", fixed=T))
    {
      coord[,axe2] = -coord[,axe2]
    }
    else if(grepl(toupper(beLike), "FACTOMINER", fixed=T))
    {
      # Rien a faire
    }
  }
  # On retrouve les couleurs utilisees pour les classes
  classColors = colors
  colorToBlack <- F
  if(!is.null(classColors)) {
    colorToBlack <- T
    if(!grepl(toupper(classColors), "AUTO", fixed=T)) {
      classColors = levels(as.factor(classColors))
      colorToBlack <- F
    }
  } 
  colors = classColors
  # Utilisation de la fonction kmeans de la library "stats"
  kmean <- kmeans(coord[, c(axe1, axe2)], nbCenters, nstart = 1)
  # Si on souhaite un nouveau graphique, on lance l'affichage des individus
  if(!usePreviousGraph){
    displayIndividus(data, axe1=axe1, axe2=axe2, colors=colors[kmean$cluster])
  }
  # Affichage de chaque groupes
  for(i in 1:nbCenters) {
    valeurs <- coord[which(kmean$cluster == i),]
    if(!is.matrix(valeurs)) {
      # Leger traitement dans le cas où il y a un groupe avec un seul individu
      valeurs <- t(as.matrix(valeurs))
    }
    if(identical(toupper(colors), "AUTO"))
    {
      color = rainbow(n = nbCenters+1)[i+1]
      # Si on ne souhaite pas de couleurs, on affiche les groupes en tracant une ligne entre les points et le centre du groupe
      segments(x0=valeurs[,axe1], 
               y0=valeurs[,axe2], 
               x1=rep(kmean$centers[i, 1], nrow(valeurs)), 
               y1=rep(kmean$centers[i, 2], nrow(valeurs)),
               col = color)
    } 
    else {
      # Sinon on affiche une ellipse d'inertie
      ell <- dataEllipse(valeurs[,axe1], valeurs[,axe2], levels = 0.95, draw = F)
      polygon(ell, col=colors[i], density = 5)
    }
  }
}

# Comparer deux variables  pour savoir s'il y a des differences significatives entre les bibliotheques
compareValues <- function(v1, v2) {
  v1 <- as.matrix(v1)
  v2 <- as.matrix(v2)
  # Il est possible qu'une matrice est transposee par rapport a l'autre, on corrige grossierement
  if(ncol(v1) != ncol(v2) & ncol(v1) == nrow(v2)) v2 <- t(v2)
  
  # On verifie les dimensions
  if(identical(dim(v1), dim(v2))) {
    print(paste("Dimensions identiques : ", dim(v1)[1], " x ", dim(v2)[2], collapse=""))
  } else {
    print(paste("Dimension 1 : ", dim(v1)[1], " x ", dim(v1)[2], collapse=""))
    print(paste("Dimension 2 : ", dim(v2)[1], " x ", dim(v2)[2], collapse=""))
  }
  # On calcule la difference entre les matrices
  dimensions <- c(min(dim(v1)[1], dim(v2)[1]), min(dim(v1)[2], dim(v2)[2]))
  difference <- abs(v1[1:dimensions[1], 1:dimensions[2]]) - abs(v2[1:dimensions[1], 1:dimensions[2]])
  if(any(which(difference > 10^(-epsilonPower)) > 0)) { # S'il y a une difference, on affiche la matrice de difference
    print("Difference remarquee")
    print(v1[1:dimensions[1], 1:dimensions[2]] - v2[1:dimensions[1], 1:dimensions[2]])
    return(FALSE)
  } else { # Sinon, on est content
    print("Pas de difference")
    return(TRUE)
  }
}

# Comparaison des valeurs trouvees entre notre fonction et celle de FactoMineR
comparaisonAvecFactoMineR <- function(table, center=T, 
                                      scale=T, ind.sup=NULL, 
                                      var.sup=NULL, numberOfAxis = 5) 
{
  print("Comparaison avec FactoMineR :")
  print("-----------------------------")
  # Il semblerait que FactoMineR ne peut pas centrer sans reduire
  centrerReduire <- center & scale
  # On lance les PCA avec FactoMineR et notre fonction pour les comparer
  facto <- FactoMineR::PCA(table, scale.unit = centrerReduire, 
                           ind.sup = ind.sup, quanti.sup = var.sup, 
                           graph = F, ncp = numberOfAxis)
  nantes <- nantes_pca(table, center = centrerReduire, scale = centrerReduire, 
                       additionalIndividus = ind.sup, additionalVariables = var.sup, 
                       numberOfAxis = numberOfAxis)
  
  egal <- TRUE
  # SVD :
  print("Valeurs propres :")
  egal <- compareValues(facto$eig[,1], nantes$eig[nantes$eig > 10^(-epsilonPower)])
  
  print("Matrice U :")
  egal <- compareValues(facto$svd$U, nantes$svd$U)
  
  print("Matrice V :")
  egal <- compareValues(facto$svd$V, nantes$svd$V)
  
  print("Valeurs singulieres :")
  egal <- compareValues(facto$svd$vs, nantes$svd$vs)
  
  # Individus :
  print("Coordonnees des individus :")
  egal <- compareValues(facto$ind$coord, nantes$ind$coord)
  
  # Variables :
  print("Coordonnees des variables :")
  egal <- compareValues(facto$var$coord, nantes$var$coord)
  
  print(egal)
  if(egal == TRUE) {
    print("Tout est identique! Bravo.")
    return(TRUE)
  }
  return(FALSE)
}
# Comparaison des valeurs trouvees entre notre fonction et celle de ADE4
comparaisonAvecADE4 <- function(table, center=T, scale=T, ind.sup=NULL, 
                                var.sup=NULL, numberOfAxis = 5) 
{
  print("Comparaison avec ADE4 :")
  print("-----------------------")
  # Il semblerait que ADE4 ne traite pas les individus et variables supplementaires
  ind.sup = NULL
  var.sup = NULL
  # On lance les PCA avec FactoMineR et notre fonction pour les comparer
  ade <- ade4::dudi.pca(table, center = center, scale = scale, 
                        scannf = F, nf = numberOfAxis, )
  nantes <- nantes_pca(table, center = center, scale = scale, 
                       additionalIndividus = ind.sup, 
                       additionalVariables = var.sup, 
                       numberOfAxis = numberOfAxis)
  
  egal <- TRUE
  # SVD :
  print("Valeurs propres :")
  egal <- compareValues(ade$eig, nantes$eig[nantes$eig > 10^(-epsilonPower)])
  
  # Individus :
  print("Coordonnees des individus :")
  egal <- compareValues(ade$li, nantes$ind$coord)
  
  # Variables :
  print("Coordonnees des variables :")
  egal <- compareValues(ade$co, nantes$var$coord)
  
  print(egal)
  if(egal == TRUE) {
    print("Tout est identique! Bravo.")
    return(TRUE)
  }
  return(FALSE)
}


# Notre fonction d'ACP
nantes_pca <- function(table, center=T, scale=T, scannf = F, 
                       numberOfAxis=NULL, additionalVariables=NULL, 
                       additionalIndividus=NULL, minAxis=2) {
  
  data <- list()
  originalTable <- table
  data$originalTable <- table # On garde en memoire notre tableau d'origine avant modifications
  
  ind.sup = NULL
  var.sup = NULL
  if(!is.null(additionalIndividus)) { # S'il y a des individus supplementaires
    ind.sup <- as.matrix(originalTable[additionalIndividus,]) # On les place dans une variable
    # Peut-être a decommenter
    if(!is.null(additionalVariables)) { # S'il y a en plus des variables supplementaires, on les retire de notre tableau
      ind.sup <- ind.sup[, -additionalVariables]
    }
    # Ou peut-être a commenter...
    table <- table[-additionalIndividus,] # On supprime les individus de notre table
  }
  if(!is.null(additionalVariables)) { # S'il y a des variables supplementaires
    var.sup <- as.matrix(originalTable[, additionalVariables]) # On les place dans une variable
    if(!is.null(additionalIndividus)) { # On retire les lignes concernees par des individus supplementaires
      var.sup <- as.matrix(var.sup[-additionalIndividus, ])
    }
    table <- table[,-additionalVariables] # On retire les variables de notre table
  }
  # En cours : retirer les variables "qualitatives" de notre table
  # qualitatifs <- 
  
  
  nbIndividus <- nrow(table)
  nbParameters <- ncol(table)
  data$nbInd <- nbIndividus
  data$nbVar <- nbParameters
  
  #if (!exists("rowW") | is.null(rowW)) 
  rowW <- rep(1, nbIndividus)
  #if (!exists("colW") | is.null(colW)) 
  colW <- rep(1/nbParameters, nbParameters)
  if(is.null(colnames(table))) colnames(table) <- 1:ncol(table)
  D <- diag(rep(1/nbIndividus, nbIndividus))
  Q <- diag(rep(1, nbParameters))
  
  # On recupere la table centree non-reduite
  centered <- centerAndScale(table, center=T, scale=F)
  # la table centree-reduite
  centeredReducted <- centerAndScale(table, center=T, scale=T)
  # et celle demandee
  data$table <- centerAndScale(table, center=center, scale=scale)
  
  # On stock la moyenne
  cent <- colMeans(table)
  # Et les ecarts-types
  norm <- apply(table, 2, function(x){ sqrt(getVariance(x)) })
  data$centeredReducted <- centeredReducted
  
  # La table de covariance se recupere avec S = Xc' D Xc
  covariance <- t(centered) %*% D %*% centered
  # La table de correlation se calcule avec R = Xcr' D Xcr Q
  correlation <- (t(centeredReducted) %*% D %*% centeredReducted %*% Q)
  data$correlation <- correlation
  
  # Recuperation des valeurs propres
  eigenValues <- getEigenValues(data)
  data$eig <- eigenValues
  
  # Le rang est defini par le nombre de valeurs propres (superieures a 0)
  rank <- length(eigenValues[eigenValues>0])
  
  # Si on ne demande pas un nombre de composantes exact, on le calcul "automatiquement"
  if(scannf){
    data$numberOfAxis = 0
    displayScreePlot(data)
    numberOfAxis <- as.integer(readline(prompt = "Nombre d'axes à garder : "))
  } else if(is.null(numberOfAxis)){
    numberOfAxis <- min(getNbAxes(data))
    numberOfAxis <- max(numberOfAxis, minAxis)
  }
  data$numberOfAxis <- numberOfAxis
  
  # Les composantes de la SVD (U et V) sont calculees dans une fonction a part
  svd <- SVD(data)
  U <- svd$U
  V <- svd$V
  data$svd <- svd
  
  # On va raccourcir la liste de valeurs propres si elle est trop grande 
  # pour les donnees, on reduit
  # (Solution peut-être simplifiee, mais suffisante...)
  if(length(eigenValues) > min(nbIndividus, nbParameters)) {
    eigenValues <- eigenValues[1:min(nbIndividus, nbParameters)]
    data$rank <- min(nbIndividus, nbParameters)
  }
  # Les valeurs singulieres sont les racines carrees des valeurs propres
  VS <- sqrt(eigenValues)
  
  # La suite des calculs a été inspiré par FactoMineR
  # Les coordonnees des individus et variables
  # Individus = U * VS
  ind.coord <- t(t(as.matrix(U)) * VS[1:numberOfAxis])
  # Variables = V * VS
  var.coord <- t(t(as.matrix(V)) * VS[1:numberOfAxis])
  
  # Ajout de noms si possible aux matrices
  colnames(ind.coord) <- paste("Axe", 1:ncol(ind.coord), sep=" ")
  if(length(rownames(table)) > 0) {
    rownames(ind.coord) <- rownames(table)
  } else {
    rownames(ind.coord) <- 1:nbIndividus
  }
  colnames(var.coord) <- paste("Axe", 1:ncol(var.coord), sep=" ")
  if(length(colnames(table)) > 0) {
    rownames(var.coord) <- colnames(table)
  } else {
    rownames(var.coord) <- 1:nbParameters
  }
  
  # La contribution par axe est calcule par (coord^2)/valeur propre de l'axe
  ind.contrib <- t(t(ind.coord**2) / eigenValues[1:ncol(ind.coord)])
  var.contrib <- t(t(var.coord**2) / eigenValues[1:ncol(var.coord)])
  # Si la valeur propre est tres proche de 0, la contribution 
  # sera proche de +infini, dans le doute, on passe a 0...
  ind.contrib[ind.contrib > 100] <- 0
  var.contrib[var.contrib > 100] <- 0 
  
  # Distance entre les individus : somme des coordonnees ^2
  ind.distanc <- rowSums(ind.coord**2)
  # Distance entre les variables : 
  var.distanc <- as.vector(t(rep(1, nrow(centeredReducted))) %*% 
                             as.matrix((centeredReducted**2)/nbIndividus))
  
  ind.quality <- ind.coord**2 / ind.distanc
  colnames(ind.quality) <- paste("Axe", 1:ncol(ind.quality), sep=" ")
  rownames(ind.quality) <- 1:nbIndividus
  
  var.cor <- var.coord / sqrt(var.distanc)
  var.quality <- var.cor**2
  colnames(var.quality) <- paste("Axe", 1:ncol(var.quality), sep=" ")
  if(length(colnames(table)) > 0) {
    rownames(var.quality) <- colnames(table)
  } else {
    rownames(var.quality) <- 1:nbParameters
  }
  
  data$ind <- list(coord = ind.coord, contrib = ind.contrib, 
                   quality = ind.quality, dist = ind.distanc)
  data$var <- list(coord = var.coord, contrib = var.contrib, 
                   quality = var.quality, dist = var.distanc, cor = var.cor)
  data$call <- list()
  
  if(!is.null(ind.sup)) {
    ind.sup.centered <- t(t(ind.sup) - cent)
    ind.sup.centeredReducted <- t(t(ind.sup.centered) / norm)
    
    ind.sup.coord <- ind.sup.centeredReducted %*% svd$V
    ind.sup.distanc <- rowSums(ind.sup.coord**2)
    ind.sup.contrib <- 1#t(t((ind.sup.coord**2) * rowW / sum(rowW)) / eigenValues)
    ind.sup.quality <- ind.sup.coord**2 / ind.sup.distanc
    
    data$ind$sup <- list(nbSup = nrow(ind.sup), 
                         coord = ind.sup.coord, 
                         contrib = ind.sup.contrib, 
                         quality = ind.sup.quality, 
                         dist = ind.sup.distanc)
  }
  if(!is.null(var.sup)) {
    var.sup.centered <- t(t(var.sup) - colMeans(originalTable)[additionalVariables])
    var.sup.norm <- sqrt(colSums((var.sup**2)/ncol(var.sup)))
    var.sup.centeredReducted <- t(t(var.sup.centered) / var.sup.norm)
    
    var.sup.coord <- t(var.sup.centeredReducted) %*% svd$U
    var.sup.contrib <- t(t(var.sup.coord**2) / eigenValues)
    
    var.sup.distanc <- as.vector(produitVect(rep(1,nrow(centeredReducted)),
                                             as.matrix(centeredReducted**2)))
    
    var.sup.cor <- var.sup.coord / sqrt(var.sup.distanc)
    var.sup.quality <- var.sup.cor**2
    
    
    data$var$sup <- list(nbSup = ncol(var.sup), 
                         coord = var.sup.coord, 
                         contrib = var.sup.contrib, 
                         quality = var.sup.quality, 
                         dist = var.sup.distanc)
  }
  return(data)
}

table <- recuperer_donnees_en_matrice("Projet M1 AD 1920.csv", transpose = T, entete = F)
pca <- nantes_pca(table)
displayFullSummary(pca)
