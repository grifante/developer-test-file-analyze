package br.com.segware;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Registro {

	private int codigo;
	private String cdCliente;
	private String cdEvento;
	private Tipo tipoEvento;
	private LocalDateTime inicio;
	private LocalDateTime fim;
	private String cdAtendente;
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public Registro(String linha) {
		String[] strings = linha.split(",");
		codigo = Integer.parseInt(strings[0]);
		cdCliente = strings[1];
		cdEvento = strings[2];
		tipoEvento = Tipo.valueOf(strings[3]);
		inicio = LocalDateTime.parse(strings[4], FORMATTER);
		fim = LocalDateTime.parse(strings[5], FORMATTER);
		cdAtendente = strings[6];
	}

	public long getTempoAtendimento() {
		return ChronoUnit.SECONDS.between(inicio, fim);
	}

	public String getCdEvento() {
		return cdEvento;
	}

	public int getCodigo() {
		return codigo;
	}

	public String getCdAtendente() {
		return cdAtendente;
	}

	public LocalDateTime getInicio() {
		return inicio;
	}

	public LocalDateTime getFim() {
		return fim;
	}

	public String getCdCliente() {
		return cdCliente;
	}

	public Tipo getTipoEvento() {
		return tipoEvento;
	}

}
