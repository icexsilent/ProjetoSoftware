package com.redemaissocial;

import static spark.Spark.*;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

public class Main {

    private static final String URL = "jdbc:h2:./data/redemaisdb";
    private static final ThymeleafTemplateEngine engine = new ThymeleafTemplateEngine();

    public static void main(String[] args) {
        port(8080);
        staticFiles.location("/static");
        criarBancoSeNaoExistir();

        // Tela Inicial (Dashboard) - acesso livre
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", req.session().attribute("user"));
            try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
                model.put("candidatos", buscarCandidatos(c));
                model.put("doacoes", buscarDoacoes(c));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ModelAndView(model, "dashboard.html");  // Mudança: usa dashboard.html
        }, engine);

        // Campanhas/Eventos - acesso livre (lista candidatos + doações como "campanhas")
        get("/campanhas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", req.session().attribute("user"));
            try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
                model.put("candidatos", buscarCandidatos(c));  // Como "campanhas"
                model.put("doacoes", buscarDoacoes(c));         // Como "eventos"
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ModelAndView(model, "campanhas.html");
        }, engine);

        // Perfil - só para logados
        get("/perfil", (req, res) -> {
            String user = req.session().attribute("user");
            if (user == null) {
                res.redirect("/login?erro=Acesse com login");
                return null;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", user);
            // Futuro: busque dados do perfil do DB
            return new ModelAndView(model, "perfil.html");
        }, engine);

        // Login - público
        get("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("erro", req.queryParams("erro"));
            return new ModelAndView(model, "login.html");
        }, engine);

        post("/login", (req, res) -> processarLogin(req, res));

        // Logout
        get("/logout", (req, res) -> {
            req.session().invalidate();
            res.redirect("/");
            return null;
        });

        // Cadastros - pós no dashboard
        post("/cadastrar-candidato", (req, res) -> {
            cadastrarCandidato(req);
            res.redirect("/");
            return null;
        });

        post("/nova-doacao", (req, res) -> {
            registrarDoacao(req);
            res.redirect("/");
            return null;
        });

        System.out.println("REDE MAIS SOCIAL rodando → http://localhost:8080");
        System.out.println("Login padrão: admin | senha: 123456");
    }

    // ====================== LOGIN ======================
    private static Object processarLogin(spark.Request req, spark.Response res) {
        String login = req.queryParams("login");
        String senha = req.queryParams("senha");

        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            PreparedStatement ps = c.prepareStatement("SELECT senha FROM USUARIOS WHERE login = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && BCrypt.checkpw(senha, rs.getString("senha"))) {
                req.session(true).attribute("user", login);
                res.redirect("/");  // Redireciona para Dashboard
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        res.redirect("/login?erro=Login ou senha incorretos");
        return null;
    }

    // ====================== BANCO ======================
    private static void criarBancoSeNaoExistir() {
        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            Statement s = c.createStatement();

            s.execute("CREATE TABLE IF NOT EXISTS USUARIOS (login VARCHAR(50) PRIMARY KEY, senha VARCHAR(100))");
            s.execute("CREATE TABLE IF NOT EXISTS CANDIDATOS (id VARCHAR(20) PRIMARY KEY, nome VARCHAR(100), email VARCHAR(100), cpf VARCHAR(11))");
            s.execute("CREATE TABLE IF NOT EXISTS DOACOES (\n" +
                      "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                      "    doador_id VARCHAR(20),\n" +
                      "    valor DECIMAL(10,2),\n" +
                      "    tipo VARCHAR(20),\n" +
                      "    descricao VARCHAR(255),\n" +
                      "    status VARCHAR(20) DEFAULT 'PENDENTE'\n" +
                      ")");

            // Usuário padrão → admin / 123456
            String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
            s.executeUpdate("MERGE INTO USUARIOS KEY(login) VALUES ('admin', '" + hash + "')");

            System.out.println("Banco de dados criado/atualizado e usuário admin inserido.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== CADASTROS ======================
    private static void cadastrarCandidato(spark.Request req) {
        String id = req.queryParams("id");
        if (id == null || id.trim().isEmpty()) return;

        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO CANDIDATOS VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE nome=VALUES(nome), email=VALUES(email), cpf=VALUES(cpf)");
            ps.setString(1, id.trim());
            ps.setString(2, req.queryParams("nome"));
            ps.setString(3, req.queryParams("email"));
            ps.setString(4, req.queryParams("cpf"));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registrarDoacao(spark.Request req) {
        String doadorId = req.queryParams("doadorId");
        String valorStr = req.queryParams("valor");
        if (doadorId == null || doadorId.trim().isEmpty() || valorStr == null || valorStr.trim().isEmpty()) return;

        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO DOACOES (doador_id, valor, tipo, descricao, status) VALUES (?, ?, 'MONETARIA', ?, 'CONFIRMADA')");
            ps.setString(1, doadorId.trim());
            ps.setDouble(2, Double.parseDouble(valorStr));
            ps.setString(3, req.queryParams("descricao"));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== BUSCAS ======================
    private static List<Map<String, Object>> buscarCandidatos(Connection c) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        ResultSet rs = c.createStatement().executeQuery("SELECT * FROM CANDIDATOS");
        while (rs.next()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rs.getString("id"));
            m.put("nome", rs.getString("nome"));
            m.put("email", rs.getString("email"));
            m.put("cpf", rs.getString("cpf"));
            lista.add(m);
        }
        return lista;
    }

    private static List<Map<String, Object>> buscarDoacoes(Connection c) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        ResultSet rs = c.createStatement().executeQuery("SELECT * FROM DOACOES");
        while (rs.next()) {
            Map<String, Object> m = new HashMap<>();
            m.put("valor", rs.getDouble("valor"));
            m.put("descricao", rs.getString("descricao"));
            m.put("doadorId", rs.getString("doador_id"));
            m.put("status", rs.getString("status"));
            lista.add(m);
        }
        return lista;
    }
}