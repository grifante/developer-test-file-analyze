package br.com.segware;

import static br.com.segware.Tipo.ALARME;
import static br.com.segware.Tipo.DESARME;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.averagingLong;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class AnalizadorCSV implements IAnalisadorRelatorio {

	private Path csv;
	private static final int MAX_MINUTOS_SEQ_ALARME_DESARME = 5;

	public AnalizadorCSV(Path csv) {
		this.csv = csv;
	}

	@Override
	public Map<String, Integer> getTotalEventosCliente() {
		Collector<Registro, ?, Integer> counting = reducing(0, e -> 1, Integer::sum);
		return stream().collect(groupingBy(Registro::getCdCliente, counting));
	}

	@Override
	public Map<String, Long> getTempoMedioAtendimentoAtendente() {
		return stream()
				.collect(groupingBy(Registro::getCdAtendente, averagingLong(Registro::getTempoAtendimento)))
				.entrySet()
				 .stream()
				  .collect(toMap(Map.Entry::getKey, e -> e.getValue().longValue()));
	}

	@Override
	public List<Tipo> getTiposOrdenadosNumerosEventosDecrescente() {
		return stream()
				.collect(groupingBy(Registro::getTipoEvento, counting()))
				.entrySet()
				 .stream()
				 .sorted(Map.Entry.comparingByValue(reverseOrder()))
				 .map(Map.Entry::getKey)
				 .collect(toList());
	}

	/**
	 * Diferente dos demais métodos, nesse caso os dados são todos carregados em
	 * memória para então executar as consultas. Na hipótese de ser processado
	 * um arquivo muito grande vale utilizar outra estratégia, como por exemplo
	 * Files.lines() faz.
	 */
	@Override
	public List<Integer> getCodigoSequencialEventosDesarmeAposAlarme() {
		List<Registro> registros = stream().collect(toList());
		Set<Integer> codigoEventosDesarme = new HashSet<>();

		for (int indexAlarme = 0; indexAlarme < registros.size(); indexAlarme++) {
			indexAlarme = findNext(indexAlarme, Optional.empty(), ALARME, registros);
			if (indexAlarme < 0) {
				break;
			}
			Registro alarme = registros.get(indexAlarme);

			int indexDesarme = findNext(indexAlarme, Optional.of(alarme.getCdCliente()), DESARME, registros);
			if (indexDesarme < 0) {
				continue;
			}
			Registro desarme = registros.get(indexDesarme);
			if (MINUTES.between(alarme.getInicio(), desarme.getInicio()) <= MAX_MINUTOS_SEQ_ALARME_DESARME) {
				codigoEventosDesarme.add(desarme.getCodigo());
			}
		}
		return new ArrayList<>(codigoEventosDesarme);
	}

	private int findNext(int offset, Optional<String> cdCliente, Tipo tipo, List<Registro> registros) {
		for (int index = offset; index < registros.size(); index++) {
			Registro registro = registros.get(index);
			if (tipo.equals(registro.getTipoEvento()) && cdCliente.map(cd -> registro.getCdCliente().equals(cd)).orElse(true)) {
				return index;
			}
		}
		return -1;
	}

	private Stream<Registro> stream() {
		try {
			return Files.lines(csv).map(Registro::new);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
