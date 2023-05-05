package ohm.softa.a07.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import ohm.softa.a07.api.OpenMensaAPI;
import ohm.softa.a07.model.Meal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {

	// use annotation to tie to component in XML
	@FXML
	private Button btnRefresh;

	@FXML
	private Button btnClose;

	@FXML
	private CheckBox chkVegetarian;

	@FXML
	private ListView<String> mealsList;

	private final OpenMensaAPI mensaAPI;

	public MainController() {
		var retrofit = new Retrofit.Builder()
			.baseUrl("https://openmensa.org/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();

		mensaAPI = retrofit.create(OpenMensaAPI.class);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// set the event handler (callback)
		btnRefresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// create a new (observable) list and tie it to the view
				reloadMeals(false);
			}
		});

		btnClose.setOnAction(e -> {
			Platform.exit();
			System.exit(0);
		});

		chkVegetarian.selectedProperty().addListener(e -> reloadMeals(chkVegetarian.isSelected()));
	}

	private void reloadMeals(boolean onlyVegetarian) {
		mensaAPI.getMeals(getDateFormatToday()).enqueue(new Callback<>() {
			@Override
			public void onResponse(Call<List<Meal>> call, Response<List<Meal>> response) {
				if (!response.isSuccessful()) {
					throw new RuntimeException("Failed to connect to openmensa API. Received response code " + response.code());
				}

				mealsList.setItems(FXCollections.observableArrayList(
					response.body()
						.stream()
						.filter(meal -> !onlyVegetarian || meal.isVegetarian())
						.map(Meal::getName)
						.collect(Collectors.toList())));
			}

			@Override
			public void onFailure(Call<List<Meal>> call, Throwable t) {
				mealsList.setItems(FXCollections.emptyObservableList());
				throw new RuntimeException("Call to openmensa failed", t);
			}
		});
	}

	private String getDateFormatToday() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
}
