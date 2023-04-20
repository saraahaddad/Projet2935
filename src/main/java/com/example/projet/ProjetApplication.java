package com.example.projet;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.util.Callback;

import java.sql.*;

/**
 *
 * Application qui affiche avec JavaFX le résultat d'une requête SQL
 * source : https://stackoverflow.com/questions/18941093/how-to-fill-up-a-tableview-with-database-data/22032294#22032294
 *
 * aussi : une autre partie du code est reprise du fichier Requetes.java que j'avais fait
 */

public class ProjetApplication extends Application{

    // REMPLACER "projet2935" par le nom de la database contenant le schéma "Projet" (ou autre nom de schéma) sur votre ordi
    private final String url = "jdbc:postgresql://localhost/Projet29325?currentSchema=Projet";
    private final String user = "postgres";  // METTRE LE "BON" USERNAME SI CE N'EST PAS LE MÊME
    private final String password = "sara";  // METTRE LE "VRAI" MOT DE PASSE


    String requete1 = "with r1 as (select no_entreprise from Entreprise where nom_entreprise='ABC'),r2 as (select no_stage from Stage where modalite='Virtuel'),r3 as (select no_stage,no_stagiaire,no_entreprise from Convention where annee_courante=2020),r4 as (select no_stage,no_stagiaire from r1 natural join r3),r5 as (select no_stagiaire as no_etudiant from r2 natural join r4),r6 as (select no_etudiant,nom,prenom from Etudiant where sexe='F')select nom,prenom from r6 natural join r5;";
    String question1 = "Le nom et le prénom des stagiaires de sexe féminin qui ont effectué un stage en mode virtuel débutant en 2020 avec l'entreprise ABC";

    String requete2 = "with r1 as(select no_enseignant from Enseignant where nom_e='X' and annee_embauche<2000), r2 as (select nom_poste from Paie where remuneration>6000.00),r3 as (select no_stage,no_enseignant from Encadre where annee_encadrement=2022),r4 as (select no_stage from r3 natural join r1),r5 as (select no_stage,nom_poste from Stage),r6 as (select no_stage from r5 natural join r2),r7 as (select no_stage from r6 natural join r4)select count(no_stage) from r7;";

    String question2 = "Le nombre de stages rémunérés à plus de 6000$ qui ont été encadré par l'enseignant dont le nom est X et qui a été embauché avant l'année 2000.";

    String requete3 = "with r1 as (select no_etudiant from Etudiant where programme = 'informatique' and sexe = 'F'),r2 as (select no_entreprise from Entreprise where nom_entreprise = 'Facebook'),r3 as (select annee_stage from contact join r2 on contact.no_entreprise = r2.no_entreprise join r1 on contact.no_etudiant = r1.no_etudiant),r4 as (select no_stagiaire from convention join r3 on convention.annee_courante =r3.annee_stage),r5 as (select no_enseignant from enseignant where domaine = 'informatique'),r6 as (select r4.no_stagiaire, no_enseignant from r4 join encadre on r4.no_stagiaire =encadre.no_stagiaire),r7 as (select no_stagiaire from r6 join r5 on r6.no_enseignant = r5.no_enseignant)select distinct nom, prenom from etudiant join r7 on etudiant.no_etudiant = r7.no_stagiaire;";

    String question3 = "Le nom et prénom des étudiantes qui ont contacté l'entreprise Facebook, qui ont reçu un stage la même année et qui ont été encadrées par un enseignant du DIRO";

    String requete4 = "with r1 as (select no_etudiant from Etudiant where programme = 'économie'),r2 as (select no_entreprise from Entreprise where nom_entreprise = 'Banque nationale'),r3 as (select no_stage, no_stagiaire, no_entreprise from convention),r4 as (select no_entreprise, no_stagiaire, no_stage from r3 join r1 on r3.no_stagiaire =r1.no_etudiant),r5 as (select no_stage, no_stagiaire from r2 natural join r4),r6 as (select no_enseignant, no_stagiaire, no_stage from encadre),r7 as (select distinct no_enseignant from r5 natural join r6) select count(no_enseignant) from r7;";

    String question4 = "Le nombre d'enseignants qui ont encadré des étudiants du programme d'économie pour un stage à la Banque Nationale.";

    //TABLE VIEW AND DATA
    private ObservableList<ObservableList> data;
    private TableView tableview;
    private TableView table1 = new TableView();
    private TableView table2 = new TableView();
    private TableView table3 = new TableView();
    private TableView table4 = new TableView();

    class BoutonTableHandler implements EventHandler<ActionEvent> {
        private TableView table;
        BoutonTableHandler(TableView table) {
            this.table = table;
        }
        @Override
        public void handle(ActionEvent event) {
            table.setVisible(true);
            table.toFront();
        }

    }


    /**
     * Connect to the PostgreSQL database
     * source : https://www.postgresqltutorial.com/postgresql-jdbc/connecting-to-postgresql-database/
     * @return a Connection object
     * @throws SQLException
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * obtient la requête sql passée en paramètre
     */
    public ResultSet requete(String question) {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(question)) {
            return(resultSet);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public TableView tableFromResult(ResultSet rs) {
        return null;
    }

    public void buildTable(String requete, TableView tableview) {
        data = FXCollections.observableArrayList();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(requete)) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setSortable(false);
                col.setReorderable(false);
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                tableview.getColumns().addAll(col);
            }

            /********************************
             * Data added to ObservableList *
             ********************************/
            while (rs.next()) {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));//Iterate Column
                }
                data.add(row);

            }
            //FINALLY ADDED TO TableView
            tableview.setItems(data);

            // https://stackoverflow.com/questions/12933918/tableview-has-more-columns-than-specified/12950052#12950052
            tableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tableview.setSelectionModel(null);  // https://stackoverflow.com/a/43392253
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane bp = new BorderPane();
        // http://www.java2s.com/Code/Java/JavaFX/SetPaddingforBorderPane.htm
        bp.setPadding(new Insets(10, 20, 10, 20));

        // 1 - top : titre
        final Label labelTitre = new Label("Requêtes");
        labelTitre.setFont(new Font("Arial", 20));
        labelTitre.setPadding(new Insets(5, 5, 5, 5));
        bp.setTop(labelTitre);


        // 2- center : stackpane avec les tableview empilées
        StackPane stackPane = new StackPane();
        stackPane.setPadding(new Insets(15, 10, 30, 10));
        // requête 1
        buildTable(requete1, table1);
        table1.setVisible(false);;
        // requête 2
        buildTable(requete2, table2);
        //stackPane.getChildren().add(table2);
        table2.setVisible(false);
        // requête 3
        buildTable(requete3, table3);
        table3.setVisible(false);
        // requête 4
        buildTable(requete4, table4);
        table4.setVisible(false);

        Text texte = new Text("Veuillez choisir une requête.");
        stackPane.setAlignment(texte, Pos.TOP_CENTER);
        StackPane.setMargin(texte, new Insets(8,8,8,8));
        stackPane.getChildren().addAll(texte, table1, table2, table3, table4);
        texte.toFront();
        texte.managedProperty().bind(texte.visibleProperty());
        bp.setCenter(stackPane);


        // 3- left : boutons
        VBox vb = new VBox(10);
        vb.setPadding(new Insets(15, 10, 5, 5));
        // Label creation
        Label lb = new Label("Choix de la requête");
        //boutons
        Button bouton1 = new Button("Question 1");
        bouton1.setOnAction(new BoutonTableHandler(table1));
        Button bouton2 = new Button("Question 2");
        bouton2.setOnAction(new BoutonTableHandler(table2));
        Button bouton3 = new Button("Question 3");
        bouton3.setOnAction(new BoutonTableHandler(table3));
        Button bouton4 = new Button("Question 4");
        bouton4.setOnAction(new BoutonTableHandler(table4));
        vb.getChildren().addAll(lb, bouton1, bouton2, bouton3, bouton4);
        bp.setLeft(vb);

        // 4- bottom : noms
        final Label labelNoms = new Label("Par Cloé Chandonnet, Aurélie Dansereau, Marylou Fauchard et Sara Haddad.\nProjet IFT2935 - Hiver 2023");
        labelNoms.setFont(new Font("Arial", 12));
        labelNoms.setPadding(new Insets(5, 5, 5, 5));
        bp.setBottom(labelNoms);


        Scene scene = new Scene(bp);

        // ajout de css pour que les lignes supplémentaires soient en blanc (on ne les verra pas)
        // source pour ajouter du css : https://stackoverflow.com/a/15759024 et https://stackoverflow.com/a/23325353
        // source du css : http://fxexperience.com/2011/11/alternate-row-highlighting-in-empty-tableview-and-listview-rows/
        scene.getStylesheets().add("table.css");

        // ajout d'une icone à l'application (j'ai juste pris le logo de postgresql)
        // https://stackoverflow.com/questions/10121991/javafx-application-icon/10122335#10122335
        //stage.getIcons().add(new Image(getClass().getResourceAsStream("logo-postgresql.png")));

        stage.setTitle("Application");
        stage.setScene(scene);
        stage.show();
    }
}