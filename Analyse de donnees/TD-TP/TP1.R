
ecartType <- function(values) {
  return(sqrt(sum(((values - mean(values))**2)/length(values))))
}
trace_matrix <- function(matrice) {
  return(sum(diag(matrice)))
}
vecteurs_orthogonaux <- function(v1, v2) {
  if(v1 %*% v2 == 0)
    return(T)
  return(F)
}

# 1.
X1 <- c(4, 2, 1, 1, 3, 4, 5, 2, 1, 3)
X2 <- c(1, 4, 2, 3, 2, 2, 4, 2, 3, 3)
X3 <- c(5, 0, 5, 1, 4, 2, 3, 5, 5, 5)

exo1 <- function(values) {
  print(c("Moyenne : ", mean(values)))
  print(c("Mediane : ", median(values)))
  print(c("Ecart-t : ", ecartType(values)))
}

# 2.
"Rien à faire..."

# 3.
A <- matrix(c(3,1,2,3,1,3,3,2,2,2,1,1), nrow=4)
B <- matrix(c(1,0,1,0,1,1,1,1,0), nrow=3)
C <- matrix(c(-1,-1,1,1,-1,-1,0,0,1), nrow=3)
U <- matrix(c(1,-1,1), nrow=3)
V <- matrix(c(1,2,-2,-1), nrow=4)

exo3_b <- function(m1, m2) {
  if(ncol(m1) != nrow(m2)) {
    print("m1 et m2 n'ont pas les dimensions adaptées")
  } else {
    print(m1 %*% m2)
  }
}
exo3_c <- function(m1, m2) {
  exo3_b(t(m1), m2)
}
exo3_d <- function(m1, m2, m3) {
  print(t(m1) %*% m2 %*% m3)
}

# 4.
X <- matrix(c(1,0,0,1), nrow=2)
Xb <- matrix(rep(colMeans(X), 2), byrow=T, nrow=2)
V <- (1/2)*t(X - Xb)%*%(X - Xb)
valPropres <- sort(eigen(V)$values)
tr <- trace_matrix(V)
vectPropre <- eigen(V)$vectors
if(vecteurs_orthogonaux(vectPropre[,1], vectPropre[,2])) {
  print("Vecteurs orthogonaux")
} else {
  print("Vecteurs non-orthogonaux")
}

# 5.
M <- matrix(c(1, 4, 2, 3), nrow=2)
N <- matrix(c(2, 3, 2, 1), nrow=2)
P <- matrix(c(1,0,0,0,0,1,0,1,0), nrow=3)

exo5 <- function(matrice) {
  print(eigen(matrice))
}

# 6.

# 12.
M1 <- matrix(c(1,0,0,0,0,0,0,4,0,3,0,0,2,0,0,0), nrow=4)
M2 <- matrix(c(1,1,0,-1,0,1,0,1,1), nrow=3)

svd_maigre <- function(matrice) {
  propres <- eigen(matrice)
  values <- propres$values[propres$values != 0]
  vectors <- propres$vectors[propres$values != 0]
  print(values)
  print(vectors)
}
