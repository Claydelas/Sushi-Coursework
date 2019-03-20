package comp1206.sushi.server.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.NumberValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import comp1206.sushi.common.Dish;
import comp1206.sushi.server.ServerInterface;
import comp1206.sushi.server.components.NumericTableCell;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;

public class DishesController extends MainViewController {

    protected static Dish currentlySelectedDish;
    @FXML
    private AnchorPane recipe;
    @FXML
    private AnchorPane newDishView;
    @FXML
    private TableView<Dish> dishesTable;
    @FXML
    private TableColumn<Dish, String> name;
    @FXML
    private TableColumn<Dish, String> description;
    @FXML
    private TableColumn<Dish, Number> price;
    @FXML
    private TableColumn<Dish, Number> restockThreshold;
    @FXML
    private TableColumn<Dish, Number> restockAmount;
    @FXML
    private TableColumn<Dish, Number> stock;
    @FXML
    private JFXButton editRecipeButton;
    @FXML
    private RecipeController recipeController;
    @FXML
    private JFXTextField nameF;
    @FXML
    private JFXTextField descriptionF;
    @FXML
    private JFXTextField priceF;
    @FXML
    private JFXTextField restockValF;
    @FXML
    private JFXTextField restockAtF;
    @FXML
    private JFXButton addNewDishButton;
    @FXML
    private JFXButton newDishButton;
    @FXML
    private JFXButton deleteDishButton;


    @FXML
    public void initialize() {

        //---------------------Name Column---------------------------------
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        //off-spec, modifies directly due to lack of api implementation
        name.setCellFactory(TextFieldTableCell.forTableColumn());
        name.setOnEditCommit(e -> dishesTable.getSelectionModel().getSelectedItem().setName(e.getNewValue()));

        //---------------------Description Column---------------------------------
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        //off-spec, modifies directly due to lack of api implementation
        description.setCellFactory(TextFieldTableCell.forTableColumn());
        description.setOnEditCommit(e -> dishesTable.getSelectionModel().getSelectedItem().setDescription(e.getNewValue()));

        //---------------------Price Column---------------------------------
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        //off-spec, modifies directly due to lack of api implementation
        price.setCellFactory(column -> new NumericTableCell<>());
        price.setOnEditCommit(e -> dishesTable.getSelectionModel().getSelectedItem().setPrice(e.getNewValue()));

        //---------------------Restock Threshold Column---------------------------------
        restockThreshold.setCellValueFactory(new PropertyValueFactory<>("restockThreshold"));
        restockThreshold.setCellFactory(column -> new NumericTableCell<>(0, 0));
        restockThreshold.setOnEditCommit(e -> dishesTable.getSelectionModel().getSelectedItem().setRestockThreshold(e.getNewValue()));

        //---------------------Restock Amount Column---------------------------------
        restockAmount.setCellValueFactory(new PropertyValueFactory<>("restockAmount"));
        restockAmount.setCellFactory(c -> new NumericTableCell<>(0, 0));
        restockAmount.setOnEditCommit(e -> dishesTable.getSelectionModel().getSelectedItem().setRestockAmount(e.getNewValue()));

        //---------------------Stock Column---------------------------------
        stock.setCellValueFactory(stock -> new SimpleIntegerProperty(server.getDishStockLevels().get(stock.getValue()).intValue()));

        //wraps the dishes list in a observable one, so table listens for updates on the model
        ObservableList<Dish> dishData = FXCollections.observableList(server.getDishes());
        //sets the table's contents to the observable list
        dishesTable.setItems(dishData);

        //Selecting in the table triggers an update to the recipe editing view
        dishesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentlySelectedDish = dishesTable.getSelectionModel().getSelectedItem();
                recipeController.initIngredientList();
            }
        });

        //Opens recipe editing view and updates the list of a dish's ingredients
        editRecipeButton.setOnAction(e -> {
            if (!server.getDishes().isEmpty()) {
                //if no dish was selected in the table, pick the first one as a default
                if (currentlySelectedDish == null) {
                    //currentlySelectedDish = server.getDishes().get(0);
                    dishesTable.getSelectionModel().selectFirst();
                }
                //initialises ingredients in dish list in the popover view
                recipeController.initIngredientList();
                newDishView.setVisible(false);
                recipe.setVisible(true);
                //cancels any cell edit in progress
                dishesTable.edit(-1, null);
            }
        });

        newDishButton.setOnAction(e -> {
            recipe.setVisible(false);
            newDishView.setVisible(true);
            //cancels any cell edit in progress
            dishesTable.edit(-1, null);
        });

        deleteDishButton.setOnAction(actionEvent -> {
            Dish tempSelect = dishesTable.getSelectionModel().getSelectedItem();
            if (tempSelect != null) {
                try {
                    server.removeDish(tempSelect);
                    dishesTable.getSelectionModel().clearSelection();
                    currentlySelectedDish = null;
                    recipe.setVisible(false);
                    dishesTable.refresh();
                } catch (ServerInterface.UnableToDeleteException e) {
                    showToastNotification("Unable to delete selected dish!");
                }
            }
        });

        addNewDishButton.setOnAction(e -> {
            //if all fields are correctly filled, adds the dish to the server
            if (nameF.validate() && restockValF.validate() && priceF.validate()
                    && descriptionF.validate() && restockAtF.validate()) {

                //adds the new dish to the data
                server.addDish(nameF.getText(), descriptionF.getText(), Float.valueOf(priceF.getText()),
                        Float.valueOf(restockAtF.getText()), Float.valueOf(restockValF.getText()));
                dishesTable.refresh();
                newDishView.setVisible(false);
                showToastNotification("Dish added successfully!");

                //prints the data in the server for testing
                System.out.println("Dishes currently in the server: "
                        + server.getDishes() + "\nNewly added: "
                        + server.getDishes().get(server.getDishes().size() - 1));
            }
        });
        RequiredFieldValidator evalInput = new RequiredFieldValidator();
        NumberValidator evalNum = new NumberValidator();

        nameF.getValidators().add(evalInput);
        descriptionF.getValidators().add(evalInput);
        priceF.getValidators().addAll(evalInput, evalNum);
        restockValF.getValidators().addAll(evalInput, evalNum);
        restockAtF.getValidators().addAll(evalInput, evalNum);
    }

}
