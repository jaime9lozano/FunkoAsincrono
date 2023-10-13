package jaime.Servicios;

import jaime.Exception.FunkoNoEncontrado;
import jaime.modelos.Funko;
import jaime.repositorios.FunkoRepositorio;
import jaime.repositorios.FunkoRepositorioImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class FunkoServicioImp implements FunkoServicio{
    private static final int CACHE_SIZE = 10; // Tamaño de la cache
    // Singleton
    private static FunkoServicioImp instance;
    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkoServicioImp.class);
    private final FunkoRepositorio funkoRepositorio;
    private FunkoServicioImp(FunkoRepositorio funkoRepository) {
        this.funkoRepositorio = funkoRepository;
        // Inicializamos la cache con el tamaño y la política de borrado de la misma
        // borramos el más antiguo cuando llegamos al tamaño máximo
        this.cache = new FunkoCacheImp(CACHE_SIZE);
    }


    public static FunkoServicioImp getInstance(FunkoRepositorio funkoRepository) {
        if (instance == null) {
            instance = new FunkoServicioImp(funkoRepository);
        }
        return instance;
    }

    @Override
    public List<Funko> findAll() throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Obteniendo todos los funkos");
        return funkoRepositorio.findAll().get();
    }

    @Override
    public List<Funko> findAllByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Obteniendo todos los funkos ordenados por nombre");
        return funkoRepositorio.findByNombre(nombre).get();
    }

    @Override
    public Optional<Funko> findById(long id) throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Obteniendo funko por id");
        // Buscamos en la cache
        Funko alumno = cache.get(id);
        if (alumno != null) {
            logger.debug("Funko encontrado en cache");
            return Optional.of(alumno);
        } else {
            // Buscamos en la base de datos
            logger.debug("Funko no encontrado en cache, buscando en base de datos");
            return funkoRepositorio.findById(id).get();
        }
    }

    @Override
    public Funko save(Funko funko) throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Guardando funko");
        // Guardamos en la base de datos
        funko = funkoRepositorio.save(funko).get();
        // Guardamos en la cache
        cache.put(funko.myID(), funko);
        return funko;
    }

    @Override
    public Funko update(Funko funko) throws SQLException, FunkoNoEncontrado, ExecutionException, InterruptedException {
        logger.debug("Actualizando funko");
        // Actualizamos en la base de datos
        funko = funkoRepositorio.update(funko).get();
        // Actualizamos en la cache
        cache.put(funko.myID(), funko);
        return funko;
    }

    @Override
    public boolean deleteById(long id) throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Borrando funko");
        // Borramos en la base de datos
        var deleted = funkoRepositorio.deleteById(id).get();
        // Borramos en la cache si existe en ella
        if (deleted) {
            cache.remove(id);
        }
        return deleted;
    }

    @Override
    public void deleteAll() throws SQLException, ExecutionException, InterruptedException {
        logger.debug("Borrando todos los funkos");
        // Borramos en la base de datos
        funkoRepositorio.deleteAll().get();
        // Borramos en la cache
        cache.clear();
    }
    @Override
    public Optional<Funko> funkoCaro() throws ExecutionException, InterruptedException {
        logger.debug("Buscando el funko mas caro");
        return funkoRepositorio.FunkoCaro().get();
    }

    @Override
    public Double mediaFunko() throws ExecutionException, InterruptedException {
        logger.debug("Buscando la media de los funkos");
        return funkoRepositorio.mediaFunko().get();
    }

    @Override
    public List<Funko> funko2023() throws ExecutionException, InterruptedException {
        logger.debug("Buscando funkos lanzados en 2023");
        return funkoRepositorio.funko2023().get();
    }
}
