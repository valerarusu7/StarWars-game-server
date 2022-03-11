import Model.mediator.Server;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception {
		Server server = new Server(6666);
	}
}
