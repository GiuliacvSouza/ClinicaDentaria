package controller.administrador;

import bll.FaturaService;
import bll.PagamentoService;
import bll.PedidoCompraService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Fatura;
import model.Pagamento;
import model.PedidoCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FinanceiroController extends BaseAdministradorController {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;

    @FXML private Label lblReceitaDiaria;
    @FXML private Label lblReceitaMensal;
    @FXML private Label lblReceitaAnual;
    @FXML private Label lblDespesaDiaria;
    @FXML private Label lblDespesaMensal;
    @FXML private Label lblLucroLiquido;

    @FXML private TableView<Fatura> tabelaFaturas;
    @FXML private TableColumn<Fatura, Integer> colFaturaId;
    @FXML private TableColumn<Fatura, LocalDate> colFaturaData;
    @FXML private TableColumn<Fatura, BigDecimal> colFaturaValor;
    @FXML private TableColumn<Fatura, String> colFaturaEstado;

    @FXML private TableView<Pagamento> tabelaPagamentos;
    @FXML private TableColumn<Pagamento, Integer> colPagId;
    @FXML private TableColumn<Pagamento, LocalDate> colPagData;
    @FXML private TableColumn<Pagamento, BigDecimal> colPagValor;
    @FXML private TableColumn<Pagamento, String> colPagMetodo;

    @FXML private TableView<PedidoCompra> tabelaPedidos;
    @FXML private TableColumn<PedidoCompra, Integer> colPedidoId;
    @FXML private TableColumn<PedidoCompra, LocalDate> colPedidoData;
    @FXML private TableColumn<PedidoCompra, String> colPedidoEstado;

    @FXML private BarChart<String, Number> graficoFinanceiro;

    @Autowired private FaturaService faturaService;
    @Autowired private PagamentoService pagamentoService;
    @Autowired private PedidoCompraService pedidoCompraService;

    private final ObservableList<Fatura> faturas = FXCollections.observableArrayList();
    private final ObservableList<Pagamento> pagamentos = FXCollections.observableArrayList();
    private final ObservableList<PedidoCompra> pedidos = FXCollections.observableArrayList();

    @Override
    protected void inicializarEcra() {
        colFaturaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFaturaData.setCellValueFactory(new PropertyValueFactory<>("dataEmissao"));
        colFaturaValor.setCellValueFactory(new PropertyValueFactory<>("valorFinal"));
        colFaturaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colPagId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPagData.setCellValueFactory(new PropertyValueFactory<>("dataPagamento"));
        colPagValor.setCellValueFactory(new PropertyValueFactory<>("valorPago"));
        colPagMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));

        colPedidoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPedidoData.setCellValueFactory(new PropertyValueFactory<>("dataPedido"));
        colPedidoEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tabelaFaturas.setItems(faturas);
        tabelaPagamentos.setItems(pagamentos);
        tabelaPedidos.setItems(pedidos);

        dpInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dpFim.setValue(LocalDate.now());

        carregar();
    }

    @FXML
    private void aplicarFiltro() {
        carregar();
    }

    @FXML
    private void resetFiltro() {
        dpInicio.setValue(LocalDate.now());
        dpFim.setValue(LocalDate.now());
        carregar();
    }

    private void carregar() {
        try {
            LocalDate inicio = dpInicio.getValue() != null ? dpInicio.getValue() : LocalDate.now().withDayOfMonth(1);
            LocalDate fim = dpFim.getValue() != null ? dpFim.getValue() : LocalDate.now();

            List<Fatura> listaFaturas = faturaService.listarTodos() != null ? faturaService.listarTodos() : List.of();
            List<Pagamento> listaPagamentos = pagamentoService.listarTodos() != null ? pagamentoService.listarTodos() : List.of();
            List<PedidoCompra> listaPedidos = pedidoCompraService.listarTodos() != null ? pedidoCompraService.listarTodos() : List.of();

            // Filtrar por período
            faturas.setAll(listaFaturas.stream()
                    .filter(f -> f.getDataEmissao() != null
                            && !f.getDataEmissao().isBefore(inicio)
                            && !f.getDataEmissao().isAfter(fim))
                    .toList());
            pagamentos.setAll(listaPagamentos.stream()
                    .filter(p -> p.getDataPagamento() != null
                            && !p.getDataPagamento().isBefore(inicio)
                            && !p.getDataPagamento().isAfter(fim))
                    .toList());
            pedidos.setAll(listaPedidos.stream()
                    .filter(p -> p.getDataPedido() != null
                            && !p.getDataPedido().isBefore(inicio)
                            && !p.getDataPedido().isAfter(fim))
                    .toList());

            // Calcular indicadores
            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = hoje.withDayOfMonth(1);
            LocalDate inicioAno = hoje.withDayOfYear(1);

            BigDecimal receitaDiaria = faturas.stream()
                    .filter(f -> f.getDataEmissao() != null && f.getDataEmissao().equals(hoje))
                    .map(f -> f.getValorFinal() != null ? f.getValorFinal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal receitaMensal = faturas.stream()
                    .filter(f -> f.getDataEmissao() != null
                            && !f.getDataEmissao().isBefore(inicioMes))
                    .map(f -> f.getValorFinal() != null ? f.getValorFinal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal receitaAnual = faturas.stream()
                    .filter(f -> f.getDataEmissao() != null
                            && !f.getDataEmissao().isBefore(inicioAno))
                    .map(f -> f.getValorFinal() != null ? f.getValorFinal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal despesaDiaria = pedidos.stream()
                    .filter(p -> p.getDataPedido() != null && p.getDataPedido().equals(hoje))
                    .map(p -> {
                        try {
                            return pedidoCompraService.calcularTotal(pedidoCompraService.listarItensDoPedido(p.getId()));
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal despesaMensal = pedidos.stream()
                    .filter(p -> p.getDataPedido() != null
                            && !p.getDataPedido().isBefore(inicioMes))
                    .map(p -> {
                        try {
                            return pedidoCompraService.calcularTotal(pedidoCompraService.listarItensDoPedido(p.getId()));
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal lucroLiquido = receitaMensal.subtract(despesaMensal);

            lblReceitaDiaria.setText("€ " + formatar(receitaDiaria));
            lblReceitaMensal.setText("€ " + formatar(receitaMensal));
            lblReceitaAnual.setText("€ " + formatar(receitaAnual));
            lblDespesaDiaria.setText("€ " + formatar(despesaDiaria));
            lblDespesaMensal.setText("€ " + formatar(despesaMensal));
            lblLucroLiquido.setText("€ " + formatar(lucroLiquido));

            if (graficoFinanceiro != null) {
                graficoFinanceiro.getData().clear();
                
                XYChart.Series<String, Number> seriesReceitas = new XYChart.Series<>();
                seriesReceitas.setName("Receitas");
                seriesReceitas.getData().add(new XYChart.Data<>("Mensal", receitaMensal));
                
                XYChart.Series<String, Number> seriesDespesas = new XYChart.Series<>();
                seriesDespesas.setName("Despesas");
                seriesDespesas.getData().add(new XYChart.Data<>("Mensal", despesaMensal));
                
                XYChart.Series<String, Number> seriesLucro = new XYChart.Series<>();
                seriesLucro.setName("Lucro");
                seriesLucro.getData().add(new XYChart.Data<>("Mensal", lucroLiquido));
                
                graficoFinanceiro.getData().addAll(seriesReceitas, seriesDespesas, seriesLucro);
            }

        } catch (Exception e) {
            mostrarErro("Erro ao carregar: " + e.getMessage());
        }
    }

    private String formatar(BigDecimal valor) {
        if (valor == null) return "0,00";
        return String.format("%.2f", valor.doubleValue()).replace(".", ",");
    }
}
