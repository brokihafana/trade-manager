/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.ui.portfolio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SpinnerDateModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.MaskFormatter;

import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.TradelogDetail;
import org.trade.persistent.dao.TradelogReport;
import org.trade.persistent.dao.TradelogSummary;
import org.trade.ui.base.BaseButton;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.BaseUIPropertyCodes;
import org.trade.ui.base.ExampleFileChooser;
import org.trade.ui.base.ExampleFileFilter;
import org.trade.ui.base.FilePreviewer;
import org.trade.ui.base.Table;
import org.trade.ui.models.TradelogDetailTableModel;
import org.trade.ui.models.TradelogSummaryTableModel;
import org.trade.ui.tables.TradelogDetailTable;
import org.trade.ui.tables.TradelogSummaryTable;
import org.trade.ui.widget.DAODecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxRenderer;
import org.trade.ui.widget.MoneyField;
import org.trade.ui.widget.StringField;

/**
 */
public class PortfolioPanel extends BasePanel implements ChangeListener,
		ItemListener {

	private static final long serialVersionUID = 98016024273398947L;

	private PersistentModel m_tradePersistentModel = null;
	private TradelogReport m_tradelogReport = new TradelogReport();
	private String m_csvDefaultDir = null;
	private Table m_tableTradelogSummary = null;
	private TradelogSummaryTableModel m_tradelogSummaryModel = null;
	private Table m_tableTradelogDetail = null;
	private TradelogDetailTableModel m_tradelogDetailModel = null;
	private BaseButton transferButton = null;
	private JSpinner spinnerStart = new JSpinner();
	private JSpinner spinnerEnd = new JSpinner();
	private JCheckBox filterButton = new JCheckBox("Show not traded");
	private StringField symbolField = null;
	private DAODecodeComboBoxEditor portfolioEditorComboBox = null;
	private static final String DATEFORMAT = "MM/dd/yyyy";
	private TradelogDetail selectedTradelogDetail = null;
	private Portfolio portfolio = null;
	private MoneyField m_lossGainAmt = new MoneyField();

	private static final String MASK = "**********";
	private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789. ";
	private static final String PLACE_HOLDER = " ";

	/**
	 * Constructor for PortfolioPanel.
	 * 
	 * @param controller
	 *            BasePanel
	 * @param tradePersistentModel
	 *            PersistentModel
	 */

	public PortfolioPanel(BasePanel controller,
			PersistentModel tradePersistentModel) {
		try {
			if (null != getMenu())
				getMenu().addMessageListener(this);
			this.setLayout(new BorderLayout());

			m_tradePersistentModel = tradePersistentModel;
			m_csvDefaultDir = ConfigProperties
					.getPropAsString("trade.csv.default.dir");
			transferButton = new BaseButton(controller,
					BaseUIPropertyCodes.TRANSFER);
			JLabel portfolioLabel = new JLabel("Portfolio:");
			portfolioEditorComboBox = new DAODecodeComboBoxEditor(DAOPortfolio
					.newInstance().getCodesDecodes());
			DecodeComboBoxRenderer portfolioRenderer = new DecodeComboBoxRenderer();
			portfolioEditorComboBox.setRenderer(portfolioRenderer);
			this.portfolio = (Portfolio) DAOPortfolio.newInstance().getObject();
			portfolioEditorComboBox.setItem(DAOPortfolio.newInstance());
			portfolioEditorComboBox.addItemListener(this);

			m_tradelogSummaryModel = new TradelogSummaryTableModel();
			m_tableTradelogSummary = new TradelogSummaryTable(
					m_tradelogSummaryModel);
			m_tradelogDetailModel = new TradelogDetailTableModel();
			m_tableTradelogDetail = new TradelogDetailTable(
					m_tradelogDetailModel);
			spinnerStart.setModel(new SpinnerDateModel());
			JSpinner.DateEditor dateStart = new JSpinner.DateEditor(
					spinnerStart, DATEFORMAT);
			spinnerStart.setEditor(dateStart);
			spinnerStart.setValue((new Date(TradingCalendar.getYearStart()))
					.getDate());
			spinnerEnd.setModel(new SpinnerDateModel());
			JSpinner.DateEditor dateEnd = new JSpinner.DateEditor(spinnerEnd,
					DATEFORMAT);
			spinnerEnd.setEditor(dateEnd);
			spinnerEnd.setValue((new Date(TradingCalendar
					.getTradingDayStart(TradingCalendar
							.getDateTimeNowMarketTimeZone()))).getDate());
			filterButton.setSelected(false);

			JPanel jPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			jPanel1.setBorder(new BevelBorder(BevelBorder.RAISED));
			JLabel jLabelSummary = new JLabel("Ignor in Batting/Sharpe $ under");
			m_lossGainAmt.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jPanel1.add(jLabelSummary, null);
			jPanel1.add(m_lossGainAmt, null);
			JPanel jPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel startLabel = new JLabel("Start Date:");
			JLabel endLabel = new JLabel("End Date:");
			JLabel symbolLabel = new JLabel("Symbol:");
			symbolField = new StringField(new MaskFormatter(MASK), VALID_CHARS,
					PLACE_HOLDER);
			symbolField.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jPanel2.add(portfolioLabel, null);
			jPanel2.add(portfolioEditorComboBox, null);
			jPanel2.add(startLabel, null);
			jPanel2.add(spinnerStart, null);
			jPanel2.add(endLabel, null);
			jPanel2.add(spinnerEnd, null);
			jPanel2.add(symbolLabel, null);
			jPanel2.add(symbolField, null);
			jPanel2.add(filterButton, null);
			jPanel2.setBorder(new BevelBorder(BevelBorder.RAISED));

			JToolBar jToolBar = new JToolBar();
			jToolBar.setLayout(new BorderLayout());
			jToolBar.add(jPanel2, BorderLayout.WEST);
			jToolBar.add(jPanel1, BorderLayout.EAST);

			m_tableTradelogSummary.setEnabled(false);
			m_tableTradelogSummary.setFont(new Font("Monospaced", Font.PLAIN,
					12));
			JScrollPane jScrollPane = new JScrollPane();
			jScrollPane.getViewport().add(m_tableTradelogSummary,
					BorderLayout.CENTER);
			jScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jScrollPane.addMouseListener(m_tableTradelogSummary);
			JPanel jPanel3 = new JPanel(new BorderLayout());
			jPanel3.add(jScrollPane, BorderLayout.CENTER);

			m_tableTradelogDetail
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			m_tableTradelogDetail
					.setFont(new Font("Monospaced", Font.PLAIN, 12));
			JScrollPane jScrollPane1 = new JScrollPane();
			jScrollPane1.getViewport().add(m_tableTradelogDetail,
					BorderLayout.CENTER);
			jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jScrollPane1.addMouseListener(m_tableTradelogDetail);
			JPanel jPanel4 = new JPanel(new BorderLayout());
			jPanel4.add(jScrollPane1, BorderLayout.CENTER);

			JSplitPane jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					true, jPanel3, jPanel4);
			jSplitPane1.setOneTouchExpandable(true);
			jSplitPane1.setResizeWeight(0.2d);
			this.add(jToolBar, BorderLayout.NORTH);
			this.add(jSplitPane1, BorderLayout.CENTER);

			m_tableTradelogDetail.getSelectionModel().addListSelectionListener(
					new TradelogDetailTableRowListener());
			m_tableTradelogDetail.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						if (null != selectedTradelogDetail) {
							transferButton
									.setTransferObject(selectedTradelogDetail
											.getIdTradestrategy());
							transferButton.doClick();
						}
					}
				}
			});

		} catch (Exception ex) {
			this.setErrorMessage("Error during initialization.",
					ex.getMessage(), ex);
		}
	}

	public void doSearch() {

		try {

			ZonedDateTime startDate = TradingCalendar
					.getZonedDateTimeFromMilli(((java.util.Date) spinnerStart
							.getValue()).getTime());
			startDate = TradingCalendar.getDateAtTime(startDate, 0, 0, 0);

			ZonedDateTime endDate = TradingCalendar
					.getZonedDateTimeFromMilli(((java.util.Date) spinnerEnd
							.getValue()).getTime());
			endDate = TradingCalendar.getDateAtTime(endDate, 23, 59, 59);

			if (endDate.isBefore(startDate)) {
				startDate = TradingCalendar.getDateAtTime(endDate, 0, 0, 0);
				spinnerStart.setValue((new Date(startDate)).getDate());
			}
			String symbol = symbolField.getText().toUpperCase().trim();
			if (symbol.length() == 0)
				symbol = null;

			m_tradelogReport = m_tradePersistentModel.findTradelogReport(
					this.portfolio, startDate, endDate, filterButton
							.isSelected(), symbol, m_lossGainAmt.getMoney()
							.getBigDecimalValue());
			this.clearStatusBarMessage();
			if (m_tradelogReport.getTradelogDetail().isEmpty()) {
				this.setStatusBarMessage("No data found for selected criteria",
						INFORMATION);
			}

			m_tradelogDetailModel.setData(m_tradelogReport);
			m_tradelogSummaryModel.setData(m_tradelogReport);
			RowSorter<?> rsDetail = m_tableTradelogDetail.getRowSorter();
			rsDetail.setSortKeys(null);
			RowSorter<?> rsSummary = m_tableTradelogSummary.getRowSorter();
			rsSummary.setSortKeys(null);

		} catch (Exception ex) {
			this.setErrorMessage("Error finding Tradingday.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method itemStateChanged.
	 * 
	 * @param e
	 *            ItemEvent
	 * @see java.awt.event.ItemListener#itemStateChanged(ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {
			this.portfolio = (Portfolio) ((DAOPortfolio) e.getItem())
					.getObject();
		}
	}

	/**
	 * Method stateChanged.
	 * 
	 * @param e
	 *            ChangeEvent
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
	}

	public void doSaveAs() {
		doReadWrite(openFileChooser());
	}

	public void doOpen() {
	}

	/**
	 * This is fired when the menu-bar Contract Details is pressed from the
	 * Action menu.
	 * 
	 */
	public void doProperties() {
		if (null != selectedTradelogDetail) {
			transferButton.setTransferObject(selectedTradelogDetail
					.getIdTradestrategy());
			transferButton.doClick();
		}
	}

	public void doSave() {
		doReadWrite(openFileChooser());
	}

	public void doWindowOpen() {

	}

	public void doWindowClose() {
	}

	public void doWindowActivated() {

		try {
			resetPortfolioComboBox(portfolioEditorComboBox);
			if (m_tradelogReport.getTradelogDetail().isEmpty()) {
				doSearch();
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error activating window.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method doWindowDeActivated.
	 * 
	 * @return boolean
	 */
	public boolean doWindowDeActivated() {
		return true;
	}

	/**
	 * Method resetPortfolioComboBox.
	 * 
	 * @param editorComboBox
	 *            DAODecodeComboBoxEditor
	 * @throws ValueTypeException
	 */

	private void resetPortfolioComboBox(
			final DAODecodeComboBoxEditor editorComboBox)
			throws ValueTypeException {

		Vector<Decode> codesNew = ((new DAOPortfolio()).getCodesDecodes());
		DefaultComboBoxModel<Decode> model = new DefaultComboBoxModel<Decode>(
				codesNew);
		editorComboBox.setModel(model);
		editorComboBox.setItem(DAOPortfolio.newInstance());
		editorComboBox.setRenderer(new DecodeComboBoxRenderer());
	}

	/**
	 */
	private class TradelogDetailTableRowListener implements
			ListSelectionListener {
		/**
		 * Method valueChanged.
		 * 
		 * @param event
		 *            ListSelectionEvent
		 * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {
				ListSelectionModel model = (ListSelectionModel) event
						.getSource();
				if (model.getLeadSelectionIndex() > -1) {
					selectedTradelogDetail = m_tradelogDetailModel
							.getData()
							.getTradelogDetail()
							.get(m_tableTradelogDetail
									.convertRowIndexToModel(model
											.getLeadSelectionIndex()));
				}
			}
		}
	}

	/**
	 * Method openFileChooser.
	 * 
	 * @return String
	 */
	private String openFileChooser() {

		String fileName = null;
		ExampleFileFilter filter = new ExampleFileFilter(
				new String[] { "csv" }, "Portfolio Files");
		// Start in the curr dir

		if (null == m_csvDefaultDir) {
			m_csvDefaultDir = System.getProperty("user.dir");
		}

		JFileChooser filer1 = new JFileChooser(m_csvDefaultDir);
		ExampleFileChooser fileView = new ExampleFileChooser();
		filer1.setFileView(fileView);
		filer1.addChoosableFileFilter(filter);
		filer1.setFileFilter(filter);
		filer1.setAccessory(new FilePreviewer(filer1));

		int returnVal = 0;

		returnVal = filer1.showSaveDialog(this);

		// Upon return, getFile() will be null if user cancelled the dialog.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Non-null file property after return implies user
			// selected a file to open.
			// Call openFile to attempt to load the text from file into TextArea
			if (filer1.getSelectedFile().exists()) {

				int result = JOptionPane.showConfirmDialog(this.getFrame(),
						"File Exists. Do you want to over write ?", "Warning",
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					fileName = filer1.getSelectedFile().getPath();

				} else {
					// cancel
					return null;
				}
			} else {
				fileName = filer1.getSelectedFile().getPath();
			}
			if (!fileName.toUpperCase().endsWith(".CSV")) {
				return fileName + ".csv";
			}
			return fileName;
		}
		return null;
	}

	/**
	 * Method doReadWrite.
	 * 
	 * @param fileName
	 *            String
	 */
	private void doReadWrite(String fileName) {
		FileWriter fileWriter = null;
		PrintWriter writer = null;
		if (null != fileName) {
			this.setStatusBarMessage("Saving file " + fileName,
					BasePanel.INFORMATION);
			try {
				fileWriter = new FileWriter(fileName);
				writer = new PrintWriter(fileWriter);
				// Write out the header
				writer.println(TradelogSummaryTableModel.PERIOD + ","
						+ TradelogSummaryTableModel.BATTING_AVERAGE + ","
						+ TradelogSummaryTableModel.SHARPE_RATIO + ","
						+ TradelogSummaryTableModel.GROSS_PL + ","
						+ TradelogSummaryTableModel.QUANTITY + ","
						+ TradelogSummaryTableModel.COMMISSION + ","
						+ TradelogSummaryTableModel.NET_PL + ","
						+ TradelogSummaryTableModel.WIN_COUNT + ","
						+ TradelogSummaryTableModel.WIN_AMOUNT + ","
						+ TradelogSummaryTableModel.LOSS_COUNT + ","
						+ TradelogSummaryTableModel.LOSS_AMOUNT + ","
						+ TradelogSummaryTableModel.POSITION_COUNT + ","
						+ TradelogSummaryTableModel.CONTRACT_COUNT);
				// Write out the lines
				if (null != m_tradelogReport) {
					for (TradelogSummary tradelogSummary : m_tradelogReport
							.getTradelogSummary()) {
						writer.println(formatTradelogSummaryLine(tradelogSummary));
					}
				}
				// Write out the header
				writer.println(TradelogDetailTableModel.DATE + ","
						+ TradelogDetailTableModel.SYMBOL + ","
						+ TradelogDetailTableModel.LONGSHORT + ","
						+ TradelogDetailTableModel.TIER + ","
						+ TradelogDetailTableModel.MARKET_BIAS + ","
						+ TradelogDetailTableModel.MARKET_BAR + ","
						+ TradelogDetailTableModel.STRATEGY + ","
						+ TradelogDetailTableModel.STATUS + ","
						+ TradelogDetailTableModel.ACTION + ","
						+ TradelogDetailTableModel.STOP_PRICE + ","
						+ TradelogDetailTableModel.STATUS + ","
						+ TradelogDetailTableModel.FILLED_DATE + ","
						+ TradelogDetailTableModel.QUANTITY + ","
						+ TradelogDetailTableModel.AVG_FILL_PRICE + ","
						+ TradelogDetailTableModel.COMMISION + ","
						+ TradelogDetailTableModel.PROFIT_LOSS);
				// Write out the lines
				if (null != m_tradelogReport) {
					for (TradelogDetail tradelogDetail : m_tradelogReport
							.getTradelogDetail()) {
						writer.println(formatTradelogDetailLine(tradelogDetail));
					}
				}
				writer.flush();
				writer.close();
				fileWriter.close();
				this.setStatusBarMessage("File: " + fileName + " saved.",
						BasePanel.INFORMATION);
			} catch (Exception ex) {
				setErrorMessage("Error Reading Writing.", ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Method formatTradelogSummaryLine.
	 * 
	 * @param tradelogSummary
	 *            TradelogSummary
	 * @return StringBuffer
	 */
	private StringBuffer formatTradelogSummaryLine(
			final TradelogSummary tradelogSummary) {
		StringBuffer tradelogLine = new StringBuffer();
		tradelogLine.append((tradelogSummary.getPeriod() == null ? ""
				: tradelogSummary.getPeriod())
				+ ","
				+ (tradelogSummary.getBattingAverage() == null ? ""
						: new Money(tradelogSummary.getBattingAverage()))
				+ ","
				+ (tradelogSummary.getSimpleSharpeRatio() == null ? ""
						: new Money(tradelogSummary.getSimpleSharpeRatio()))
				+ ","
				+ (tradelogSummary.getGrossProfitLoss() == null ? ""
						: tradelogSummary.getGrossProfitLoss())
				+ ","
				+ (tradelogSummary.getQuantity() == null ? "" : tradelogSummary
						.getQuantity())
				+ ","
				+ (tradelogSummary.getCommission() == null ? ""
						: tradelogSummary.getCommission())
				+ ","
				+ (tradelogSummary.getNetProfitLoss() == null ? ""
						: tradelogSummary.getNetProfitLoss())
				+ ","
				+ (tradelogSummary.getWinCount() == null ? "" : tradelogSummary
						.getWinCount())
				+ ","
				+ (tradelogSummary.getProfitAmount() == null ? ""
						: tradelogSummary.getProfitAmount())
				+ ","
				+ (tradelogSummary.getLossCount() == null ? ""
						: tradelogSummary.getLossCount())
				+ ","
				+ (tradelogSummary.getLossAmount() == null ? ""
						: tradelogSummary.getLossAmount())
				+ ","
				+ (tradelogSummary.getPositionCount() == null ? ""
						: tradelogSummary.getPositionCount())
				+ ","
				+ (tradelogSummary.getTradestrategyCount() == null ? ""
						: tradelogSummary.getTradestrategyCount()));

		return tradelogLine;
	}

	/**
	 * Method formatTradelogDetailLine.
	 * 
	 * @param tradelogDetail
	 *            TradelogDetail
	 * @return StringBuffer
	 */
	private StringBuffer formatTradelogDetailLine(
			final TradelogDetail tradelogDetail) {
		StringBuffer tradelogLine = new StringBuffer();
		tradelogLine.append(tradelogDetail.getOpen()
				+ ","
				+ (tradelogDetail.getSymbol() == null ? "" : tradelogDetail
						.getSymbol())
				+ ","
				+ (tradelogDetail.getLongShort() == null ? "" : tradelogDetail
						.getLongShort())
				+ ","
				+ (tradelogDetail.getTier() == null ? "" : tradelogDetail
						.getTier())
				+ ","
				+ (tradelogDetail.getMarketBias() == null ? ""
						: ("\'" + tradelogDetail.getMarketBias()))
				+ ","
				+ (tradelogDetail.getMarketBar() == null ? ""
						: ("\'" + tradelogDetail.getMarketBar()))
				+ ","
				+ (tradelogDetail.getName() == null ? "" : tradelogDetail
						.getName())
				+ ","
				+ (tradelogDetail.getStatus() == null ? "" : tradelogDetail
						.getStatus())
				+ ","
				+ (tradelogDetail.getAction() == null ? "" : tradelogDetail
						.getAction())
				+ ","
				+ (tradelogDetail.getStopPrice() == null ? "" : tradelogDetail
						.getStopPrice())
				+ ","
				+ (tradelogDetail.getOrderStatus() == null ? ""
						: tradelogDetail.getOrderStatus())
				+ ","
				+ (tradelogDetail.getFilledDate() == null ? "" : tradelogDetail
						.getFilledDate())
				+ ","
				+ (tradelogDetail.getQuantity() == null ? "" : tradelogDetail
						.getQuantity())
				+ ","
				+ (tradelogDetail.getAverageFilledPrice() == null ? ""
						: tradelogDetail.getAverageFilledPrice())
				+ ","
				+ (tradelogDetail.getCommission() == null ? "" : tradelogDetail
						.getCommission())
				+ ","
				+ (tradelogDetail.getProfitLoss() == null ? "" : tradelogDetail
						.getProfitLoss()));

		return tradelogLine;
	}
}
