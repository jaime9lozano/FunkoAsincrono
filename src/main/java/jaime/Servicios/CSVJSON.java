package jaime.Servicios;

import jaime.modelos.Funko;
import jaime.modelos.MyIDStore;
import jaime.modelos.Tipo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CSVJSON {
    MyIDStore m=MyIDStore.getInstance();
    private static CSVJSON instance;
    private CSVJSON(){
    }
    public static CSVJSON getInstance() {
        if (instance == null) {
            instance = new CSVJSON();
        }
        return instance;
    }
    private String csv() {
        Path currentRelativePath = Paths.get("");
        String ruta = currentRelativePath.toAbsolutePath().toString();
        String dir = ruta + File.separator + "data";
        return dir + File.separator + "funkos.csv";
    }
    private String json() {
        Path currentRelativePath = Paths.get("");
        String ruta = currentRelativePath.toAbsolutePath().toString();
        String dir = ruta + File.separator + "data";
        return dir + File.separator + "funkos.json";
    }
    public CompletableFuture<List<Funko>> mostrarCsv() {
        return CompletableFuture.supplyAsync(() -> {
            List<Funko> funkoList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(csv()))) {
                funkoList = reader.lines()
                        .skip(1)
                        .map(linea -> linea.split(","))
                        .map(valores -> new Funko(
                                UUID.fromString(valores[0].substring(0, 35)),
                                valores[1],
                                Tipo.valueOf(valores[2]),
                                Double.parseDouble(valores[3]),
                                LocalDate.parse(valores[4]),
                                m.addandgetID()
                        ))
                        .toList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return funkoList;
        });
    }
}
