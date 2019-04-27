/**
 * Package/imports
 */
package main;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author luckydu43
 */
public class TheReflex extends Application {

	/**
	 * Variables publiques.
	 */

	/**
	 * Variables privées.
	 */
	private Stage fenetre;
	private Timeline timeline;
	private DoubleProperty splitTimeSeconds = new SimpleDoubleProperty();
	private Duration splitTime = Duration.ZERO;
	private final Label compteur = new Label();
	private final Button boutonTechnique = new Button();
	private final EventHandler<KeyEvent> keyEventHandler = event -> actionTouche(event);
	private final ArrayList<Line> damier = new ArrayList<Line>();
	private final double RAYON_CERCLE_MIN;
	/**
	 * CONSTANTES.
	 */
	private static final Random RANDOM = new Random();
	private static final double OPACITE_MINIMUM = 0.2;
	private static final int TEMPS_ATTENTE_MAX_MILLIS = 1;
	private static final int NOMBRE_DE_CASES_PAR_LIGNE = 3;
	// 600 est parfait pour gérer une quantité énorme de cases. C'est divisible par
	// pas mal de nombres ;-)
	private static final double LONGITUDE_MAX = 600;
	private static final double LATITUDE_MAX = TheReflex.LONGITUDE_MAX;
	private static final double RAYON_CERCLE_MAX = (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE) / 2;

	/**
	 * Constructeur sans paramètres.
	 */
	public TheReflex() {
		super();

		// Calcul de la valeur minimale du cercle. 20 par défaut. Eclipse marque une des
		// 2 portions en "dead code" selon la valeur de NOMBRE_DE_CASES_PAR_LIGNE.
		// Normal : ce traitement conditionnel est purement basé sur des constantes !
		if (20 < ((TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE) / 2)) {
			this.RAYON_CERCLE_MIN = 20;
		} else {
			this.RAYON_CERCLE_MIN = (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE) / 2;
		}
		/**
		 * Définition du damier. Pour éviter trop d'instanciations, on définit ces
		 * lignes dans une liste finale.
		 */
		// Le point {0,0} est en haut à gauche, le point {LAT_MAX, LONG_MAX] est en bas
		// à droite.
		// Tout à droite.
		this.damier.add(new Line(TheReflex.LATITUDE_MAX, 0, TheReflex.LATITUDE_MAX, TheReflex.LONGITUDE_MAX));
		// Tout à gauche.
		this.damier.add(new Line(0, 0, 0, TheReflex.LONGITUDE_MAX));
		// Tout en haut (les 2). Il y a 2 lignes décalées d'un pixel pour corriger le
		// problème d'epaisseur de la ligne du haut.
		this.damier.add(new Line(0, 0, TheReflex.LATITUDE_MAX, 0));
		this.damier.add(new Line(0, 1, TheReflex.LATITUDE_MAX, 1));
		// Tout en bas.
		this.damier.add(new Line(0, TheReflex.LONGITUDE_MAX, TheReflex.LATITUDE_MAX, TheReflex.LONGITUDE_MAX));
		// Lignes centrales.
		for (int i = 1; i <= TheReflex.NOMBRE_DE_CASES_PAR_LIGNE; i++) {
			this.damier.add(new Line((TheReflex.LATITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE * i), 0,
					(TheReflex.LATITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE * i), TheReflex.LONGITUDE_MAX));
			this.damier.add(new Line(0, (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE * i),
					TheReflex.LATITUDE_MAX, TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE * i));
		}
	}

	/**
	 * Point d'entrée de l'application.
	 * 
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Surcharge de Application. Permet d'utiliser JavaFX.
	 */
	@Override
	public void start(Stage pStage) {
		// On crée la fenêtre.
		this.creerFenetre(pStage);
		// Bouton technique permettant d'utiliser un Handler de KeyEvent (pour capturer
		// la saisie clavier).
		this.creerBouton();
		// On crée la scène, c-à-d contenu de la fenêtre.
		this.creerScene();
		// On affecte le récupérateur de KeyEvent à la fenêtre, attendant
		// une pression de touche pour lancer l'action.
		this.fenetre.addEventHandler(KeyEvent.KEY_PRESSED, this.keyEventHandler);
		// On appuie sur le bouton pour créer le premier ActionEvent.
		this.boutonTechnique.fire();
	}

	/**
	 * Les paramètres suivants n'ont pas à changer à chaque itération. De plus, pour
	 * une meilleure lecture, il convient de les séparer de la partie "exécution".
	 * 
	 * @param pFenetre
	 */
	private void creerFenetre(Stage pFenetre) {
		// Propriétés de la fenêtre
		this.fenetre = pFenetre;
		this.fenetre.setTitle("Sois vif ;-)");
		// Définition de la taille de la fenêtre. L'offset est ajusté à la main --'
		this.fenetre.setWidth(LONGITUDE_MAX + 17);
		this.fenetre.setHeight(LATITUDE_MAX + 40);

		// Propriétés du compteur
		this.compteur.textProperty().bind(splitTimeSeconds.asString());
		this.compteur.setTextFill(Color.BLUE);
		this.compteur.setStyle("-fx-font-size: 5em;");
	}

	/**
	 * Le bouton n'a pas besoin d'être recréé à chaque itération. Autant déporter
	 * cette partie en prenant soin de placer le bouton en tant que variable de
	 * classe. Il contient une l'eventHandler qui va créer la timeline. Ainsi, en
	 * exécutant le bouton, on génère une timeline. L'eventHandler va alors être
	 * appelé toutes les 10 millisecondes par la KeyFrame de la timeline pour
	 * incrémenter le compteur du chrono affiché à l'écran, d'où la précision au
	 * centième ;-)
	 */
	private void creerBouton() {
		this.boutonTechnique.setOnAction(event -> {
			timeline = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent t) {
					Duration duration = ((KeyFrame) t.getSource()).getTime();
					splitTime = splitTime.add(duration);
					splitTimeSeconds.set(splitTime.toSeconds());
				}
			}));
			timeline.setCycleCount(Timeline.INDEFINITE);
			timeline.play();
		});
		// Le bouton étant un bouton purement technique, il n'a pas à être vu par
		// l'utilisateur. On le masque. Cela n'empêche pas son bon fonctionnement.
		this.boutonTechnique.setVisible(false);
	}

	/**
	 * Cette partie sera relancée à chaque itération. On y génère aléatoirement
	 * l'opacité de la couleur, le rayon et la position du cercle
	 */
	private void creerScene() {
		Group vGroupe = new Group();
		Scene scene = new Scene(vGroupe);
		double rayonCercleGenere = RANDOM.nextInt(
				new Double(TheReflex.RAYON_CERCLE_MAX - this.RAYON_CERCLE_MIN + 1).intValue()) + this.RAYON_CERCLE_MIN;

		// Placement aléatoire du cercle.
		/**
		 * double longitudeMinCercle = rayonCercleGenere; double longitudeMaxCercle =
		 * TheReflex.LONGITUDE_MAX - rayonCercleGenere; double latitudeMinCercle =
		 * rayonCercleGenere; double latitudeMaxCercle = LATITUDE_MAX -
		 * rayonCercleGenere; double longitudeCercleGenere = TheReflex.RANDOM
		 * .nextInt(new Double(longitudeMaxCercle - longitudeMinCercle + 1).intValue())
		 * + longitudeMinCercle; double latitudeCercleGenere = TheReflex.RANDOM
		 * .nextInt(new Double(latitudeMaxCercle - latitudeMinCercle + 1).intValue()) +
		 * latitudeMinCercle;
		 */

		// Placement aléatoire du cercle au centre d'une case. Il y a moins de code car
		// l'ensemble des possibilités est déterminable.
		double longitudeCercleGenere = TheReflex.RANDOM
				.nextInt(new Double(TheReflex.NOMBRE_DE_CASES_PAR_LIGNE).intValue()) * TheReflex.LONGITUDE_MAX
				/ TheReflex.NOMBRE_DE_CASES_PAR_LIGNE
				+ (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE / 2);
		double latitudeCercleGenere = TheReflex.RANDOM
				.nextInt(new Double(TheReflex.NOMBRE_DE_CASES_PAR_LIGNE).intValue()) * TheReflex.LONGITUDE_MAX
				/ TheReflex.NOMBRE_DE_CASES_PAR_LIGNE
				+ (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE / 2);

		double opaciteCercleNoir = (RANDOM.nextDouble() * (1 - TheReflex.OPACITE_MINIMUM) + TheReflex.OPACITE_MINIMUM);
		Circle cercleNoir = new Circle(longitudeCercleGenere, latitudeCercleGenere, rayonCercleGenere,
				Color.web("black", opaciteCercleNoir));
		vGroupe.getChildren().addAll(cercleNoir, this.compteur, this.boutonTechnique);
		vGroupe.getChildren().addAll(this.damier);
		this.fenetre.setScene(scene);
		this.fenetre.show();
		try {
			Thread.sleep(TheReflex.RANDOM.nextInt(TheReflex.TEMPS_ATTENTE_MAX_MILLIS));
		} catch (InterruptedException e) {
			// LoggerFactory.getLogger(TheReflex.class).warn(e.getMessage());
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Execute l'arrêt du chrono, regénère la scène et reset le compteur lors de la
	 * pression d'une touche. La timeline étant propre à la fenêtre et non à la
	 * scène, elle reste exécutée et il suffit simplement de remettre à zéro le
	 * compteur
	 * 
	 * @param keyEvent
	 * @throws Exception
	 */
	private void actionTouche(KeyEvent keyEvent) {
		keyEvent.consume();
		System.out.println(this.compteur.getText());
		this.creerScene();
		this.splitTime = Duration.ZERO;
		this.splitTimeSeconds.set(splitTime.toSeconds());
		this.timeline.playFromStart();
	}
}
