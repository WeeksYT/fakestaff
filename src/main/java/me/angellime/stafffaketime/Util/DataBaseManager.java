package me.angellime.stafffaketime.Util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseManager {


    final HikariDataSource dataSource;

    String url = "jdbc:mysql://199.83.103.236:3306/stafffaketime";

    public DataBaseManager() {
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername("angellime");
        config.setPassword("cY7kE9nH4w");
        config.setMaximumPoolSize(80);
        dataSource = new HikariDataSource(config);

    }



    public void addReason(String nickname, String banet, String type, String reason) {

        try (Connection connection = dataSource.getConnection()){
            String insertSql = "INSERT INTO punishments (type, evidence, issued_by, player_name) VALUES (?, ?, ?, ?)";
            String selectSql = "SELECT coefficient FROM users WHERE username = ?";
            String updateSql = "UPDATE users SET coefficient = coefficient + 0.03, rubles = rubles + ?, tops = tops + 1 WHERE username = ?";

            try {
                connection.setAutoCommit(false); // Устанавливаем ручное управление транзакциями
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                     PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                     PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

                    // Вставка данных в таблицу punishments
                    insertStatement.setString(1, type);
                    insertStatement.setString(2, reason);
                    insertStatement.setString(3, nickname);
                    insertStatement.setString(4, banet);
                    insertStatement.executeUpdate();

                    // Получение коэффициента пользователя
                    selectStatement.setString(1, nickname);
                    try (ResultSet resultSet = selectStatement.executeQuery()) {
                        if (resultSet.next()) {
                            float coef = resultSet.getFloat("coefficient");
                            float dur = 1 * coef;

                            // Обновление данных пользователя
                            updateStatement.setFloat(1, dur);
                            updateStatement.setString(2, nickname);
                            updateStatement.executeUpdate();
                        }
                    }

                    connection.commit(); // Фиксация транзакции
                } catch (SQLException e) {
                    connection.rollback(); // Откат изменений в случае ошибки
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.setAutoCommit(true); // Возвращаем автоматическое управление транзакциями
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }







}
