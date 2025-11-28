package com.redemaissocial;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class Main {

    private static final String URL = "jdbc:h2:./data/redemaisdb";
    private static final ThymeleafTemplateEngine engine = new ThymeleafTemplateEngine();

    public static void main(String[] args) {
        port(8080);
        staticFiles.location("/static");

        try {
            criarBancoSeNaoExistir();
        } catch (Exception e) {
            e.printStackTrace();
        }

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", req.session().attribute("user"));
            try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
                model.put("candidatos", buscarCandidatos(c));
                model.put("doacoes", buscarDoacoes(c));
            } catch (Exception e) {
                model.put("candidatos", new ArrayList<>());
                model.put("doacoes", new ArrayList<>());
            }
            return new ModelAndView(model, "index.html");
        }, engine);

        get("/login", (req, res) -> new ModelAndView(new HashMap<>(), "login.html"), engine);
        post("/login", Main::processarLogin);

        get("/cadastro", (req, res) -> new ModelAndView(new HashMap<>(), "cadastro.html"), engine);
        post("/cadastrar-usuario", Main::cadastrarNovoUsuario);

        get("/logout", (req, res) -> { req.session().invalidate(); res.redirect("/"); return null; });

        get("/perfil", (req, res) -> {
            String user = req.session().attribute("user");
            if (user == null) { res.redirect("/login"); return null; }
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", user);
            try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
                PreparedStatement ps = c.prepareStatement("SELECT nome, email FROM USUARIOS WHERE login = ?");
                ps.setString(1, user);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    model.put("nome", rs.getString("nome"));
                    model.put("email", rs.getString("email"));
                }
            }
            return new ModelAndView(model, "perfil.html");
        }, engine);

        get("/campanhas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", req.session().attribute("user"));
            try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
                model.put("campanhas", buscarCampanhas(c));
            } catch (Exception e) {
                model.put("campanhas", new ArrayList<>());
            }
            return new ModelAndView(model, "campanhas.html");
        }, engine);

        post("/nova-campanha", (req, res) -> {
            if (req.session().attribute("user") == null) { res.redirect("/login"); return null; }
            String titulo = req.queryParams("titulo");
            String descricao = req.queryParams("descricao");
            if (titulo != null && !titulo.trim().isEmpty()) {
                try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
                    PreparedStatement ps = c.prepareStatement("INSERT INTO CAMPANHAS (titulo, descricao, autor) VALUES (?, ?, ?)");
                    ps.setString(1, titulo);
                    ps.setString(2, descricao);
                    ps.setString(3, req.session().attribute("user").toString());
                    ps.executeUpdate();
                }
            }
            res.redirect("/campanhas");
            return null;
        });

        post("/cadastrar-candidato", (req, res) -> { cadastrarCandidato(req); res.redirect("/"); return null; });
        post("/nova-doacao", (req, res) -> { registrarDoacao(req); res.redirect("/"); return null; });

        System.out.println("REDE MAIS SOCIAL RODANDO → http://localhost:8080");
        System.out.println("Login padrão: admin / 123456");
    }

    private static Object processarLogin(Request req, Response res) {
        String login = req.queryParams("login");
        String senha = req.queryParams("senha");
        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            PreparedStatement ps = c.prepareStatement("SELECT senha FROM USUARIOS WHERE login = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && BCrypt.checkpw(senha, rs.getString("senha"))) {
                req.session(true).attribute("user", login);
                res.redirect("/");
                return null;
            }
        } catch (Exception e) { e.printStackTrace(); }
        res.redirect("/login?erro=Login ou senha incorretos");
        return null;
    }

    private static Object cadastrarNovoUsuario(Request req, Response res) {
        String login = req.queryParams("login");
        String senha = req.queryParams("senha");
        String nome = req.queryParams("nome");
        String email = req.queryParams("email");
        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            PreparedStatement ps = c.prepareStatement("SELECT 1 FROM USUARIOS WHERE login = ?");
            ps.setString(1, login);
            if (ps.executeQuery().next()) {
                req.session().attribute("erro", "Login já existe");
                res.redirect("/cadastro");
                return null;
            }
            String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
            ps = c.prepareStatement("INSERT INTO USUARIOS (login, senha, nome, email) VALUES (?, ?, ?, ?)");
            ps.setString(1, login);
            ps.setString(2, hash);
            ps.setString(3, nome);
            ps.setString(4, email);
            ps.executeUpdate();
            res.redirect("/login?sucesso=Conta criada!");
        } catch (Exception e) {
            e.printStackTrace();
            req.session().attribute("erro", "Erro ao criar conta");
            res.redirect("/cadastro");
        }
        return null;
    }

    private static void criarBancoSeNaoExistir() throws Exception {
        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            Statement s = c.createStatement();
            s.execute("CREATE TABLE IF NOT EXISTS USUARIOS (login VARCHAR(50) PRIMARY KEY, senha VARCHAR(100), nome VARCHAR(100), email VARCHAR(100))");
            s.execute("CREATE TABLE IF NOT EXISTS CANDIDATOS (id VARCHAR(20) PRIMARY KEY, nome VARCHAR(100), email VARCHAR(100), cpf VARCHAR(11))");
            s.execute("CREATE TABLE IF NOT EXISTS DOACOES (id INT AUTO_INCREMENT PRIMARY KEY, doador_id VARCHAR(50), valor DECIMAL(10,2), descricao VARCHAR(255), status VARCHAR(20))");
            s.execute("CREATE TABLE IF NOT EXISTS CAMPANHAS (id INT AUTO_INCREMENT PRIMARY KEY, titulo VARCHAR(200), descricao TEXT, autor VARCHAR(50), data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
            s.executeUpdate("MERGE INTO USUARIOS KEY(login) VALUES ('admin', '" + hash + "', 'Administrador', 'admin@redemais.com')");
        }
    }

    private static void cadastrarCandidato(Request req) {
        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) + 1 AS novo FROM CANDIDATOS");
            rs.next();
            String id = "V" + String.format("%03d", rs.getInt("novo"));
            PreparedStatement ps = c.prepareStatement("INSERT INTO CANDIDATOS VALUES (?, ?, ?, ?)");
            ps.setString(1, id);
            ps.setString(2, req.queryParams("nome"));
            ps.setString(3, req.queryParams("email"));
            ps.setString(4, req.queryParams("cpf") != null ? req.queryParams("cpf").replaceAll("\\D", "") : "");
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void registrarDoacao(Request req) {
        try (Connection c = DriverManager.getConnection(URL, "sa", "")) {
            String doador = (String) req.session().attribute("user");
            if (doador == null) {
                doador = req.queryParams("doadorNome");
                if (doador == null || doador.trim().isEmpty()) doador = "Anônimo";
            }
            PreparedStatement ps = c.prepareStatement("INSERT INTO DOACOES (doador_id, valor, tipo, descricao, status) VALUES (?, ?, 'MONETARIA', ?, 'CONFIRMADA')");
            ps.setString(1, doador);
            ps.setDouble(2, Double.parseDouble(req.queryParams("valor")));
            ps.setString(3, req.queryParams("descricao"));
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // MÉTODOS DE BUSCA (iguais aos anteriores)
    private static List<Map<String, Object>> buscarCandidatos(Connection c) throws SQLException {
        List<Map<String, Object>> l = new ArrayList<>();
        ResultSet rs = c.createStatement().executeQuery("SELECT * FROM CANDIDATOS");
        while (rs.next()) { Map<String, Object> m = new HashMap<>(); m.put("id", rs.getString("id")); m.put("nome", rs.getString("nome")); m.put("email", rs.getString("email")); m.put("cpf", rs.getString("cpf")); l.add(m); }
        return l;
    }

    private static List<Map<String, Object>> buscarDoacoes(Connection c) throws SQLException {
        List<Map<String, Object>> l = new ArrayList<>();
        ResultSet rs = c.createStatement().executeQuery("SELECT * FROM DOACOES ORDER BY id DESC");
        while (rs.next()) { Map<String, Object> m = new HashMap<>(); m.put("valor", rs.getDouble("valor")); m.put("descricao", rs.getString("descricao")); m.put("doadorId", rs.getString("doador_id")); l.add(m); }
        return l;
    }

    private static List<Map<String, Object>> buscarCampanhas(Connection c) throws SQLException {
        List<Map<String, Object>> l = new ArrayList<>();
        ResultSet rs = c.createStatement().executeQuery("SELECT c.*, u.nome AS nome_autor FROM CAMPANHAS c LEFT JOIN USUARIOS u ON c.autor = u.login ORDER BY c.id DESC");
        while (rs.next()) { Map<String, Object> m = new HashMap<>(); m.put("id", rs.getInt("id")); m.put("titulo", rs.getString("titulo")); m.put("descricao", rs.getString("descricao")); m.put("autor", rs.getString("nome_autor")); m.put("data", rs.getTimestamp("data_criacao")); l.add(m); }
        return l;
    }
}