# Teste tes reflexes en JavaFX
Ce programme est entièrement basé sur du JavaFX

Le principe du programme consiste à tester vos réflexes.
Appuyez sur n'importe quelle touche dès qu'un rond apparaît.
Dès qu'un rond apparaît, le chrono se lance. Sa précision est au centième.
Dès que vous appuyez sur n'importe quelle touche :
- le chrono s'arrête
- sa valeur est loggée en console
- un temps d'attente aléatoire est mis en place
- un rond réapparaît etc

Vous pouvez changer les valeurs (constantes)
- du temps d'attente
- le nombre de cases par ligne, 
- l'opacité minimale du rond
- la taille par défaut du rond (dans le constructeur)
le tout à votre guise sans que cela n'impacte le programme
Toutes les autres valeurs sont calculées en fonction de celles-ci.

Changer la taille de la scène (600 px par défaut) risque d'impacter l'offset appliqué pour la dimension de la fenêtre.
Je vous ai prévenu.

Il reste un bug : Le spam permet de tricher apacher. Je n'ai pas trouvé le moyen de corriger ça.

TODO : le faire tourner sur une JRE
https://stackoverflow.com/questions/53453212/how-to-deploy-a-javafx-11-desktop-application-with-a-jre

