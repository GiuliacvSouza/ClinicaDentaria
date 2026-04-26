package controller;

import app.MainFX;
import app.SceneManager;
import app.SessionContext;
import bll.ConsultaService;
import bll.AtendimentoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Consulta;
import model.Paciente;
import model.Utilizador;
import model.dto.ConsultaAgendadaDTO;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import model.Atendimento;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgendaController {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label nomeUtilizador;
    @FXML private TableView<ConsultaAgendadaDTO> tblConsultas;
    @FXML private TableColumn<ConsultaAgendadaDTO, LocalDateTime> colHora;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colPaciente;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colDentista;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colProcedimento;
    @FXML private TableColumn<ConsultaAgendadaDTO, EstadoConsulta> colEstado;
    @FXML private TableColumn<ConsultaAgendadaDTO, String> colAcoes;

    @FXML private Button btnTodas;
    @FXML private Button btnPendentes;
    @FXML private Button btnEmEspera;
    @FXML private Button btnEmConsulta;
    @FXML private Button btnConcluidas;
    @FXML private Button btnHoje;
    @FXML private javafx.scene.control.TextField txtPesquisa;

    @Autowired
    private ConsultaService consultaService;
    @Autowired
    private AtendimentoService atendimentoService;

    private EstadoConsulta filtroAtual = null;
    private ObservableList<ConsultaAgendadaDTO> consultasCarregadas;
    private FilteredList<ConsultaAgendadaDTO> consultasFiltradas;
    private java.time.LocalDate dataFiltro;

    @FXML
    public void initialize() {
        Utilizador utilizadorLogado = SessionContext.getUtilizadorLogado();
        if (utilizadorLogado != null && nomeUtilizador != null) {
            nomeUtilizador.setText(utilizadorLogado.getPrimeiroNome() + " " + utilizadorLogado.getUltimoNome());
        }

        configurarTabela();
        atualizarEstilosFiltro(btnTodas);

        // configurar pesquisa
        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((obs, oldV, newV) -> aplicarFiltroPesquisa(newV));
        }

        // inicializa o filtro de data para hoje e atualiza o rótulo do botão
        dataFiltro = java.time.LocalDate.now();
        atualizarLabelHoje();
        carregarConsultas();
    }

    private void atualizarLabelHoje() {
        if (btnHoje != null) {
            if (dataFiltro == null) {
                btnHoje.setText("Hoje");
            } else {
                btnHoje.setText("Hoje: " + dataFiltro.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }
    }

    private void configurarTabela() {
        colHora.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                converterParaDataHora(cellData.getValue() != null ? cellData.getValue().getDataHoraInicio() : null)
        ));

        colHora.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(HORA_FORMATTER));
            }
        });

        colPaciente.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valorOuPadrao(cellData.getValue() != null ? cellData.getValue().getNomePaciente() : null)
                )
        );

        colDentista.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valorOuPadrao(cellData.getValue() != null ? cellData.getValue().getNomeDentista() : null)
                )
        );

        colProcedimento.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        valorOuPadrao(cellData.getValue() != null ? cellData.getValue().getProcedimento() : null)
                ));

        colEstado.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue() != null ? cellData.getValue().getStatus() : null
        ));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label statusLabel = new Label();
            private final StackPane wrapper = new StackPane(statusLabel);

            {
                statusLabel.getStyleClass().add("agenda-status-pill");
                wrapper.setPrefHeight(44);
            }

            @Override
            protected void updateItem(EstadoConsulta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                statusLabel.setText(getTextoEstado(item));
                statusLabel.getStyleClass().setAll("agenda-status-pill", getClasseEstado(item));
                setText(null);
                setGraphic(wrapper);
            }
        });

        colAcoes.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(""));
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVerFicha = new Button("Ver ficha do paciente");
            private final Button btnAcaoPrimaria = new Button();
            private final Button btnAcaoSecundaria = new Button();
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox actionsBox = new HBox(10, btnVerFicha, btnAcaoPrimaria, btnAcaoSecundaria, btnCancelar);

            {
                actionsBox.getStyleClass().add("agenda-actions-box");
                btnVerFicha.getStyleClass().add("agenda-action-button");
                btnAcaoPrimaria.getStyleClass().add("agenda-action-button");
                btnAcaoSecundaria.getStyleClass().add("agenda-action-button");
                btnCancelar.getStyleClass().add("agenda-link-button");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                ConsultaAgendadaDTO consulta = getTableRow() != null ? getTableRow().getItem() : null;
                if (empty || consulta == null) {
                    setGraphic(null);
                    return;
                }

                configurarBotoesAcao(consulta);
                setGraphic(actionsBox);
            }

            private void configurarBotoesAcao(ConsultaAgendadaDTO consulta) {
                EstadoConsulta status = consulta.getStatus();
                btnVerFicha.setOnAction(e -> abrirFichaPaciente(consulta));

                btnAcaoPrimaria.setVisible(false);
                btnAcaoPrimaria.setManaged(false);
                btnAcaoSecundaria.setVisible(false);
                btnAcaoSecundaria.setManaged(false);
                btnCancelar.setVisible(false);
                btnCancelar.setManaged(false);

                if (status == EstadoConsulta.AGENDADA) {
                    btnAcaoPrimaria.setText("Confirmar consulta");
                    btnAcaoPrimaria.setVisible(true);
                    btnAcaoPrimaria.setManaged(true);
                    btnAcaoPrimaria.setOnAction(e -> executarAcaoComFeedback(
                            () -> consultaService.confirmarConsulta(consulta.getIdConsulta()),
                            "Consulta confirmada com sucesso."
                    ));

                    btnAcaoSecundaria.setText("Reagendar");
                    btnAcaoSecundaria.setVisible(true);
                    btnAcaoSecundaria.setManaged(true);
                    btnAcaoSecundaria.setOnAction(e -> reagendarConsulta(consulta));

                    btnCancelar.setVisible(true);
                    btnCancelar.setManaged(true);
                    btnCancelar.setOnAction(e -> executarAcaoComFeedback(
                            () -> consultaService.cancelar(consulta.getIdConsulta()),
                            "Consulta cancelada com sucesso."
                    ));
                    return;
                }

                if (status == EstadoConsulta.CONFIRMADA) {
                    btnAcaoPrimaria.setText("Marcar chegada");
                    btnAcaoPrimaria.setVisible(true);
                    btnAcaoPrimaria.setManaged(true);
                    btnAcaoPrimaria.setOnAction(e -> executarAcaoComFeedback(
                            () -> consultaService.marcarChegada(consulta.getIdConsulta()),
                            "Consulta atualizada para Em espera."
                    ));
                    return;
                }

                if (status == EstadoConsulta.EM_ESPERA) {
                    btnAcaoPrimaria.setText("Iniciar consulta");
                    btnAcaoPrimaria.setVisible(true);
                    btnAcaoPrimaria.setManaged(true);
                    btnAcaoPrimaria.setOnAction(e -> executarAcaoComFeedback(
                            () -> consultaService.iniciarConsulta(consulta.getIdConsulta()),
                            "Consulta iniciada com sucesso."
                    ));
                    return;
                }

                if (status == EstadoConsulta.EM_CONSULTA) {
                    btnAcaoPrimaria.setText("Finalizar consulta");
                    btnAcaoPrimaria.setVisible(true);
                    btnAcaoPrimaria.setManaged(true);
                    btnAcaoPrimaria.setOnAction(e -> executarAcaoComFeedback(
                            () -> consultaService.finalizarConsulta(consulta.getIdConsulta()),
                            "Consulta finalizada com sucesso."
                    ));
                }
            }
        });
    }

    private void aplicarFiltroPesquisa(String termo) {
        if (consultasFiltradas == null) return;
        String t = termo == null ? "" : termo.trim().toLowerCase();
        if (t.isBlank()) {
            consultasFiltradas.setPredicate(c -> true);
            return;
        }

        consultasFiltradas.setPredicate(c -> {
            if (c == null) return false;
            String nomePac = c.getNomePaciente() != null ? c.getNomePaciente().toLowerCase() : "";
            String nomeDen = c.getNomeDentista() != null ? c.getNomeDentista().toLowerCase() : "";
            String nif = c.getNifPaciente() != null ? c.getNifPaciente().toLowerCase() : "";
            String proc = c.getProcedimento() != null ? c.getProcedimento().toLowerCase() : "";

            return nomePac.contains(t) || nomeDen.contains(t) || nif.contains(t) || proc.contains(t);
        });
    }

    private void carregarConsultas() {
        try {
            List<ConsultaAgendadaDTO> consultas = buscarConsultasParaAgenda();

            System.out.println("[AGENDA] Consultas retornadas pelo serviço: " + (consultas != null ? consultas.size() : 0));
            if (consultas != null && !consultas.isEmpty()) {
                System.out.println("[AGENDA] Exemplo de entrada: id=" + consultas.get(0).getIdConsulta() + ", paciente=" + consultas.get(0).getNomePaciente());
            }

            if (consultasCarregadas == null) {
                consultasCarregadas = FXCollections.observableArrayList();
                consultasFiltradas = new FilteredList<>(consultasCarregadas, p -> true);
                tblConsultas.setItems(consultasFiltradas);
            }

            consultasCarregadas.setAll(consultas);
            tblConsultas.setPlaceholder(new Label("Nenhuma consulta encontrada."));
            System.out.println("[AGENDA] tblConsultas items size after setAll: " + (tblConsultas.getItems() != null ? tblConsultas.getItems().size() : 0));
            System.out.println("[AGENDA] tblConsultas visible=" + tblConsultas.isVisible() + ", managed=" + tblConsultas.isManaged());
            System.out.println("[AGENDA] tblConsultas scene present=" + (tblConsultas.getScene() != null));
            if (tblConsultas.getScene() != null && tblConsultas.getScene().getRoot() != null) {
                System.out.println("[AGENDA] Scene root: " + tblConsultas.getScene().getRoot().getClass().getName());
            }
            tblConsultas.refresh();
        } catch (Exception e) {
            System.err.println("[AGENDA] Erro ao carregar consultas: " + e.getMessage());
            e.printStackTrace();
            if (consultasCarregadas != null) {
                consultasCarregadas.clear();
            }
            tblConsultas.setPlaceholder(new Label("Nao foi possivel carregar as consultas."));
        }
    }

    private List<ConsultaAgendadaDTO> buscarConsultasParaAgenda() {
        try {
            List<ConsultaAgendadaDTO> consultas = filtroAtual == null
                    ? consultaService.listarTodasAgendadas()
                    : consultaService.listarPorStatusAgendadas(filtroAtual);

            if (!consultas.isEmpty()) {
                // aplicar filtro de data se definido
                if (dataFiltro != null) {
                    return consultas.stream()
                            .filter(c -> c.getDataHoraInicio() != null && java.time.LocalDateTime.ofInstant(c.getDataHoraInicio(), java.time.ZoneId.systemDefault()).toLocalDate().equals(dataFiltro))
                            .toList();
                }
                return consultas;
            }
        } catch (Exception ex) {
            System.err.println("[AGENDA] Falha na listagem DTO, a usar fallback local: " + ex.getMessage());
        }

        List<Consulta> consultasBase = filtroAtual == null
                ? consultaService.listarTodas()
                : consultaService.listarPorStatus(filtroAtual);

        List<ConsultaAgendadaDTO> converted = consultasBase.stream()
                .sorted(Comparator.comparing(Consulta::getDataHoraInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toConsultaAgendadaDTO)
                .toList();

        if (dataFiltro != null) {
            return converted.stream()
                    .filter(c -> c.getDataHoraInicio() != null && java.time.LocalDateTime.ofInstant(c.getDataHoraInicio(), java.time.ZoneId.systemDefault()).toLocalDate().equals(dataFiltro))
                    .toList();
        }

        return converted;
    }

    @FXML
    private void escolherDia() {
        Dialog<java.time.LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Selecionar dia");
        dialog.setHeaderText("Escolha o dia cujas consultas pretende ver.");

        ButtonType confirmar = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmar, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(dataFiltro != null ? dataFiltro : java.time.LocalDate.now());
        dialog.getDialogPane().setContent(datePicker);

        dialog.setResultConverter(btn -> btn == confirmar ? datePicker.getValue() : null);

        dialog.showAndWait().ifPresent(selected -> {
            dataFiltro = selected;
            atualizarLabelHoje();
            carregarConsultas();
        });
    }

    /**
     * Reseta o filtro de data para hoje (usar ao entrar/logar na tela)
     */
    private void resetDataParaHoje() {
        dataFiltro = java.time.LocalDate.now();
        atualizarLabelHoje();
    }

    private void executarAcaoComFeedback(Runnable acao, String mensagemSucesso) {
        try {
            acao.run();
            carregarConsultas();
            tblConsultas.refresh();
            mostrarAlerta(mensagemSucesso);
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void reagendarConsulta(ConsultaAgendadaDTO consultaDto) {
        try {
            Consulta consulta = consultaService.buscarPorId(consultaDto.getIdConsulta());
            if (consulta == null || consulta.getIdDentista() == null) {
                mostrarErro("Nao foi possivel determinar o dentista da consulta.");
                return;
            }

            LocalDate dataPadrao = consulta.getDataHoraInicio() != null
                    ? consulta.getDataHoraInicio().atZone(ZoneId.systemDefault()).toLocalDate()
                    : LocalDate.now().plusDays(1);

            Dialog<LocalDateTime> dialog = new Dialog<>();
            dialog.setTitle("Reagendar consulta");
            dialog.setHeaderText("Escolha uma nova data e um horario disponivel.");

            ButtonType confirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmar, ButtonType.CANCEL);

            DatePicker datePicker = new DatePicker(dataPadrao);
            ComboBox<String> cbHora = new ComboBox<>();
            cbHora.setPrefWidth(150);

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.add(new Label("Data"), 0, 0);
            form.add(datePicker, 1, 0);
            form.add(new Label("Hora"), 0, 1);
            form.add(cbHora, 1, 1);
            dialog.getDialogPane().setContent(form);

            // atualizar horarios quando a data for alterada
            Runnable atualizar = () -> {
                try {
                    LocalDate data = datePicker.getValue();
                    int duracao = 30;
                    Atendimento atendimento = atendimentoService.buscarPorConsulta(consulta);
                    if (atendimento != null && atendimento.getProcedimentos() != null && !atendimento.getProcedimentos().isEmpty()) {
                        Integer d = atendimento.getProcedimentos().get(0).getProcedimento().getDuracaoEstimada();
                        if (d != null && d > 0) duracao = d;
                    }

                    List<String> horarios = calcularHorariosDisponiveis(consulta.getIdDentista().getHorarioEntrada(),
                            consulta.getIdDentista().getHorarioSaida(),
                            consulta.getIdDentista().getId(), data, duracao, consulta.getId());

                    cbHora.setItems(FXCollections.observableArrayList(horarios));
                    if (!horarios.isEmpty()) {
                        cbHora.setValue(horarios.get(0));
                    }
                } catch (Exception ex) {
                    cbHora.setItems(FXCollections.observableArrayList());
                }
            };

            datePicker.valueProperty().addListener((obs, oldV, newV) -> atualizar.run());
            atualizar.run();

            dialog.setResultConverter(button -> {
                if (button != confirmar) return null;
                LocalDate data = datePicker.getValue();
                String horaStr = cbHora.getValue();
                if (data == null || horaStr == null || horaStr.isBlank()) {
                    throw new IllegalArgumentException("Escolha data e hora disponiveis para reagendar.");
                }
                LocalTime hora = LocalTime.parse(horaStr, HORA_FORMATTER);
                return LocalDateTime.of(data, hora);
            });

            Optional<LocalDateTime> resultado = dialog.showAndWait();
            if (resultado.isEmpty()) return;

            Instant novaDataHora = resultado.get().atZone(ZoneId.systemDefault()).toInstant();
            executarAcaoComFeedback(
                    () -> consultaService.reagendar(consultaDto.getIdConsulta(), novaDataHora),
                    "Consulta reagendada com sucesso."
            );

        } catch (IllegalArgumentException ex) {
            mostrarErro(ex.getMessage());
        } catch (Exception ex) {
            mostrarErro("Nao foi possivel reagendar a consulta: " + ex.getMessage());
        }
    }

    private List<String> calcularHorariosDisponiveis(LocalTime horarioEntrada, LocalTime horarioSaida,
                                                     Integer dentistaId, LocalDate data, int duracaoMinutos, Integer idConsultaAtual) {
        if (data == null || dentistaId == null) return List.of();

        LocalTime inicio = horarioEntrada != null ? horarioEntrada : LocalTime.of(8,0);
        LocalTime fim = horarioSaida != null ? horarioSaida : LocalTime.of(18,0);

        inicio = ajustarInicioParaHoje(data, inicio);

        List<Consulta> consultasNoDia = consultaService.listarPorDentistaEDia(dentistaId, data);
        // remove a propria consulta que estamos a reagendar
        consultasNoDia.removeIf(c -> c.getId() != null && c.getId().equals(idConsultaAtual));

        java.util.List<String> disponiveis = new java.util.ArrayList<>();

        for (LocalTime slot = inicio; !slot.plusMinutes(duracaoMinutos).isAfter(fim); slot = slot.plusMinutes(30)) {
            LocalTime fimSlot = slot.plusMinutes(duracaoMinutos);
            boolean conflito = false;
            for (Consulta c : consultasNoDia) {
                if (c.getDataHoraInicio() == null) continue;
                LocalTime inicioExistente = c.getDataHoraInicio().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
                int durExist = c.getDuracao() != null ? c.getDuracao() : 30;
                LocalTime fimExistente = inicioExistente.plusMinutes(durExist);
                if (slot.isBefore(fimExistente) && fimSlot.isAfter(inicioExistente)) {
                    conflito = true;
                    break;
                }
            }
            if (!conflito) {
                disponiveis.add(slot.format(HORA_FORMATTER));
            }
        }

        return disponiveis;
    }

    private LocalTime ajustarInicioParaHoje(LocalDate data, LocalTime inicioPadrao) {
        if (!LocalDate.now().equals(data)) {
            return inicioPadrao;
        }

        LocalTime agora = LocalTime.now().plusMinutes(15);
        int minutoArredondado = ((agora.getMinute() + 29) / 30) * 30;
        LocalTime arredondado = agora.withMinute(0).withSecond(0).withNano(0);
        if (minutoArredondado >= 60) {
            arredondado = arredondado.plusHours(1);
            minutoArredondado = 0;
        }
        arredondado = arredondado.withMinute(minutoArredondado);
        return arredondado.isAfter(inicioPadrao) ? arredondado : inicioPadrao;
    }

    private void abrirFichaPaciente(ConsultaAgendadaDTO consultaDto) {
        try {
            Consulta consulta = consultaService.buscarPorId(consultaDto.getIdConsulta());
            if (consulta == null || consulta.getIdPaciente() == null || consulta.getIdPaciente().getId() == null) {
                mostrarErro("Paciente sem dados associados.");
                return;
            }

            Integer pacienteId = consulta.getIdPaciente().getId();

            var resource = getClass().getResource("/fxml/PacienteView.fxml");
            if (resource == null) {
                mostrarErro("A pagina de perfil do paciente nao esta disponivel.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            if (MainFX.getSpringContext() != null) {
                loader.setControllerFactory(MainFX.getSpringContext()::getBean);
            }

            Stage stage = (Stage) nomeUtilizador.getScene().getWindow();
            boolean estavaMaximizada = stage.isMaximized();
            boolean estavaTelaCheia = stage.isFullScreen();
            double larguraCenaAtual = stage.getScene() != null && stage.getScene().getWidth() > 0
                    ? stage.getScene().getWidth()
                    : Math.max(stage.getWidth(), 1);
            double alturaCenaAtual = stage.getScene() != null && stage.getScene().getHeight() > 0
                    ? stage.getScene().getHeight()
                    : Math.max(stage.getHeight(), 1);
            double larguraJanelaAtual = stage.getWidth() > 0 ? stage.getWidth() : larguraCenaAtual;
            double alturaJanelaAtual = stage.getHeight() > 0 ? stage.getHeight() : alturaCenaAtual;

            Parent root = loader.load();
            PacientePerfilController controller = loader.getController();
            controller.setPacienteId(pacienteId);

            Scene scene = new Scene(root, larguraCenaAtual, alturaCenaAtual);
            aplicarStylesheet(scene, "/fxml/PacienteView.fxml");
            stage.setScene(scene);
            if (estavaTelaCheia) {
                stage.setFullScreen(true);
            } else if (estavaMaximizada) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(larguraJanelaAtual);
                stage.setHeight(alturaJanelaAtual);
            }
            stage.show();
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private String montarFichaPaciente(Consulta consulta, Paciente paciente, Utilizador utilizador) {
        String dataNascimento = utilizador.getDataNascimento() != null ? utilizador.getDataNascimento().toString() : "-";
        String email = valorOuPadrao(utilizador.getEmail());
        String nif = valorOuPadrao(utilizador.getNif());
        String telefone = valorOuPadrao(utilizador.getTelefone());
        String telemovel = valorOuPadrao(utilizador.getTelemovel());
        String statusPaciente = paciente != null ? valorOuPadrao(paciente.getStatus()) : "-";
        String dentista = formatarNome(consulta.getIdDentista() != null ? consulta.getIdDentista().getUtilizador() : null);
        String dataConsulta = consulta.getDataHoraInicio() != null
                ? LocalDateTime.ofInstant(consulta.getDataHoraInicio(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "-";

        return "NIF: " + nif + "\n"
                + "Email: " + email + "\n"
                + "Telefone: " + telefone + "\n"
                + "Telemovel: " + telemovel + "\n"
                + "Data de nascimento: " + dataNascimento + "\n"
                + "Status do paciente: " + statusPaciente + "\n"
                + "Consulta: " + valorOuPadrao(consulta.getTipo()) + "\n"
                + "Data/Hora: " + dataConsulta + "\n"
                + "Dentista: " + valorOuPadrao(dentista);
    }

    private String valorOuPadrao(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private String formatarNome(Utilizador utilizador) {
        if (utilizador == null) {
            return null;
        }

        String primeiroNome = utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome().trim() : "";
        String ultimoNome = utilizador.getUltimoNome() != null ? utilizador.getUltimoNome().trim() : "";
        String nomeCompleto = (primeiroNome + " " + ultimoNome).trim();
        return nomeCompleto.isEmpty() ? null : nomeCompleto;
    }

    private String getTextoEstado(EstadoConsulta estado) {
        return switch (estado) {
            case CONCLUIDA -> "Concluida";
            case FATURADA -> "Faturada";
            case EM_CONSULTA -> "Em consulta";
            case EM_ESPERA -> "Sala de Espera";
            case CONFIRMADA -> "Confirmado";
            case AGENDADA -> "Agendado";
            default -> estado.getDescricao();
        };
    }

    private String getClasseEstado(EstadoConsulta estado) {
        return switch (estado) {
            case CONCLUIDA -> "agenda-status-concluido";
            case FATURADA -> "agenda-status-confirmado";
            case EM_CONSULTA -> "agenda-status-em-consulta";
            case EM_ESPERA -> "agenda-status-sala-espera";
            case CONFIRMADA -> "agenda-status-confirmado";
            case AGENDADA -> "agenda-status-agendado";
            default -> "agenda-status-default";
        };
    }

    @FXML
    private void filtrarTodas() {
        filtroAtual = null;
        atualizarEstilosFiltro(btnTodas);
        carregarConsultas();
    }

    @FXML
    private void filtrarPendentes() {
        filtroAtual = EstadoConsulta.PENDENTE;
        atualizarEstilosFiltro(btnPendentes);
        carregarConsultas();
    }

    @FXML
    private void filtrarEmEspera() {
        filtroAtual = EstadoConsulta.EM_ESPERA;
        atualizarEstilosFiltro(btnEmEspera);
        carregarConsultas();
    }

    @FXML
    private void filtrarEmConsulta() {
        filtroAtual = EstadoConsulta.EM_CONSULTA;
        atualizarEstilosFiltro(btnEmConsulta);
        carregarConsultas();
    }

    @FXML
    private void filtrarConcluidas() {
        filtroAtual = EstadoConsulta.CONCLUIDA;
        atualizarEstilosFiltro(btnConcluidas);
        carregarConsultas();
    }

    private void atualizarEstilosFiltro(Button botaoSelecionado) {
        List<Button> botoes = List.of(btnTodas, btnPendentes, btnEmEspera, btnEmConsulta, btnConcluidas);
        for (Button botao : botoes) {
            if (!botao.getStyleClass().contains("filter-chip")) {
                botao.getStyleClass().add("filter-chip");
            }
            botao.getStyleClass().remove("filter-chip-active");
        }

        if (!botaoSelecionado.getStyleClass().contains("filter-chip-active")) {
            botaoSelecionado.getStyleClass().add("filter-chip-active");
        }
    }

    @FXML
    private void abrirAgenda() {
        // Pagina atual
    }

    @FXML
    private void abrirNovaMarcacao() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/nova-marcacao.fxml"));
            if (MainFX.getSpringContext() != null) {
                loader.setControllerFactory(MainFX.getSpringContext()::getBean);
            }

            Parent root = loader.load();
            NovaMarcacaoController controller = loader.getController();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(nomeUtilizador.getScene().getWindow());
            modalStage.setResizable(false);
            modalStage.setTitle("Nova Marcacao");

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/dashboard-style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            controller.setStage(modalStage);
            modalStage.setScene(scene);
            modalStage.showAndWait();

            if (controller.isSaved()) {
                carregarConsultas();
            }
        } catch (Exception ex) {
            Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
            mostrarErro(causa.getMessage());
        }
    }

    @FXML
    private void abrirPacientes() throws IOException {
        SceneManager.trocarTela("/fxml/pacientes.fxml", "/css/dashboard-style.css");
    }

    @FXML
    private void abrirFaturacao() throws IOException {
        SceneManager.trocarTela("/fxml/payment-view.fxml", "/css/payment-style.css");
    }

    @FXML
    private void fazerLogout() throws IOException {
        SessionContext.limparSessao();
        SceneManager.trocarTelaMaximizado("/fxml/login-view.fxml", "/css/login-style.css");
    }
    

    private void aplicarStylesheet(Scene scene, String fxmlPath) {
        String cssPath = switch (fxmlPath) {
            case "/fxml/Agenda.fxml", "/fxml/pacientes.fxml" -> "/css/dashboard-style.css";
            case "/fxml/payment-view.fxml" -> "/css/payment-style.css";
            case "/fxml/login-view.fxml" -> "/css/login-style.css";
            default -> null;
        };

        if (cssPath == null) {
            return;
        }

        var cssResource = getClass().getResource(cssPath);
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacao");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem != null && !mensagem.isBlank() ? mensagem : "Nao foi possivel concluir a operacao.");
        alert.showAndWait();
    }

    private ConsultaAgendadaDTO toConsultaAgendadaDTO(Consulta consulta) {
        if (consulta == null) {
            return new ConsultaAgendadaDTO();
        }

        ConsultaAgendadaDTO dto = new ConsultaAgendadaDTO(
                consulta.getId(),
                formatarNome(consulta.getIdPaciente() != null ? consulta.getIdPaciente().getUtilizador() : null),
                formatarNome(consulta.getIdDentista() != null ? consulta.getIdDentista().getUtilizador() : null),
                resolverProcedimento(consulta),
                consulta.getDataHoraInicio(),
                consulta.getStatus()
        );

        try {
            if (consulta.getIdPaciente() != null && consulta.getIdPaciente().getUtilizador() != null) {
                dto.setNifPaciente(consulta.getIdPaciente().getUtilizador().getNif());
            }
        } catch (Exception ignored) {}

        return dto;
    }

    private LocalDateTime converterParaDataHora(Instant instante) {
        if (instante == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instante, ZoneId.systemDefault());
    }

    private String resolverProcedimento(Consulta consulta) {
        if (consulta == null) {
            return null;
        }

        if (consulta.getObservacoes() != null) {
            for (String linha : consulta.getObservacoes().split("\\R")) {
                if (linha != null && linha.startsWith("Procedimento:")) {
                    return linha.substring("Procedimento:".length()).trim();
                }
            }
        }

        return consulta.getTipo();
    }
}
