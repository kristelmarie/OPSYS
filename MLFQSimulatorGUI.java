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
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2, 10, 10));

        processField = new JTextField();
        arrivalTimeField = new JTextField();
        burstTimeField = new JTextField();
        timeQuantumField = new JTextField();

        inputPanel.add(new JLabel("Process ID:"));
        inputPanel.add(processField);
        inputPanel.add(new JLabel("Arrival Time:"));
        inputPanel.add(arrivalTimeField);
        inputPanel.add(new JLabel("Burst Time:"));
        inputPanel.add(burstTimeField);
        inputPanel.add(new JLabel("Time Quantum for RR:"));
        inputPanel.add(timeQuantumField);

        addProcessButton = new JButton("Add Process");
        runButton = new JButton("Run Scheduler");
        resetButton = new JButton("Reset");

        inputPanel.add(addProcessButton);
        inputPanel.add(runButton);
        inputPanel.add(resetButton);

        add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Process");
        tableModel.addColumn("Arrival Time");
        tableModel.addColumn("Burst Time");
        tableModel.addColumn("Completion Time");
        tableModel.addColumn("Waiting Time");
        tableModel.addColumn("Turnaround Time");
        tableModel.addColumn("Algorithm");

        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

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
