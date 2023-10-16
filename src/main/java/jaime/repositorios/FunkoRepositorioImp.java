package jaime.repositorios;

import jaime.Exception.FunkoNoAlmacenado;
import jaime.Exception.FunkoNoEncontrado;
import jaime.Servicios.DatabaseManager;
import jaime.modelos.Funko;
import jaime.modelos.Tipo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

public class FunkoRepositorioImp implements FunkoRepositorio{
    private static FunkoRepositorioImp instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositorioImp.class);
    // Base de datos
    private final DatabaseManager db;
    private FunkoRepositorioImp(DatabaseManager db) {
        this.db = db;
    }
    public static FunkoRepositorioImp getInstance(DatabaseManager db) {
        if (instance == null) {
            instance = new FunkoRepositorioImp(db);
        }
        return instance;
    }

    @Override
    public CompletableFuture<Funko> save(Funko funko) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            String query = "INSERT INTO FUNKOS (cod,MyID,nombre,modelo,precio,fecha_lanzamiento,updated_at,created_at) VALUES (?, ?, ?, ?, ?,?,?,?)";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
            ) {
                logger.debug("Guardando el funko: " + funko);
                stmt.setObject(1, funko.cod());
                stmt.setLong(2, funko.myID());
                stmt.setString(3, funko.nombre());
                stmt.setString(4, String.valueOf(funko.tipo()));
                stmt.setDouble(5, funko.precio());
                stmt.setObject(6,funko.fecha_cre());
                stmt.setObject(7,LocalDateTime.now());
                stmt.setObject(8,funko.fecha_cre());
                var res = stmt.executeUpdate();
                if (res > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    rs.close();
                } else {
                    logger.error("Funko no guardado");
                    throw new FunkoNoAlmacenado("Funko no guardado con id: " + funko.myID());
                }
            } catch (SQLException | FunkoNoAlmacenado e) {
                logger.error("Error al guardar el funko", e);
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    @Override
    public CompletableFuture<Funko> update(Funko funko) throws SQLException, FunkoNoEncontrado {
        return CompletableFuture.supplyAsync(() -> {
            String query = "UPDATE FUNKOS SET nombre = ?, modelo = ?, precio = ?,updated_at = ? WHERE MyID = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Actualizando el funko: " + funko);
                stmt.setString(1, funko.nombre());
                stmt.setString(2, String.valueOf(funko.tipo()));
                stmt.setDouble(3, funko.precio());
                stmt.setObject(4, LocalDateTime.now());
                stmt.setDouble(5,funko.myID());
                var res = stmt.executeUpdate();
                if (res > 0) {
                    logger.debug("Funko actualizado");
                } else {
                    logger.error("Funko no actualizado al no encontrarse en la base de datos con id: " + funko.myID());
                    throw new FunkoNoEncontrado("Funko no encontrado con id: " + funko.myID());
                }
            } catch (SQLException | FunkoNoEncontrado e) {
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    @Override
    public CompletableFuture<Optional<Funko>> findById(Long id) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Funko> funko = Optional.empty();
            String query = "SELECT * FROM FUNKOS WHERE id =?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                stmt.setLong(1, id);
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    funko = Optional.of(Funko.builder()
                            .cod(rs.getObject("UUID", UUID.class))
                            .nombre(rs.getString("nombre"))
                            .tipo(Tipo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fecha_cre(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .myID(rs.getLong("MyID"))
                            .build()
                    );
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funko por id", e);
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    @Override
    public CompletableFuture<List<Funko>> findAll() throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            List<Funko> lista = new ArrayList<>();
            String query = "SELECT * FROM FUNKOS";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Obteniendo todos los funkos");
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    Funko funko = Funko.builder()
                            .cod(rs.getObject("UUID", UUID.class))
                            .nombre(rs.getString("nombre"))
                            .tipo(Tipo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fecha_cre(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .myID(rs.getLong("MyID"))
                            .build();
                    lista.add(funko);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar todos los funkos", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteById(Long id) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            String query = "DELETE FROM FUNKOS WHERE id = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Borrando el funko con id: " + id);
                stmt.setLong(1, id);
                var res = stmt.executeUpdate();
                stmt.close();
                //db.closeConnection();
                return res > 0;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteAll() throws SQLException {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM FUNKOS";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                stmt.executeUpdate();
                stmt.close();
                // db.closeConnection();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<Funko>> findByNombre(String nombre) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            var lista = new ArrayList<Funko>();
            String query = "SELECT * FROM FUNKOS WHERE nombre LIKE ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Obteniendo todos los funkos por nombre que contenga: " + nombre);
                // Vamos a usar Like para buscar por nombre
                stmt.setString(1, "%" + nombre + "%");
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    // Creamos un alumno
                    Funko funko = Funko.builder()
                            .cod(rs.getObject("UUID", UUID.class))
                            .nombre(rs.getString("nombre"))
                            .tipo(Tipo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fecha_cre(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .myID(rs.getLong("MyID"))
                            .build();
                    // Lo añadimos a la lista
                    lista.add(funko);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funkos por nombre", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }

    @Override
    public CompletableFuture<Optional<Funko>> FunkoCaro() {
        logger.debug("Buscando el funko mas caro");
        return CompletableFuture.supplyAsync(() -> {
            Optional<Funko> funko = Optional.empty();
            String query = "SELECT * FROM FUNKOS ORDER BY precio DESC LIMIT 1";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    funko = Optional.of(Funko.builder()
                            .cod(rs.getObject("UUID", UUID.class))
                            .nombre(rs.getString("nombre"))
                            .tipo(Tipo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fecha_cre(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .myID(rs.getLong("MyID"))
                            .build()
                    );
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funko mas caro", e);
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    @Override
    public CompletableFuture<Double> mediaFunko() {
        final Double[] mediaPrecios = {null};
        logger.debug("Buscando la media de los funkos");
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT AVG(precio) AS media_precios FROM FUNKOS";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    mediaPrecios[0] =rs.getDouble("media_precios");
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funko mas caro", e);
                throw new CompletionException(e);
            }
            return mediaPrecios[0];
        });
    }

    @Override
    public CompletableFuture<List<Funko>> funko2023() {
        return CompletableFuture.supplyAsync(() -> {
            var lista = new ArrayList<Funko>();
            String query = "SELECT * FROM FUNKOS WHERE YEAR(fecha_lanzamiento) = 2023";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)
            ) {
                logger.debug("Obteniendo todos los funkos lanzados en 2023");
                // Vamos a usar Like para buscar por nombre

                var rs = stmt.executeQuery();
                while (rs.next()) {
                    // Creamos un alumno
                    Funko funko = Funko.builder()
                            .cod(rs.getObject("UUID", UUID.class))
                            .nombre(rs.getString("nombre"))
                            .tipo(Tipo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fecha_cre(rs.getObject("fecha_lanzamiento", LocalDate.class))
                            .myID(rs.getLong("MyID"))
                            .build();
                    // Lo añadimos a la lista
                    lista.add(funko);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar funkos por fecha", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }
}
