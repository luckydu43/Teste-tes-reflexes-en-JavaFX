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
	 * Variables priv�es.
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
	// 600 est parfait pour g�rer une quantit� �norme de cases. C'est divisible par
	// pas mal de nombres ;-)
	private static final double LONGITUDE_MAX = 600;
	private static final double LATITUDE_MAX = TheReflex.LONGITUDE_MAX;
	private static final double RAYON_CERCLE_MAX = (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE) / 2;

	/**
	 * Constructeur sans param�tres.
	 */
	public TheReflex() {
		super();

		// Calcul de la valeur minimale du cercle. 20 par d�faut. Eclipse marque une des
		// 2 portions en "dead code" selon la valeur de NOMBRE_DE_CASES_PAR_LIGNE.
		// Normal : ce traitement conditionnel est purement bas� sur des constantes !
		if (20 < ((TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE) / 2)) {
			this.RAYON_CERCLE_MIN = 20;
		} else {
			this.RAYON_CERCLE_MIN = (TheReflex.LONGITUDE_MAX / TheReflex.NOMBRE_DE_CASES_PAR_LIGNE) / 2;
		}
		/**
		 * D�finition du damier. Pour �viter trop d'instanciations, on d�finit ces
		 * lignes dans une liste finale.
		 */
		// Le point {0,0} est en haut � gauche, le point {LAT_MAX, LONG_MAX] est en bas
		// � droite.
		// Tout � droite.
		this.damier.add(new Line(TheReflex.LATITUDE_MAX, 0, TheReflex.LATITUDE_MAX, TheReflex.LONGITUDE_MAX));
		// Tout � gauche.
		this.damier.add(new Line(0, 0, 0, TheReflex.LONGITUDE_MAX));
		// Tout en haut (les 2). Il y a 2 lignes d�cal�es d'un pixel pour corriger le
		// probl�me d'epaisseur de la ligne du haut.
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
	 * Point d'entr�e de l'application.
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
		// On cr�e la fen�tre.
		this.creerFenetre(pStage);
		// Bouton technique permettant d'utiliser un Handler de KeyEvent (pour capturer
		// la saisie clavier).
		this.creerBouton();
		// On cr�e la sc�ne, c-�-d contenu de la fen�tre.
		this.creerScene();
		// On affecte le r�cup�rateur de KeyEvent � la fen�tre, attendant
		// une pression de touche pour lancer l'action.
		this.fenetre.addEventHandler(KeyEvent.KEY_PRESSED, this.keyEventHandler);
		// On appuie sur le bouton pour cr�er le premier ActionEvent.
		this.boutonTechnique.fire();
	}

	/**
	 * Les param�tres suivants n'ont pas � changer � chaque it�ration. De plus, pour
	 * une meilleure lecture, il convient de les s�parer de la partie "ex�cution".
	 * 
	 * @param pFenetre
	 */
	private void creerFenetre(Stage pFenetre) {
		// Propri�t�s de la fen�tre
		this.fenetre = pFenetre;
		this.fenetre.setTitle("Sois vif ;-)");
		// D�finition de la taille de la fen�tre. L'offset est ajust� � la main --'
		this.fenetre.setWidth(LONGITUDE_MAX + 17);
		this.fenetre.setHeight(LATITUDE_MAX + 40);

		// Propri�t�s du compteur
		this.compteur.textProperty().bind(splitTimeSeconds.asString());
		this.compteur.setTextFill(Color.BLUE);
		this.compteur.setStyle("-fx-font-size: 5em;");
	}

	/**
	 * Le bouton n'a pas besoin d'�tre recr�� � chaque it�ration. Autant d�porter
	 * cette partie en prenant soin de placer le bouton en tant que variable de
	 * classe. Il contient une l'eventHandler qui va cr�er la timeline. Ainsi, en
	 * ex�cutant le bouton, on g�n�re une timeline. L'eventHandler va alors �tre
	 * appel� toutes les 10 millisecondes par la KeyFrame de la timeline pour
	 * incr�menter le compteur du chrono affich� � l'�cran, d'o� la pr�cision au
	 * centi�me ;-)
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
		// Le bouton �tant un bouton purement technique, il n'a pas � �tre vu par
		// l'utilisateur. On le masque. Cela n'emp�che pas son bon fonctionnement.
		this.boutonTechnique.setVisible(false);
	}

	/**
	 * Cette partie sera relanc�e � chaque it�ration. On y g�n�re al�atoirement
	 * l'opacit� de la couleur, le rayon et la position du cercle
	 */
	private void creerScene() {
		Group vGroupe = new Group();
		Scene scene = new Scene(vGroupe);
		double rayonCercleGenere = RANDOM.nextInt(
				new Double(TheReflex.RAYON_CERCLE_MAX - this.RAYON_CERCLE_MIN + 1).intValue()) + this.RAYON_CERCLE_MIN;

		// Placement al�atoire du cercle.
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

		// Placement al�atoire du cercle au centre d'une case. Il y a moins de code car
		// l'ensemble des possibilit�s est d�terminable.
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
	 * Execute l'arr�t du chrono, reg�n�re la sc�ne et reset le compteur lors de la
	 * pression d'une touche. La timeline �tant propre � la fen�tre et non � la
	 * sc�ne, elle reste ex�cut�e et il suffit simplement de remettre � z�ro le
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
