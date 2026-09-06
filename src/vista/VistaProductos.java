package vista;

import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class VistaProductos extends JPanel {
    private final BaseFrame frame;
    private final JPanel grid;
    private final JLabel headerTitle;
    private final JTextField searchField;

    public VistaProductos(BaseFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        searchField = new JTextField();
        headerTitle = new JLabel("Catálogo de Productos");

        add(createHeader(), BorderLayout.NORTH);

        grid = new JPanel(new GridLayout(0, 4, 20, 20));
        grid.setBackground(EstilosUI.FONDO);
        grid.setBorder(new EmptyBorder(25, 45, 25, 45));

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        mostrarTodosLosProductos();
    }

    public void mostrarTodosLosProductos() {
        headerTitle.setText("Todos los Productos");
        renderizarLista(Inventario.getInstancia().leerProductos());
    }

    public void filtrarPorCategoria(Categorias cat) {
        headerTitle.setText("Categoría: " + cat.name().replace("_", " "));
        renderizarLista(Inventario.getInstancia().filtrarPorCategoria(cat));
    }
    

    private void renderizarLista(List<Producto> lista) {
        grid.removeAll();

        if (lista.isEmpty()) {
            JLabel vacio = new JLabel("No se encontraron productos registrados.");
            vacio.setFont(EstilosUI.FONT_BOLD);
            grid.add(vacio);
        } else {
            for (Producto p : lista) {
                grid.add(createProductCard(p));
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(12, 25, 12, 25));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setOpaque(false);

        JButton btnVolver = EstilosUI.iconButton("☰");
        btnVolver.setToolTipText("Volver al Inicio");
        btnVolver.addActionListener(e -> frame.mostrarVista("INICIO"));
        left.add(btnVolver);

        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);
        left.add(headerTitle);
        header.add(left, BorderLayout.WEST);

        String placeholder = "Buscar...";
        searchField.setText(placeholder);
        searchField.setFont(EstilosUI.FONT_NORMAL);
        searchField.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent e) {
            if (searchField.getText().equals(placeholder)) {
                searchField.setText("");
                searchField.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent e) {
            if (searchField.getText().trim().isEmpty()) {
                searchField.setText(placeholder);
                searchField.setForeground(Color.GRAY);
                mostrarTodosLosProductos(); 
            }
        }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String texto = searchField.getText().trim();
                if (texto.isEmpty() || texto.equals(placeholder)) {
                    mostrarTodosLosProductos();
                } else {
                    buscarPorTexto(texto);
                }
            }
        });
        header.add(searchField, BorderLayout.CENTER);

        JButton cart = EstilosUI.iconButton("🛒");
        cart.addActionListener(e -> frame.mostrarVista("CARRO"));
        header.add(cart, BorderLayout.EAST);

        return header;
    }

    public void buscarPorTexto(String query) {
        headerTitle.setText("Resultados para: \"" + query + "\"");
        renderizarLista(Inventario.getInstancia().buscarPorNombre(query));
    }

    public void setTextoBuscador(String texto) {
        searchField.setText(texto);
        searchField.setForeground(Color.BLACK);
    }

    private JPanel createProductCard(Producto prod) {
        JPanel card = new JPanel();
        card.setBackground(EstilosUI.FONDO);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel boxColor = new JPanel();
        boxColor.setBackground(EstilosUI.getColorPorCategoria(prod.getCategoria()));
        boxColor.setMaximumSize(new Dimension(140, 80));
        boxColor.setPreferredSize(new Dimension(140, 80));
        boxColor.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(prod.getNombre());
        nameLabel.setFont(EstilosUI.FONT_BOLD);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("$" + (int) prod.getPrecio());
        priceLabel.setFont(EstilosUI.FONT_NORMAL);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stockLabel = new JLabel("Stock: " + prod.getStock());
        stockLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stockLabel.setForeground(Color.GRAY);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addBtn = EstilosUI.roundedButton("Agregar", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(120, 32));
        addBtn.addActionListener(e -> showQuantityDialog(prod));

        card.add(boxColor);
        card.add(Box.createVerticalStrut(8));
        card.add(nameLabel);
        card.add(priceLabel);
        card.add(stockLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(addBtn);

        return card;
    }

    private void showQuantityDialog(Producto prod) {
        JDialog dialog = new JDialog(frame, "Agregar " + prod.getNombre(), true);
        dialog.setSize(420, 230);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(EstilosUI.VERDE);
        JLabel title = new JLabel(" Seleccionar cantidad para " + prod.getNombre());
        title.setForeground(Color.WHITE);
        title.setFont(EstilosUI.FONT_BOLD);
        title.setBorder(new EmptyBorder(8, 12, 8, 12));
        top.add(title, BorderLayout.CENTER);
        dialog.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(EstilosUI.FONDO);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel("Cantidad:");
        lbl.setFont(EstilosUI.FONT_BOLD);
        JTextField txtUnits = new JTextField("1", 4);
        txtUnits.setHorizontalAlignment(JTextField.CENTER);
        txtUnits.setEditable(false);
        
        //Botones MAS Y MENOS
        
        JButton botonMenos = new JButton("-");
        JButton botonMas = new JButton("+");
        
        botonMenos.setPreferredSize(new Dimension(45, 32));
        botonMas.setPreferredSize(new Dimension(45, 32));

        botonMenos.setFocusPainted(false);
        botonMas.setFocusPainted(false);

        row.add(lbl);
        row.add(txtUnits);
        body.add(row);
        body.add(Box.createVerticalStrut(20));

        JButton btnConfirmar = EstilosUI.roundedButton("CONFIRMAR", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmar.addActionListener(e -> {
            try {
            String texto = txtUnits.getText().trim();

            if (texto.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe ingresar una cantidad.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cant = Integer.parseInt(texto);

            if (cant <= 0) {
                JOptionPane.showMessageDialog(dialog, "La cantidad debe ser mayor a 0.", "Cantidad Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean agregado = Inventario.getInstancia().agregarAlCarrito(prod, cant);

            if (agregado) {
                JOptionPane.showMessageDialog(dialog, "¡" + prod.getNombre() + " agregado al carrito!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "No se puede agregar: la cantidad solicitada (sumada a lo que ya tienes en el carro) supera el stock disponible (" + prod.getStock() + ").", 
                    "Stock Insuficiente", 
                    JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Por favor ingrese un número entero válido.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            txtUnits.setText("1");
        }
        });

        body.add(btnConfirmar);
        dialog.add(body, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}