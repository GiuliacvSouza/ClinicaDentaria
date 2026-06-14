package controller.assistente;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.PedidoCompra;
import model.enums.EstadoPedidoCompra;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConfirmarRececaoController {

    @FXML private Label lblTituloConfirmacao;

    private Stage   stage;
    private boolean confirmed = false;

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isConfirmed()      { return confirmed; }

    public void setPedido(PedidoCompra pedido) {
        if (pedido != null && lblTituloConfirmacao != null) {
            lblTituloConfirmacao.setText("Confirmar receção do Pedido #" + pedido.getId() + "?");
        }
    }

    @FXML
    private void confirmarRececao() {
        confirmed = true;
        if (stage != null) stage.close();
    }

    @FXML
    private void cancelar() {
        if (stage != null) stage.close();
    }
}
