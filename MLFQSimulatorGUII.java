import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

class Process {
    int id;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int completionTime;
    int waitingTime;
    int turnaroundTime;
    String algorithmUsed;

    public Process(int id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}

public class MLFQSimulatorGUI extends JFrame {
    private JTextField processField, arrivalTimeField, burstTimeField, timeQuantumField;
    private JButton addProcessButton, runButton, resetButton;
    private DefaultTableModel tableModel;
    private List<Process> processes = new ArrayList<>();
    private int timeQuantum;

    public MLFQSimulatorGUI() {
        setTitle("MLFQ Scheduler Simulator");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        JPanel inputPanel = new JPanel();
        inputPanel.setBounds(0, 0, 784, 188);

        processField = new JTextField();
        processField.setBounds(150, 0, 150, 25);
        arrivalTimeField = new JTextField();
        arrivalTimeField.setBounds(150, 33, 150, 25);
        burstTimeField = new JTextField();
        burstTimeField.setBounds(150, 66, 150, 25);
        timeQuantumField = new JTextField();
        timeQuantumField.setBounds(150, 99, 150, 25);
        inputPanel.setLayout(null);

        JLabel lblProcess = new JLabel("Process ID:");
        lblProcess.setBounds(80, 0, 70, 25);
        lblProcess.setFont(new Font("Tahoma", Font.BOLD, 12));
        inputPanel.add(lblProcess);
        inputPanel.add(processField);
        JLabel lblArrivalTime = new JLabel("Arrival Time:");
        lblArrivalTime.setBounds(73, 33, 75, 25);
        lblArrivalTime.setFont(new Font("Tahoma", Font.BOLD, 12));
        inputPanel.add(lblArrivalTime);
        inputPanel.add(arrivalTimeField);
        JLabel lblBurstTime = new JLabel("Burst Time:");
        lblBurstTime.setBounds(78, 66, 70, 25);
        lblBurstTime.setFont(new Font("Tahoma", Font.BOLD, 12));
        inputPanel.add(lblBurstTime);
        inputPanel.add(burstTimeField);
        JLabel lblTimeQuantum = new JLabel("Time Quantum for RR:");
        lblTimeQuantum.setBounds(10, 99, 140, 25);
        lblTimeQuantum.setFont(new Font("Tahoma", Font.BOLD, 12));
        inputPanel.add(lblTimeQuantum);
        inputPanel.add(timeQuantumField);

        addProcessButton = new JButton("Add Process");
        addProcessButton.setFont(new Font("Tahoma", Font.BOLD, 12));
        addProcessButton.setBounds(350, 40, 150, 23);
        runButton = new JButton("Run Scheduler");
        runButton.setFont(new Font("Tahoma", Font.BOLD, 12));
        runButton.setBounds(350, 70, 150, 23);
        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Tahoma", Font.BOLD, 12));
        resetButton.setBounds(515, 55, 150, 25);

        inputPanel.add(addProcessButton);
        inputPanel.add(runButton);
        inputPanel.add(resetButton);

        getContentPane().add(inputPanel);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Process");
        tableModel.addColumn("Arrival Time");
        tableModel.addColumn("Burst Time");
        tableModel.addColumn("Completion Time");
        tableModel.addColumn("Waiting Time");
        tableModel.addColumn("Turnaround Time");
        tableModel.addColumn("Algorithm");

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(0, 188, 784, 273);
        getContentPane().add(scrollPane);

        addProcessButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProcess();
            }
        });

        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScheduler();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetAll();
            }
        });
    }

    private void addProcess() {
        try {
            int id = Integer.parseInt(processField.getText());
            int arrivalTime = Integer.parseInt(arrivalTimeField.getText());
            int burstTime = Integer.parseInt(burstTimeField.getText());

            Process process = new Process(id, arrivalTime, burstTime);
            processes.add(process);

            // Immediately display the process in the table with initial values
            tableModel.addRow(new Object[]{
                "P" + process.id,
                process.arrivalTime,
                process.burstTime,
                "-", "-", "-", "-"
            });

            processField.setText("");
            arrivalTimeField.setText("");
            burstTimeField.setText("");
            JOptionPane.showMessageDialog(this, "Process added successfully!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integer values.");
        }
    }

    private void runScheduler() {
        try {
            timeQuantum = Integer.parseInt(timeQuantumField.getText());
            scheduleProcesses(processes);
            displayProcessDetails(processes);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid time quantum.");
        }
    }

    private void resetAll() {
        processes.clear();
        tableModel.setRowCount(0); // Clear the table
        processField.setText("");
        arrivalTimeField.setText("");
        burstTimeField.setText("");
        timeQuantumField.setText("");
    }

    private void scheduleProcesses(List<Process> processes) {
        int time = 0;
        Queue<Process> queue1 = new LinkedList<>();
        Queue<Process> queue2 = new PriorityQueue<>(Comparator.comparingInt(p -> p.remainingTime)); // SJF
        Queue<Process> queue3 = new LinkedList<>(); // FCFS

        for (Process process : processes) {
            if (process.arrivalTime <= time) {
                queue1.add(process);
            }
        }

        while (!queue1.isEmpty() || !queue2.isEmpty() || !queue3.isEmpty()) {
            if (!queue1.isEmpty()) {
                Process p = queue1.poll();
                p.algorithmUsed = "Round Robin";
                int execTime = Math.min(timeQuantum, p.remainingTime);
                time += execTime;
                p.remainingTime -= execTime;

                if (p.remainingTime > 0) {
                    queue2.add(p); // Move to the next queue
                } else {
                    p.completionTime = time;
                }

            } else if (!queue2.isEmpty()) {
                Process p = queue2.poll();
                p.algorithmUsed = "SJF";
                time += p.remainingTime;
                p.remainingTime = 0;
                p.completionTime = time;
            } else if (!queue3.isEmpty()) {
                Process p = queue3.poll();
                p.algorithmUsed = "FCFS";
                time += p.remainingTime;
                p.remainingTime = 0;
                p.completionTime = time;
            }

            for (Process process : processes) {
                if (process.arrivalTime <= time && process.remainingTime > 0 &&
                    !queue1.contains(process) && !queue2.contains(process) && !queue3.contains(process)) {
                    queue1.add(process);
                }
            }

            while (!queue2.isEmpty() && queue2.peek().remainingTime > 0) {
                queue3.add(queue2.poll());
            }
        }

        for (Process process : processes) {
            process.turnaroundTime = process.completionTime - process.arrivalTime;
            process.waitingTime = process.turnaroundTime - process.burstTime;
        }
    }

    private void displayProcessDetails(List<Process> processes) {
        tableModel.setRowCount(0); // Clear previous results
        for (Process process : processes) {
            tableModel.addRow(new Object[]{
                "P" + process.id,
                process.arrivalTime,
                process.burstTime,
                process.completionTime,
                process.waitingTime,
                process.turnaroundTime,
                process.algorithmUsed
            });
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	MLFQSimulatorGUI simulatorGUI = new MLFQSimulatorGUI();
            simulatorGUI.setVisible(true);
        });
    }
}
