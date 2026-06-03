package bll;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.Atendimento;
import model.AtendimentoProcedimento;
import model.Consulta;
import model.Fatura;
import model.Paciente;
import model.Pagamento;
import model.Procedimento;
import model.Utilizador;
import model.enums.EstadoFatura;
import model.enums.MetodoPagamento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class FaturaPdfService {

    private static final Color TEAL = new Color(0, 121, 107);
    private static final Color TEAL_LIGHT = new Color(232, 245, 242);
    private static final Color LIGHT_GRAY = new Color(241, 243, 245);
    private static final Color BORDER_GRAY = new Color(215, 220, 224);
    private static final Color DARK_GRAY = new Color(52, 58, 64);
    private static final Color TEXT_GRAY = new Color(73, 80, 87);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String NAO_DISPONIVEL = "N\u00e3o dispon\u00edvel";
    private static final String CLINICA_NOME = "Cl\u00ednica Dent\u00e1ria";
    private static final String CLINICA_TELEFONE = "Tel: +351 210 000 000";
    private static final String CLINICA_EMAIL = "contacto@clinicadentaria.pt";

    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 20, Font.BOLD, TEAL);
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 14, Font.BOLD, DARK_GRAY);
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD, TEAL);
    private static final Font FONT_LABEL = new Font(Font.HELVETICA, 10, Font.BOLD, DARK_GRAY);
    private static final Font FONT_TEXT = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_GRAY);
    private static final Font FONT_TABLE_HEADER = new Font(Font.HELVETICA, 9, Font.BOLD, DARK_GRAY);
    private static final Font FONT_TABLE_TEXT = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_GRAY);
    private static final Font FONT_TOTAL = new Font(Font.HELVETICA, 12, Font.BOLD, TEAL);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 8, Font.ITALIC, TEXT_GRAY);

    @Value("${app.faturas-dir:uploads/faturas}")
    private String faturasDir;

    private final PagamentoService pagamentoService;

    public FaturaPdfService(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    public String gerarPdfFatura(Fatura fatura) {
        try {
            garantirDiretorio();
            String caminhoFicheiro = gerarCaminhoFicheiro(fatura);
            gerarDocumentoPdf(fatura, caminhoFicheiro);
            return caminhoFicheiro;
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF da fatura: " + e.getMessage(), e);
        }
    }

    private void garantirDiretorio() throws IOException {
        Path diretorio = Paths.get(faturasDir);
        if (!Files.exists(diretorio)) {
            Files.createDirectories(diretorio);
        }
    }

    private String gerarCaminhoFicheiro(Fatura fatura) {
        String nomeArquivo = String.format("fatura_%d_%s.pdf",
                fatura != null && fatura.getId() != null ? fatura.getId() : 0,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")));
        return faturasDir + File.separator + nomeArquivo;
    }

    private void gerarDocumentoPdf(Fatura fatura, String caminhoFicheiro) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 45, 45, 42, 42);
        PdfWriter.getInstance(document, new FileOutputStream(caminhoFicheiro));
        document.open();

        adicionarCabecalho(document, fatura);
        adicionarDadosFatura(document, fatura);
        adicionarDadosPaciente(document, fatura);
        adicionarTabelaProcedimentos(document, fatura);
        adicionarTotais(document, fatura);
        adicionarDadosPagamento(document, fatura);
        adicionarRodape(document);

        document.close();
    }

    private void adicionarCabecalho(Document document, Fatura fatura) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{65, 35});

        PdfPCell left = semBorda();
        Paragraph nomeClinica = new Paragraph(CLINICA_NOME, FONT_TITLE);
        nomeClinica.setSpacingAfter(4);
        left.addElement(nomeClinica);
        left.addElement(new Paragraph(CLINICA_TELEFONE, FONT_TEXT));
        left.addElement(new Paragraph(CLINICA_EMAIL, FONT_TEXT));

        PdfPCell right = semBorda();
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph subtitulo = new Paragraph("Fatura / Recibo", FONT_SUBTITLE);
        subtitulo.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(subtitulo);

        Paragraph dataGeracao = new Paragraph("Gerado em: " + formatarDataHora(LocalDateTime.now()), FONT_TEXT);
        dataGeracao.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(dataGeracao);

        if (fatura != null && EstadoFatura.PAGA.equals(fatura.getEstado())) {
            PdfPTable selo = new PdfPTable(1);
            selo.setWidthPercentage(45);
            PdfPCell celulaSelo = new PdfPCell(new Phrase("PAGA", new Font(Font.HELVETICA, 11, Font.BOLD, TEAL)));
            celulaSelo.setHorizontalAlignment(Element.ALIGN_CENTER);
            celulaSelo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celulaSelo.setBackgroundColor(TEAL_LIGHT);
            celulaSelo.setBorderColor(TEAL);
            celulaSelo.setPadding(6);
            selo.addCell(celulaSelo);
            selo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            right.addElement(selo);
        }

        header.addCell(left);
        header.addCell(right);
        document.add(header);

        adicionarLinhaSeparadora(document, TEAL, 8, 12);
    }

    private void adicionarDadosFatura(Document document, Fatura fatura) throws DocumentException {
        adicionarTituloSecao(document, "Dados da Fatura");

        PdfPTable tabela = criarTabelaDados(3);
        adicionarCampo(tabela, "N\u00ba Fatura", fatura != null && fatura.getId() != null ? fatura.getId().toString() : "-");
        adicionarCampo(tabela, "Data de Emiss\u00e3o", fatura != null ? formatarData(fatura.getDataEmissao()) : NAO_DISPONIVEL);
        adicionarCampo(tabela, "Estado", fatura != null && fatura.getEstado() != null ? fatura.getEstado().toString() : NAO_DISPONIVEL);
        document.add(tabela);
        adicionarEspaco(document, 8);
    }

    private void adicionarDadosPaciente(Document document, Fatura fatura) throws DocumentException {
        adicionarTituloSecao(document, "Dados do Paciente");

        Utilizador utilizador = obterUtilizadorPaciente(fatura);
        PdfPTable tabela = criarTabelaDados(3);
        adicionarCampo(tabela, "Nome", obterNomePaciente(utilizador));
        adicionarCampo(tabela, "Email", valorOuNaoDisponivel(utilizador != null ? utilizador.getEmail() : null));
        adicionarCampo(tabela, "Telem\u00f3vel", valorOuNaoDisponivel(utilizador != null ? utilizador.getTelemovel() : null));
        document.add(tabela);
        adicionarEspaco(document, 8);
    }

    private void adicionarTabelaProcedimentos(Document document, Fatura fatura) throws DocumentException {
        adicionarTituloSecao(document, "Procedimentos / Servi\u00e7os");

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{52, 13, 17, 18});

        adicionarCabecalhoTabela(tabela, "Procedimento", Element.ALIGN_LEFT);
        adicionarCabecalhoTabela(tabela, "Quantidade", Element.ALIGN_CENTER);
        adicionarCabecalhoTabela(tabela, "Valor Unit\u00e1rio", Element.ALIGN_RIGHT);
        adicionarCabecalhoTabela(tabela, "Subtotal", Element.ALIGN_RIGHT);

        List<AtendimentoProcedimento> procedimentos = obterProcedimentos(fatura);
        if (procedimentos.isEmpty()) {
            PdfPCell vazio = new PdfPCell(new Phrase("Sem procedimentos registados.", FONT_TABLE_TEXT));
            vazio.setColspan(4);
            aplicarBordaDiscreta(vazio);
            vazio.setPadding(8);
            tabela.addCell(vazio);
        } else {
            for (AtendimentoProcedimento item : procedimentos) {
                Procedimento procedimento = item != null ? item.getProcedimento() : null;
                int quantidade = item != null && item.getQuantidade() != null ? item.getQuantidade() : 1;
                BigDecimal valorUnitario = procedimento != null && procedimento.getValor() != null
                        ? procedimento.getValor()
                        : BigDecimal.ZERO;
                BigDecimal subtotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));

                adicionarCelulaTabela(tabela, procedimento != null ? valorOuTraco(procedimento.getNome()) : "-", Element.ALIGN_LEFT);
                adicionarCelulaTabela(tabela, String.valueOf(quantidade), Element.ALIGN_CENTER);
                adicionarCelulaTabela(tabela, formatarMoeda(valorUnitario), Element.ALIGN_RIGHT);
                adicionarCelulaTabela(tabela, formatarMoeda(subtotal), Element.ALIGN_RIGHT);
            }
        }

        document.add(tabela);
        adicionarEspaco(document, 10);
    }

    private void adicionarTotais(Document document, Fatura fatura) throws DocumentException {
        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);
        wrapper.setWidths(new float[]{60, 40});
        wrapper.addCell(semBorda());

        PdfPTable totais = new PdfPTable(2);
        totais.setWidthPercentage(100);
        totais.setWidths(new float[]{52, 48});

        BigDecimal valorBase = obterValor(fatura != null ? fatura.getValorBase() : null);
        BigDecimal valorIva = obterValor(fatura != null ? fatura.getValorIva() : null);
        BigDecimal valorFinal = obterValor(fatura != null ? fatura.getValorFinal() : null);

        adicionarLinhaTotal(totais, "Valor Base", formatarMoeda(valorBase), false);
        if (valorIva.compareTo(BigDecimal.ZERO) > 0) {
            adicionarLinhaTotal(totais, "IVA", formatarMoeda(valorIva), false);
        }
        adicionarLinhaTotal(totais, "Valor Total", formatarMoeda(valorFinal), true);

        PdfPCell celulaTotais = new PdfPCell(totais);
        celulaTotais.setBorder(Rectangle.NO_BORDER);
        wrapper.addCell(celulaTotais);
        document.add(wrapper);
        adicionarEspaco(document, 10);
    }

    private void adicionarDadosPagamento(Document document, Fatura fatura) throws DocumentException {
        adicionarTituloSecao(document, "Dados do Pagamento");

        Pagamento pagamento = obterUltimoPagamento(fatura);
        PdfPTable tabela = criarTabelaDados(3);
        adicionarCampo(tabela, "Data de Pagamento", pagamento != null ? formatarData(pagamento.getDataPagamento()) : NAO_DISPONIVEL);
        adicionarCampo(tabela, "Valor Pago", pagamento != null ? formatarMoeda(pagamento.getValorPago()) : "-");
        adicionarCampo(tabela, "M\u00e9todo de Pagamento", pagamento != null ? obterNomeMetodoPagamento(pagamento.getMetodo()) : NAO_DISPONIVEL);
        document.add(tabela);
        adicionarEspaco(document, 14);
    }

    private void adicionarRodape(Document document) throws DocumentException {
        adicionarLinhaSeparadora(document, BORDER_GRAY, 8, 8);

        Paragraph linha1 = new Paragraph("Documento gerado automaticamente pelo sistema da Cl\u00ednica Dent\u00e1ria.", FONT_FOOTER);
        linha1.setAlignment(Element.ALIGN_CENTER);
        document.add(linha1);

        Paragraph linha2 = new Paragraph("Este documento \u00e9 v\u00e1lido como comprovativo interno de pagamento.", FONT_FOOTER);
        linha2.setAlignment(Element.ALIGN_CENTER);
        document.add(linha2);
    }

    private void adicionarTituloSecao(Document document, String titulo) throws DocumentException {
        Paragraph secao = new Paragraph(titulo, FONT_SECTION);
        secao.setSpacingBefore(6);
        secao.setSpacingAfter(6);
        document.add(secao);
    }

    private PdfPTable criarTabelaDados(int colunas) throws DocumentException {
        PdfPTable tabela = new PdfPTable(colunas);
        tabela.setWidthPercentage(100);
        return tabela;
    }

    private void adicionarCampo(PdfPTable tabela, String label, String valor) {
        PdfPCell celula = new PdfPCell();
        celula.setBorderColor(BORDER_GRAY);
        celula.setPadding(7);
        celula.addElement(new Phrase(label, FONT_LABEL));
        celula.addElement(new Phrase(valorOuNaoDisponivel(valor), FONT_TEXT));
        tabela.addCell(celula);
    }

    private void adicionarCabecalhoTabela(PdfPTable tabela, String texto, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, FONT_TABLE_HEADER));
        celula.setHorizontalAlignment(alinhamento);
        celula.setBackgroundColor(LIGHT_GRAY);
        celula.setBorderColor(BORDER_GRAY);
        celula.setPadding(7);
        tabela.addCell(celula);
    }

    private void adicionarCelulaTabela(PdfPTable tabela, String texto, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(valorOuTraco(texto), FONT_TABLE_TEXT));
        celula.setHorizontalAlignment(alinhamento);
        aplicarBordaDiscreta(celula);
        celula.setPadding(7);
        tabela.addCell(celula);
    }

    private void adicionarLinhaTotal(PdfPTable tabela, String label, String valor, boolean destaque) {
        Font fonte = destaque ? FONT_TOTAL : FONT_LABEL;
        Color fundo = destaque ? TEAL_LIGHT : Color.WHITE;

        PdfPCell celulaLabel = new PdfPCell(new Phrase(label + ":", fonte));
        celulaLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaLabel.setBackgroundColor(fundo);
        aplicarBordaDiscreta(celulaLabel);
        celulaLabel.setPadding(7);
        tabela.addCell(celulaLabel);

        PdfPCell celulaValor = new PdfPCell(new Phrase(valor, fonte));
        celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaValor.setBackgroundColor(fundo);
        aplicarBordaDiscreta(celulaValor);
        celulaValor.setPadding(7);
        tabela.addCell(celulaValor);
    }

    private PdfPCell semBorda() {
        PdfPCell celula = new PdfPCell();
        celula.setBorder(Rectangle.NO_BORDER);
        return celula;
    }

    private void aplicarBordaDiscreta(PdfPCell celula) {
        celula.setBorderColor(BORDER_GRAY);
        celula.setBorderWidth(0.5f);
    }

    private void adicionarEspaco(Document document, float altura) throws DocumentException {
        Paragraph espaco = new Paragraph(" ");
        espaco.setLeading(altura);
        document.add(espaco);
    }

    private void adicionarLinhaSeparadora(Document document, Color cor, float espacoAntes, float espacoDepois) throws DocumentException {
        PdfPTable linha = new PdfPTable(1);
        linha.setWidthPercentage(100);
        linha.setSpacingBefore(espacoAntes);
        linha.setSpacingAfter(espacoDepois);

        PdfPCell celula = new PdfPCell(new Phrase(" "));
        celula.setFixedHeight(1);
        celula.setBorder(Rectangle.BOTTOM);
        celula.setBorderColor(cor);
        celula.setBorderWidth(0.8f);
        celula.setPadding(0);
        linha.addCell(celula);
        document.add(linha);
    }

    private Utilizador obterUtilizadorPaciente(Fatura fatura) {
        try {
            Atendimento atendimento = fatura != null ? fatura.getIdAtendimento() : null;
            Consulta consulta = atendimento != null ? atendimento.getIdConsulta() : null;
            Paciente paciente = consulta != null ? consulta.getIdPaciente() : null;
            return paciente != null ? paciente.getUtilizador() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String obterNomePaciente(Utilizador utilizador) {
        if (utilizador == null) {
            return NAO_DISPONIVEL;
        }

        String primeiroNome = utilizador.getPrimeiroNome() != null ? utilizador.getPrimeiroNome().trim() : "";
        String ultimoNome = utilizador.getUltimoNome() != null ? utilizador.getUltimoNome().trim() : "";
        String nomeCompleto = (primeiroNome + " " + ultimoNome).trim();
        return nomeCompleto.isEmpty() ? NAO_DISPONIVEL : nomeCompleto;
    }

    private List<AtendimentoProcedimento> obterProcedimentos(Fatura fatura) {
        try {
            Atendimento atendimento = fatura != null ? fatura.getIdAtendimento() : null;
            List<AtendimentoProcedimento> procedimentos = atendimento != null ? atendimento.getProcedimentos() : null;
            return procedimentos != null ? procedimentos : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private Pagamento obterUltimoPagamento(Fatura fatura) {
        try {
            if (fatura == null || fatura.getId() == null) {
                return null;
            }

            List<Pagamento> pagamentos = pagamentoService.listarPorFatura(fatura.getId());
            if (pagamentos == null || pagamentos.isEmpty()) {
                return null;
            }
            return pagamentos.get(pagamentos.size() - 1);
        } catch (Exception e) {
            return null;
        }
    }

    private String obterNomeMetodoPagamento(MetodoPagamento metodo) {
        if (metodo == null) {
            return NAO_DISPONIVEL;
        }
        return switch (metodo) {
            case DINHEIRO -> "Dinheiro";
            case CARTAO -> "Cart\u00e3o";
            case TRANSFERENCIA -> "Transfer\u00eancia";
            case MBWAY -> "MB Way";
        };
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(DATE_FORMAT) : NAO_DISPONIVEL;
    }

    private String formatarDataHora(LocalDateTime dataHora) {
        return dataHora != null ? dataHora.format(DATE_TIME_FORMAT) : NAO_DISPONIVEL;
    }

    private String formatarMoeda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(new Locale("pt", "PT"));
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return "\u20ac" + formato.format(obterValor(valor).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal obterValor(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private String valorOuNaoDisponivel(String valor) {
        return valor != null && !valor.trim().isEmpty() ? valor.trim() : NAO_DISPONIVEL;
    }

    private String valorOuTraco(String valor) {
        return valor != null && !valor.trim().isEmpty() ? valor.trim() : "-";
    }
}
