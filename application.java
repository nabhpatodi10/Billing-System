import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class application {

    private static String jdbcURL = "jdbc:mysql://localhost:3306/";
    private static String dbName = "billing_system";
    private static String dbUser = "root";
    private static String dbPassword = "nabh1005";
    private static Connection connection;

    private static void createDatabase(Connection connection, String dbName) throws Exception {
        Statement createDbStatement = connection.createStatement();
        String createDbQuery = "CREATE DATABASE IF NOT EXISTS " + dbName;
        createDbStatement.executeUpdate(createDbQuery);
    }

    private static void createLoginTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS login ( Company_Name VARCHAR(100) BINARY NOT NULL, User_Name VARCHAR(50) BINARY NOT NULL, Paswrd VARCHAR(50) BINARY NOT NULL )";
        createTableStatement.executeUpdate(createTableQuery);
    }

	private static void createCustomersTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS customers ( Customer_Id INT AUTO_INCREMENT PRIMARY KEY, Customer_Name VARCHAR(50) NOT NULL, Customer_Company VARCHAR(100), GSTIN VARCHAR(15), Phone_Number VARCHAR(13), Email VARCHAR(50), Address VARCHAR(200) )";
        createTableStatement.executeUpdate(createTableQuery);
	}

    private static void createItemStockTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS item_stock ( Item_Id INT AUTO_INCREMENT PRIMARY KEY, Item_Name VARCHAR(100) UNIQUE NOT NULL, Item_Stock INT DEFAULT 0)";
        createTableStatement.executeUpdate(createTableQuery);
    }

    private static void createOrdersTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS orders ( Order_Id INT AUTO_INCREMENT PRIMARY KEY, Order_Date DATE DEFAULT (CURDATE()), Customer_Id INT NOT NULL, Order_Status VARCHAR(20) NOT NULL, FOREIGN KEY (Customer_Id) REFERENCES customers(Customer_Id) )";
        createTableStatement.executeUpdate(createTableQuery);
    }

    private static void createBillsTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS bills ( Bill_Id INT PRIMARY KEY, Order_Id INT NOT NULL, Date DATE DEFAULT (CURDATE()), Time TIME DEFAULT (CURTIME()), Customer_Id INT NOT NULL, Shipping_Address VARCHAR(200) NOT NULL, tax_percentage INT, User_Name VARCHAR(50) NOT NULL, FOREIGN KEY (Order_Id) REFERENCES orders(Order_Id), FOREIGN KEY (Customer_Id) REFERENCES customers(Customer_Id) )";
        createTableStatement.executeUpdate(createTableQuery);
    }

    private static void createPaymentsTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS payments (Order_Id INT NOT NULL, Bill_Id INT, Payment_Method VARCHAR(25) NOT NULL, Payment_Status VARCHAR(30) NOT NULL, FOREIGN KEY (Order_Id) REFERENCES orders(Order_Id), FOREIGN KEY (Bill_Id) REFERENCES bills(Bill_Id) )";
        createTableStatement.executeUpdate(createTableQuery);
    }

    private static void createItemsTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS items (Order_Id INT NOT NULL, Bill_Id INT, Item_Id INT NOT NULL, Quantity INT NOT NULL, Rate INT NOT NULL, FOREIGN KEY (Order_Id) REFERENCES orders(Order_Id), FOREIGN KEY (Bill_Id) REFERENCES bills(Bill_Id), FOREIGN KEY (Item_Id) REFERENCES item_stock(Item_Id) );";
        createTableStatement.executeUpdate(createTableQuery);
    }

    private static void createCompanyDetailsTable(Connection connection) throws Exception {
        Statement createTableStatement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS company_details (GSTIN VARCHAR(15) UNIQUE NOT NULL, Office_Address VARCHAR(300) NOT NULL, Email VARCHAR(100), Website VARCHAR(75), Phone_Number VARCHAR(20) NOT NULL);";
        createTableStatement.executeUpdate(createTableQuery);
    }

    private static boolean isLoginTableEmpty(Connection connection) throws Exception {
        String query = "SELECT COUNT(*) FROM login";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        int count = resultSet.getInt(1);
        return count == 0;
    }

    private static void insertUserData(Connection connection, String companyName, String userName, String password) throws Exception {

        createDatabase(connection, companyName);

        String insertQuery = "INSERT INTO login (Company_Name, User_Name, Paswrd) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setString(1, companyName);
        preparedStatement.setString(2, userName);
        preparedStatement.setString(3, password);
        preparedStatement.executeUpdate();

        connection.setCatalog(companyName);

		createCustomersTable(connection);
        createItemStockTable(connection);
        createOrdersTable(connection);
        createBillsTable(connection);
        createPaymentsTable(connection);
        createItemsTable(connection);
        createCompanyDetailsTable(connection);
    }

    private static boolean checkCredentials(Connection connection, String companyName, String userName, String password) throws Exception {
        String query = "SELECT * FROM login WHERE Company_Name = ? AND User_Name = ? AND Paswrd = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, companyName);
        preparedStatement.setString(2, userName);
        preparedStatement.setString(3, password);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    private static JFrame frame = new JFrame("Billing Software");
    private static JMenuBar menuBar = new JMenuBar();
    private static JMenu file = new JMenu("File");
    private static JMenuItem exit = new JMenuItem("Exit");
    private static JMenuItem logout = new JMenuItem("Logout");
    private static JMenuItem addusermenu = new JMenuItem("Add New User");
    private static JMenuItem homepage = new JMenuItem("Home Page");
    private static JMenu view = new JMenu("View");
    private static JMenuItem viewcustomer = new JMenuItem("View Customers");
    private static JMenuItem viewbill = new JMenuItem("View Bills");
    private static JMenuItem vieworder = new JMenuItem("View Orders");
    private static JMenu add = new JMenu("Add");
    private static JMenuItem addcustomer = new JMenuItem("Add Customer");
    private static JMenuItem addorder = new JMenuItem("Add Order");
    private static JMenuItem makebill = new JMenuItem("Make Bill");
    private static JMenuItem additem = new JMenuItem("Add Item");
    private static JMenu edit = new JMenu("Edit");
    private static JMenuItem editpayment = new JMenuItem("Edit Payment");
    private static JMenuItem editorder = new JMenuItem("Edit Order");
    private static JMenuItem editbill = new JMenuItem("Edit Bill");
    private static JMenuItem editstock = new JMenuItem("Edit Stock");
    private static String username;

    private static void edit_item(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel itemname = new JLabel("Item Name:");
        itemname.setForeground(new Color(0x293dff));
        itemname.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemname.setVerticalAlignment(JLabel.CENTER);
        itemname.setHorizontalAlignment(JLabel.LEFT);
        itemname.setBounds(700, 70, 105, 35);

        JTextField itemnametext = new JTextField();
        itemnametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemnametext.setBackground(new Color(0x99ccff));
        itemnametext.setForeground(new Color(0x000099));
        itemnametext.setCaretColor(new Color(0x000099));
        itemnametext.setBounds(815, 70, 200, 35);

        JLabel itemquantity = new JLabel("Item Quantity:");
        itemquantity.setForeground(new Color(0x293dff));
        itemquantity.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemquantity.setVerticalAlignment(JLabel.CENTER);
        itemquantity.setHorizontalAlignment(JLabel.LEFT);
        itemquantity.setBounds(732, 120, 135, 35);

        JTextField itemquantitytext = new JTextField();
        itemquantitytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemquantitytext.setBackground(new Color(0x99ccff));
        itemquantitytext.setForeground(new Color(0x000099));
        itemquantitytext.setCaretColor(new Color(0x000099));
        itemquantitytext.setBounds(877, 120, 100, 35);

        JButton additembutton = new JButton("Edit Stock");
        additembutton.setFocusable(false);
        additembutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        additembutton.setBackground(new Color(0x4c73ff));
        additembutton.setForeground(new Color(0x000099));
        additembutton.setBounds(785, 210, 130, 35);
        additembutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Object itemnamevar = itemnametext.getText();
                Object itemstockvar = itemquantitytext.getText();

                if(itemnamevar!=null && itemstockvar!=null && itemnamevar!="" && itemstockvar!=""){

                    try{

                        String additemquery = "UPDATE item_stock SET Item_Stock = " + itemstockvar + " WHERE Item_Name = '" + itemnamevar + "'";
                        Statement statement = connection.createStatement();
                        statement.execute(additemquery);
                        JOptionPane.showMessageDialog(null, "Stock Edited", "Information", JOptionPane.INFORMATION_MESSAGE);

                    }catch(SQLException e1){
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not Edit Stock!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }catch(Exception e2){
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not Edit Stock!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                }else{
                    JOptionPane.showMessageDialog(null, "Enter Valid Data!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(itemname);
        frame.add(itemnametext);
        frame.add(itemquantity);
        frame.add(itemquantitytext);
        frame.add(additembutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void edit_bills(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel date = new JLabel("Date:");
        date.setForeground(new Color(0x293dff));
        date.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        date.setVerticalAlignment(JLabel.CENTER);
        date.setHorizontalAlignment(JLabel.LEFT);
        date.setBounds(471, 50, 50, 35);

        JDateChooser dateentry = new JDateChooser();
        dateentry.setDateFormatString("yyyy-MM-dd");
        JTextField datetext = ((JTextField) dateentry.getDateEditor().getUiComponent());
        datetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        datetext.setBackground(new Color(0x99ccff));
        datetext.setForeground(new Color(0x000099));
        datetext.setCaretColor(new Color(0x000099));

        dateentry.setBounds(531, 50, 150, 35);

        JLabel time = new JLabel("Time:");
        time.setForeground(new Color(0x293dff));
        time.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        time.setVerticalAlignment(JLabel.CENTER);
        time.setHorizontalAlignment(JLabel.LEFT);
        time.setBounds(691, 50, 50, 35);

        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setBackground(new Color(0x99ccff));
        timeSpinner.setForeground(new Color(0x000099));
        timeSpinner.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        timeSpinner.setBounds(751, 50, 150, 35);

        JLabel company = new JLabel("Customer Company:");
        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.CENTER);
        company.setHorizontalAlignment(JLabel.LEFT);
        company.setBounds(50, 100, 185, 35);

        JComboBox<String> companytext = new JComboBox<>();

        JComboBox<String> nametext = new JComboBox<>();

        java.util.List<String> companylist = new ArrayList<>();

        java.util.List<String> namelist = new ArrayList<>();

        JTextField billingAddtext = new JTextField();

        JTextField shippingAddtext = new JTextField();

        companytext.addItem("Select:");

        nametext.addItem("Select:");

        try{

            String companyquery = "SELECT DISTINCT Customer_Company FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(companyquery);
            while(resultSet.next()){
                companylist.add(resultSet.getString("Customer_Company"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : companylist){
            companytext.addItem(i);
        }

        companytext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a){
                
                nametext.removeAllItems();
                nametext.addItem("Select:");
                java.util.List<String> namelist2 = new ArrayList<>();
                try{
                    
                    String companyquery = "";
                    String addressquery = "";
                    String addressTemp = "";
                    if(companytext.getSelectedItem()=="" || companytext.getSelectedItem()==null){
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers";
                    } else{
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers WHERE Customer_Company = '" + companytext.getSelectedItem() + "'";
                        addressquery = "SELECT Address from customers WHERE Customer_Company = '" + companytext.getSelectedItem() + "'";
                    }
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(companyquery);
                    while(resultSet.next()){
                        namelist2.add(resultSet.getString("Customer_Name"));
                    }

                    ResultSet addressResult = statement.executeQuery(addressquery);
                    while (addressResult.next()) {
                        addressTemp = addressResult.getString("Address");
                    }

                    billingAddtext.setText(addressTemp);
                    shippingAddtext.setText(addressTemp);

                } catch(SQLException e){
                    e.printStackTrace();
                } catch(Exception e1){
                    e1.printStackTrace();
                }

                for(String i : namelist2){
                    nametext.addItem(i);
                }
            }
        });

        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));

        JScrollPane companyscroll = new JScrollPane(companytext);
        companyscroll.setPreferredSize(new Dimension(400, 45));
        companyscroll.setBounds(245, 100, 400, 45);

        JLabel name = new JLabel("Customer Name:");
        name.setForeground(new Color(0x293dff));
        name.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        name.setVerticalAlignment(JLabel.CENTER);
        name.setHorizontalAlignment(JLabel.LEFT);
        name.setBounds(50, 160, 155, 35);

        try{

            String namequery = "SELECT DISTINCT Customer_Name FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(namequery);
            while(resultSet.next()){
                namelist.add(resultSet.getString("Customer_Name"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : namelist){
            nametext.addItem(i);
        }

        nametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        nametext.setBackground(new Color(0x99ccff));
        nametext.setForeground(new Color(0x000099));

        JScrollPane namescroll = new JScrollPane(nametext);
        namescroll.setPreferredSize(new Dimension(400, 45));
        namescroll.setBounds(215, 160, 400, 45);

        JLabel billingAdd = new JLabel("Billing Address:");
        billingAdd.setForeground(new Color(0x293dff));
        billingAdd.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billingAdd.setVerticalAlignment(JLabel.CENTER);
        billingAdd.setHorizontalAlignment(JLabel.LEFT);
        billingAdd.setBounds(50, 220, 145, 35);

        billingAddtext.setBackground(new Color(0x99ccff));
        billingAddtext.setForeground(new Color(0x000099));
        billingAddtext.setCaretColor(new Color(0x000099));
        billingAddtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billingAddtext.setEditable(false);
        billingAddtext.setBounds(205, 220, 500, 35);

        JLabel shippingAdd = new JLabel("Shipping Address:");
        shippingAdd.setForeground(new Color(0x293dff));
        shippingAdd.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        shippingAdd.setVerticalAlignment(JLabel.CENTER);
        shippingAdd.setHorizontalAlignment(JLabel.LEFT);
        shippingAdd.setBounds(50, 270, 165, 35);

        shippingAddtext.setBackground(new Color(0x99ccff));
        shippingAddtext.setForeground(new Color(0x000099));
        shippingAddtext.setCaretColor(new Color(0x000099));
        shippingAddtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        shippingAddtext.setBounds(225, 270, 500, 35);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Item Name");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("Rate");
        tableModel.addColumn("Price");
        tableModel.addColumn("Tax");

        JTable orderTable = new JTable(tableModel);
        orderTable.setBackground(new Color(0x99ccff));

        JScrollPane tableScrollPane = new JScrollPane(orderTable);
        tableScrollPane.setBounds(50, 365, 1000, 400);

        JLabel paymentmethod = new JLabel("Payment Method:");
        paymentmethod.setForeground(new Color(0x293dff));
        paymentmethod.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethod.setVerticalAlignment(JLabel.CENTER);
        paymentmethod.setHorizontalAlignment(JLabel.LEFT);
        paymentmethod.setBounds(50, 785, 165, 35);

        JTextField paymentmethodtext = new JTextField();
        paymentmethodtext.setBackground(new Color(0x99ccff));
        paymentmethodtext.setForeground(new Color(0x000099));
        paymentmethodtext.setCaretColor(new Color(0x000099));
        paymentmethodtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethodtext.setBounds(225, 785, 200, 35);

        JLabel paymentstatus = new JLabel("Payment Status:");
        paymentstatus.setForeground(new Color(0x293dff));
        paymentstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatus.setVerticalAlignment(JLabel.CENTER);
        paymentstatus.setHorizontalAlignment(JLabel.LEFT);
        paymentstatus.setBounds(460, 785, 155, 35);

        JComboBox<String> paymentstatustext = new JComboBox<>();
        paymentstatustext.addItem("Pending");
        paymentstatustext.addItem("Partial Payment");
        paymentstatustext.addItem("Complete Payment");
        paymentstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatustext.setBackground(new Color(0x99ccff));
        paymentstatustext.setForeground(new Color(0x000099));
        paymentstatustext.setBounds(625, 785, 200, 35);

        JLabel tax = new JLabel("Tax Percentage:");
        tax.setForeground(new Color(0x293dff));
        tax.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        tax.setVerticalAlignment(JLabel.CENTER);
        tax.setHorizontalAlignment(JLabel.LEFT);
        tax.setBounds(50, 320, 152, 35);

        JComboBox<Integer> taxtext = new JComboBox<>();
        taxtext.addItem(5);
        taxtext.addItem(12);
        taxtext.addItem(18);
        taxtext.addItem(28);
        taxtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        taxtext.setBackground(new Color(0x99ccff));
        taxtext.setForeground(new Color(0x000099));
        taxtext.setBounds(212, 320, 70, 35);

        taxtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                for(int row = 0; row < tableModel.getRowCount(); row++){
                    float price = (float)tableModel.getValueAt(row, 3);
                    tableModel.setValueAt(price*(int)taxtext.getSelectedItem()/100, row, 4);
                }
            }
        });

        JLabel orderid = new JLabel("Order ID:");
        orderid.setForeground(new Color(0x293dff));
        orderid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderid.setVerticalAlignment(JLabel.CENTER);
        orderid.setHorizontalAlignment(JLabel.LEFT);
        orderid.setBounds(251, 50, 85, 35);

        JTextField orderidtext = new JTextField();

        orderidtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{

                    String cutstomeridquery = "SELECT Customer_Id FROM orders WHERE Order_Id = " + orderidtext.getText();
                    Statement statement = connection.createStatement();
                    ResultSet customeridResultSet = statement.executeQuery(cutstomeridquery);
                    customeridResultSet.next();
                    int customerid = customeridResultSet.getInt("Customer_Id");

                    String customerquery = "SELECT Customer_Name, Customer_Company FROM customers WHERE Customer_Id = " + customerid;
                    ResultSet customerResultSet = statement.executeQuery(customerquery);
                    customerResultSet.next();
                    String customerName = customerResultSet.getString("Customer_Name");
                    String customerCompany = customerResultSet.getString("Customer_Company");

                    companytext.setSelectedItem(customerCompany);
                    nametext.setSelectedItem(customerName);

                    String paymentquery = "SELECT Payment_Method, Payment_Status FROM payments WHERE Order_Id = " + orderidtext.getText();
                    ResultSet paymentResultSet = statement.executeQuery(paymentquery);
                    paymentResultSet.next();
                    String payment_meth = paymentResultSet.getString("Payment_Method");
                    String payment_stat = paymentResultSet.getString("Payment_Status");

                    paymentmethodtext.setText(payment_meth);
                    paymentstatustext.setSelectedItem(payment_stat);

                    String items = "SELECT Item_Name, Quantity, Rate, Quantity*Rate AS Price FROM items WHERE Order_Id = " + orderidtext.getText();
                    ResultSet itemsResultSet = statement.executeQuery(items);
                    tableModel.setRowCount(0);
                    while(itemsResultSet.next()){
                        tableModel.addRow(new Object[]{itemsResultSet.getString("Item_Name"), itemsResultSet.getInt("Quantity"), itemsResultSet.getFloat("Rate"), itemsResultSet.getFloat("Price"), (float)itemsResultSet.getFloat("Price")*(int)taxtext.getSelectedItem()/100});
                    }

                    String addressTemp = "";
                    String addressquery = "SELECT Address from customers, orders WHERE orders.Order_Id = " + orderidtext.getText() + " AND orders.Customer_Id = customers.Customer_Id";
                    ResultSet addressResult = statement.executeQuery(addressquery);
                    while (addressResult.next()) {
                        addressTemp = addressResult.getString("Address");
                    }
                    billingAddtext.setText(addressTemp);
                    shippingAddtext.setText(addressTemp);

                }catch(SQLException e1){
                    e1.printStackTrace();
                }catch(Exception e2){
                    e2.printStackTrace();
                }
            }
        });

        orderidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderidtext.setBackground(new Color(0x99ccff));
        orderidtext.setForeground(new Color(0x000099));
        orderidtext.setCaretColor(new Color(0x000099));
        orderidtext.setBounds(346, 45, 100, 45);

        JLabel billid = new JLabel("Bill ID:");
        billid.setForeground(new Color(0x293dff));
        billid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billid.setVerticalAlignment(JLabel.CENTER);
        billid.setHorizontalAlignment(JLabel.LEFT);
        billid.setBounds(50, 50, 83, 35);

        JTextField billidtext = new JTextField();
        billidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billidtext.setBackground(new Color(0x99ccff));
        billidtext.setForeground(new Color(0x000099));
        billidtext.setCaretColor(new Color(0x000099));
        billidtext.setBounds(143, 50, 83, 35);

        billidtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try{

                    String billquery = "SELECT Order_Id, Date, Time FROM bills WHERE Bill_Id = " + billidtext.getText();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(billquery);
                    resultSet.next();
                    orderidtext.setText(resultSet.getString("Order_Id"));
                    orderidtext.setEditable(false);
                    datetext.setText(resultSet.getString("Date"));
                    timeSpinner.setValue(resultSet.getObject("Time"));

                    String cutstomeridquery = "SELECT Customer_Id FROM orders WHERE Order_Id = " + orderidtext.getText();
                    ResultSet customeridResultSet = statement.executeQuery(cutstomeridquery);
                    customeridResultSet.next();
                    int customerid = customeridResultSet.getInt("Customer_Id");

                    String customerquery = "SELECT Customer_Name, Customer_Company FROM customers WHERE Customer_Id = " + customerid;
                    ResultSet customerResultSet = statement.executeQuery(customerquery);
                    customerResultSet.next();
                    String customerName = customerResultSet.getString("Customer_Name");
                    String customerCompany = customerResultSet.getString("Customer_Company");

                    companytext.setSelectedItem(customerCompany);
                    nametext.setSelectedItem(customerName);

                    String paymentquery = "SELECT Payment_Method, Payment_Status FROM payments WHERE Order_Id = " + orderidtext.getText();
                    ResultSet paymentResultSet = statement.executeQuery(paymentquery);
                    paymentResultSet.next();
                    String payment_meth = paymentResultSet.getString("Payment_Method");
                    String payment_stat = paymentResultSet.getString("Payment_Status");

                    paymentmethodtext.setText(payment_meth);
                    paymentstatustext.setSelectedItem(payment_stat);

                    String items = "SELECT Item_Name, Quantity, Rate, Quantity*Rate AS Price FROM items, item_stock WHERE items.Item_Id = item_stock.Item_Id and Order_Id = " + orderidtext.getText();
                    ResultSet itemsResultSet = statement.executeQuery(items);
                    tableModel.setRowCount(0);
                    while(itemsResultSet.next()){
                        tableModel.addRow(new Object[]{itemsResultSet.getString("Item_Name"), itemsResultSet.getInt("Quantity"), itemsResultSet.getFloat("Rate"), itemsResultSet.getFloat("Price"), (float)itemsResultSet.getFloat("Price")*(int)taxtext.getSelectedItem()/100});
                    }

                    String addressTemp = "";
                    String addressquery = "SELECT Address from customers, bills WHERE bills.Bill_Id = " + billidtext.getText() + " AND bills.Customer_Id = customers.Customer_Id";
                    ResultSet addressResult = statement.executeQuery(addressquery);
                    while (addressResult.next()) {
                        addressTemp = addressResult.getString("Address");
                    }
                    billingAddtext.setText(addressTemp);
                    shippingAddtext.setText(addressTemp);

                }catch(SQLException e1){
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Order ID not Found!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }catch(Exception e2){
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Order ID not Found!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        JButton addorderbutton = new JButton("Edit Bill");
        addorderbutton.setFocusable(false);
        addorderbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        addorderbutton.setBackground(new Color(0x4c73ff));
        addorderbutton.setForeground(new Color(0x000099));
        addorderbutton.setBounds(1060, 780, 120, 35);
        addorderbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Object order_id = orderidtext.getText();
                java.util.Date selecteddate = dateentry.getDate();
                java.util.Date selectedTime = (Date) timeSpinner.getValue();
                Object companyname = companytext.getSelectedItem();
                Object customername = nametext.getSelectedItem();
                String shippingAddress = shippingAddtext.getText();
                String paymethod = paymentmethodtext.getText();
                Object paystatus = paymentstatustext.getSelectedItem();
                Object taxperc = taxtext.getSelectedItem();

                if(billidtext.getText().length()!=0 && shippingAddress.length()!=0 && shippingAddress!=null && selecteddate!=null && selectedTime!=null && companyname!=null && customername!=null && paymethod!=null && paystatus!=null && taxperc!=null && companyname!="" && customername!="" && shippingAddress!="" && paymethod!="" && paystatus!="" && taxperc!="" && paymethod.length()!=0){

                    try{

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = sdf.format(selecteddate);

                        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
                        String formattedTime = stf.format(selectedTime);

                        Statement statement = connection.createStatement();

                        String insertOrderQuery = "UPDATE orders SET Order_Status = 'Complete' WHERE Order_Id = " + order_id;
                        statement.execute(insertOrderQuery);

                        int Temp = 0;
                        String customerIdQuery = "SELECT * from customers WHERE Customer_Name = '" + customername + "' AND Customer_Company = '" + companyname + "'";
                        ResultSet customerIdResult = statement.executeQuery(customerIdQuery);
                        Temp = 0;
                        while (customerIdResult.next()) {
                            Temp = customerIdResult.getInt("Customer_Id");     
                        }
                        int customerId = Temp;

                        String insertBillQuery = "UPDATE bills SET Order_Id =" + order_id + ", Date ='" + java.sql.Date.valueOf(formattedDate) + "', Time = '" + java.sql.Time.valueOf(formattedTime) + "', Customer_Id = '" + customerId + "', Shipping_Address = '" + shippingAddress + "', tax_percentage = " + taxperc + " WHERE Bill_Id = " + billidtext.getText();
                        statement.execute(insertBillQuery);

                        String insertPaymentQuery = "UPDATE payments set Payment_Method = '" + paymethod + "', Payment_Status = '" + paystatus + "' WHERE Bill_Id = " + billidtext.getText();
                        statement.execute(insertPaymentQuery);

                        JOptionPane.showMessageDialog(null, "Bill Details Added", "Information", JOptionPane.INFORMATION_MESSAGE);

                    } catch(SQLException e1){
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Bill Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch(Exception e2){
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Bill Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else{
                    JOptionPane.showMessageDialog(null, "Enter Valid Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(billid);
        frame.add(billidtext);
        frame.add(orderid);
        frame.add(orderidtext);
        frame.add(date);
        frame.add(dateentry);
        frame.add(time);
        frame.add(timeSpinner);
        frame.add(company);
        frame.add(companyscroll);
        frame.add(name);
        frame.add(namescroll);
        frame.add(billingAdd);
        frame.add(billingAddtext);
        frame.add(shippingAdd);
        frame.add(shippingAddtext);
        frame.add(tableScrollPane);
        frame.add(paymentmethod);
        frame.add(paymentmethodtext);
        frame.add(paymentstatus);
        frame.add(paymentstatustext);
        frame.add(tax);
        frame.add(taxtext);
        frame.add(addorderbutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void edit_orders(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel date = new JLabel("Date:");
        date.setForeground(new Color(0x293dff));
        date.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        date.setVerticalAlignment(JLabel.CENTER);
        date.setHorizontalAlignment(JLabel.LEFT);
        date.setBounds(250, 50, 50, 35);

        JDateChooser dateentry = new JDateChooser();
        dateentry.setDateFormatString("yyyy-MM-dd");
        JTextField datetext = ((JTextField) dateentry.getDateEditor().getUiComponent());
        datetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        datetext.setBackground(new Color(0x99ccff));
        datetext.setForeground(new Color(0x000099));
        datetext.setCaretColor(new Color(0x000099));

        dateentry.setBounds(310, 50, 150, 35);

        JLabel company = new JLabel("Customer Company:");
        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.CENTER);
        company.setHorizontalAlignment(JLabel.LEFT);
        company.setBounds(50, 100, 185, 35);

        JComboBox<String> companytext = new JComboBox<>();

        JComboBox<String> nametext = new JComboBox<>();

        java.util.List<String> companylist = new ArrayList<>();

        java.util.List<String> namelist = new ArrayList<>();

        companytext.addItem("Select:");

        nametext.addItem("Select:");

        try{

            String companyquery = "SELECT DISTINCT Customer_Company FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(companyquery);
            while(resultSet.next()){
                companylist.add(resultSet.getString("Customer_Company"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : companylist){
            companytext.addItem(i);
        }

        companytext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a){
                
                nametext.removeAllItems();
                nametext.addItem("Select:");
                java.util.List<String> namelist2 = new ArrayList<>();
                try{
                    
                    String companyquery = "";
                    if(companytext.getSelectedItem()=="" || companytext.getSelectedItem()==null){
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers";
                    } else{
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers WHERE Customer_Company = '" + companytext.getSelectedItem() + "'";
                    }
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(companyquery);
                    while(resultSet.next()){
                        namelist2.add(resultSet.getString("Customer_Name"));
                    }

                } catch(SQLException e){
                    e.printStackTrace();
                } catch(Exception e1){
                    e1.printStackTrace();
                }

                for(String i : namelist2){
                    nametext.addItem(i);
                }
            }
        });

        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));

        JScrollPane companyscroll = new JScrollPane(companytext);
        companyscroll.setPreferredSize(new Dimension(400, 45));
        companyscroll.setBounds(245, 100, 400, 45);

        JLabel name = new JLabel("Customer Name:");
        name.setForeground(new Color(0x293dff));
        name.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        name.setVerticalAlignment(JLabel.CENTER);
        name.setHorizontalAlignment(JLabel.LEFT);
        name.setBounds(50, 160, 155, 35);

        try{

            String namequery = "SELECT DISTINCT Customer_Name FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(namequery);
            while(resultSet.next()){
                namelist.add(resultSet.getString("Customer_Name"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : namelist){
            nametext.addItem(i);
        }

        nametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        nametext.setBackground(new Color(0x99ccff));
        nametext.setForeground(new Color(0x000099));

        JScrollPane namescroll = new JScrollPane(nametext);
        namescroll.setPreferredSize(new Dimension(400, 45));
        namescroll.setBounds(215, 160, 400, 45);

        DefaultTableModel model1 = new DefaultTableModel();
        model1.addColumn("Item Name");
        model1.addColumn("Item Quantity");
        model1.addColumn("Item Rate");

        JTable orderTable = new JTable(model1);
        orderTable.setBackground(new Color(0x99ccff));

        orderTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
        orderTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        orderTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));

        JScrollPane tableScrollPane = new JScrollPane(orderTable);
        tableScrollPane.setBounds(50, 220, 1000, 400);

        JButton addButton = new JButton("Add Row");
        addButton.setFocusable(false);
        addButton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        addButton.setBackground(new Color(0x4c73ff));
        addButton.setForeground(new Color(0x000099));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model1.addRow(new Object[]{"", "", ""});
            }
        });
        addButton.setBounds(1060, 540, 110, 35);

        JButton delButton = new JButton("Delete Row");
        delButton.setFocusable(false);
        delButton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        delButton.setBackground(new Color(0x4c73ff));
        delButton.setForeground(new Color(0x000099));
        delButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int rows = model1.getRowCount();
                model1.setRowCount(rows-1);
            }
        });
        delButton.setBounds(1060, 585, 130, 35);

        JLabel paymentmethod = new JLabel("Payment Method:");
        paymentmethod.setForeground(new Color(0x293dff));
        paymentmethod.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethod.setVerticalAlignment(JLabel.CENTER);
        paymentmethod.setHorizontalAlignment(JLabel.LEFT);
        paymentmethod.setBounds(50, 640, 165, 35);

        JTextField paymentmethodtext = new JTextField();
        paymentmethodtext.setBackground(new Color(0x99ccff));
        paymentmethodtext.setForeground(new Color(0x000099));
        paymentmethodtext.setCaretColor(new Color(0x000099));
        paymentmethodtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethodtext.setBounds(225, 640, 200, 35);

        JLabel paymentstatus = new JLabel("Payment Status:");
        paymentstatus.setForeground(new Color(0x293dff));
        paymentstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatus.setVerticalAlignment(JLabel.CENTER);
        paymentstatus.setHorizontalAlignment(JLabel.LEFT);
        paymentstatus.setBounds(460, 640, 155, 35);

        JComboBox<String> paymentstatustext = new JComboBox<>();
        paymentstatustext.addItem("Pending");
        paymentstatustext.addItem("Partial Payment");
        paymentstatustext.addItem("Complete Payment");
        paymentstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatustext.setBackground(new Color(0x99ccff));
        paymentstatustext.setForeground(new Color(0x000099));
        paymentstatustext.setBounds(625, 640, 200, 35);

        JLabel orderstatus = new JLabel("Order Status:");
        orderstatus.setForeground(new Color(0x293dff));
        orderstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderstatus.setVerticalAlignment(JLabel.CENTER);
        orderstatus.setHorizontalAlignment(JLabel.LEFT);
        orderstatus.setBounds(50, 690, 130, 35);

        JComboBox<String> orderstatustext = new JComboBox<>();
        orderstatustext.addItem("Pending");
        orderstatustext.addItem("Complete");
        orderstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderstatustext.setBackground(new Color(0x99ccff));
        orderstatustext.setForeground(new Color(0x000099));
        orderstatustext.setBounds(190, 690, 200, 35);

        JLabel orderid = new JLabel("Order ID:");
        orderid.setForeground(new Color(0x293dff));
        orderid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderid.setVerticalAlignment(JLabel.CENTER);
        orderid.setHorizontalAlignment(JLabel.LEFT);
        orderid.setBounds(50, 50, 83, 35);

        JTextField orderidtext = new JTextField();
        orderidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderidtext.setBackground(new Color(0x99ccff));
        orderidtext.setForeground(new Color(0x000099));
        orderidtext.setCaretColor(new Color(0x000099));
        orderidtext.setBounds(143, 50, 83, 35);

        orderidtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try{
                    String orderquery = "SELECT * FROM orders WHERE Order_Id = " + orderidtext.getText();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(orderquery);
                    resultSet.next();
                    Date orderdate = resultSet.getDate("Order_Date");
                    String customerid = resultSet.getString("Customer_Id");
                    String orderstatus = resultSet.getString("Order_Status");
                    dateentry.setDate(orderdate);
                    orderstatustext.setSelectedItem(orderstatus);
                    String customerdetails = "SELECT * FROM customers WHERE Customer_Id = " + customerid;
                    ResultSet customerResultSet = statement.executeQuery(customerdetails);
                    customerResultSet.next();
                    String customername = customerResultSet.getString("Customer_Name");
                    String companyname = customerResultSet.getString("Customer_Company");
                    companytext.setSelectedItem(companyname);
                    nametext.setSelectedItem(customername);
                    String paymentdetails = "SELECT * FROM payments WHERE Order_Id = " + orderidtext.getText();
                    ResultSet paymentResultSet = statement.executeQuery(paymentdetails);
                    paymentResultSet.next();
                    String paymethod = paymentResultSet.getString("Payment_Method");
                    String paystatus = paymentResultSet.getString("Payment_Status");
                    paymentmethodtext.setText(paymethod);
                    paymentstatustext.setSelectedItem(paystatus);

                    String searchquery1 = "select Order_Id, Item_Name, Quantity, Rate from items, item_stock WHERE items.Item_Id = item_stock.Item_Id and Order_Id = " + orderidtext.getText();

                    Statement statementselect = connection.createStatement();
                    ResultSet resultsetselect = statementselect.executeQuery(searchquery1);
                    while(resultsetselect.next()){
                        Object[] row = new Object[3];
                        row[0]=resultsetselect.getObject("Item_Name");
                        row[1]=resultsetselect.getObject("Quantity");
                        row[2]=resultsetselect.getObject("Rate");
                        model1.addRow(row);
                    }
                    orderTable.setModel(model1);

                }catch(SQLException e1){
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Order ID not Found!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }catch(Exception e2){
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Order ID not Found!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        JButton addorderbutton = new JButton("Edit Order");
        addorderbutton.setFocusable(false);
        addorderbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        addorderbutton.setBackground(new Color(0x4c73ff));
        addorderbutton.setForeground(new Color(0x000099));
        addorderbutton.setBounds(1060, 775, 120, 35);
        addorderbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                java.util.Date selecteddate = dateentry.getDate();
                Object companyname = companytext.getSelectedItem();
                Object customername = nametext.getSelectedItem();
                String paymethod = paymentmethodtext.getText();
                Object paystatus = paymentstatustext.getSelectedItem();
                Object ordstatus = orderstatustext.getSelectedItem();
                int customerid;

                if(orderidtext.getText().length()!=0 && selecteddate!=null && companyname!=null && customername!=null && paymethod!=null && paystatus!=null && ordstatus!=null && companyname!="" && customername!="" && paymethod!="" && paystatus!="" && ordstatus!="" && paymethod.length()!=0 && model1.getRowCount()!=0){

                    try{

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = sdf.format(selecteddate);

                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT Customer_Id from customers WHERE Customer_Company = '" + companyname + "' AND Customer_Name = '" + customername + "'");
                        resultSet.next();
                        customerid = resultSet.getInt("Customer_Id");

                        String insertOrderQuery = "UPDATE orders SET Order_Date = '" + formattedDate + "', Customer_Id = " + customerid + ", Order_Status = '" + ordstatus + "' WHERE Order_Id = " + orderidtext.getText();
                        statement.execute(insertOrderQuery);

                        String insertPaymentQuery = "UPDATE payments SET Payment_Method = '" + paymethod + "', Payment_Status = '" + paystatus + "' WHERE Order_Id = " + orderidtext.getText();
                        statement.execute(insertPaymentQuery);

                        String itemdelete = "DELETE FROM items WHERE Order_Id = " + orderidtext.getText();
                        statement.execute(itemdelete);

                        for (int row = 0; row < model1.getRowCount(); row++) {
                            Object cellname = model1.getValueAt(row, 0);
                            Object cellquantity = model1.getValueAt(row, 1);
                            Object cellrate = model1.getValueAt(row, 2);
                            ResultSet resultSet2 = statement.executeQuery("SELECT Item_Id FROM item_stock WHERE Item_Name = '" + cellname + "'");
                            resultSet2.next();
                            int itemid = resultSet2.getInt("Item_Id");

                            String insertItem = "INSERT INTO items VALUES (?, ?, ?, ?, ?)";
                            PreparedStatement itemstatement = connection.prepareStatement(insertItem);
                            itemstatement.setObject(1, orderidtext.getText());
                            itemstatement.setNull(2, java.sql.Types.NULL);
                            itemstatement.setInt(3, itemid);
                            itemstatement.setObject(4, cellquantity);
                            itemstatement.setObject(5, cellrate);
                            itemstatement.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(null, "Order Details Updated", "Information", JOptionPane.INFORMATION_MESSAGE);

                    } catch(SQLException e1){
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not Update Order Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch(Exception e2){
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not Update Order Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else{
                    JOptionPane.showMessageDialog(null, "Enter Valid Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(orderid);
        frame.add(orderidtext);
        frame.add(date);
        frame.add(dateentry);
        frame.add(company);
        frame.add(companyscroll);
        frame.add(name);
        frame.add(namescroll);
        frame.add(tableScrollPane);
        frame.add(addButton);
        frame.add(delButton);
        frame.add(paymentmethod);
        frame.add(paymentmethodtext);
        frame.add(paymentstatus);
        frame.add(paymentstatustext);
        frame.add(orderstatus);
        frame.add(orderstatustext);
        frame.add(addorderbutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void edit_payment(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel billid = new JLabel("Bill Id:");
        billid.setForeground(new Color(0x293dff));
        billid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billid.setVerticalAlignment(JLabel.CENTER);
        billid.setHorizontalAlignment(JLabel.LEFT);
        billid.setBounds(775, 120, 55, 35);

        JTextField billidtext = new JTextField();
        billidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billidtext.setBackground(new Color(0x99ccff));
        billidtext.setForeground(new Color(0x000099));
        billidtext.setCaretColor(new Color(0x000099));
        billidtext.setBounds(840, 120, 83, 35);

        JLabel paymentmethod = new JLabel("Payment Method:");
        paymentmethod.setForeground(new Color(0x293dff));
        paymentmethod.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethod.setVerticalAlignment(JLabel.CENTER);
        paymentmethod.setHorizontalAlignment(JLabel.LEFT);
        paymentmethod.setBounds(675, 170, 165, 35);

        JTextField paymentmethodtext = new JTextField();
        paymentmethodtext.setBackground(new Color(0x99ccff));
        paymentmethodtext.setForeground(new Color(0x000099));
        paymentmethodtext.setCaretColor(new Color(0x000099));
        paymentmethodtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethodtext.setBounds(850, 170, 200, 35);

        JLabel paymentstatus = new JLabel("Payment Status:");
        paymentstatus.setForeground(new Color(0x293dff));
        paymentstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatus.setVerticalAlignment(JLabel.CENTER);
        paymentstatus.setHorizontalAlignment(JLabel.LEFT);
        paymentstatus.setBounds(680, 220, 155, 35);

        JComboBox<String> paymentstatustext = new JComboBox<>();
        paymentstatustext.addItem("Pending");
        paymentstatustext.addItem("Partial Payment");
        paymentstatustext.addItem("Complete Payment");
        paymentstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatustext.setBackground(new Color(0x99ccff));
        paymentstatustext.setForeground(new Color(0x000099));
        paymentstatustext.setBounds(840, 220, 200, 35);

        JLabel orderid = new JLabel("Order Id:");
        orderid.setForeground(new Color(0x293dff));
        orderid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderid.setVerticalAlignment(JLabel.CENTER);
        orderid.setHorizontalAlignment(JLabel.LEFT);
        orderid.setBounds(765, 70, 83, 35);

        JTextField orderidtext = new JTextField();
        orderidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderidtext.setBackground(new Color(0x99ccff));
        orderidtext.setForeground(new Color(0x000099));
        orderidtext.setCaretColor(new Color(0x000099));
        orderidtext.setBounds(858, 70, 83, 35);

        orderidtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try{
                String paymentquery = "SELECT * FROM payments WHERE Order_Id = " + orderidtext.getText();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(paymentquery);
                resultSet.next();
                String bill_id = resultSet.getString("Bill_Id");
                String paymethod = resultSet.getString("Payment_Method");
                String paystatus = resultSet.getString("Payment_Status");
                billidtext.setText(bill_id);
                billidtext.setEditable(false);
                paymentmethodtext.setText(paymethod);
                paymentstatustext.setSelectedItem(paystatus);
                }catch(SQLException e1){
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Order ID not Found!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }catch(Exception e2){
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Order ID not Found!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        JButton editpaymentbutton = new JButton("Edit Payment");
        editpaymentbutton.setFocusable(false);
        editpaymentbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        editpaymentbutton.setBackground(new Color(0x4c73ff));
        editpaymentbutton.setForeground(new Color(0x000099));
        editpaymentbutton.setBounds(770, 300, 140, 35);
        editpaymentbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try{
                    if(orderidtext.getText()!=null && paymentmethodtext.getText()!=null && paymentstatustext.getSelectedItem()!=null && orderidtext.getText()!="" && paymentmethodtext.getText()!="" && paymentstatustext.getSelectedItem()!="" && orderidtext.getText().length()!=0 && paymentmethodtext.getText().length()!=0){
                        String updatepayment = "UPDATE payments SET Payment_Method = '" + paymentmethodtext.getText() + "', Payment_Status = '" + paymentstatustext.getSelectedItem() + "' WHERE Order_Id = " + orderidtext.getText();
                        Statement statement = connection.createStatement();
                        statement.execute(updatepayment);
                        JOptionPane.showMessageDialog(null, "Payment Information Updated", "Information", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, "Enter Valid Information!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                    }
                }catch(SQLException e1){
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Could not Update Payment Information!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }catch(Exception e2){
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Could not Update Payment Information!!!", "Error", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        frame.add(orderid);
        frame.add(orderidtext);
        frame.add(billid);
        frame.add(billidtext);
        frame.add(paymentmethod);
        frame.add(paymentmethodtext);
        frame.add(paymentstatus);
        frame.add(paymentstatustext);
        frame.add(editpaymentbutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void add_item(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel itemname = new JLabel("Item Name:");
        itemname.setForeground(new Color(0x293dff));
        itemname.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemname.setVerticalAlignment(JLabel.CENTER);
        itemname.setHorizontalAlignment(JLabel.LEFT);
        itemname.setBounds(700, 70, 105, 35);

        JTextField itemnametext = new JTextField();
        itemnametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemnametext.setBackground(new Color(0x99ccff));
        itemnametext.setForeground(new Color(0x000099));
        itemnametext.setCaretColor(new Color(0x000099));
        itemnametext.setBounds(815, 70, 200, 35);

        JLabel itemquantity = new JLabel("Item Quantity:");
        itemquantity.setForeground(new Color(0x293dff));
        itemquantity.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemquantity.setVerticalAlignment(JLabel.CENTER);
        itemquantity.setHorizontalAlignment(JLabel.LEFT);
        itemquantity.setBounds(732, 120, 135, 35);

        JTextField itemquantitytext = new JTextField();
        itemquantitytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        itemquantitytext.setBackground(new Color(0x99ccff));
        itemquantitytext.setForeground(new Color(0x000099));
        itemquantitytext.setCaretColor(new Color(0x000099));
        itemquantitytext.setBounds(877, 120, 100, 35);

        JButton additembutton = new JButton("Add Item");
        additembutton.setFocusable(false);
        additembutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        additembutton.setBackground(new Color(0x4c73ff));
        additembutton.setForeground(new Color(0x000099));
        additembutton.setBounds(800, 210, 115, 35);
        additembutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Object itemnamevar = itemnametext.getText();
                Object itemstockvar = itemquantitytext.getText();

                if(itemnamevar!=null && itemstockvar!=null && itemnamevar!="" && itemstockvar!=""){

                    try{

                        String additemquery = "INSERT INTO item_stock (Item_Name, Item_Stock) VALUES (?, ?)";
                        PreparedStatement itemstatement = connection.prepareStatement(additemquery);
                        itemstatement.setObject(1, itemnamevar);
                        itemstatement.setObject(2, itemstockvar);
                        itemstatement.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Item Added", "Information", JOptionPane.INFORMATION_MESSAGE);

                    }catch(SQLException e1){
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Item!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }catch(Exception e2){
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Item!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                }else{
                    JOptionPane.showMessageDialog(null, "Enter Valid Data!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(itemname);
        frame.add(itemnametext);
        frame.add(itemquantity);
        frame.add(itemquantitytext);
        frame.add(additembutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void add_bills(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel date = new JLabel("Date:");
        date.setForeground(new Color(0x293dff));
        date.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        date.setVerticalAlignment(JLabel.CENTER);
        date.setHorizontalAlignment(JLabel.LEFT);
        date.setBounds(471, 50, 50, 35);

        JDateChooser dateentry = new JDateChooser();
        dateentry.setDateFormatString("yyyy-MM-dd");
        JTextField datetext = ((JTextField) dateentry.getDateEditor().getUiComponent());
        datetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        datetext.setBackground(new Color(0x99ccff));
        datetext.setForeground(new Color(0x000099));
        datetext.setCaretColor(new Color(0x000099));

        dateentry.setBounds(531, 50, 150, 35);

        JLabel time = new JLabel("Time:");
        time.setForeground(new Color(0x293dff));
        time.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        time.setVerticalAlignment(JLabel.CENTER);
        time.setHorizontalAlignment(JLabel.LEFT);
        time.setBounds(691, 50, 50, 35);

        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setBackground(new Color(0x99ccff));
        timeSpinner.setForeground(new Color(0x000099));
        timeSpinner.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        timeSpinner.setBounds(751, 50, 150, 35);

        JLabel company = new JLabel("Customer Company:");
        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.CENTER);
        company.setHorizontalAlignment(JLabel.LEFT);
        company.setBounds(50, 100, 185, 35);

        JComboBox<String> companytext = new JComboBox<>();

        JComboBox<String> nametext = new JComboBox<>();

        java.util.List<String> companylist = new ArrayList<>();

        java.util.List<String> namelist = new ArrayList<>();

        JTextField billingAddtext = new JTextField();

        JTextField shippingAddtext = new JTextField();

        companytext.addItem("Select:");

        nametext.addItem("Select:");

        try{

            String companyquery = "SELECT DISTINCT Customer_Company FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(companyquery);
            while(resultSet.next()){
                companylist.add(resultSet.getString("Customer_Company"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : companylist){
            companytext.addItem(i);
        }

        companytext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a){
                
                nametext.removeAllItems();
                nametext.addItem("Select:");
                java.util.List<String> namelist2 = new ArrayList<>();
                try{
                    
                    String companyquery = "";
                    String addressquery = "";
                    String addressTemp = "";
                    if(companytext.getSelectedItem()=="" || companytext.getSelectedItem()==null){
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers";
                    } else{
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers WHERE Customer_Company = '" + companytext.getSelectedItem() + "'";
                        addressquery = "SELECT Address from customers WHERE Customer_Company = '" + companytext.getSelectedItem() + "'";
                    }
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(companyquery);
                    while(resultSet.next()){
                        namelist2.add(resultSet.getString("Customer_Name"));
                    }
                    ResultSet addressResult = statement.executeQuery(addressquery);
                    while (addressResult.next()) {
                        addressTemp = addressResult.getString("Address");
                    }

                    billingAddtext.setText(addressTemp);
                    shippingAddtext.setText(addressTemp);

                } catch(SQLException e){
                    e.printStackTrace();
                } catch(Exception e1){
                    e1.printStackTrace();
                }

                for(String i : namelist2){
                    nametext.addItem(i);
                }
            }
        });

        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));

        JScrollPane companyscroll = new JScrollPane(companytext);
        companyscroll.setPreferredSize(new Dimension(400, 45));
        companyscroll.setBounds(245, 100, 400, 45);

        JLabel name = new JLabel("Customer Name:");
        name.setForeground(new Color(0x293dff));
        name.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        name.setVerticalAlignment(JLabel.CENTER);
        name.setHorizontalAlignment(JLabel.LEFT);
        name.setBounds(50, 160, 155, 35);

        try{

            String namequery = "SELECT DISTINCT Customer_Name FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(namequery);
            while(resultSet.next()){
                namelist.add(resultSet.getString("Customer_Name"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : namelist){
            nametext.addItem(i);
        }

        nametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        nametext.setBackground(new Color(0x99ccff));
        nametext.setForeground(new Color(0x000099));

        JScrollPane namescroll = new JScrollPane(nametext);
        namescroll.setPreferredSize(new Dimension(400, 45));
        namescroll.setBounds(215, 160, 400, 45);

        JLabel billingAdd = new JLabel("Billing Address:");
        billingAdd.setForeground(new Color(0x293dff));
        billingAdd.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billingAdd.setVerticalAlignment(JLabel.CENTER);
        billingAdd.setHorizontalAlignment(JLabel.LEFT);
        billingAdd.setBounds(50, 220, 145, 35);

        billingAddtext.setBackground(new Color(0x99ccff));
        billingAddtext.setForeground(new Color(0x000099));
        billingAddtext.setCaretColor(new Color(0x000099));
        billingAddtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billingAddtext.setEditable(false);
        billingAddtext.setBounds(205, 220, 500, 35);

        JLabel shippingAdd = new JLabel("Shipping Address:");
        shippingAdd.setForeground(new Color(0x293dff));
        shippingAdd.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        shippingAdd.setVerticalAlignment(JLabel.CENTER);
        shippingAdd.setHorizontalAlignment(JLabel.LEFT);
        shippingAdd.setBounds(50, 270, 165, 35);

        shippingAddtext.setBackground(new Color(0x99ccff));
        shippingAddtext.setForeground(new Color(0x000099));
        shippingAddtext.setCaretColor(new Color(0x000099));
        shippingAddtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        shippingAddtext.setBounds(225, 270, 500, 35);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Item Name");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("Rate");
        tableModel.addColumn("Price");
        tableModel.addColumn("Tax");

        JTable orderTable = new JTable(tableModel);
        orderTable.setBackground(new Color(0x99ccff));

        JScrollPane tableScrollPane = new JScrollPane(orderTable);
        tableScrollPane.setBounds(50, 365, 1000, 400);

        JLabel paymentmethod = new JLabel("Payment Method:");
        paymentmethod.setForeground(new Color(0x293dff));
        paymentmethod.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethod.setVerticalAlignment(JLabel.CENTER);
        paymentmethod.setHorizontalAlignment(JLabel.LEFT);
        paymentmethod.setBounds(50, 785, 165, 35);

        JTextField paymentmethodtext = new JTextField();
        paymentmethodtext.setBackground(new Color(0x99ccff));
        paymentmethodtext.setForeground(new Color(0x000099));
        paymentmethodtext.setCaretColor(new Color(0x000099));
        paymentmethodtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethodtext.setBounds(225, 785, 200, 35);

        JLabel paymentstatus = new JLabel("Payment Status:");
        paymentstatus.setForeground(new Color(0x293dff));
        paymentstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatus.setVerticalAlignment(JLabel.CENTER);
        paymentstatus.setHorizontalAlignment(JLabel.LEFT);
        paymentstatus.setBounds(460, 785, 155, 35);

        JComboBox<String> paymentstatustext = new JComboBox<>();
        paymentstatustext.addItem("Pending");
        paymentstatustext.addItem("Partial Payment");
        paymentstatustext.addItem("Complete Payment");
        paymentstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatustext.setBackground(new Color(0x99ccff));
        paymentstatustext.setForeground(new Color(0x000099));
        paymentstatustext.setBounds(625, 785, 200, 35);

        JLabel tax = new JLabel("Tax Percentage:");
        tax.setForeground(new Color(0x293dff));
        tax.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        tax.setVerticalAlignment(JLabel.CENTER);
        tax.setHorizontalAlignment(JLabel.LEFT);
        tax.setBounds(50, 320, 152, 35);

        JComboBox<Integer> taxtext = new JComboBox<>();
        taxtext.addItem(5);
        taxtext.addItem(12);
        taxtext.addItem(18);
        taxtext.addItem(28);
        taxtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        taxtext.setBackground(new Color(0x99ccff));
        taxtext.setForeground(new Color(0x000099));
        taxtext.setBounds(212, 320, 70, 35);

        taxtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                for(int row = 0; row < tableModel.getRowCount(); row++){
                    float price = (float)tableModel.getValueAt(row, 3);
                    tableModel.setValueAt(price*(int)taxtext.getSelectedItem()/100, row, 4);
                }
            }
        });

        JLabel orderid = new JLabel("Order ID:");
        orderid.setForeground(new Color(0x293dff));
        orderid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderid.setVerticalAlignment(JLabel.CENTER);
        orderid.setHorizontalAlignment(JLabel.LEFT);
        orderid.setBounds(251, 50, 85, 35);

        JComboBox<Integer> orderidtext = new JComboBox<>();

        java.util.List<Integer> distinctorderid = new ArrayList<>();

        try{

            String orderidquery = "SELECT DISTINCT Order_Id FROM items WHERE Bill_Id IS NULL";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(orderidquery);
            while(resultSet.next()){
                distinctorderid.add(resultSet.getInt("Order_Id"));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }catch(Exception e1){
            e1.printStackTrace();
        }

        for (Integer i : distinctorderid){
            orderidtext.addItem(i);
        }

        orderidtext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{

                    String cutstomeridquery = "SELECT Customer_Id FROM orders WHERE Order_Id = " + orderidtext.getSelectedItem();
                    Statement statement = connection.createStatement();
                    ResultSet customeridResultSet = statement.executeQuery(cutstomeridquery);
                    customeridResultSet.next();
                    int customerid = customeridResultSet.getInt("Customer_Id");

                    String customerquery = "SELECT Customer_Name, Customer_Company FROM customers WHERE Customer_Id = " + customerid;
                    ResultSet customerResultSet = statement.executeQuery(customerquery);
                    customerResultSet.next();
                    String customerName = customerResultSet.getString("Customer_Name");
                    String customerCompany = customerResultSet.getString("Customer_Company");

                    companytext.setSelectedItem(customerCompany);
                    nametext.setSelectedItem(customerName);

                    String paymentquery = "SELECT Payment_Method, Payment_Status FROM payments WHERE Order_Id = " + orderidtext.getSelectedItem();
                    ResultSet paymentResultSet = statement.executeQuery(paymentquery);
                    paymentResultSet.next();
                    String payment_meth = paymentResultSet.getString("Payment_Method");
                    String payment_stat = paymentResultSet.getString("Payment_Status");

                    paymentmethodtext.setText(payment_meth);
                    paymentstatustext.setSelectedItem(payment_stat);

                    String items = "SELECT Item_Name, Quantity, Rate, Quantity*Rate AS Price FROM items, item_stock WHERE items.Item_Id = item_stock.Item_Id and Order_Id = " + orderidtext.getSelectedItem();
                    ResultSet itemsResultSet = statement.executeQuery(items);
                    tableModel.setRowCount(0);
                    while(itemsResultSet.next()){
                        tableModel.addRow(new Object[]{itemsResultSet.getString("Item_Name"), itemsResultSet.getInt("Quantity"), itemsResultSet.getFloat("Rate"), itemsResultSet.getFloat("Price"), (float)itemsResultSet.getFloat("Price")*(int)taxtext.getSelectedItem()/100});
                    }

                    String addressTemp = "";
                    String addressquery = "SELECT Address from customers, orders WHERE orders.Order_Id = " + orderidtext.getSelectedItem() + " AND orders.Customer_Id = customers.Customer_Id";
                    ResultSet addressResult = statement.executeQuery(addressquery);
                    while (addressResult.next()) {
                        addressTemp = addressResult.getString("Address");
                    }
                    billingAddtext.setText(addressTemp);
                    shippingAddtext.setText(addressTemp);

                }catch(SQLException e1){
                    e1.printStackTrace();
                }catch(Exception e2){
                    e2.printStackTrace();
                }
            }
        });

        orderidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderidtext.setBackground(new Color(0x99ccff));
        orderidtext.setForeground(new Color(0x000099));

        JScrollPane orderidscroll = new JScrollPane(orderidtext);
        orderidscroll.setPreferredSize(new Dimension(210, 45));
        orderidscroll.setBounds(346, 45, 100, 45);

        JLabel billid = new JLabel("Bill ID:");
        billid.setForeground(new Color(0x293dff));
        billid.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billid.setVerticalAlignment(JLabel.CENTER);
        billid.setHorizontalAlignment(JLabel.LEFT);
        billid.setBounds(50, 50, 83, 35);

        int billno = 0;
        try{
            Statement statement = connection.createStatement();
            String billquery = "SELECT * FROM bills";
            ResultSet billresult = statement.executeQuery(billquery);
            while (billresult.next()) {
                billno = billresult.getInt("Bill_Id");
            }

        }catch(SQLException e1){
            e1.printStackTrace();
        }catch(Exception e2){
            e2.printStackTrace();
        }
        billno+=1;

        JTextField billidtext = new JTextField();
        billidtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        billidtext.setBackground(new Color(0x99ccff));
        billidtext.setForeground(new Color(0x000099));
        billidtext.setCaretColor(new Color(0x000099));
        billidtext.setText(String.valueOf(billno));
        billidtext.setBounds(143, 50, 83, 35);

        JButton addorderbutton = new JButton("Create Bill");
        addorderbutton.setFocusable(false);
        addorderbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        addorderbutton.setBackground(new Color(0x4c73ff));
        addorderbutton.setForeground(new Color(0x000099));
        addorderbutton.setBounds(1060, 880, 130, 35);
        addorderbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Object billId = billidtext.getText();
                Object order_id = orderidtext.getSelectedItem();
                java.util.Date selecteddate = dateentry.getDate();
                java.util.Date selectedTime = (Date) timeSpinner.getValue();
                Object companyname = companytext.getSelectedItem();
                Object customername = nametext.getSelectedItem();
                Object shippingAddress = shippingAddtext.getText();
                String paymethod = paymentmethodtext.getText();
                Object paystatus = paymentstatustext.getSelectedItem();
                Object taxperc = taxtext.getSelectedItem();

                if(billId!=null && selecteddate!=null && selectedTime!=null && companyname!=null && customername!=null && paymethod!=null && paystatus!=null && taxperc!=null && billId!="" && companyname!="" && customername!="" && paymethod!="" && paystatus!="" && taxperc!="" && paymethod.length()!=0){

                    try{

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = sdf.format(selecteddate);

                        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
                        String formattedTime = stf.format(selectedTime);

                        Statement statement = connection.createStatement();

                        String insertOrderQuery = "UPDATE orders SET Order_Status = 'Complete' WHERE Order_Id = " + order_id;
                        statement.execute(insertOrderQuery);

                        int Temp = 0;
                        String customerIdQuery = "SELECT * from customers WHERE Customer_Name = '" + customername + "' AND Customer_Company = '" + companyname + "'";
                        ResultSet customerIdResult = statement.executeQuery(customerIdQuery);
                        Temp = 0;
                        while (customerIdResult.next()) {
                            Temp = customerIdResult.getInt("Customer_Id");     
                        }
                        int customerId = Temp;

                        String insertBillQuery = "INSERT INTO bills VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement billStatement = connection.prepareStatement(insertBillQuery);
                        billStatement.setObject(1, billId);
                        billStatement.setInt(2, (int)order_id);
                        billStatement.setDate(3, java.sql.Date.valueOf(formattedDate));
                        billStatement.setTime(4, java.sql.Time.valueOf(formattedTime));
                        billStatement.setInt(5, customerId);
                        billStatement.setObject(6, shippingAddress);
                        billStatement.setObject(7, taxperc);
                        billStatement.setString(8, username);
                        billStatement.executeUpdate();

                        String insertPaymentQuery = "UPDATE payments set Bill_Id = " + billId + ", Payment_Method = '" + paymethod + "', Payment_Status = '" + paystatus + "' WHERE Order_Id = " + order_id;
                        statement.execute(insertPaymentQuery);

                        String insertItem = "UPDATE items SET Bill_Id = " + billId + " WHERE Order_Id = " + order_id;
                        statement.execute(insertItem);

                        for (int row = 0; row < tableModel.getRowCount(); row++) {
                            Object cellname = tableModel.getValueAt(row, 0);
                            Object cellquantity = tableModel.getValueAt(row, 1);

                            ResultSet resultSet2 = statement.executeQuery("SELECT Item_Stock FROM item_stock WHERE Item_Name = '" + cellname + "'");
                            resultSet2.next();
                            int itemstock = resultSet2.getInt("Item_Stock");

                            String insertItemStock = "UPDATE item_stock SET Item_Stock = " + (itemstock-(int)cellquantity) + " WHERE Item_Name = '" + cellname + "'";
                            statement.execute(insertItemStock);
                        }

                        JOptionPane.showMessageDialog(null, "Bill Details Added", "Information", JOptionPane.INFORMATION_MESSAGE);

                    } catch(SQLException e1){
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Bill Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch(Exception e2){
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Bill Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else{
                    JOptionPane.showMessageDialog(null, "Enter Valid Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(billid);
        frame.add(billidtext);
        frame.add(orderid);
        frame.add(orderidscroll);
        frame.add(date);
        frame.add(dateentry);
        frame.add(time);
        frame.add(timeSpinner);
        frame.add(company);
        frame.add(companyscroll);
        frame.add(name);
        frame.add(namescroll);
        frame.add(billingAdd);
        frame.add(billingAddtext);
        frame.add(shippingAdd);
        frame.add(shippingAddtext);
        frame.add(tableScrollPane);
        frame.add(paymentmethod);
        frame.add(paymentmethodtext);
        frame.add(paymentstatus);
        frame.add(paymentstatustext);
        frame.add(tax);
        frame.add(taxtext);
        frame.add(addorderbutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void add_orders(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel date = new JLabel("Date:");
        date.setForeground(new Color(0x293dff));
        date.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        date.setVerticalAlignment(JLabel.CENTER);
        date.setHorizontalAlignment(JLabel.LEFT);
        date.setBounds(50, 50, 50, 35);

        JDateChooser dateentry = new JDateChooser();
        dateentry.setDateFormatString("yyyy-MM-dd");
        JTextField datetext = ((JTextField) dateentry.getDateEditor().getUiComponent());
        datetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        datetext.setBackground(new Color(0x99ccff));
        datetext.setForeground(new Color(0x000099));
        datetext.setCaretColor(new Color(0x000099));

        dateentry.setBounds(110, 50, 150, 35);

        JLabel company = new JLabel("Customer Company:");
        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.CENTER);
        company.setHorizontalAlignment(JLabel.LEFT);
        company.setBounds(50, 100, 185, 35);

        JComboBox<String> companytext = new JComboBox<>();

        JComboBox<String> nametext = new JComboBox<>();

        java.util.List<String> companylist = new ArrayList<>();

        java.util.List<String> namelist = new ArrayList<>();

        companytext.addItem("Select:");

        nametext.addItem("Select:");

        try{

            String companyquery = "SELECT DISTINCT Customer_Company FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(companyquery);
            while(resultSet.next()){
                companylist.add(resultSet.getString("Customer_Company"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : companylist){
            companytext.addItem(i);
        }

        companytext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a){
                
                nametext.removeAllItems();
                nametext.addItem("Select:");
                java.util.List<String> namelist2 = new ArrayList<>();
                try{
                    
                    String companyquery = "";
                    if(companytext.getSelectedItem()=="" || companytext.getSelectedItem()==null){
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers";
                    } else{
                        companyquery = "SELECT DISTINCT Customer_Name FROM customers WHERE Customer_Company = '" + companytext.getSelectedItem() + "'";
                    }
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(companyquery);
                    while(resultSet.next()){
                        namelist2.add(resultSet.getString("Customer_Name"));
                    }

                } catch(SQLException e){
                    e.printStackTrace();
                } catch(Exception e1){
                    e1.printStackTrace();
                }

                for(String i : namelist2){
                    nametext.addItem(i);
                }
            }
        });

        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));

        JScrollPane companyscroll = new JScrollPane(companytext);
        companyscroll.setPreferredSize(new Dimension(400, 45));
        companyscroll.setBounds(245, 100, 400, 45);

        JLabel name = new JLabel("Customer Name:");
        name.setForeground(new Color(0x293dff));
        name.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        name.setVerticalAlignment(JLabel.CENTER);
        name.setHorizontalAlignment(JLabel.LEFT);
        name.setBounds(50, 160, 155, 35);

        try{

            String namequery = "SELECT DISTINCT Customer_Name FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(namequery);
            while(resultSet.next()){
                namelist.add(resultSet.getString("Customer_Name"));
            }

        } catch(SQLException e){
            e.printStackTrace();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        for(String i : namelist){
            nametext.addItem(i);
        }

        nametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        nametext.setBackground(new Color(0x99ccff));
        nametext.setForeground(new Color(0x000099));

        JScrollPane namescroll = new JScrollPane(nametext);
        namescroll.setPreferredSize(new Dimension(400, 45));
        namescroll.setBounds(215, 160, 400, 45);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Item Name");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("Rate");

        JTable orderTable = new JTable(tableModel);
        orderTable.setBackground(new Color(0x99ccff));

        orderTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
        orderTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        orderTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));

        JScrollPane tableScrollPane = new JScrollPane(orderTable);
        tableScrollPane.setBounds(50, 220, 1000, 400);

        JButton addButton = new JButton("Add Row");
        addButton.setFocusable(false);
        addButton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        addButton.setBackground(new Color(0x4c73ff));
        addButton.setForeground(new Color(0x000099));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tableModel.addRow(new Object[]{"", "", ""});
            }
        });
        addButton.setBounds(1060, 585, 110, 35);

        JLabel paymentmethod = new JLabel("Payment Method:");
        paymentmethod.setForeground(new Color(0x293dff));
        paymentmethod.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethod.setVerticalAlignment(JLabel.CENTER);
        paymentmethod.setHorizontalAlignment(JLabel.LEFT);
        paymentmethod.setBounds(50, 640, 165, 35);

        JTextField paymentmethodtext = new JTextField();
        paymentmethodtext.setBackground(new Color(0x99ccff));
        paymentmethodtext.setForeground(new Color(0x000099));
        paymentmethodtext.setCaretColor(new Color(0x000099));
        paymentmethodtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentmethodtext.setBounds(225, 640, 200, 35);

        JLabel paymentstatus = new JLabel("Payment Status:");
        paymentstatus.setForeground(new Color(0x293dff));
        paymentstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatus.setVerticalAlignment(JLabel.CENTER);
        paymentstatus.setHorizontalAlignment(JLabel.LEFT);
        paymentstatus.setBounds(460, 640, 155, 35);

        JComboBox<String> paymentstatustext = new JComboBox<>();
        paymentstatustext.addItem("Pending");
        paymentstatustext.addItem("Partial Payment");
        paymentstatustext.addItem("Complete Payment");
        paymentstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        paymentstatustext.setBackground(new Color(0x99ccff));
        paymentstatustext.setForeground(new Color(0x000099));
        paymentstatustext.setBounds(625, 640, 200, 35);

        JLabel orderstatus = new JLabel("Order Status:");
        orderstatus.setForeground(new Color(0x293dff));
        orderstatus.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderstatus.setVerticalAlignment(JLabel.CENTER);
        orderstatus.setHorizontalAlignment(JLabel.LEFT);
        orderstatus.setBounds(50, 690, 130, 35);

        JComboBox<String> orderstatustext = new JComboBox<>();
        orderstatustext.addItem("Pending");
        orderstatustext.addItem("Complete");
        orderstatustext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orderstatustext.setBackground(new Color(0x99ccff));
        orderstatustext.setForeground(new Color(0x000099));
        orderstatustext.setBounds(190, 690, 200, 35);

        JButton addorderbutton = new JButton("Add Order");
        addorderbutton.setFocusable(false);
        addorderbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        addorderbutton.setBackground(new Color(0x4c73ff));
        addorderbutton.setForeground(new Color(0x000099));
        addorderbutton.setBounds(1060, 775, 120, 35);
        addorderbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                java.util.Date selecteddate = dateentry.getDate();
                Object companyname = companytext.getSelectedItem();
                Object customername = nametext.getSelectedItem();
                String paymethod = paymentmethodtext.getText();
                Object paystatus = paymentstatustext.getSelectedItem();
                Object ordstatus = orderstatustext.getSelectedItem();
                int customerid;

                if(selecteddate!=null && companyname!=null && customername!=null && paymethod!=null && paystatus!=null && ordstatus!=null && companyname!="" && customername!="" && paymethod!="" && paystatus!="" && ordstatus!="" && paymethod.length()!=0 && tableModel.getRowCount()!=0){

                    try{

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate = sdf.format(selecteddate);

                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT Customer_Id from customers WHERE Customer_Company = '" + companyname + "' AND Customer_Name = '" + customername + "'");
                        resultSet.next();
                        customerid = resultSet.getInt("Customer_Id");

                        String insertOrderQuery = "INSERT INTO orders (Order_Date, Customer_Id, Order_Status) VALUES (?, ?, ?)";
                        PreparedStatement orderStatement = connection.prepareStatement(insertOrderQuery);
                        orderStatement.setDate(1, java.sql.Date.valueOf(formattedDate));
                        orderStatement.setInt(2, customerid);
                        orderStatement.setString(3, (String)ordstatus);
                        orderStatement.executeUpdate();

                        String getLastOrderQuery = "SELECT LAST_INSERT_ID()";
                        Statement getLastOrderStatement = connection.createStatement();
                        ResultSet lastOrderIdResult = getLastOrderStatement.executeQuery(getLastOrderQuery);
                        lastOrderIdResult.next();
                        int orderId = lastOrderIdResult.getInt(1);

                        String insertPaymentQuery = "INSERT INTO payments (Order_Id, Bill_Id, Payment_Method, Payment_Status) VALUES (?, ?, ?, ?)";
                        PreparedStatement paymentStatement = connection.prepareStatement(insertPaymentQuery);
                        paymentStatement.setInt(1, orderId);
                        paymentStatement.setNull(2, java.sql.Types.NULL);;
                        paymentStatement.setString(3, paymethod);
                        paymentStatement.setString(4, (String)paystatus);
                        paymentStatement.executeUpdate();

                        for (int row = 0; row < tableModel.getRowCount(); row++) {
                            Object cellname = tableModel.getValueAt(row, 0);
                            Object cellquantity = tableModel.getValueAt(row, 1);
                            Object cellrate = tableModel.getValueAt(row, 2);
                            ResultSet resultSet2 = statement.executeQuery("SELECT Item_Id FROM item_stock WHERE Item_Name = '" + cellname + "'");
                            resultSet2.next();
                            int itemid = resultSet2.getInt("Item_Id");

                            String insertItem = "INSERT INTO items VALUES (?, ?, ?, ?, ?)";
                            PreparedStatement itemstatement = connection.prepareStatement(insertItem);
                            itemstatement.setInt(1, orderId);
                            itemstatement.setNull(2, java.sql.Types.NULL);
                            itemstatement.setInt(3, itemid);
                            itemstatement.setObject(4, cellquantity);
                            itemstatement.setObject(5, cellrate);
                            itemstatement.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(null, "Order Details Added", "Information", JOptionPane.INFORMATION_MESSAGE);

                    } catch(SQLException e1){
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Order Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch(Exception e2){
                        e2.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Could not add Order Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else{
                    JOptionPane.showMessageDialog(null, "Enter Valid Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(date);
        frame.add(dateentry);
        frame.add(company);
        frame.add(companyscroll);
        frame.add(name);
        frame.add(namescroll);
        frame.add(tableScrollPane);
        frame.add(addButton);
        frame.add(paymentmethod);
        frame.add(paymentmethodtext);
        frame.add(paymentstatus);
        frame.add(paymentstatustext);
        frame.add(orderstatus);
        frame.add(orderstatustext);
        frame.add(addorderbutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void add_customer(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JLabel name = new JLabel("Name:");
        name.setForeground(new Color(0x293dff));
        name.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        name.setVerticalAlignment(JLabel.CENTER);
        name.setHorizontalAlignment(JLabel.LEFT);
        name.setBounds(630, 60, 150, 35);

        JTextField nametext = new JTextField();
        nametext.setBackground(new Color(0x99ccff));
        nametext.setForeground(new Color(0x000099));
        nametext.setCaretColor(new Color(0x000099));
        nametext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        nametext.setBounds(790, 60, 300, 35);

        JLabel company = new JLabel("Company:");
        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.CENTER);
        company.setHorizontalAlignment(JLabel.LEFT);
        company.setBounds(630, 105, 150, 35);

        JTextField companytext = new JTextField();
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));
        companytext.setCaretColor(new Color(0x000099));
        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBounds(790, 105, 300, 35);

        JLabel gstin = new JLabel("GSTIN:");
        gstin.setForeground(new Color(0x293dff));
        gstin.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        gstin.setVerticalAlignment(JLabel.CENTER);
        gstin.setHorizontalAlignment(JLabel.LEFT);
        gstin.setBounds(630, 150, 150, 35);

        JTextField gstintext = new JTextField();
        gstintext.setBackground(new Color(0x99ccff));
        gstintext.setForeground(new Color(0x000099));
        gstintext.setCaretColor(new Color(0x000099));
        gstintext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        gstintext.setBounds(790, 150, 300, 35);

        JLabel phone = new JLabel("Phone Number:");
        phone.setForeground(new Color(0x293dff));
        phone.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        phone.setVerticalAlignment(JLabel.CENTER);
        phone.setHorizontalAlignment(JLabel.LEFT);
        phone.setBounds(630, 195, 150, 35);

        JTextField phonetext = new JTextField();
        phonetext.setBackground(new Color(0x99ccff));
        phonetext.setForeground(new Color(0x000099));
        phonetext.setCaretColor(new Color(0x000099));
        phonetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        phonetext.setBounds(790, 195, 300, 35);

        JLabel email = new JLabel("Email:");
        email.setForeground(new Color(0x293dff));
        email.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        email.setVerticalAlignment(JLabel.CENTER);
        email.setHorizontalAlignment(JLabel.LEFT);
        email.setBounds(630, 240, 150, 35);

        JTextField emailtext = new JTextField();
        emailtext.setBackground(new Color(0x99ccff));
        emailtext.setForeground(new Color(0x000099));
        emailtext.setCaretColor(new Color(0x000099));
        emailtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        emailtext.setBounds(790, 240, 300, 35);

        JLabel address = new JLabel("Address:");
        address.setForeground(new Color(0x293dff));
        address.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        address.setVerticalAlignment(JLabel.CENTER);
        address.setHorizontalAlignment(JLabel.LEFT);
        address.setBounds(630, 285, 150, 35);

        JTextField addresstext = new JTextField();
        addresstext.setBackground(new Color(0x99ccff));
        addresstext.setForeground(new Color(0x000099));
        addresstext.setCaretColor(new Color(0x000099));
        addresstext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        addresstext.setBounds(790, 285, 300, 35);

        JButton addbutton = new JButton("Add Customer");
        addbutton.setFocusable(false);
        addbutton.setBackground(new Color(0x4c73ff));
        addbutton.setForeground(new Color(0x000099));
        addbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        addbutton.setBounds(760, 380, 170, 35);
        addbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                String namevar = nametext.getText();
                String companyvar = companytext.getText();
                String gstinvar = gstintext.getText();
                String phonevar = phonetext.getText();
                String emailvar = emailtext.getText();
                String addressvar = addresstext.getText();

                if(namevar.length()!=0 && companyvar.length()!=0 && phonevar.length()!=0 && emailvar.length()!=0 && addressvar.length()!=0){
                    try{
                        String query = "INSERT INTO customers (Customer_Name, Customer_Company, GSTIN, Phone_Number, Email, Address) values (?, ?, ?, ?, ?, ?)";
                        PreparedStatement insertquery = connection.prepareStatement(query);
                        insertquery.setString(1, namevar);
                        insertquery.setString(2, companyvar);
                        insertquery.setString(3, gstinvar);
                        insertquery.setString(4, phonevar);
                        insertquery.setString(5, emailvar);
                        insertquery.setString(6, addressvar);
                        insertquery.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Customer Added", "Information", JOptionPane.INFORMATION_MESSAGE);
                        nametext.setText("");
                        companytext.setText("");
                        gstintext.setText("");
                        phonetext.setText("");
                        emailtext.setText("");
                        addresstext.setText("");
                    } catch(SQLException e1){
                        JOptionPane.showMessageDialog(null, "Could not add Customer Details!!!", "Error", JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "Please Enter Valid Data!!!", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        frame.add(name);
        frame.add(nametext);
        frame.add(company);
        frame.add(companytext);
        frame.add(gstin);
        frame.add(gstintext);
        frame.add(phone);
        frame.add(phonetext);
        frame.add(email);
        frame.add(emailtext);
        frame.add(address);
        frame.add(addresstext);
        frame.add(addbutton);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void view_order(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(new GridLayout(1, 2, 10, 10));

        String query = "SELECT orders.Order_Id, Order_Date, orders.Customer_Id, Customer_Name, Customer_Company, Order_Status, Payment_Method, Payment_Status FROM orders, customers, payments WHERE orders.Customer_Id = customers.Customer_id and orders.Order_Id = payments.Order_Id";

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Order ID");
        model.addColumn("Order Date");
        model.addColumn("Customer Id");
        model.addColumn("Customer Name");
        model.addColumn("Customer Company");
        model.addColumn("Order Status");
        model.addColumn("Payment Method");
        model.addColumn("Payment Status");

        try{
            Statement statementselect3 = connection.createStatement();
            ResultSet resultsetselect3 = statementselect3.executeQuery(query);
            while(resultsetselect3.next()){
                Object[] row = new Object[8];
                row[0]=resultsetselect3.getObject("Order_Id");
                row[1]=resultsetselect3.getObject("Order_Date");
                row[2]=resultsetselect3.getObject("Customer_Id");
                row[3]=resultsetselect3.getObject("Customer_Name");
                row[4]=resultsetselect3.getObject("Customer_Company");
                row[5]=resultsetselect3.getObject("Order_Status");
                row[6]=resultsetselect3.getObject("Payment_Method");
                row[7]=resultsetselect3.getObject("Payment_Status");
                model.addRow(row);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        JTable datatable1 = new JTable();
        datatable1.setBackground(new Color(0x99ccff));
        JScrollPane scroll1 = new JScrollPane(datatable1);
        scroll1.setBounds(50, 100, 1600, 750);

        JTable datatable = new JTable(model);
        datatable.setBackground(new Color(0x99ccff));
        JScrollPane scroll = new JScrollPane(datatable);
        scroll.setBounds(50, 100, 1600, 750);
        datatable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                int selectedrow = datatable.rowAtPoint(e.getPoint());
                Object val1 = model.getValueAt(selectedrow, 0);

                String searchquery1 = "select Order_Id, Item_Name, Quantity, Rate, Quantity*Rate AS Price from items, item_stock WHERE Order_Id = " + val1 + " AND items.Item_Id = item_stock.Item_Id";

                DefaultTableModel model1 = new DefaultTableModel();
                model1.addColumn("Order ID");
                model1.addColumn("Item Name");
                model1.addColumn("Item Quantity");
                model1.addColumn("Item Rate");
                model1.addColumn("Item Price");

                try{
                    Statement statementselect = connection.createStatement();
                    ResultSet resultsetselect = statementselect.executeQuery(searchquery1);
                    while(resultsetselect.next()){
                        Object[] row = new Object[5];
                        row[0]=resultsetselect.getObject("Order_Id");
                        row[1]=resultsetselect.getObject("Item_Name");
                        row[2]=resultsetselect.getObject("Quantity");
                        row[3]=resultsetselect.getObject("Rate");
                        row[4]=resultsetselect.getObject("Price");
                        model1.addRow(row);
                    }
                    datatable1.setModel(model1);
                } catch(SQLException e1){
                    e1.printStackTrace();
                }
            }
        });

        JPanel panel1 = new JPanel();
        panel1.setBackground(new Color(0x99ccff));
        panel1.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel stock = new JLabel("Order Details");
        stock.setForeground(new Color(0x293dff));
        stock.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        stock.setVerticalAlignment(JLabel.TOP);
        stock.setHorizontalAlignment(JLabel.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(stock, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scroll, gbc);

        JPanel panel2 = new JPanel();
        panel2.setBackground(new Color(0x99ccff));
        panel2.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();

        JLabel stock1 = new JLabel("Order Items");
        stock1.setForeground(new Color(0x293dff));
        stock1.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        stock1.setVerticalAlignment(JLabel.TOP);
        stock1.setHorizontalAlignment(JLabel.CENTER);

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0;
        gbc2.weighty = 0.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(stock1, gbc2);

        gbc2.gridy = 1;
        gbc2.weighty = 1.0;
        gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        panel2.add(scroll1, gbc2);

        frame.add(panel1);
        frame.add(panel2);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void view_bill(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(new GridLayout(1, 2, 10, 10));

        String query = "SELECT * FROM bills, payments, customers WHERE bills.Bill_Id = payments.Bill_Id AND bills.Customer_Id = customers.Customer_Id";

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Bill ID");
        model.addColumn("Order ID");
        model.addColumn("Bill Date");
        model.addColumn("Bill Time");
        model.addColumn("Customer Name");
        model.addColumn("Customer Company");
        model.addColumn("Billing Address");
        model.addColumn("Shipping Address");
        model.addColumn("Payment Method");
        model.addColumn("Payment Status");
        model.addColumn("Created by");

        try{
            Statement statementselect3 = connection.createStatement();
            ResultSet resultsetselect3 = statementselect3.executeQuery(query);
            while(resultsetselect3.next()){
                Object[] row = new Object[11];
                row[0]=resultsetselect3.getObject("Bill_Id");
                row[1]=resultsetselect3.getObject("Order_Id");
                row[2]=resultsetselect3.getObject("Date");
                row[3]=resultsetselect3.getObject("Time");
                row[4]=resultsetselect3.getObject("Customer_Name");
                row[5]=resultsetselect3.getObject("Customer_Company");
                row[6]=resultsetselect3.getObject("Address");
                row[7]=resultsetselect3.getObject("Shipping_Address");
                row[8]=resultsetselect3.getObject("Payment_Method");
                row[9]=resultsetselect3.getObject("Payment_Status");
                row[10]=resultsetselect3.getObject("User_Name");
                model.addRow(row);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        JTable datatable1 = new JTable();
        datatable1.setBackground(new Color(0x99ccff));
        JScrollPane scroll1 = new JScrollPane(datatable1);
        scroll1.setBounds(50, 100, 1600, 750);

        JTable datatable = new JTable(model);
        datatable.setBackground(new Color(0x99ccff));
        JScrollPane scroll = new JScrollPane(datatable);
        scroll.setBounds(50, 100, 1600, 750);
        datatable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                int selectedrow = datatable.rowAtPoint(e.getPoint());
                Object val1 = model.getValueAt(selectedrow, 0);

                String searchquery1 = "select Item_Id, Item_Name, Quantity, Rate, Quantity*Rate AS Price, ROUND(tax_percentage*Quantity*Rate/100, 2) AS Tax from items NATURAL JOIN bills NATURAL JOIN item_stock WHERE Bill_Id = " + val1;

                DefaultTableModel model1 = new DefaultTableModel();
                model1.addColumn("Item ID");
                model1.addColumn("Item Name");
                model1.addColumn("Item Quantity");
                model1.addColumn("Item Rate");
                model1.addColumn("Item Price");
                model1.addColumn("Tax Ammount");

                try{
                    Statement statementselect = connection.createStatement();
                    ResultSet resultsetselect = statementselect.executeQuery(searchquery1);
                    float total = 0;
                    while(resultsetselect.next()){
                        Object[] row = new Object[6];
                        row[0]=resultsetselect.getObject("Item_Id");
                        row[1]=resultsetselect.getObject("Item_Name");
                        row[2]=resultsetselect.getObject("Quantity");
                        row[3]=resultsetselect.getObject("Rate");
                        row[4]=resultsetselect.getObject("Price");
                        row[5]=resultsetselect.getObject("Tax");
                        total+=resultsetselect.getFloat("Price");
                        total+=resultsetselect.getFloat("Tax");
                        model1.addRow(row);
                    }
                    Object[] row = new Object[6];
                    row[0]="";
                    row[1]="";
                    row[2]="";
                    row[3]="";
                    row[4]="Total";
                    row[5]=total;
                    model1.addRow(row);
                    datatable1.setModel(model1);
                } catch(SQLException e1){
                    e1.printStackTrace();
                }
                    }
        });

        JPanel panel1 = new JPanel();
        panel1.setBackground(new Color(0x99ccff));
        panel1.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel stock = new JLabel("Bill Details");
        stock.setForeground(new Color(0x293dff));
        stock.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        stock.setVerticalAlignment(JLabel.TOP);
        stock.setHorizontalAlignment(JLabel.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(stock, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scroll, gbc);

        JPanel panel2 = new JPanel();
        panel2.setBackground(new Color(0x99ccff));
        panel2.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();

        JLabel stock1 = new JLabel("Bill Items");
        stock1.setForeground(new Color(0x293dff));
        stock1.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        stock1.setVerticalAlignment(JLabel.TOP);
        stock1.setHorizontalAlignment(JLabel.CENTER);

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0;
        gbc2.weighty = 0.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(stock1, gbc2);

        gbc2.gridy = 1;
        gbc2.weighty = 1.0;
        gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        panel2.add(scroll1, gbc2);

        frame.add(panel1);
        frame.add(panel2);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void view_customers(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        JTextField search = new JTextField();
        search.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        search.setBackground(new Color(0x99ccff));
        search.setForeground(new Color(0x000099));
        search.setCaretColor(new Color(0x000099));
        search.setBounds(50, 30, 750, 40);

        String searchquery = "SELECT * FROM customers";

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Customer ID");
        model.addColumn("Customer Name");
        model.addColumn("Customer Company");
        model.addColumn("GSTIN");
        model.addColumn("Phone Number");
        model.addColumn("Email");
        model.addColumn("Address");

        try{
            Statement statementselect = connection.createStatement();
            ResultSet resultsetselect = statementselect.executeQuery(searchquery);
            while(resultsetselect.next()){
                Object[] row = new Object[7];
                row[0]=resultsetselect.getObject("Customer_id");
                row[1]=resultsetselect.getObject("Customer_Name");
                row[2]=resultsetselect.getObject("Customer_Company");
                row[3]=resultsetselect.getObject("GSTIN");
                row[4]=resultsetselect.getObject("Phone_Number");
                row[5]=resultsetselect.getObject("Email");
                row[6]=resultsetselect.getObject("Address");
                model.addRow(row);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        JTable datatable = new JTable(model);
        datatable.setBackground(new Color(0x99ccff));
        JScrollPane scroll = new JScrollPane(datatable);
        scroll.setBounds(50, 100, 1600, 750);

        ImageIcon searchimage = new ImageIcon("Search_Logo.png");

        JButton searchbutton = new JButton("Search");
        searchbutton.setFocusable(false);
        searchbutton.setIcon(searchimage);
        searchbutton.setHorizontalTextPosition(JButton.RIGHT);
        searchbutton.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        searchbutton.setBackground(new Color(0x4c73ff));
        searchbutton.setForeground(new Color(0x000099));
        searchbutton.setBounds(825, 30, 150, 40);
        searchbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if (search.getText() != null){
                    DefaultTableModel modelsq = new DefaultTableModel();
                    modelsq.addColumn("Customer ID");
                    modelsq.addColumn("Customer Name");
                    modelsq.addColumn("Customer Company");
                    modelsq.addColumn("GSTIN");
                    modelsq.addColumn("Phone Number");
                    modelsq.addColumn("Email");
                    modelsq.addColumn("Address");
                    String sq = "SELECT * FROM customers WHERE Customer_Id LIKE '%" + search.getText() + "%' or Customer_Name LIKE '%" + search.getText() + "%' or Customer_Company LIKE '%" + search.getText() + "%' or GSTIN LIKE '%" + search.getText() + "%' or Phone_Number LIKE '%" + search.getText() + "%' or Email LIKE '%" + search.getText() + "%' or Address LIKE '%" + search.getText() + "%'";
                    try{
                        Statement statementselect = connection.createStatement();
                        ResultSet resultsetselect = statementselect.executeQuery(sq);
                        while(resultsetselect.next()){
                            Object[] row = new Object[7];
                            row[0]=resultsetselect.getObject("Customer_id");
                            row[1]=resultsetselect.getObject("Customer_Name");
                            row[2]=resultsetselect.getObject("Customer_Company");
                            row[3]=resultsetselect.getObject("GSTIN");
                            row[4]=resultsetselect.getObject("Phone_Number");
                            row[5]=resultsetselect.getObject("Email");
                            row[6]=resultsetselect.getObject("Address");
                            modelsq.addRow(row);
                        }
                    } catch(SQLException esq){
                        esq.printStackTrace();
                    }
                    datatable.setModel(modelsq);
                }
            }
        });

        frame.add(search);
        frame.add(searchbutton);
        frame.add(scroll);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void home_page(){

        frame.getContentPane().removeAll();
        frame.setLayout(new GridLayout(02, 02, 10, 10));
        frame.revalidate();
        frame.repaint();

        view.setEnabled(true);
        add.setEnabled(true);
        edit.setEnabled(true);
        homepage.setEnabled(true);

        JPanel panel1 = new JPanel();
        panel1.setBackground(new Color(0x99ccff));
        panel1.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        String query="SELECT * from item_stock";

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Item ID");
        model.addColumn("Item Name");
        model.addColumn("Item Quantity");

        try{
            Statement statementselect = connection.createStatement();
            ResultSet resultsetselect = statementselect.executeQuery(query);
            while(resultsetselect.next()){
                Object[] row = new Object[3];
                row[0]=resultsetselect.getObject("Item_Id");
                row[1]=resultsetselect.getObject("Item_Name");
                row[2]=resultsetselect.getObject("Item_Stock");
                model.addRow(row);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        JTable datatable = new JTable(model);
        datatable.setBackground(new Color(0x99ccff));
        JScrollPane scroll = new JScrollPane(datatable);

        JLabel stock = new JLabel("Item Stock");
        stock.setForeground(new Color(0x293dff));
        stock.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        stock.setVerticalAlignment(JLabel.TOP);
        stock.setHorizontalAlignment(JLabel.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(stock, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scroll, gbc);

        JPanel panel2 = new JPanel();
        panel2.setBackground(new Color(0x99ccff));
        panel2.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();

        String query2="SELECT orders.Order_Id, Order_Date, orders.Customer_Id, customers.Customer_Name, customers.Customer_Company, Order_Status, payments.Payment_Method, payments.Payment_Status from orders, customers, payments where Order_Status = 'Pending' and orders.Customer_Id = customers.Customer_id and orders.Order_Id = payments.Order_Id";

        DefaultTableModel model2 = new DefaultTableModel();
        model2.addColumn("Order ID");
        model2.addColumn("Order Date");
        model2.addColumn("Customer ID");
        model2.addColumn("Customer Name");
        model2.addColumn("Customer Company");
        model2.addColumn("Order Status");
        model2.addColumn("Payment Method");
        model2.addColumn("Payment Status");

        try{
            Statement statementselect2 = connection.createStatement();
            ResultSet resultsetselect2 = statementselect2.executeQuery(query2);
            while(resultsetselect2.next()){
                Object[] row2 = new Object[8];
                row2[0]=resultsetselect2.getObject("Order_Id");
                row2[1]=resultsetselect2.getObject("Order_Date");
                row2[2]=resultsetselect2.getObject("Customer_Id");
                row2[3]=resultsetselect2.getObject("Customer_Name");
                row2[4]=resultsetselect2.getObject("Customer_Company");
                row2[5]=resultsetselect2.getObject("Order_Status");
                row2[6]=resultsetselect2.getObject("Payment_Method");
                row2[7]=resultsetselect2.getObject("Payment_Status");
                model2.addRow(row2);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        JTable datatable2 = new JTable(model2);
        datatable2.setBackground(new Color(0x99ccff));
        JScrollPane scroll2 = new JScrollPane(datatable2);

        JLabel orders = new JLabel("Pending Orders");
        orders.setForeground(new Color(0x293dff));
        orders.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        orders.setVerticalAlignment(JLabel.TOP);
        orders.setHorizontalAlignment(JLabel.CENTER);

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0;
        gbc2.weighty = 0.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(orders, gbc2);

        gbc2.gridy = 1;
        gbc2.weighty = 1.0;
        gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        panel2.add(scroll2, gbc2);

        JPanel panel3 = new JPanel();
        panel3.setBackground(new Color(0x99ccff));
        panel3.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel3.setLayout(new GridBagLayout());
        GridBagConstraints gbc3 = new GridBagConstraints();

        String query3="SELECT * FROM bills NATURAL JOIN payments NATURAL JOIN customers WHERE Payment_Status = 'Pending' OR Payment_Status = 'Partial Payment'";

        DefaultTableModel model3 = new DefaultTableModel();
        model3.addColumn("Bill ID");
        model3.addColumn("Order ID");
        model3.addColumn("Bill Date");
        model3.addColumn("Bill Time");
        model3.addColumn("Customer Name");
        model3.addColumn("Customer Company");
        model3.addColumn("Payment Method");
        model3.addColumn("Payment Status");
        model3.addColumn("Created by");

        try{
            Statement statementselect3 = connection.createStatement();
            ResultSet resultsetselect3 = statementselect3.executeQuery(query3);
            while(resultsetselect3.next()){
                Object[] row3 = new Object[9];
                row3[0]=resultsetselect3.getObject("Bill_Id");
                row3[1]=resultsetselect3.getObject("Order_Id");
                row3[2]=resultsetselect3.getObject("Date");
                row3[3]=resultsetselect3.getObject("Time");
                row3[4]=resultsetselect3.getObject("Customer_Name");
                row3[5]=resultsetselect3.getObject("Customer_Company");
                row3[6]=resultsetselect3.getObject("Payment_Method");
                row3[7]=resultsetselect3.getObject("Payment_Status");
                row3[8]=resultsetselect3.getObject("User_Name");
                model3.addRow(row3);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        JTable datatable3 = new JTable(model3);
        datatable3.setBackground(new Color(0x99ccff));
        JScrollPane scroll3 = new JScrollPane(datatable3);
        
        JLabel payment = new JLabel("Pending Payments");
        payment.setForeground(new Color(0x293dff));
        payment.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        payment.setVerticalAlignment(JLabel.TOP);
        payment.setHorizontalAlignment(JLabel.CENTER);

        gbc3.gridx = 0;
        gbc3.gridy = 0;
        gbc3.weightx = 1.0;
        gbc3.weighty = 0.0;
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(payment, gbc3);

        gbc3.gridy = 1;
        gbc3.weighty = 1.0;
        gbc3.weightx = 1.0;
        gbc3.fill = GridBagConstraints.BOTH;
        panel3.add(scroll3, gbc3);

        JPanel panel4 = new JPanel();
        panel4.setBackground(new Color(0x99ccff));
        panel4.setBorder(BorderFactory.createLineBorder(new Color(0x000099)));
        panel4.setLayout(null);

        String gst = "";
        String adr = "";
        String eml = "";
        String web = "";
        String phn = "";

        try{
            String querycomapny = "SELECT * FROM Company_Details";
            Statement statement = connection.createStatement();
            ResultSet resultSetcompany = statement.executeQuery(querycomapny);
            resultSetcompany.next();
            gst = resultSetcompany.getString("GSTIN");
            adr = resultSetcompany.getString("Office_Address");
            eml = resultSetcompany.getString("Email");
            web = resultSetcompany.getString("Website");
            phn = resultSetcompany.getString("Phone_Number");
        }catch(SQLException e){
            e.printStackTrace();
        }catch(Exception e1){
            e1.printStackTrace();
        }

        JLabel heading = new JLabel("Company Details");
        heading.setForeground(new Color(0x293dff));
        heading.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        heading.setVerticalAlignment(JLabel.CENTER);
        heading.setHorizontalAlignment(JLabel.CENTER);
        heading.setBounds(345, 0, 155, 35);

        JLabel gstin = new JLabel("GSTIN:");
        gstin.setForeground(new Color(0x293dff));
        gstin.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        gstin.setVerticalAlignment(JLabel.CENTER);
        gstin.setHorizontalAlignment(JLabel.LEFT);
        gstin.setBounds(10, 55, 60, 35);

        JTextField gstintext = new JTextField(gst);
        gstintext.setEditable(false);
        gstintext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        gstintext.setBackground(new Color(0x99ccff));
        gstintext.setForeground(new Color(0x000099));
        gstintext.setCaretColor(new Color(0x000099));
        gstintext.setBounds(80, 55, 200, 35);

        JLabel address = new JLabel("Office Address:");
        address.setForeground(new Color(0x293dff));
        address.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        address.setVerticalAlignment(JLabel.CENTER);
        address.setHorizontalAlignment(JLabel.LEFT);
        address.setBounds(10, 100, 145, 35);

        JTextField addresstext = new JTextField(adr);
        addresstext.setEditable(false);
        addresstext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        addresstext.setBackground(new Color(0x99ccff));
        addresstext.setForeground(new Color(0x000099));
        addresstext.setCaretColor(new Color(0x000099));
        addresstext.setBounds(165, 100, 500, 35);

        JLabel email = new JLabel("Email:");
        email.setForeground(new Color(0x293dff));
        email.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        email.setVerticalAlignment(JLabel.CENTER);
        email.setHorizontalAlignment(JLabel.LEFT);
        email.setBounds(10, 145, 55, 35);

        JTextField emailtext = new JTextField(eml);
        emailtext.setEditable(false);
        emailtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        emailtext.setBackground(new Color(0x99ccff));
        emailtext.setForeground(new Color(0x000099));
        emailtext.setCaretColor(new Color(0x000099));
        emailtext.setBounds(75, 145, 400, 35);

        JLabel website = new JLabel("Website:");
        website.setForeground(new Color(0x293dff));
        website.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        website.setVerticalAlignment(JLabel.CENTER);
        website.setHorizontalAlignment(JLabel.LEFT);
        website.setBounds(10, 190, 85, 35);

        JTextField websitetext = new JTextField(web);
        websitetext.setEditable(false);
        websitetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        websitetext.setBackground(new Color(0x99ccff));
        websitetext.setForeground(new Color(0x000099));
        websitetext.setCaretColor(new Color(0x000099));
        websitetext.setBounds(105, 190, 400, 35);

        JLabel phone = new JLabel("Phone:");
        phone.setForeground(new Color(0x293dff));
        phone.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        phone.setVerticalAlignment(JLabel.CENTER);
        phone.setHorizontalAlignment(JLabel.LEFT);
        phone.setBounds(10, 235, 60, 35);

        JTextField phonetext = new JTextField(phn);
        phonetext.setEditable(false);
        phonetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        phonetext.setBackground(new Color(0x99ccff));
        phonetext.setForeground(new Color(0x000099));
        phonetext.setCaretColor(new Color(0x000099));
        phonetext.setBounds(80, 235, 200, 35);

        panel4.add(heading);
        panel4.add(gstin);
        panel4.add(gstintext);
        panel4.add(address);
        panel4.add(addresstext);
        panel4.add(email);
        panel4.add(emailtext);
        panel4.add(website);
        panel4.add(websitetext);
        panel4.add(phone);
        panel4.add(phonetext);

        frame.add(panel1);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(panel4);

        frame.setVisible(true);

        frame.revalidate();
        frame.repaint();
    }

    private static void adduser_page(){

        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        frame.setLayout(null);

        view.setEnabled(false);
        add.setEnabled(false);
        edit.setEnabled(false);
        homepage.setEnabled(false);

        JButton adduser = new JButton("Add New User");
        JLabel company = new JLabel("Company:");
        JTextField companytext = new JTextField();
        JLabel user = new JLabel("Username:");
        JTextField usertext = new JTextField();
        JLabel pass = new JLabel("Password:");
        JPasswordField passtext = new JPasswordField();

        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.TOP);
        company.setHorizontalAlignment(JLabel.CENTER);
        company.setBounds(690, 100, 115, 35);
        frame.add(company);

        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));
        companytext.setCaretColor(new Color(0x000099));
        companytext.setBounds(815, 100, 192, 35);
        frame.add(companytext);

        user.setForeground(new Color(0x293dff));
        user.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        user.setVerticalAlignment(JLabel.TOP);
        user.setHorizontalAlignment(JLabel.CENTER);
        user.setBounds(690, 145, 115, 35);
        frame.add(user);

        usertext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        usertext.setBackground(new Color(0x99ccff));
        usertext.setForeground(new Color(0x000099));
        usertext.setCaretColor(new Color(0x000099));
        usertext.setBounds(815, 145, 192, 35);
        frame.add(usertext);

        pass.setForeground(new Color(0x293dff));
        pass.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        pass.setVerticalAlignment(JLabel.TOP);
        pass.setHorizontalAlignment(JLabel.CENTER);
        pass.setBounds(690, 190, 115, 35);
        frame.add(pass);

        passtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        passtext.setEchoChar('*');
        passtext.setBackground(new Color(0x99ccff));
        passtext.setForeground(new Color(0x000099));
        passtext.setCaretColor(new Color(0x000099));
        passtext.setBounds(815, 190, 192, 35);
        frame.add(passtext);

        adduser.setFocusable(false);
        adduser.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        adduser.setBackground(new Color(0x4c73ff));
        adduser.setForeground(new Color(0x000099));
        adduser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String companyName = companytext.getText().replace(" ", "_");
                String userName = usertext.getText();
                String password = String.valueOf(passtext.getPassword());
                if (companyName.length()!=0 && userName.length()!=0 && password.length()!=0){
                    try{
                        connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
                        createDatabase(connection, dbName);  
                        connection.setCatalog(dbName);
                        createLoginTable(connection);
                        insertUserData(connection, companyName, userName, password);
                        JOptionPane.showMessageDialog(null, "User Data Entered", "Information", JOptionPane.INFORMATION_MESSAGE);
                        login_page();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "Please Enter Valid Data!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        adduser.setBounds(779, 245, 147, 30);
        frame.add(adduser);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();
    }

    private static void company_details(){

        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.validate();
        frame.repaint();

        JLabel gstin = new JLabel("GSTIN:");
        gstin.setForeground(new Color(0x293dff));
        gstin.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        gstin.setVerticalAlignment(JLabel.CENTER);
        gstin.setHorizontalAlignment(JLabel.LEFT);
        gstin.setBounds(730, 70, 60, 35);

        JTextField gstintext = new JTextField();
        gstintext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        gstintext.setBackground(new Color(0x99ccff));
        gstintext.setForeground(new Color(0x000099));
        gstintext.setCaretColor(new Color(0x000099));
        gstintext.setBounds(800, 70, 200, 35);

        JLabel address = new JLabel("Office Address:");
        address.setForeground(new Color(0x293dff));
        address.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        address.setVerticalAlignment(JLabel.CENTER);
        address.setHorizontalAlignment(JLabel.LEFT);
        address.setBounds(645, 120, 145, 35);

        JTextField addresstext = new JTextField();
        addresstext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        addresstext.setBackground(new Color(0x99ccff));
        addresstext.setForeground(new Color(0x000099));
        addresstext.setCaretColor(new Color(0x000099));
        addresstext.setBounds(800, 120, 500, 35);

        JLabel email = new JLabel("Email:");
        email.setForeground(new Color(0x293dff));
        email.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        email.setVerticalAlignment(JLabel.CENTER);
        email.setHorizontalAlignment(JLabel.LEFT);
        email.setBounds(735, 170, 55, 35);

        JTextField emailtext = new JTextField();
        emailtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        emailtext.setBackground(new Color(0x99ccff));
        emailtext.setForeground(new Color(0x000099));
        emailtext.setCaretColor(new Color(0x000099));
        emailtext.setBounds(800, 170, 400, 35);

        JLabel website = new JLabel("Website:");
        website.setForeground(new Color(0x293dff));
        website.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        website.setVerticalAlignment(JLabel.CENTER);
        website.setHorizontalAlignment(JLabel.LEFT);
        website.setBounds(705, 220, 85, 35);

        JTextField websitetext = new JTextField();
        websitetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        websitetext.setBackground(new Color(0x99ccff));
        websitetext.setForeground(new Color(0x000099));
        websitetext.setCaretColor(new Color(0x000099));
        websitetext.setBounds(800, 220, 400, 35);

        JLabel phone = new JLabel("Phone:");
        phone.setForeground(new Color(0x293dff));
        phone.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        phone.setVerticalAlignment(JLabel.CENTER);
        phone.setHorizontalAlignment(JLabel.LEFT);
        phone.setBounds(730, 270, 60, 35);

        JTextField phonetext = new JTextField();
        phonetext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        phonetext.setBackground(new Color(0x99ccff));
        phonetext.setForeground(new Color(0x000099));
        phonetext.setCaretColor(new Color(0x000099));
        phonetext.setBounds(800, 270, 300, 35);

        JButton adddetails = new JButton("Add Details");
        adddetails.setFocusable(false);
        adddetails.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        adddetails.setBackground(new Color(0x4c73ff));
        adddetails.setForeground(new Color(0x000099));
        adddetails.setBounds(780, 400, 140, 35);
        adddetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try{
                    
                    if(gstintext.getText().length()!=0 && addresstext.getText().length()!=0 && emailtext.getText().length()!=0 && websitetext.getText().length()!=0 && phonetext.getText().length()!=0){
                        String query = "INSERT INTO Company_Details VALUES ('" + gstintext.getText() + "', '" + addresstext.getText() + "', '" + emailtext.getText() + "', '" + websitetext.getText() + "', '" + phonetext.getText() + "')";
                        Statement statement = connection.createStatement();
                        statement.execute(query);
                        JOptionPane.showMessageDialog(null, "Details Entered", "Information", JOptionPane.INFORMATION_MESSAGE);
                        home_page();
                    }else{
                        JOptionPane.showMessageDialog(null, "Enter Valid Details!!!", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                        
                }catch(SQLException e1){
                    JOptionPane.showMessageDialog(null, "Could not Enter Details!!!", "Error!", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }catch(Exception e2){
                    JOptionPane.showMessageDialog(null, "Could not Enter Details!!!", "Error!", JOptionPane.ERROR_MESSAGE);
                    e2.printStackTrace();
                }

            }
        });
        
        frame.add(gstin);
        frame.add(gstintext);
        frame.add(address);
        frame.add(addresstext);
        frame.add(email);
        frame.add(emailtext);
        frame.add(website);
        frame.add(websitetext);
        frame.add(phone);
        frame.add(phonetext);
        frame.add(adddetails);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();

    }

    private static void login_page(){

        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.validate();
        frame.repaint();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        ImageIcon image = new ImageIcon("Logo.jpg");
        frame.setIconImage(image.getImage());
        
        frame.getContentPane().setBackground(new Color(0x99ccff));

        frame.setLayout(null);

        JButton login = new JButton("Login");
        JButton adduser = new JButton("Add New User");
        JLabel company = new JLabel("Company:");
        JTextField companytext = new JTextField();
        JLabel user = new JLabel("Username:");
        JTextField usertext = new JTextField();
        JLabel pass = new JLabel("Password:");
        JPasswordField passtext = new JPasswordField();

        file.setMnemonic(KeyEvent.VK_F);

        homepage.setMnemonic(KeyEvent.VK_H);
        homepage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                home_page();
            }
        });
        homepage.setEnabled(false);

        exit.setMnemonic(KeyEvent.VK_E);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }
        });

        logout.setMnemonic(KeyEvent.VK_L);
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                login_page();
            }
        });

        addusermenu.setMnemonic(KeyEvent.VK_A);
        addusermenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                adduser_page();
            }
        });

        file.add(homepage);
        file.add(addusermenu);
        file.add(logout);
        file.add(exit);

        view.setMnemonic(KeyEvent.VK_V);
        view.setEnabled(false);

        viewcustomer.setMnemonic(KeyEvent.VK_C);
        viewcustomer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                view_customers();
            }
        });

        viewbill.setMnemonic(KeyEvent.VK_B);
        viewbill.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                view_bill();
            }
        });

        vieworder.setMnemonic(KeyEvent.VK_O);
        vieworder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                view_order();
            }
        });

        view.add(viewcustomer);
        view.add(viewbill);
        view.add(vieworder);

        add.setMnemonic(KeyEvent.VK_A);
        add.setEnabled(false);

        addcustomer.setMnemonic(KeyEvent.VK_C);
        addcustomer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                add_customer();
            }
        });

        addorder.setMnemonic(KeyEvent.VK_O);
        addorder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                add_orders();
            }
        });

        makebill.setMnemonic(KeyEvent.VK_B);
        makebill.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                add_bills();
            }
        });

        additem.setMnemonic(KeyEvent.VK_I);
        additem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                add_item();
            }
        });

        edit.setMnemonic(KeyEvent.VK_E);
        edit.setEnabled(false);

        editpayment.setMnemonic(KeyEvent.VK_P);
        editpayment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                edit_payment();
            }
        });

        editorder.setMnemonic(KeyEvent.VK_O);
        editorder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                edit_orders();
            }
        });

        editbill.setMnemonic(KeyEvent.VK_B);
        editbill.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                edit_bills();
            }
        });

        editstock.setMnemonic(KeyEvent.VK_S);
        editstock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                edit_item();
            }
        });

        add.add(addcustomer);
        add.add(addorder);
        add.add(makebill);
        add.add(additem);

        edit.add(editpayment);
        edit.add(editorder);
        edit.add(editbill);
        edit.add(editstock);

        menuBar.add(file);
        menuBar.add(view);
        menuBar.add(add);
        menuBar.add(edit);

        frame.setJMenuBar(menuBar);

        company.setForeground(new Color(0x293dff));
        company.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        company.setVerticalAlignment(JLabel.TOP);
        company.setHorizontalAlignment(JLabel.CENTER);
        company.setBounds(690, 100, 115, 35);
        frame.add(company);

        companytext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        companytext.setBackground(new Color(0x99ccff));
        companytext.setForeground(new Color(0x000099));
        companytext.setCaretColor(new Color(0x000099));
        companytext.setBounds(815, 100, 192, 35);
        frame.add(companytext);

        user.setForeground(new Color(0x293dff));
        user.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        user.setVerticalAlignment(JLabel.TOP);
        user.setHorizontalAlignment(JLabel.CENTER);
        user.setBounds(690, 145, 115, 35);
        frame.add(user);

        usertext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        usertext.setBackground(new Color(0x99ccff));
        usertext.setForeground(new Color(0x000099));
        usertext.setCaretColor(new Color(0x000099));
        usertext.setBounds(815, 145, 192, 35);
        frame.add(usertext);

        pass.setForeground(new Color(0x293dff));
        pass.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        pass.setVerticalAlignment(JLabel.TOP);
        pass.setHorizontalAlignment(JLabel.CENTER);
        pass.setBounds(690, 190, 115, 35);
        frame.add(pass);

        passtext.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        passtext.setEchoChar('*');
        passtext.setBackground(new Color(0x99ccff));
        passtext.setForeground(new Color(0x000099));
        passtext.setCaretColor(new Color(0x000099));
        passtext.setBounds(815, 190, 192, 35);
        frame.add(passtext);

        adduser.setFocusable(false);
        adduser.setFont(new Font("Haettenschweiler", Font.PLAIN, 25));
        adduser.setBackground(new Color(0x4c73ff));
        adduser.setForeground(new Color(0x000099));
        adduser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                adduser_page();
            }
        });
        adduser.setBounds(15, 15, 147, 30);
        frame.add(adduser);

        login.setBounds(810, 245, 85, 35);
        login.setFocusable(false);
        login.setFont(new Font("Haettenschweiler", Font.PLAIN, 30));
        login.setBackground(new Color(0x4c73ff));
        login.setForeground(new Color(0x000099));
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String companyName = companytext.getText().replace(" ", "_");
                String userName = usertext.getText();
                String password = String.valueOf(passtext.getPassword());
                try {
                    connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
                    createDatabase(connection, dbName);  
                    connection.setCatalog(dbName);
                    createLoginTable(connection);
                    if(!isLoginTableEmpty(connection)){
                        if (checkCredentials(connection, companyName, userName, password)) {
                            connection.setCatalog(companyName);
                            username = userName;
                            String checkquery = "SELECT * FROM company_details";
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery(checkquery);
                            int len=0;
                            while (resultSet.next()) {
                                len++;
                            }
                            if(len==0){
                                company_details();
                            }else{
                                home_page();
                            }
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "Incorrect Credintials!!!", "Error!", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "No User Data Found!!!", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        frame.add(login);

        frame.setVisible(true);

        frame.validate();
        frame.repaint();
    }

    protected void finalize(){
        try {
            connection.close();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args){
        login_page();
    }
}