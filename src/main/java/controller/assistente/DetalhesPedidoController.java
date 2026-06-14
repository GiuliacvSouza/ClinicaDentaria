package controller.assistente;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.ItemPedido;
import model.PedidoCompra;
import model.enums.EstadoPedidoCompra;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetalhesPedidoController {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double ROW_HEIGHT = 36.0;
    private static final double HEADER_HEIGHT = 40.0;
    private static final double MAX_TABLE_HEIGHT = 280.0;
    private static final double MIN_TABLE_HEIGHT = 100.0;

    @FXML private Label lblTituloJanela;
    @FXML private Label lblNumero;
    @FXML private Label lblFornecedor;
    @FXML private Label lblData;
    @FXML private Label lblResponsavel;
    @FXML private Label lblEstado;
    @FXML private Label lblObservacoes;
    @FXML private Label lblTotal;

    @FXML private TableView<ItemPedido>          tblItens;
    @FXML private TableColumn<ItemPedido, String> colMaterial;
    @FXML private TableColumn<ItemPedido, String> colQuantidade;
    @FXML private TableColumn<ItemPedido, String> colValorUnit;
    @FXML private TableColumn<ItemPedido, String> colSubtotal;

    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }

    public void setPedido(PedidoCompra pedido) {
        if (pedido == null) return;

        lblTituloJanela.setText("Pedido #" + pedido.getId());

        lblNumero.setText(pedido.getId() != null ? "#" + pedido.getId() : "-");

        var forn = pedido.getIdFornecedor();
        if (forn != null) {
            String nome = (forn.getNome() != null ? forn.getNome() : "")
                    + (forn.getUltimoNome() != null && !forn.getUltimoNome().isBlank()
                    ? " " + forn.getUltimoNome() : "");
            lblFornecedor.setText(nome.isBlank() ? "-" : nome.trim());
        } else {
            lblFornecedor.setText("-");
        }

        lblData.setText(pedido.getDataPedido() != null ? pedido.getDataPedido().format(DATA_FMT) : "-");

        var assist = pedido.getIdAssistente();
        if (assist != null && assist.getUtilizador() != null) {
            var u = assist.getUtilizador();
            String nome = (u.getPrimeiroNome() != null ? u.getPrimeiroNome() : "")
                    + " " + (u.getUltimoNome() != null ? u.getUltimoNome() : "");
            lblResponsavel.setText(nome.trim().isBlank() ? "-" : nome.trim());
        } else {
            lblResponsavel.setText("-");
        }

        lblEstado.setText(textoEstado(pedido.getEstado()));

        lblObservacoes.setText(pedido.getObservacoes() != null && !pedido.getObservacoes().isBlank()
                ? pedido.getObservacoes() : "—");

        List<ItemPedido> itens = pedido.getItens();
        if (itens != null && !itens.isEmpty()) {
            configurarTabela();
            tblItens.setItems(FXCollections.observableArrayList(itens));
            ajustarAlturaTabela(itens.size());

            BigDecimal total = itens.stream()
                    .filter(i -> i.getValor() != null && i.getQuantidade() != null)
                    .map(i -> i.getValor().multiply(BigDecimal.valueOf(i.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblTotal.setText(String.format("%.2f €", total));
        } else {
            lblTotal.setText("0,00 €");
        }
    }

    private void ajustarAlturaTabela(int itemCount) {
        double altura = HEADER_HEIGHT + itemCount * ROW_HEIGHT;
        altura = Math.max(MIN_TABLE_HEIGHT, Math.min(altura, MAX_TABLE_HEIGHT));
        tblItens.setPrefHeight(altura);
    }

    private void configurarTabela() {
        colMaterial.setCellValueFactory(c -> {
            var m = c.getValue().getIdMaterial();
            return new SimpleStringProperty(m != null && m.getNome() != null ? m.getNome() : "-");
        });

        colQuantidade.setCellValueFactory(c -> {
            Integer qty = c.getValue().getQuantidade();
            String unid = c.getValue().getIdMaterial() != null
                    && c.getValue().getIdMaterial().getUnidadeMedida() != null
                    ? " " + c.getValue().getIdMaterial().getUnidadeMedida() : "";
            return new SimpleStringProperty((qty != null ? qty : 0) + unid);
        });

        colValorUnit.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getValor();
            return new SimpleStringProperty(v != null ? String.format("%.2f €", v) : "0,00 €");
        });
        colValorUnit.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle("-fx-alignment: CENTER-RIGHT;");
            }
        });

        colSubtotal.setCellValueFactory(c -> {
            Integer qty = c.getValue().getQuantidade();
            BigDecimal v = c.getValue().getValor();
            if (qty != null && v != null) {
                BigDecimal sub = v.multiply(BigDecimal.valueOf(qty));
                return new SimpleStringProperty(String.format("%.2f €", sub));
            }
            return new SimpleStringProperty("0,00 €");
        });
        colSubtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle("-fx-alignment: CENTER-RIGHT;");
            }
        });
    }

    @FXML
    private void fechar() {
        if (stage != null) stage.close();
    }

    private String textoEstado(EstadoPedidoCompra e) {
        if (e == null) return "-";
        return switch (e) {
            case PENDENTE  -> "Pendente";
            case ENVIADO   -> "Enviado";
            case RECEBIDO  -> "Recebido";
            case CANCELADO -> "Cancelado";
        };
    }
}
