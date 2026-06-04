package controller.assistente;

import bll.FornecedorService;
import bll.MaterialService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Fornecedor;
import model.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AdicionarMaterialController {

    @FXML private TextField codigoInternoField;
    @FXML private TextField nomeField;
    @FXML private TextArea descricaoArea;
    @FXML private ComboBox<Fornecedor> fornecedorComboBox;
    @FXML private ComboBox<String> unidadeMedidaComboBox;
    @FXML private TextField quantidadeAtualField;
    @FXML private TextField quantidadeMinimaField;
    @FXML private TextField valorUnitarioField;
    @FXML private CheckBox ativoCheckBox;
    @FXML private Label erroLabel;
    @FXML private Button cancelarButton;
    @FXML private Button guardarButton;

    @Autowired private MaterialService materialService;
    @Autowired private FornecedorService fornecedorService;

    private Stage stage;
    private Runnable onMaterialGuardado;

    @FXML
    private void initialize() {
        aplicarFiltroInteiro(quantidadeAtualField);
        aplicarFiltroInteiro(quantidadeMinimaField);
        aplicarFiltroDecimal(valorUnitarioField);
        configurarFornecedores();
        configurarUnidadesMedida();

        if (ativoCheckBox != null) {
            ativoCheckBox.setSelected(true);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnMaterialGuardado(Runnable onMaterialGuardado) {
        this.onMaterialGuardado = onMaterialGuardado;
    }

    @FXML
    private void cancelar() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void guardarMaterial() {
        limparErro();

        try {
            Material material = new Material();
            material.setCodigoInterno(texto(codigoInternoField));
            material.setNome(texto(nomeField));
            material.setDescricao(texto(descricaoArea));
            material.setIdFornecedor(fornecedorComboBox != null ? fornecedorComboBox.getValue() : null);
            material.setUnidadeMedida(unidadeMedidaComboBox != null ? unidadeMedidaComboBox.getValue() : null);
            material.setQuantidadeAtual(parseInteiro(quantidadeAtualField));
            material.setQuantidadeMinima(parseInteiro(quantidadeMinimaField));
            material.setValorUnitario(parseDecimal(valorUnitarioField));
            material.setAtivo(ativoCheckBox == null || ativoCheckBox.isSelected());

            materialService.criarMaterial(material);

            cancelar();

            if (onMaterialGuardado != null) {
                onMaterialGuardado.run();
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Material adicionado");
            ok.setHeaderText(null);
            ok.setContentText("Material adicionado com sucesso.");
            ok.showAndWait();
        } catch (RuntimeException ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void configurarFornecedores() {
        if (fornecedorComboBox == null) return;

        fornecedorComboBox.setItems(FXCollections.observableArrayList(fornecedorService.listarTodos()));
        fornecedorComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Fornecedor fornecedor) {
                if (fornecedor == null) return "";
                String nome = fornecedor.getNome() != null ? fornecedor.getNome().trim() : "";
                String ultimoNome = fornecedor.getUltimoNome() != null ? fornecedor.getUltimoNome().trim() : "";
                String completo = (nome + " " + ultimoNome).trim();
                return completo.isBlank() ? "Fornecedor " + fornecedor.getId() : completo;
            }

            @Override
            public Fornecedor fromString(String string) {
                return null;
            }
        });
    }

    private void configurarUnidadesMedida() {
        if (unidadeMedidaComboBox == null) return;

        unidadeMedidaComboBox.setItems(FXCollections.observableArrayList(
                "Unidade",
                "Caixa",
                "Pacote",
                "ml",
                "L",
                "g",
                "Par",
                "Kit"
        ));
    }

    private void aplicarFiltroInteiro(TextField campo) {
        if (campo == null) return;

        campo.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    private void aplicarFiltroDecimal(TextField campo) {
        if (campo == null) return;

        campo.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*([,.]\\d{0,2})?") ? change : null));
    }

    private String texto(TextField campo) {
        return campo == null || campo.getText() == null ? null : campo.getText().trim();
    }

    private String texto(TextArea campo) {
        return campo == null || campo.getText() == null ? null : campo.getText().trim();
    }

    private Integer parseInteiro(TextField campo) {
        String valor = texto(campo);
        return valor == null || valor.isBlank() ? 0 : Integer.parseInt(valor);
    }

    private BigDecimal parseDecimal(TextField campo) {
        String valor = texto(campo);
        if (valor == null || valor.isBlank()) {
            return BigDecimal.ZERO;
        }
        if (!valor.matches("\\d+([,.]\\d{1,2})?")) {
            throw new RuntimeException("O valor unitário deve ser um valor numérico válido.");
        }
        return new BigDecimal(valor.replace(',', '.'));
    }

    private void mostrarErro(String mensagem) {
        if (erroLabel != null) {
            erroLabel.setText(mensagem);
            erroLabel.setVisible(true);
            erroLabel.setManaged(true);
        }
    }

    private void limparErro() {
        if (erroLabel != null) {
            erroLabel.setText("");
            erroLabel.setVisible(false);
            erroLabel.setManaged(false);
        }
    }
}
