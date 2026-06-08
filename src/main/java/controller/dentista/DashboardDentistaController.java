package controller.dentista;

import bll.ConsultaService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.dto.ConsultaAgendadaDTO;
import model.enums.EstadoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DashboardDentistaController extends BaseDentistaController {

    private static final DateTimeFormatter DATA_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM", new Locale("pt", "PT"));
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label lblSaudacao;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblConsultasHoje;
    @FXML private Label lblConsultasEspera;
    @FXML private Label lblConsultasAndamento;
    @FXML private Label lblProximaHora;
    @FXML private Label lblProximoPaciente;
    @FXML private Label lblProximoEstado;
    @FXML private VBox containerConsultas;
    @FXML private Button btnIniciarProxima;

    @Autowired private ConsultaService consultaService;

    @Override
    protected void inicializarEcra() {
        String primeiroNome = nomeDentista().split(" ")[0];
        set(lblSaudacao, saudacao() + ", Dr(a). " + primeiroNome);
        set(lblSubtitulo, "Resumo clinico para hoje, " + LocalDate.now().format(DATA_FMT) + ".");
        carregarResumo();
    }

    private void carregarResumo() {
        try {
            List<ConsultaAgendadaDTO> consultas = consultaService
                    .listarAgendadasPorDentistaEDia(dentistaId(), LocalDate.now()).stream()
                    .sorted(Comparator.comparing(ConsultaAgendadaDTO::getDataHoraInicio,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            set(lblConsultasHoje, String.valueOf(consultas.size()));

            long emEspera = consultas.stream().filter(c -> c.getStatus() == EstadoConsulta.EM_ESPERA).count();
            long emAndamento = consultas.stream().filter(c -> c.getStatus() == EstadoConsulta.EM_CONSULTA).count();
            set(lblConsultasEspera, String.valueOf(emEspera));
            set(lblConsultasAndamento, String.valueOf(emAndamento));

            // Find next consulta that can be started
            consultas.stream()
                    .filter(c -> c.getDataHoraInicio() != null)
                    .filter(c -> c.getStatus() == EstadoConsulta.EM_ESPERA
                            || c.getStatus() == EstadoConsulta.CONFIRMADA
                            || c.getStatus() == EstadoConsulta.AGENDADA)
                    .findFirst()
                    .ifPresentOrElse(this::preencherProximaConsulta, () -> {
                        set(lblProximaHora, "--:--");
                        set(lblProximoPaciente, "Sem proxima consulta");
                        set(lblProximoEstado, "-");
                        if (btnIniciarProxima != null) btnIniciarProxima.setDisable(true);
                    });

            carregarLista(consultas);

            // Enable/disable main button
            boolean temProxima = consultas.stream().anyMatch(c ->
                    c.getStatus() == EstadoConsulta.EM_ESPERA || c.getStatus() == EstadoConsulta.CONFIRMADA);
            if (btnIniciarProxima != null) {
                btnIniciarProxima.setDisable(!temProxima);
                if (!temProxima) {
                    btnIniciarProxima.setText("Nenhuma consulta disponível");
                }
            }

        } catch (Exception e) {
            set(lblConsultasHoje, "0");
            set(lblConsultasEspera, "0");
            set(lblConsultasAndamento, "0");
            set(lblProximaHora, "--:--");
            set(lblProximoPaciente, "Nao foi possivel carregar");
            set(lblProximoEstado, "-");
            if (btnIniciarProxima != null) btnIniciarProxima.setDisable(true);
        }
    }

    private void preencherProximaConsulta(ConsultaAgendadaDTO consulta) {
        LocalDateTime data = LocalDateTime.ofInstant(consulta.getDataHoraInicio(), ZoneId.systemDefault());
        set(lblProximaHora, data.format(HORA_FMT));
        set(lblProximoPaciente, vazio(consulta.getNomePaciente()));
        set(lblProximoEstado, textoEstado(consulta.getStatus()));
    }

    private void carregarLista(List<ConsultaAgendadaDTO> consultas) {
        if (containerConsultas == null) {
            return;
        }
        containerConsultas.getChildren().clear();

        if (consultas.isEmpty()) {
            Label vazio = new Label("Sem consultas agendadas para hoje.");
            vazio.getStyleClass().add("section-caption");
            containerConsultas.getChildren().add(vazio);
            return;
        }

        consultas.stream().limit(6).forEach(c -> containerConsultas.getChildren().add(criarLinhaConsulta(c)));
    }

    private HBox criarLinhaConsulta(ConsultaAgendadaDTO c) {
        HBox linha = new HBox(12);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("consulta-card");

        String hora = c.getDataHoraInicio() != null
                ? LocalDateTime.ofInstant(c.getDataHoraInicio(), ZoneId.systemDefault()).format(HORA_FMT)
                : "--:--";
        Label lblHora = new Label(hora);
        lblHora.getStyleClass().add("consulta-hora");
        lblHora.setMinWidth(72);

        VBox textos = new VBox(2);
        Label paciente = new Label(vazio(c.getNomePaciente()));
        paciente.getStyleClass().add("consulta-paciente");
        Label meta = new Label(vazio(c.getProcedimento()));
        meta.getStyleClass().add("consulta-meta");
        textos.getChildren().addAll(paciente, meta);
        HBox.setHgrow(textos, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label estado = new Label(textoEstado(c.getStatus()));
        estado.getStyleClass().addAll("status-pill", classeEstado(c.getStatus()));

        String acao;
        if (c.getStatus() == EstadoConsulta.EM_ESPERA || c.getStatus() == EstadoConsulta.CONFIRMADA) {
            acao = "Iniciar";
        } else if (c.getStatus() == EstadoConsulta.EM_CONSULTA) {
            acao = "Continuar";
        } else {
            acao = "Abrir";
        }
        Button btn = new Button(acao);
        btn.getStyleClass().add("table-action-button");
        if ("Iniciar".equals(acao)) {
            btn.getStyleClass().add("primary-button");
            btn.setOnAction(e -> {
                consultaService.iniciarConsulta(c.getIdConsulta());
                navegar("/fxml/dentista/atendimento-dentista.fxml?consulta=" + c.getIdConsulta());
            });
        } else {
            btn.setOnAction(e -> navegar("/fxml/dentista/atendimento-dentista.fxml?consulta=" + c.getIdConsulta()));
        }

        linha.getChildren().addAll(lblHora, textos, spacer, estado, btn);
        return linha;
    }

    @FXML
    private void atalhoIniciarProxima() {
        try {
            List<ConsultaAgendadaDTO> consultas = consultaService
                    .listarAgendadasPorDentistaEDia(dentistaId(), LocalDate.now()).stream()
                    .filter(c -> c.getStatus() == EstadoConsulta.EM_ESPERA || c.getStatus() == EstadoConsulta.CONFIRMADA)
                    .sorted(Comparator.comparing(ConsultaAgendadaDTO::getDataHoraInicio,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            if (!consultas.isEmpty()) {
                ConsultaAgendadaDTO proxima = consultas.get(0);
                consultaService.iniciarConsulta(proxima.getIdConsulta());
                navegar("/fxml/dentista/atendimento-dentista.fxml?consulta=" + proxima.getIdConsulta());
            } else {
                mostrarInfo("Nenhuma consulta em espera para iniciar.");
            }
        } catch (Exception e) {
            mostrarErro("Erro ao iniciar consulta: " + e.getMessage());
        }
    }

    @FXML private void atalhoAgenda() { abrirAgenda(); }
    @FXML private void atalhoProntuarios() { abrirProntuarios(); }
    @FXML private void atalhoPrescricoes() { abrirPrescricoes(); }
    @FXML private void atalhoPlanos() { abrirPlanosTratamento(); }

    private String saudacao() {
        int h = LocalDateTime.now().getHour();
        if (h < 12) return "Bom dia";
        if (h < 19) return "Boa tarde";
        return "Boa noite";
    }

    private String vazio(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor.trim();
    }

    private void set(Label label, String valor) {
        if (label != null) {
            label.setText(valor);
        }
    }

    private String textoEstado(EstadoConsulta estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case AGENDADA -> "Agendada";
            case CONFIRMADA -> "Confirmada";
            case EM_ESPERA -> "Em espera";
            case EM_CONSULTA -> "Em consulta";
            case CONCLUIDA -> "Concluida";
            case CANCELADA -> "Cancelada";
            default -> estado.getDescricao();
        };
    }

    private String classeEstado(EstadoConsulta estado) {
        if (estado == null) return "status-agendado";
        return switch (estado) {
            case AGENDADA -> "status-agendado";
            case CONFIRMADA -> "status-confirmado";
            case EM_ESPERA -> "status-em-espera";
            case EM_CONSULTA -> "status-em-consulta";
            case CONCLUIDA -> "status-concluido";
            case CANCELADA -> "status-cancelado";
            default -> "status-agendado";
        };
    }
}