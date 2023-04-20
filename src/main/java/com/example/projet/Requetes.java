package com.example.projet;

import java.sql.*;

/**
 * Classe vite fait utilisant JDBC (https://jdbc.postgresql.org/)
 *
 * source principale du code : postgresqltutorial.com
 */
public class Requetes {

    // REMPLACER "projet2935" par le nom de la database contenant le schéma "Projet" (ou autre nom de schéma) sur votre ordi
    private final String url = "jdbc:postgresql://localhost:5432/Projet29325";
    private final String user = "postgres";  // METTRE LE "BON" USERNAME SI CE N'EST PAS LE MÊME
    private final String password = "sara";  // METTRE LE "VRAI" MOT DE PASSE

    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object
     * @throws SQLException
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }


    /**
     * obtient et print la requête sql passée en paramètre
     */
    public void requete(String question) {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(question)) {
            print_resultat(resultSet);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * inspiration :
     * https://stackoverflow.com/questions/24229442/print-the-data-in-resultset-along-with-column-names/28165814#28165814
     *
     * @param resultSet
     * @throws SQLException
     */
    private void print_resultat(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        for (int i=1; i<= columnsNumber; i++) {
            System.out.print(rsmd.getColumnName(i).toUpperCase());
            if (i<columnsNumber) {
                System.out.print(", ");
            } else {
                System.out.println("");
            }
        }
        while (resultSet.next()) {
            // print le titre de la colonne

            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = resultSet.getString(i);
                System.out.print(columnValue);
            }
            System.out.println("");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Requetes essai = new Requetes();

        //exemple
        System.out.println("Exemple : SELECT * FROM PAIE;");
        essai.requete("SELECT * FROM PAIE;");
        System.out.println("");

        String question1 = " with r1 as (select no_entreprise from Entreprise where nom_entreprise='ABC'), r2 as (select no_stage from Stage where modalite='Virtuel'), r3 as (select no_stage,no_stagiaire,no_entreprise from Convention where annee_courante=2020), r4 as (select no_stage,no_stagiaire from r1 natural join r3), r5 as (select no_stagiaire as no_etudiant from r2 natural join r4), r6 as (select no_etudiant,nom,prenom from Etudiant where sexe='F') select nom,prenom from r6 natural join r5";
        System.out.println("Requete 1 :");
        essai.requete(question1);
        String question2 = "with r1 as(select no_enseignant from Enseignant where nom_e='X' and annee_embauche<2000), r2 as (select nom_poste from Paie where remuneration>6000.00), r3 as (select no_stage,no_enseignant from Encadre where annee_encadrement=2022), r4 as (select no_stage from r3 natural join r1), r5 as (select no_stage,nom_poste from Stage), r6 as (select no_stage from r5 natural join r2), r7 as (select no_stage from r6 natural join r4) select count(no_stage) from r7;";
        System.out.println("");
        System.out.println("Requete 2 :");
        essai.requete(question2);
    }
}