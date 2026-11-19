package mx.edu.utch.dwi.alumnos;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AlumnoService {
    private final AtomicLong nextId = new AtomicLong(1);
    private final ConcurrentMap<Long, Alumno> alumnos = new ConcurrentHashMap<>();

    public List<Alumno> listar() {
        return alumnos.values().stream()
                .sorted(Comparator.comparing(Alumno::id))
                .toList();
    }

    public Alumno buscar(Long id) {
        Alumno alumno = alumnos.get(id);
        if (alumno == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado");
        }
        return alumno;
    }

    public Alumno crear(AlumnoRequest request) {
        Long id = nextId.getAndIncrement();
        Alumno alumno = new Alumno(id, request.nombre(), request.matricula(), request.carrera());
        alumnos.put(id, alumno);
        return alumno;
    }

    public Alumno actualizar(Long id, AlumnoRequest request) {
        if (!alumnos.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado");
        }
        Alumno alumno = new Alumno(id, request.nombre(), request.matricula(), request.carrera());
        alumnos.put(id, alumno);
        return alumno;
    }

    public void eliminar(Long id) {
        if (alumnos.remove(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado");
        }
    }
}
