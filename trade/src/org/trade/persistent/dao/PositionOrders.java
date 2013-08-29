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
package org.trade.persistent.dao;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.trade.core.dao.Aspect;
import org.trade.dictionary.valuetype.Side;

/**
 * TradestrategyContract generated by hbm2java
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
@Entity
@Table(name = "tradestrategy")
public class PositionOrders extends Aspect implements Serializable {

	private static final long serialVersionUID = -2181676329258092177L;

	private ContractLite contract;
	private String status;
	private Date lastUpdateDate;
	private TradePosition tradePosition;
	private List<TradeOrder> tradeOrders = new ArrayList<TradeOrder>(0);

	public PositionOrders() {
	}

	/**
	 * Method getIdTradeStrategy.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idTradeStrategy", unique = true, nullable = false)
	public Integer getIdTradeStrategy() {
		return this.id;
	}

	/**
	 * Method setIdTradeStrategy.
	 * 
	 * @param idTradeStrategy
	 *            Integer
	 */
	public void setIdTradeStrategy(Integer idTradeStrategy) {
		this.id = idTradeStrategy;
	}

	/**
	 * Method getContract.
	 * 
	 * @return ContractId
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
	@JoinColumn(name = "idContract", insertable = false, updatable = false, nullable = false)
	public ContractLite getContract() {
		return this.contract;
	}

	/**
	 * Method setContract.
	 * 
	 * @param contract
	 *            ContractId
	 */
	public void setContract(ContractLite contract) {
		this.contract = contract;
	}

	/**
	 * Method getStatus.
	 * 
	 * @return String
	 */
	@Column(name = "status", length = 20)
	public String getStatus() {
		return this.status;
	}

	/**
	 * Method setStatus.
	 * 
	 * @param status
	 *            String
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Method getLastUpdateDate.
	 * 
	 * @return Date
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastUpdateDate", nullable = false, length = 19)
	public Date getLastUpdateDate() {
		return this.lastUpdateDate;
	}

	/**
	 * Method setLastUpdateDate.
	 * 
	 * @param lastUpdateDate
	 *            Date
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	/**
	 * Method getTradeOrders.
	 * 
	 * @return List<Trade>
	 */
	@OneToMany(mappedBy = "tradestrategy", fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
	public List<TradeOrder> getTradeOrders() {
		return this.tradeOrders;
	}

	/**
	 * Method setTradeOrders.
	 * 
	 * @param tradeOrders
	 *            List<TradeOrder>
	 */
	public void setTradeOrders(List<TradeOrder> tradeOrders) {
		this.tradeOrders = tradeOrders;
	}

	/**
	 * Method addTradeOrder.
	 * 
	 * @param tradeOrders
	 *            TradeOrder
	 */
	public void addTradeOrder(TradeOrder tradeOrders) {
		this.tradeOrders.add(tradeOrders);
	}

	/**
	 * Method getOpenTradePosition.
	 * 
	 * @return TradePosition
	 */
	@Transient
	public TradePosition getOpenTradePosition() {
		return this.tradePosition;
	}

	/**
	 * Method setOpenTradePosition.
	 * 
	 * @param tradePosition
	 *            TradePosition
	 */
	public void setOpenTradePosition(TradePosition tradePosition) {
		this.tradePosition = tradePosition;
	}

	/**
	 * Method hasOpenTradePosition.
	 * 
	 * @return boolean
	 */
	public boolean hasOpenTradePosition() {
		if (null == getOpenTradePosition())
			return false;
		return true;
	}

	/**
	 * Method getUnRealizedProfit.
	 * 
	 * @param lastPrice
	 *            BigDecimal
	 * @return BigDecimal
	 */
	@Transient
	public BigDecimal getUnRealizedProfit(BigDecimal lastPrice) {
		double unRealizedProfit = 0;

		Integer prevIdTradePosition = null;

		for (TradeOrder order : getTradeOrders()) {
			if (order.getIsFilled()) {
				if (null == prevIdTradePosition
						|| prevIdTradePosition != order.getTradePosition()
								.getIdTradePosition()) {

					prevIdTradePosition = order.getTradePosition()
							.getIdTradePosition();
					if (order.getTradePosition().getIsOpen()) {
						unRealizedProfit = order.getTradePosition()
								.getTotalNetValue().doubleValue()
								+ (order.getTradePosition().getOpenQuantity() * lastPrice
										.doubleValue());

					}
				}
			}
		}
		return new BigDecimal(unRealizedProfit);
	}

	/**
	 * Method getRealizedProfit.
	 * 
	 * @return BigDecimal
	 */
	@Transient
	public BigDecimal getRealizedProfit() {
		double realizedProfit = 0;
		Integer prevIdTradePosition = null;

		for (TradeOrder order : getTradeOrders()) {

			if (order.getIsFilled()) {
				if (null == prevIdTradePosition
						|| prevIdTradePosition != order.getTradePosition()
								.getIdTradePosition()) {

					prevIdTradePosition = order.getTradePosition()
							.getIdTradePosition();

					if (order.getTradePosition().getTotalSellQuantity()
							.doubleValue() > 0
							&& order.getTradePosition().getTotalBuyQuantity()
									.doubleValue() > 0) {

						double qty = (order.getTradePosition()
								.getTotalSellQuantity().doubleValue() - order
								.getTradePosition().getTotalBuyQuantity()
								.doubleValue());
						if (qty == 0) {
							realizedProfit = realizedProfit
									+ order.getTradePosition()
											.getTotalNetValue().doubleValue()
									- order.getTradePosition()
											.getTotalCommission().doubleValue();
						} else {

							double avgBuy = (order.getTradePosition()
									.getTotalBuyValue().doubleValue() / order
									.getTradePosition().getTotalBuyQuantity()
									.doubleValue());

							double avgSell = (order.getTradePosition()
									.getTotalSellValue().doubleValue() / order
									.getTradePosition().getTotalSellQuantity()
									.doubleValue());

							int sideVal = (Side.BOT.equals(order
									.getTradePosition().getSide()) ? -1 : 1);
							realizedProfit = realizedProfit
									+ (qty * (avgSell - avgBuy) * sideVal)
									- order.getTradePosition()
											.getTotalCommission().doubleValue();
						}
					}
				}
			}
		}
		return new BigDecimal(realizedProfit);
	}

	/**
	 * Method getCommision.
	 * 
	 * @return BigDecimal
	 */
	@Transient
	public BigDecimal getCommision() {
		double commision = 0;

		Integer prevIdTradePosition = null;

		for (TradeOrder order : getTradeOrders()) {

			if (order.getIsFilled()) {
				if (null == prevIdTradePosition
						|| prevIdTradePosition != order.getTradePosition()
								.getIdTradePosition()) {

					commision = commision
							+ order.getTradePosition().getTotalCommission()
									.doubleValue();

					prevIdTradePosition = order.getTradePosition()
							.getIdTradePosition();
				}
			}
		}
		return new BigDecimal(commision);
	}

	/**
	 * Method getNetValue.
	 * 
	 * @return BigDecimal
	 */
	@Transient
	public BigDecimal getNetValue() {
		double netValue = 0;

		Integer prevIdTradePosition = null;

		for (TradeOrder order : getTradeOrders()) {

			if (order.getIsFilled()) {
				if (null == prevIdTradePosition
						|| prevIdTradePosition != order.getTradePosition()
								.getIdTradePosition()) {
					netValue = netValue
							+ order.getTradePosition().getTotalNetValue()
									.doubleValue();

					prevIdTradePosition = order.getTradePosition()
							.getIdTradePosition();
				}
			}
		}
		return new BigDecimal(netValue);
	}

	/**
	 * Method getVersion.
	 * 
	 * @return Integer
	 */
	@Version
	@Column(name = "version")
	public Integer getVersion() {
		return this.version;
	}

	/**
	 * Method setVersion.
	 * 
	 * @param version
	 *            Integer
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
}
