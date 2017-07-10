package com.nic.blackbox;

import java.awt.Frame;
import com.nic.streamprocessor.ui.SeriesVisualiserPanel;
import com.nic.streamprocessor.streamtargets.MemoryStreamTarget;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class SimulatorFrame extends JFrame implements MouseListener
{
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	protected Simulator sim;
	protected SeriesVisualiserPanel seriesVisualiserPanel;
	
	public SimulatorFrame() 
	{
		setTitle("PID Simulator");
		setBounds(100, 100, 800, 500);

		seriesVisualiserPanel = new SeriesVisualiserPanel(null);
		getContentPane().add(seriesVisualiserPanel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.WEST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JButton btnSimulate = new JButton("Simulate");
		panel.add(btnSimulate);

		
		JButton btnGenerate = new JButton("Generate");
		panel.add(btnGenerate);
		
		JLabel lblNewLabel = new JLabel("P Factor");
		panel.add(lblNewLabel);
		
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("I Factor");
		panel.add(lblNewLabel_1);
		
		textField_1 = new JTextField();
		panel.add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblDFactor = new JLabel("D Factor");
		panel.add(lblDFactor);
		
		textField_2 = new JTextField();
		panel.add(textField_2);
		textField_2.setColumns(10);	
		
	}
	
	public void setSourceAndTarget(Simulator s, MemoryStreamTarget t)
	{
		sim = s;
		seriesVisualiserPanel.setTarget(t);
	}


}
