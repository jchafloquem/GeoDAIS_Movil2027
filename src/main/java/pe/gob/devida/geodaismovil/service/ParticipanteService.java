package pe.gob.devida.geodaismovil.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.devida.geodaismovil.model.Participante;
import pe.gob.devida.geodaismovil.repository.ParticipanteRepository;

@Service
public class ParticipanteService {

    private final ParticipanteRepository repository;

    public ParticipanteService(ParticipanteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Participante> buscarPorDni(String dni) {
        return repository.findByDniOrderByIdParticipanteAsc(dni);
    }
}
