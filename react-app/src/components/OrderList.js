import React from 'react'
import PropTypes from 'prop-types'
import Order from './Order'

const OrderList = ({ orders, deliverOrder }) => (
  <ul>
    {orders.map(order =>
	  <Order
		  key={order.id}
		  {...order}
		  onClick={() => deliverOrder(order.id)} />
    )}
  </ul>
)

OrderList.propTypes = {
  orders: PropTypes.arrayOf(
    PropTypes.shape({
	  id: PropTypes.number.isRequired,
	  customerId: PropTypes.number.isRequired,
	  status: PropTypes.string.isRequired
    }).isRequired
  ).isRequired,
  deliverOrder: PropTypes.func.isRequired
}

export default OrderList