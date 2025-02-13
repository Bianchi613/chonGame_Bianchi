package chon.group;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

import chon.group.game.domain.agent.Agent;
import chon.group.game.domain.environment.Environment;
import chon.group.game.drawer.EnvironmentDrawer;
import chon.group.game.drawer.JavaFxMediator;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * The {@code Engine} class represents the main entry point of the application
 * and serves as the game engine for "Chon: The Learning Game."
 */
public class Engine extends Application {

    private boolean isPaused = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage theStage) {
        try {
            // Inicializa a tela inicial com a imagem
            Image startImage = new Image(getClass().getResource("/images/environment/MoonPatrol.jpg").toExternalForm());
            ImageView startImageView = new ImageView(startImage);

            // Configura a tela inicial
            StackPane startRoot = new StackPane();
            startRoot.getChildren().add(startImageView);
            Scene startScene = new Scene(startRoot, 950, 700); // Tamanho da cena inicial
            theStage.setTitle("Chon: The Learning Game");
            theStage.setScene(startScene);
            theStage.show();

            // Usar Timeline para esperar 6 segundos antes de iniciar o jogo
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> startGame(theStage))); 
            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para iniciar o jogo após 6 segundos
    public void startGame(Stage theStage) {
        try {
            // Inicializa o ambiente do jogo e os agentes
            Environment environment = new Environment(0, 0, 1280, 780, "/images/environment/moon'surface.png");
            Agent chonBota = new Agent(400, 390, 60, 80, 3, 100, "/images/agents/moon.png", false);
            Agent chonBot = new Agent(920, 440, 60, 80, 1, 5, "/images/agents/moonE.png", true);
            environment.setProtagonist(chonBota);
            environment.getAgents().add(chonBot);
            environment.setPauseImage("/images/environment/pause.png");

            // Configura o canvas gráfico
            Canvas canvas = new Canvas(environment.getWidth(), environment.getHeight());
            GraphicsContext gc = canvas.getGraphicsContext2D();
            EnvironmentDrawer mediator = new JavaFxMediator(environment, gc);

            // Configura a cena do jogo
            StackPane root = new StackPane();
            Scene scene = new Scene(root, environment.getWidth(), environment.getHeight());
            theStage.setScene(scene);
            root.getChildren().add(canvas);

            // Configura os controles do teclado
            ArrayList<String> input = new ArrayList<>();
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    String code = e.getCode().toString();
                    input.clear();
                    if (code.equals("P")) {
                        isPaused = !isPaused;
                    }

                    if (code.equals("SPACE")) {
                        environment.getProtagonist().jump();
                    }

                    if (!isPaused && !input.contains(code)) {
                        input.add(code);
                    }
                }
            });

            scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    String code = e.getCode().toString();
                    input.remove(code);
                }
            });

            // Começa o loop do jogo
            new AnimationTimer() {
                @Override
                public void handle(long arg0) {
                    mediator.clearEnvironment();
                    if (isPaused) {
                        mediator.drawBackground();
                        mediator.drawAgents();
                        mediator.drawPauseScreen();
                    } else {
                        if (!input.isEmpty()) {
                            environment.getProtagonist().move(input);
                            environment.checkBorders();
                        }

                        environment.getAgents().get(0).chase(environment.getProtagonist().getPosX(),
                                environment.getProtagonist().getPosY());

                        environment.detectCollision();
                        mediator.drawBackground();
                        mediator.drawAgents();
                    }
                }
            }.start();
            theStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
